package org.simbit.shaft
package services.comm.rest

import scala.collection._

import app.controllers._

import config.routes._

import util._

protected object RestRoutes
{
	// fight off type erasure
	//case class RouteEntry1(entry:Pair[String, Pair[Class[_ <: Controller], String]])
	//implicit def re1(entry:Pair[String, Pair[Class[_ <: Controller], String]]) = RouteEntry1(entry)	
	//case class RouteEntry2(entry:Pair[String, Route])
	//implicit def re2(entry:Pair[String, Route]) = RouteEntry2(entry)
	
	def apply(routes:Map[String, Route]):Map[String, FullRoute] =
	{
		val map =  mutable.HashMap[String, FullRoute]()
		routes.foreach{ case(path, route) => 
		{
			route match
			{
				case route:FullRoute => map += path -> route
				case route:StandardRoute => map ++= buildStandardRoutes(route.controller)
				case route => throw new Exception("unknown route type: %s".format(route))
			}
		}}
		map
	}
	
	//private val routes = mutable.HashMap[String, FullRoute]()
					
	private def buildStandardRoutes(controller:Class[_ <: Controller]):Map[String, FullRoute] = 
	{
		// TODO: add annotation support for custom controller names
		val service = StringHelpers.snakify(controller.getSimpleName.replace("Controller", ""))
		val routes = mutable.HashMap[String, FullRoute]()				
		routes += "GET:%s".format(service) -> FullRoute(controller, "list")
		routes += "GET:%s/:id".format(service) -> FullRoute(controller, "show", Some(":id"))
		routes += "GET:%s/:id/:api".format(service) -> FullRoute(controller, ":api", Some(":id"))
		routes += "POST:%s".format(service) -> FullRoute(controller, "create")		
		routes += "POST:%s/:id/:api".format(service) -> FullRoute(controller, ":api", Some(":id"))
		routes += "PUT:%s/:id".format(service) -> FullRoute(controller, "update", Some(":id"))
		routes += "DELETE:%s/:id".format(service) -> FullRoute(controller, "destroy", Some(":id"))
		// none traditional routes
		routes += "GET:%s/create".format(service) -> FullRoute(controller, "create")
		routes += "POST:%s/create".format(service) -> FullRoute(controller, "create")
		//routes += "GET:%s/:id/update".format(service) -> FullRoute(controller, "update")
		//routes += "POST:%s/:id/update".format(service) -> FullRoute(controller, "update")
		//routes += "GET:%s/:id/destroy".format(service) -> FullRoute(controller, "destroy")
		// ~ none traditional routes		
		routes
	}
	
	//def += (wrapper:RouteEntry1):Unit = this += (wrapper.entry._1 -> Route(wrapper.entry._2._1, wrapper.entry._2._2))
	//def += (wrapper:RouteEntry2):Unit = routes += wrapper.entry._1 -> wrapper.entry._2
	//def += (path:String, route:FullRoute):Unit = routes += path -> route
	//def += (entry:Pair[String, FullRoute]):Unit = routes += entry._1 -> entry._2
	//def += (controller:Class[_ <: Controller]):Unit = buildStandardRoutes(controller)
	
	//def get(path:String):Option[FullRoute] = routes.get(path)
}
