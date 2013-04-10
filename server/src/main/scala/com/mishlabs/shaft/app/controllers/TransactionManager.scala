package com.mishlabs.shaft
package app.controllers

protected trait TransactionManager
{
	def newTransaction[A](a: => A):A
	def inTransaction[A](a: => A):A
}