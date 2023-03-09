package com.example.realchat.test

class Test {
}

fun main() {
    val itemList = ArrayList<String>()
    itemList.add("1")
    itemList.add("2")
    itemList.add("3")
    itemList.add("4")
    itemList.add("5")
    itemList.add("6")
    itemList.add("7")
    itemList.add("8")
    itemList.add("9")
    itemList.add("10")
    itemList.reverse()
    for (item in itemList) {
        println("previous item - $item")
    }

/*    for (i in 0 until 10) {
        itemList.add(i, "hi")
    }

    for (item in itemList) {
        println("item - $item")
    }*/

}