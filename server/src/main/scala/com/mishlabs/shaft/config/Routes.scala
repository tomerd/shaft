package com.mishlabs.shaft
package config.routes

import scala.collection._

import app.controllers.Controller

import util._

// FIXME: replace this with a nicer routes solution, something along the lines of ostrich config file or move to unfiltered


abstract class Route

protected case class FullRoute(controller:Class[_ <: Controller], api:String, id:Option[String]=None) extends Route

protected case class StandardRoute(controller:Class[_ <: Controller]) extends Route

object Routes
{	
	private val routes = mutable.HashMap[String, Route]()
	def all:Map[String, Route] = routes
	
	def += (controller:Class[_ <: Controller]):Unit = routes += StringHelpers.snakify(controller.getSimpleName.replace("Controller", "")) -> StandardRoute(controller)
	def += (entry:Pair[Pair[String, Class[_ <: Controller]], String]):Unit = routes += entry._1._1 -> FullRoute(entry._1._2, entry._2)
	
	def get(path:String):Option[Route] = routes.get(path)	
}

object custom
{
	def apply(entry:Pair[Pair[String, Class[_ <: Controller]], String]):Unit = Routes += entry
	def apply(entries:Pair[Pair[String, Class[_ <: Controller]], String]*):Unit = entries.foreach( Routes += _ )
}

object standard
{
	def apply(controller:Class[_ <: Controller]):Unit = Routes += controller
	def apply(controllers:Class[_ <: Controller]*):Unit = controllers.foreach( Routes += _ )
}

