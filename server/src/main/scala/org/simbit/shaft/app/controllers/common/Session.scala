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