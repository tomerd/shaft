package org.simbit.shaft
package services
package comm
package rest

import java.util.Date

import scala.xml._
import scala.collection._
import scala.collection.JavaConversions._

import org.eclipse.jetty.servlet.ServletContextHandler
import org.eclipse.jetty.servlet.ServletHolder

import com.google.inject.{ Guice, Inject, Injector, Module, Binder } 

import services.storage.StorageService
import services.web.WebService

import app.controllers._
import app.controllers.common._
//import app.controllers.annotation._

import config._
import routes._

import util._

trait RestCommunicationService extends CommunicationService
{
}
		
class ShaftRestCommunicationService extends ShaftCommunicationService with RestCommunicationService
{
	@Inject var config:RestConfig = null
	@Inject var storageService:StorageService = null
	@Inject var webService:WebService = null
	
	//val routes = config.routes
	lazy val routes = RestRoutes(Routes.all)
	
	def startup
	{
	  	if (!config.enabled)
	  	{
	  		info("rest communication service disabled")
	  		return
	  	}
	  
		info("rest communication service starting up")
				
		val context = new ServletContextHandler(ServletContextHandler.NO_SESSIONS)
		context.setContextPath(config.path)		
		context.addServlet(new ServletHolder(new RestServlet(/*this.controllers*/)), "/*")
		webService.registerHandler(context)
		
	    info("rest communication service is up")
	}
	
	def shutdown
	{
		if (!config.enabled) return;
	  
		info("rest communication service shutting down")		
		
		info("rest communication service is down")
	}	
		
	private class RestServlet(/*controllers:Iterable[Class[_ <: Controller[_]]]*/) extends javax.servlet.http.HttpServlet
	{
		import javax.servlet.http.HttpServletRequest;
	  	import javax.servlet.http.HttpServletResponse;

	  	import java.io._
	  	
	  	implicit def xmlToString(node:scala.xml.Node):String = node.toString
	  	implicit def jsonToString(json:net.liftweb.json.JsonAST.JValue):String = net.liftweb.json.Printer.compact(net.liftweb.json.JsonAST.render(json))

		override protected def service(request:HttpServletRequest, response:HttpServletResponse)
		{               
	  		try
	  		{
	  			val sessionAccessor = new HttpSessionAccesor
		  		{
		  			val server = new ServeletHttpServerProxy(request, response)
		  			// TODO: read the token name from a configuration file
		  			val tokenName = "shaft_token" 
		  		} 
 	  			  			
	  			val restRequest = parseRequest(request) match
	  			{
	  				case Some(request) => request
	  				case None => throw new UnknownRequestException()
	  			}
	  				  			
	  			val result = invokeApi(restRequest, sessionAccessor.currentSession)	  			 
		  		response.setContentType(restRequest.contentType match
		  		{
		  			case ContentType.Xml => "text/xml"
		  			case ContentType.Json => "application/json"
		  			case _ => "text/plain"
		  		})
		  		val writer = new BufferedWriter(new OutputStreamWriter(response.getOutputStream))
		  		writer.write(result)
				writer.flush
				writer.close
	  		}
	  		catch
	  		{
	  		  	case e:BadRequestException => response.sendError(HttpServletResponse.SC_BAD_REQUEST, ExceptionUtil.describe(e))
	  		  	case e => error(e); response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ExceptionUtil.describe(e))
	  		}
		}
	  	
	  	private def parseRequest(request:HttpServletRequest):Option[RestRequest] =
	  	{
	  		var path = request.getPathInfo
  			if (null == path || path.length == 0 || "/".equals(path)) return None
  			
  			val secured = "https" == request.getProtocol
  			
  			val params = RequestParams(request.getParameterMap)
  			
  			val serverName = request.getServerName
  			
  			val view = path.indexOf(":") match
  			{  				
  			  	case index if index > 0 && index < path.length => 
  			  	{
  			  		val view = path.substring(index+1).toLowerCase()
  			  		path = path.substring(0, index)
  			  		Some(view)	
  			  	}
  			  	case _ => None
  			}
  									
  			val contentType = path.indexOf(".") match
  			{
  			  	case index if index > 0 && index < path.length =>
  			  	{
  			  		val ext = path.substring(index+1).toLowerCase()
  			  		path = path.substring(0, index) 			  	  
  			  		ext match
  			  		{
  			  		  	case "xml" => ContentType.Xml
  			  		  	case "json" => ContentType.Json
  			  		  	case _ => ContentType.Json
  			  		}  			  		
  			  	}
  			  	case _ => ContentType.Json
  			}
	  		
	  		if (path.startsWith("/")) path = path.substring(1)
	  		val parts = path.split("/")
	  		routes.get(request.getMethod + ":" + path) match
	  		{
	  			case Some(route:FullRoute) => Some(RestRequest(secured, contentType, parts(0), route, None, view, params, serverName))
	  			case None =>
	  			{	  				
	  				// FIXME: find a better way to do this	 
	  				routes.get(request.getMethod + ":" + (parts.size match
	  				{
	  					case 1 => parts(0)
	  					case 2 => parts(0) + "/:id"
	  					case 3 => parts(0) + "/:id/:api"
	  					case _ => ""
	  				})) match
					{
						case Some(route:FullRoute) => 
						{
							val api = if (":api" == route.api) parts(2) else route.api
							val id = if (route.id.isDefined && ":id" == route.id.get) Some(parts(1)) else None
							Some(RestRequest(secured, contentType, parts(0), FullRoute(route.controller, api), id, view, params, serverName))		
						}
						case _ => None
					} 				
	  			}
	  		}
	  	}
	  	
