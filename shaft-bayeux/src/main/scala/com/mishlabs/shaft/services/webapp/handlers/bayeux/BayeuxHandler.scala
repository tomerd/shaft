package com.mishlabs.shaft
package services
package webapp
package handlers
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

object BayeuxHandler extends WebappHandler with Logger
{	
	private val pubsub = new PubSub()
	
	private var service:BayeuxService = null
	
	private var config:BayeuxConfig = null
	
	def getServlet(config:Option[Any]) =
	{
		config match
		{
		  	case Some(config:BayeuxConfig) =>
		    {		
		    	this.config = config
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
				  servlet.onStart(() =>
			    {
			    	pubsub.startup			      
					  service = new BayeuxService(servlet.getBayeux)
					  service.startup
			    })
			    
			    servlet.onEnd(() => 
			    {
			    	pubsub.shutdown			    	
			    	service.shutdown
			    })
			    
			    val initParams = mutable.HashMap[String, String]()
			    initParams += "transports" -> "org.cometd.websocket.server.WebSocketTransport" 	    
				  initParams += "allowedTransports" -> "org.cometd.websocket.server.WebSocketTransport"
				  initParams += "logLevel" -> getLogLevel().toString

          servlet -> initParams
		    }
		  	case _ => throw new Exception("invalid configuration, expected BayeuxConfig")
		}
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
					
				ShfatJsonCodec.decode(payload) match 
				{				  	
				  	case request:KeepAliveRequest => // do nothing	
				  	case request:SubscribeRequest => pubsub.subscribe(request.channel, communicator)
				  	case request:UnsubscribeRequest => pubsub.unsubscribe(request.channel, communicator)
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
				session.deliver(session, config.responseChannel + "/" + response.kind, ShfatJsonCodec.encode(response), null);
			} 
			catch  
			{
			  	case e => reportError("error encoding or sending response " + e.getMessage, e) 
			}
		}
	}
	
	protected [webapp] class CometdServletWrapper extends CometdServlet
	{
		var initCallback:Option[()=>Unit] = None
		var destroyCallback:Option[()=>Unit] = None
		
		//setInitParameter("transports", "org.cometd.websocket.server.WebSocketTransport")
		//setInitParameter("allowedTransports", "org.cometd.websocket.server.WebSocketTransport")
		//setInitParameter("logLevel", getLogLevel().toString)
		
		override def init
		{		  	
			super.init
			if (initCallback.isDefined) initCallback.get.apply
		}
		
		override def destroy
		{
			super.destroy
			if (destroyCallback.isDefined) destroyCallback.get.apply
		}
		
		def onStart(callback:()=>Unit)
		{
			initCallback = Some(callback)
		}
		
		def onEnd(callback:()=>Unit)
		{
			destroyCallback = Some(callback)
		}
	}		
}