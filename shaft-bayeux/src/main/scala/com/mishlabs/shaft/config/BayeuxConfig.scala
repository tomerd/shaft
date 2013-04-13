package com.mishlabs.shaft
package config

case class BayeuxConfig(requestChannel:String, responseChannel:String)
{
	//var requestChannel:String = "/request"
	//var responseChannel:String = "/response"
	
	override def toString() = "requestChannel=%s, responseChannel=%s".format(requestChannel, responseChannel)
}