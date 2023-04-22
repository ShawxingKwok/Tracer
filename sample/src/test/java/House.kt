import pers.apollokwok.tracer.common.annotations.Tracer
import java.io.File

@Tracer.Root
class House : HouseTracer{
    val bedroom = Bedroom()
    val door = Door()
    val livingRoom = LivingRoom()

    override val _House: House get() = this
}

context (HouseTracer)
class Bedroom{
    private val wifiRouter get() = _WifiRouter_LivingRoom_wifiRouter
}

context (HouseTracer)
class Door

context (HouseTracer)
class LivingRoom{
    val wifiRouter = WifiRouter()

    private val door get() = _Door_House_door
    private val bedroom get() = _Bedroom_House_bedroom
}

context (HouseTracer)
class WifiRouter{
    private val livingRoom get() = _LivingRoom_House_livingRoom
}

fun main() {
    val dir = File("/Users/william/AndroidStudioProjects/released/TracerCommon/sample/src/test/java/tracer")
    dir.listFiles()!!.filterNot { it.name == "House.kt" || it.name == "main.kt" }.sortedBy { it.name }.joinToString("\n"){
        val lines = it.readLines()
        val i = lines.indexOfFirst{ it.startsWith("interface") || it.startsWith("class") || it.startsWith("context")}
        "```kotlin\n" + lines.subList(i, lines.size).joinToString("\n") + "\n```\n"
    }
    .let(::println)
}