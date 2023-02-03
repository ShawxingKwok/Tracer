interface Super{
    val a: Int
}

interface A : Super {
//    val a: Int
    override val a: Int get() = 1
}

interface B : Super {
    override val a: Int get() = 2
}

interface AI : A
interface BI : B