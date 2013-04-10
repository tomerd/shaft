package com.mishlabs.shaft

import java.util.Date

//import java.util.concurrent.CountDownLatch

//import scala.collection._

import scala.actors.Actor
//import scala.actors.Actor._

//import net.liftweb.common._
//import net.liftweb.util._

import com.twitter.ostrich._
import com.twitter.ostrich.admin.RuntimeEnvironment
import com.twitter.ostrich.admin.Service
import com.twitter.ostrich.admin.ServiceTracker

import lib.messaging._
import services.ServiceManager

import config._
import util._

trait ShaftServerLauncher[T <: ShaftServer[_]] extends Logger 
{
	final def main(args:Array[String]):Unit = 
	{
		try 
		{		  
			// this is required in case the server fails before the config is set
			LoggerConfigurator.default()
		  
			val runtime = RuntimeEnvironment(this, args)
			val server = runtime.loadRuntimeConfig[T]()
									
			//Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {  def run{ server.shutdown } }))
														
			server.start
		} 
		catch 
		{
			case e =>
			{				
				error("error during startup: " + ExceptionUtil.describe(e))
				System.exit(1)
			}
		}					
	}
}

protected object ShaftServer
{	
	val server:ShaftServer[_] =
	{
		// FIXME: this is an ugly hack to figure out the package name....find a better way to do this
		val services = com.twitter.ostrich.admin.ServiceTracker.peek
		services.find(!_.getClass.getPackage.getName.startsWith("com.twitter")) match
		{
			case Some(service) => service.asInstanceOf[ShaftServer[_]]
			case _ => throw new Exception("internal frameowrk error, failed deducing package name")
		}
	}			
}

protected abstract class ShaftServer[C <: ShaftServerConfiguration](config:C) extends Service with Logger
{
	private val startTime = System.currentTimeMillis

	final def uptime = System.currentTimeMillis - startTime
	
	private val bus = new MessageBus()  	
	//private var shuttingDown = false
	
	val name:String
		
	final override def start()
	{
		info("--------------------------------------------------------------------------------------------------------")
		info("%s server starting with configuration:".format(name))			
		info(config.toString)
		
		messageHandler.start		
		bus.subscribe(classOf[Error], messageHandler)
		
		loadRoutes()		
				
		ServiceManager.startup(bus, config.shaftConfig)

		info("ready for action")
		info("--------------------------------------------------------------------------------------------------------")
	}
	
	final override def shutdown()
	{		
		//if (shuttingDown) return		
		//shuttingDown = true
	  
		info("--------------------------------------------------------------------------------------------------------")
		info("%s server shutting down".format(name))
		
		messageHandler ! Stop
		bus.unsubscribeAll(messageHandler)
				
		ServiceManager.shutdown
		
		// not sure this is required, doing it to be on the safe side. need to look further into it		
		bus.clear		
		
		info("goodbye")
		info("--------------------------------------------------------------------------------------------------------")
	}

	override def quiesce() 
	{
		// TODO: implement this for real
		shutdown()
	}

	override def reload() 
	{
		info("%s server restarting".format(name))
		
		// TODO: implement this for real
		shutdown
		start
	}
	
	def reload(config:C)
	{
		//TODO: implement this for real 
	}
	
	private def loadRoutes()
	{		
		val routes = try 
		{
			val clasName = "%s.config.Routes".format(this.getClass.getPackage.getName)
			val clazz = Class.forName(clasName)
			val constructor = clazz.getConstructor()		
			clazz.getConstructor().newInstance()
		}
		catch
		{
			case e:ClassNotFoundException => throw new Exception("routes not found, expected at config/Routes")
			case _ => throw new Exception("routes class was found, but could not be instantiated. make sure it has a simple constructor")
		}
	}
	
	private val messageHandler:Actor = new Actor 
	{
		def act()
		{			
			while (true) 
			{
				receive 
				{
					case e:Error =>
					{
						error(e.description + " " + e.cause)
						Stats.incr("errors", 1)
					}
					case Stop => exit
				}
			}
		}
	}
}


