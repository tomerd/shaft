package com.mishlabs.shaft
package repository
package redis

import com.redis._

import config._

abstract trait RedisStore extends DataStore 
{
	private var pool:Option[RedisClientPool] = None
	
	final def initialize(config:DataStoreConfig) = 
	{
		val redisConfig = config match
		{
			case config:RedisConfig => config
			case _ => throw new Exception("invaid configuration, expected RedisConfig") 
		}
		
		pool = Some(new RedisClientPool(redisConfig.host, redisConfig.port))
	}
	
	final def disconnect
	{
		if (pool.isDefined) pool.get.close
	}
		
	// TODO
	final def newTransaction[A](a: => A):A = a 

	// TODO	
	final def inTransaction[A](a: => A):A = a	
	
	final def withClient[A](a:RedisClient => A) = 
	{
		if (!pool.isDefined) throw new Exception("redis was not correctly initialized")
		pool.get.withClient(a)
	}	
}
