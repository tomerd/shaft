package org.simbit.shaft
package app.controllers
package common

import scala.collection._

protected trait Request 
{
	val serverName:String
	val secured:Boolean
	val params:RequestParams
	
}

protected trait RequestParams extends Map[String, String]