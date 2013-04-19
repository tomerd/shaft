package com.mishlabs.shaft
package app.views

protected trait View

trait ViewEngine
{	
	/*
	protected def buildView(data:AnyRef, view:TemplatedView):Response = XmlResponse(ViewBuilder.build(data, view))
	protected def buildView(data:Iterable[AnyRef], nodeName:String):Response = XmlResponse(ViewBuilder.build(data, nodeName))
	protected def buildView(entity:Model, viewName:Option[String]):Response = XmlResponse(ViewBuilder.build(entity, viewName))	
	protected def buildView(entities:Iterable[_ <: Model], viewName:Option[String]):Response = buildView(entities, None, viewName)
	protected def buildView(entities:Iterable[_ <: Model], nodeName:Option[String], viewName:Option[String]):Response = XmlResponse(ViewBuilder.build(entities, nodeName, viewName))
	*/
  
	//def render(data:AnyRef, viewName:Option[String]):Any
	//def render(data:AnyRef, view:View):Any
}