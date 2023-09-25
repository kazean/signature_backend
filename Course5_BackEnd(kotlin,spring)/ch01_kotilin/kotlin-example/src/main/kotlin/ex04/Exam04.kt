package ex04

fun main() {
    // java Object == Any
    var map = mapOf<String, Any>(
        Pair("", ""),
        "key" to "value"
    )

    val mutableMap = mutableMapOf(
        "key" to "value"
    )
//    mutableMap.put("key", "value")
    mutableMap["key"] = "value"

//    val value = mutableMap.get("key")
    val value = mutableMap["key"]

    val hashMap = hashMapOf<String, Any>()
    hashMap["key"] = "value"

    var triple = Triple<String, String, String>(
        first = "",
        second = "",
        third = ""
    )
}