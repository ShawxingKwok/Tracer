package pers.shawxingkwok.tracer.util

internal val SUPPRESSING =
    """
    //region Suppressing
    @file:Suppress(
        "RemoveRedundantBackticks", "NonAsciiCharacters", "RedundantVisibilityModifier", "unused",
        "PropertyName", "USELESS_CAST", "UNCHECKED_CAST", "NO_EXPLICIT_VISIBILITY_IN_API_MODE_WARNING", 
        "NO_EXPLICIT_RETURN_TYPE_IN_API_MODE_WARNING", "RemoveExplicitSuperQualifier", "ObjectPropertyName",
        "ClassName"
    )
    //endregion
    """.trimIndent()