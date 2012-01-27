package org.simbit.shaft
package app.model

import java.util.Date
import java.util.UUID

trait Model

trait KeyedModel[T <: KeyedModel[T]] extends Model
{
	self:T =>
	
	var id:Long = -1
	def id(value:Long):T = { this.id = value; this }	
	
	//var exuid:UUID = new UUID(0,0)
	//def exuid(value:UUID):T = { this.exuid = value; this }
	
	//protected final def isNew:Boolean = this.id < 0	
}

/*
protected trait TimestampedModel[T <: TimestampedModel[T]] extends Model
{
	self:T =>
		
	var createdBy:Option[String] = None
	def createdBy(value:Option[String]):T = { this.createdBy = value;  this }
	
	var createdOn:Option[Date] = None
	def createdOn(value:Option[Date]):T = { this.createdOn = value;  this }
	
	var modifiedBy:Option[String] = None
	def modifiedBy(value:Option[String]):T = { this.modifiedBy = value;  this }
	
	var modifiedOn:Option[Date] = None
	def modifiedOn(value:Option[Date]):T = { this.modifiedOn = value;  this }
		
	//final def touch(modifier:User):T = touch(modifier.username)
	
	final def touch(modifier:String):T = this.modifiedBy(Some(modifier)).modifiedOn(Some(new Date()))	
	
	//final def touchNew(modifier:User):T = touchNew(modifier.username)
	
	final def touchNew(modifier:String):T = this.createdBy(Some(modifier)).createdOn(Some(new Date())).touch(modifier)
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