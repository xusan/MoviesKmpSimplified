package com.base.impl.Repository

import com.base.abstractions.Domain.IEntity
import com.base.abstractions.Repository.ILocalDbInitilizer
import com.base.abstractions.Repository.IRepoMapper
import com.base.abstractions.Repository.IRepository
import com.base.abstractions.Repository.ITable
import com.base.impl.Diagnostic.LoggableService
import io.realm.kotlin.Realm
//import io.realm.kotlin.ext.query
import io.realm.kotlin.types.RealmObject
import org.koin.core.component.inject
import kotlin.reflect.KClass

open class BaseRepository<TEntity, Tb>(private val tableClass: KClass<Tb>) : LoggableService(), IRepository<TEntity>
where TEntity : IEntity, Tb : RealmObject, Tb: ITable
{
    protected var realm: Realm? = null
    protected val mapper: IRepoMapper<TEntity, Tb> by inject()
    protected val dbConnectionInitilizer: ILocalDbInitilizer by inject()


    override suspend fun GetListAsync(count: Int, skip: Int): List<TEntity>
    {
        LogMethodStart(::GetListAsync.name, count, skip)
        EnsureInitalized()

        var results: List<Tb>? = null;
        if(count > 0)
        {
            results = realm!!.query(tableClass, "id > $0 SORT(id ASC)", skip)
                .limit(count)
                .find()
        }
        else
        {
            results = realm!!.query(tableClass).find()
        }
        val result = results.map { mapper.ToEntity(it) }
        return result;
    }

    override suspend fun AddAllAsync(entities: List<TEntity>): Int
    {
        LogMethodStart(::AddAllAsync.name, entities)
        EnsureInitalized()
        var lastId = -1;
        realm!!.write()
        {
            entities.forEach{ entity ->

                if(lastId != -1)
                    lastId ++;
                else
                    lastId = GetNextId();
                entity.Id = lastId

                val tbRow = mapper.ToTb(entity);
                copyToRealm(tbRow)
            }
        }
        return entities.size
    }

    override suspend fun FindById(id: Int): TEntity?
    {
        LogMethodStart(::FindById.name, id)
        EnsureInitalized()
        val tb = realm!!.query(tableClass, "Id == $0", id).first().find()
        if(tb != null)
        {
            val entity = mapper.ToEntity(tb)
            return entity;
        }
        return null;
    }

    override suspend fun AddAsync(entity: TEntity) : Int
    {
        LogMethodStart(::AddAsync.name, entity)
        EnsureInitalized()

        entity.Id = GetNextId();
        val tb = mapper.ToTb(entity)
        realm!!.write { copyToRealm(tb) } //we can call with !! because we have EnsureInitalized() check
        return 1;
    }

    /** Update via mapping */
    override suspend fun UpdateAsync(entity: TEntity) : Int
    {
        LogMethodStart(::UpdateAsync.name, entity)
        EnsureInitalized()
        var hasValue = false;
        realm!!.write() //we can call with !! because we have EnsureInitalized() check
        {
            val tb = query(tableClass, "Id == $0", entity.Id).first().find()
            if (tb != null)
            {
                hasValue = true;
                mapper.MoveData(entity, tb);
            }
        }

        return if(hasValue) 1 else 0;
    }

    /** Delete by id */
    override suspend fun RemoveAsync(entity: TEntity) : Int
    {
        LogMethodStart(::RemoveAsync.name, entity)
        EnsureInitalized()
        var hasValue = false;
        realm!!.write() //we can call with !! because we have EnsureInitalized() check
        {
            val tb = query(tableClass, "Id == $0", entity.Id).first().find()
            if (tb != null)
            {
                hasValue = true;
                delete(tb)
            }
        }
        return if(hasValue) 1 else 0;
    }

    override suspend fun ClearAsync(reason: String): Int
    {
        LogMethodStart(::ClearAsync.name, reason)
        EnsureInitalized()
        var deletedCount = 0
        realm!!.write() //we can call with !! because we have EnsureInitalized() check
        {
            val query = this.query(tableClass)
            deletedCount = query.count().find().toInt()
            delete(tableClass)// deletes all objects of this type
        }
        loggingService.LogWarning("${this::class.simpleName!!}: Cleared $deletedCount records from ${tableClass.simpleName}: $reason")
        return deletedCount
    }

    private fun EnsureInitalized()
    {
        //check whether realm inited
        if (realm == null || IsDbClosed())
        {
            realm = dbConnectionInitilizer.GetDbConnection() as Realm;
            if (realm == null)
            {
                throw IllegalStateException("realm is null, it seems it doesn't initialized (ILocalDbInitilizer.Init())");
            }
        }

    }

    private fun IsDbClosed() : Boolean
    {
        try
        {
            return realm?.isClosed()!!
        }
        catch (ex: Throwable)
        {
            return true
        }
    }

    private fun GetNextId() : Int
    {
        val maxId = realm!!.query(tableClass).max("Id", Int::class).find() ?: 0
        if(maxId == 0)
        {
            return 1;
        }
        else
        {
            val newId = maxId + 1;
            return newId;
        }

    }

}