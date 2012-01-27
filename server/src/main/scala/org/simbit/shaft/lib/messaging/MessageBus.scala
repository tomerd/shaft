package org.simbit.shaft
package lib.messaging

import scala.collection._
import scala.actors._

import org.simbit.shaft.util.Logger

/*
object MessageBus
{
	private val default = new MessageBus()
	
	def getDefault():MessageBus = default
}
*/

/*
trait MessageBus
{
	def publish(message:Object)
	def subscribe[T](topic:Class[_], actor:Actor)
	def unsubscribe[T](topic:Class[T], actor:Actor)
	def unsubscribeAll(actor:Actor)
	def clear()
}
*/

class MessageBus /*extends MessageBus with Logger*/ extends Logger
{
	val map = mutable.HashMap[Class[_], mutable.ListBuffer[Actor]]()
  
	def publish(message:Object)
	{
		map.get(message.getClass) match
		{
		  	case Some(actors) => actors.foreach { _ ! message  }
			case None => // no subscribers, do nothing
		} 
	}
	
	/*
	def unpublish(message:Object)
	{
	}
	*/
	
	def subscribe[T](topic:Class[_], actor:Actor)
	{
		map.get(topic) match
		{
		  	case Some(actors) => actors += actor
			case None => 
		  	{
		  		val actors = new mutable.ListBuffer[Actor]()
		  		actors += actor
		  		map += topic -> actors
		  	}
		}
	}

    def unsubscribe[T](topic:Class[T], actor:Actor)
    {
    	map.get(topic) match
		{
		  	case Some(actors) => actors -= actor
			case None => // not found, do nothing
		}
    }
    
    def unsubscribeAll(actor:Actor)
    {
    	map.foreach { case (topic, actors) => actors -= actor }
    }
    
    def clear()
    {
    	map.foreach { case (topic, actors) => actors.clear }
    	map.clear
    }
	
}
