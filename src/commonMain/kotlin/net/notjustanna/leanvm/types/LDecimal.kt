package net.notjustanna.leanvm.types

public data class LDecimal(val value: Double) : LNumber() {
    override val type: String get() = "decimal"

    override val decimalValue: Double get() = value
    override fun compareTo(other: LNumber): Int {
        return value.compareTo(other.decimalValue)
    }

    override fun plus(right: LNumber): LNumber {
        return LDecimal(value + right.decimalValue)
    }

    override fun minus(right: LNumber): LNumber {
        return LDecimal(value - right.decimalValue)
    }

    override fun times(right: LNumber): LNumber {
        return LDecimal(value * right.decimalValue)
    }

    override fun div(right: LNumber): LNumber {
        return LDecimal(value / right.decimalValue)
    }

    override fun rem(right: LNumber): LNumber {
        return LDecimal(value % right.decimalValue)
    }

    override fun unaryPlus(): LNumber {
        return LDecimal(+value)
    }

    override fun unaryMinus(): LNumber {
        return LDecimal(-value)
    }

    override fun truth(): Boolean {
        return value != 0.0
    }

    override fun toString(): String {
        return value.toString()
    }
}
