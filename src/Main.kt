
fun main() {
    val matrix = DataManager().readDataFromFile("Input.txt")

    val factory = Factory(matrix)
    factory.printGlobal()
    println(factory.getGlobalEffectiveness())

    factory.generalVNS(200)

    factory.printGlobal()
    println(factory.getGlobalEffectiveness())
}