package com.chat.android.im.database

import androidx.room.Room
import com.chat.android.im.config.RLS

/**
 * Created by Ryan on 2020/9/10.
 */
class DBInstance private constructor() {

    private val DB_NAME: String = "room_im"
    private var dataBase: AppDataBase? = null

    companion object {

        fun getInstance(): AppDataBase {
            return Helper.instance.getDataBase()
        }
    }

    private object Helper {
        val instance = DBInstance()
    }

    private fun getDataBase(): AppDataBase {
        if (dataBase == null) {
            dataBase = Room.databaseBuilder(RLS.getInstance().getApp(), AppDataBase::class.java, DB_NAME)
                    .allowMainThreadQueries()
//                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
        }
        return dataBase!!
    }

    //升级语句
//    private val MIGRATION_1_2 = object : Migration(1, 2) {
//        override fun migrate(database: SupportSQLiteDatabase) {
//            database.execSQL("ALTER TABLE ChatMessage ADD COLUMN a INTEGER NOT NULL DEFAULT 0")
//        }
//    }
}