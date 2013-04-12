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
	override def toString() = ("level=%s, fileSize=%s, maxFiles=%s").format(level, fileSize, maxFiles) 		
}

class LoggerConfigBuilder extends Config[LoggerConfig]
{
	var level:String = "INFO"
	var fileSize:Int = 1 // MB
	var maxFiles:Int = 10

	def apply() = LoggerConfig(level, fileSize, maxFiles)
}

trait DataStoreConfig

case class RepositoryConfig(handler:Repository, dataStores:List[DataStoreConfig]) 
{
	override def toString() = ("handler=[%s], configs=%s").format(handler, dataStores.map( c => "[%s %s]".format(c.getClass.getSimpleName, c.toString) )) 		
}

class RepositoryConfigBuilder extends Config[RepositoryConfig]
{
	var handler:Repository = null
	var dataStores:List[DataStoreConfig] = null

	def apply() = RepositoryConfig(handler, dataStores)
}


/*
case class WebAppConfig(enabled:Boolean, path:String, client:WebClientConfig) 
{
	override def toString() = ("enabled=%s, path=%s").format(enabled, path) 		
}

class WebAppConfigBuilder extends Config[WebAppConfig]
{
	var enabled:Boolean = false
	var path:String = null
	var client:WebClientConfig = null

	def apply() = WebAppConfig(enabled, path, client)
}
*/

/*
case class WebClientConfig
{
}
*/

/*
case class CommunicationConfig(agents:List[InternalCommunicationConfig])
{
	override def toString() = agents.map("[" + _.toString + "]").reduceLeft(_ + "," + _) 
}

class CommunicationConfigBuilder extends Config[CommunicationConfig]
{
	var agents:List[InternalCommunicationConfig] = null

	def apply() = CommunicationConfig(agents)
}

trait InternalCommunicationConfig
{
}
*/

trait WebServerConfig

case class JettyConfig(path:String, host:String, port:Int, sslPort:Int, keystoreFile:String, keystorePassword:String) extends WebServerConfig
{
	override def toString() = ("path=%s, host=%s, port=%s, sslPort=%s, keystoreFile=%s").format(path, host, port, sslPort, keystoreFile)
}

trait WebappHandlerConfig
{
	var handler:WebappHandler
	var path:String
	var config:Any
}

case class WebappConfig(embeddedServer:Option[WebServerConfig], restPath:String, handlers:List[WebappHandlerConfig]) 
{
	override def toString() = ("embeddedServer=[%s], restPath=%s, handlers=%s").format(embeddedServer, restPath, handlers.map( h => "[%s %s]".format(h.getClass.getSimpleName, h.toString) ))
}

class WebAppConfigBuilder extends Config[WebappConfig]
{
	var enbeddedWebServer:Option[WebServerConfig] = Some(JettyConfig("/", "127.0.0.1", 8080, 8443, "shaft.pkcs12", "shaft"))
	var restPath = "rest"
	var handlers = List.empty[WebappHandlerConfig]
	
	def apply() = WebappConfig(enbeddedWebServer, restPath, handlers)
}

// rollup
/*
protected final object ShaftConfig
{
	def apply(configurator:ShaftServerConfigurator[_,_]):ShaftConfig = 
	{
		new ShaftConfig(configurator.storage(), 
						configurator.web(),
						configurator.rest(),
						configurator.bayeux())
	}
}
*/

protected final case class ShaftConfig(	repository:RepositoryConfig, 
										webapp:WebappConfig)
{
	override def toString() = ("repository=[%s]\nwebapp=[%s]").format(repository, webapp)		
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

		

