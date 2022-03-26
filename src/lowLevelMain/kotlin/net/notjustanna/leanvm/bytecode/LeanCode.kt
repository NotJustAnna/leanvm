package net.notjustanna.leanvm.bytecode

import net.notjustanna.leanvm.utils.*
import okio.Buffer
import okio.ByteString
import okio.ByteString.Companion.encodeUtf8

public actual class LeanCode(
    private val lConstArr: LongArray,
    private val sConstArr: Array<String>,
    private val funcData: Array<ByteString>,
    public actual val sectCount: Int,
    private val sectData: ByteString,
    private val nodeData: Array<ByteString>,
) : Serializable {

    private val funcArr: Array<LeanFuncDecl?> = arrayOfNulls(funcData.size)
    private val sectArr: Array<LeanSectDecl?> = arrayOfNulls(sectCount)
    private val nodeArr: Array<LeanNode?> = arrayOfNulls(nodeData.size)

    public actual val lCount: Int get() = lConstArr.size

    public actual fun lConstOrNull(index: Int): Long? {
        return lConstArr.getOrNull(index)
    }

    public actual fun lConst(index: Int): Long {
        return lConstOrNull(index)
            ?: throw IndexOutOfBoundsException("Tried to access lConst $index on array with length $lCount")
    }

    public actual val sCount: Int get() = sConstArr.size

    public actual fun sConstOrNull(index: Int): String? {
        return sConstArr.getOrNull(index)
    }

    public actual fun sConst(index: Int): String {
        return sConstOrNull(index)
            ?: throw IndexOutOfBoundsException("Tried to access sConst $index on array with length $sCount")
    }

    public actual val nodeCount: Int get() = nodeArr.size

    public actual fun nodeOrNull(index: Int): LeanNode? {
        if (index !in nodeArr.indices) return null
        val fromArray = nodeArr[index]
        if (fromArray != null) return fromArray
        val node = LeanNode.fromBytes(nodeData[index])
        nodeArr[index] = node
        return node
    }

    public actual fun node(index: Int): LeanNode {
        return nodeOrNull(index)
            ?: throw IndexOutOfBoundsException("Tried to access node $index on array with length ${nodeArr.size}")
    }

    public actual fun sectOrNull(index: Int): LeanSectDecl? {
        if (index !in 0 until sectCount) return null
        val fromArray = sectArr[index]
        if (fromArray != null) return fromArray
        val sect = LeanSectDecl.fromBytes(
            sectData.substring(index * LeanSectDecl.SIZE_BYTES, (index + 1) * LeanSectDecl.SIZE_BYTES)
        )
        sectArr[index] = sect
        return sect
    }

    public actual fun sect(index: Int): LeanSectDecl {
        return sectOrNull(index)
            ?: throw IndexOutOfBoundsException("Tried to access sect $index on array with length $sectCount")
    }

    public actual val funcCount: Int get() = funcArr.size

    public actual fun funcOrNull(index: Int): LeanFuncDecl? {
        if (index !in funcArr.indices) return null
        val fromArray = funcArr[index]
        if (fromArray != null) return fromArray
        val func = LeanFuncDecl.fromBytes(funcData[index])
        funcArr[index] = func
        return func
    }

    public actual fun func(index: Int): LeanFuncDecl {
        return funcOrNull(index)
            ?: throw IndexOutOfBoundsException("Tried to access func $index on array with length ${funcArr.size}")
    }

    override fun serializeTo(buffer: Buffer) {
        check(lConstArr.size.isU24) {
            "Compiled Source cannot be serialized as the long pool exceeds the maximum size (${lConstArr.size} >= $maxU24)"
        }
        check(sConstArr.size.isU24) {
            "Compiled Source cannot be serialized as the string pool exceeds the maximum size (${sConstArr.size} >= $maxU24)"
        }
        check(funcData.size.isU24) {
            "Compiled Source cannot be serialized as the function definitions exceeds the maximum size (${funcData.size} >= $maxU24)"
        }

        buffer.writeU24(lConstArr.size)
        for (l in lConstArr) buffer.writeLong(l)

        buffer.writeU24(sConstArr.size)
        for (s in sConstArr) {
            val encoded = s.encodeUtf8()
            buffer.writeInt(encoded.size).write(encoded)
        }

        buffer.writeU24(funcData.size)
        for (func in funcData) buffer.write(func)

        buffer.writeInt(sectCount).write(sectData)

        buffer.writeInt(nodeData.size)
        for (node in nodeData) buffer.write(node)
    }

    override fun toString(): String {
        return "LeanCode[$lCount long consts, $sCount string consts, $sectCount sects, $nodeCount nodes]"
    }

    public actual companion object : Deserializer<LeanCode> {
        public actual fun create(
            lConstArr: List<Long>,
            sConstArr: List<String>,
            funcArr: List<LeanFuncDecl>,
            sectArr: List<LeanSectDecl>,
            nodeArr: List<LeanNode>,
        ): LeanCode {
            return LeanCode(
                lConstArr.toLongArray(),
                sConstArr.toTypedArray(),
                funcArr.map { it.toBytes() }.toTypedArray(),
                sectArr.size,
                Buffer().apply { sectArr.forEach { it.serializeTo(this) } }.snapshot(),
                nodeArr.map { it.toBytes() }.toTypedArray(),
            )
        }

        override fun deserializeFrom(buffer: Buffer): LeanCode {
            val lConstArr = LongArray(buffer.readU24()) { buffer.readLong() }
            val sConstArr = Array(buffer.readU24()) { buffer.readByteString(buffer.readInt().toLong()).utf8() }
            val funcData = Array(buffer.readU24()) { buffer.readByteString(LeanFuncDecl.determineByteSize(buffer)) }
            val sectCount = buffer.readInt()
            val sectData = buffer.readByteString(sectCount.toLong() * LeanSectDecl.SIZE_BYTES)
            val nodeData = Array(buffer.readInt()) { buffer.readByteString(LeanNode.determineByteSize(buffer)) }
            return LeanCode(lConstArr, sConstArr, funcData, sectCount, sectData, nodeData)
        }
    }
}
