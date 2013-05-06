package com.mishlabs.shaft
package app

case class PermissionDeniedException(reason:String) extends Exception(reason)
case class AccessDeniedException extends Exception
case class NotImplmentedException extends Exception
case class ApiException(description:String) extends Exception(description)
case class ValidationException(reason:String) extends Exception(reason)
case class NotFoundException(reason:String) extends Exception(reason)
case class ViewException(reason:String) extends Exception(reason)
		
	