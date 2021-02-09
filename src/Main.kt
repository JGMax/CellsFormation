
fun main() {
    val matrix = DataManager().readDataFromFile("Input.txt")

    val factory = Factory(matrix)
    factory.printGlobal()
    println(factory.getGlobalEffectiveness())
    val startTime = System.currentTimeMillis()
    factory.generalVNS(800)
    val time = System.currentTimeMillis() - startTime
    factory.printGlobal()
    println(factory.getGlobalEffectiveness())
    println(time / 1000.0)
}