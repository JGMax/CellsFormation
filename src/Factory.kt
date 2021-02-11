
import kotlin.math.min
import kotlin.math.max
import kotlin.random.Random

class Factory(private val matrix: Array<Array<Byte>>, numOfClusters: Int = 1) {
    private var currentDistribution = Distribution(
        matrix,
        Array(matrix.size) { 1 },
        Array(matrix[0].size) { 1 },
        numOfClusters
    )

    init {
        currentDistribution.generateDistribution()
    }

    private var localMinimum = currentDistribution.copy()

    private var globalMinimum = currentDistribution.copy()

    private fun swap(data: Array<Int>, index1: Int, index2: Int) {
        if (index1 in data.indices && index2 in data.indices) {
            val temp = data[index1]
            data[index1] = data[index2]
            data[index2] = temp
            currentDistribution.calculateEffectiveness()
        }
    }

    private fun bestSwap(data: Array<Int>): Double {
        var profit = 0.0
        for (i in data.indices) {
            for (j in data.indices) {
                swap(data, i, j)
                if (currentDistribution.effectiveness > localMinimum.effectiveness) {
                    profit = currentDistribution.effectiveness - localMinimum.effectiveness
                    localMinimum = currentDistribution.copy()
                    return profit
                }
            }
        }
        return profit
    }

    private fun raiseRaw(indexFrom: Int, clusterTo: Int): Boolean {
        if (indexFrom in currentDistribution.rawClusters.indices
            && clusterTo in 1..currentDistribution.numOfClusters
        ) {
            val thisTry = currentDistribution.copy()
            thisTry.rawClusters[indexFrom] = clusterTo

            if (thisTry.isValid()) {
                currentDistribution = thisTry
                currentDistribution.calculateEffectiveness()
                return true
            }
        }
        return false
    }

    private fun raiseColumn(indexFrom: Int, clusterTo: Int): Boolean {
        if (indexFrom in currentDistribution.columnClusters.indices
            && clusterTo in 1..currentDistribution.numOfClusters
        ) {
            val thisTry = currentDistribution.copy()
            thisTry.columnClusters[indexFrom] = clusterTo

            if (thisTry.isValid()) {
                currentDistribution = thisTry
                currentDistribution.calculateEffectiveness()
                return true
            }
        }
        return false
    }

    private fun bestRaiseRaw(): Double {
        var profit = 0.0
        for (i in currentDistribution.rawClusters.indices) {
            for (cluster in 1..currentDistribution.numOfClusters) {
                if (raiseRaw(i, cluster)) {
                    if (currentDistribution.effectiveness - localMinimum.effectiveness > profit) {
                        profit = currentDistribution.effectiveness - localMinimum.effectiveness
                        localMinimum = currentDistribution.copy()
                    }
                } else {
                    break
                }
            }
            currentDistribution = localMinimum.copy()
        }
        return profit
    }

    private fun bestRaiseColumn(): Double {
        var profit = 0.0
        for (i in currentDistribution.columnClusters.indices) {
            for (cluster in 1..currentDistribution.numOfClusters) {
                if (raiseColumn(i, cluster)) {
                    if (currentDistribution.effectiveness - localMinimum.effectiveness > profit) {
                        profit = currentDistribution.effectiveness - localMinimum.effectiveness
                        localMinimum = currentDistribution.copy()
                    }
                } else {
                    break
                }
            }
            currentDistribution = localMinimum.copy()
        }
        return profit
    }

    private fun merge(clusterNum1: Int, clusterNum2: Int): Boolean {
        val thisTry = localMinimum.copy()
        if (
            merge(thisTry.columnClusters, clusterNum1, clusterNum2) &&
            merge(thisTry.rawClusters, clusterNum1, clusterNum2)
        ) {
            thisTry.numOfClusters--
            thisTry.calculateEffectiveness()
            localMinimum = thisTry
            return true
        }
        return false
    }

    private fun merge(data: Array<Int>, clusterNum1: Int, clusterNum2: Int): Boolean {
        if (clusterNum1 in data &&
            clusterNum2 in data &&
            clusterNum1 != clusterNum2
        ) {
            for (i in data.indices) {
                if (data[i] == max(clusterNum1, clusterNum2)) {
                    data[i] = min(clusterNum1, clusterNum2)
                } else if (data[i] > max(clusterNum1, clusterNum2)) {
                    data[i]--
                }
            }
            return true
        }
        return false
    }

