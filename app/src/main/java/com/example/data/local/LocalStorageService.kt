package com.example.data.local

import com.example.domain.calculation.CalculationEngine
import com.example.domain.model.AuditActionType
import com.example.domain.model.AuditLogEntry
import com.example.domain.model.BusinessProfile
import com.example.domain.model.Customer
import com.example.domain.model.DriveUploadStatus
import com.example.domain.model.GoogleAccountState
import com.example.domain.model.InventoryMovement
import com.example.domain.model.InventoryMovementDirection
import com.example.domain.model.InventoryMovementType
import com.example.domain.model.InventoryReconciliationRecord
import com.example.domain.model.Invoice
import com.example.domain.model.InvoiceStatus
import com.example.domain.model.MetalRate
import com.example.domain.model.MetalType
import com.example.domain.model.OpeningStockBalance
import com.example.domain.model.PaymentMode
import com.example.domain.model.PendingDriveUpload
import com.example.domain.model.PendingSyncRecord
import com.example.domain.model.PurchaseRecord
import com.example.domain.model.PuritySelectionMode
import com.example.domain.model.RateUnit
import com.example.domain.model.ReminderType
import com.example.domain.model.SaleRecord
import com.example.domain.model.ScrapProcessingRecord
import com.example.domain.model.SecuritySettings
import com.example.domain.model.ShopReminder
import com.example.domain.model.SyncStatus
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionStatus
import com.example.domain.model.TransactionType
import com.example.domain.model.Vendor
import com.example.domain.service.InventoryService
import com.example.domain.service.InvoiceService
import com.example.domain.service.StorageService
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

