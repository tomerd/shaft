package com.mishlabs.shaft
package repository
package squeryl

import org.squeryl._
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._

import com.mishlabs.shaft.app.model.KeyedModel

import dao.Dao

protected abstract class SquerylStorage[M <: KeyedModel[Long, M], D <: Dao] extends Storage[M]
{
	val table:Table[D]
		
	def find(id:Long):Option[M] = find ( r => where(r.id === id) select(r) )
		
	def findAll():Iterable[M] = findAll( r => select(r) )
	
	def find(condition:Function1[D,QueryYield[D]]):Option[M] =
	{
		val result = findAll(condition)
		result.size match
		{
			case 0 => None
			case 1 => Some(result.head)
			case count => throw new Exception("too many records matched this criteria, expected 1 fond %d".format(count))
		}		
	}
	
	def findAll(condition:Function1[D,QueryYield[D]]):Iterable[M] = inTransaction { from(table)(condition) }
	
	def save(entity:M):M = if (entity.id < 0) create(entity) else update(entity)
	
	def create(entity:M):M = inTransaction { table.insert(entity); entity }
	
	def update(entity:M):M = inTransaction { table.update(entity); entity }
		
	def delete(key:Long) { inTransaction { table.delete(key) } }
	
	def delete(keys:Iterable[Long]) { inTransaction { table.deleteWhere( r => r.id in keys ) } }
	
	/*
	private implicit def conditionToSqueryl(condition:Condition):Function1[D,QueryYield[D]] = 
	{
		condition match
		{
			case condition:By =>
			{
				r => where(r.id === condition.value) select(r)
			}
			case condition:Like =>
			{
				//r => where(r.id like condition.value) select(r)
				throw new Exception("implment this")
			}
			case condition => throw new Exception("can't or dont know how to handle condition %s".format(condition))
		}
		
	}
	*/

    val serializer:SquerylSerializer[M, D]
	
	implicit def model2dao(model:M):D = serializer.toDao(model)
	implicit def dao2model(dao:D):M = serializer.toModel(dao)
	implicit def daoit2modelit(daoit:Iterable[D]):Iterable[M] = daoit.map(dao2model(_))
    //implicit def optdate2opttimestamp(optdate:Option[Date]):Option[Timestamp] = if (optdate.isDefined) Some(new Timestamp(optdate.get.getTime)) else None
}

trait SquerylSerializer[M <: KeyedModel[Long, M], D <: Dao]
{
    def toDao(model:M):D
    def toModel(dao:D):M
}

object DaoConversions
{
    implicit def timestamp2timetamp(d:org.joda.time.DateTime):java.sql.Timestamp = new java.sql.Timestamp(d.getMillis)
    implicit def timestamp2timetamp2(d:java.sql.Timestamp):org.joda.time.DateTime = new org.joda.time.DateTime(d.getTime)
    implicit def opttimestamp2opttimetamp(od:Option[org.joda.time.DateTime]):Option[java.sql.Timestamp] = od.flatMap( d => Some(new java.sql.Timestamp(d.getMillis)))
    implicit def opttimestamp2opttimetamp2(od:Option[java.sql.Timestamp]):Option[org.joda.time.DateTime] = od.flatMap( d => Some(new org.joda.time.DateTime(d.getTime)))
}
