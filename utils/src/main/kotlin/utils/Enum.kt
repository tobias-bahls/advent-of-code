package utils

inline fun <reified T : Enum<T>> caseInsensitiveEnumValueOf(name: String) =
    enumValues<T>().find { it.name.lowercase() == name.lowercase() }

inline fun <reified T : Enum<T>> String.toEnum() =
    caseInsensitiveEnumValueOf<T>(this) ?: error("No enum with value $this in ${T::class}")
