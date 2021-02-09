import java.lang.Integer.min
import kotlin.random.Random

data class Distribution(
    val matrix: Array<Array<Byte>>,
    var rawClusters: Array<Int>, var columnClusters: Array<Int>,
    var numOfClusters: Int = 1,
    var effectiveness: Double = 0.0
) {

    fun isValid(): Boolean {
        for (raw in rawClusters) {
            if (raw !in columnClusters) {
                return false
            }
        }

        for (column in columnClusters) {
            if (column !in rawClusters) {
                return false
            }
        }
        return true
    }

    fun generateDistribution() {
        setCorrectNumOfClusters()

        for (i in rawClusters.indices) {
            rawClusters[i] = Random.nextInt(1, numOfClusters + 1)
        }

        for (i in columnClusters.indices) {
            columnClusters[i] = Random.nextInt(1, numOfClusters + 1)
        }

        setCorrectColumn()
        setCorrectRaw()
        calculateEffectiveness()
    }

    private fun setCorrectNumOfClusters() {
        if (numOfClusters > min(rawClusters.size, columnClusters.size)) {
            numOfClusters = min(rawClusters.size, columnClusters.size)
        } else if (numOfClusters < 1) {
            numOfClusters = 1
        }
    }

    private fun setCorrectColumn() {
        var index = 0
        for (raw in rawClusters) {
            if (raw !in columnClusters) {
                columnClusters[index++] = raw
            }
        }
    }

    private fun setCorrectRaw() {
        var index = 0
        for (column in columnClusters) {
            if (column !in rawClusters) {
                rawClusters[index++] = column
            }
        }
    }

    fun calculateEffectiveness(): Double {
        var zerosInClustersCount = 0
        var onesInClustersCount = 0
        var allOnesCount = 0

        for (i in matrix.indices) {
            for (j in matrix[0].indices) {
                allOnesCount += matrix[i][j]

                if (rawClusters[i] == columnClusters[j]) {
                    onesInClustersCount += matrix[i][j]
                    if (matrix[i][j] == 0.toByte())
                        zerosInClustersCount++
                }
            }
        }

        effectiveness = (onesInClustersCount.toDouble() / (zerosInClustersCount + allOnesCount))
        return effectiveness
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Distribution

        if (!columnClusters.contentEquals(other.columnClusters)) return false
        if (!rawClusters.contentEquals(other.rawClusters)) return false
        if (effectiveness != other.effectiveness) return false

        return true
    }

    override fun hashCode(): Int {
        var result = columnClusters.contentHashCode()
        result = 31 * result + rawClusters.contentHashCode()
        result = 31 * result + effectiveness.hashCode()
        return result
    }

    fun copy(): Distribution {
        val rawClusters = Array(this.rawClusters.size) { rawClusters[it] }
        val columnClusters = Array(this.columnClusters.size) { columnClusters[it] }

        return Distribution(matrix, rawClusters, columnClusters, numOfClusters, effectiveness)
    }
}