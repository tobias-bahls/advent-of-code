package utils

import java.util.BitSet
import java.util.stream.Collectors

fun BitSet.toIntSet(): Set<Int> = stream().boxed().collect(Collectors.toSet())

fun BitSet.setAll(elems: Iterable<Int>) = elems.forEach { set(it) }

fun BitSet.clearAll(elems: Iterable<Int>) = elems.forEach { clear(it) }

fun BitSet.copy(): BitSet = clone() as BitSet

fun Set<Int>.toBitSet() = BitSet().also { bs -> bs.setAll(this) }
