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
                    if (currentDistribution.effectiveness > localMinimum.effectiveness) {
                        profit = currentDistribution.effectiveness - localMinimum.effectiveness
                        localMinimum = currentDistribution.copy()
                        return profit
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
                    if (currentDistribution.effectiveness > localMinimum.effectiveness) {
                        profit = currentDistribution.effectiveness - localMinimum.effectiveness
                        localMinimum = currentDistribution.copy()
                        return profit
                    }
                } else {
                    break
                }
            }
            currentDistribution = localMinimum.copy()
        }
        return profit
    }

    private fun merge(clusterNum1: Int, clusterNum2: Int) {
        val thisTry = localMinimum.copy()
        if (
            merge(thisTry.columnClusters, clusterNum1, clusterNum2) &&
            merge(thisTry.rawClusters, clusterNum1, clusterNum2)
        ) {
            thisTry.numOfClusters--
            thisTry.calculateEffectiveness()
            localMinimum = thisTry
        }
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

    private fun split(clusterNum: Int) {
        val thisTry = localMinimum.copy()
        if (
            split(thisTry.rawClusters, clusterNum) &&
            split(thisTry.columnClusters, clusterNum)
        ) {
            thisTry.numOfClusters++
            thisTry.calculateEffectiveness()
            localMinimum = thisTry
        }
    }

    private fun split(data: Array<Int>, clusterNum: Int): Boolean {
        if (clusterNum in data) {
            var count = data.count() { it == clusterNum }
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
        do {
            var profit = bestSwap(currentDistribution.rawClusters)
            if (profit > 0.0) {
                fullProfit += profit
                continue
            }
            profit = bestSwap(currentDistribution.columnClusters)
            if (profit > 0.0) {
                fullProfit += profit
                continue
            }
            profit = bestRaiseRaw()
            if (profit > 0.0) {
                fullProfit += profit
                continue
            }
            profit = bestRaiseColumn()
            if (profit > 0.0) {
                fullProfit += profit
                continue
            }
            break
        } while (true)
        return fullProfit
    }

    fun generalVNS(iterationsCount: Int = 15000) {
        if (variableNeighborhoodDescent() > 0.0) {
            globalMinimum = localMinimum.copy()
            currentDistribution = globalMinimum.copy()
            DataManager().writeDataToFile("BestAnswer.txt",
                globalMinimum.rawClusters, globalMinimum.columnClusters)
        }
        for(i in 0..iterationsCount) {
            println("$i Global effectiveness: ${globalMinimum.effectiveness} Clusters: ${globalMinimum.numOfClusters}")

            localMinimum = globalMinimum.copy()
            merge(
                Random.nextInt(1, localMinimum.numOfClusters + 1),
                Random.nextInt(1, localMinimum.numOfClusters + 1)
            )
            currentDistribution = localMinimum.copy()

            if (variableNeighborhoodDescent() > 0.0
                && globalMinimum.effectiveness < localMinimum.effectiveness) {
                globalMinimum = localMinimum.copy()
                currentDistribution = globalMinimum.copy()
                println("!!!Improve!!!")
                DataManager().writeDataToFile("BestAnswer.txt",
                    globalMinimum.rawClusters, globalMinimum.columnClusters)
                continue
            }
            localMinimum = globalMinimum.copy()
            split(Random.nextInt(1, localMinimum.numOfClusters + 1))

            currentDistribution = localMinimum.copy()
            if (variableNeighborhoodDescent() > 0.0
                && globalMinimum.effectiveness < localMinimum.effectiveness) {
                globalMinimum = localMinimum.copy()
                currentDistribution = globalMinimum.copy()
                println("!!!Improve!!!")
                DataManager().writeDataToFile("BestAnswer.txt",
                    globalMinimum.rawClusters, globalMinimum.columnClusters)
                continue
            }
        }
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