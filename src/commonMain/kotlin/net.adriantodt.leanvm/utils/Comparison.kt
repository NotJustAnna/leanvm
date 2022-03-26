package net.adriantodt.leanvm.utils

public enum class Comparison(public val operator: String, public val block: (Int) -> Boolean) {
    GT(">", { it > 0 }),
    GTE(">=", { it >= 0 }),
    LT("<", { it < 0 }),
    LTE("<=", { it <= 0 });

    override fun toString(): String {
        return operator
    }
}
