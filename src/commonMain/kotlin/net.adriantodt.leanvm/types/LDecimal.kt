package net.adriantodt.leanvm.types

public data class LDecimal(val value: Double) : LNumber() {
    override val type: String get() = "decimal"

    override fun compareTo(other: LNumber): Int {
        return when (other) {
            is LDecimal -> value.compareTo(other.value)
            is LInteger -> value.compareTo(other.value)
            else -> throw AssertionError("Exhaustive match failed")
        }
    }

    override fun plus(right: LNumber): LNumber {
        return when (right) {
            is LDecimal -> LDecimal(value + right.value)
            is LInteger -> LDecimal(value + right.value)
            else -> throw AssertionError("Exhaustive match failed")
        }
    }

    override fun minus(right: LNumber): LNumber {
        return when (right) {
            is LDecimal -> LDecimal(value - right.value)
            is LInteger -> LDecimal(value - right.value)
            else -> throw AssertionError("Exhaustive match failed")
        }
    }

    override fun times(right: LNumber): LNumber {
        return when (right) {
            is LDecimal -> LDecimal(value * right.value)
            is LInteger -> LDecimal(value * right.value)
            else -> throw AssertionError("Exhaustive match failed")
        }
    }

    override fun div(right: LNumber): LNumber {
        return when (right) {
            is LDecimal -> LDecimal(value / right.value)
            is LInteger -> LDecimal(value / right.value)
            else -> throw AssertionError("Exhaustive match failed")
        }
    }

    override fun rem(right: LNumber): LNumber {
        return when (right) {
            is LDecimal -> LDecimal(value % right.value)
            is LInteger -> LDecimal(value % right.value)
            else -> throw AssertionError("Exhaustive match failed")
        }
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
