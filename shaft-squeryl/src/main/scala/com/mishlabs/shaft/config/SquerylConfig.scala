package com.mishlabs.shaft
package config

abstract class DatabaseAdapter(val klass:Class[_ <: org.squeryl.internals.DatabaseAdapter])
object DB2Adapter extends DatabaseAdapter(classOf[org.squeryl.adapters.DB2Adapter])
object DerbyAdapter extends DatabaseAdapter(classOf[org.squeryl.adapters.DerbyAdapter])
object H2Adapter extends DatabaseAdapter(classOf[org.squeryl.adapters.H2Adapter])
object MSSQLServer extends DatabaseAdapter(classOf[org.squeryl.adapters.MSSQLServer])
object MySQLAdapter extends DatabaseAdapter(classOf[org.squeryl.adapters.MySQLAdapter])
object MySQLInnoDBAdapter extends DatabaseAdapter(classOf[org.squeryl.adapters.MySQLInnoDBAdapter])
object OracleAdapter extends DatabaseAdapter(classOf[org.squeryl.adapters.OracleAdapter])
object PostgreSqlAdapter extends DatabaseAdapter(classOf[org.squeryl.adapters.PostgreSqlAdapter])

case class SquerylConfig extends DataStoreConfig
{
	var adapter:DatabaseAdapter = null
	var url:String = null
	var user:String = null
	var password:String = null
	
	override def toString() = ("adapter=%s, url=%s, user=%s").format( adapter.getClass.getSimpleName, url, user)
}
