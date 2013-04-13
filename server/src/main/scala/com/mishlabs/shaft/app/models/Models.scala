package com.mishlabs.shaft
package app.model

import java.util.Date
import java.util.UUID

trait Model

trait KeyedModel[K <: Any, T <: KeyedModel[K, T]] extends Model
{
	self:T =>
	
	var id:K = Unit.asInstanceOf[K] //HACK...TEST THIS!
	def id(value:K):T = { this.id = value; this }		
}

/*
trait NumericKeyedModel[T <: NumericKeyedModel[T]] extends KeyedModel[Long, T]
{
	self:T =>
	
	var id:Long = Long.MinValue
	
	//protected final def isNew:Boolean = Long.MinValue == this.id	
}

trait StringKeyedModel[T <: StringKeyedModel[T]] extends KeyedModel[String, T]
{
	self:T =>
	
	var id:String = null
}
*/

/*
protected trait TimestampedModel[T <: TimestampedModel[T]] extends Model
{
	self:T =>
		
	var createdBy:Option[String] = None
	def createdBy(value:Option[String]):T = { this.createdBy = value;  this }
	
	var createdAt:Option[Date] = None
	def createdAt(value:Option[Date]):T = { this.createdAt = value;  this }
	
	var modifiedBy:Option[String] = None
	def modifiedBy(value:Option[String]):T = { this.modifiedBy = value;  this }
	
	var modifiedAt:Option[Date] = None
	def modifiedAt(value:Option[Date]):T = { this.modifiedAt = value;  this }
		
	//final def touch(modifier:User):T = touch(modifier.username)
	
	final def touch(modifier:String):T = this.modifiedBy(Some(modifier)).modifiedAt(Some(new Date()))	
	
	//final def touchNew(modifier:User):T = touchNew(modifier.username)
	
	final def touchNew(modifier:String):T = this.createdBy(Some(modifier)).createdAt(Some(new Date())).touch(modifier)
}

protected trait NamedModel[T <: NamedModel[T]] extends Model
{
	self:T =>
	
	var name:String = ""
	def name(value:String):T = { this.name = value; this }
	
	var description:Option[String] = None
	def description(value:Option[String]):T = { this.description = value; this }
}

protected trait BasicModel[T <: BasicModel[T]] extends IdentifiableModel[T] with TimestampedModel[T]
{
	self:T =>
}

protected trait FullModel[T <: FullModel[T]] extends BasicModel[T] with NamedModel[T]
{
	self:T =>	
}
*/