package pers.shawxingkwok.tracer.shared

import pers.shawxingkwok.ksputil.Environment

public object Tags {
    public val AllInternal: Boolean = "tracer.internal" in Environment.options.keys

    public var interfacesBuilt: Boolean = false
        internal set

    public var interfacesFixed: Boolean = false
        internal set

    public var propsBuilt: Boolean = false
        internal set
}