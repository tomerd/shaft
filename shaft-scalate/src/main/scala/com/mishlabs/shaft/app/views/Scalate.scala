package com.mishlabs.shaft
package app
package views

import java.util.Date

import java.io.{ File, StringWriter, PrintWriter }

import scala.collection._

import org.fusesource.scalate._
import org.fusesource.scalate.util._

import com.mishlabs.shaft.util.StringHelpers

import model.Model

import com.mishlabs.shaft.util._

object Scalate extends ViewEngine with Logger
{
	private val engine = new TemplateEngine
	
	// defaults	
	val path = getClass.getClassLoader.getResource(".")
    if (null == path) throw new Exception ("error retrieveing core path")
	// TODO: read from config
	engine.workingDirectory = new File("%s/tmp/scalate".format(path)) 
	engine.allowReload = true
	engine.allowCaching =  true

	lazy val helpers:Map[String, Any] = 
	{
    	Map()      
	}
	
	// wrappers
	//def classpath_= (value:String) = engine.classpath = value	
	def workingDirectory_= (value:File) = engine.workingDirectory = value
	//def resourceLoader_= (value:ResourceLoader) = engine.resourceLoader = value	
	def allowReload_= (value:Boolean) = engine.allowReload = value
	def allowCaching_= (value:Boolean) = engine.allowCaching =  value

	//def startup = engine.boot	
	//def shutdown = engine.shutdown
	
	def render(model:Model, path:String):String = render(modelToMap(model), path)	
	
	def render(data:Map[String, Any], path:String):String =
	{    
    	debug("rendering scalate template '%s' with %s".format(path, data))
    	
    	var url = getClass.getClassLoader.getResource(path)
        if (null == url) 
        {        	
        	val defaultPath = "%s/app/views/%s".format(ShaftServer.server.getClass.getPackage.getName.replace(".", "/"), path)
        	url = getClass.getClassLoader.getResource(defaultPath)
        	if (null == url) throw new ViewException("view '%s' not found".format(path))
        }
    	        
    	val template = engine.load(url.getFile)
    	engine.layout(template.source, data.toMap ++ helpers)
    }
	
	private def modelToMap(model:Model):Map[String, Any] =
	{
    	val map = mutable.HashMap[String, Any]()
		val methods = model.getClass.getMethods.filter( method => (0 == method.getParameterTypes.length)
														//&&
														//view.approve(method.getName)
														&& 
														!method.getReturnType.equals(model.getClass)
														&& 
														!method.getReturnType.equals(classOf[Unit]))
		methods.foreach( method =>
		{							
			val value = try
			{								
				method.invoke(model)							 							
			}
			catch
			{
				case e => error("failed generating %s's view, exception thrown when evaluating '%s' attribute, %s".format(model.getClass.getSimpleName, method.getName, ExceptionUtil.describe(e))) 
			}
			map += StringHelpers.snakify(method.getName) -> value
		})
		
		map
	}
}