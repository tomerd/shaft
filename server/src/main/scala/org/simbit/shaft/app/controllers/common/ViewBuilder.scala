package org.simbit.shaft
package app.controllers
package common

import java.util.Date
import java.sql.Timestamp
	
import scala.collection._
import scala.xml._

import app.model.Model
import app.views._

import util._
	
protected object ViewBuilder
{
	private val DEFAULT_VIEW_NAME = "default"
	// FIXME: read formats from configuration file
	private val dateFormat = "dd/MMM/yyyy"
	private val dateFormatter = new java.text.SimpleDateFormat(dateFormat)
	private val timestampFormat = "%s HH:mm:ss".format(dateFormat)	
	private val timestampFormatter = new java.text.SimpleDateFormat(timestampFormat)
	
	def build(data:AnyRef, view:TemplatedView):Option[Elem] =
	{
		// FIXME: implement custom view using a template engine such as scalate
		throw new Exception("not implmented")
	}
		
	def build(data:Iterable[AnyRef], nodeName:String):Option[Elem] =
	{
		// TODO: this should not be used with model objects need to validate it here		
		objectToXml(data, nodeName, None)
	}

	def build(entity:Model, viewName:Option[String]):Option[Elem] = 
	{		
		modelToXml(entity, None, viewName)
	}
	
	def build(entities:Iterable[_ <: Model], nodeName:Option[String], viewName:Option[String]):Option[Elem] = 
	{		
		objectToXml(entities, nodeName.getOrElse("list"), viewName)
	}
	
	private def objectToXml(data:Any, nodeName:String, viewName:Option[String]):Option[Elem] =
	{
		//val nodeName:String = StringHelpers.snakify(rootNodeName)
		//info("processing " + nodeName + " value: " + value)
		 
		data match
		{
			case null => Some(Elem(null, nodeName, Null, TopScope))
			case model:Model => modelToXml(model, Some(nodeName), viewName)
			case option:Option[_] => objectToXml(option.getOrElse(null), nodeName, viewName)
			case iterable:Iterable[_] => 
			{
				//info("seq of " + seq.size)
				var children = mutable.ListBuffer[Node]()
				iterable.foreach( iterator => 
				{
					val child = iterator match
					{
						// FIXME: find a nicer way to do this
						/*
						case null => None
						case value:String if value.isEmpty => None
						case value:String => primitiveArrayItemToXml(value.asInstanceOf[AnyRef])
						case value:Boolean => primitiveArrayItemToXml(value.asInstanceOf[AnyRef])
						case value:Integer => primitiveArrayItemToXml(value.asInstanceOf[AnyRef])
						case value:Long => primitiveArrayItemToXml(value.asInstanceOf[AnyRef])
						case value:Short => primitiveArrayItemToXml(value.asInstanceOf[AnyRef])
						case value:Double => primitiveArrayItemToXml(value.asInstanceOf[AnyRef])
						case value:Float => primitiveArrayItemToXml(value.asInstanceOf[AnyRef])
						case value:Timestamp => timestampArrayItemToXml(value)
						case value:Date => dateArrayItemToXml(value)
						*/
						case model:Model => modelToXml(model, None, viewName)
						case value:Timestamp => timestampArrayItemToXml(value)
						case value:Date => dateArrayItemToXml(value)
						case value:Any if (value.getClass.isPrimitive) => primitiveArrayItemToXml(value)						
						case _ => objectToXml(iterator, nodeName, viewName)
						
					}
					if (child.isDefined) children += child.get
				})
				Some(Elem(null, nodeName, new UnprefixedAttribute("array", Text("true"), Null), TopScope, children:_*))
			}
			case timestamp:Timestamp => Some(Elem(null, nodeName, Null, TopScope, Text(formatTimestamp(timestamp))))
			case date:Date => Some(Elem(null, nodeName, Null, TopScope, Text(formatDate(date))))			
			case generic:Any => Some(Elem(null, nodeName, Null, TopScope, Text(generic.toString)))
		}
	}
	
	private def primitiveArrayItemToXml(value:Any) = arrayItemToXml(value.toString)
	
