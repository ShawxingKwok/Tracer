import com.sun.org.apache.xml.internal.dtm.ref.DTMChildIterNodeList

annotation class SYMBOL

class LocalSymbols {
    fun foo(){
        @SYMBOL
        val s = 1
    }
}