class LocalStorageService(
  private val dao: JewelleryDao,
  private val invoiceService: InvoiceService = InvoiceService(),
  private val calculationEngine: CalculationEngine = CalculationEngine(),
) : StorageService {

  private val inventoryService: InventoryService by lazy {
    InventoryService(calculationEngine = calculationEngine)
  }

  override val businessProfileFlow: Flow<BusinessProfile> =
    dao.observeBusinessProfile().map { it?.toDomain() ?: BusinessProfile() }

  override val metalRateFlow: Flow<MetalRate> =
    dao.observeMetalRate().map { it?.toDomain() ?: MetalRate() }

  override val customersFlow: Flow<List<Customer>> =
    dao.observeCustomers().map { list -> list.map { it.toDomain() } }

  override val transactionsFlow: Flow<List<Transaction>> =
    dao.observeTransactions().map { list -> list.map { it.toDomain() } }

  override val invoicesFlow: Flow<List<Invoice>> =
    dao.observeInvoices().map { list -> list.map { it.toDomain() } }

  override val pendingSyncQueueFlow: Flow<List<PendingSyncRecord>> =
    dao.observePendingSyncQueue().map { list -> list.map { it.toDomain() } }

  override val pendingDriveUploadsFlow: Flow<List<PendingDriveUpload>> =
    dao.observePendingDriveUploads().map { list -> list.map { it.toDomain() } }

  override val openingStocksFlow: Flow<List<OpeningStockBalance>> =
    dao.observeOpeningStocks().map { list -> list.map { it.toDomain() } }

  override val inventoryMovementsFlow: Flow<List<InventoryMovement>> =
    dao.observeInventoryMovements().map { list -> list.map { it.toDomain() } }

  override val inventoryReconciliationsFlow: Flow<List<InventoryReconciliationRecord>> =
    dao.observeInventoryReconciliations().map { list -> list.map { it.toDomain() } }

  override val vendorsFlow: Flow<List<Vendor>> =
    dao.observeVendors().map { list -> list.map { it.toDomain() } }

  override val purchasesFlow: Flow<List<PurchaseRecord>> =
    dao.observePurchases().map { list -> list.map { it.toDomain() } }

  override val salesFlow: Flow<List<SaleRecord>> =
    dao.observeSales().map { list -> list.map { it.toDomain() } }

  override val scrapProcessingsFlow: Flow<List<ScrapProcessingRecord>> =
    dao.observeScrapProcessings().map { list -> list.map { it.toDomain() } }

  override val securitySettingsFlow: Flow<SecuritySettings> =
    dao.observeSecuritySettings().map { it?.toDomain() ?: SecuritySettings() }

  override val auditLogsFlow: Flow<List<AuditLogEntry>> =
    dao.observeAuditLogs().map { list -> list.map { it.toDomain() } }

  override val remindersFlow: Flow<List<ShopReminder>> =
    dao.observeReminders().map { list -> list.map { it.toDomain() } }

  override val googleAccountStateFlow: Flow<GoogleAccountState> =
    combine(
      dao.observeGoogleAccountConfig(),
      dao.observePendingSyncQueue(),
      dao.observePendingDriveUploads(),
    ) { config, queue, driveQueue ->
      val totalPending = queue.size + driveQueue.size
      val base = config?.toDomain(totalPending) ?: GoogleAccountState(pendingSyncCount = totalPending)
      val computedSyncStatus =
        when {
          base.syncStatus == SyncStatus.SYNCING -> SyncStatus.SYNCING
          queue.any { it.syncStatus == SyncStatus.SYNC_FAILED.name } ||
            driveQueue.any { it.status == DriveUploadStatus.FAILED.name } -> SyncStatus.SYNC_FAILED
          totalPending > 0 -> SyncStatus.PENDING_SYNC
          else -> SyncStatus.SYNCED
        }
      base.copy(syncStatus = computedSyncStatus, pendingSyncCount = totalPending)
    }

  override suspend fun saveBusinessProfile(profile: BusinessProfile) {
    val existing = dao.getBusinessProfileOnce()
    val effectiveProfile =
      profile.copy(
        businessId = existing?.businessId?.takeIf { it.isNotBlank() } ?: profile.businessId.ifBlank { "BIZ-001" },
        createdAt = existing?.createdAt?.takeIf { it > 0L } ?: profile.createdAt,
        updatedAt = System.currentTimeMillis(),
        updatedBy = profile.updatedBy.ifBlank { profile.ownerName.ifBlank { "Owner" } },
      )
    dao.upsertBusinessProfile(BusinessProfileEntity.fromDomain(effectiveProfile))
  }

  override suspend fun saveMetalRate(rate: MetalRate) {
    dao.upsertMetalRate(MetalRateEntity.fromDomain(rate))
  }

  override suspend fun saveCustomer(customer: Customer) {
    val existingByMobile =
      if (customer.mobileNumber.isNotBlank()) {
        dao.findCustomerByMobile(customer.mobileNumber.trim())?.toDomain()
      } else null
    val effectiveCustomer =
      if (existingByMobile != null && existingByMobile.id != customer.id) {
        customer.copy(
          id = existingByMobile.id,
          createdAt = existingByMobile.createdAt,
          updatedAt = System.currentTimeMillis(),
        )
      } else {
        customer
      }
    dao.upsertCustomer(CustomerEntity.fromDomain(effectiveCustomer))
  }

  override suspend fun saveCustomersBatch(customers: List<Customer>) {
    dao.upsertCustomers(customers.map { CustomerEntity.fromDomain(it) })
  }

  override suspend fun findCustomerByMobileLocal(mobile: String): Customer? {
    val clean = mobile.trim().filter { it.isDigit() }
    if (clean.isEmpty()) return null
    val exact = dao.findCustomerByMobile(mobile.trim())?.toDomain()
    if (exact != null) return exact
    return dao
      .getCustomersOnce()
      .map { it.toDomain() }
      .find { it.mobileNumber.filter { ch -> ch.isDigit() }.endsWith(clean) }
  }

  override suspend fun deleteCustomer(customerId: String) {
    dao.deleteCustomerById(customerId)
  }

  override suspend fun saveTransaction(transaction: Transaction) {
    dao.upsertTransaction(TransactionEntity.fromDomain(transaction))
    if (
      transaction.status == TransactionStatus.COMPLETED ||
        transaction.status == TransactionStatus.PENDING_SYNC
    ) {
      inventoryService.createMovementFromTransaction(transaction)?.let { mov ->
        dao.upsertInventoryMovement(InventoryMovementEntity.fromDomain(mov))
      }
    }
  }

  override suspend fun saveInvoice(invoice: Invoice) {
    dao.upsertInvoice(InvoiceEntity.fromDomain(invoice))
  }

  override suspend fun getInvoiceByIdOrNumber(invoiceIdOrNumber: String): Invoice? {
    return dao.getInvoiceByIdOrNumber(invoiceIdOrNumber)?.toDomain()
  }

  override suspend fun saveTransactionAndInvoice(transaction: Transaction, invoice: Invoice) {
    dao.upsertTransaction(TransactionEntity.fromDomain(transaction))
    dao.upsertInvoice(InvoiceEntity.fromDomain(invoice))
    if (
      transaction.status == TransactionStatus.COMPLETED ||
        transaction.status == TransactionStatus.PENDING_SYNC
    ) {
      inventoryService.createMovementFromTransaction(transaction)?.let { mov ->
        dao.upsertInventoryMovement(InventoryMovementEntity.fromDomain(mov))
      }
    }
  }

  override suspend fun saveTransactionsBatch(transactions: List<Transaction>) {
    dao.upsertTransactions(transactions.map { TransactionEntity.fromDomain(it) })
  }

  override suspend fun saveInvoicesBatch(invoices: List<Invoice>) {
    dao.upsertInvoices(invoices.map { InvoiceEntity.fromDomain(it) })
  }

  override suspend fun updateTransactionSyncStatus(transactionId: String, syncStatus: SyncStatus) {
    dao.updateTransactionSyncStatus(transactionId, syncStatus.name)
  }

  override suspend fun cancelTransaction(
    transactionId: String,
    cancelledBy: String,
    cancellationReason: String,
    cancelledAt: Long,
  ): Transaction? {
    val existingEntity = dao.getTransactionById(transactionId) ?: return null
    val existingTx = existingEntity.toDomain()
    val cancelledTx =
      existingTx.copy(
        status = TransactionStatus.CANCELLED,
        cancelledAt = cancelledAt,
        cancelledBy = cancelledBy.ifBlank { "Owner" },
        cancellationReason = cancellationReason.ifBlank { "Cancelled by user" },
        updatedAt = cancelledAt,
      )
    dao.upsertTransaction(TransactionEntity.fromDomain(cancelledTx))

    val matchedInvoice =
      dao.getInvoicesOnce().find { it.transactionId == transactionId }?.toDomain()
    if (matchedInvoice != null) {
      val updatedInvoice =
        matchedInvoice.copy(
          status = InvoiceStatus.CANCELLED.name,
          invoiceStatus = InvoiceStatus.CANCELLED,
          notes =
            if (matchedInvoice.notes.isBlank()) {
              "CANCELLED: ${cancelledTx.cancellationReason}"
            } else if (!matchedInvoice.notes.contains("CANCELLED:")) {
              "${matchedInvoice.notes} | CANCELLED: ${cancelledTx.cancellationReason}"
            } else {
              matchedInvoice.notes
            },
          updatedAt = cancelledAt,
        )
      dao.upsertInvoice(InvoiceEntity.fromDomain(updatedInvoice))
    }

    // Stage 5: Create a REVERSAL inventory movement without deleting the original movement
    val existingMovs = dao.getInventoryMovementsOnce().map { it.toDomain() }
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(cancelledAt))
    val reversals =
      inventoryService.createReversalMovementsForCancelledTransaction(
        transactionId = transactionId,
        existingMovements = existingMovs,
        reversalDate = todayDate,
        reason = cancelledTx.cancellationReason,
      )
    if (reversals.isNotEmpty()) {
      dao.upsertInventoryMovements(reversals.map { InventoryMovementEntity.fromDomain(it) })
    }

    return cancelledTx
  }

  override suspend fun deleteTransaction(transactionId: String) {
    dao.deleteTransactionById(transactionId)
    dao.deleteInvoiceByTransactionId(transactionId)
  }

  override suspend fun deleteInvoice(invoiceNumber: String) {
    dao.deleteInvoiceByNumber(invoiceNumber)
  }

  override suspend fun getNextInvoiceNumber(): String {
    val profile = dao.getBusinessProfileOnce()?.toDomain() ?: BusinessProfile()
    val existingTxs = dao.getTransactionsOnce()
    val existingInvs = dao.getInvoicesOnce()
    val prefix = profile.invoicePrefix.ifBlank { "HGR-" }

    val allNumbers =
      (existingTxs.map { it.invoiceNumber } + existingInvs.map { it.invoiceNumber }).filter {
        it.isNotBlank() && !it.startsWith("DRAFT-")
      }

    val maxExistingSeq =
      allNumbers.mapNotNull { inv ->
        if (inv.startsWith(prefix)) {
          inv.removePrefix(prefix).filter { it.isDigit() }.toIntOrNull()
        } else {
          inv.filter { it.isDigit() }.toIntOrNull()
        }
      }.maxOrNull() ?: (profile.invoiceStartingNumber - 1)

    val nextNum = maxOf(profile.invoiceStartingNumber, maxExistingSeq + 1)
    return String.format(Locale.US, "%s%06d", prefix, nextNum)
  }

  // ==================== STAGE 5 STORAGE OPERATIONS ====================

  override suspend fun getOpeningStockOnce(metal: MetalType): OpeningStockBalance {
    return dao.getOpeningStockByMetal(metal.name)?.toDomain()
      ?: OpeningStockBalance(
        metal = metal,
        grossWeight = if (metal == MetalType.GOLD) BigDecimal("100.000") else BigDecimal("2500.000"),
        tunch = BigDecimal("99.00"),
        fineWeight = if (metal == MetalType.GOLD) BigDecimal("99.000") else BigDecimal("2475.000"),
        referenceRatePerGram = if (metal == MetalType.GOLD) BigDecimal("7450.00") else BigDecimal("92.50"),
        notes = "Initial Opening Stock",
      )
  }

  override suspend fun saveOpeningStock(balance: OpeningStockBalance) {
    dao.upsertOpeningStock(OpeningStockEntity.fromDomain(balance))
  }

  override suspend fun saveOpeningStocksBatch(stocks: List<OpeningStockBalance>) {
    dao.upsertOpeningStocks(stocks.map { OpeningStockEntity.fromDomain(it) })
  }

  override suspend fun getInventoryMovementsOnce(): List<InventoryMovement> {
    return dao.getInventoryMovementsOnce().map { it.toDomain() }
  }

  override suspend fun saveInventoryMovement(movement: InventoryMovement) {
    dao.upsertInventoryMovement(InventoryMovementEntity.fromDomain(movement))
  }

  override suspend fun saveInventoryMovementsBatch(movements: List<InventoryMovement>) {
    dao.upsertInventoryMovements(movements.map { InventoryMovementEntity.fromDomain(it) })
  }

  override suspend fun saveInventoryReconciliation(record: InventoryReconciliationRecord) {
    dao.upsertInventoryReconciliation(InventoryReconciliationEntity.fromDomain(record))
  }

  override suspend fun saveVendor(vendor: Vendor) {
    dao.upsertVendor(VendorEntity.fromDomain(vendor))
  }

  override suspend fun saveVendorsBatch(vendors: List<Vendor>) {
    dao.upsertVendors(vendors.map { VendorEntity.fromDomain(it) })
  }

  override suspend fun deleteVendor(vendorId: String) {
    dao.deleteVendorById(vendorId)
  }

  override suspend fun savePurchase(purchase: PurchaseRecord, movement: InventoryMovement?) {
    dao.upsertPurchase(PurchaseEntity.fromDomain(purchase))
    val mov = movement ?: inventoryService.createMovementFromPurchase(purchase)
    dao.upsertInventoryMovement(InventoryMovementEntity.fromDomain(mov))
  }

  override suspend fun savePurchasesBatch(purchases: List<PurchaseRecord>) {
    dao.upsertPurchases(purchases.map { PurchaseEntity.fromDomain(it) })
  }

  override suspend fun cancelPurchase(purchaseId: String, reason: String): PurchaseRecord? {
    val existing = dao.getPurchaseById(purchaseId)?.toDomain() ?: return null
    val cancelled = existing.copy(status = TransactionStatus.CANCELLED, notes = "${existing.notes} | CANCELLED: $reason".trim())
    dao.upsertPurchase(PurchaseEntity.fromDomain(cancelled))
    val movs = dao.getInventoryMovementsOnce().map { it.toDomain() }
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val reversals =
      inventoryService.createReversalMovementsForCancelledTransaction(
        transactionId = purchaseId,
        existingMovements = movs,
        reversalDate = todayDate,
        reason = reason,
      )
    if (reversals.isNotEmpty()) {
      dao.upsertInventoryMovements(reversals.map { InventoryMovementEntity.fromDomain(it) })
    }
    return cancelled
  }

  override suspend fun saveSale(sale: SaleRecord, movement: InventoryMovement?) {
    dao.upsertSale(SaleEntity.fromDomain(sale))
    val mov = movement ?: inventoryService.createMovementFromSale(sale)
    dao.upsertInventoryMovement(InventoryMovementEntity.fromDomain(mov))
  }

  override suspend fun saveSalesBatch(sales: List<SaleRecord>) {
    dao.upsertSales(sales.map { SaleEntity.fromDomain(it) })
  }

  override suspend fun cancelSale(saleId: String, reason: String): SaleRecord? {
    val existing = dao.getSaleById(saleId)?.toDomain() ?: return null
    val cancelled = existing.copy(status = TransactionStatus.CANCELLED, notes = "${existing.notes} | CANCELLED: $reason".trim())
    dao.upsertSale(SaleEntity.fromDomain(cancelled))
    val movs = dao.getInventoryMovementsOnce().map { it.toDomain() }
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    val reversals =
      inventoryService.createReversalMovementsForCancelledTransaction(
        transactionId = saleId,
        existingMovements = movs,
        reversalDate = todayDate,
        reason = reason,
      )
    if (reversals.isNotEmpty()) {
      dao.upsertInventoryMovements(reversals.map { InventoryMovementEntity.fromDomain(it) })
    }
    return cancelled
  }

  override suspend fun saveScrapProcessing(record: ScrapProcessingRecord) {
    dao.upsertScrapProcessing(ScrapProcessingEntity.fromDomain(record))
  }

  // ==================== STAGE 6 STORAGE OPERATIONS ====================

  override suspend fun saveSecuritySettings(settings: SecuritySettings) {
    dao.upsertSecuritySettings(SecuritySettingsEntity.fromDomain(settings))
  }

  override suspend fun saveAuditLog(entry: AuditLogEntry) {
    dao.upsertAuditLog(AuditLogEntity.fromDomain(entry))
  }

  override suspend fun saveReminder(reminder: ShopReminder) {
    dao.upsertReminder(ReminderEntity.fromDomain(reminder))
  }

  override suspend fun deleteReminder(reminderId: String) {
    dao.deleteReminderById(reminderId)
  }

  override suspend fun exportFullBackupJson(): String {
    val profile = dao.getBusinessProfileOnce()?.toDomain() ?: BusinessProfile()
    val rate = dao.getMetalRateOnce()?.toDomain() ?: MetalRate()
    val customers = dao.getCustomersOnce().map { it.toDomain() }
    val vendors = dao.getVendorsOnce().map { it.toDomain() }
    val txs = dao.getTransactionsOnce().map { it.toDomain() }
    val invoices = dao.getInvoicesOnce().map { it.toDomain() }
    val movements = dao.getInventoryMovementsOnce().map { it.toDomain() }
    val purchases = dao.getPurchasesOnce().map { it.toDomain() }
    val sales = dao.getSalesOnce().map { it.toDomain() }

    return JSONObject().apply {
      put("backupVersion", 7)
      put("exportedAt", System.currentTimeMillis())
      put("shopName", profile.shopName)
      put("ownerName", profile.ownerName)
      put("businessProfile", JSONObject().apply {
        put("businessId", profile.businessId)
        put("shopName", profile.shopName)
        put("ownerName", profile.ownerName)
        put("mobileNumber", profile.mobileNumber)
        put("whatsappNumber", profile.whatsappNumber)
        put("email", profile.email)
        put("address", profile.address)
        put("city", profile.city)
        put("district", profile.district)
        put("state", profile.state)
        put("pinCode", profile.pinCode)
        put("panNumber", profile.panNumber)
        put("gstNumber", profile.gstNumber)
        put("bankName", profile.bankName)
        put("branchName", profile.branchName)
        put("bankAccountNumber", profile.bankAccountNumber)
        put("ifsc", profile.ifsc)
        put("upiId", profile.upiId)
        put("invoicePrefix", profile.invoicePrefix)
        put("invoiceStartingNumber", profile.invoiceStartingNumber)
        put("businessLogoUri", profile.businessLogoUri)
        put("invoiceFooter", profile.invoiceFooter)
        put("termsAndConditions", profile.termsAndConditions)
        put("gstEnabled", profile.gstEnabled)
        put("cgstRatePercent", profile.cgstRatePercent.toPlainString())
        put("sgstRatePercent", profile.sgstRatePercent.toPlainString())
        put("igstRatePercent", profile.igstRatePercent.toPlainString())
        put("gstRegistrationType", profile.gstRegistrationType)
        put("gstStateName", profile.gstStateName)
        put("gstTaxTreatment", profile.gstTaxTreatment)
        put("createdAt", profile.createdAt)
        put("updatedAt", profile.updatedAt)
        put("updatedBy", profile.updatedBy)
        put("status", profile.status)
      })
      put("goldRate", rate.goldRate.toPlainString())
      put("silverRate", rate.silverRate.toPlainString())
      put("customerCount", customers.size)
      put("vendorCount", vendors.size)
      put("transactionCount", txs.size)
      put("invoiceCount", invoices.size)
      put("movementCount", movements.size)
      put("purchaseCount", purchases.size)
      put("saleCount", sales.size)

      val custArr = JSONArray()
      customers.forEach { c ->
        custArr.put(
          JSONObject().apply {
            put("id", c.id)
            put("name", c.name)
            put("mobileNumber", c.mobileNumber)
            put("whatsappNumber", c.whatsappNumber)
            put("address", c.address)
            put("city", c.city)
            put("state", c.state)
            put("pinCode", c.pinCode)
            put("panNumber", c.panNumber)
            put("gstNumber", c.gstNumber)
            put("email", c.email)
            put("pendingAmount", c.pendingAmount.toPlainString())
            put("notes", c.notes)
          }
        )
      }
      put("customers", custArr)

      val vendorArr = JSONArray()
      vendors.forEach { v ->
        vendorArr.put(
          JSONObject().apply {
            put("vendorId", v.vendorId)
            put("name", v.name)
            put("mobileNumber", v.mobile)
            put("companyName", v.companyName)
            put("address", v.address)
            put("panNumber", v.panNumber)
            put("gstNumber", v.gstNumber)
            put("pendingPayableAmount", v.pendingMoney.toPlainString())
            put("notes", v.notes)
          }
        )
      }
      put("vendors", vendorArr)
    }.toString(2)
  }

  override suspend fun importFullBackupJson(json: String): Result<Int> {
    return try {
      val root = JSONObject(json)
      var restoredCount = 0
      val bpObj = root.optJSONObject("businessProfile")
      if (bpObj != null) {
        val existingBp = dao.getBusinessProfileOnce()?.toDomain() ?: BusinessProfile()
        val restoredBp =
          existingBp.copy(
            businessId = bpObj.optString("businessId", existingBp.businessId),
            shopName = bpObj.optString("shopName", existingBp.shopName),
            ownerName = bpObj.optString("ownerName", existingBp.ownerName),
            mobileNumber = bpObj.optString("mobileNumber", existingBp.mobileNumber),
            whatsappNumber = bpObj.optString("whatsappNumber", existingBp.whatsappNumber),
            email = bpObj.optString("email", existingBp.email),
            address = bpObj.optString("address", existingBp.address),
            city = bpObj.optString("city", existingBp.city),
            district = bpObj.optString("district", existingBp.district),
            state = bpObj.optString("state", existingBp.state),
            pinCode = bpObj.optString("pinCode", existingBp.pinCode),
            panNumber = bpObj.optString("panNumber", existingBp.panNumber),
            gstNumber = bpObj.optString("gstNumber", existingBp.gstNumber),
            bankName = bpObj.optString("bankName", existingBp.bankName),
            branchName = bpObj.optString("branchName", existingBp.branchName),
            bankAccountNumber = bpObj.optString("bankAccountNumber", existingBp.bankAccountNumber),
            ifsc = bpObj.optString("ifsc", existingBp.ifsc),
            upiId = bpObj.optString("upiId", existingBp.upiId),
            invoicePrefix = bpObj.optString("invoicePrefix", existingBp.invoicePrefix),
            invoiceStartingNumber = bpObj.optInt("invoiceStartingNumber", existingBp.invoiceStartingNumber),
            businessLogoUri = bpObj.optString("businessLogoUri", existingBp.businessLogoUri),
            invoiceFooter = bpObj.optString("invoiceFooter", existingBp.invoiceFooter),
            termsAndConditions = bpObj.optString("termsAndConditions", existingBp.termsAndConditions),
            gstEnabled = bpObj.optBoolean("gstEnabled", existingBp.gstEnabled),
            cgstRatePercent = bpObj.optString("cgstRatePercent", existingBp.cgstRatePercent.toPlainString()).toBigDecimalOrNull() ?: existingBp.cgstRatePercent,
            sgstRatePercent = bpObj.optString("sgstRatePercent", existingBp.sgstRatePercent.toPlainString()).toBigDecimalOrNull() ?: existingBp.sgstRatePercent,
            igstRatePercent = bpObj.optString("igstRatePercent", existingBp.igstRatePercent.toPlainString()).toBigDecimalOrNull() ?: existingBp.igstRatePercent,
            gstRegistrationType = bpObj.optString("gstRegistrationType", existingBp.gstRegistrationType),
            gstStateName = bpObj.optString("gstStateName", existingBp.gstStateName),
            gstTaxTreatment = bpObj.optString("gstTaxTreatment", existingBp.gstTaxTreatment),
            createdAt = bpObj.optLong("createdAt", existingBp.createdAt),
            updatedAt = bpObj.optLong("updatedAt", System.currentTimeMillis()),
            updatedBy = bpObj.optString("updatedBy", existingBp.updatedBy),
            status = bpObj.optString("status", existingBp.status),
          )
        dao.upsertBusinessProfile(BusinessProfileEntity.fromDomain(restoredBp))
        restoredCount++
      }
      val custArr = root.optJSONArray("customers")
      if (custArr != null) {
        for (i in 0 until custArr.length()) {
          val obj = custArr.optJSONObject(i) ?: continue
          val c =
            Customer(
              id = obj.optString("id", "CUST-${i + 1}"),
              name = obj.optString("name", "Customer"),
              mobileNumber = obj.optString("mobileNumber", ""),
              whatsappNumber = obj.optString("whatsappNumber", obj.optString("mobileNumber", "")),
              address = obj.optString("address", ""),
              city = obj.optString("city", ""),
              state = obj.optString("state", ""),
              pinCode = obj.optString("pinCode", ""),
              panNumber = obj.optString("panNumber", ""),
              gstNumber = obj.optString("gstNumber", ""),
              email = obj.optString("email", ""),
              pendingAmount =
                obj.optString("pendingAmount", "0").toBigDecimalOrNull() ?: BigDecimal.ZERO,
              notes = obj.optString("notes", ""),
            )
          dao.upsertCustomer(CustomerEntity.fromDomain(c))
          restoredCount++
        }
      }
      val vendorArr = root.optJSONArray("vendors")
      if (vendorArr != null) {
        for (i in 0 until vendorArr.length()) {
          val obj = vendorArr.optJSONObject(i) ?: continue
          val v =
            Vendor(
              vendorId = obj.optString("vendorId", "VEND-${i + 1}"),
              name = obj.optString("name", "Vendor"),
              mobile = obj.optString("mobileNumber", ""),
              companyName = obj.optString("companyName", ""),
              address = obj.optString("address", ""),
              panNumber = obj.optString("panNumber", ""),
              gstNumber = obj.optString("gstNumber", ""),
              pendingMoney =
                obj.optString("pendingPayableAmount", "0").toBigDecimalOrNull() ?: BigDecimal.ZERO,
              notes = obj.optString("notes", ""),
            )
          dao.upsertVendor(VendorEntity.fromDomain(v))
          restoredCount++
        }
      }
      val (repaired, _) = verifyDataIntegrity()
      Result.success(restoredCount + repaired)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  /**
   * Stage 6 Data Recovery & Integrity Verification:
   * - Rebuilds any missing Invoice records for completed transactions (preserving permanent InvoiceNumber)
   * - Rebuilds any missing InventoryMovement records for completed transactions, purchases, and sales
   * - Ensures OpeningStock exists for both GOLD and SILVER
   */
  override suspend fun verifyDataIntegrity(): Pair<Int, List<String>> {
    val issues = mutableListOf<String>()
    val profile = dao.getBusinessProfileOnce()?.toDomain() ?: BusinessProfile()
    val customersMap = dao.getCustomersOnce().map { it.toDomain() }.associateBy { it.id }
    val allTxs = dao.getTransactionsOnce().map { it.toDomain() }
    val existingInvoices = dao.getInvoicesOnce().map { it.toDomain() }
    val existingMovements = dao.getInventoryMovementsOnce().map { it.toDomain() }

    val invoiceTxIds = existingInvoices.map { it.transactionId }.toSet()
    val movementTxIds = existingMovements.map { it.transactionId }.toSet()

    // 1. Ensure Opening Stock exists for Gold and Silver
    if (dao.getOpeningStockByMetal(MetalType.GOLD.name) == null) {
      dao.upsertOpeningStock(OpeningStockEntity.fromDomain(getOpeningStockOnce(MetalType.GOLD)))
      issues.add("Restored default Gold opening stock")
    }
    if (dao.getOpeningStockByMetal(MetalType.SILVER.name) == null) {
      dao.upsertOpeningStock(OpeningStockEntity.fromDomain(getOpeningStockOnce(MetalType.SILVER)))
      issues.add("Restored default Silver opening stock")
    }

    // 2. Rebuild missing invoices & missing inventory movements for completed transactions
    allTxs.forEach { tx ->
      if (tx.status != TransactionStatus.DRAFT) {
        if (tx.transactionId !in invoiceTxIds) {
          val rebuiltInvoice =
            invoiceService.createInvoiceFromTransaction(
              transaction = tx,
              customer = customersMap[tx.customerId],
              businessProfile = profile,
            )
          dao.upsertInvoice(InvoiceEntity.fromDomain(rebuiltInvoice))
          issues.add("Rebuilt missing invoice ${rebuiltInvoice.invoiceNumber} for ${tx.transactionId}")
        }
        if (tx.status != TransactionStatus.CANCELLED && tx.transactionId !in movementTxIds) {
          inventoryService.createMovementFromTransaction(tx)?.let { mov ->
            dao.upsertInventoryMovement(InventoryMovementEntity.fromDomain(mov))
            issues.add("Rebuilt missing inventory movement for ${tx.transactionId}")
          }
        }
      }
    }

    return issues.size to issues
  }

  // ==================== SYNC QUEUE & DRIVE QUEUE ====================

  override suspend fun enqueueSyncRecord(record: PendingSyncRecord) {
    dao.upsertPendingSyncRecord(PendingSyncQueueEntity.fromDomain(record))
  }

  override suspend fun getPendingSyncRecords(): List<PendingSyncRecord> {
    return dao.getPendingSyncQueueOnce().map { it.toDomain() }
  }

  override suspend fun markSyncRecordCompleted(recordId: String) {
    dao.deletePendingSyncRecord(recordId)
  }

  override suspend fun markSyncRecordFailed(recordId: String, errorMessage: String) {
    dao.markPendingSyncFailed(recordId, SyncStatus.SYNC_FAILED.name, errorMessage)
  }

  override suspend fun enqueueDriveUpload(upload: PendingDriveUpload) {
    dao.upsertPendingDriveUpload(PendingDriveUploadEntity.fromDomain(upload))
  }

  override suspend fun getPendingDriveUploads(): List<PendingDriveUpload> {
    return dao.getPendingDriveUploadsOnce().map { it.toDomain() }
  }

  override suspend fun markDriveUploadCompleted(
    uploadId: String,
    driveFileId: String,
    driveFileName: String,
    driveFileUrl: String,
  ) {
    val pending = dao.getPendingDriveUploadById(uploadId)?.toDomain()
    dao.deletePendingDriveUpload(uploadId)
    val now = System.currentTimeMillis()
    val invNumberOrId = pending?.invoiceNumber?.ifBlank { pending.invoiceId } ?: pending?.invoiceId
    if (!invNumberOrId.isNullOrBlank()) {
      val existingInv = dao.getInvoiceByIdOrNumber(invNumberOrId)?.toDomain()
      if (existingInv != null) {
        val updatedStatus =
          if (existingInv.isCancelled) InvoiceStatus.CANCELLED else InvoiceStatus.DRIVE_SAVED
        val updatedInv =
          existingInv.copy(
            driveFileId = driveFileId,
            driveFileName = driveFileName,
            driveFileUrl = driveFileUrl,
            driveSavedAt = now,
            status = updatedStatus.name,
            invoiceStatus = updatedStatus,
            updatedAt = now,
          )
        dao.upsertInvoice(InvoiceEntity.fromDomain(updatedInv))
      }
    }
  }

  override suspend fun markDriveUploadFailed(uploadId: String, errorMessage: String) {
    dao.markPendingDriveUploadFailed(uploadId, DriveUploadStatus.FAILED.name, errorMessage)
  }

  override suspend fun getGoogleAccountStateOnce(): GoogleAccountState {
    val queueCount = dao.getPendingSyncQueueOnce().size + dao.getPendingDriveUploadsOnce().size
    return dao.getGoogleAccountConfigOnce()?.toDomain(queueCount)
      ?: GoogleAccountState(pendingSyncCount = queueCount)
  }

  override suspend fun saveGoogleAccountState(state: GoogleAccountState) {
    dao.upsertGoogleAccountConfig(GoogleAccountConfigEntity.fromDomain(state))
  }

  override suspend fun seedInitialDataIfNeeded() {
    val existingProfile = dao.getBusinessProfileOnce()
    if (existingProfile == null) {
      val defaultProfile = BusinessProfile()
      dao.upsertBusinessProfile(BusinessProfileEntity.fromDomain(defaultProfile))
    }
    val existingRate = dao.getMetalRateOnce()
    if (existingRate == null) {
      val defaultRate =
        MetalRate(
          rateId = "RATE-1001",
          goldRate = BigDecimal("7450.00"),
          goldRateUnit = RateUnit.PER_GRAM,
          silverRate = BigDecimal("92.50"),
          silverRateUnit = RateUnit.PER_GRAM,
          updatedAt = System.currentTimeMillis(),
        )
      dao.upsertMetalRate(MetalRateEntity.fromDomain(defaultRate))
    }
    if (dao.getSecuritySettingsOnce() == null) {
      dao.upsertSecuritySettings(SecuritySettingsEntity.fromDomain(SecuritySettings()))
    }
    if (dao.getCustomerCount() == 0 && dao.getTransactionCount() == 0) {
      populateSampleData()
    } else if (dao.getOpeningStocksOnce().isEmpty()) {
      seedStage5And6Defaults(System.currentTimeMillis())
    }
  }

  override suspend fun resetDemoData() {
    dao.clearAllTransactions()
    dao.clearAllInvoices()
    dao.clearAllCustomers()
    dao.clearAllInventoryMovements()
    dao.clearAllVendors()
    dao.clearAllPurchases()
    dao.clearAllSales()
    dao.clearAllPendingSync()
    dao.clearAllPendingDriveUploads()
    val defaultProfile = BusinessProfile()
    dao.upsertBusinessProfile(BusinessProfileEntity.fromDomain(defaultProfile))
    val defaultRate =
      MetalRate(
        rateId = "RATE-1001",
        goldRate = BigDecimal("7450.00"),
        goldRateUnit = RateUnit.PER_GRAM,
        silverRate = BigDecimal("92.50"),
        silverRateUnit = RateUnit.PER_GRAM,
        updatedAt = System.currentTimeMillis(),
      )
    dao.upsertMetalRate(MetalRateEntity.fromDomain(defaultRate))
    populateSampleData()
  }

  private suspend fun seedStage5And6Defaults(now: Long) {
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
    val yesterdayDate =
      SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now - 24L * 60 * 60 * 1000))

    // 1. Opening Stock for Gold (100g gross / 99g fine) & Silver (2500g gross / 2475g fine)
    val openingGold =
      OpeningStockBalance(
        metal = MetalType.GOLD,
        grossWeight = BigDecimal("100.000"),
        tunch = BigDecimal("99.00"),
        fineWeight = BigDecimal("99.000"),
        referenceRatePerGram = BigDecimal("7450.00"),
        date = yesterdayDate,
        notes = "Opening Gold Bullion Stock",
        updatedAt = now - 86400000L * 5,
      )
    val openingSilver =
      OpeningStockBalance(
        metal = MetalType.SILVER,
        grossWeight = BigDecimal("2500.000"),
        tunch = BigDecimal("99.00"),
        fineWeight = BigDecimal("2475.000"),
        referenceRatePerGram = BigDecimal("92.50"),
        date = yesterdayDate,
        notes = "Opening Silver Bullion Stock",
        updatedAt = now - 86400000L * 5,
      )
    dao.upsertOpeningStock(OpeningStockEntity.fromDomain(openingGold))
    dao.upsertOpeningStock(OpeningStockEntity.fromDomain(openingSilver))

    // 2. Sample Vendors
    val sampleVendors =
      listOf(
        Vendor(
          vendorId = "VEND-201",
          name = "Zaveri Bullion Refinery Pvt Ltd",
          mobile = "9821009988",
          companyName = "Zaveri Bullion Refinery Pvt Ltd",
          address = "Mumbadevi Bullion Exchange, Mumbai",
          panNumber = "AAACZ1234B",
          gstNumber = "27AAACZ1234B1Z5",
          pendingMoney = BigDecimal("45000.00"),
          pendingGoldFineGrams = BigDecimal.ZERO,
          pendingSilverFineGrams = BigDecimal.ZERO,
          notes = "99.50% & 99.90% Standard Gold/Silver Bar Supplier",
          createdAt = now - 86400000L * 5,
        ),
        Vendor(
          vendorId = "VEND-202",
          name = "Shree Chamunda Silver House",
          mobile = "9867554433",
          companyName = "Shree Chamunda Silver House",
          address = "Rajkot / Zaveri Bazaar Branch",
          panNumber = "ABFCS5566P",
          gstNumber = "27ABFCS5566P1Z2",
          pendingMoney = BigDecimal.ZERO,
          notes = "Wholesale Silver Payal, Utensils & Fine Chorsa",
          createdAt = now - 86400000L * 4,
        ),
      )
    dao.upsertVendors(sampleVendors.map { VendorEntity.fromDomain(it) })

    // 3. Sample Bullion Purchase
    val samplePurchase =
      PurchaseRecord(
        purchaseId = "PUR-1001",
        date = yesterdayDate,
        time = "11:00",
        timestamp = now - 86400000L,
        vendorId = "VEND-201",
        vendorName = "Zaveri Bullion Refinery Pvt Ltd",
        vendorMobile = "9821009988",
        metal = MetalType.GOLD,
        description = "24K Gold Bullion Bar (50g)",
        grossWeight = BigDecimal("50.000"),
        tunch = BigDecimal("99.50"),
        fineWeight = BigDecimal("49.750"),
        rate = BigDecimal("7400.00"),
        rateUnit = RateUnit.PER_GRAM,
        totalAmount = BigDecimal("368150.00"),
        amountPaid = BigDecimal("323150.00"),
        balancePending = BigDecimal("45000.00"),
        paymentMode = PaymentMode.BANK_TRANSFER,
        notes = "Bullion stock replenishment",
      )
    dao.upsertPurchase(PurchaseEntity.fromDomain(samplePurchase))
    dao.upsertInventoryMovement(
      InventoryMovementEntity.fromDomain(
        InventoryMovement(
          movementId = "MOV-PUR-1001",
          date = samplePurchase.date,
          transactionId = samplePurchase.purchaseId,
          invoiceNumber = samplePurchase.purchaseId,
          movementType = InventoryMovementType.PURCHASE,
          metal = samplePurchase.metal,
          grossWeight = samplePurchase.grossWeight,
          tunch = samplePurchase.tunch,
          fineWeight = samplePurchase.fineWeight,
          rate = samplePurchase.rate,
          value = samplePurchase.totalAmount,
          direction = InventoryMovementDirection.IN,
          notes = "Purchase from ${samplePurchase.vendorName} (${samplePurchase.purchaseId})",
          createdAt = samplePurchase.createdAt,
        )
      )
    )

    // 4. Sample Reminders for Customer Bakaya (Stage 6)
    val reminder1 =
      ShopReminder(
        reminderId = "REM-101",
        customerId = "CUST-101",
        customerName = "Ananya Deshmukh",
        customerMobile = "9820112233",
        reminderType = ReminderType.PENDING_PAYMENT,
        dueDate = todayDate,
        pendingAmount = BigDecimal("18500.00"),
        notes = "Pending Bakaya follow-up for Bridal Gold purchase",
        isCompleted = false,
        createdAt = now - 3600000L * 6,
      )
    val reminder2 =
      ShopReminder(
        reminderId = "REM-102",
        customerId = "CUST-104",
        customerName = "SureshPatel & Sons",
        customerMobile = "9920334455",
        reminderType = ReminderType.PENDING_PAYMENT,
        dueDate = todayDate,
        pendingAmount = BigDecimal("12000.00"),
        notes = "Wholesale account Bakaya settlement",
        isCompleted = false,
        createdAt = now - 3600000L * 4,
      )
    dao.upsertReminder(ReminderEntity.fromDomain(reminder1))
    dao.upsertReminder(ReminderEntity.fromDomain(reminder2))

    // 5. Initial Audit Entry
    dao.upsertAuditLog(
      AuditLogEntity.fromDomain(
        AuditLogEntry(
          actionId = "AUD-1001",
          timestamp = now - 3600000L * 5,
          date = todayDate,
          time = "09:00",
          actionType = AuditActionType.INVENTORY_ADJUSTED,
          entityId = "STOCK-INIT",
          description = "Initialized Gold (99.000g fine) and Silver (2475.000g fine) Opening Stock",
          performedBy = "Rajeshwar Soni",
        )
      )
    )
  }

  private suspend fun populateSampleData() {
    val profile = dao.getBusinessProfileOnce()?.toDomain() ?: BusinessProfile()
    val prefix = profile.invoicePrefix.ifBlank { "HGR-" }
    val now = System.currentTimeMillis()
    val todayDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
    val yesterdayDate =
      SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now - 24L * 60 * 60 * 1000))

    seedStage5And6Defaults(now)

    val customers =
      listOf(
        Customer(
          id = "CUST-101",
          name = "Ananya Deshmukh",
          mobileNumber = "9820112233",
          address = "14 Malabar Hill, Mumbai",
          panNumber = "BKMPD4421L",
          gstNumber = "27BKMPD4421L1Z9",
          pendingAmount = BigDecimal("18500.00"),
          notes = "Regular 24K gold bullion & Bridal jewellery buyer",
          createdAt = now - 86400000L * 5,
        ),
        Customer(
          id = "CUST-102",
          name = "Vikramaditya Shah",
          mobileNumber = "9819445566",
          address = "88 Kalbadevi Road, Mumbai",
          panNumber = "AHJPS9920K",
          gstNumber = "",
          pendingAmount = BigDecimal("6200.00"),
          notes = "Prefers 99% pure gold settlement",
          createdAt = now - 86400000L * 4,
        ),
        Customer(
          id = "CUST-103",
          name = "Meenakshi Kothari",
          mobileNumber = "9769887744",
          address = "7B Ghatkopar East, Mumbai",
          panNumber = "CLMPK8812R",
          gstNumber = "27CLMPK8812R1Z2",
          pendingAmount = BigDecimal.ZERO,
          notes = "Silver utensils and coins trader",
          createdAt = now - 86400000L * 3,
        ),
        Customer(
          id = "CUST-104",
          name = "SureshPatel & Sons",
          mobileNumber = "9920334455",
          address = "Shop 12, Borivali West, Mumbai",
          panNumber = "AAFPS1122M",
          gstNumber = "27AAFPS1122M1Z8",
          pendingAmount = BigDecimal("12000.00"),
          notes = "Old gold & scrap silver wholesale account",
          createdAt = now - 86400000L * 2,
        ),
      )

    dao.upsertCustomers(customers.map { CustomerEntity.fromDomain(it) })

    val sampleTransactions =
      listOf(
        Transaction(
          transactionId = "TXN-1001",
          date = todayDate,
          time = "10:30",
          timestamp = now - 3600_000L * 4,
          customerId = "CUST-101",
          customerName = "Ananya Deshmukh",
          customerMobile = "9820112233",
          transactionType = TransactionType.MONEY_TO_GOLD,
          metalType = MetalType.GOLD,
          purityMode = PuritySelectionMode.CUSTOM_TUNCH,
          grossWeight = BigDecimal("10.000"),
          tunch = BigDecimal("100.00"),
          fineWeight = BigDecimal("10.000"),
          rate = BigDecimal("7450.00"),
          rateUnit = RateUnit.PER_GRAM,
          amount = BigDecimal("74500.00"),
          paymentMode = PaymentMode.UPI,
          notes = "Money converted to 10.000g Gold @ ₹7,450/g",
          invoiceNumber = "${prefix}000001",
        ),
        Transaction(
          transactionId = "TXN-1002",
          date = todayDate,
          time = "11:45",
          timestamp = now - 3600_000L * 3,
          customerId = "CUST-102",
          customerName = "Vikramaditya Shah",
          customerMobile = "9819445566",
          transactionType = TransactionType.GOLD_PAYMENT,
          metalType = MetalType.GOLD,
          purityMode = PuritySelectionMode.PURE_99,
          grossWeight = BigDecimal("20.000"),
          tunch = BigDecimal("99.00"),
          fineWeight = BigDecimal("19.800"),
          rate = BigDecimal("7450.00"),
          rateUnit = RateUnit.PER_GRAM,
          amount = BigDecimal("147510.00"),
          paymentMode = PaymentMode.CASH,
          notes = "99% Pure Gold bar received (20g gross = 19.800g fine)",
          invoiceNumber = "${prefix}000002",
        ),
        Transaction(
          transactionId = "TXN-1003",
          date = todayDate,
          time = "13:15",
          timestamp = now - 3600_000L * 2,
          customerId = "CUST-104",
          customerName = "SureshPatel & Sons",
          customerMobile = "9920334455",
          transactionType = TransactionType.SCRAP_GOLD,
          metalType = MetalType.GOLD,
          purityMode = PuritySelectionMode.CUSTOM_TUNCH,
          grossWeight = BigDecimal("10.000"),
          tunch = BigDecimal("75.00"),
          fineWeight = BigDecimal("7.500"),
          rate = BigDecimal("7450.00"),
          rateUnit = RateUnit.PER_GRAM,
          amount = BigDecimal("55875.00"),
          paymentMode = PaymentMode.BANK_TRANSFER,
          notes = "Old ornament scrap gold: 10g × 75 ÷ 100 = 7.500g fine",
          invoiceNumber = "${prefix}000003",
        ),
        Transaction(
          transactionId = "TXN-1004",
          date = todayDate,
          time = "14:50",
          timestamp = now - 3600_000L * 1,
          customerId = "CUST-103",
          customerName = "Meenakshi Kothari",
          customerMobile = "9769887744",
          transactionType = TransactionType.MONEY_TO_SILVER,
          metalType = MetalType.SILVER,
          purityMode = PuritySelectionMode.CUSTOM_TUNCH,
          grossWeight = BigDecimal("200.000"),
          tunch = BigDecimal("100.00"),
          fineWeight = BigDecimal("200.000"),
          rate = BigDecimal("92.50"),
          rateUnit = RateUnit.PER_GRAM,
          amount = BigDecimal("18500.00"),
          paymentMode = PaymentMode.CASH,
          notes = "Money to Silver booking 200g @ ₹92.50/g",
          invoiceNumber = "${prefix}000004",
        ),
        Transaction(
          transactionId = "TXN-1005",
          date = todayDate,
          time = "16:10",
          timestamp = now - 1800_000L,
          customerId = "CUST-104",
          customerName = "SureshPatel & Sons",
          customerMobile = "9920334455",
          transactionType = TransactionType.SCRAP_SILVER,
          metalType = MetalType.SILVER,
          purityMode = PuritySelectionMode.CUSTOM_TUNCH,
          grossWeight = BigDecimal("150.000"),
          tunch = BigDecimal("80.00"),
          fineWeight = BigDecimal("120.000"),
          rate = BigDecimal("92.50"),
          rateUnit = RateUnit.PER_GRAM,
          amount = BigDecimal("11100.00"),
          paymentMode = PaymentMode.CASH,
          notes = "Scrap silver anklets: 150g × 80 ÷ 100 = 120.000g fine",
          invoiceNumber = "${prefix}000005",
        ),
        Transaction(
          transactionId = "TXN-1000",
          date = yesterdayDate,
          time = "17:20",
          timestamp = now - 86400_000L,
          customerId = "CUST-103",
          customerName = "Meenakshi Kothari",
          customerMobile = "9769887744",
          transactionType = TransactionType.SILVER_PAYMENT,
          metalType = MetalType.SILVER,
          purityMode = PuritySelectionMode.PURE_99,
          grossWeight = BigDecimal("500.000"),
          tunch = BigDecimal("99.00"),
          fineWeight = BigDecimal("495.000"),
          rate = BigDecimal("92.50"),
          rateUnit = RateUnit.PER_GRAM,
          amount = BigDecimal("45787.50"),
          paymentMode = PaymentMode.UPI,
          notes = "99% Pure Silver bar received",
          invoiceNumber = "${prefix}000000",
        ),
      )

    val customerMap = customers.associateBy { it.id }
    val invoices =
      sampleTransactions.map { tx ->
        invoiceService.createInvoiceFromTransaction(tx, customerMap[tx.customerId], profile)
      }
    val movements =
      sampleTransactions.mapNotNull { tx -> inventoryService.createMovementFromTransaction(tx) }

    dao.upsertTransactions(sampleTransactions.map { TransactionEntity.fromDomain(it) })
    dao.upsertInvoices(invoices.map { InvoiceEntity.fromDomain(it) })
    dao.upsertInventoryMovements(movements.map { InventoryMovementEntity.fromDomain(it) })
  }
}
