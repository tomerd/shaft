package com.mishlabs.shaft
package app
package views
package serializers

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalTime

import scala.collection._
import scala.xml._

import model.Model

import util._


object XmlSerializer extends ViewEngine with Logger
{
	private val DEFAULT_VIEW_NAME = "default"
	//private val dateFormat = ISO8601.FORMAT.split("'T'")(0)
	//private val dateFormatter = new java.text.SimpleDateFormat(dateFormat)
	//private val timestampFormat = "%s HH:mm:ss".format(dateFormat)	
	//private val timestampFormatter = new java.text.SimpleDateFormat(ISO8601.FORMAT) //timestampFormat)
	
	def render(data:AnyRef, viewName:Option[String]):Elem =
	{
		objectToXml(data, None, viewName)
	}
	
	def render(data:AnyRef, view:View):Elem =
	{
		objectToXml(data, None, Some(view))
	}
	
	def render(data:Iterable[AnyRef], nodeName:String):Elem =
	{
		// TODO: this should not be used with model objects need to validate it here		
		objectToXml(data, Some(nodeName), None)
	}

	def render(entity:Model, viewName:Option[String]):Elem = 
	{		
		objectToXml(entity, None, viewName)
	}
	
	def render(entities:Iterable[_ <: Model], nodeName:Option[String], viewName:Option[String]):Elem = //Option[Elem] = 
	{		
		objectToXml(entities, Some(nodeName.getOrElse("list")), viewName)
	}
	
	private def objectToXml(data:Any, nodeName1:Option[String], viewDef:Option[Any] /*viewName:Option[String]*/):Elem = //Option[Elem] =
	{
		//val nodeName:String = StringHelpers.snakify(rootNodeName)
		//info("processing " + nodeName + " value: " + value)
		val nodeName = nodeName1.getOrElse(StringHelpers.snakify(data.getClass.getSimpleName))
		
		data match
		{
			case null => /*Some(*/Elem(null, nodeName, Null, TopScope)//)
			case option:Option[_] => objectToXml(option.getOrElse(null), Some(nodeName), viewDef)
			case model:Model => modelToXml(model, nodeName, viewDef)			
			case tuple:Tuple2[String, Any] => tupleToXml(tuple, nodeName, viewDef)
			case tuple:Tuple3[String, Any, Any] => tupleToXml(tuple, nodeName, viewDef)
            case time:java.sql.Date => /*Some(*/Elem(null, nodeName, Null, TopScope, Text(formatDate(new LocalDate(time))))//)
            case time:java.sql.Time => /*Some(*/Elem(null, nodeName, Null, TopScope, Text(formatTime(new LocalTime(time))))//)
            case timestamp:java.util.Date => /*Some(*/Elem(null, nodeName, Null, TopScope, Text(formatTimestamp(new DateTime(timestamp))))//)
			case timestamp:DateTime => /*Some(*/Elem(null, nodeName, Null, TopScope, Text(formatTimestamp(timestamp)))//)
			case date:LocalDate => /*Some(*/Elem(null, nodeName, Null, TopScope, Text(formatDate(date)))//)
            case time:LocalTime => /*Some(*/Elem(null, nodeName, Null, TopScope, Text(formatTime(time)))//)
			case iterable:Iterable[_] => 
			{
				//info("seq of " + seq.size)
				var children = mutable.ListBuffer[Node]()
				iterable.foreach( iterator => 
				{
					val child = iterator match
					{
						// FIXME: find a nicer way to do this
						//case model:Model => modelToXml(model, None, viewName)
                        case value:java.sql.Date => dateArrayItemToXml(new LocalDate(value))
                        case value:java.sql.Time => timeArrayItemToXml(new LocalTime(value))
                        case value:java.util.Date => timestampArrayItemToXml(new DateTime(value))
                        case value:DateTime => timestampArrayItemToXml(value)
						case value:LocalDate => dateArrayItemToXml(value)
                        case value:LocalTime => timeArrayItemToXml(value)
						case value:Any if isSimple(value) => simpleArrayItemToXml(value)						
						case _ => objectToXml(iterator, None, viewDef)					
					}
					/*(if (child.isDefined)*/ children += child //.get
				})
				/*Some(*/Elem(null, nodeName, new UnprefixedAttribute("array", Text("true"), Null), TopScope, children:_*)//)
			}
			case generic:Any => /*Some(*/Elem(null, nodeName, Null, TopScope, Text(generic.toString))//)
		}
	}
	
	private def isSimple(value:Any):Boolean = 
	{		
		if (value.getClass.isPrimitive) return true;
		value match
		{
			case v:String => true
			case v:Boolean => true
            case v:Int => true
            case v:Integer => true
			case v:Short => true
			case v:Long => true
			case v:Float => true
			case v:Double => true		
			case _ => false
		}		
	}
	
