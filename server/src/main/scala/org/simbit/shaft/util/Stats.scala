package org.simbit.shaft
package util

object Stats 
{
	def incr(name:String, count:Int)
	{
		com.twitter.ostrich.stats.Stats.incr("shaft." + name, count)
	}
}