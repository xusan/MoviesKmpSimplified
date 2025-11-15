package com.app.impl.cross.Infasructures.Repository.Tables

import com.base.abstractions.Repository.ITable
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey

internal class MovieTb : RealmObject, ITable
{
    @PrimaryKey
    override var Id: Int = 0
    var Name: String = ""
    var Overview: String = ""
    var PostUrl: String = ""
}