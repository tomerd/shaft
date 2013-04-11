package com.mishlabs.shaft
package repository
package redis

import scala.collection._

import com.mishlabs.shaft.app.model.Model

protected abstract class RedisStorage[T <: Model] extends Storage[T]
{
}