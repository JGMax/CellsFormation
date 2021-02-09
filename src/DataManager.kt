import java.io.File

class DataManager {
    var matrix: Array<Array<Byte>>? = null

    fun readDataFromFile(fileName: String): Array<Array<Byte>> {
        val lines = File(fileName).readLines()
        val m = lines[0].split(" ")[0].toInt()
        val p = lines[0].split(" ")[1].toInt()

        matrix = Array(m) { Array(p) { 0.toByte() } }

        for (line in lines) {
            if (line != lines.first()) {
                val nums = line.split(" ")

                for (j in nums.indices) {
                    if (j != 0 && nums[j].isNotEmpty()) {
                        matrix!![nums[0].toInt() - 1][nums[j].toInt() - 1] = 1.toByte()
                    }
                }
            }
        }

        return matrix as Array<Array<Byte>>
    }

    fun writeDataToFile(fileName: String, machineClusters: Array<Int>, partsClusters: Array<Int>) {
        File(fileName).printWriter().use {
            for (i in machineClusters) {
                it.print("$i ")
            }
            it.println()
            for (i in partsClusters) {
                it.print("$i ")
            }
        }
    }
}