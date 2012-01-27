package org.simbit.shaft.util

object SecurityHelpers 
{
	// TODO: get rid of lift dependency	
	def hash(bytes:Array[Byte]):Array[Byte] = net.liftweb.util.SecurityHelpers.hash(bytes)
	
}