package com.cafemanager.app.data

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    @ColumnInfo(defaultValue = "''") val cityCode: String = "",
    @ColumnInfo(defaultValue = "''") val birthCity: String = "",
    @ColumnInfo(defaultValue = "''") val idIssueCity: String = "",
    @ColumnInfo(defaultValue = "''") val idIssueDate: String = "",
    @ColumnInfo(defaultValue = "''") val idSerialLetter: String = "",
    @ColumnInfo(defaultValue = "''") val idSerialSeries: String = "",
    @ColumnInfo(defaultValue = "''") val idSerialNumber: String = "",
    @ColumnInfo(defaultValue = "''") val job: String = "",
    @ColumnInfo(defaultValue = "''") val education: String = "",
    @ColumnInfo(defaultValue = "''") val iban: String = "",
    @ColumnInfo(defaultValue = "''") val bankName: String = "",
    @ColumnInfo(defaultValue = "''") val bankBranch: String = "",
    @ColumnInfo(defaultValue = "''") val branchCode: String = "",
    @ColumnInfo(defaultValue = "''") val eitaaId: String = "",
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
        InvoiceItem::class, Txn::class, Debt::class, CashDay::class, CustomerPassword::class
    ],
    version = 4,
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
    abstract fun passwords(): PasswordDao

    companion object {
        @Volatile private var inst: AppDatabase? = null
        fun get(ctx: Context): AppDatabase = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(ctx.applicationContext, AppDatabase::class.java, "cafe_manager.db")
                .addMigrations(MIGRATION_2_3, MIGRATION_3_4).fallbackToDestructiveMigration().build().also { inst = it }
        }
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE customers ADD COLUMN cityCode TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        listOf("birthCity", "idIssueCity", "idIssueDate", "idSerialLetter", "idSerialSeries", "idSerialNumber", "job", "education", "iban", "bankName", "bankBranch", "branchCode", "eitaaId").forEach { col ->
            db.execSQL("ALTER TABLE customers ADD COLUMN " + col + " TEXT NOT NULL DEFAULT ''")
        }
        db.execSQL("CREATE TABLE IF NOT EXISTS `customer_passwords` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `customerId` INTEGER NOT NULL, `system` TEXT NOT NULL, `username` TEXT NOT NULL, `password` TEXT NOT NULL)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_customer_passwords_customerId` ON `customer_passwords` (`customerId`)")
    }
}
