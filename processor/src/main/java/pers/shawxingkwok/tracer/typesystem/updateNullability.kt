package pers.shawxingkwok.tracer.typesystem

// There may be some type inference error when putting this function in class Type.
@Suppress("UNCHECKED_CAST")
internal fun <T: Type<*>> T.updateNullability(isNullable: Boolean): T =
    when{
        isNullable == this.isNullable -> this
        this is Type.Generic -> copy(isNullable = isNullable)
        this is Type.Alias -> copy(isNullable = isNullable)
        this is Type.Specific -> copy(isNullable = isNullable)
        this is Type.Compound -> copy(isNullable = isNullable)
        else -> error("")
    } as T