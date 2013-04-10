package com.mishlabs.shaft
package services

import java.util.Date

import scala.collection._

import com.google.inject. { Guice, Injector, Module, Binder}

import lib.messaging._

import config._
import util._

object ServiceManager 
{
	val services = mutable.ListBuffer[Service]()
  
	def startup(bus:MessageBus, config:ShaftConfig)
	{	  
	   val injector = Guice.createInjector(new ServicesModule(bus, config))	   
	   // repository service must be first as it is used by other services
	   services += injector.getInstance(classOf[repository.RepositoryService])	   
	   services += injector.getInstance(classOf[comm.rest.RestCommunicationService])
	   services += injector.getInstance(classOf[comm.bayeux.BayeuxCommunicationService])	   
	   // web service must be created last as it will look for servlets registered by prior services
	   services += injector.getInstance(classOf[web.WebService])
	   services.foreach { _.startup }
	}
				
	def shutdown()
	{
		services.foreach { _.shutdown }
		services.clear
	}	
}

class ServicesModule(bus:MessageBus, configuration:ShaftConfig) extends Module 
{  
	def configure(binder:Binder) = 
	{					  
		// context
		binder.bind(classOf[MessageBus]).toInstance(bus)
		binder.bind(classOf[config.RepositoryConfig]).toInstance(configuration.repository)
		binder.bind(classOf[config.WebConfig]).toInstance(configuration.web)
		binder.bind(classOf[config.RestConfig]).toInstance(configuration.rest)
		binder.bind(classOf[config.BayeuxConfig]).toInstance(configuration.bayeux)
		// services are singletons
		binder.bind(classOf[repository.RepositoryService]).toInstance(new repository.ShaftRepositoryService())
		binder.bind(classOf[web.WebService]).toInstance(new web.ShaftWebService())
		binder.bind(classOf[comm.rest.RestCommunicationService]).toInstance(new comm.rest.ShaftRestCommunicationService())
		binder.bind(classOf[comm.bayeux.BayeuxCommunicationService]).toInstance(new comm.bayeux.ShaftBayeuxCommunicationService())
	} 
}  
 