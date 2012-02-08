package org.simbit.shaft
package app.controllers

import scala.collection._
import scala.xml._

import com.google.inject.Inject

import app.views.TemplatedView
import app.model.Model

import common._
import util._

protected trait Controller
{
	@Inject private var transactionManager:TransactionManager = null
	@Inject var request:Request = null
	@Inject var session:Session = null
	
	// before API filters	
	def beforeFilter(api:String):Unit = Unit
		
	private var _skipBeforeFilter:Seq[String] = Nil
	def skipBeforeFilter = _skipBeforeFilter
	final protected def skipBeforeFilter(apis:String*) = _skipBeforeFilter = apis
			
	// transactions
	final protected def newTransaction[A](a: => A):A = transactionManager.newTransaction(a)
	final protected def inTransaction[A](a: => A):A = transactionManager.inTransaction(a)
	
	// views	
	protected def buildView(data:AnyRef, view:TemplatedView):Response = XmlResponse(ViewBuilder.build(data, view))
	protected def buildView(data:Iterable[AnyRef], nodeName:String):Response = XmlResponse(ViewBuilder.build(data, nodeName))
	protected def buildView(entity:Model, viewName:Option[String]):Response = XmlResponse(ViewBuilder.build(entity, viewName))	
	protected def buildView(entities:Iterable[_ <: Model], viewName:Option[String]):Response = buildView(entities, None, viewName)
	protected def buildView(entities:Iterable[_ <: Model], nodeName:Option[String], viewName:Option[String]):Response = XmlResponse(ViewBuilder.build(entities, nodeName, viewName)) 
			
	// core APIs, to be overridden by sub classes
	protected def list(view:Option[String]):Response = NotImplmentedResponse()
	protected def show(id:Long, view:Option[String]):Response = NotImplmentedResponse()
	protected def create(view:Option[String]):Response = NotImplmentedResponse()
	protected def update(id:Long, view:Option[String]):Response = NotImplmentedResponse()
	protected def destroy(id:Long):Response = NotImplmentedResponse()
		
	private implicit def optxml2response(optxml:Option[Elem]):Response = optxml match
	{
		case Some(xml) => XmlResponse(xml)
		case None => EmptyResponse()
	}
	
}




