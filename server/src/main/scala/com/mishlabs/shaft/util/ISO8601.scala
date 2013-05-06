package com.mishlabs.shaft.util

import java.text.SimpleDateFormat
import org.joda.time.{DateTime, LocalDate, LocalTime}
import org.joda.time.format.ISODateTimeFormat

object ISO8601 
{
    def parseTimestamp(s:String):DateTime =
    {
        // normalize string
        var ns = if (s.endsWith("Z")) s.substring(0, s.size - 1) + "-00:00" else s.replaceAll("GMT", "")
        ns = math.max(ns.lastIndexOf("+"), ns.lastIndexOf("-")) - ns.indexOf("T") match
        {
            case 13 => ns
            case 9 => "%s.000%s".format(ns.substring(0, ns.size - 5), ns.substring(ns.size - 5, ns.size))
            case _ => throw new Exception("invalid ISO8601 date")
        }
        ISODateTimeFormat.dateTime.parseDateTime(ns)
    }

    def parseDate(s:String):LocalDate = ISODateTimeFormat.date.parseLocalDate(s)

    def parseTime(s:String):LocalTime = ISODateTimeFormat.time.parseLocalTime(s)

    def formatTimestamp(d:DateTime):String = ISODateTimeFormat.dateTime.print(d)

    def formatDate(d:LocalDate):String = ISODateTimeFormat.date.print(d)

    def formatTime(t:LocalTime):String = ISODateTimeFormat.time.print(t)

    def formatTimestamp(d:java.util.Date):String = formatTimestamp(new DateTime(d))
    def formatTimestamp(d:Long):String = formatTimestamp(new DateTime(d))

    def formatDate(d:java.util.Date):String = formatDate(new LocalDate(d))
    def formatDate(d:Long):String = formatDate(new LocalDate(d))

    def formatTime(t:java.util.Date):String = formatTime(new LocalTime(t))
    def formatTime(t:Long):String = formatTime(new LocalTime(t))
}