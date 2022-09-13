package net.notjustanna.leanvm.types

public data class LMetaObject(val value: MutableMap<LAny, LAny> = mutableMapOf()) : LAny() {
    override val type: String get() = "object"

    public constructor(vararg pairs: Pair<LAny, LAny>) : this(pairs.toMap(mutableMapOf()))

    public fun access(key: LAny): LAny? = value[key]

    override fun truth(): Boolean {
        val metaTruth = value.getOrElse(LString("truth")) {
            return value.isNotEmpty()
        }
        if (metaTruth is LFunction) {
            val truth = metaTruth.call(this, emptyList())
            return truth.truth()
        }
        return metaTruth.truth()
    }

    override fun toString(): String {
        val metaToString = value.getOrElse(LString("toString")) {
            return value.toString()
        }
        if (metaToString is LFunction) {
            val toString = metaToString.call(this, emptyList())
            return toString.toString()
        }
        return metaToString.toString()
    }

    public companion object {
        public fun of(vararg pairs: Pair<LAny, LAny>): LMetaObject {
            return LMetaObject(pairs.toMap(mutableMapOf()))
        }
    }
}
