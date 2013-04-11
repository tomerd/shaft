package com.mishlabs.shaft
package config

case class SquerylConfig extends DataStoreConfig
{
	var driver:String = null
	var url:String = null
	var user:String = null
	var password:String = null
	
	override def toString() = ("driver=%s, url=%s, user=%s").format( driver, url, user)
}
