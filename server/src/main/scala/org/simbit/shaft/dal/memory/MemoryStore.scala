package org.simbit.shaft
package dal
package memory

import config._

// TODO: this is just a test for the storage abstraction layer. 
// if we want to keep this need to make this more robust and scalable
protected trait MemoryStore extends DataStore 
{
	def initialize(config:DataStoreConfig) = Unit	
	def disconnect = Unit
	
	def newTransaction[A](a: => A):A = a
	def inTransaction[A](a: => A):A = a
}
