package me.cookie

class Chance(private val from: Int, to: Int) {
    private val random = java.util.Random()
    private val range = to - from

    fun next(): Int {
        return random.nextInt(range) + from
    }
}