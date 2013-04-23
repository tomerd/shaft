package com.mishlabs.shaft
package repository
package redis

import com.redis._

import config._

trait RedisStore extends DataStore
{
    private var config:Option[RedisConfig] = None
	private var pool:Option[RedisClientPool] = None
	
	final def initialize(config:DataStoreConfig)
	{
        this.config = config match
		{
			case config:RedisConfig => Some(config)
			case _ => throw new Exception("invalid configuration, expected RedisConfig")
		}
		
		pool = Some(new RedisClientPool(this.config.get.host, this.config.get.port))
	}
	
	final def disconnect()
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

    final def getDedicatedClient:RedisClient =
    {
        if (!config.isDefined) throw new Exception("redis was not correctly initialized")
        new RedisClient(config.get.host, config.get.port)
    }
}
