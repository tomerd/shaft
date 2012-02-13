package org.simbit.shaft
package app.controllers
package common

import java.io.InputStream

import scala.collection._

protected trait Request 
{
	val serverName:String
	val secured:Boolean
	val params:Map[String, String]
	val uploads:Map[String, UploadedFile]
}

//protected trait RequestParams extends Map[String, String]

///protected trait UploadedFiles extends Map[String, UploadedFile]

protected trait UploadedFile
{
	val fileName:String
	val contentType:String	
	val size:Long
	val stream:InputStream
}