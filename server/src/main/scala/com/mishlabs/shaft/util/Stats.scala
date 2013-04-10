package com.mishlabs.shaft
package util

object Stats 
{
	def incr(name:String, count:Int)
	{
		com.twitter.ostrich.stats.Stats.incr("%.%".format(ShaftServer.server.name, name), count)
	}
}