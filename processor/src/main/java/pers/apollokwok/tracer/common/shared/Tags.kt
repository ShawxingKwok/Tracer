package pers.apollokwok.tracer.common.shared

import pers.apollokwok.ksputil.Environment

public object Tags {
    public val AllInternal: Boolean = "tracer.allInternallyGenerated" in Environment.options.keys
    public val FullNameProperties: Boolean = "tracer.fullNameProperties" in Environment.options.keys

    public var interfacesBuilt: Boolean = false
        internal set

    public var propsBuilt: Boolean = false
        internal set
}