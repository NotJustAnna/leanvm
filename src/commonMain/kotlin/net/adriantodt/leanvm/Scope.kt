package net.adriantodt.leanvm

import net.adriantodt.leanvm.types.LAny

public class Scope(public val parent: Scope? = null) {
    public sealed class Declaration(public val mutable: Boolean) {
        public class Undefined(mutable: Boolean) : Declaration(mutable)
        public class Defined(mutable: Boolean, public val value: LAny) : Declaration(mutable)
    }

    private val declarations: MutableMap<String, Declaration> = mutableMapOf()

    public fun define(name: String, mutable: Boolean) {
        declarations[name] = Declaration.Undefined(mutable)
    }

    public fun define(name: String, mutable: Boolean, value: LAny) {
        declarations[name] = Declaration.Defined(mutable, value)
    }

    public fun get(name: String): LAny {
        var s: Scope? = this
        while (s != null) {
            val declaration = s.declarations[name]
            if (declaration == null) {
                s = s.parent
                continue
            }
            if (declaration is Declaration.Defined) {
                return declaration.value
            }
            throw IllegalStateException("Variable $name is declared but not set.")
        }
        throw IllegalStateException("Could not resolve $name")
    }

    public fun getOrNull(name: String): LAny? {
        var s: Scope? = this
        while (s != null) {
            val declaration = s.declarations[name]
            if (declaration == null) {
                s = s.parent
                continue
            }
            return (declaration as? Declaration.Defined)?.value
        }
        return null
    }

    public fun set(name: String, value: LAny) {
        var s: Scope? = this
        while (s != null) {
            val declaration = s.declarations[name]
            if (declaration == null) {
                s = s.parent
                continue
            }
            if (!declaration.mutable && declaration is Declaration.Defined) {
                throw IllegalStateException("Tried to set immutable variable $name with value already set.")
            }
            s.declarations[name] = Declaration.Defined(declaration.mutable, value)
            return
        }
        throw IllegalStateException("Tried to set variable $name not declared in any scope yet.")
    }
}
