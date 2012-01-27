package org.simbit.shaft
package dal

protected trait Storage[T <: app.model.KeyedModel[T]]
{	
	//def find(condition:Condition):Option[T]
		
	//def findAll(condition:Option[Condition]):Iterable[T]
	
	def find(id:Long):Option[T]
	
	def findAll():Iterable[T]
	
	def create(entity:T):T
	
	def update(entity:T):T
		
	def delete(key:Long):Unit
	
	def delete(keys:Iterable[Long]):Unit
}

//protected trait Condition
//case class By(field:String, value:Any) extends Condition
//case class Like(field:String, value:Any) extends Condition