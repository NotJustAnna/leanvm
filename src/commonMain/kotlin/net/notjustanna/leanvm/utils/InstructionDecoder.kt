package net.notjustanna.leanvm.utils

import net.notjustanna.leanvm.bytecode.LeanInsn
import net.notjustanna.leanvm.bytecode.LeanInsn.Opcode
import net.notjustanna.leanvm.bytecode.LeanInsn.ParameterlessCode
import net.notjustanna.leanvm.context.LeanMachineControl
import net.notjustanna.leanvm.exceptions.MalformedBytecodeException

public abstract class InstructionDecoder {
    protected abstract val control: LeanMachineControl

    protected open fun handle(insn: LeanInsn) {
        val opcodes = Opcode.values()
        val parameterlessCodes = ParameterlessCode.values()

        if (insn.opcode !in opcodes.indices) {
            throw MalformedBytecodeException("Invalid opcode ${insn.opcode}", control.stackTrace())
        }
        when (opcodes[insn.opcode]) {
            Opcode.PARAMETERLESS -> {
                if (insn.immediate !in parameterlessCodes.indices) {
                    throw MalformedBytecodeException(
                        "Invalid parameterless code ${insn.immediate}",
                        control.stackTrace()
                    )
                }

                when (parameterlessCodes[insn.immediate]) {
                    ParameterlessCode.ARRAY_INSERT -> handleArrayInsert()
                    ParameterlessCode.DUP -> handleDup()
                    ParameterlessCode.NEW_ARRAY -> handleNewArray()
                    ParameterlessCode.NEW_OBJECT -> handleNewObject()
                    ParameterlessCode.OBJECT_INSERT -> handleObjectInsert()
                    ParameterlessCode.POP -> handlePop()
                    ParameterlessCode.POP_SCOPE -> handlePopScope()
                    ParameterlessCode.POP_EXCEPTION_HANDLING -> handlePopExceptionHandling()
                    ParameterlessCode.PUSH_NULL -> handlePushNull()
                    ParameterlessCode.PUSH_SCOPE -> handlePushScope()
                    ParameterlessCode.PUSH_THIS -> handlePushThis()
                    ParameterlessCode.RETURN -> handleReturn()
                    ParameterlessCode.THROW -> handleThrow()
                    ParameterlessCode.TYPEOF -> handleTypeof()
                    ParameterlessCode.PUSH_TRUE -> handlePushBoolean(true)
                    ParameterlessCode.PUSH_FALSE -> handlePushBoolean(false)
                    ParameterlessCode.POSITIVE -> handlePositive()
                    ParameterlessCode.NEGATIVE -> handleNegative()
                    ParameterlessCode.TRUTH -> handleTruth()
                    ParameterlessCode.NOT -> handleNot()
                    ParameterlessCode.ADD -> handleAdd()
                    ParameterlessCode.SUBTRACT -> handleSubtract()
                    ParameterlessCode.MULTIPLY -> handleMultiply()
                    ParameterlessCode.DIVIDE -> handleDivide()
                    ParameterlessCode.REMAINING -> handleRemaining()
                    ParameterlessCode.EQUALS -> handleEquals()
                    ParameterlessCode.NOT_EQUALS -> handleNotEquals()
                    ParameterlessCode.LT -> handleComparison(Comparison.LT)
                    ParameterlessCode.LTE -> handleComparison(Comparison.LTE)
                    ParameterlessCode.GT -> handleComparison(Comparison.GT)
                    ParameterlessCode.GTE -> handleComparison(Comparison.GTE)
                    ParameterlessCode.IN -> handleIn()
                    ParameterlessCode.RANGE -> handleRange()
                }
            }

            Opcode.ASSIGN -> handleAssign(insn.immediate)
            Opcode.BRANCH_IF_FALSE -> handleBranchIf(false, insn.immediate)
            Opcode.BRANCH_IF_TRUE -> handleBranchIf(true, insn.immediate)
            Opcode.DECLARE_VARIABLE_IMMUTABLE -> handleDeclareVariable(false, insn.immediate)
            Opcode.DECLARE_VARIABLE_MUTABLE -> handleDeclareVariable(true, insn.immediate)
            Opcode.GET_MEMBER_PROPERTY -> handleGetMemberProperty(insn.immediate)
            Opcode.GET_SUBSCRIPT -> handleGetSubscript(insn.immediate)
            Opcode.GET_VARIABLE -> handleGetVariable(insn.immediate)
            Opcode.INVOKE -> handleInvoke(insn.immediate)
            Opcode.INVOKE_LOCAL -> handleInvokeLocal(insn.immediate shr 16, insn.immediate and 0xff)
            Opcode.INVOKE_MEMBER -> handleInvokeMember(insn.immediate shr 16, insn.immediate and 0xff)
            Opcode.INVOKE_EXTENSION -> handleInvokeExtension(insn.immediate)
            Opcode.JUMP -> handleJump(insn.immediate)
            Opcode.LOAD_DECIMAL -> handleLoadDecimal(insn.immediate)
            Opcode.LOAD_INTEGER -> handleLoadInteger(insn.immediate)
            Opcode.LOAD_STRING -> handleLoadString(insn.immediate)
            Opcode.NEW_FUNCTION -> handleNewFunction(insn.immediate)
            Opcode.PUSH_CHAR -> handlePushChar(insn.immediate)
            Opcode.PUSH_DECIMAL -> handlePushDecimal(insn.immediate)
            Opcode.PUSH_INTEGER -> handlePushInteger(insn.immediate)
            Opcode.PUSH_EXCEPTION_HANDLING -> handlePushExceptionHandling(insn.immediate)
            Opcode.SET_MEMBER_PROPERTY -> handleSetMemberProperty(insn.immediate)
            Opcode.SET_SUBSCRIPT -> handleSetSubscript(insn.immediate)
            Opcode.SET_VARIABLE -> handleSetVariable(insn.immediate)
        }
    }

