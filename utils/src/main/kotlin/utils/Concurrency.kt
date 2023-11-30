package utils

import java.util.concurrent.Callable
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.ForkJoinTask

val forkJoinPool = ForkJoinPool.commonPool()

fun <T> runAsync(block: () -> T): ForkJoinTask<T> = forkJoinPool.submit(Callable { block() })

fun <T, R> Iterable<T>.mapAsync(block: (T) -> R) = map { runAsync { block(it) } }

fun <T> Iterable<ForkJoinTask<T>>.awaitAll() = map { it.get() }
