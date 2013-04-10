package com.mishlabs.shaft
package repository

protected trait StorageService[T <: com.mishlabs.shaft.app.model.KeyedModel[T]]
{			
	val storage:Storage[T]
				
	//def find(id:Long):Option[T] = find(By("id", id))
	//def findByExuid(exuid:String):Option[T] = findBy("exuid", exuid)
	
	//def find(condition:Condition):Option[T] = storage.find(condition)
		
	//def findAll():Iterable[T] = findAll(None)
	
	//def findAll(condition:Condition):Iterable[T] = findAll(Some(condition))
	
	//def findAll(condition:Option[Condition]):Iterable[T] = storage.findAll(condition)
	
	def find(id:Long):Option[T] = storage.find(id)
		
	def findAll():Iterable[T] = storage.findAll()

	def create(entity:T):T = storage.create(entity)
	
	def update(entity:T):T = storage.update(entity)
	
	def save(entity:T):T = if (entity.id < 0) create(entity) else update(entity)
	
	def delete(key:Long):Unit = storage.delete(key)
	
	def delete(keys:Iterable[Long]):Unit = storage.delete(keys)
	
	def delete(entity:T):Unit = this.delete(entity.id)
	
	// arrow in entities: => Iterable[T] is a trick to fight type erasure
	def delete(entities: => Iterable[T]):Unit = this.delete(entities.map(_.id))
}