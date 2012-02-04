package org.simbit.shaft 
package services.comm

import java.util.Date
import java.util.UUID

import scala.collection._

import app.controllers.common.Session
import app.controllers.common.SessionImpl

private object SessionManager
{
	private val map = mutable.HashMap[String, Session]()

	def get(token:String):Option[Session] = map.get(token)
		
	def add(session:Session):Unit = map += session.token -> session
	
	def replace(oldToken:String, session:Session):Unit = 
	{
		 map -= oldToken
		 this.add(session)
	}
}

protected trait SessionAccesor
{
	// TODO: read session timeout this from the config file
	private val SESSSION_TIMEOUT = 30 //M
	private val TOKEN_TTL = 1 //M
	
	
	def currentSession:Session =
	{		
		this.getSessionToken() match
		{
			case Some(token) =>
			{
				SessionManager.get(token) match					
				{
					case Some(session) =>
					{
						val now = System.currentTimeMillis			
						// this should never really happen as the module is responsible to expire the user's session and 
						// conetxt.user should be undefined, but to be on safe side...
						if (now > session.expires.getTime) throw new Exception("session timed out, but %s did not expire it correctly".format(this))
						// set new token if needed				
						if (now - (session.expires.getTime - SESSSION_TIMEOUT*60*1000) > TOKEN_TTL*60*1000) newSession(Some(token), Some(session))
						session
					}
					case None => newSession()
				}
			}
			case None => newSession()
		}
	}
	
	private def newSession(oldToken:Option[String]=None, oldSession:Option[Session]=None):Session = 
	{
		val newToken = UUID.randomUUID.toString
		val expires = new Date(System.currentTimeMillis + SESSSION_TIMEOUT*60*1000)
		val session = if (oldSession.isDefined) oldSession.get.asInstanceOf[SessionImpl].update(newToken, expires) else new SessionImpl(newToken, expires)
		if (oldToken.isDefined) SessionManager.replace(oldToken.get, session) else SessionManager.add(session)
		setSessionToken(newToken, expires)
		session
	}
		
	def clear():Unit = clearSessionToken()
	
	protected def getSessionToken():Option[String]
	protected def setSessionToken(token:String, expires:Date):Unit
	protected def clearSessionToken():Unit
}

protected trait HttpSessionAccesor extends SessionAccesor
{
	protected val server:HttpServerProxy
	
	protected val tokenName:String
		
	final protected def getSessionToken():Option[String] = server.getCookie(tokenName)
	
	final protected def setSessionToken(token:String, expires:Date)
	{		
		val ttl = (expires.getTime - System.currentTimeMillis) / 1000	
		server.setCookie(tokenName, token, Some(ttl.toInt))
	}
	
	final protected def clearSessionToken() = server.deleteCookie(tokenName)
}

protected trait HttpServerProxy
{
	val requestServerName:String
	def getCookie(name:String):Option[String]
	def setCookie(name:String, value:String, maxAge:Option[Int])
	def deleteCookie(name:String)
}

/*
*** java servlet server implementation
 */

import javax.servlet.http._

private class ServeletHttpServerProxy(currentRequest:HttpServletRequest, currentResponse:HttpServletResponse)  extends HttpServerProxy
{	
	val requestServerName:String = currentRequest.getServerName
	
	def getCookie(name:String):Option[String] =
	{
		currentRequest.getCookies.find( _.getName == name) match
		{
			case Some(cookie) => Some(cookie.getValue)
			case _ => None
		}
	}
	
	def setCookie(name:String, value:String, maxAge:Option[Int])
	{
		val cookie = new Cookie(name, value)
		cookie.setPath(currentRequest.getContextPath)
		if (maxAge.isDefined) cookie.setMaxAge(maxAge.get)
		currentResponse.addCookie(cookie)
	}
	
	def deleteCookie(name:String)
	{
		setCookie(name, "deleted", Some(0))
	}
}

