package org.simbit.shaft.util

import scala.collection._

import scala.xml._

import net.liftweb.json._

object CastingHelpers 
{
	def jsonToXml(json:JValue):Elem =
	{
		def build(json:JValue, inArray:Boolean=false):Seq[Node] =
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
					Elem(null, StringHelpers.snakify(field.name), att, TopScope, build(field.value):_*)
				}
				case JArray(array)	=>
				{
					var list = mutable.ListBuffer[Node]()
					json.children.foreach({ iterator:JValue } => list = list ++ build(iterator, true))
					list
				}
				case JObject(obj) 	=>
				{
					if (1 == json.children.size && json.children(0).isInstanceOf[JField]) return build(json.children(0))
					var list = mutable.ListBuffer[Node]()
					json.children.foreach({ iterator:JValue } => list = list ++ build(iterator))
					list
				}
				case _ => null
			}
		}
		
		build(json) match
		{
			case elem:Elem => elem
			case children:Seq[Node] => Elem(null, "xml", Null, TopScope, children:_*)
			case _ => null
		}
	}
	
	def xmlToJson(xml:Elem):JValue = 
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
		
		return JObject(List(build(xml, Some(xml.label), Nil)(0).asInstanceOf[JField]))
	}
}