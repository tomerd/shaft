package com.mishlabs.shaft
package config

import scala.collection._
import com.twitter.util.Config
import com.twitter.ostrich.admin.RuntimeEnvironment
import com.twitter.ostrich.admin.config.ServerConfig
import com.mishlabs.shaft.ShaftServer
import com.mishlabs.shaft.repository.Repository

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

case class RepositoryConfig(handler:Repository, configuration:List[DataStoreConfig]) 
{
	override def toString() = ("handler=[%s], configuration=[%s]").format(handler, configuration) 		
}

class RepositoryConfigBuilder extends Config[RepositoryConfig]
{
	var handler:Repository = null
	var configuration:List[DataStoreConfig] = null

	def apply() = RepositoryConfig(handler, configuration)
}

trait DataStoreConfig

trait WebServerConfig

case class JettyConfig(path:String, host:String, port:Int, sslPort:Int, keystoreFile:String, keystorePassword:String) extends WebServerConfig
{
	override def toString() = ("path=%s, host=%s, port=%s, sslPort=%s, keystoreFile=%s").format(path, host, port, sslPort, keystoreFile)
}

case class WebConfig(embeddedServer:Option[WebServerConfig]) 
{
	override def toString() = ("embeddedServer=[%s]").format(embeddedServer)
}

class WebConfigBuilder extends Config[WebConfig]
{
	/*
	var path:String = "/"
	var host:String = "127.0.0.1"
	var port:Int = 8080
	var sslPort:Int = 8443
	var keystoreFile = "shaft.pkcs12"
	var keystorePassword = "shaft"
	*/  

	def apply() = WebConfig(Some(JettyConfig("/", "127.0.0.1", 8080, 8443, "shaft.pkcs12", "shaft")))
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

case class RestConfig(enabled:Boolean, path:String) //extends InternalCommunicationConfig
{
	override def toString() = ("enabled=%s, path=%s").format(enabled, path)
}

class RestConfigBuilder extends Config[RestConfig]
{
	var enabled:Boolean = true
	var path:String = "/rest"

	def apply() = RestConfig(enabled, path)
}

case class BayeuxConfig(enabled:Boolean, path:String, requestChannel:String, responseChannel:String) //extends InternalCommunicationConfig
{
	override def toString() = ("enabled=%s, path=%s, requestChannel=%s, responseChannel=%s").format(enabled, path, requestChannel, responseChannel)
}

class BayeuxConfigBuilder extends Config[BayeuxConfig]
{
	var enabled:Boolean = false
	var path:String = "/bayeux"
	var requestChannel:String = "/request"
	var responseChannel:String = "/response"

	def apply() = BayeuxConfig(enabled, path, requestChannel, responseChannel)
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
										web:WebConfig,  
										rest:RestConfig, 
										bayeux:BayeuxConfig)
{
	override def toString() = ("repository=[%s]\nweb=[%s]\nrest=[%s]\nbayeux=[%s]").format(repository, web, rest, bayeux)		
}

protected abstract class ShaftServerConfiguration(val shaftConfig:ShaftConfig)

protected trait ShaftServerConfigurator[S <: ShaftServer[C], C <: ShaftServerConfiguration] extends ServerConfig[S] 
{ 
	final val logger = new LoggerConfigBuilder()
	final val repository = new RepositoryConfigBuilder()
	final val web = new WebConfigBuilder()
	final val rest = new RestConfigBuilder()
	final val bayeux = new BayeuxConfigBuilder()
	
	private var _shaftConfig:ShaftConfig = null
	final def shaftConfig = _shaftConfig
	
	final def apply(runtime:RuntimeEnvironment) = 
	{
	  	import com.mishlabs.shaft.util.LoggerConfigurator
	  	import org.apache.log4j.Level
	  	
		val loggerConfig = logger()
		LoggerConfigurator.configure(Level.toLevel(loggerConfig.level), loggerConfig.fileSize, loggerConfig.maxFiles)
		
		_shaftConfig = new ShaftConfig(repository(), web(), rest(), bayeux())
			
		this.createServer(this.buildConfiguration())
	}
	
	final def reload(server:S) 
	{
		server.reload(this.buildConfiguration())
	}
		
	protected def createServer(config:C):S
	
	protected def buildConfiguration():C
	
}

		

