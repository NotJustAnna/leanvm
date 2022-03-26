package net.notjustanna.leanvm.bytecode

import net.notjustanna.leanvm.utils.Deserializer
import net.notjustanna.leanvm.utils.Serializable

public expect class LeanCode : Serializable {

    public val lCount: Int
    public fun lConstOrNull(index: Int): Long?
    public fun lConst(index: Int): Long

    public val sCount: Int
    public fun sConstOrNull(index: Int): String?
    public fun sConst(index: Int): String

    public val nodeCount: Int
    public fun nodeOrNull(index: Int): LeanNode?
    public fun node(index: Int): LeanNode

    public val sectCount: Int
    public fun sectOrNull(index: Int): LeanSectDecl?
    public fun sect(index: Int): LeanSectDecl

    public val funcCount: Int
    public fun funcOrNull(index: Int): LeanFuncDecl?
    public fun func(index: Int): LeanFuncDecl

    public companion object : Deserializer<LeanCode> {
        public fun create(
            lConstArr: List<Long>,
            sConstArr: List<String>,
            funcArr: List<LeanFuncDecl>,
            sectArr: List<LeanSectDecl>,
            nodeArr: List<LeanNode>,
        ): LeanCode
    }
}
