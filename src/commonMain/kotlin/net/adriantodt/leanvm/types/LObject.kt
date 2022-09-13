package net.adriantodt.leanvm.types

public data class LObject(val value: MutableMap<LAny, LAny> = mutableMapOf()) : LAny() {
    override val type: String get() = "object"

    public constructor(vararg pairs: Pair<LAny, LAny>) : this(pairs.toMap(mutableMapOf()))

    override fun truth(): Boolean {
        return value.isNotEmpty()
    }

    override fun toString(): String {
        return value.toString()
    }

    public companion object {
        public fun of(vararg pairs: Pair<LAny, LAny>): LObject {
            return LObject(pairs.toMap(mutableMapOf()))
        }
    }
}

