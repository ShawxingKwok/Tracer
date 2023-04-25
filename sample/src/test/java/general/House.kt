@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package general

class House {
    val bedroom: Bedroom
    val door: Door
    val livingRoom: LivingRoom

    init {
        // this part is long, changeable, and error-prone in a big project
        val wifiRouter = WifiRouter()
        bedroom = Bedroom(wifiRouter)
        door = Door()
        livingRoom = LivingRoom(wifiRouter, door, bedroom)

        // solves dependency recycle(LivingRoom and WifiRouter need each other)
        wifiRouter.livingRoom = livingRoom
    }
}

class Bedroom(private val wifiRouter: WifiRouter)

class Door

class LivingRoom(
    private val wifiRouter: WifiRouter,
    private val door: Door,
    private val bedroom: Bedroom,
)

class WifiRouter{
    lateinit var livingRoom: LivingRoom
}