	private def simpleArrayItemToXml(value:Any) = stringArrayItemToXml(value.toString)
	
	private def timestampArrayItemToXml(timestamp:DateTime) = stringArrayItemToXml(formatTimestamp(timestamp))
	private def dateArrayItemToXml(date:LocalDate) = stringArrayItemToXml(formatDate(date))
    private def timeArrayItemToXml(time:LocalTime) = stringArrayItemToXml(formatTime(time))
	
	private def stringArrayItemToXml(text:String) = arrayItemToXml(Text(text)) /*Some(Elem(null, "value", new UnprefixedAttribute("arrayitem", Text("true"), Null), TopScope, Text(text)))*/
	
	private def arrayItemToXml(child:Node) = /*Some(*/Elem(null, "value", new UnprefixedAttribute("arrayitem", Text("true"), Null), TopScope, child)//)
	
	private def formatTimestamp(timestamp:DateTime):String = if (null != timestamp) ISO8601.formatTimestamp(timestamp) else ""
	private def formatDate(date:LocalDate):String = if (null != date) ISO8601.formatDate(date) else ""
    private def formatTime(time:LocalTime):String = if (null != time) ISO8601.formatTime(time) else ""
	
	private def tupleToXml(tuple:Product, nodeName:String, view:Option[Any]):Elem =
	{
		var children = mutable.ListBuffer[Node]()
		children += Elem(null, "key", Null, TopScope, Text(tuple.productElement(0).toString))
		for (index <- 1 until tuple.productArity)
		{
			val productElement = tuple.productElement(index)
			val grandChild = if (isSimple(productElement)) Text(productElement.toString) else objectToXml(productElement, None, view)
			children += Elem(null, "value%s".format(if(index > 1) index else ""), Null, TopScope, grandChild)			
		}
		/*Some(*/Elem(null, nodeName, Null, TopScope, children:_*)//)
	}
		
	private def modelToXml(model:Model, nodeName:String, viewDef:Option[Any]):Elem = //Option[Elem] =
	{	
		val modelName = StringHelpers.snakify(model.getClass.getSimpleName)
		//val nodeName = nodeName1.getOrElse(modelName)
		val directoryName = modelName //StringHelpers.pluralify(modelName)
		
		val view = viewDef match
		{
		  	case Some(view:SerializerView) => view
		  	case Some(name:String) => loadView(directoryName, name)
		  	case None => loadView(directoryName, DEFAULT_VIEW_NAME)
		  	case _ => throw new ViewException("unknown view definition " + viewDef)
		}
		
		val children = mutable.ListBuffer[Node]()
		val methods = model.getClass.getMethods.filter( method => (0 == method.getParameterTypes.length)
														&&
														view.approve(method.getName)
														&& 
														!method.getReturnType.equals(model.getClass)
														&& 
														!method.getReturnType.equals(classOf[Unit]))
		methods.foreach( method =>
		{							
			// FIXME: add support to specifying the child view name as part of the parent view definition 
			// this means the view's definition will be more robust then a simple list of fields
			val childView = None 
				
			val value = try
			{								
				method.invoke(model)							 							
			}
			catch
			{
				//throw new ViewException("failed generating %s's xml, exception thrown when evaluating '%s' attribute, %s".format(model.getClass.getSimpleName, method.getName, ExceptionUtil.describe(e)) )
				case e => error("failed generating %s's xml, exception thrown when evaluating '%s' attribute, %s".format(model.getClass.getSimpleName, method.getName, ExceptionUtil.describe(e))) 
			}
			children += objectToXml(value, Some(StringHelpers.snakify(method.getName)), childView)
			/*
			objectToXml(value, StringHelpers.snakify(method.getName), childView) match
			{
				case Some(child) => children += child
				case _ => // do nothing
			}
			*/						
		})
		Elem(null, nodeName, Null, TopScope, children:_*)
	}
	
	private def loadView(directory:String, name:String):SerializerView =
	{
		val packageName = ShaftServer.server.getClass.getPackage.getName
		val className = "%s.app.views.%s.%s".format(packageName, directory, StringHelpers.camelify(name))
		loadView(className)
	}
	
	private def loadView(className:String):SerializerView =
	{		
		val view = try 
		{
			val klass = Class.forName(className)
			klass.getConstructor().newInstance()
		}
		catch
		{
			case e:ClassNotFoundException => if (DEFAULT_VIEW_NAME == className.substring(className.lastIndexOf("."))) new BlackList() else throw new ViewException("view not found, expected at %s".format(className))
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
			case view:SerializerView => view
			case _ => throw new ViewException("view '%s' was found but is not of expected type. make sure it extends 'SerializerView'")
		}
		
	}
	
}