/*
protected case class AuthenticationContext(user:Option[model.User], domain:Option[model.Domain])

protected trait AuthenticationContextManager
{
	private val SESSSION_TIMEOUT = 30 //M
	private val TOKEN_TTL = 1 //M
	
	protected val userStorage:dal.UserStorageService
	protected val domainStorage:dal.DomainStorageService	
	
	def getContext():AuthenticationContext =
	{
		val context = getContext2()

		// TODO: move out of here?
		// manage session and token timestamps
		context.user match
		{
			case Some(user) =>
			{				
				val now = System.currentTimeMillis			
				// this should never really happen as the module is responsible to expire the user's session and 
				// conetxt.user should be undefined, but to be on safe side...			
				var sessionExpirationDate = user.sessionTokenExpirationDate.getOrElse(new Date(0))
				if (now > sessionExpirationDate.getTime) throw new Exception("session timed out, but %s did not expire it correctly".format(this))
				// set new token if needed				
				if (now - (sessionExpirationDate.getTime - SESSSION_TIMEOUT*60*1000) > TOKEN_TTL*60*1000) setContext(user)
			}
			case _ => // do nothing 
		}
		
		context
	}
	
	protected def getContext2():AuthenticationContext
	
	final def setContext(user:model.User):Unit = 
	{
		val expires = new Date(System.currentTimeMillis + SESSSION_TIMEOUT*60*1000)
		userStorage.update(user.rememberMe(expires))
		setContext(user, expires)		
	}

	protected def setContext(user:model.User, expires:Date):Unit
		
	def clearContext()
}

protected trait HttpAuthenticationContextManager extends AuthenticationContextManager
{
	private val tokenCookieName = "element_token"
	
	protected val server:HttpServerProxy
		
	final protected def getContext2():AuthenticationContext =
	{		
		val user = server.getCookie(tokenCookieName) match
		{
			case Some(token) => userStorage.findBySessionToken(token)
			case _ => None
		}
				
		val domain = domainStorage.findByHostName(server.requestServerName)
		
		AuthenticationContext(user, domain)		
	}
	
	final def setContext(user:model.User, expires:Date)
	{		
		val ttl = (expires.getTime - System.currentTimeMillis) / 1000	
		server.setCookie(tokenCookieName, user.sessionToken.getOrElse("unknown"), Some(ttl.toInt))
	}
	
	final def clearContext()
	{
		server.deleteCookie(tokenCookieName)
	}
}

protected trait HttpServerProxy
{
	//val contextPath:String
	//val requestUrl:String
	val requestServerName:String
	//val requestPathInfo:String
	def getCookie(name:String):Option[String]
	def setCookie(name:String, value:String, maxAge:Option[Int])
	def deleteCookie(name:String)
}

/*
*** jetty servlet implementation
 */

/*
protected class JettyHttpAuthenticationContextManager extends HttpAuthenticationContextManager
{
	protected val server = new JettyProxy()
}
*/

import javax.servlet.http._

private class JettyProxy(currentRequest:HttpServletRequest, currentResponse:HttpServletResponse)  extends HttpServerProxy
{
	
	//import org.eclipse.jetty.server.handler.ContextHandler
	
	/*
	lazy val currentRequest:HttpServletRequest = 
	{		
		// FIXME: temp for testing
		ContextHandler.getCurrentContext.getAttribute("request") match
		{
			case request:HttpServletRequest => request
			case _ => throw new Exception("unknown or invalid request")
		}
	}
	
	lazy val currentResponse:HttpServletResponse = 
	{		
		// FIXME: temp for testing
		ContextHandler.getCurrentContext.getAttribute("response") match
		{
			case response:HttpServletResponse => response
			case _ => throw new Exception("unknown or invalid response")
		}
	}
	 */
	
	//val contextPath:String = request.getContextPath
	
	//val requestUrl:String = request.getRequestURI
	
	val requestServerName:String = currentRequest.getServerName
	
	//val requestPathInfo:String = request.getPathInfo
			
	def getCookie(name:String):Option[String] =
	{
		currentRequest.getCookies.find( _.getName == name) match
		{
			case Some(cookie) => Some(cookie.getValue)
			case _ => None
		}
	}
	
	def setCookie(name:String, value:String, maxAge:Option[Int])
	{
		val cookie = new Cookie(name, value)
		cookie.setPath(currentRequest.getContextPath)
		if (maxAge.isDefined) cookie.setMaxAge(maxAge.get)
		currentResponse.addCookie(cookie)
	}
	
	def deleteCookie(name:String)
	{
		setCookie(name, "deleted", Some(0))
	}
}

*/
