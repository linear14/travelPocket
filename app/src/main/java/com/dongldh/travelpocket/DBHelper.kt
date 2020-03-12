package com.dongldh.travelpocket

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DBHelper(context: Context): SQLiteOpenHelper(context, "travelDB", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        val t_travel_create = "create table t_travel " +
                "(num integer primary key autoincrement, title, start_day, end_day, country, currency, flag, cover_image, made_time, total_money_mycountry)"


        // 앞으로 할 떄는 datecode를 num과 합쳐서 만들면 되겠다.. 하나 배움 ㅎㅎ^^
        // 예를 들어서.. num = 2, date = 19년 3월 4일 이면.. 2_190304 이런식으로 코드를 만들어서 db에 저장 ㅎㅎ
        val t_content_create = "create table t_content " +
                "(num integer, datecode, type, isPlus, isCash, moneyUsed, used, detail_content, image)"


        val t_budget_create = "create table t_budget " +
                "(num integer, currency, money, code, rate_fromto, rate_tofrom)"


        db?.execSQL(t_travel_create)
        db?.execSQL(t_content_create)
        db?.execSQL(t_budget_create)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("drop table t_travel")
        db?.execSQL("drop table t_content")
        db?.execSQL("drop table t_budget")
        onCreate(db)
    }

}