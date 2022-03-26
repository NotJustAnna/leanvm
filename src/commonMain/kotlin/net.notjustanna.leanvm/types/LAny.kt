package net.notjustanna.leanvm.types

public abstract class LAny {
    public abstract fun truth(): Boolean

    public abstract val type: String

    public companion object {
        public fun of(value: Any?): LAny {
            return when (value) {
                null, is Unit -> LNull
                is Boolean -> LBoolean.of(value)
                is String -> LString(value)
                is Char -> LString(value.toString())
                is Number -> if (value is Float || value is Double) {
                    LDecimal(value.toDouble())
                } else {
                    LInteger(value.toLong())
                }
                is List<*> -> LArray(value.mapTo(mutableListOf()) { of(it) })
                is Map<*, *> -> LObject(value.entries.associateTo(mutableMapOf()) { of(it.key) to of(it.value) })
                else -> throw IllegalArgumentException("Can't convert $value to LAny.")
            }
        }

        public fun ofEntry(entry: Map.Entry<LAny, LAny>): LAny {
            return LObject(LString("key") to entry.key, LString("value") to entry.value)
        }
    }
}
