package net.adriantodt.leanvm.types

public data class LRange(val value: LongRange) : LAny() {
    override val type: String get() = "range"

    override fun truth(): Boolean {
        return value.first <= value.last
    }
}
