package com.example.realchatapp.test

class Test {
}

fun main() {
    val itemList = ArrayList<String>()
    itemList.add("20")
    itemList.add("19")
    itemList.add("18")
    itemList.add("17")
    itemList.add("16")
    itemList.add("15")
    itemList.add("14")
    itemList.add("13")
    itemList.add("12")
    itemList.add("11")
    itemList.add("10")
    itemList.add("9")
    itemList.add("8")
    itemList.add("7")
    itemList.add("6")
    itemList.add("5")
    itemList.add("4")
    itemList.add("3")
    itemList.add("2")
    itemList.add("1")

    for (i in 21..30) {
        itemList.add(0, ""+i)
    }

    for (item in itemList) {
        println("final Item List - $item")
    }

}