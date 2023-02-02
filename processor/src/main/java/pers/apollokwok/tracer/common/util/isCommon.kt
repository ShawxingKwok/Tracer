package pers.apollokwok.tracer.common.util

import pers.apollokwok.ksputil.qualifiedName
import pers.apollokwok.tracer.common.typesystem.Arg
import pers.apollokwok.tracer.common.typesystem.Type

@OptIn(ExperimentalUnsignedTypes::class)
private val commonQualifiedNames: Set<String> =
    arrayOf(
        String::class,
        Any::class,

        Short::class,
        ShortArray::class,
        UShort::class,
        UShortArray::class,

        Int::class,
        IntArray::class,
        UInt::class,
        UIntArray::class,

        Long::class,
        LongArray::class,
        ULong::class,
        ULongArray::class,

        Byte::class,
        ByteArray::class,
        UByte::class,
        UByteArray::class,

        Float::class,
        FloatArray::class,

        Double::class,
        DoubleArray::class,

        Boolean::class,
        BooleanArray::class,

        Char::class,
        CharArray::class,
    )
    .mapTo(mutableSetOf()){ it.qualifiedName!! }

// Note that List::class.qualifiedName == MutableList::class.qualifiedName, and other pairs are the same.
private val commonForableQualifiedNames: Set<String> = run {
    val withMutableChildrenClasses =
        arrayOf(
            Iterable::class,
            Collection::class,
            List::class,
            Set::class,
            Map::class,
        )

    arrayOf(
        Array::class,
        Sequence::class,
        java.util.ArrayList::class,
        java.util.ArrayDeque::class,
        java.util.LinkedList::class,
        java.util.LinkedHashSet::class,
        java.util.LinkedHashMap::class,
        java.util.HashMap::class,
    )
    .mapTo(mutableSetOf()) { it.qualifiedName!! }
    .plus(withMutableChildrenClasses.map { it.qualifiedName!! })
    .plus(withMutableChildrenClasses.map { "kotlin.collections.Mutable${it.simpleName}" })
}

internal fun Type<*>.isCommon(): Boolean =
    this is Type.Specific
    &&(decl.qualifiedName() in commonQualifiedNames
        || decl.qualifiedName() in commonForableQualifiedNames
        && args.all { arg ->
            arg is Arg.General<*>
            && arg.type is Type.Specific
            && arg.type.decl.qualifiedName() in commonQualifiedNames
        }
    )