package net.notjustanna.leanvm.types

import net.notjustanna.leanvm.LeanMachine
import net.notjustanna.leanvm.ctx.LeanContext
import net.notjustanna.leanvm.ctx.LeanMachineControl
import net.notjustanna.leanvm.ctx.LeanRuntime

public abstract class LFunction : LAny() {
    override val type: String get() = "function"

    override fun truth(): Boolean {
        return true
    }

    public operator fun invoke(vararg args: LAny): LAny {
        return createVM(null, args.toList()).run().getOrThrow()
    }

    public operator fun LAny?.invoke(vararg args: LAny): LAny {
        return createVM(this, args.toList()).run().getOrThrow()
    }

    public fun call(thisValue: LAny?, args: List<LAny>): LAny {
        return createVM(thisValue, args).run().getOrThrow()
    }

    public fun createVM(thisValue: LAny?, args: List<LAny>): LeanMachine {
        return LeanMachine { setupContext(it, thisValue, args) }
    }

    public abstract fun setupContext(
        control: LeanMachineControl,
        thisValue: LAny?,
        args: List<LAny>,
        runtime: LeanRuntime? = null,
    ): LeanContext

    public abstract val name: String?

    override fun toString(): String {
        if (name != null) {
            return "<function $name>"
        }
        return "<function>"
    }
}
