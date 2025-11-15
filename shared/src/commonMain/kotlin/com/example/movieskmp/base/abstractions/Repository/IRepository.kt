package com.base.abstractions.Repository
import com.base.abstractions.Domain.IEntity;

interface IRepository<TEntity> where TEntity : IEntity
{
    suspend fun FindById(id: Int) : TEntity?;
    suspend fun GetListAsync(count: Int = -1, skip: Int = 0): List<TEntity>;
    suspend fun AddAsync(entity: TEntity) : Int
    suspend fun UpdateAsync(entity: TEntity): Int
    suspend fun AddAllAsync(entities: List<TEntity>): Int
    suspend fun RemoveAsync(entity: TEntity): Int
    suspend fun ClearAsync(reason: String): Int
}