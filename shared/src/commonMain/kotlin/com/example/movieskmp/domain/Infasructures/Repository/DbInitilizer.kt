package com.app.impl.cross.Infasructures.Repository

import com.app.impl.cross.Infasructures.Repository.Tables.MovieTb
import com.base.abstractions.Essentials.IDirectoryService
import com.base.abstractions.Repository.ILocalDbInitilizer
import com.base.impl.Diagnostic.LoggableService
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import org.koin.core.component.inject

internal class DbInitilizer : LoggableService(), ILocalDbInitilizer
{
    var isInited: Boolean = false;
    var realmConn: Realm? = null;

    val directoryService: IDirectoryService by inject()

    override val DbsFolderName: String = "Databases"
    override val DbExtenstion: String = ".realm"
    override val DbName: String = "AppDb$DbExtenstion"

    override fun GetDbDir(): String
    {
        val dbFolder =  "${directoryService.GetAppDataDir()}/$DbsFolderName";
        if(!directoryService.IsExistDir(dbFolder))
        {
            directoryService.CreateDir(dbFolder)
        }

        return dbFolder;
    }

    override fun GetDbPath(): String
    {
        val dbPath = "${GetDbDir()}/$DbName";
        return dbPath;
    }

    override fun GetDbConnection(): Any
    {
        LogMethodStart(::GetDbConnection.name);
        if(realmConn == null)
            throw IllegalStateException("Please call Init() before calling GetDbConnection()")

        return  realmConn!!;
    }

    override suspend fun Init()
    {
        LogMethodStart(::Init.name);
        if(!isInited)
        {
            isInited = true;

            val config = RealmConfiguration.Builder(
                schema = setOf(MovieTb::class)) // your RealmObject classes
                .name(DbName)
                .directory(GetDbDir())
//                .log(LogLevel.ALL, object : RealmLogger {
//                    override fun log(level: LogLevel, message: String) {
//                        println("Realm [$level]: $message")
//                    }
//                })
                .build()

            realmConn = Realm.open(config)
        }
        else
        {
            loggingService.LogWarning("SqliteDbInitilizer skip Init() because isInited:True");
        }
    }

    override suspend fun Release(closeConnection: Boolean)
    {
        LogMethodStart(::Release.name,closeConnection);
        isInited = false
        if(realmConn == null)
        {
            loggingService.LogWarning("DbInitilizer: attempt to close on null realmConn")
        }
        realmConn?.close()
    }

}