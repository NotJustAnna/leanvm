package net.notjustanna.leanvm.types

public data class LString(val value: String) : LAny() {
    override val type: LType = LType.STRING

    override fun truth(): Boolean {
        return value.isNotEmpty()
    }

    override fun toString(): String {
        return value
    }
}