    protected abstract fun handleArrayInsert()
    protected abstract fun handleDup()
    protected abstract fun handleNewArray()
    protected abstract fun handleNewObject()
    protected abstract fun handleObjectInsert()
    protected abstract fun handlePop()
    protected abstract fun handlePopScope()
    protected abstract fun handlePopExceptionHandling()
    protected abstract fun handlePushNull()
    protected abstract fun handlePushScope()
    protected abstract fun handlePushThis()
    protected abstract fun handleReturn()
    protected abstract fun handleThrow()
    protected abstract fun handleTypeof()
    protected abstract fun handlePushBoolean(b: Boolean)
    protected abstract fun handlePositive()
    protected abstract fun handleNegative()
    protected abstract fun handleTruth()
    protected abstract fun handleNot()
    protected abstract fun handleAdd()
    protected abstract fun handleSubtract()
    protected abstract fun handleMultiply()
    protected abstract fun handleDivide()
    protected abstract fun handleRemaining()
    protected abstract fun handleEquals()
    protected abstract fun handleNotEquals()
    protected abstract fun handleComparison(comparison: Comparison)
    protected abstract fun handleIn()
    protected abstract fun handleRange()
    protected abstract fun handleAssign(immediate: Int)
    protected abstract fun handleBranchIf(b: Boolean, labelCode: Int)
    protected abstract fun handleDeclareVariable(mutable: Boolean, immediate: Int)
    protected abstract fun handleGetMemberProperty(nameConst: Int)
    protected abstract fun handleGetSubscript(size: Int)
    protected abstract fun handleGetVariable(immediate: Int)
    protected abstract fun handleInvoke(size: Int)
    protected abstract fun handleInvokeLocal(nameConst: Int, size: Int)
    protected abstract fun handleInvokeMember(nameConst: Int, size: Int)
    protected abstract fun handleInvokeExtension(size: Int)
    protected abstract fun handleJump(immediate: Int)
    protected abstract fun handleLoadDecimal(immediate: Int)
    protected abstract fun handleLoadInteger(immediate: Int)
    protected abstract fun handleLoadString(immediate: Int)
    protected abstract fun handleNewFunction(immediate: Int)
    protected abstract fun handlePushChar(immediate: Int)
    protected abstract fun handlePushDecimal(immediate: Int)
    protected abstract fun handlePushInteger(immediate: Int)
    protected abstract fun handlePushExceptionHandling(immediate: Int)
    protected abstract fun handleSetMemberProperty(nameConst: Int)
    protected abstract fun handleSetSubscript(size: Int)
    protected abstract fun handleSetVariable(immediate: Int)
}
