import components.CommandProcessor
import components.Database
import components.Outputter
import pers.apollokwok.tracer.common.annotations.Tracer
import pers.apollokwok.tracer.common.generated.AtmTracer
import java.util.*
import kotlin.system.measureTimeMillis

// This example also teaches you how to design a good app. Valid code line number is 116 in this
// module, whereas it's 252 in the original atm sample of kotlin version.
@Tracer.Root
class Atm : AtmTracer {
    companion object{
        @JvmStatic
        fun main(args: Array<String>) {
            val atm: Atm

            val duration = measureTimeMillis { atm = Atm() }
            println("Initializing my atm takes $duration ms.")

            atm.on()
        }
    }

    // Here declares own elements.
    val db = Database()
    val outputter = Outputter()

    // Assume it's expensive to keep the commandProcessor. Therefore, I make it created when somebody
    // starts to input, and destroyed once that user logs out.
    private var commandProcessor: CommandProcessor? = null

    fun on(){
        val scanner = Scanner(System.`in`)
        while (scanner.hasNextLine()){
            if (commandProcessor == null)
                commandProcessor = CommandProcessor(this)

            val continuing = commandProcessor!!.process(scanner.nextLine())

            if (!continuing)
                commandProcessor = null
        }
    }

    // Declare this tracer property here to make it most invisible in this class.
    override val _Atm: Atm = this
}