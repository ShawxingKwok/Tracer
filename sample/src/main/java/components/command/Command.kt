package components.command

interface Command {
    fun handleInput(input: String): Result

    data class Result(val msg: String, val continuing: Boolean = true)
}