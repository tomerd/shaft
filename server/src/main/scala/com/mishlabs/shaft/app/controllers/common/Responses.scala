package com.mishlabs.shaft
package app.controllers
package common

import scala.collection._
import scala.xml._

// TODO: look into a better json library
//import net.liftweb.json._
import net.liftweb.json.DefaultFormats
import net.liftweb.json.JsonParser
import net.liftweb.json.JsonDSL._
import net.liftweb.json.JsonAST._
import net.liftweb.json.Extraction._

import util._

trait Response

protected case class StringResponse(string:String) extends Response
{
	override def toString():String = string
}

trait XmlResponse extends Response
{
	def toXml():Elem
}

trait JsonResponse extends Response
{
	def toJson():JValue
}

/*
protected abstract class Response
{
	// to be overridden by sub-classes
	def toXml():Elem
	
	// copied from net.liftweb.json.Xml.toJson and changed to our needs: better handling of arrays, camel case for property names
	//final def toJson(view:String=null):JValue = net.liftweb.json.Xml.toJson(this.toXml(view))
	def toJson():JValue = 
	{
		def childElems(n:Node) = n.child.filter(_.getClass == classOf[Elem])
		
		def build(root:NodeSeq, fieldName:Option[String], argStack:List[JValue], noName:Boolean=false):List[JValue] = root match 
		{
			case n:Node =>
			{
				if (!n.attribute("arrayitem").isEmpty) 
				{
					JString(n.text) :: argStack
				}
				else if (1 == n.descendant.size)
				{
					JField(StringHelpers.camelifyMethod(n.label), JString(n.text)) :: argStack
				}
				else 
				{
					val arrayHint = !n.attribute("array").isEmpty
					val children = childElems(n)
					val allLabels = children.map(_.label)
					val sameLabel = allLabels.size != 1 && allLabels.toList.removeDuplicates.size == 1
					if (sameLabel || arrayHint)
					{
						val arr = JArray(build(childElems(n), Some(n.label), Nil, true))
						(fieldName match 
						{
							case Some(f) => JField(f, arr)
							case None => JField(allLabels(0), arr)
						}) :: argStack
					}
					else
					{
						val obj = JObject(build(childElems(n), Some(n.label), Nil).asInstanceOf[List[JField]])
						(fieldName match 
						{
							case Some(f) => JField(f, obj)
							case None => obj
						}) :: argStack
					}
				}
			}
			case s:NodeSeq => s.toList.flatMap(e => build(e, if (noName) None else Some(e.label), Nil))
		}
		
		val xml = this.toXml(/*view*/)
		return JObject(List(build(xml, Some(xml.label), Nil)(0).asInstanceOf[JField]))
	}
}
*/

protected object XmlResponse 
{ 
	def apply(xml:String):XmlResponse = 
	{
		try
		{
			if (null == xml) return EmptyResponse()
			if (0 == xml.size) return EmptyResponse()
			this.apply(XML.loadString(xml))
		}
		catch
		{
			case e => Failed(e)
		}
	}
	
	def apply(xml:Elem):XmlResponse = new XmlResponseImpl(xml)
}

private class XmlResponseImpl(xml:Elem) extends XmlResponse
{
	def toXml():Elem = xml
}

protected object JsonResponse
{
	def apply(json:String):JsonResponse = 
	{
		try
		{
			if (null == json) return EmptyResponse()
			if (0 == json.size) return EmptyResponse()
			this.apply(JsonParser.parse(json))
		}
		catch
		{
			case e => Failed(e)
		}
	}
	
	def apply(json:JValue):JsonResponse = new JsonResponseImpl(json)
}

private class JsonResponseImpl(json:JValue) extends JsonResponse
{
	/*
	override def toXml():Elem = 
	{
		jsonToXml(json) match
		{
			case elem:Elem => elem
			case children:Seq[Node] => Elem(null, "xml", Null, TopScope, children:_*)
			case _ => null
		}
	}
	*/
	
	def toJson():JValue = json
	
