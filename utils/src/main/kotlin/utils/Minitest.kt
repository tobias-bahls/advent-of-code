package utils

@Retention(AnnotationRetention.RUNTIME) @Target(AnnotationTarget.FUNCTION) annotation class Test

@Suppress("NOTHING_TO_INLINE")
inline fun runTests() {
    val anon = object {}

    val classFileName = anon.javaClass.name.split("$")[0]
    val fileClass = Class.forName(classFileName)
    runTestsInFile(fileClass)
}

fun runTestsInFile(fileClass: Class<*>) {
    val testMethods =
        fileClass.declaredMethods.filter { method ->
            method.annotations.any { it.annotationClass == Test::class }
        }

    if (testMethods.isNotEmpty()) {
        println("== Running Tests == ")
    }

    val successes =
        testMethods.sumOf {
            it.isAccessible = true
            val testName = it.name
            val result = runCatching { it.invoke(fileClass) }

            if (result.isSuccess) {
                println("✅ $testName")
                1L
            } else {
                val exceptionMessage = result.exceptionOrNull()?.cause?.message
                System.err.println("❌ $testName: $exceptionMessage")
                0L
            }
        }

    println("[$successes/${testMethods.size}] Tests succeeded.\n")
}
