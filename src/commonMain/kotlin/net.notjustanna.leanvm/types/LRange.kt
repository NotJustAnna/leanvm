package net.notjustanna.leanvm.types

public data class LRange(val value: LongRange) : LAny() {
    override val type: LType = LType.RANGE

    override fun truth(): Boolean {
        return value.first <= value.last
    }
}
