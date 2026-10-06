package com.cafemanager.app.data

import android.content.Context
import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "customers", indices = [Index("mobile"), Index("nationalId")])
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val fatherName: String = "",
    val nationalId: String = "",
    val mobile: String = "",
    val phone: String = "",
    val birthDate: String = "",
    val address: String = "",
    val postalCode: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null
)

@Entity(tableName = "activity_log")
data class ActivityEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val message: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface CustomerDao {
    @Query(
        "SELECT * FROM customers WHERE deletedAt IS NULL AND (" +
            "firstName LIKE '%' || :q || '%' OR lastName LIKE '%' || :q || '%' OR " +
            "mobile LIKE '%' || :q || '%' OR nationalId LIKE '%' || :q || '%') " +
            "ORDER BY lastName, firstName"
    )
    fun search(q: String): Flow<List<Customer>>

    @Query("SELECT COUNT(*) FROM customers WHERE deletedAt IS NULL")
    fun count(): Flow<Int>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun get(id: Long): Customer?

    @Insert suspend fun insert(c: Customer): Long
    @Update suspend fun update(c: Customer)

    @Query("UPDATE customers SET deletedAt = :t WHERE id = :id")
    suspend fun trash(id: Long, t: Long)
}

@Dao
interface ActivityDao {
    @Insert suspend fun insert(e: ActivityEntry)

    @Query("SELECT * FROM activity_log ORDER BY createdAt DESC LIMIT :n")
    fun recent(n: Int): Flow<List<ActivityEntry>>
}

@Database(
    entities = [
        Customer::class, ActivityEntry::class, Service::class, Invoice::class,
        InvoiceItem::class, Txn::class, Debt::class, CashDay::class
    ],
    version = 2,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun customers(): CustomerDao
    abstract fun activity(): ActivityDao
    abstract fun services(): ServiceDao
    abstract fun invoices(): InvoiceDao
    abstract fun txns(): TxnDao
    abstract fun debts(): DebtDao
    abstract fun cash(): CashDao

    companion object {
        @Volatile private var inst: AppDatabase? = null
        fun get(ctx: Context): AppDatabase = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "cafe_manager.db")
                .fallbackToDestructiveMigration().build().also { inst = it }
        }
    }
}
