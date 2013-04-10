package com.mishlabs.shaft
package repository
package memory

import config._

protected trait TransientStore extends DataStore 
{
	def initialize(config:DataStoreConfig) = Unit	
	def disconnect = Unit
	
	def newTransaction[A](a: => A):A = a
	def inTransaction[A](a: => A):A = a
}
