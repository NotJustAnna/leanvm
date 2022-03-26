package net.adriantodt.leanvm.types

public object LFalse : LAny() {
    override val type: LType = LType.BOOLEAN

    override fun truth(): Boolean {
        return false
    }

    override fun toString(): String {
        return "false"
    }
}
