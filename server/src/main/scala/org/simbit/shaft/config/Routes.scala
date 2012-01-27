package org.simbit.shaft
package config.routes

import scala.collection._

import app.controllers.Controller

import util._

// FIXME: replace this with a nicer routes solution, something along the lines of ostrich config file or move to unfiltered


abstract class Route

protected case class FullRoute(controller:Class[_ <: Controller], api:String, id:Option[String]=None) extends Route

protected case class StandardRoute(controller:Class[_ <: Controller]) extends Route

/*
trait Routes
{
	def get(path:String):Option[Route] = Router.get(path)
}
*/

object Routes
{
	// fight off type erasure
	//case class RouteEntry1(entry:Pair[String, Pair[Class[_ <: Controller], String]])
	//implicit def re1(entry:Pair[String, Pair[Class[_ <: Controller], String]]) = RouteEntry1(entry)	
	//case class RouteEntry2(entry:Pair[String, Route])
	//implicit def re2(entry:Pair[String, Route]) = RouteEntry2(entry)
	
	private val routes = mutable.HashMap[String, Route]()
	def all:Map[String, Route] = routes
				
	/*
	private def buildStandardRoutes(controller:Class[_ <: Controller]):Unit = 
	{
		// TODO: add annotation support for custom controller names
		val service = StringHelpers.snakify(controller.getSimpleName.replace("Controller", ""))
		val routes = mutable.HashMap[String, Route]()				
		this += "GET:%s".format(service) -> Route(controller, "list")
		this += "GET:%s/:id".format(service) -> Route(controller, "show", Some(":id"))
		this += "GET:%s/:id/:api".format(service) -> Route(controller, ":api", Some(":id"))
		// FIXME routes for testing
		this += "GET:%s/create".format(service) -> Route(controller, "create")
		this += "GET:%s/destroy/:id".format(service) -> Route(controller, "destroy")
		// ~ FIXME routes for testing
		this += "POST:%s".format(service) -> Route(controller, "create")
		this += "POST:%s/:id/:api".format(service) -> Route(controller, ":api", Some(":id"))
		this += "PUT:%s/:id".format(service) -> Route(controller, "update", Some(":id"))
		this += "DELETE:%s/:id".format(service) -> Route(controller, "destroy", Some(":id"))
	}
	*/
	
	//def += (wrapper:RouteEntry1):Unit = this += (wrapper.entry._1 -> Route(wrapper.entry._2._1, wrapper.entry._2._2))
	//def += (wrapper:RouteEntry2):Unit = routes += wrapper.entry._1 -> wrapper.entry._2
	//def route (path:String, route:Route):Unit = routes += path -> route
	//def route (entry:Pair[String, Route]):Unit = routes += entry._1 -> entry._2
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

