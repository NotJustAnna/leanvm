package net.adriantodt.leanvm.bytecode.builder

import net.adriantodt.leanvm.bytecode.LeanCode
import net.adriantodt.leanvm.bytecode.LeanFuncDecl
import net.adriantodt.leanvm.bytecode.LeanParamDecl
import net.adriantodt.leanvm.bytecode.LeanSectDecl

public class LeanCodeBuilder {
    private val lConstArr = mutableListOf<Long>()
    private val sConstArr = mutableListOf<String>()
    private val sectArr = mutableListOf<LeanSectDecl>()
    private val funcArr = mutableListOf<LeanFuncDecl>()

    private val nodeBuilders = mutableListOf<LeanNodeBuilder>()

    public fun newNodeBuilder(): LeanNodeBuilder {
        val builder = LeanNodeBuilder(this, nodeBuilders.size)
        nodeBuilders += builder
        return builder
    }

    public fun sectionId(name: String, line: Int, column: Int): Int {
        val nameConst = constantId(name)
        val value = LeanSectDecl(nameConst, line, column)
        val indexOf = sectArr.indexOf(value)
        if (indexOf != -1) return indexOf
        sectArr.add(value)
        return sectArr.lastIndex
    }

    public fun constantId(value: String): Int {
        val indexOf = sConstArr.indexOf(value)
        if (indexOf != -1) return indexOf
        sConstArr.add(value)
        return sConstArr.lastIndex
    }

    public fun constantId(value: Double): Int {
        return constantId(value.toBits())
    }

    public fun constantId(value: Long): Int {
        val indexOf = lConstArr.indexOf(value)
        if (indexOf != -1) return indexOf
        lConstArr.add(value)
        return sConstArr.lastIndex
    }

    public fun registerFunction(name: String?, bodyId: Int, varargsParam: Int, parameters: List<LeanParamDecl>): Int {
        funcArr += LeanFuncDecl.create(name?.let(this::constantId) ?: -1, bodyId, varargsParam, parameters)
        return funcArr.lastIndex
    }

    public fun build(): LeanCode {
        return LeanCode.create(
            lConstArr.toList(),
            sConstArr.toList(),
            funcArr.toList(),
            sectArr.toList(),
            nodeBuilders.map { it.build() }
        )
    }
}
