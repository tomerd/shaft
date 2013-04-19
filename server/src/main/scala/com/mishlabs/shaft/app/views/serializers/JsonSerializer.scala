package com.mishlabs.shaft
package app
package views
package serializers

import net.liftweb.json.JValue

import scala.xml.Elem

import model.Model
import util._

// TODO: make this more efficient
object JsonSerializer 
{
	def render(data:AnyRef, viewName:Option[String]):JValue =
	{
		XmlSerializer.render(data, viewName)
	}
	
	def render(data:AnyRef, view:View):JValue =
	{
		XmlSerializer.render(data, view)
	}
	
	def render(data:Iterable[AnyRef], nodeName:String):JValue =
	{		
		XmlSerializer.render(data, nodeName)
	}

	def render(entity:Model, viewName:Option[String]):JValue = 
	{		
		XmlSerializer.render(entity, viewName)
	}
	
	def render(entities:Iterable[_ <: Model], nodeName:Option[String], viewName:Option[String]):JValue =  
	{		
		XmlSerializer.render(entities, nodeName, viewName)
	}
	
	implicit def xmlToJson(xml:Elem):JValue = CastingHelpers.xmlToJson(xml)
}