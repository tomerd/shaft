package com.mishlabs.shaft
package services.webapp
package handlers

import scala.collection.Map

import javax.inject.Inject
import javax.servlet.Servlet

import lib.messaging._

import util._

//case class ServletInfo(initParams:Map[String,String], servlet:Servlet)

trait WebappHandler 
{	
	@Inject var bus:MessageBus = null
  
	def getServlet(config:Option[Any]):Pair[Servlet, Map[String, String]]
	
	protected final def reportError(description:String, cause:Throwable)
	{	
		bus.publish(Error(description, cause))
	}
}