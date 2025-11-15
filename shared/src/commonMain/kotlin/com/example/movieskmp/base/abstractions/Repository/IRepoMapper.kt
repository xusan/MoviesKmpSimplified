package com.base.abstractions.Repository

import com.base.abstractions.Domain.IEntity

interface IRepoMapper<TEntity : IEntity, Tb: ITable>
{
    fun ToTb(entity: TEntity): Tb
    fun ToEntity(tb: Tb): TEntity

    fun MoveData(from: TEntity, to: Tb);
}