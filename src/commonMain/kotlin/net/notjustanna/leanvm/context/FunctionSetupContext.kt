package net.notjustanna.leanvm.context

import net.notjustanna.leanvm.LAnyException
import net.notjustanna.leanvm.Scope
import net.notjustanna.leanvm.StackTrace
import net.notjustanna.leanvm.bytecode.LeanNode
import net.notjustanna.leanvm.exceptions.old.Exceptions
import net.notjustanna.leanvm.types.LAny
import net.notjustanna.leanvm.types.LCompiledFunction

public class FunctionSetupContext(
    private val control: LeanMachineControl,
    private val function: LCompiledFunction,
    override val runtime: LeanRuntime,
    private val thisValue: LAny? = null,
    arguments: List<LAny>,
) : LeanContext {
    private val body: LeanNode = function.source.node(function.data.bodyId)
    private val scope: Scope = Scope(function.rootScope)
    private val paramCount: Int = function.data.paramCount
    private var paramNext: Int = 0
    private val argsLeft: MutableList<LAny> = arguments.toMutableList()
    private var resolvedParamName: String? = null

    override fun step() {
        while (paramNext < paramCount) {
            val parameter = function.data.param(paramNext++)
            val value = argsLeft.removeFirstOrNull()

            val paramName = function.source.sConst(parameter.nameConst)
            // TODO Not yet implemented: varargs parameter

            if (value != null) {
                scope.define(paramName, true, value)
                continue
            }

            if (parameter.defaultValueNodeId != -1) {
                val paramBody = function.source.node(parameter.defaultValueNodeId)
                resolvedParamName = paramName
                scope.define(paramName, true)
                control.push(
                    NodeExecutionContext(
                        control,
                        function.source,
                        scope,
                        function.name ?: "<anonymous function>",
                        runtime,
                        paramBody,
                        thisValue
                    )
                )
                return
            }

            // TODO Throw actually useful exception
            throw LAnyException(Exceptions.mismatchedArgs(control.stackTrace()))
        }

        control.replace(
            NodeExecutionContext(
                control,
                function.source,
                Scope(scope),
                function.name ?: "<anonymous function>",
                runtime,
                body,
                thisValue
            )
        )
    }

    override fun onReturn(value: LAny) {
        val paramName = resolvedParamName ?: error("resolvedParamName should not be null")
        scope.set(paramName, value)
    }

    override fun onThrow(value: LAny) {
        control.onThrow(value) // Keep cascading.
    }

    override fun trace(): StackTrace? {
        return null
    }
}
