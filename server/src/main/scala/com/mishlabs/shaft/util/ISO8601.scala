package com.mishlabs.shaft.util

import java.text.SimpleDateFormat
import java.util.{ Date, TimeZone }

// copied from https://github.com/softprops/hubcat/blob/master/src/main/scala/iso8601.scala
object ISO8601 
{
  val FORMAT = "yyyy-MM-dd'T'HH:mm:ssz"

  def apply(s: String) =
  {
    new SimpleDateFormat(FORMAT).parse(
      if (s.endsWith("Z")) s.substring(0, s.size - 1) + "GMT-00:00"
      else "%sGMT%s" format(s.substring(0, s.size - 6), s.substring(s.size - 6, s.size)))
  }

  def apply(d: Date) =
  {
    (new SimpleDateFormat(FORMAT) {
      setTimeZone(TimeZone.getTimeZone( "UTC" ))
    }.format(d) match {
      case s => s.substring(0, s.size - 9) + s.substring(s.size - 6, s.size)
    }) replaceAll( "UTC", "+00:00" )
  }
}