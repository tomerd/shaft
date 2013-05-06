package com.mishlabs.shaft
package repository
package redis

import com.mishlabs.shaft.util.Logger

import com.redis._

import config._

trait RedisStore extends DataStore with Logger
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
		//if (!pool.isDefined) throw new Exception("redis was not correctly initialized")
		//pool.get.withClient(a)

        pool match
        {
            case Some(pool) =>
            {
                val client = pool.pool.borrowObject

                try
                {
                    a(client)
                }
                catch
                {
                    case e => throw e
                }
                finally
                {
                    pool.pool.returnObject(client)
                }
            }
            case _ => throw new Exception("redis was not correctly initialized")
        }


	}

    final def getDedicatedClient:RedisClient =
    {
        if (!config.isDefined) throw new Exception("redis was not correctly initialized")
        new RedisClient(config.get.host, config.get.port)
    }
}
