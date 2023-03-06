package pers.apollokwok.tracer.common.util

import com.google.devtools.ksp.containingFile
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.Origin
import pers.apollokwok.ksputil.Environment
import pers.apollokwok.ksputil.previousGeneratedFiles

internal fun KSNode.isNativeKt(): Boolean =
    containingFile?.origin == Origin.KOTLIN
    && Environment.codeGenerator.previousGeneratedFiles.all {
        it.path != containingFile!!.filePath
    }