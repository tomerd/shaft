package org.simbit.shaft
package app.controllers
package common

import java.util.Date

import scala.collection._

protected class Session(val token:String, val expires:Date)
{	
	private val map = mutable.HashMap[String, AnyRef]() 
	
	def set[T <: AnyRef](key:String, value:T):T = { map += key -> value; value }
	def get(key:String):Option[AnyRef] = map.get(key)
	def remove(key:String) = map -= key
	def clear() = map.clear
	
	def copy(token:String, expires:Date):Session = 
	{
		val copy = new Session(token, expires)
		copy.map ++ this.map
		copy
	}	
	
}