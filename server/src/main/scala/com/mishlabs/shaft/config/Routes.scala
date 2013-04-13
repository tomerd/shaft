package com.mishlabs.shaft
package config.routes

import scala.collection._
import app.controllers.Controller
import util._
import javax.servlet.Servlet

// FIXME: replace this with a nicer routes solution, something along the lines of ostrich config file or move to unfiltered

abstract class Route

protected case class FullRoute(controller:Class[_ <: Controller], api:String, id:Option[String]=None) extends Route

protected case class StandardRoute(controller:Class[_ <: Controller]) extends Route

object Routes
{	
	private val _routes = mutable.HashMap[String, Route]()
	def routes:Map[String, Route] = routes
	
	private val _servlets = mutable.HashMap[String, Pair[Servlet, Map[String, String]]]()
	def servlets:Map[String, Pair[Servlet, Map[String, String]]] = _servlets
	
	def += (controller:Class[_ <: Controller]):Unit = _routes += StringHelpers.snakify(controller.getSimpleName.replace("Controller", "")) -> StandardRoute(controller)
	def += (entry:Pair[Pair[String, Class[_ <: Controller]], String]):Unit = _routes += entry._1._1 -> FullRoute(entry._1._2, entry._2)
	
	//def += (entry: => Pair[String, Servlet]):Unit = _servlets += entry._1 -> (entry._2 -> Map.empty[String, String])
	def += (entry: => Pair[String, Pair[Servlet, Map[String, String]]]):Unit = _servlets += entry
	
	//def get(path:String):Option[Route] = routes.get(path)	
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

object servlets
{
	def apply(entry:Pair[String, Servlet]):Unit = apply(entry._1 -> (entry._2 -> Map.empty[String, String]))	
	def apply(entries:Pair[String, Servlet]*):Unit = entries.foreach( entry => apply(entry._1 -> (entry._2 -> Map.empty[String, String]) ) )
	
	def apply(entry: => Pair[String, Pair[Servlet, Map[String, String]]]):Unit = Routes += entry
	// FIXME: type erasure issue
	//def apply(entries:Pair[String, Pair[Servlet, Map[String, String]]]*):Unit = entries.foreach( Routes +=  _ )
}

