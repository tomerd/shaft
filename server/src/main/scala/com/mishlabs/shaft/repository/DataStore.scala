package com.mishlabs.shaft
package repository

import com.mishlabs.shaft.config.DataStoreConfig

trait DataStore
{
	def initialize(config:DataStoreConfig)
	def disconnect
	
	def newTransaction[A](a: => A):A
	def inTransaction[A](a: => A):A
}