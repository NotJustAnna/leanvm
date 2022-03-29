package net.notjustanna.leanvm.types

public sealed class LBoolean private constructor(private val value: Boolean) : LAny() {
    override val type: String get() = "boolean"

    public object True : LBoolean(true)
    public object False : LBoolean(false)

    override fun truth(): Boolean {
        return value
    }

    override fun toString(): String {
        return value.toString()
    }

    public companion object {
        public fun of(value: Boolean): LBoolean {
            return if (value) True else False
        }
    }
}
