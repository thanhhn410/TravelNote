package vn.travelnote.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor

class Repo(context: Context) {

    private val helper = Db(context)

    // ---------- Trips ----------

    fun trips(): List<Trip> {
        val out = ArrayList<Trip>()
        helper.readableDatabase.rawQuery(
            "SELECT id,name,destination,currency,startDate,endDate,closed FROM trips ORDER BY startDate DESC, id DESC",
            null
        ).use { c ->
            while (c.moveToNext()) out.add(readTrip(c))
        }
        return out
    }

    fun trip(id: Long): Trip? {
        helper.readableDatabase.rawQuery(
            "SELECT id,name,destination,currency,startDate,endDate,closed FROM trips WHERE id=?",
            arrayOf(id.toString())
        ).use { c -> if (c.moveToFirst()) return readTrip(c) }
        return null
    }

    fun saveTrip(t: Trip): Long {
        val v = ContentValues().apply {
            put("name", t.name)
            put("destination", t.destination)
            put("currency", t.currency)
            put("startDate", t.startDate)
            put("endDate", t.endDate)
            put("closed", if (t.closed) 1 else 0)
        }
        val db = helper.writableDatabase
        return if (t.id == 0L) db.insert("trips", null, v)
        else {
            db.update("trips", v, "id=?", arrayOf(t.id.toString()))
            t.id
        }
    }

    fun deleteTrip(id: Long) {
        val db = helper.writableDatabase
        db.delete("expenses", "tripId=?", arrayOf(id.toString()))
        db.delete("topups", "tripId=?", arrayOf(id.toString()))
        db.delete("trips", "id=?", arrayOf(id.toString()))
    }

    // ---------- Expenses ----------

    fun expenses(tripId: Long): List<Expense> {
        val out = ArrayList<Expense>()
        helper.readableDatabase.rawQuery(
            "SELECT id,tripId,category,amount,currency,rate,amountVnd,note,date FROM expenses WHERE tripId=? ORDER BY date DESC, id DESC",
            arrayOf(tripId.toString())
        ).use { c -> while (c.moveToNext()) out.add(readExpense(c)) }
        return out
    }

    fun saveExpense(e: Expense): Long {
        val v = ContentValues().apply {
            put("tripId", e.tripId)
            put("category", e.category)
            put("amount", e.amount)
            put("currency", e.currency)
            put("rate", e.rate)
            put("amountVnd", e.amountVnd)
            put("note", e.note)
            put("date", e.date)
        }
        val db = helper.writableDatabase
        return if (e.id == 0L) db.insert("expenses", null, v)
        else {
            db.update("expenses", v, "id=?", arrayOf(e.id.toString()))
            e.id
        }
    }

    fun deleteExpense(id: Long) {
        helper.writableDatabase.delete("expenses", "id=?", arrayOf(id.toString()))
    }

    // ---------- Top-ups ----------

    fun topups(tripId: Long): List<TopUp> {
        val out = ArrayList<TopUp>()
        helper.readableDatabase.rawQuery(
            "SELECT id,tripId,amount,currency,rate,amountVnd,note,date FROM topups WHERE tripId=? ORDER BY date DESC, id DESC",
            arrayOf(tripId.toString())
        ).use { c -> while (c.moveToNext()) out.add(readTopUp(c)) }
        return out
    }

    fun saveTopUp(t: TopUp): Long {
        val v = ContentValues().apply {
            put("tripId", t.tripId)
            put("amount", t.amount)
            put("currency", t.currency)
            put("rate", t.rate)
            put("amountVnd", t.amountVnd)
            put("note", t.note)
            put("date", t.date)
        }
        val db = helper.writableDatabase
        return if (t.id == 0L) db.insert("topups", null, v)
        else {
            db.update("topups", v, "id=?", arrayOf(t.id.toString()))
            t.id
        }
    }

    fun deleteTopUp(id: Long) {
        helper.writableDatabase.delete("topups", "id=?", arrayOf(id.toString()))
    }

    // ---------- Backup ----------

    fun wipeAll() {
        val db = helper.writableDatabase
        db.delete("expenses", null, null)
        db.delete("topups", null, null)
        db.delete("trips", null, null)
    }

    fun insertRawTrip(t: Trip): Long {
        val v = ContentValues().apply {
            put("id", t.id)
            put("name", t.name)
            put("destination", t.destination)
            put("currency", t.currency)
            put("startDate", t.startDate)
            put("endDate", t.endDate)
            put("closed", if (t.closed) 1 else 0)
        }
        return helper.writableDatabase.insertWithOnConflict("trips", null, v, 5)
    }

    fun insertRawExpense(e: Expense) {
        val v = ContentValues().apply {
            put("id", e.id)
            put("tripId", e.tripId)
            put("category", e.category)
            put("amount", e.amount)
            put("currency", e.currency)
            put("rate", e.rate)
            put("amountVnd", e.amountVnd)
            put("note", e.note)
            put("date", e.date)
        }
        helper.writableDatabase.insertWithOnConflict("expenses", null, v, 5)
    }

    fun insertRawTopUp(t: TopUp) {
        val v = ContentValues().apply {
            put("id", t.id)
            put("tripId", t.tripId)
            put("amount", t.amount)
            put("currency", t.currency)
            put("rate", t.rate)
            put("amountVnd", t.amountVnd)
            put("note", t.note)
            put("date", t.date)
        }
        helper.writableDatabase.insertWithOnConflict("topups", null, v, 5)
    }

    // ---------- Aggregates ----------

    /** tripId -> [tong chi VND, tong nap VND] */
    fun totals(): Map<Long, DoubleArray> {
        val m = HashMap<Long, DoubleArray>()
        val db = helper.readableDatabase
        db.rawQuery("SELECT tripId, SUM(amountVnd) FROM expenses GROUP BY tripId", null).use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(0)
                m.getOrPut(id) { doubleArrayOf(0.0, 0.0) }[0] = c.getDouble(1)
            }
        }
        db.rawQuery("SELECT tripId, SUM(amountVnd) FROM topups GROUP BY tripId", null).use { c ->
            while (c.moveToNext()) {
                val id = c.getLong(0)
                m.getOrPut(id) { doubleArrayOf(0.0, 0.0) }[1] = c.getDouble(1)
            }
        }
        return m
    }

    // ---------- Cursor mappers ----------

    private fun readTrip(c: Cursor) = Trip(
        id = c.getLong(0),
        name = c.getString(1),
        destination = c.getString(2) ?: "",
        currency = c.getString(3),
        startDate = c.getLong(4),
        endDate = c.getLong(5),
        closed = c.getInt(6) == 1
    )

    private fun readExpense(c: Cursor) = Expense(
        id = c.getLong(0),
        tripId = c.getLong(1),
        category = c.getString(2),
        amount = c.getDouble(3),
        currency = c.getString(4),
        rate = c.getDouble(5),
        amountVnd = c.getDouble(6),
        note = c.getString(7) ?: "",
        date = c.getLong(8)
    )

    private fun readTopUp(c: Cursor) = TopUp(
        id = c.getLong(0),
        tripId = c.getLong(1),
        amount = c.getDouble(2),
        currency = c.getString(3),
        rate = c.getDouble(4),
        amountVnd = c.getDouble(5),
        note = c.getString(6) ?: "",
        date = c.getLong(7)
    )
}
