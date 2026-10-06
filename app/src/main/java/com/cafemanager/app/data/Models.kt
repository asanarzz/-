package com.cafemanager.app.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "services")
data class Service(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val price: Long,
    val unit: String = "عدد",
    val category: String = "سایر",
    val description: String = "",
    val deletedAt: Long? = null
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long? = null,
    val customerName: String = "",
    val subtotal: Long,
    val discount: Long = 0,
    val total: Long,
    val paid: Long,
    val method: String = "نقدی",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "invoice_items", indices = [Index("invoiceId")])
data class InvoiceItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val invoiceId: Long,
    val serviceName: String,
    val unitPrice: Long,
    val qty: Int,
    val lineTotal: Long
)

/** تراکنش مالی. inProfit=false یعنی حرکت صندوق (واریز/برداشت) که در سود حساب نمی‌شود. */
@Entity(tableName = "transactions", indices = [Index("createdAt")])
data class Txn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val isIncome: Boolean,
    val amount: Long,
    val title: String,
    val category: String = "",
    val method: String = "نقدی",
    val customerId: Long? = null,
    val note: String = "",
    val inProfit: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "debts", indices = [Index("customerId")])
data class Debt(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long?,
    val customerName: String,
    val amount: Long,
    val paid: Long = 0,
    val reason: String = "",
    val dueDate: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "cash_days")
data class CashDay(
    @PrimaryKey val dateKey: String,
    val opening: Long,
    val closedAt: Long? = null,
    val actual: Long? = null,
    val expectedAtClose: Long? = null
)

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services WHERE deletedAt IS NULL ORDER BY category, name")
    fun all(): Flow<List<Service>>

    @Insert suspend fun insert(s: Service): Long
    @Update suspend fun update(s: Service)

    @Query("UPDATE services SET deletedAt = :t WHERE id = :id")
    suspend fun trash(id: Long, t: Long)
}

@Dao
interface InvoiceDao {
    @Insert suspend fun insert(i: Invoice): Long
    @Insert suspend fun insertItems(items: List<InvoiceItem>)

    @Query("SELECT * FROM invoices ORDER BY createdAt DESC LIMIT 200")
    fun recent(): Flow<List<Invoice>>

    @Query("SELECT * FROM invoices WHERE id = :id")
    suspend fun get(id: Long): Invoice?

    @Query("SELECT * FROM invoice_items WHERE invoiceId = :id")
    suspend fun items(id: Long): List<InvoiceItem>
}

@Dao
interface TxnDao {
    @Insert suspend fun insert(t: Txn): Long

    @Query("SELECT * FROM transactions WHERE createdAt BETWEEN :from AND :to ORDER BY createdAt DESC")
    fun between(from: Long, to: Long): Flow<List<Txn>>
}

@Dao
interface DebtDao {
    @Insert suspend fun insert(d: Debt): Long

    @Query("SELECT * FROM debts WHERE paid < amount ORDER BY createdAt DESC")
    fun open(): Flow<List<Debt>>

    @Query("UPDATE debts SET paid = paid + :p WHERE id = :id")
    suspend fun addPayment(id: Long, p: Long)
}

@Dao
interface CashDao {
    @Query("SELECT * FROM cash_days WHERE dateKey = :k")
    fun day(k: String): Flow<CashDay?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(d: CashDay)
}
