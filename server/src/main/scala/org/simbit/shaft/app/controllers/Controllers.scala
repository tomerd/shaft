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
	// transaction manager
	@Inject private var transactionManager:TransactionManager = null
	@Inject var request:Request = null
	@Inject var session:Session = null
		
	// transactions
	protected final def newTransaction[A](a: => A):A = transactionManager.newTransaction(a)
	protected final def inTransaction[A](a: => A):A = transactionManager.inTransaction(a)
	
	// views	
	def buildView(data:AnyRef, view:TemplatedView):Response = ViewBuilder.build(data, view)
	def buildView(data:Iterable[AnyRef], nodeName:String):Response = ViewBuilder.build(data, nodeName)
	def buildView(entity:Model, viewName:Option[String]):Response = ViewBuilder.build(entity, viewName)	
	def buildView(entities:Iterable[_ <: Model], viewName:Option[String]):Response = buildView(entities, None, viewName)
	def buildView(entities:Iterable[_ <: Model], nodeName:Option[String], viewName:Option[String]):Response = ViewBuilder.build(entities, nodeName, viewName) 
	
	private implicit def optxml2response(optxml:Option[Elem]):Response =
	{
		optxml match
		{
			case Some(xml) => XmlResponse(xml)
			case None => EmptyResponse()
		}
	}	
	
	// core APIs, to be overridden by sub classes
	protected def list(view:Option[String]):Response = NotImplmentedResponse()
	protected def show(id:Long, view:Option[String]):Response = NotImplmentedResponse()
	protected def create(view:Option[String]):Response = NotImplmentedResponse()
	protected def update(id:Long, view:Option[String]):Response = NotImplmentedResponse()
	protected def destroy(id:Long):Response = NotImplmentedResponse()
}

protected trait TransactionManager
{
	def newTransaction[A](a: => A):A
	def inTransaction[A](a: => A):A
}




