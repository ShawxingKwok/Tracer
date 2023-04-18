@file:Suppress("unused", "MemberVisibilityCanBePrivate")

package general

class House {
    // this part is long, changeable, and error-prone in a big project
    private val wifiRouter = WifiRouter()
    val bedroom = Bedroom(wifiRouter)
    val door = Door()
    val livingRoom = LivingRoom(wifiRouter, door, bedroom)
    // solves dependency recycle(LivingRoom and WifiRouter need each other)
    init {
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