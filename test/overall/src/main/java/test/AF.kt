package test

import pers.apollokwok.tracer.common.annotations.Tracer

abstract class AF<T> : @Tracer.Omitted List<T & Any>