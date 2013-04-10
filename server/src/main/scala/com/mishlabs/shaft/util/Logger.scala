package com.mishlabs.shaft
package util

object LoggerConfigurator
{
	import java.util.Properties
	
	import org.apache.log4j.PropertyConfigurator
	import org.apache.log4j.LogManager
	import org.apache.log4j.Level
	
	//val defaultConsoleFormat = "%d{ABSOLUTE} %5p %c{1} - %m%n"
	val defaultConsoleFormat = "%m%n"
	val defaultFileFormat 	 = "%d{DATE} %5p %c{1} - %m%n"
	  				
	def default() = configure(Level.INFO)
	  
	def configure(level:Level):Unit = configure(level, None, None, None, None)
	
	def configure(level:Level, maxFileSize:Int, maxFiles:Int):Unit = configure(level, Some(maxFileSize), Some(maxFiles), None, None)
	
	def configure(level:Level, maxFileSize:Option[Int], maxFiles:Option[Int], fileFormat:Option[String], consoleFormat:Option[String]):Unit =
	{
		val props = new Properties() 
		
		val maxFileSize1 = maxFileSize match
		{
		  	case Some(value) => value
		  	case _ => 1
		}
		
		val maxFiles1 = maxFiles match
		{
		  	case Some(value) => value
		  	case _ => 10
		}
		
		val fileFormat1 = fileFormat match
		{
		  	case Some(value) => value
		  	case _ => defaultFileFormat
		}
		
		val consoleFormat1 = consoleFormat match
		{
		  	case Some(value) => value
		  	case _ => defaultConsoleFormat
		}
		
		props.setProperty("log4j.rootLogger", "%s, stdout, file".format(level))
	
		// console
		props.setProperty("log4j.appender.stdout", "org.apache.log4j.ConsoleAppender")
		props.setProperty("log4j.appender.stdout.layout", "org.apache.log4j.PatternLayout")
		props.setProperty("log4j.appender.stdout.layout.ConversionPattern", consoleFormat1)
		
		// log file
		props.setProperty("log4j.appender.file", "org.apache.log4j.RollingFileAppender")
		props.setProperty("log4j.appender.file.File", "log/element.log")
		props.setProperty("log4j.appender.file.MaxFileSize",  "%sMB".format(maxFileSize1))
		props.setProperty("log4j.appender.file.MaxBackupIndex", maxFiles1.toString)
		props.setProperty("log4j.appender.file.layout", "org.apache.log4j.PatternLayout")
		props.setProperty("log4j.appender.file.layout.ConversionPattern", fileFormat1)
		
		LogManager.resetConfiguration()
		PropertyConfigurator.configure(props)
	}
}

trait Logger 
{
	import org.slf4j.{Marker, Logger => SLF4JLogger, LoggerFactory}

	private lazy val logger:SLF4JLogger = LoggerFactory.getLogger(loggerNameFor(this.getClass))
	
	private def loggerNameFor(cls:Class[_]) = 
	{
		val className = cls.getName
		if (className endsWith "$") className.substring(0, className.length - 1) else className
  	}
	
	def assertLog(assertion: Boolean, msg: => String) = if (assertion) info(msg)
	
	/**
	 * Log the value of v with trace and return v. Useful for tracing values in expressions
	 */
	def trace[T](msg:String, v:T):T = 
	{
		logger.trace(msg+": "+v.toString)
		v
	}
  
  	def trace(msg: => AnyRef):String = { if (isTraceEnabled) logger.trace(String.valueOf(msg)); String.valueOf(msg) }
  	def trace(msg: => AnyRef, t: Throwable):String = { if (isTraceEnabled) logger.trace(String.valueOf(msg), t); String.valueOf(msg) }
  	def trace(msg: => AnyRef, marker:  Marker):String = { if (isTraceEnabled) logger.trace(marker,String.valueOf(msg)); String.valueOf(msg) }
  	def trace(msg: => AnyRef, t: Throwable, marker: => Marker):String = { if (isTraceEnabled) logger.trace(marker,String.valueOf(msg), t); String.valueOf(msg) }
  	def isTraceEnabled = logger.isTraceEnabled
  
  	def debug(msg: => AnyRef):String = { if (isDebugEnabled) logger.debug(String.valueOf(msg)); String.valueOf(msg) }
  	def debug(msg: => AnyRef, t:  Throwable):String = { if (isDebugEnabled) logger.debug(String.valueOf(msg), t); String.valueOf(msg) }
  	def debug(msg: => AnyRef, marker: Marker):String = { if (isDebugEnabled) logger.debug(marker, String.valueOf(msg)); String.valueOf(msg) }
  	def debug(msg: => AnyRef, t: Throwable, marker: Marker):String = { if (isDebugEnabled) logger.debug(marker, String.valueOf(msg), t); String.valueOf(msg) }
  	def isDebugEnabled = logger.isDebugEnabled
  
  	def info(msg: => AnyRef):String = { if (isInfoEnabled) logger.info(String.valueOf(msg)); String.valueOf(msg) }
  	def info(msg: => AnyRef, t: => Throwable):String = { if (isInfoEnabled) logger.info(String.valueOf(msg), t); String.valueOf(msg) }
  	def info(msg: => AnyRef, marker: Marker):String = { if (isInfoEnabled) logger.info(marker,String.valueOf(msg)); String.valueOf(msg) }
  	def info(msg: => AnyRef, t: Throwable, marker: Marker):String = { if (isInfoEnabled) logger.info(marker,String.valueOf(msg), t); String.valueOf(msg) }
  	def isInfoEnabled = logger.isInfoEnabled
  
  	def warn(msg: => AnyRef):String = { if (isWarnEnabled) logger.warn(String.valueOf(msg)); String.valueOf(msg) }
  	def warn(msg: => AnyRef, t: Throwable):String = { if (isWarnEnabled) logger.warn(String.valueOf(msg), t); String.valueOf(msg) }
  	def warn(msg: => AnyRef, marker: Marker):String = { if (isWarnEnabled) logger.warn(marker,String.valueOf(msg)); String.valueOf(msg) }
  	def warn(msg: => AnyRef, t: Throwable, marker: Marker):String = { if (isWarnEnabled) logger.warn(marker,String.valueOf(msg), t); String.valueOf(msg) }
  	def isWarnEnabled = logger.isWarnEnabled
  
  	def error(msg: => AnyRef):String = { if (isErrorEnabled) logger.error(String.valueOf(msg)); String.valueOf(msg) }
  	def error(msg: => AnyRef, t: Throwable):String = { if (isErrorEnabled) logger.error(String.valueOf(msg), t); String.valueOf(msg) }
  	def error(msg: => AnyRef, marker: Marker):String = { if (isErrorEnabled) logger.error(marker,String.valueOf(msg)); String.valueOf(msg) }
  	def error(msg: => AnyRef, t: Throwable, marker: Marker):String = { if (isErrorEnabled) logger.error(marker,String.valueOf(msg), t); String.valueOf(msg) }
  	def isErrorEnabled = logger.isErrorEnabled	
}