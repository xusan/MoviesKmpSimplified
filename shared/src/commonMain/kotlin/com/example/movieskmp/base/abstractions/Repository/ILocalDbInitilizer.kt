package com.base.abstractions.Repository

interface ILocalDbInitilizer
{
    val DbsFolderName: String;
    val DbExtenstion: String;
    val DbName: String;
    fun GetDbPath(): String
    fun GetDbDir(): String

    fun GetDbConnection(): Any;
    suspend fun Init();
    suspend fun Release(closeConnection: Boolean = false);
}