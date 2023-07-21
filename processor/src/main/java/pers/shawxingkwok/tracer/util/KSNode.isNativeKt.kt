package pers.shawxingkwok.tracer.util

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.Origin
import pers.shawxingkwok.ksputil.Environment
import pers.shawxingkwok.ksputil.previousGeneratedFiles

internal fun KSNode.isNativeKt(): Boolean =
    containingFile?.origin == Origin.KOTLIN
    && Environment.codeGenerator.previousGeneratedFiles.all {
        it.path != containingFile!!.filePath
    }