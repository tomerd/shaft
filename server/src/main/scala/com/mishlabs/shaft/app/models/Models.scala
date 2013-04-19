package com.mishlabs.shaft
package app.model

import java.util.Date
import java.util.UUID

trait Model

trait KeyedModel[K <: Any, T <: KeyedModel[K, T]] extends Model
{
	self:T =>
	
	var id:K = Unit.asInstanceOf[K] //FIXME: HACK
	def id(value:K):T = { this.id = value; this }		
}
