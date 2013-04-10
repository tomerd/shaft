package com.mishlabs.shaft
package services
package web

import scala.collection._

import javax.servlet._
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import com.google.inject.Inject

import services._
import config._
import util._

case class ServletInfo(name:String, path:String, initParams:Map[String,String], servlet:Servlet)

trait WebService extends Service
{  
	def registerServlet(servlet:ServletInfo)
	val servlets:Seq[ServletInfo]
}

class ShaftWebService extends ShaftService with WebService
{
	@Inject var config:WebConfig = null
	
	private var embeddedServer:Option[WebServer[_]] = None;
		
	val servlets = mutable.ListBuffer[ServletInfo]()
	
	def startup
	{
		info("web service starting up")	
		
		config.embeddedServer match
		{
		  	case Some(jettyConfig:JettyConfig) =>
		  	{
		  		val server:JettyServer = new JettyServer(jettyConfig)
		  		embeddedServer = Some(server)
		  	}
		  	case _ =>
		  	{
		  		throw new Exception("unknown embedded web server")
		  	}
		}
		
		try
		{
			if (embeddedServer.isDefined) embeddedServer.get.startup
			
	        //onstartCallbacks.foreach( _() )
	        
	        info("embedded web server is started")
		}
		catch
		{
		  	case e => throw new Exception("failed starting embedded web server: " + e.getMessage , e)
		}
        
		info("web service is up")
	}
		
	def shutdown
	{
		info("web service shutting down")
				
		try
		{
			if (embeddedServer.isDefined) embeddedServer.get.shutdown
		}
		catch
		{
		  	case e => reportError("failed stopping embedded web server", e)
		}
		
		servlets.clear
		
		info("web service is down") 
	}
	
	def registerServlet(servlet:ServletInfo)
	{
		servlets += servlet
	}
}

class WebServerContextListener extends ServletContextListener
{
	val webService = 
	{
		ServiceManager.services.find( _.isInstanceOf[WebService] ) match
		{
		  	case Some(webService:WebService) => webService
		  	case _ => throw new Exception("web service not initialized")
		}
	}
  
	def contextInitialized(contextEvent:ServletContextEvent) 
	{
		val context = contextEvent.getServletContext();
		//context.addFilter("/router", new RouterServlet)
				
		// FIXME, not sure why the generic addFilter is not working
		// this will not work for other javax containers (jetty only code)!
		context match
		{
		  	case webapp:org.eclipse.jetty.webapp.WebAppContext#Context =>
		  	{
		  		val handler = webapp.getContextHandler.asInstanceOf[org.eclipse.jetty.webapp.WebAppContext]
		  		val holder = new org.eclipse.jetty.servlet.FilterHolder
		  		holder.setFilter(new Router)
		  	    handler.addFilter(holder, "/*", null)
		  	}
		  	case other => throw new Exception("unknown web server context: " +  other.getClass().toString())		  	
		}
		
		/*
		ServiceManager.services.find( _.isInstanceOf[WebService] ) match
		{
		  	case Some(webService:WebService) =>
		  	{
		  		val context = contextEvent.getServletContext();
		  		context.addFilter("router", new RouterServlet)
		  		/*
		  		webService.servlets.foreach( servlet => 
		  		{
		  		    val registration:ServletRegistration = context.addServlet(servlet.name, servlet.servlet)
		  		    servlet.initParams.foreach({ case(name, value) => registration.setInitParameter(name, value) })		  		    
		  		})*/
		  	}
		  	case _ => throw new Exception("web service not initialized")
		}
		*/
	}
	
	def contextDestroyed(contextEvent:ServletContextEvent) 
	{
	}
	
	private class Router extends Filter with Logger
	{
		def init(config:FilterConfig) {}		
		def destroy {}
	  
		def doFilter(request:ServletRequest, response:ServletResponse, chain:FilterChain)
		{
			request match
			{
			  	case request:HttpServletRequest =>
			    {
			    	webService.servlets.find( info => request.getRequestURI.toLowerCase.indexOf(info.path.toLowerCase) == 0) match
					{
			    	  	case Some(info) => info.servlet.service(request, response)
			    	  	case _ => response.asInstanceOf[HttpServletResponse].sendError(404);
					}
			    }
			  	case _ => response.asInstanceOf[HttpServletResponse].sendError(400);
			}
		}
	}
}


