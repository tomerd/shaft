package org.simbit.shaft
package dal

import com.google.inject.Module

import config._

trait DataStore
{
	def initialize(config:DataStoreConfig)
	def disconnect
	
	def newTransaction[A](a: => A):A
	def inTransaction[A](a: => A):A
	
	val servicesInjectionModule:Module
	
	override def toString():String = this.getClass.getName
}

