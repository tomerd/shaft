package org.simbit.shaft
package dal.squeryl
package dao

import java.util.Date
import java.util.UUID
import java.sql.Timestamp

import org.squeryl._
import org.squeryl.dsl._
import org.squeryl.PrimitiveTypeMode._

trait Dao extends KeyedEntity[Long]