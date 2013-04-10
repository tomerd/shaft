package com.mishlabs.shaft
package services.comm

import scala.actors.Actor
import scala.collection._

import util._

protected class PubSub(/*bus:MessageBus*/) extends /*Actor with*/ Logger
{
	val subscriptions = new mutable.HashMap[String, mutable.HashSet[Communicator]]() with mutable.SynchronizedMap[String, mutable.HashSet[Communicator]]
	
	def startup()
	{
		//start
	}
	
	def shutdown()
	{
		//this ! Stop
		//bus.unsubscribeAll(this)
		
		subscriptions.foreach({ case(channel, subscribers) => subscribers.clear })
		subscriptions.clear
	}
	
	def subscribe(channel:String, subscriber:Communicator)
	{
		val subscribers = subscriptions.get(channel) match
		{
		  	case Some(list) => list
		  	case None => 
		  	{
		  		val list = new mutable.HashSet[Communicator]() with mutable.SynchronizedSet[Communicator]
		  		subscriptions += channel -> list
		  		list
		  	}
		}
	  
		if (!subscribers.contains(subscriber)) subscribers += subscriber
	}
	
	def unsubscribe(channel:String, subscriber:Communicator)
	{
		subscriptions.get(channel) match
		{
		  	case Some(list) => list -= subscriber
		  	case None => // do nothing
		}
	}
	
	def unsubscribeAll(subscriber:Communicator)
	{
		subscriptions.foreach({ case(channel, subscribers) => subscribers -= subscriber })
	}
		
	private def publish(subscribers:Iterable[Communicator], response:Response)
	{
		subscribers.foreach( _.send(response) )
	}
		
	/*
	def act()
	{
		while (true) 
		{
			receive 
			{
				case Stop => exit
				case _ => throw new Exception("pubsub module in communication service received unknown message")
			}
		}
	}
	*/
}

protected trait Communicator
{
	def send(message:Response)
}

protected abstract class Packet

protected abstract class Request extends Packet
protected case class KeepAliveRequest extends Request
protected case class SubscribeRequest(channel:String) extends Request
protected case class UnsubscribeRequest(channel:String) extends Request

protected abstract class Response(val kind:String) extends Packet

protected object ElementJsonCodec extends Logger
{
	import net.liftweb.json._
	import net.liftweb.json.JsonAST._
	import net.liftweb.json.JsonDSL._
   
	def encode(response:Response):String =
	{
		val json = response match
		{		  	
			case _ => throw new Exception("unknown response type: " + response)
		}
		 
		compact(JsonAST.render(json))
	}
		
	def decode(jsonString:String):Request =
	{
		implicit val formats = DefaultFormats
	
		val json = JsonParser.parse(jsonString)
		decode(json)
	}
	
	def decode(json:JsonAST.JValue):Request =
	{
		implicit val formats = DefaultFormats
	  
		val requesteType = (json \ "type").extract[String]
	
		requesteType match 
		{
			case "keep_alive" => KeepAliveRequest()
			case "subscribe" => 
			{
				val channel = (json \ "channel").extract[String]
			    SubscribeRequest(channel)
			}
			case "unsubscribe" => 
			{
				val channel = (json \ "channel").extract[String] 
			    UnsubscribeRequest(channel)
			}
			case _ => throw new Exception("unknown request: " + requesteType)
		}
	}
}