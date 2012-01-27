package org.simbit.shaft
package lib.security.authentication

import util._

protected trait AuthenticationModule extends Logger
{
}

object UserNamePasswordAuthentication extends AuthenticationModule
{	
	// RFC 2307
	def encryptPassword(password:String):String =
	{
		val salt = net.liftweb.util.Helpers.randomString(10)
		val hash = hashPassword(password, salt)
		return net.liftweb.util.SecurityHelpers.base64Encode(Array.concat(hash, salt.getBytes("UTF-8")))
	}
	
	// RFC 2307
	def checkPassword(password:String, enryptedPassword:String):Boolean =
	{
		val bytes = net.liftweb.util.SecurityHelpers.base64Decode(enryptedPassword)
		val hash = new Array[Byte](20)
		Array.copy(bytes, 0, hash, 0, 20)
		val salt = new Array[Byte](bytes.length - 20)
		Array.copy(bytes, 20, salt, 0, bytes.length - 20)
		val testHash = hashPassword(password, new String(salt, "UTF-8"))
		return java.util.Arrays.equals(hash, testHash)
	}
	
	private def hashPassword(password:String, salt:String):Array[Byte] = SecurityHelpers.hash((password + salt).getBytes("UTF-8"))
}


