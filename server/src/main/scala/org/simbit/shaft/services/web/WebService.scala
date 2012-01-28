package org.simbit.shaft
package services
package web

import scala.collection._

import org.eclipse.jetty.server.{Server, Connector, Handler}
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.handler.{ ContextHandler, HandlerList, ResourceHandler, DefaultHandler }
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder

import com.google.inject.Inject

import services._
import config._

trait WebService extends Service
{
	// FIXME: these are an abstraction leak
	def registerHandler(handler:Handler)
	
	def onStart(callback:()=>Unit)
}

class ShaftWebService extends ShaftService with WebService
{
	@Inject var config:WebConfig = null
	
	private var server:Server = null
	
	private val additioalHandlers = mutable.ListBuffer[Handler]()	
	
	private val onstartCallbacks = mutable.ListBuffer[()=>Unit]()
	
	def startup()
	{
		info("web service starting up")		
		
		try
		{
			server = new Server()
	
	        val connector = new SelectChannelConnector()	        
	        connector.setHost(config.host)
	        connector.setPort(config.port)
	        server.addConnector(connector)
				        
	        val handlers = new HandlerList()
			server.setHandler(handlers)
			
			additioalHandlers.foreach { handlers.addHandler(_) }
			
			//val context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS)
			//context.setContextPath(config.path + "/scripts/element")		
			//context.addServlet(new ServletHolder(new ConfigurationServlet()), "/*")
			//web.registerHandler(context)
			
			val context = new ContextHandler()
			context.setContextPath(config.path)
			
			val handler = new HandlerList()
			val url = ShaftServer.server.getClass.getClassLoader.getResource("web/public")			
			if (null == url) throw new Exception("webapp directory not found. expected at web/public")
			val resourceHandler = new ResourceHandler()			
	        resourceHandler.setDirectoriesListed(false)
	        resourceHandler.setWelcomeFiles(Array("index.htm", "index.html"))	 
	        resourceHandler.setResourceBase(url.toExternalForm)
	        //web.registerHandler(resourceHandler)			    
	        //web.registerHandler(new DefaultHandler())	
	        handler.addHandler(resourceHandler)
	        handler.addHandler(new DefaultHandler())
	        context.setHandler(handler)
	        handlers.addHandler(context)
			
			
			
	        server.start
	        			
	        onstartCallbacks.foreach( _() )
	        
	        info("embedded web server is started. listening on %s:%s".format(config.host, config.port))
		}
		catch
		{
		  	case e => throw new Exception("failed starting embedded web server: " + e.getMessage , e)
		}
        
		info("web service is up")
	}
		
	def shutdown()
	{
		info("web service shutting down")
	  			
		additioalHandlers.clear
		onstartCallbacks.clear
		
		try
		{
			if (null != server) server.stop
		}
		catch
		{
		  	case e => reportError("failed stopping embedded web server", e)
		}
		
		info("web service is down") 
	}
	
	def registerHandler(handler:Handler)
	{
		additioalHandlers += handler
	}
		
	def onStart(callback:()=>Unit)
	{
		onstartCallbacks += callback
	}
		
}




