package com.mishlabs.shaft
package app.controllers

import java.lang.reflect.Method

import scala.collection._
import scala.xml._

import com.google.inject.Inject

import repository.TransactionManager

import app.model.Model

import common._
import util._
import net.liftweb.json.JValue

protected trait Controller
{	
	@Inject protected var server:Server = null
	@Inject protected var request:Request = null
	@Inject protected var session:Session = null
	@Inject private var transactionManager:TransactionManager = null
	
	// before API filters	
	def beforeFilter(api:Method):Unit = Unit
		
	private var _skipBeforeFilter:Seq[String] = Nil
	def skipBeforeFilter = _skipBeforeFilter
	final protected def skipBeforeFilter(apis:String*) = _skipBeforeFilter = apis
			
	// transactions
	final protected def newTransaction[A](a: => A):A = transactionManager.newTransaction(a)
	final protected def inTransaction[A](a: => A):A = transactionManager.inTransaction(a)
				
	// core APIs, to be overridden by sub classes
	protected def list(view:Option[String]):Response = NotImplmentedResponse()
	protected def show(id:Long, view:Option[String]):Response = NotImplmentedResponse()
	protected def create(view:Option[String]):Response = NotImplmentedResponse()
	protected def update(id:Long, view:Option[String]):Response = NotImplmentedResponse()
	protected def destroy(id:Long):Response = NotImplmentedResponse()
		
	/*
	private implicit def optxml2response(opt:Option[Elem]):Response = opt match
	{
		case Some(xml) => XmlResponse(xml)
		case None => EmptyResponse()
	}
	
	private implicit def optjson2response(opt:Option[JValue]):Response = opt match
	{
		case Some(json) => JsonResponse(json)
		case None => EmptyResponse()
	}
	*/
	
}




