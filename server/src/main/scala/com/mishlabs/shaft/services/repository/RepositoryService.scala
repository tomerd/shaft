package com.mishlabs.shaft
package services
package repository

import javax.inject.Inject
import com.mishlabs.shaft.config.RepositoryConfig
import com.mishlabs.shaft.repository.Repository
import com.mishlabs.shaft.util.StringHelpers

trait RepositoryService extends Service
{
	val repository:Option[Repository]
}

protected [services] class ShaftRepositoryService extends ShaftService with RepositoryService
{
	@Inject var config:RepositoryConfig = null
		
	lazy val repository = 
	{
		try 
		{		  
			val className = "%s.repository.%sRepository".format(ShaftServer.server.getClass.getPackage.getName,
																StringHelpers.camelify(ShaftServer.server.name))
			val klass = Class.forName(className + "$")
			if (!classOf[Repository].isAssignableFrom(klass)) throw new Exception("repository implementation was found, but could not be instantiated. make sure it is and object and inherits from Repository")
			Some( klass.getField("MODULE$").get(klass).asInstanceOf[Repository] )
		}
		catch
		{
			case e:ClassNotFoundException => { warn("repository not found, expected a scala object at repository/Repository"); None }
			case e:Exception => throw e
		} 
	}
	
	def startup
	{
		if (!repository.isDefined) return
		
		try
		{
			repository.get.initialize(config.dataStores)
		}
		catch
		{
	    	case e => throw new Exception("failed initializing repository: " + e.getMessage , e)
		}			
	}
	
	def shutdown
	{
		if (!repository.isDefined) return 
		
		try
		{
			repository.get.disconnect
		}
		catch
		{
	    	case e => reportError("failed disconnect repository: " + e.getMessage , e)
		}
	}
}