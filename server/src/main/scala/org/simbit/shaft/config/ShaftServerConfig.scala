package org.simbit.shaft
package config

import scala.collection._

import com.twitter.util.Config
import com.twitter.ostrich.admin.RuntimeEnvironment
import com.twitter.ostrich.admin.config.ServerConfig

import org.simbit.shaft.ShaftServer

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

case class StorageConfig(store:dal.DataStore, configuration:DataStoreConfig) 
{
	override def toString() = ("store=%s, configuration=[%s]").format(store.getClass.getSimpleName, configuration) 		
}

class StorageConfigBuilder extends Config[StorageConfig]
{
	var store:dal.DataStore = null
	var configuration:DataStoreConfig = null

	def apply() = StorageConfig(store, configuration)
}

trait DataStoreConfig

case class RdbmsConfig(driver:String, url:String, user:String, password:String) extends DataStoreConfig
{
	override def toString() = ("driver=%s, url=%s, user=%s").format( driver, url, user) 		
}


case class WebConfig(host:String, port:Int, path:String) 
{
	override def toString() = ("host=%s, port=%s, path=%s").format(host, port,  path)
}

class WebConfigBuilder extends Config[WebConfig]
{
	var host:String = "127.0.0.1"
	var port:Int = 8080
	var path:String = "/"

	def apply() = WebConfig(host, port, path)
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

protected final case class ShaftConfig(	storage:StorageConfig, 
										web:WebConfig,  
										rest:RestConfig, 
										bayeux:BayeuxConfig)
{
	override def toString() = ("storage=[%s]\nweb=[%s]\nrest=[%s]\nbayeux=[%s]").format(storage, web, rest, bayeux)		
}

protected abstract class ShaftServerConfiguration(val shaftConfig:ShaftConfig)

protected trait ShaftServerConfigurator[S <: ShaftServer[C], C <: ShaftServerConfiguration] extends ServerConfig[S] 
{ 
	final val logger = new LoggerConfigBuilder()
	final val storage = new StorageConfigBuilder()
	final val web = new WebConfigBuilder()
	final val rest = new RestConfigBuilder()
	final val bayeux = new BayeuxConfigBuilder()
	
	private var _shaftConfig:ShaftConfig = null
	final def shaftConfig = _shaftConfig
	
	final def apply(runtime:RuntimeEnvironment) = 
	{
	  	import org.simbit.shaft.util.LoggerConfigurator
	  	import org.apache.log4j.Level
	  	
		val loggerConfig = logger()
		LoggerConfigurator.configure(Level.toLevel(loggerConfig.level), loggerConfig.fileSize, loggerConfig.maxFiles)
		
		_shaftConfig = new ShaftConfig(storage(), web(), rest(), bayeux())
			
		this.createServer(this.buildConfiguration())
	}
	
	final def reload(server:S) 
	{
		server.reload(this.buildConfiguration())
	}
		
	protected def createServer(config:C):S
	
	protected def buildConfiguration():C
	
}

		

