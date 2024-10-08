package com.pangaea.idothecooking.state

import com.pangaea.idothecooking.state.db.entities.BaseEntity
import java.util.Date

open class RepositoryBase<T: BaseEntity> {
    fun insertWithTimestamp(o: T): T {
        val curTime = System.currentTimeMillis()
        o.createdAt = Date(curTime)
        o.modifiedAt = Date(curTime)
        return o
    }

    fun bulkInsertWithTimestamps(objs: List<T>): List<T> {
        return objs.map {obj -> insertWithTimestamp(obj)}
    }

    fun updateWithTimestamp(o: T): T {
        val curTime = System.currentTimeMillis()
        o.modifiedAt = Date(curTime)
        return o
    }
}