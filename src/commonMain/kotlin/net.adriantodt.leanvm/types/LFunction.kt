package net.adriantodt.leanvm.types

public sealed class LFunction : LAny() {
    override val type: LType = LType.FUNCTION

    override fun truth(): Boolean {
        return true
    }

    public open operator fun invoke(vararg args: LAny): LAny {
        return call(null, args.toList())
    }

    public abstract fun call(thisValue: LAny?, args: List<LAny>): LAny

    public abstract val name: String?

    override fun toString(): String {
        if (name != null) {
            return "<function $name>"
        }
        return "<function>"
    }
}
