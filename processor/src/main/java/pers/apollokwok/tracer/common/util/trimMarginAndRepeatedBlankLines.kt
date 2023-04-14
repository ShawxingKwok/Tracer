package pers.apollokwok.tracer.common.util

internal fun String.trimMarginAndRepeatedBlankLines(marginPrefix: String = "|"): String{
    val lines = trimMargin(marginPrefix).lines()

    return lines.windowed(2)
        .mapNotNull { (former, latter) ->
            if(former.isBlank() && latter.isBlank() )
                null
            else
                latter
        }
        .joinToString("\n", lines.first() + "\n")
}