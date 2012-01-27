package org.simbit.shaft

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

trait ShaftServerLoader[T <: ShaftServer[_]] extends Logger 
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

protected abstract class ShaftServer[C <: ShaftServerConfiguration](config:C) extends Service with Logger
{
	private val startTime = System.currentTimeMillis

	final def uptime = System.currentTimeMillis - startTime
	
	private val bus = new MessageBus()  	
	//private var shuttingDown = false
	
	protected val name:String
		
	final override def start()
	{
		info("--------------------------------------------------------------------------------------------------------")
		info("%s server starting with configuration:".format(name))			
		info(config.toString)
		
		messageHandler.start		
		bus.subscribe(classOf[Error], messageHandler)
				
		ServiceManager.startup(bus, config)

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


