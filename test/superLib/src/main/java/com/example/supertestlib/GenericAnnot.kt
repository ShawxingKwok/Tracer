package com.example.supertestlib

annotation class GenericAnnot<T>(val i: Int)

@GenericAnnot<List<*>>(1)
class GenericAnnotCarrier