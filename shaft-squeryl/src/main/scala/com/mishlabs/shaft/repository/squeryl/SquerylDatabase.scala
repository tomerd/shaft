package com.mishlabs.shaft
package repository
package squeryl

import org.squeryl._
import org.squeryl.PrimitiveTypeMode._

import config._
import util._
 
trait SquerylDatabase extends DataStore
{	
	val schema:Schema
	
	final def initialize(config:DataStoreConfig)
	{
		val squerylConfig = config match
		{
			case config:SquerylConfig => config
			case _ => throw new Exception("invaid configuration, expected SquerylConfig") 
		}
		
		SessionFactory.concreteFactory = Some(() => 
		{
			if (null == squerylConfig.adapter) throw new Exception("unknown squeryl adpater")
			val adapter = squerylConfig.adapter.klass.newInstance
			val connection = java.sql.DriverManager.getConnection(squerylConfig.url, squerylConfig.user, squerylConfig.password)
			val session = Session.create(connection, adapter)
			session.setLogger((sql) => SqlLogger.debug(sql) )
			session
		})
		
		this.schemify()
	}
	
	private def schemify()
	{
		transaction
		{			
			try
			{
				schema.create
			}
			catch
			{
				case e => // do nothing, database already exists
			}
		}
	}
	
	final def disconnect()
	{		
		if (!Session.hasCurrentSession) return
		Session.currentSession.close
	}
		
	final def newTransaction[A](a: => A):A = __thisDsl.transaction(a) 
	
	final def inTransaction[A](a: => A):A = __thisDsl.inTransaction(a)	
	
	private object SqlLogger extends Logger
}


protected trait ShaftSchema extends org.squeryl.Schema
{
	override def tableNameFromClassName(tableName:String):String = StringHelpers.snakify(StringHelpers.pluralify(tableName.replace("Dao", "")))
	
	override def columnNameFromPropertyName(propertyName:String):String = StringHelpers.snakify(propertyName)
	
	override def applyDefaultForeignKeyPolicy(foreignKeyDeclaration: ForeignKeyDeclaration) = foreignKeyDeclaration.unConstrainReference
}

