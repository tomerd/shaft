package org.simbit.shaft
package util

protected object ExceptionUtil
{
	def describe(throwable:Throwable):String = 
	{
		if (null != throwable.getCause) return describe(throwable.getCause)
		if (null != throwable.getMessage) return throwable.getMessage
		return throwable.toString
	}
}