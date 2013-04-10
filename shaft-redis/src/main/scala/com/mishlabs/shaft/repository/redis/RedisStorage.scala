package com.mishlabs.shaft
package repository
package redis

import scala.collection._

import java.util.Date
import java.util.UUID
import java.sql.Timestamp
 
import app.model.KeyedModel

protected abstract class RedisStorage[T <: KeyedModel[T]] extends Storage[T]
{	
	private val store = new mutable.HashMap[Long, T] with mutable.SynchronizedMap[Long, T]
	
	def find(id:Long):Option[T] = store.get(id)
		
	def findAll():Iterable[T] = store.values
			
	// as this is an in-memory representation, we need to create IDs by code
	// naturally, this is not scalabale nor thread safe
	def create(entity:T):T = 
	{ 
		entity.id = store.size + 1
		store += entity.id -> entity
		entity 
	}
	
	def update(entity:T):T = { store.update(entity.id, entity); entity }
		
	def delete(key:Long):Unit = store -= key
	
	def delete(keys:Iterable[Long]):Unit = store --= keys
}