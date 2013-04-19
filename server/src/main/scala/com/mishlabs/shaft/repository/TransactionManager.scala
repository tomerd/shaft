package com.mishlabs.shaft
package repository

trait TransactionManager
{
	def newTransaction[A](a: => A):A
	def inTransaction[A](a: => A):A
}