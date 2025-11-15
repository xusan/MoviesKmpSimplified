package com.base.abstractions

import kotlin.experimental.ExperimentalObjCName
import kotlin.native.ObjCName

@OptIn(ExperimentalObjCName::class)
@ObjCName(name = "IConstant", exact = true)
interface IConstant
{
    val ServerUrlHost: String
}