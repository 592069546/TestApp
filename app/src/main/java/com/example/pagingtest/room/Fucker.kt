package com.example.pagingtest.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.pagingtest.room.DBConst.FuckerName
import com.example.pagingtest.room.DBConst.FuckerNum
import com.example.pagingtest.room.DBConst.FuckerTableName

@Entity(tableName = FuckerTableName)
class Fucker(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = FuckerName) val fuckerName: String = "fucker",
    @ColumnInfo(name = FuckerNum) val fuckNum: Int = 0
) {
    override fun toString(): String {
        return "Fucker(id=$id, fuckerName='$fuckerName', fuckNum=$fuckNum)"
    }
}