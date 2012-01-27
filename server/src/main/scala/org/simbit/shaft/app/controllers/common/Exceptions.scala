package org.simbit.shaft
package app.controllers
package common

//protected case class LoginRequiredException extends Exception
//protected case class UserInactiveException extends Exception
protected case class PermissionDeniedException(reason:String) extends Exception(reason)
protected case class AccessDeniedException extends Exception
protected case class NotImplmentedException extends Exception
protected case class ApiException(description:String) extends Exception(description)
protected case class ValidationException(reason:String) extends Exception(reason)
protected case class NotFoundException(reason:String) extends Exception(reason)
protected case class ViewException(reason:String) extends Exception(reason)
		
	