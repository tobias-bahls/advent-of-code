package utils

inline fun <reified T : Enum<T>> caseInsensitiveEnumValueOf(name: String) =
    enumValues<T>().find { it.name.lowercase() == name.lowercase() }
