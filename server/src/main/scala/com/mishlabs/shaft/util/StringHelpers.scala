package com.mishlabs.shaft.util

object StringHelpers 
{
	// TODO: get rid of lift dependency
	def snakify(string:String):String = net.liftweb.util.StringHelpers.snakify(string)

	// TODO: naive pluralization. find a string helper that can do a better job
	def pluralify(verb:String):String =
	{
		if (verb.endsWith("s")) verb + "es"
		else verb + "s"			
	}
	
	def camelify(string:String):String = net.liftweb.util.StringHelpers.camelify(string)
	
	def camelifyMethod(string:String):String = net.liftweb.util.StringHelpers.camelifyMethod(string)
	
}