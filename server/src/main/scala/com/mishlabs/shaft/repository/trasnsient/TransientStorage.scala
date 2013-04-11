package com.mishlabs.shaft
package repository
package memory

import scala.collection._
 
import app.model.KeyedModel

protected abstract class TransientStorage[K <: AnyVal, T <: KeyedModel[K, T]] extends Storage[T]
{	
	private val store = new mutable.HashMap[K, T] with mutable.SynchronizedMap[K, T]
	
	def find(id:K):Option[T] = store.get(id)
		
	def findAll():Iterable[T] = store.values
			
	def create(entity:T):T = 
	{
		store += entity.id -> entity
		entity 
	}
	
	def update(entity:T):T = { store.update(entity.id, entity); entity }
		
	def delete(key:K):Unit = store -= key
	
	def delete(keys:Iterable[K]):Unit = store --= keys
}