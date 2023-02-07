package pers.apollokwok.tracer.common.shared

import pers.apollokwok.ksputil.KspProvider
import pers.apollokwok.tracer.common.MyProcessor

public class MyProvider : KspProvider({ MyProcessor })