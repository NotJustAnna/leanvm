package net.adriantodt.leanvm.bytecode

import net.adriantodt.leanvm.utils.Deserializer
import net.adriantodt.leanvm.utils.Serializable

// IMPLEMENTATION REMINDER:
// sectArr and jumpArr have to be sorted so that the arrays can be binary-searched.
public expect class LeanNode : Serializable {
    public val insnCount: Int
    public fun insnOrNull(index: Int): LeanInsn?
    public fun insn(index: Int): LeanInsn

    public val sectCount: Int
    public fun sectOrNull(index: Int): LeanSectLabel?
    public fun sect(index: Int): LeanSectLabel
    public fun findSect(last: Int): LeanSectLabel?

    public val jumpCount: Int
    public fun jumpOrNull(index: Int): LeanJumpLabel?
    public fun jump(index: Int): LeanJumpLabel
    public fun findJump(code: Int): LeanJumpLabel?

    public companion object : Deserializer<LeanNode> {
        public fun create(insnArr: List<LeanInsn>, jumpArr: List<LeanJumpLabel>, sectArr: List<LeanSectLabel>): LeanNode
    }
}
