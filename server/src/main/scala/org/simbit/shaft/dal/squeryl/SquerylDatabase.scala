package org.simbit.shaft
package dal
package squeryl

import org.squeryl._
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._
//import org.squeryl.annotations.Column

import config._
import util._

import dao._
 
abstract trait SquerylDatabase extends DataStore
{	
	val schema:SquerylSchema
	
	final def initialize(config:DataStoreConfig) = 
	{
		val squerylConfig = config match
		{
			case config:RdbmsConfig => config
			case _ => throw new Exception("invaid configuration, expected RdbmsConfig") 
		}
		
		SessionFactory.concreteFactory = Some(() => 
		{			
			val adapter = Class.forName(squerylConfig.driver).getName match
			{
				// TODO: map rest of squeryl drivers 
				case "org.h2.Driver" => new adapters.H2Adapter
				case "com.mysql.jdbc.Driver" => new adapters.MySQLInnoDBAdapter
				case "org.postgresql.Driver" => new adapters.PostgreSqlAdapter
				case _ => throw new Exception("unknown db driver: " + squerylConfig.driver)
			}
			val connection = java.sql.DriverManager.getConnection(squerylConfig.url, squerylConfig.user, squerylConfig.password)
			val session = Session.create(connection, adapter)
			// FIXME: should be debug
			session.setLogger((sql) => SqlLogger.info(sql) )
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

protected trait SquerylSchema extends org.squeryl.Schema
{
	override def tableNameFromClassName(tableName:String):String = StringHelpers.snakify(StringHelpers.pluralify(tableName.replace("Dao", "")))
	
	override def columnNameFromPropertyName(propertyName:String):String = StringHelpers.snakify(propertyName)
	
	override def applyDefaultForeignKeyPolicy(foreignKeyDeclaration: ForeignKeyDeclaration) = foreignKeyDeclaration.unConstrainReference
}