    private fun split(clusterNum: Int): Boolean {
        val thisTry = localMinimum.copy()
        if (
            split(thisTry.rawClusters, clusterNum) &&
            split(thisTry.columnClusters, clusterNum)
        ) {
            thisTry.numOfClusters++
            thisTry.calculateEffectiveness()
            localMinimum = thisTry
            return true
        }
        return false
    }

    private fun split(data: Array<Int>, clusterNum: Int): Boolean {
        if (clusterNum in data) {
            var count = data.count { it == clusterNum }
            count /= 2
            if (count < 1) {
                return false
            }
            for (i in data.indices) {
                if (count > 0) {
                    if (data[i] == clusterNum) {
                        data[i] = localMinimum.numOfClusters + 1
                        count--
                    }
                } else {
                    break
                }
            }
            return true
        }
        return false
    }

    private fun variableNeighborhoodDescent(): Double {
        var fullProfit = 0.0
        loop@ do {
            var profit: Double
            currentDistribution = localMinimum.copy()

            val seq = arrayOf(1, 2, 3, 4)
            seq.shuffle()

            for (i in seq) {
                when (i) {
                    1 -> {
                        /*profit = if (raiseRaw(
                                Random.nextInt(0, currentDistribution.rawClusters.size),
                                Random.nextInt(1, currentDistribution.numOfClusters + 1)
                            )
                        ) {
                            currentDistribution.effectiveness - localMinimum.effectiveness
                        } else {
                            0.0
                        }*/
                        profit = bestRaiseRaw()
                        if (profit > 0.0) {
                            localMinimum = currentDistribution.copy()
                            fullProfit += profit
                            break
                        }
                    }
                    2 -> {
                        swap(
                            currentDistribution.rawClusters,
                            Random.nextInt(0, currentDistribution.rawClusters.size),
                            Random.nextInt(0, currentDistribution.rawClusters.size)
                        )
                        profit = currentDistribution.effectiveness - localMinimum.effectiveness
                        //profit = bestSwap(currentDistribution.rawClusters)
                        if (profit > 0.0) {
                            localMinimum = currentDistribution.copy()
                            fullProfit += profit
                            break
                        }
                    }
                    3 -> {
                        /*profit = if (raiseColumn(
                                Random.nextInt(0, currentDistribution.columnClusters.size),
                                Random.nextInt(1, currentDistribution.numOfClusters + 1)
                            )
                        ) {
                            currentDistribution.effectiveness - localMinimum.effectiveness
                        } else {
                            0.0
                        }*/
                        profit = bestRaiseColumn()
                        if (profit > 0.0) {
                            localMinimum = currentDistribution.copy()
                            fullProfit += profit
                            break
                        }
                    }
                    4 -> {
                        swap(
                            currentDistribution.columnClusters,
                            Random.nextInt(0, currentDistribution.columnClusters.size),
                            Random.nextInt(0, currentDistribution.columnClusters.size)
                        )
                        profit = currentDistribution.effectiveness - localMinimum.effectiveness
                        //profit = bestSwap(currentDistribution.columnClusters)
                        if (profit > 0.0) {
                            localMinimum = currentDistribution.copy()
                            fullProfit += profit
                            break
                        }
                    }
                }
                if (i == seq.last()) {
                    break@loop
                }
            }

            /*profit = bestRaiseRaw()
            if (profit > 0.0) {
                fullProfit += profit
                continue
            }

            swap(
                currentDistribution.rawClusters,
                Random.nextInt(0, currentDistribution.rawClusters.size),
                Random.nextInt(0, currentDistribution.rawClusters.size)
            )
            profit = currentDistribution.effectiveness - localMinimum.effectiveness
            if (profit > 0.0) {
                localMinimum = currentDistribution.copy()
                fullProfit += profit
                continue
            }

            profit = bestRaiseColumn()
            if (profit > 0.0) {
                fullProfit += profit
                continue
            }

            swap(
                currentDistribution.columnClusters,
                Random.nextInt(0, currentDistribution.columnClusters.size),
                Random.nextInt(0, currentDistribution.columnClusters.size)
            )
            profit = currentDistribution.effectiveness - localMinimum.effectiveness
            if (profit > 0.0) {
                localMinimum = currentDistribution.copy()
                fullProfit += profit
                continue
            }

            break*/
        } while (true)
        return fullProfit
    }

