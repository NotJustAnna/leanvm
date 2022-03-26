package net.adriantodt.leanvm

import net.adriantodt.leanvm.types.LAny

public class LAnyException(public val value: LAny) : RuntimeException("Object thrown from Lin: $value")
