package net.notjustanna.leanvm.types

public object LNull : LAny() {
    override val type: String get() = "null"

    override fun truth(): Boolean {
        return false
    }

    override fun toString(): String {
        return "null"
    }
}

