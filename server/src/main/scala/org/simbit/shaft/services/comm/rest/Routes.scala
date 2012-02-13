package org.simbit.shaft
package services.comm.rest

import scala.collection._

import app.controllers._

import config.routes._

import util._

protected object RestRoutes
{	
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
						
	private def buildStandardRoutes(controller:Class[_ <: Controller]):Map[String, FullRoute] = 
	{
		// TODO: add annotation support for custom service names (annotations?)
		val serviceName = StringHelpers.snakify(controller.getSimpleName.replace("Controller", ""))
		
		buildStandardRoutes(serviceName, controller) ++
		buildStandardRoutes(StringHelpers.pluralify(serviceName), controller)
	}
	
	private def buildStandardRoutes(serviceName:String, controller:Class[_ <: Controller]):Map[String, FullRoute] = 
	{
		val routes = mutable.HashMap[String, FullRoute]()				
		routes += "GET:%s".format(serviceName) -> FullRoute(controller, "list")
		routes += "GET:%s/:id".format(serviceName) -> FullRoute(controller, "show", Some(":id"))
		routes += "GET:%s/:id/:api".format(serviceName) -> FullRoute(controller, ":api", Some(":id"))
		routes += "POST:%s".format(serviceName) -> FullRoute(controller, "create")
		routes += "POST:%s/:id/:api".format(serviceName) -> FullRoute(controller, ":api", Some(":id"))
		routes += "PUT:%s/:id".format(serviceName) -> FullRoute(controller, "update", Some(":id"))
		routes += "DELETE:%s/:id".format(serviceName) -> FullRoute(controller, "destroy", Some(":id"))
		// none traditional routes
		routes += "GET:%s/create".format(serviceName) -> FullRoute(controller, "create")
		routes += "POST:%s/create".format(serviceName) -> FullRoute(controller, "create")
		// ~ none traditional routes
		routes
	}
}
