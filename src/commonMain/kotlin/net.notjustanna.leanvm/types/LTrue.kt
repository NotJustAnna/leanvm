package net.notjustanna.leanvm.types

public object LTrue : LAny() {
    override val type: LType = LType.BOOLEAN

    override fun truth(): Boolean {
        return true
    }

    override fun toString(): String {
        return "true"
    }
}
