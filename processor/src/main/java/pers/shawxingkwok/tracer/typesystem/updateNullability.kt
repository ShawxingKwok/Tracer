package pers.shawxingkwok.tracer.typesystem

// There may be some type inference error when putting this function in class Type.
@Suppress("UNCHECKED_CAST")
internal fun <T: Type<*>> T.updateNullability(nullable: Boolean): T =
    when{
        nullable == this.nullable -> this
        this is Type.Generic -> copy(nullable = nullable)
        this is Type.Alias -> copy(nullable = nullable)
        this is Type.Specific -> copy(nullable = nullable)
        this is Type.Compound -> copy(nullable = nullable)
        else -> error("")
    } as T