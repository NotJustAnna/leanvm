package net.adriantodt.leanvm.types

public object LNull : LAny() {
    override val type: LType = LType.NULL

    override fun truth(): Boolean {
        return false
    }

    override fun toString(): String {
        return "null"
    }
}

