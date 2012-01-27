package org.simbit.shaft
package dal

protected trait StorageService[T <: app.model.KeyedModel[T]]
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
	
	def delete(key:Long):Unit = storage.delete(key)
	
	def delete(keys:Iterable[Long]):Unit = storage.delete(keys)
	
	def delete(entity:T):Unit = this.delete(entity.id)
	
	// arrow in entities: => Iterable[T] is a trick to fight type erasure
	def delete(entities: => Iterable[T]):Unit = this.delete(entities.map(_.id))
}