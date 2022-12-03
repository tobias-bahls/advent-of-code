object Resource {
    private fun getResource(name: String) = this::class.java.getResource(name)!!

    fun readResourceAsString(name: String): String = getResource(name).readText()
}
