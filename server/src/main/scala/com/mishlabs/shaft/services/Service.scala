package com.mishlabs.shaft
package services

import com.google.inject.Inject

import lib.messaging._

import util._

trait Service
{
	def startup
	def shutdown
}

abstract class ShaftService extends Service with Logger
{	
	@Inject var bus:MessageBus = null
	
	protected final def reportError(description:String, cause:Throwable)
	{	
		bus.publish(Error(description, cause))
	}
}