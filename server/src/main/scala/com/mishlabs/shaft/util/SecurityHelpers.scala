package com.mishlabs.shaft.util

import java.security.MessageDigest

object SecurityHelpers 
{
	// TODO: get rid of lift dependency	
	def hash(bytes:Array[Byte]) = net.liftweb.util.SecurityHelpers.hash(bytes)
	
	def hash(string:String) = net.liftweb.util.SecurityHelpers.hash(string)
	
	def md5(string:String):String = md5(string.getBytes)

	def md5(bytes:Array[Byte]):String = 
	{
		val md5 = MessageDigest.getInstance("MD5")
		md5.reset()
		md5.update(bytes)		
		return md5.digest().map(0xFF & _).map { "%02x".format(_) }.foldLeft(""){_ + _}
	}

	
}