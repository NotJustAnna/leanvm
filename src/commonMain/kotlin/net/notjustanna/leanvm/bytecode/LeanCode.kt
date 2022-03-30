package net.notjustanna.leanvm.bytecode

import net.notjustanna.leanvm.utils.*
import okio.Buffer
import okio.ByteString.Companion.encodeUtf8

public class LeanCode(
    public val lConstArr: LongArray,
    public val sConstArr: Array<String>,
    public val funcArr: Array<LeanFuncDecl>,
    public val sectArr: Array<LeanSectDecl>,
    public val nodeArr: Array<LeanNode>,
) : Serializable {

    override fun serializeTo(buffer: Buffer) {
        check(lConstArr.size.isU24) {
            "Compiled Source cannot be serialized as the long pool exceeds the maximum size (${lConstArr.size} >= $maxU24)"
        }
        check(sConstArr.size.isU24) {
            "Compiled Source cannot be serialized as the string pool exceeds the maximum size (${sConstArr.size} >= $maxU24)"
        }
        check(funcArr.size.isU24) {
            "Compiled Source cannot be serialized as the function definitions exceeds the maximum size (${funcArr.size} >= $maxU24)"
        }

        buffer.writeU24(lConstArr.size)
        for (l in lConstArr) buffer.writeLong(l)

        buffer.writeU24(sConstArr.size)
        for (s in sConstArr) {
            val encoded = s.encodeUtf8()
            buffer.writeInt(encoded.size).write(encoded)
        }

        buffer.writeU24(funcArr.size)
        for (func in funcArr) func.serializeTo(buffer)

        buffer.writeInt(sectArr.size)
        for (sect in sectArr) sect.serializeTo(buffer)

        buffer.writeInt(nodeArr.size)
        for (node in nodeArr) node.serializeTo(buffer)
    }

    public companion object : Deserializer<LeanCode> {
        public fun create(
            lConstArr: List<Long>,
            sConstArr: List<String>,
            funcArr: List<LeanFuncDecl>,
            sectArr: List<LeanSectDecl>,
            nodeArr: List<LeanNode>,
        ): LeanCode {
            return LeanCode(
                lConstArr.toLongArray(),
                sConstArr.toTypedArray(),
                funcArr.toTypedArray(),
                sectArr.toTypedArray(),
                nodeArr.toTypedArray(),
            )
        }

        override fun deserializeFrom(buffer: Buffer): LeanCode {
            val lConstArr = LongArray(buffer.readU24()) { buffer.readLong() }
            val sConstArr = Array(buffer.readU24()) { buffer.readByteString(buffer.readInt().toLong()).utf8() }
            val funcArr = Array(buffer.readU24()) { LeanFuncDecl.deserializeFrom(buffer) }
            val sectArr = Array(buffer.readInt()) { LeanSectDecl.deserializeFrom(buffer) }
            val nodeArr = Array(buffer.readInt()) { LeanNode.deserializeFrom(buffer) }
            return LeanCode(lConstArr, sConstArr, funcArr, sectArr, nodeArr)
        }
    }
}