	// TODO: this is somewhat of hack, need to find a library that does this type of transformations out of the box
	/*
	private def jsonToXml(json:JValue, inArray:Boolean=false):Seq[Node] =
	{
		json match
		{
			case null => null
			case JNull => null
			case JNothing => null
			case value:JBool   	=> if (inArray) Elem(null, "value", new UnprefixedAttribute("arrayitem", Text("true"), Null), TopScope, Seq[Node](Text(value.value.toString.toLowerCase)):_*) else Text(value.value.toString.toLowerCase)
			case value:JDouble	=> if (inArray) Elem(null, "value", new UnprefixedAttribute("arrayitem", Text("true"), Null), TopScope, Seq[Node](Text(value.num.toString)):_*) else Text(value.num.toString)
			case value:JInt		=> if (inArray) Elem(null, "value", new UnprefixedAttribute("arrayitem", Text("true"), Null), TopScope, Seq[Node](Text(value.num.toString)):_*) else Text(value.num.toString)
			case value:JString	=> if (inArray) Elem(null, "value", new UnprefixedAttribute("arrayitem", Text("true"), Null), TopScope, Seq[Node](Text(value.s)):_*) else Text(value.s)
			case field:JField	=> 
			{
				val att = if (field.value.isInstanceOf[JArray]) new UnprefixedAttribute("array", Text("true"), Null) else Null
				Elem(null, StringHelpers.snakify(field.name), att, TopScope, jsonToXml(field.value):_*)
			}
			case JArray(array)	=>
			{
				var list = mutable.ListBuffer[Node]()
				json.children.foreach({ iterator:JValue } => list = list ++ jsonToXml(iterator, true))
				list
			}
			case JObject(obj) 	=>
			{
				if (1 == json.children.size && json.children(0).isInstanceOf[JField]) return jsonToXml(json.children(0))
				var list = mutable.ListBuffer[Node]()
				json.children.foreach({ iterator:JValue } => list = list ++ jsonToXml(iterator))
				list
			}
			case _ => null
		}
	}
	*/
}

protected case class EmptyResponse extends XmlResponse with JsonResponse
{
	def toXml():Elem = <empty/>
	def toJson():JValue = new JObject(Nil)
}

protected object NotImplmentedResponse 
{
	def apply():Response = XmlResponse(<not_implemented/>)
}

protected object Succeeded 
{
	def apply():Response = XmlResponse(<success/>)
}

case class Error(kind:String, description:Option[String]=None) extends XmlResponse with JsonResponse
{
	def toXml():Elem = <error>
						 	<type>{ kind }</type>
							<description>{ description.getOrElse("") }</description>
						</error>
									
	def toJson():JValue = CastingHelpers.xmlToJson(this.toXml())
									
	override def toString():String = "%s:%s".format(kind, description.getOrElse(""))
}

protected object Failed
{
	def apply(reason:Throwable):Error = apply(ExceptionUtil.describe(reason))
	def apply(reason:String):Error = Error("general_error", Some(reason))
}

protected object BadInput
{
	def apply(reason:String) = Error("bad_input", Some(reason))
}

protected object NotFound
{
	def apply(reason:String) = Error("not_found", Some(reason))
}

protected object AccessDenied
{
	def apply() = Error("access_denied")
}

protected object PermissionDenied
{
	def apply(reason:String) = Error("permission_denied", Some(reason))
}

/*
protected object LoginRequired
{
	def apply() = Error("login_required")
}

protected object UserInactive
{
	def apply() = Error("user_inactive")
}
*/

protected object BooleanResponse
{
	def apply(value:Boolean):Response = XmlResponse(<result>{ value }</result>)
}

case class IterableResponse(iterable:Iterable[XmlResponse], name:String="list") extends XmlResponse
{
	def toXml():Elem = 
	{
		var children = new mutable.ListBuffer[Node]()
		iterable.foreach({ iterator => children += iterator.toXml() })
		Elem(null, name, new UnprefixedAttribute("array", Text("true"), Null), TopScope, children:_*)
	}
}

