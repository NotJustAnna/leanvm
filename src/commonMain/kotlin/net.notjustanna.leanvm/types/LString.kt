package net.notjustanna.leanvm.types

public data class LString(val value: String = "") : LAny() {
    override val type: String get() = "string"

    override fun truth(): Boolean {
        return value.isNotEmpty()
    }

    override fun toString(): String {
        return value
    }
}
