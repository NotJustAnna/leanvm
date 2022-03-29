package net.adriantodt.leanvm.bytecode

import net.adriantodt.leanvm.utils.Deserializer
import net.adriantodt.leanvm.utils.Serializable
import okio.Buffer

public data class LeanSectDecl(val nameConst: Int, val line: Int, val column: Int) : Serializable {
    override fun serializeTo(buffer: Buffer) {
        buffer.writeInt(nameConst).writeInt(line).writeInt(column)
    }

    public companion object : Deserializer<LeanSectDecl> {
        override fun deserializeFrom(buffer: Buffer): LeanSectDecl {
            return LeanSectDecl(buffer.readInt(), buffer.readInt(), buffer.readInt())
        }
    }
}
