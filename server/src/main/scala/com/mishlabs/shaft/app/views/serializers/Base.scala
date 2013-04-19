package com.mishlabs.shaft
package app.views
package serializers

import scala.collection._

protected abstract class SerializerView(fields:String*) extends View
{
	def approve(field:String):Boolean  
}

private object BlackList
{
	val coreExclude = List("wait", "notify", "notifyAll", "hashCode", "getClass", "toString")
}

protected [views] case class BlackList(fields:String*) extends SerializerView
{
	def this(fields:Array[String]) = this(fields:_*)
	// the arrow in fields: => Seq[String is a trick to overcome type erasure
	def this(fields: => Seq[String]) = this(fields:_*)	
	
	private val exclude = BlackList.coreExclude ++ fields
	def approve(field:String) = !exclude.contains(field)
}

protected [views] case class WhiteList(fields:String*) extends SerializerView
{	
	def this(fields:Array[String]) = this(fields:_*)
	// the arrow in fields: => Seq[String is a trick to overcome type erasure
	def this(fields: => Seq[String]) = this(fields:_*)
	
	private val include = fields.dropWhile(BlackList.coreExclude.contains(_))
	def approve(field:String) = include.contains(field)
}

