package org.simbit.shaft
package services
package web

import scala.collection._

import javax.servlet.Filter
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.FilterConfig

import org.eclipse.jetty.server.{Server, Connector, Handler}
import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.handler.{ ContextHandler, HandlerList, ResourceHandler, DefaultHandler }
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.servlet.FilterMapping
import org.eclipse.jetty.servlet.FilterHolder

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
	        
	        val keystoreFileUrl = ShaftServer.server.getClass.getClassLoader.getResource("web/%s".format(config.keystoreFile))
	        if (null == keystoreFileUrl) throw new Exception ("webapp keystore file not found. expected at web/%s".format(config.keystoreFile))
	        val sslConnector = new SslSelectChannelConnector()
			sslConnector.setHost(config.host)
			sslConnector.setPort(config.sslPort)
			sslConnector.setKeystore(keystoreFileUrl.getFile)
			sslConnector.setKeystoreType("PKCS12")
			sslConnector.setKeyPassword(config.keystorePassword)
			sslConnector.setPassword(config.keystorePassword)
			server.addConnector(sslConnector)
				        
	        val handlers = new HandlerList()
			server.setHandler(handlers)
			
			additioalHandlers.foreach ( handler => 
			{ 
				handler match
				{
					// overcome http/https cross domain issues
					case servelt:ServletContextHandler => servelt.addFilter(new FilterHolder(new CrossDomainFilter), "/*", FilterMapping.REQUEST);
					case _ => // do nothing
				}				
	        	handlers.addHandler(handler) 
	        })
				        
			//val context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS)
			//context.setContextPath(config.path + "/scripts/element")		
			//context.addServlet(new ServletHolder(new ConfigurationServlet()), "/*")
			//web.registerHandler(context)
			
			val context = new ContextHandler()
			context.setContextPath(config.path)
			
			// static resources
			val handler = new HandlerList()
			val url = ShaftServer.server.getClass.getClassLoader.getResource("web/public")			
			if (null == url) throw new Exception("webapp directory not found. expected at web/public")
			val resourceHandler = new ResourceHandler()			
	        resourceHandler.setDirectoriesListed(false)
	        resourceHandler.setWelcomeFiles(Array("index.htm", "index.html"))	 
	        resourceHandler.setResourceBase(url.toExternalForm)	
	        handler.addHandler(resourceHandler)
	        handler.addHandler(new DefaultHandler())
	        context.setHandler(handler)
	        	        
	        handlers.addHandler(context)
	        
	        server.start
	        			
	        onstartCallbacks.foreach( _() )
	        
	        info("embedded web server is started. listening on %s:%d, %s:%d".format(config.host, config.port, config.host, config.sslPort))
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
	
	private class CrossDomainFilter extends Filter
	{
		def init(filterConfig:FilterConfig)
		{			
		}
				
		def doFilter (request:ServletRequest, response:ServletResponse, chain:FilterChain) 
		{
			request match 
			{
				case request:HttpServletRequest =>
				{
					response match 
					{
						case response:HttpServletResponse =>
						{
							val allowed = List("http://%s:%d".format(request.getServerName(), config.port), "https://%s:%d".format(request.getServerName(), config.sslPort))
							val origin = request.getHeader("origin")
							if (allowed.contains(origin)) 
							{
								response.setHeader("Access-Control-Allow-Origin", origin)
								response.setHeader("Access-Control-Allow-Credentials", "true")	  				
							}							
						}
						case _ => // do nothing	
					}					
				}
				case _ => // do nothing
			}
			
			chain.doFilter(request, response)
		}
		
		def destroy()
		{			
		}
	}
		

}





