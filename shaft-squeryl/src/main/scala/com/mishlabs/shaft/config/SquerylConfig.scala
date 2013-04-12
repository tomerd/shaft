package com.mishlabs.shaft
package config

import org.squeryl.internals.DatabaseAdapter

case class SquerylConfig extends DataStoreConfig
{
	//var driver:String = null
	var adapter:Class[_ <: DatabaseAdapter] = null;
	var url:String = null
	var user:String = null
	var password:String = null
	
	//override def toString() = ("driver=%s, url=%s, user=%s").format( driver, url, user)
	override def toString() = ("adapter=%s, url=%s, user=%s").format( adapter, url, user)
}
