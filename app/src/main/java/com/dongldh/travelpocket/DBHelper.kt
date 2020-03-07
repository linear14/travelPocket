package com.dongldh.travelpocket

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context): SQLiteOpenHelper(context, "travelDB", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        val t_travel_create = "create table t_travel " +
                "(num integer primary key autoincrement, title, start_day, end_day, country, flag, cover_image)"

        db?.execSQL(t_travel_create)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("drop table t_travel")
        onCreate(db)
    }

}