	private def timestampArrayItemToXml(timestamp:Timestamp) = arrayItemToXml(formatTimestamp(timestamp))
	private def dateArrayItemToXml(date:Date) = arrayItemToXml(formatDate(date))
	
	private def arrayItemToXml(text:String) = Some(Elem(null, "value", new UnprefixedAttribute("arrayitem", Text("true"), Null), TopScope, Text(text)))
	
	private def formatTimestamp(timestamp:Timestamp):String = if (null != timestamp) timestampFormatter.format(timestamp) else ""
	private def formatDate(date:Date):String = if (null != date) dateFormatter.format(date) else ""
	
	private def modelToXml(model:Model, nodeName1:Option[String], viewName:Option[String]):Option[Elem] =
	{		
		val modelName = StringHelpers.snakify(model.getClass.getSimpleName)
		val nodeName = nodeName1.getOrElse(modelName)
		val directoryName = StringHelpers.pluralify(modelName)	
		
		val view = viewName match 
		{
			case Some(name) => findView(directoryName, name)
			case None => findView(directoryName, DEFAULT_VIEW_NAME)
		}
		
		view match
		{
			case view:TemplatedView => build(model, view)
			case view:FieldsView => 
			{
				var children = mutable.ListBuffer[Node]()
				model.getClass.getMethods.filter( 	method => (0 == method.getParameterTypes.length)
													&&
													view.approve(method.getName)
													&& 
													!method.getReturnType.equals(model.getClass))
					.foreach( method =>
					{							
						// FIXME: add support to specifying the child view name as part of the parent view definition 
						// this means the view's definition will be more robust then a simple list of fields
						val childView = None 
							
						try
						{								
							var value = method.invoke(model)
							objectToXml(value, StringHelpers.snakify(method.getName), childView) match
							{
								case Some(child) => children += child
								case _ => // do nothing
							}
						}
						catch
						{
							case e => throw new ViewException("failed generating %s's xml, exception thrown when evaluating '%s' attribute, %s".format(model.getClass.getSimpleName, method.getName, ExceptionUtil.describe(e)) )
						}
						//}
					})
					Some(Elem(null, nodeName, Null, TopScope, children:_*))
			}
			//case None => objectToXml(data, nodeName, Some(new BlackListView()))						
			case _ => throw new ViewException("unknown view type " + view)
		}	
	}
	
	private def findView(directory:String, name:String):View =
	{		
		val packageName = ShaftServer.server.getClass.getPackage.getName
		val viewClassName = "%s.app.views.%s.%s".format(packageName, directory, StringHelpers.camelify(name))
		
		val view = try 
		{
			val clazz = Class.forName(viewClassName)
			clazz.getConstructor().newInstance()
		}
		catch
		{
			case e:ClassNotFoundException => if (DEFAULT_VIEW_NAME == name) new BlackListView() else throw new ViewException("view '%s' not found, expected at app/views/%s/%s".format(name, directory, StringHelpers.camelify(name)))
			case e:NoSuchMethodException => throw new ViewException("view '%s' was found, but could not be instantiated. make sure it has a simple constructor")
			case e:SecurityException => throw new ViewException("view '%s' was found, but could not be instantiated due to access/security issue")
			case e:IllegalAccessException => throw new ViewException("view '%s' was found, but could not be instantiated due to access/security issue")
			case e:IllegalArgumentException => throw new ViewException("view '%s' was found, but could not be instantiated. make sure it has a simple constructor")
			case e:InstantiationException => throw new ViewException("view '%s' was found, but could not be instantiated due instantiation failure")
			case e:java.lang.reflect.InvocationTargetException => throw new ViewException("view '%s' was found, but could not be instantiated due instantiation failure")
			case e:ExceptionInInitializerError => throw new ViewException("view '%s' was found, but could not be instantiated due instantiation failure")
			case _ => throw new ViewException("view '%s' was found, but could not be instantiated due an unknown error")
		}
				
		view match
		{
			case view:View => view
			case _ => throw new ViewException("view '%s' was found but is not of expected type. make sure it extends 'View'")
		}
		
	}
	
}
