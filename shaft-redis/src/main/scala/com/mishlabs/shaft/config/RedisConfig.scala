package com.mishlabs.shaft
package config

case class RedisConfig(host:String, port:Int) extends DataStoreConfig
{
	override def toString() = ("host=%s, port=%s").format( host, port ) 		
}