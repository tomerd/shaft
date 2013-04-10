package com.mishlabs.shaft
package util

object StringValue 
{
	def apply(x:String) = x
	def unapply(x:AnyRef):Option[String] = x match 
	{
		case s:String => Some(s)
		case _ => None
	}
}

object IntValue 
{
	def apply(x:Int) = x.toString
	def unapply(x:AnyRef):Option[Int] = x match 
	{
		case s:String => try { Some(s.toInt) } catch { case _ => None }
		case _ => None
	}
}

object ShortValue 
{
	def apply(x:Short) = x.toString
	def unapply(x:AnyRef):Option[Short] = x match 
	{
		case s:String => try { Some(s.toShort) } catch { case _ => None }
		case _ => None
	}
}

object LongValue
{ 
	def apply(x:Long) = x.toString 
	def unapply(x:AnyRef):Option[Long] = x match 
	{ 
		case s:String => try { Some(s.toLong) } catch { case _ => None } 
		case _ => None 
  	}
}

object FloatValue 
{
	def apply(x:Float) = x.toString
	def unapply(x:AnyRef):Option[Float] = x match 
	{
		case s:String => try { Some(s.toFloat) } catch { case _ => None }
		case _ => None
	}
}

object DoubleValue 
{
	def apply(x:Double) = x.toString
	def unapply(x:AnyRef):Option[Double] = x match 
	{
		case s:String => try { Some(s.toDouble) } catch { case _ => None }
		case _ => None
	}
}

object BooleanValue 
{
	def apply(x:Boolean) = x.toString
	def unapply(x:AnyRef):Option[Boolean] = x match 
	{
		case s:String => try { Some(s.toBoolean) } catch { case _ => None }
		case _ => None
	}
}

		
