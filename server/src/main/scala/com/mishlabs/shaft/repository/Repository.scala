package com.mishlabs.shaft
package repository

import com.google.inject.Module

import config._

trait Repository extends TransactionManager
{
	def initialize(config:List[DataStoreConfig])
	def disconnect
	
	val storageInjectionModule:Module
	
	override def toString():String = Repository.this.getClass.getName
}