	  	private def invokeApi(request:RestRequest, session:Session):String = 
	  	{
	  		val response = invokeApi2(request, session)
	  		request.contentType match
		  	{
	  			case ContentType.Xml => 
	  			{
	  				response match
	  				{
	  					case result:XmlResponse => result.toXml() 
	  					case result:JsonResponse => CastingHelpers.jsonToXml(result.toJson())
	  					case result => throw new BadRequestException("this API does not return xml")
	  				}
	  			} 
	  			case ContentType.Json => 
  				{
  					response match
	  				{
	  					case result:XmlResponse => CastingHelpers.xmlToJson(result.toXml()) 
	  					case result:JsonResponse => result.toJson()
	  					case result => throw new BadRequestException("this API does not return json")
	  				}
  				}
	  			case _ => response.toString
	  		}
	  	}
	  	
	  	private def invokeApi2(request:RestRequest, session:Session):Response =
	  	{
	  		try
	  		{
		  		val methodName:String = StringHelpers.camelifyMethod(request.route.api)
		  		
		  		debug("processing API " + request.service + ":" + request.route.api + " as " + request.route.controller.getSimpleName + ":" + methodName)
		  				  			  		
		  		// TODO: support custom method annotations (alias, permissions, access, etc)
		  		val method = request.route.controller.getMethods.find(methodName == _.getName) match
		  		{
		  		  	case Some(method) => method
		  		  	case _ => throw new BadRequestException("unknown API %s:%s".format(request.service, request.route.api))
		  		}
		  		
		  		//val metadata = method.getAnnotation(classOf[API])
		  		//if (null == metadata) throw new BadRequestException("API %s:%s found, but metadaa (annotation is missing)".format(request.service, request.route.api))
		  		
		  		// TODO: consider moving out of here
		  		// check access rights and permissions		  		
		  		//if (metadata.sslRequired && !request.secured) throw new PermissionDeniedException("%s:%s must be accessed securly".format(request.service, request.route.api))
		  		/*
		  		val auth = authenticationContextManager.getContext()		  		
		  		auth.user match
		  		{
		  			case Some(user) => 
		  			{
		  				if (!user.active) throw new UserInactiveException()
		  				// TODO: add permissions test
		  			}
		  			// FIXME: commented out for testing
		  			case None => if (metadata.loginRequired) info("not enforcing login!!! debug mode only") // throw new LoginRequiredException() 
		  		} 
		  		*/
		  		
		  		// create instance (stateless)
		  		val controller = try
		  		{
		  			request.route.controller.getConstructor().newInstance()		  					  					  		 
		  		}
		  		catch
		  		{
		  		  	case e:NoSuchMethodException => throw new BadImplementationException("%s does not implement an simple contructor".format(request.route.controller.getSimpleName))
		  		  	case e => throw new Exception("failed creating a new instance of %s".format(request.route.controller.getSimpleName), e)		  		  	
		  		}
		  		 		  		
		  		// inject dependencies
		  		Guice.createInjector(new Module 
				{  
					def configure(binder:Binder) = 
					{				
						binder.bind(classOf[Session]).toInstance(session)
						// transaction manager
						binder.bind(classOf[TransactionManager]).toInstance(new TransactionManager
				  		{
				  			def newTransaction[A](a: => A):A = if (storageService.store.isDefined) storageService.store.get.newTransaction(a) else a
				  			def inTransaction[A](a: => A):A = if (storageService.store.isDefined) storageService.store.get.inTransaction(a) else a
				  		})
						// context
						binder.bind(classOf[Request]).toInstance(new Request
						{ 
							val serverName = request.serverName
							val params = request.params							
						})
					} 
				}, 
				// inject data services
				if (storageService.store.isDefined) storageService.store.get.servicesInjectionModule else null).injectMembers(controller)
						  		
		  		// invoke API
		  		val result = try
		  		{		  			
		  			val args = getApiArguments(method, request)
		  			
		  			storageService.store match
		  			{
		  				case Some(store) =>
	  					{
	  						store.newTransaction
			  				{
			  					method.invoke(controller, args:_*)
			  				}
	  					}
		  				case None => method.invoke(controller, args:_*)
		  			}
		  		}
		  		catch
		  		{
		  			case e:java.lang.reflect.InvocationTargetException => throw(e.getTargetException)
		  			case e:BadRequestException => throw e
		  			case e:BadImplementationException => throw e
		  			case e:ViewException => throw new BadImplementationException(e.reason)
		  		  	case e => throw new BadImplementationException("failed invoking method %s:%s".format(request.route.controller.getSimpleName, methodName), e)		  		  	
		  		}
		  		
		  		result match
	  		  	{
	  		  		case response:Response => response 
	  		  		case _ => throw new BadImplementationException("%s:%s returned unknown respose, expected RestResponse".format(request.route.controller.getSimpleName, methodName))
	  		  	}
	  		}
	  		catch
			{
	  		  	case e:BadRequestException => throw e
	  		  	case e:BadImplementationException => throw e
				//case e:LoginRequiredException => LoginRequired()
				//case e:UserInactiveException => UserInactive()				
				case e:AccessDeniedException => AccessDenied()
				case e:PermissionDeniedException => PermissionDenied(e.reason)
				case e:ApiException => Failed(e.description)
				case e:ValidationException => BadInput(e.reason)
				case e:NotFoundException => NotFound(e.reason)
				case t:Throwable => 
				{
					val response = Failed("failed invoking API " + request.service + ":" + request.route.api + ", " + ExceptionUtil.describe(t))
					error(response)
					response
				}
			}
		}
	  		  	
