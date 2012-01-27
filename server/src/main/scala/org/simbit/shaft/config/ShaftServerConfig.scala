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
	var level:String = null
	var fileSize:Int = 0
	var maxFiles:Int = 0

	def apply() = LoggerConfig(level, fileSize, maxFiles)
}

/*
sealed abstract class StorageAdapter(name:String)

object StorageAdapter 
{
	case object SQUERYL extends StorageAdapter("SQUERYL")
	case object MEMORY extends StorageAdapter("MEMORY")
}
*/
case class StorageConfig(store:dal.DataStore, configuration:DataStoreConfig) 
{
	override def toString() = ("store=%s, configuration=[%s]").format(store, configuration) 		
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

/*
class RdbmsStorageConfigBuilder extends Config[RdbmsStorageConfig]
{
	var driver:String = null
	var url:String = null
	var user:String = null
	var password:String = null

	def apply() = RdbmsStorageConfig(driver, url, user, password)
}
*/

case class WebConfig(host:String, port:Int) 
{
	override def toString() = ("host=%s, port=%s").format(host, port)
}

class WebConfigBuilder extends Config[WebConfig]
{
	var host:String = null
	var port:Int = 0

	def apply() = WebConfig(host, port)
}

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

case class WebClientConfig
{
}

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

case class RestConfig(enabled:Boolean, path:String/*, routes:org.simbit.shaft.routes.Routes*/) //extends InternalCommunicationConfig
{
	override def toString() = ("enabled=%s, path=%s").format(enabled, path/*, routes*/)
}

class RestConfigBuilder extends Config[RestConfig]
{
	var enabled:Boolean = false
	var path:String = null
	// FIXME: find a better way to do this
	// just to force initializing the routes class
	var routes:AnyRef = null
	//var views:AnyRef = null

	def apply() = RestConfig(enabled, path/*, routes*/)
}

case class BayeuxConfig(enabled:Boolean, path:String, requestChannel:String, responseChannel:String) //extends InternalCommunicationConfig
{
	override def toString() = ("enabled=%s, path=%s, requestChannel=%s, responseChannel=%s").format(enabled, path, requestChannel, responseChannel)
}

class BayeuxConfigBuilder extends Config[BayeuxConfig]
{
	var enabled:Boolean = false
	var path:String = null
	var requestChannel:String = null
	var responseChannel:String = null

	def apply() = BayeuxConfig(enabled, path, requestChannel, responseChannel)
}



// rollup
abstract class ShaftServerConfiguration(val storage:StorageConfig, 
										val web:WebConfig, 
										val webapp:WebAppConfig, 
										val rest:RestConfig, 
										val bayeux:BayeuxConfig) //extends Configuration
{
	//override def toString() = ("storage=[%s]\nweb=[%s]\nwebapp=[%s]\nrest=[%s]\nbayeux=[%s]").format(storage, web, webapp, rest, bayeux) 		
}

trait ShaftServerConfigurator[S <: ShaftServer[C], C <: ShaftServerConfiguration] extends ServerConfig[S] 
{ 
	final protected val logger = new LoggerConfigBuilder
	final protected val storage = new StorageConfigBuilder 
	final protected val web = new WebConfigBuilder
	final protected val webapp = new WebAppConfigBuilder
	final protected val rest = new RestConfigBuilder
	final protected val bayeux = new BayeuxConfigBuilder
	
	final def apply(runtime:RuntimeEnvironment) = 
	{
	  	import org.simbit.shaft.util.LoggerConfigurator
	  	import org.apache.log4j.Level
	  	
		val loggerConfig = logger()
		LoggerConfigurator.configure(Level.toLevel(loggerConfig.level), loggerConfig.fileSize, loggerConfig.maxFiles)
	
		this.createServer(this.buildConfiguration())
	}
	
	final def reload(server:S) 
	{
		server.reload(this.buildConfiguration())
	}
		
	def createServer(config:C):S
	
	def buildConfiguration():C
	
}

		

