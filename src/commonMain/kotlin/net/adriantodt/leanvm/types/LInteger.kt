package net.adriantodt.leanvm.types

public data class LInteger(val value: Long) : LNumber() {
    override val type: String get() = "integer"

    override val decimalValue: Double
        get() = value.toDouble()

    override fun compareTo(other: LNumber): Int {
        if (other is LInteger) {
            return value.compareTo(other.value)
        }
        return value.compareTo(other.decimalValue)
    }

    override fun plus(right: LNumber): LNumber {
        if (right is LInteger) {
            return LInteger(value + right.value)
        }
        return LDecimal(value + right.decimalValue)
    }

    override fun minus(right: LNumber): LNumber {
        if (right is LInteger) {
            return LInteger(value - right.value)
        }
        return LDecimal(value - right.decimalValue)
    }

    override fun times(right: LNumber): LNumber {
        if (right is LInteger) {
            return LInteger(value * right.value)
        }
        return LDecimal(value * right.decimalValue)
    }

    override fun div(right: LNumber): LNumber {
        if (right is LInteger) {
            return LInteger(value / right.value)
        }
        return LDecimal(value / right.decimalValue)
    }

    override fun rem(right: LNumber): LNumber {
        if (right is LInteger) {
            return LInteger(value % right.value)
        }
        return LDecimal(value % right.decimalValue)
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
