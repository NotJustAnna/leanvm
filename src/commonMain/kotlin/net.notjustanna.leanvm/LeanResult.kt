package net.notjustanna.leanvm

import net.notjustanna.leanvm.types.LAny

public sealed class LeanResult {
    public abstract fun getOrThrow(): LAny
    public abstract fun getOrNull(): LAny?
    public abstract fun thrownOrNull(): LAny?

    public class Returned(public val value: LAny) : LeanResult() {
        override fun getOrThrow(): LAny {
            return value
        }

        override fun getOrNull(): LAny {
            return value
        }

        override fun thrownOrNull(): LAny? {
            return null
        }
    }

    public class Thrown(public val value: LAny) : LeanResult() {
        override fun getOrThrow(): LAny {
            throw LAnyException(value)
        }

        override fun getOrNull(): LAny? {
            return null
        }

        override fun thrownOrNull(): LAny {
            return value
        }
    }
}
