package net.notjustanna.leanvm.types

public data class LArray(val value: MutableList<LAny> = mutableListOf()) : LAny() {
    override val type: String get() = "array"

    override fun truth(): Boolean {
        return value.isNotEmpty()
    }

    override fun toString(): String {
        return value.toString()
    }
}
