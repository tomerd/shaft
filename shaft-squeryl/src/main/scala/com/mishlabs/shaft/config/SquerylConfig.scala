package com.mishlabs.shaft
package config

case class SquerylConfig(driver:String, url:String, user:String, password:String) extends DataStoreConfig
{
	override def toString() = ("driver=%s, url=%s, user=%s").format( driver, url, user) 		
}