    fun generalVNS(iterationsCount: Int = 1000) {
        if (variableNeighborhoodDescent() > 0.0) {
            globalMinimum = localMinimum.copy()
            currentDistribution = globalMinimum.copy()
            DataManager().writeDataToFile(
                "BestAnswer.txt",
                globalMinimum.rawClusters, globalMinimum.columnClusters
            )
        }
        loop@for (i in 0..iterationsCount) {
            println("$i Global effectiveness: ${globalMinimum.effectiveness} Clusters: ${globalMinimum.numOfClusters}")

            localMinimum = globalMinimum.copy()

            if (Random.nextBoolean()) {
                merge(
                    Random.nextInt(1, localMinimum.numOfClusters + 1),
                    Random.nextInt(1, localMinimum.numOfClusters + 1)
                )
            } else {
                /*do {
                    val clusterForSplit = getClustersForSplit()
                    if (clusterForSplit.isEmpty()) {
                        merge(
                            Random.nextInt(1, localMinimum.numOfClusters + 1),
                            Random.nextInt(1, localMinimum.numOfClusters + 1)
                        )
                    } else {
                        split(
                            clusterForSplit[Random.nextInt(clusterForSplit.size)]
                        )
                    }
                } while(clusterForSplit.isNotEmpty())*/
                val clusters = getClustersForSplit()
                if (clusters.isEmpty()) {
                    continue@loop
                }
                split(
                    clusters[Random.nextInt(clusters.size)]
                )
            }

            currentDistribution = localMinimum.copy()

            if (checkGlobalAnswer()) {
                continue
            }

        }
    }

    private fun checkGlobalAnswer(): Boolean {
        if (variableNeighborhoodDescent() > 0.0
            && globalMinimum.effectiveness < localMinimum.effectiveness
        ) {
            globalMinimum = localMinimum.copy()
            currentDistribution = globalMinimum.copy()
            println("!!!Improve!!!")
            DataManager().writeDataToFile(
                "BestAnswer.txt",
                globalMinimum.rawClusters, globalMinimum.columnClusters
            )
            return true
        }
        return false
    }

    private fun getClustersForSplit(): List<Int> {
        val rawForSplit = localMinimum.rawClusters
            .groupingBy { it }
            .eachCount()
            .filter { it.value > 1 }.keys
        val columnForSplit = localMinimum.columnClusters
            .groupingBy { it }
            .eachCount()
            .filter { it.value > 1 }.keys
        return rawForSplit.filter { columnForSplit.contains(it) }
    }

    fun getCurrentEffectiveness() = currentDistribution.effectiveness

    fun getLocalEffectiveness() = localMinimum.effectiveness

    fun getGlobalEffectiveness() = globalMinimum.effectiveness

    fun printCurrent() {
        print("\t")
        for (cluster in currentDistribution.columnClusters) {
            print("$cluster\t")
        }
        println()

        for (i in matrix.indices) {
            print("${currentDistribution.rawClusters[i]}\t")
            for (j in matrix[0].indices) {
                print("${matrix[i][j]}\t")
            }
            println()
        }
    }

    fun printLocal() {
        print("\t")
        for (cluster in localMinimum.columnClusters) {
            print("$cluster\t")
        }
        println()

        for (i in matrix.indices) {
            print("${localMinimum.rawClusters[i]}\t")
            for (j in matrix[0].indices) {
                print("${matrix[i][j]}\t")
            }
            println()
        }
    }

    fun printGlobal() {
        print("\t")
        for (cluster in globalMinimum.columnClusters) {
            print("$cluster\t")
        }
        println()

        for (i in matrix.indices) {
            print("${globalMinimum.rawClusters[i]}\t")
            for (j in matrix[0].indices) {
                print("${matrix[i][j]}\t")
            }
            println()
        }
    }
}