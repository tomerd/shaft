package com.mishlabs.shaft
package services
package comm
package bayeux

import scala.collection._
import org.cometd.bayeux.Message
import org.cometd.bayeux.server.BayeuxServer
import org.cometd.bayeux.server.ServerSession
import org.cometd.server.AbstractService
import org.cometd.server.CometdServlet
import com.google.inject.Inject
import config._
import util._
import services.web.WebService
import javax.servlet.ServletConfig
import com.mishlabs.shaft.services.web.ServletInfo

trait BayeuxCommunicationService extends CommunicationService
{
}

class ShaftBayeuxCommunicationService extends ShaftCommunicationService with BayeuxCommunicationService
{	
	@Inject var config:BayeuxConfig = null
	@Inject var webService:WebService = null
	
	private val pusbsub = new PubSub()
	
	private var service:BayeuxService = null
	
	def startup()
	{
	  	if (!config.enabled)
	  	{
	  		info("bayeux communication service disabled")
	  		return
	  	}
	  	  	
		info("bayeux communication service starting up")
		
		pusbsub.startup
				
		//val context = new ServletContextHandler(ServletContextHandler.SESSIONS)
		//context.setContextPath(config.path)
		
		//val servlet = new CometdServlet()		
	    //val servletHolder = new ServletHolder(servlet)
		//servletHolder.setInitParameter("transports", "org.cometd.websocket.server.WebSocketTransport")
		//servletHolder.setInitParameter("allowedTransports", "org.cometd.websocket.server.WebSocketTransport")
		//servletHolder.setInitParameter("logLevel", getLogLevel().toString) 
	    //context.addServlet(servletHolder, "/*")

		//webService.registerHandler(context)
		
		//webService.onStart(()=>
	    //{
			//service = new BayeuxService(servlet.getBayeux)		
			//service.startup
	    //})
	    
		val servlet:CometdServletWrapper = new CometdServletWrapper
		servlet.onStart(()=>
	    {
			service = new BayeuxService(servlet.getBayeux)		
			service.startup
	    })
	    
	    val initParams = mutable.HashMap[String, String]()
	    initParams += "transports" -> "org.cometd.websocket.server.WebSocketTransport" 	    
		initParams += "allowedTransports" -> "org.cometd.websocket.server.WebSocketTransport"
		initParams += "logLevel" -> getLogLevel().toString	    
		webService.registerServlet(ServletInfo("bayeux", config.path, initParams, servlet))
	    	  	    
	    info("bayeux communication service is up")
	}
		
	def shutdown()
	{
		if (!config.enabled) return;
	  
		info("bayeux communication service shutting down")
		
		pusbsub.shutdown
		
		if (null != service) service.shutdown
		
		info("bayeux communication service is down")
	}
	
	// 0 = warn, 1 = info, 2 = debug
	private def getLogLevel():Int =
	{
		if (isDebugEnabled) return 2
		if (isInfoEnabled) return 1
		return 0
	}

	private class BayeuxService(bayeux:BayeuxServer) extends AbstractService(bayeux, "element") 
													//with ClientSessionChannel.MessageListener
													//with BayeuxServer.ChannelListener
													//with BayeuxServer.SubscriptionListener
													with BayeuxServer.SessionListener
													with Logger
	{
		val communicators = new mutable.HashMap[String, Communicator]() with mutable.SynchronizedMap[String, Communicator]
		
		def startup()
		{
			addService(config.requestChannel + "/*", "processRequest");
			bayeux.addListener(this)
		}
		
		def shutdown()
		{
			bayeux.removeListener(this)
			communicators.clear
		}
		
		def processRequest(session:ServerSession, message:Message)
		{
			import net.liftweb.json._
			
			implicit val formats = DefaultFormats
						
			val communicator = communicators.get(session.getId) match
			{
			  	case Some(communicator) => communicator
			  	case _ => throw new Exception("unklnonw session " + session.getId)
			}
			
			try
			{
				val json = JsonParser.parse(message.getJSON)
				val payload = (json \ "data").extract[JObject]
					
				ElementJsonCodec.decode(payload) match 
				{				  	
				  	case request:KeepAliveRequest => // do nothing	
				  	case request:SubscribeRequest => pusbsub.subscribe(request.channel, communicator)
				  	case request:UnsubscribeRequest => pusbsub.unsubscribe(request.channel, communicator)
				  	case _ => throw new Exception("unknown client request " + message)
				}
			}
			catch  
			{
			  	case e => reportError("error decoding or handling request: " + e, e) 
			}
		}
		
		override def sessionAdded(session:ServerSession)
		{
			communicators += session.getId -> new BayeuxCommunicator(session)
		}
		
		override def sessionRemoved(session:ServerSession, timedout:Boolean)
		{
			communicators -= session.getId					
		}
	}
	
	private class BayeuxCommunicator(session:ServerSession) extends Communicator
	{
		def send(response:Response)
		{					
			try 
			{					
				session.deliver(session, config.responseChannel + "/" + response.kind, ElementJsonCodec.encode(response), null);
			} 
			catch  
			{
			  	case e => reportError("error encoding or sending response " + e.getMessage, e) 
			}
		}
	}
	
	private class CometdServletWrapper extends CometdServlet
	{
		var onStartCallback:Option[()=>Unit] = None
		
		//setInitParameter("transports", "org.cometd.websocket.server.WebSocketTransport")
		//setInitParameter("allowedTransports", "org.cometd.websocket.server.WebSocketTransport")
		//setInitParameter("logLevel", getLogLevel().toString)
		
		override def init
		{		  	
			super.init
			if (onStartCallback.isDefined) onStartCallback.get.apply
		}
		
		def onStart(callback:()=>Unit)
		{
			onStartCallback = Some(callback)
		}
	}

}