	  	private def getApiArguments(method:java.lang.reflect.Method, request:RestRequest):List[AnyRef] =
	  	{  			
	  		val params = method.getParameterTypes
	  		params.length match
	  		{
	  			case 0 => 
	  			{
	  				if (request.id.isDefined) throw new BadRequestException("invalid API call, API does not take id parameter")
	  				if (request.view.isDefined) throw new BadRequestException("invalid API call, API does not take view parameter")
	  				Nil
	  			}
	  			case 1 if (params(0) == classOf[Long]) =>
	  			{
	  				if (!request.id.isDefined) throw new BadRequestException("invalid API call, API expects id parameter but none was assigned")
	  				if (request.view.isDefined) throw new BadRequestException("invalid API call, API does not take view parameter")
	  				List(request.id.get.toLong.asInstanceOf[AnyRef])
	  			}
	  			case 1 if (params(0) == classOf[Option[String]]) =>
	  			{
	  				if (request.id.isDefined) throw new BadRequestException("invalid API call, API does not take id parameter")
	  				List(request.view)
	  			}
	  			case 2 =>
	  			{
	  				if (!request.id.isDefined) throw new BadRequestException("invalid API call, API expects id parameter but none was assigned")
	  				List(request.id.get.toLong.asInstanceOf[AnyRef], request.view)
	  			}
	  			case _ => throw new BadImplementationException("invalid API signature, parameters number or type do not match")	  			
	  		}	  		
	  	}
	  	
	  	private case class RestRequest(	secured:Boolean,
	  									contentType:ContentType.Value, 
	  									service:String,
	  									route:FullRoute,
	  									id:Option[String], 
	  									view:Option[String], 
	  									params:RequestParams,
	  									serverName:String)
	  	
	  	private object ContentType extends Enumeration 
	  	{
	  		type ContentType = Value
	  		val Xml, Json, Unknown = Value
	  	}
	}
}

private object RequestParams
{
	val empty:RequestParams = RequestParams(Map.empty[String,String])
	
	def apply(map:Map[_,_]):RequestParams =
	{
		apply(map.map{ case (key:String,value:Array[String]) => key -> value.reduceLeft(_ + "," + _) }.toMap[String, String])
	}
	
	def apply(map:immutable.Map[String,String]):RequestParams = new RestRequestParams(map)
}

private class RestRequestParams(map:Map[String,String]) extends RequestParams
{	  		
	def get(key:String) = map.get(key)		 
	def iterator = map.iterator
	def +[B >: String](kv:(String, B)) = map + (kv)
	def - (key:String) = map - key
}


private class BadImplementationException(description:String, cause:Throwable=null) extends Throwable(description, cause)
private class BadRequestException(description:String) extends Throwable(description)
private class UnknownRequestException extends BadRequestException("unknown request")


