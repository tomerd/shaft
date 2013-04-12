package com.mishlabs.shaft
package services
package webapp

import scala.collection._
import javax.servlet._
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import com.google.inject.Inject
import services._
import handlers._
import config._
import util._
import com.mishlabs.shaft.services.webapp.handlers.rest.RestHandler

trait WebappService extends Service
{
	val servlets:Map[String, ServletInfo]
}

class ShaftWebappService extends ShaftService with WebappService
{
	@Inject var config:WebappConfig = null
	
	private var embeddedServer:Option[WebServer[_]] = None;
		
	val servlets = mutable.HashMap[String, ServletInfo]()
	
	def startup
	{
		info("web service starting up")
		
		servlets += config.restPath -> RestHandler.getServlet(None)
		
		config.handlers.foreach( handlerConfig =>
		{
			servlets += handlerConfig.path -> handlerConfig.handler.getServlet(handlerConfig.config)			
		})
		
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
}

class WebServerContextListener extends ServletContextListener
{
	// TODO: find a better way to do this, services are singltones
	val webappService = 
	{
		ServiceManager.services.find( _.isInstanceOf[WebappService] ) match
		{
		  	case Some(webappService:WebappService) => webappService
		  	case _ => throw new Exception("webapp service not initialized")
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
			    	webappService.servlets.find{ case(path, info) => matches(request.getRequestURI, path) } match
					{
			    	  	case Some((path, info)) => info.servlet.service(request, response)
			    	  	case _ => response.asInstanceOf[HttpServletResponse].sendError(404);
					}
			    }
			  	case _ => response.asInstanceOf[HttpServletResponse].sendError(400);
			}
		}
		
		def matches(uri:String, path:String):Boolean =
		{	
			if (null == uri || null == path) return false
			uri.split("/")(1).toLowerCase == path.toLowerCase 
		}
	}
}


