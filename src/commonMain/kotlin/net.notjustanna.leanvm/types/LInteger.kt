package net.notjustanna.leanvm.types

public data class LInteger(val value: Long) : LNumber() {
    override val type: LType = LType.INTEGER

    override fun compareTo(other: LNumber): Int {
        return when (other) {
            is LDecimal -> value.compareTo(other.value)
            is LInteger -> value.compareTo(other.value)
        }
    }

    override fun plus(right: LNumber): LNumber {
        return when (right) {
            is LDecimal -> LDecimal(value + right.value)
            is LInteger -> LInteger(value + right.value)
        }
    }

    override fun minus(right: LNumber): LNumber {
        return when (right) {
            is LDecimal -> LDecimal(value - right.value)
            is LInteger -> LInteger(value - right.value)
        }
    }

    override fun times(right: LNumber): LNumber {
        return when (right) {
            is LDecimal -> LDecimal(value * right.value)
            is LInteger -> LInteger(value * right.value)
        }
    }

    override fun div(right: LNumber): LNumber {
        return when (right) {
            is LDecimal -> LDecimal(value / right.value)
            is LInteger -> LInteger(value / right.value)
        }
    }

    override fun rem(right: LNumber): LNumber {
        return when (right) {
            is LDecimal -> LDecimal(value % right.value)
            is LInteger -> LInteger(value % right.value)
        }
    }

    override fun unaryPlus(): LNumber {
        return LInteger(+value)
    }

    override fun unaryMinus(): LNumber {
        return LInteger(-value)
    }

    override fun truth(): Boolean {
        return value != 0L
    }

    override fun toString(): String {
        return value.toString()
    }

    public operator fun rangeTo(right: LInteger): LAny {
        return LRange(value..right.value)
    }
}
