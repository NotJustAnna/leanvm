package net.notjustanna.leanvm.bytecode

import net.notjustanna.leanvm.utils.*
import net.notjustanna.leanvm.utils.readU24
import net.notjustanna.leanvm.utils.readU8
import net.notjustanna.leanvm.utils.requireU24
import net.notjustanna.leanvm.utils.writeU24
import okio.Buffer

public data class LeanInsn(val opcode: Int, val immediate: Int) : Serializable {
    override fun serializeTo(buffer: Buffer) {
        buffer.writeByte(opcode).writeU24(immediate)
    }

    public enum class Opcode {
        PARAMETERLESS, ASSIGN, BRANCH_IF_FALSE, BRANCH_IF_TRUE, DECLARE_VARIABLE_IMMUTABLE,
        DECLARE_VARIABLE_MUTABLE, GET_MEMBER_PROPERTY, GET_SUBSCRIPT, GET_VARIABLE, INVOKE, INVOKE_LOCAL, INVOKE_MEMBER,
        JUMP, LOAD_DECIMAL, LOAD_INTEGER, LOAD_STRING, NEW_FUNCTION, PUSH_CHAR, PUSH_DECIMAL,
        PUSH_INTEGER, PUSH_EXCEPTION_HANDLING, PUSH_LOOP_HANDLING, SET_MEMBER_PROPERTY, SET_SUBSCRIPT, SET_VARIABLE
    }

    public enum class ParameterlessCode {
        ARRAY_INSERT, BREAK, CONTINUE, DUP, NEW_ARRAY, NEW_OBJECT, OBJECT_INSERT, POP,
        POP_SCOPE, POP_EXCEPTION_HANDLING, POP_LOOP_HANDLING, PUSH_NULL, PUSH_SCOPE, PUSH_THIS, RETURN,
        THROW, TYPEOF, PUSH_TRUE, PUSH_FALSE, UNARY_POSITIVE, UNARY_NEGATIVE, UNARY_TRUTH, UNARY_NOT,
        BINARY_ADD, BINARY_SUBTRACT, BINARY_MULTIPLY, BINARY_DIVIDE, BINARY_REMAINING, BINARY_EQUALS,
        BINARY_NOT_EQUALS, BINARY_LT, BINARY_LTE, BINARY_GT, BINARY_GTE, BINARY_IN, BINARY_RANGE
    }

    public companion object : Deserializer<LeanInsn> {
        public fun parameterless(code: ParameterlessCode) : LeanInsn {
            return simple(Opcode.PARAMETERLESS, code.ordinal)
        }

        public fun smallBig(opcode: Opcode, small: Int, big: Int) : LeanInsn {
            return simple(
                opcode,
                small.requireU16("LeanInsn#small") shl 16 or big.requireU16("LeanInsn#big")
            )
        }

        public fun bigSmall(opcode: Opcode, big: Int, small: Int) : LeanInsn {
            return simple(
                opcode,
                big.requireU16("LeanInsn#big") shl 8 or small.requireU16("LeanInsn#small")
            )
        }

        public fun double(opcode: Opcode, first: Int, second: Int) : LeanInsn {
            return simple(
                opcode,
                first.requireU12("LeanInsn#first") shl 12 or second.requireU12("LeanInsn#second")
            )
        }

        public fun simple(opcode: Opcode, immediate: Int) : LeanInsn {
            return LeanInsn(opcode.ordinal, immediate.requireU24("LeanInsn#immediate"))
        }

        public const val SIZE_BYTES: Int = Int.SIZE_BYTES

        override fun deserializeFrom(buffer: Buffer): LeanInsn {
            return LeanInsn(buffer.readU8(), buffer.readU24())
        }
    }
}
