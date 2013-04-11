package com.mishlabs.shaft
package config

case class RedisConfig extends DataStoreConfig
{
	var host:String = null
	var port:Int = 6379
  
	override def toString() = ("host=%s, port=%s").format( host, port ) 		
}