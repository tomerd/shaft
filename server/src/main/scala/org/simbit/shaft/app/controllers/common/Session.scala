package org.simbit.shaft
package app.controllers
package common

import java.util.Date

import scala.collection._

trait Session
{
	def token:String
	def expires:Date
	
	def set[T <: AnyRef](key:String, value:T):T
	def get(key:String):Option[AnyRef]
	def remove(key:String)
	def clear()
}

protected class SessionImpl(sessionToken:String, expiresOn:Date) extends Session
{	
	private var _token = sessionToken
	private var _expires = expiresOn
	private val map = mutable.HashMap[String, AnyRef]()
	private var updateListeners = mutable.ListBuffer[() => Unit]()	
	
	def token = _token
	def expires = _expires
	
	def set[T <: AnyRef](key:String, value:T):T = { map += key -> value; value }
	def get(key:String):Option[AnyRef] = map.get(key)
	def remove(key:String) = map -= key
	def clear() = map.clear
	
	/*
	def copy(token:String, expires:Date):Session = 
	{
		val copy = new Session(token, expires)
		copy.map ++ this.map
		copy
	}
	*/
	
	def update(sessionToken:String, expiresOn:Date):Session =
	{
		_token = sessionToken;
		_expires = expiresOn;
		
		updateListeners.foreach ( listener =>
		{
			try
			{
				listener()
			}
			catch
			{
				case e => // do nothing
			}
		})		
		
		this
	}
	
	def addUpdateHandler(handler:() => Unit)
	{
		updateListeners += handler
	}
	
}