package com.mishlabs.shaft
package app.views

import scala.collection._

//import controllers.common._

import app.model.Model

protected abstract class View

protected abstract class TemplatedView extends View

protected abstract class FieldsView(fields:String*) extends View
{
	def approve(field:String):Boolean  
}

private object BlackListView
{
	val coreExclude = List("wait", "notify", "notifyAll", "hashCode", "getClass", "toString")
}

protected case class BlackListView(fields:String*) extends FieldsView
{
	def this(fields:Array[String]) = this(fields:_*)
	// the arrow in fields: => Seq[String is a trick to overcome type erasure
	def this(fields: => Seq[String]) = this(fields:_*)	
	
	private val exclude = BlackListView.coreExclude ++ fields
	def approve(field:String) = !exclude.contains(field)
}


protected case class WhiteListView(fields:String*) extends FieldsView
{	
	def this(fields:Array[String]) = this(fields:_*)
	// the arrow in fields: => Seq[String is a trick to overcome type erasure
	def this(fields: => Seq[String]) = this(fields:_*)
	
	private val include = fields.dropWhile(BlackListView.coreExclude.contains(_))
	def approve(field:String) = include.contains(field)
}

/*
protected object Views
{
	//import model.Model
	
	// fight off type erasure
	case class ViewEntry1(entry:Pair[Class[_ <: Model], Pair[String, View]])
	implicit def ve1(entry:Pair[Class[_ <: Model], Pair[String, View]]) = ViewEntry1(entry)	
	case class ViewEntry2(entry:Pair[Class[_ <: Model], View])
	implicit def ve2(entry:Pair[Class[_ <: Model], View]) = ViewEntry2(entry)	
	
	private val DEFAULT_VIEW_NAME = "default"
		
	private val coreExclude = List("wait", "notify", "notifyAll", "hashCode", "getClass", "toString") 
	//private val	elementExclude = List("createdBy", "createdOn", "modifiedBy", "modifiedOn")
	private val defaultExclude = coreExclude /*++ elementExclude*/
	
	private val views = mutable.HashMap[Class[_ <: Model], mutable.HashMap[String, View]]()
	
	def += (model:Class[_ <: Model], name:String, view:View):Unit =
	{
		val normalizedview = view match 
		{
			case view:BlackListView => BlackListView((view.fields ++ defaultExclude).distinct:_*)
			case view:WhiteListView => WhiteListView(view.fields.dropWhile(defaultExclude.contains(_)):_*)
			case _ => throw new Exception("unknown view type " + view)
		}		
		
		views.get(model) match
		{
			case Some(map) =>
			{
				map.get(name) match
				{
					case Some(old) => map.update(name, normalizedview)
					case None => map += name -> normalizedview
				}
			}
			case None => views += model -> mutable.HashMap[String, View](name -> normalizedview)
		}
	}
	
	def += (wrapper:ViewEntry1):Unit = this += (wrapper.entry._1, wrapper.entry._2._1, wrapper.entry._2._2)
	def += (wrapper:ViewEntry2):Unit = this += (wrapper.entry._1, DEFAULT_VIEW_NAME, wrapper.entry._2)
	def += (model:Class[_ <: Model]):Unit = this += (model, DEFAULT_VIEW_NAME, BlackListView(defaultExclude:_*))
	
	def get(model:Class[_ <: Model], name:Option[String]):Option[View] = this.get(model, name.getOrElse(DEFAULT_VIEW_NAME))
	
	def get(model:Class[_ <: Model], name:String):Option[View] = views.get(model) match
	{
		case Some(map) => map.get(name)
		// TODO: not sure about this, maybe need to return None and force users to register their models?
		case None => Some(BlackListView(defaultExclude:_*))
	}
}
*/

/*
object standard
{
	def apply(entry:Pair[Class[_ <: Model], View]):Unit = Views += entry
	def apply(entries:Pair[Class[_ <: Model], View]*):Unit = entries.foreach( Views += _ )
}
*/