package com.mishlabs.shaft
package config

import scala.collection._

import com.twitter.util.Config
import com.twitter.ostrich.admin.RuntimeEnvironment
import com.twitter.ostrich.admin.config.ServerConfig

import com.mishlabs.shaft.ShaftServer

import com.mishlabs.shaft.repository.Repository
import com.mishlabs.shaft.services.webapp.handlers.WebappHandler

case class LoggerConfig(level:String, fileSize:Int, maxFiles:Int)
{
	override def toString() = "level=%s, fileSize=%s, maxFiles=%s".format(level, fileSize, maxFiles) 		
}

class LoggerConfigBuilder extends Config[LoggerConfig]
{
	var level:String = "INFO"
	var fileSize:Int = 1 // MB
	var maxFiles:Int = 10

	def apply() = LoggerConfig(level, fileSize, maxFiles)
}

trait DataStoreConfig

case class RepositoryConfig(dataStores:List[DataStoreConfig]) 
{
	override def toString() = "\n\tdataStores=%s".format(dataStores.map( c => "\n\t\t[%s] %s".format(c.getClass.getSimpleName, c.toString) )) 		
}

class RepositoryConfigBuilder extends Config[RepositoryConfig]
{
	var dataStores:List[DataStoreConfig] = null

	def apply() = RepositoryConfig(dataStores)
}


case class WebServerConfig(var path:String, var host:String, var port:Int, var sslPort:Int, var keystoreFile:String, var keystorePassword:String)
{
	override def toString() = "path=%s, host=%s, port=%s, sslPort=%s, keystoreFile=%s".format(path, host, port, sslPort, keystoreFile)
}

/*
trait WebappHandlerConfig
{
	var handler:WebappHandler
	var path:String
	var config:Any
}
*/

case class WebappConfig(restPath:String, /*handlers:List[WebappHandlerConfig],*/ embeddedServer:Option[WebServerConfig]) 
{
	/*override def toString() = "\n\trestPath=%s\n\thandlers=%s\n\tembeddedServer=%s".format(	restPath, 
																							handlers.map( h => "\n\t\t[%s] %s".format(h.getClass.getSimpleName, h.toString) ), 
																							embeddedServer )*/
	override def toString() = "\n\trestPath=%s\n\tembeddedServer=%s".format(	restPath,  
																				embeddedServer )  
}

class WebAppConfigBuilder extends Config[WebappConfig]
{	
	var restPath = "rest"
	//var handlers = List.empty[WebappHandlerConfig]
	var embeddedServer:Option[WebServerConfig] = Some(WebServerConfig("/", "127.0.0.1", 8080, 8443, "shaft.pkcs12", "shaft"))
	
	def apply() = WebappConfig(restPath, /*handlers,*/ embeddedServer)
}

protected final case class ShaftConfig(	repository:RepositoryConfig, 
										webapp:WebappConfig)
{
	override def toString() = "repository%s\nwebapp%s".format(repository, webapp)		
}

protected abstract class ShaftServerConfiguration(val shaftConfig:ShaftConfig)

protected trait ShaftServerConfigurator[S <: ShaftServer[C], C <: ShaftServerConfiguration] extends ServerConfig[S] 
{ 
	final val logger = new LoggerConfigBuilder()
	final val repository = new RepositoryConfigBuilder()
	final val webapp = new WebAppConfigBuilder()
	
	private var _shaftConfig:ShaftConfig = null
	final def shaftConfig = _shaftConfig
	
	final def apply(runtime:RuntimeEnvironment) = 
	{
	  	import com.mishlabs.shaft.util.LoggerConfigurator
	  	import org.apache.log4j.Level
	  	
		_shaftConfig = new ShaftConfig(repository(), webapp())
		
		val server = this.createServer(this.buildConfiguration())
		
		val loggerConfig = logger()
		LoggerConfigurator.configure(Level.toLevel(loggerConfig.level), "%s.log".format(server.name), loggerConfig.fileSize, loggerConfig.maxFiles)
		
		server
	}
	
	final def reload(server:S) 
	{
		server.reload(this.buildConfiguration())
	}
		
	protected def createServer(config:C):S
	
	protected def buildConfiguration():C
	
}

		

