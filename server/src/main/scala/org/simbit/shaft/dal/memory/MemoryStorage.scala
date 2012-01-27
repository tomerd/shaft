package org.simbit.shaft
package dal
package memory

import scala.collection._

import java.util.Date
import java.util.UUID
import java.sql.Timestamp
 
import app.model.KeyedModel

// TODO: this is just a test for the storage abstraction layer. 
// if we want to keep this need to make this more robust and scalable
protected abstract class MemoryStorage[T <: KeyedModel[T]] extends Storage[T]
{	
	private val store = new mutable.HashMap[Long, T] with mutable.SynchronizedMap[Long, T]
	
	def find(id:Long):Option[T] = store.get(id)
		
	def findAll():Iterable[T] = store.values
			
	def create(entity:T):T = 
	{ 
		entity.id = store.size + 1
		store += entity.id -> entity
		entity 
	}
	
	def update(entity:T):T = { store.update(entity.id, entity); entity }
		
	def delete(key:Long):Unit =store -= key
	
	def delete(keys:Iterable[Long]):Unit = store --= keys
}