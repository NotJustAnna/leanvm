package net.notjustanna.leanvm.bytecode

import net.notjustanna.leanvm.utils.Deserializer
import net.notjustanna.leanvm.utils.Serializable
import okio.Buffer

public data class LeanSectDecl(val nameConst: Int, val line: Int, val column: Int) : Serializable {
    override fun serializeTo(buffer: Buffer) {
        buffer.writeInt(nameConst).writeInt(line).writeInt(column)
    }

    public companion object : Deserializer<LeanSectDecl> {
        public const val SIZE_BYTES: Int = Int.SIZE_BYTES * 3

        override fun deserializeFrom(buffer: Buffer): LeanSectDecl {
            return LeanSectDecl(buffer.readInt(), buffer.readInt(), buffer.readInt())
        }
    }
}
