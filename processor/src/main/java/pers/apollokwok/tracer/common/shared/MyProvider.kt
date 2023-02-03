package pers.apollokwok.tracer.common.shared

import pers.apollokwok.ksputil.KspProvider
import pers.apollokwok.tracer.common.MyProcessor
import pers.apollokwok.tracer.common._test.TestProcessor

public class MyProvider : KspProvider(::TestProcessor , { MyProcessor })