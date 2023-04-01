package multibounds

import pers.apollokwok.tracer.common.annotations.Tracer

abstract class AF<T> : @Tracer.Omit List<T & Any>