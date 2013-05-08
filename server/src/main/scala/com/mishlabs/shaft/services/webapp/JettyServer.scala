package com.mishlabs.shaft
package services
package webapp

import scala.collection._

import javax.servlet.Filter
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.servlet.FilterConfig

import org.eclipse.jetty.server.{Server, Connector, Handler}
import org.eclipse.jetty.server.handler.{ ContextHandler, HandlerList, ResourceHandler, DefaultHandler }
import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder
import org.eclipse.jetty.servlet.FilterMapping
import org.eclipse.jetty.servlet.FilterHolder
import org.eclipse.jetty.webapp.WebAppContext

import org.eclipse.jetty.server.nio.SelectChannelConnector
import org.eclipse.jetty.server.ssl.SslSelectChannelConnector

import com.mishlabs.shaft.config.WebServerConfig

import com.mishlabs.shaft.util._

protected class JettyServer(config:WebServerConfig) extends WebServer[WebServerConfig](config) 
{
	private var server:Server = null
	
	//private val additioalHandlers = mutable.ListBuffer[Handler]()
  
	/*
	def registerHandler(handler:Handler)
	{
		additioalHandlers += handler
	}
	*/
	
	def startup
	{
		info("embedded jetty web server starting up")		
		
		server = new Server

        val connector = new SelectChannelConnector       
        connector.setHost(config.host)
        connector.setPort(config.port)
        server.addConnector(connector)
		
		/*
		val connector = new ServerConnector(server)
		connector.setHost(config.host)
        connector.setPort(config.port)
        server.addConnector(connector)
        */
        
        val keystoreFilePath = ShaftServer.server.getClass.getClassLoader.getResource("webapp/resources/ssl/%s".format(config.keystoreFile))
        if (null == keystoreFilePath) throw new Exception ("webapp keystore file not found. expected at web/%s".format(config.keystoreFile))
        val sslConnector = new SslSelectChannelConnector
		sslConnector.setHost(config.host)
		sslConnector.setPort(config.sslPort)
		sslConnector.setKeystore(keystoreFilePath.getFile)
		sslConnector.setKeystoreType("PKCS12")
		sslConnector.setKeyPassword(config.keystorePassword)
		sslConnector.setPassword(config.keystorePassword)
		server.addConnector(sslConnector)

        /*
        val keystoreFilePath = ShaftServer.server.getClass.getClassLoader.getResource("webapp/resources/ssl/%s".format(config.keystoreFile))
        if (null == keystoreFilePath) throw new Exception ("webapp keystore file not found. expected at web/%s".format(config.keystoreFile))
        val sslContextFactory = new SslContextFactory
        sslContextFactory.setKeyStorePath(keystoreFilePath.getFile);
        sslContextFactory.setKeyStorePassword(config.keystorePassword);
        sslContextFactory.setKeyManagerPassword(config.keystorePassword);
        //sslContextFactory.setTrustStorePath(jetty_home + "/etc/keystore");
        //sslContextFactory.setTrustStorePassword("OBF:1vny1zlo1x8e1vnw1vn61x8g1zlu1vn4");
        //sslContextFactory.setExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA", "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");        
        val sslConfig = new HttpConfiguration
        sslConfig.addCustomizer(new SecureRequestCustomizer)
        val sslConnector:ServerConnector = new ServerConnector(server, new SslConnectionFactory(sslContextFactory, "http/1.1"), new HttpConnectionFactory(sslConfig))
        sslConnector.setPort(config.sslPort)
        server.addConnector(sslConnector)
        */
        val handlers = new HandlerList
		server.setHandler(handlers)
		
		/*
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
		*/
		*/
		
		//val context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS)
		//context.setContextPath(config.path + "/scripts/element")		
		//context.addServlet(new ServletHolder(new ConfigurationServlet()), "/*")
		//web.registerHandler(context)
		
        // webapp
		val webapp  = new WebAppContext
		val webappPath = ShaftServer.server.getClass.getClassLoader.getResource("webapp")
		webapp.setWar(webappPath.toExternalForm);
		webapp.setContextPath("/");
        handlers.addHandler(webapp)
        
        /*
		val context = new ContextHandler()
		context.setContextPath(config.path)				
		// static resources
		val handler = new HandlerList()
		val publicPath = ShaftServer.server.getClass.getClassLoader.getResource("web/public")			
		//if (null == url) throw new Exception("webapp directory not found. expected at web/public")
		if (null != publicPath)
		{
			val resourceHandler = new ResourceHandler		
	        resourceHandler.setDirectoriesListed(false)
	        resourceHandler.setWelcomeFiles(Array("index.htm", "index.html"))	 
	        resourceHandler.setResourceBase(publicPath.toExternalForm)	
	        handler.addHandler(resourceHandler)
		}
        handler.addHandler(new DefaultHandler)
        context.setHandler(handler)
        	        
        handlers.addHandler(context)
        */
        
        server.start        			
        
        info("embedded jetty web server is started. listening on %s:%d, %s:%d".format(config.host, config.port, config.host, config.sslPort))
	}
		
	def shutdown
	{
		info("embedded jetty web server shutting down")
	  			
		//additioalHandlers.clear
		
		if (null != server) server.stop
		
		info("embedded jetty web server is down") 
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