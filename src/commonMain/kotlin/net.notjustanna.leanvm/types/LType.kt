package net.notjustanna.leanvm.types

public enum class LType {
    NULL,
    BOOLEAN,
    INTEGER,
    DECIMAL,

    STRING,
    RANGE,
    FUNCTION,
    ARRAY,
    OBJECT;

    override fun toString(): String {
        return this.name.lowercase()
    }
}
