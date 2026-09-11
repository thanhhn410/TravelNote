package vn.travelnote.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Db(context: Context) : SQLiteOpenHelper(context.applicationContext, NAME, null, VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE trips(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "destination TEXT NOT NULL DEFAULT ''," +
                "currency TEXT NOT NULL DEFAULT 'USD'," +
                "startDate INTEGER NOT NULL," +
                "endDate INTEGER NOT NULL," +
                "closed INTEGER NOT NULL DEFAULT 0)"
        )
        db.execSQL(
            "CREATE TABLE expenses(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tripId INTEGER NOT NULL," +
                "category TEXT NOT NULL," +
                "amount REAL NOT NULL," +
                "currency TEXT NOT NULL," +
                "rate REAL NOT NULL," +
                "amountVnd REAL NOT NULL," +
                "note TEXT NOT NULL DEFAULT ''," +
                "date INTEGER NOT NULL)"
        )
        db.execSQL(
            "CREATE TABLE topups(" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "tripId INTEGER NOT NULL," +
                "amount REAL NOT NULL," +
                "currency TEXT NOT NULL," +
                "rate REAL NOT NULL," +
                "amountVnd REAL NOT NULL," +
                "note TEXT NOT NULL DEFAULT ''," +
                "date INTEGER NOT NULL)"
        )
        db.execSQL("CREATE INDEX idx_exp_trip ON expenses(tripId)")
        db.execSQL("CREATE INDEX idx_top_trip ON topups(tripId)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Schema v1: chua co migration nao
    }

    companion object {
        const val NAME = "travelnote.db"
        const val VERSION = 1
    }
}
