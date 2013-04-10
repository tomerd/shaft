package com.mishlabs.shaft
package repository
package redis

import config._

protected trait RedisStore extends DataStore 
{
	def initialize(config:DataStoreConfig) = Unit	
	def disconnect = Unit
	
	def newTransaction[A](a: => A):A = a
	def inTransaction[A](a: => A):A = a
}
