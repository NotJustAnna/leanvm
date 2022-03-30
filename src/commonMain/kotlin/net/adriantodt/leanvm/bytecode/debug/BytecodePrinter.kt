package net.adriantodt.leanvm.bytecode.debug

import net.adriantodt.leanvm.bytecode.LeanCode
import net.adriantodt.leanvm.bytecode.LeanFuncDecl
import net.adriantodt.leanvm.bytecode.LeanNode

public class BytecodePrinter {
    private var indent = 0

    public fun printCode(code: LeanCode) {
        indentedPrintln("$code:")
        indent++
        if (code.lConstArr.isNotEmpty()) {
            indentedPrintln("Long pool:")
            indent++
            for ((index, it) in code.lConstArr.withIndex()) {
                indentedPrintln("[$index] $it")
            }
            indent--
        }
        if (code.sConstArr.isNotEmpty()) {
            indentedPrintln("String pool:")
            indent++
            for ((index, it) in code.sConstArr.withIndex()) {
                indentedPrintln("[$index] $it")
            }
            indent--
        }
        if (code.nodeArr.isNotEmpty()) {
            indentedPrintln("Nodes:")
            indent++
            for (it in code.nodeArr) {
                printNode(it)
            }
            indent--
        }
        if (code.sectArr.isNotEmpty()) {
            indentedPrintln("Sections:")
            indent++
            for ((index, it) in code.sectArr.withIndex()) {
                indentedPrintln("[$index] $it")
            }
            indent--
        }
        if (code.funcArr.isNotEmpty()) {
            indentedPrintln("Functions:")
            indent++
            for (it in code.funcArr) {
                printFunc(it)
            }
            indent--
        }
    }

    private fun printNode(node: LeanNode) {
        indentedPrintln("$node:")
        indent++
        if (node.insnArr.isNotEmpty()) {
            indentedPrintln("Instructions:")
            indent++
            for ((index, it) in node.insnArr.withIndex()) {
                indentedPrintln("[$index] $it")
            }
            indent--
        }
        if (node.sectArr.isNotEmpty()) {
            indentedPrintln("Section Labels:")
            indent++
            for ((index, it) in node.sectArr.withIndex()) {
                indentedPrintln("[$index] $it")
            }
            indent--
        }
        if (node.jumpArr.isNotEmpty()) {
            indentedPrintln("Jumps:")
            indent++
            for ((index, it) in node.jumpArr.withIndex()) {
                indentedPrintln("[$index] $it")
            }
            indent--
        }
        indent--
    }

    private fun printFunc(func: LeanFuncDecl) {
        indentedPrintln("$func:")
        indent++
        if (func.paramArr.isNotEmpty()) {
            indentedPrintln("Parameters:")
            indent++
            for ((index, it) in func.paramArr.withIndex()) {
                indentedPrintln("[$index] $it")
            }
            indent--
        }
        indent--
    }

    private fun indentedPrintln(s: String) {
        for (i in 0..indent) {
            print("  ")
        }
        println(s)
    }
}
