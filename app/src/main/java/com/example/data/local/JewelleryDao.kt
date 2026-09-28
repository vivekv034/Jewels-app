package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface JewelleryDao {
  @Query("SELECT * FROM business_profile WHERE id = 1")
  fun observeBusinessProfile(): Flow<BusinessProfileEntity?>

  @Query("SELECT * FROM business_profile WHERE id = 1")
  suspend fun getBusinessProfileOnce(): BusinessProfileEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertBusinessProfile(profile: BusinessProfileEntity)

  @Query("SELECT * FROM metal_rates WHERE id = 1")
  fun observeMetalRate(): Flow<MetalRateEntity?>

  @Query("SELECT * FROM metal_rates WHERE id = 1")
  suspend fun getMetalRateOnce(): MetalRateEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertMetalRate(rate: MetalRateEntity)

  @Query("SELECT * FROM customers ORDER BY name ASC")
  fun observeCustomers(): Flow<List<CustomerEntity>>

  @Query("SELECT * FROM customers ORDER BY name ASC")
  suspend fun getCustomersOnce(): List<CustomerEntity>

  @Query("SELECT * FROM customers WHERE mobileNumber = :mobile LIMIT 1")
  suspend fun findCustomerByMobile(mobile: String): CustomerEntity?

  @Query("SELECT COUNT(*) FROM customers")
  suspend fun getCustomerCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertCustomer(customer: CustomerEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertCustomers(customers: List<CustomerEntity>)

  @Query("DELETE FROM customers WHERE id = :customerId")
  suspend fun deleteCustomerById(customerId: String)

  @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
  fun observeTransactions(): Flow<List<TransactionEntity>>

  @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
  suspend fun getTransactionsOnce(): List<TransactionEntity>

  @Query("SELECT * FROM transactions WHERE transactionId = :transactionId LIMIT 1")
  suspend fun getTransactionById(transactionId: String): TransactionEntity?

  @Query("SELECT COUNT(*) FROM transactions")
  suspend fun getTransactionCount(): Int

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertTransaction(transaction: TransactionEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertTransactions(transactions: List<TransactionEntity>)

  @Query("UPDATE transactions SET syncStatus = :syncStatus WHERE transactionId = :transactionId")
  suspend fun updateTransactionSyncStatus(transactionId: String, syncStatus: String)

  @Query("DELETE FROM transactions WHERE transactionId = :transactionId")
  suspend fun deleteTransactionById(transactionId: String)

  @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
  fun observeInvoices(): Flow<List<InvoiceEntity>>

  @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
  suspend fun getInvoicesOnce(): List<InvoiceEntity>

  @Query("SELECT * FROM invoices WHERE invoiceNumber = :idOrNumber OR invoiceId = :idOrNumber LIMIT 1")
  suspend fun getInvoiceByIdOrNumber(idOrNumber: String): InvoiceEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertInvoice(invoice: InvoiceEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertInvoices(invoices: List<InvoiceEntity>)

  @Query("DELETE FROM invoices WHERE invoiceNumber = :invoiceNumber")
  suspend fun deleteInvoiceByNumber(invoiceNumber: String)

  @Query("DELETE FROM invoices WHERE transactionId = :transactionId")
  suspend fun deleteInvoiceByTransactionId(transactionId: String)

  // Pending Sync Queue
  @Query("SELECT * FROM pending_sync_queue ORDER BY createdAt ASC")
  fun observePendingSyncQueue(): Flow<List<PendingSyncQueueEntity>>

  @Query("SELECT * FROM pending_sync_queue ORDER BY createdAt ASC")
  suspend fun getPendingSyncQueueOnce(): List<PendingSyncQueueEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertPendingSyncRecord(record: PendingSyncQueueEntity)

  @Query("DELETE FROM pending_sync_queue WHERE recordId = :recordId")
  suspend fun deletePendingSyncRecord(recordId: String)

  @Query(
    "UPDATE pending_sync_queue SET syncStatus = :status, lastError = :error, retryCount = retryCount + 1 WHERE recordId = :recordId"
  )
  suspend fun markPendingSyncFailed(recordId: String, status: String, error: String)

  @Query("DELETE FROM pending_sync_queue")
  suspend fun clearAllPendingSync()

  // Pending Drive Uploads Queue (Stage 4)
  @Query("SELECT * FROM pending_drive_uploads ORDER BY createdAt ASC")
  fun observePendingDriveUploads(): Flow<List<PendingDriveUploadEntity>>

  @Query("SELECT * FROM pending_drive_uploads ORDER BY createdAt ASC")
  suspend fun getPendingDriveUploadsOnce(): List<PendingDriveUploadEntity>

  @Query("SELECT * FROM pending_drive_uploads WHERE uploadId = :uploadId LIMIT 1")
  suspend fun getPendingDriveUploadById(uploadId: String): PendingDriveUploadEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertPendingDriveUpload(upload: PendingDriveUploadEntity)

  @Query("DELETE FROM pending_drive_uploads WHERE uploadId = :uploadId")
  suspend fun deletePendingDriveUpload(uploadId: String)

  @Query(
    "UPDATE pending_drive_uploads SET status = :status, lastError = :error, retryCount = retryCount + 1 WHERE uploadId = :uploadId"
  )
  suspend fun markPendingDriveUploadFailed(uploadId: String, status: String, error: String)

  @Query("DELETE FROM pending_drive_uploads")
  suspend fun clearAllPendingDriveUploads()

  // Google Account Config
  @Query("SELECT * FROM google_account_config WHERE id = 1")
  fun observeGoogleAccountConfig(): Flow<GoogleAccountConfigEntity?>

  @Query("SELECT * FROM google_account_config WHERE id = 1")
  suspend fun getGoogleAccountConfigOnce(): GoogleAccountConfigEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertGoogleAccountConfig(config: GoogleAccountConfigEntity)

  @Query("DELETE FROM transactions")
  suspend fun clearAllTransactions()

  @Query("DELETE FROM invoices")
  suspend fun clearAllInvoices()

  @Query("DELETE FROM customers")
  suspend fun clearAllCustomers()

  // ==================== STAGE 5: INVENTORY, VENDORS, PURCHASES, SALES ====================

  @Query("SELECT * FROM opening_stock ORDER BY metal ASC")
  fun observeOpeningStocks(): Flow<List<OpeningStockEntity>>

  @Query("SELECT * FROM opening_stock WHERE metal = :metal LIMIT 1")
  suspend fun getOpeningStockByMetal(metal: String): OpeningStockEntity?

  @Query("SELECT * FROM opening_stock")
  suspend fun getOpeningStocksOnce(): List<OpeningStockEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertOpeningStock(stock: OpeningStockEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertOpeningStocks(stocks: List<OpeningStockEntity>)

  @Query("SELECT * FROM inventory_movements ORDER BY createdAt DESC")
  fun observeInventoryMovements(): Flow<List<InventoryMovementEntity>>

  @Query("SELECT * FROM inventory_movements ORDER BY createdAt DESC")
  suspend fun getInventoryMovementsOnce(): List<InventoryMovementEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertInventoryMovement(movement: InventoryMovementEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertInventoryMovements(movements: List<InventoryMovementEntity>)

  @Query("DELETE FROM inventory_movements")
  suspend fun clearAllInventoryMovements()

  @Query("SELECT * FROM inventory_reconciliations ORDER BY createdAt DESC")
  fun observeInventoryReconciliations(): Flow<List<InventoryReconciliationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertInventoryReconciliation(record: InventoryReconciliationEntity)

  @Query("SELECT * FROM vendors ORDER BY name ASC")
  fun observeVendors(): Flow<List<VendorEntity>>

  @Query("SELECT * FROM vendors ORDER BY name ASC")
  suspend fun getVendorsOnce(): List<VendorEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertVendor(vendor: VendorEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertVendors(vendors: List<VendorEntity>)

  @Query("DELETE FROM vendors WHERE vendorId = :vendorId")
  suspend fun deleteVendorById(vendorId: String)

  @Query("DELETE FROM vendors")
  suspend fun clearAllVendors()

  @Query("SELECT * FROM purchases ORDER BY createdAt DESC")
  fun observePurchases(): Flow<List<PurchaseEntity>>

  @Query("SELECT * FROM purchases ORDER BY createdAt DESC")
  suspend fun getPurchasesOnce(): List<PurchaseEntity>

  @Query("SELECT * FROM purchases WHERE purchaseId = :purchaseId LIMIT 1")
  suspend fun getPurchaseById(purchaseId: String): PurchaseEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertPurchase(purchase: PurchaseEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertPurchases(purchases: List<PurchaseEntity>)

  @Query("DELETE FROM purchases")
  suspend fun clearAllPurchases()

  @Query("SELECT * FROM sales ORDER BY createdAt DESC")
  fun observeSales(): Flow<List<SaleEntity>>

  @Query("SELECT * FROM sales ORDER BY createdAt DESC")
  suspend fun getSalesOnce(): List<SaleEntity>

  @Query("SELECT * FROM sales WHERE saleId = :saleId LIMIT 1")
  suspend fun getSaleById(saleId: String): SaleEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertSale(sale: SaleEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertSales(sales: List<SaleEntity>)

  @Query("DELETE FROM sales")
  suspend fun clearAllSales()

  @Query("SELECT * FROM scrap_processings ORDER BY createdAt DESC")
  fun observeScrapProcessings(): Flow<List<ScrapProcessingEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertScrapProcessing(record: ScrapProcessingEntity)

  // ==================== STAGE 6: SECURITY, AUDIT LOGS & REMINDERS ====================

  @Query("SELECT * FROM security_settings WHERE id = 1")
  fun observeSecuritySettings(): Flow<SecuritySettingsEntity?>

  @Query("SELECT * FROM security_settings WHERE id = 1")
  suspend fun getSecuritySettingsOnce(): SecuritySettingsEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertSecuritySettings(settings: SecuritySettingsEntity)

  @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
  fun observeAuditLogs(): Flow<List<AuditLogEntity>>

  @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
  suspend fun getAuditLogsOnce(): List<AuditLogEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAuditLog(log: AuditLogEntity)

  @Query("SELECT * FROM shop_reminders ORDER BY isCompleted ASC, dueDate ASC")
  fun observeReminders(): Flow<List<ReminderEntity>>

  @Query("SELECT * FROM shop_reminders ORDER BY isCompleted ASC, dueDate ASC")
  suspend fun getRemindersOnce(): List<ReminderEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertReminder(reminder: ReminderEntity)

  @Query("DELETE FROM shop_reminders WHERE reminderId = :reminderId")
  suspend fun deleteReminderById(reminderId: String)
}
