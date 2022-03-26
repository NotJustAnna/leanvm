package net.notjustanna.leanvm.context

import net.notjustanna.leanvm.Scope
import net.notjustanna.leanvm.StackTrace
import net.notjustanna.leanvm.bytecode.LeanNode
import net.notjustanna.leanvm.types.LAny
import net.notjustanna.leanvm.types.LCompiledFunction

public class FunctionSetupContext(
    private val access: LeanMachineAccess,
    private val function: LCompiledFunction,
    private val thisValue: LAny? = null,
    arguments: List<LAny>,
) : LeanContext {
    private val body: LeanNode
    private val scope: Scope

    private val paramCount: Int
    private var paramNext: Int = 0
    private val argsLeft: MutableList<LAny>

    private var resolvedParamName: String? = null

    init {
        body = function.source.node(function.data.bodyId)
        scope = Scope(function.rootScope)
        paramCount = function.data.paramCount
        argsLeft = arguments.toMutableList()
    }

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
                access.push(
                    ExecutionContext(
                        access,
                        scope,
                        function.source,
                        function.name ?: "<anonymous function>",
                        paramBody,
                        thisValue
                    )
                )
                return
            }

            access.runtime.mismatchedArgs(access)
            return
        }

        access.replace(
            ExecutionContext(
                access,
                Scope(scope),
                function.source,
                function.name ?: "<anonymous function>",
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
        access.onThrow(value) // Keep cascading.
    }

    override fun trace(): StackTrace? {
        return null
    }
}
