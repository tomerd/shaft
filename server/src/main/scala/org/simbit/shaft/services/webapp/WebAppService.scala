package org.simbit.shaft
package services
package webapp

import scala.collection._

import org.eclipse.jetty.server.handler._
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder

import com.google.inject.Inject

import config._

import services.web.WebService

trait WebAppService extends Service
{
}

class ShaftWebAppService extends ShaftService with WebAppService
{
	@Inject var config:WebAppConfig = null
	@Inject var web:WebService = null
  
	def startup()
	{
	  	if (!config.enabled)
	  	{
	  		info("web app service disabled")
	  		return
	  	}
	  	
		info("web app service starting up")		
		
		try
		{
			val context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS)
			context.setContextPath(config.path + "/scripts/element")		
			context.addServlet(new ServletHolder(new ConfigurationServlet()), "/*")
			web.registerHandler(context)
			
			val context2 = new ContextHandler()
			context2.setContextPath(config.path)
			
			val handler = new HandlerList()
			val url = classOf[ShaftServer[_]].getClassLoader.getResource("webapp")
			if (null == url) throw new Exception("webapp directory not found. expected at /webapp")
			val resourceHandler = new ResourceHandler()			
	        resourceHandler.setDirectoriesListed(false)
	        resourceHandler.setWelcomeFiles(Array("index.html"))	 
	        resourceHandler.setResourceBase(url.toExternalForm)
	        //web.registerHandler(resourceHandler)			    
	        //web.registerHandler(new DefaultHandler())	
	        handler.addHandler(resourceHandler)
	        handler.addHandler(new DefaultHandler())
	        context2.setHandler(handler)
	        web.registerHandler(context2)
		}
		catch
		{
		  	case e => throw new Exception("failed starting web app server: " + e.getMessage , e)
		}
        
		info("web app service is up")
	}
		
	def shutdown()
	{
		if (!config.enabled) return;
	  
		info("web app service shutting down")
		
		try
		{
		}
		catch
		{
		  	case e => reportError("failed stopping web app server", e)
		}
		
		info("web app service is down") 
	}
	
	private class ConfigurationServlet extends javax.servlet.http.HttpServlet
	{
		import javax.servlet.http.HttpServletRequest;
	  	import javax.servlet.http.HttpServletResponse;

	  	import java.io._

		override protected def service(request:HttpServletRequest, response:HttpServletResponse)
		{               
	  		try
	  		{
	  			var path = request.getPathInfo
	  			if (null == path || path.length == 0 || "/".equals(path)) throw new UnknownRequest()
	  			
	  			if (!"GET".equals(request.getMethod)) throw new UnknownRequest()
						
	  			if (path.startsWith("/")) path = path.substring(1)
	  			val parts = path.split("/")
        
	  			if ("configuration.js".equals(parts(0)))
	  			{
	  				response.setContentType("text/javascript")
                                
	  				val writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream))
	  				writer.write(this.configurationAsJavaScript)
	  				writer.flush
	  				writer.close
				}
	  			else
	  			{
	  				throw new UnknownRequest()
	  			}
	  		}
	  		catch
	  		{
	  		  	case e:UnknownRequest => response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unknown request")
	  		  	case e => response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage)
	  		}
		}
		
		private lazy val configurationAsJavaScript:String =
		{
			val builder = new StringBuilder
			
			builder.append("/*\n")
			builder.append("*** this file was generated on %s\n".format(new java.util.Date()))
			builder.append("*/\n")
			
			builder.append("\n")
				
		  	builder.toString
		}
		
		class UnknownRequest extends Throwable
	}
}




