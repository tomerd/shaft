package com.mishlabs.shaft
package services
package repository

import javax.inject.Inject

import com.mishlabs.shaft.config.RepositoryConfig
import com.mishlabs.shaft.repository.Repository

trait RepositoryService extends Service
{
	val repository:Option[Repository]
}

class ShaftRepositoryService extends ShaftService with RepositoryService
{
	@Inject var config:RepositoryConfig = null
	
	lazy val repository = if (null != config.handler) Some(config.handler) else None 
	
	def startup
	{
		if (!repository.isDefined) return
		
		try
		{
			repository.get.initialize(config.configuration)
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