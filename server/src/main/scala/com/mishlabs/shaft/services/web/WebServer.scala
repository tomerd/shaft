package com.mishlabs.shaft
package services
package web

import com.mishlabs.shaft.config.WebServerConfig
import com.mishlabs.shaft.util.Logger

protected abstract class WebServer[C <: WebServerConfig](config:C) extends Logger
{
	def startup
	def shutdown
}