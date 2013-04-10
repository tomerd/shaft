package com.mishlabs.shaft
package util

import scala.actors.{Actor,TIMEOUT}

object Scheduler
{
	def schedule(time:Long)(calllback:() => Unit):Actor =
	{	  
		def scheduleLoop 
		{
			var stop = false
			Actor.reactWithin(time) 
			{
				case TIMEOUT => if (!stop) calllback(); scheduleLoop
				case Stop => stop = true
			}
		}
		Actor.actor(scheduleLoop)
	}	
}