package com.mishlabs.shaft
package repository

import com.google.inject.Module

import config._

trait Repository
{
	def initialize(config:List[DataStoreConfig])
	def disconnect
	
	def newTransaction[A](a: => A):A
	def inTransaction[A](a: => A):A
	
	val servicesInjectionModule:Module
	
	override def toString():String = Repository.this.getClass.getName
}

