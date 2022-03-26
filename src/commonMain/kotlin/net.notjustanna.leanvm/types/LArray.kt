package net.notjustanna.leanvm.types

public data class LArray(val value: MutableList<LAny> = mutableListOf()) : LAny() {
    override val type: LType = LType.ARRAY

    override fun truth(): Boolean {
        return value.isNotEmpty()
    }

    override fun toString(): String {
        return value.toString()
    }
}
