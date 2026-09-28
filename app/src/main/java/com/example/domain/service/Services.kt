package com.example.domain.service

import android.content.Context
import com.example.domain.calculation.CalculationEngine
import com.example.domain.model.AuditLogEntry
import com.example.domain.model.BusinessProfile
import com.example.domain.model.Customer
import com.example.domain.model.CustomerLedgerSummary
import com.example.domain.model.DailySummaryRecord
import com.example.domain.model.GoogleAccountState
import com.example.domain.model.InventoryMovement
import com.example.domain.model.InventoryReconciliationRecord
import com.example.domain.model.Invoice
import com.example.domain.model.InvoiceStatus
import com.example.domain.model.MetalRate
import com.example.domain.model.MetalType
import com.example.domain.model.OpeningStockBalance
import com.example.domain.model.PendingDriveUpload
import com.example.domain.model.PendingSyncRecord
import com.example.domain.model.PurchaseRecord
import com.example.domain.model.SaleRecord
import com.example.domain.model.ScrapProcessingRecord
import com.example.domain.model.SecuritySettings
import com.example.domain.model.ShopReminder
import com.example.domain.model.SpreadsheetInfo
import com.example.domain.model.SyncStatus
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionStatus
import com.example.domain.model.TransactionType
import com.example.domain.model.Vendor
import com.example.domain.model.WorksheetSchemas
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import kotlinx.coroutines.flow.Flow

/**
 * Clean storage abstraction for Jewellery Business Manager.
 * Combines local Room persistence with PendingSyncQueue and PendingDriveUploads management.
 */
interface StorageService {
  val businessProfileFlow: Flow<BusinessProfile>
  val metalRateFlow: Flow<MetalRate>
  val customersFlow: Flow<List<Customer>>
  val transactionsFlow: Flow<List<Transaction>>
  val invoicesFlow: Flow<List<Invoice>>
  val pendingSyncQueueFlow: Flow<List<PendingSyncRecord>>
  val pendingDriveUploadsFlow: Flow<List<PendingDriveUpload>>
  val googleAccountStateFlow: Flow<GoogleAccountState>

  // Stage 5 & 6 Reactive Flows
  val openingStocksFlow: Flow<List<OpeningStockBalance>>
  val inventoryMovementsFlow: Flow<List<InventoryMovement>>
  val inventoryReconciliationsFlow: Flow<List<InventoryReconciliationRecord>>
  val reconciliationsFlow: Flow<List<InventoryReconciliationRecord>>
    get() = inventoryReconciliationsFlow
  val vendorsFlow: Flow<List<Vendor>>
  val purchasesFlow: Flow<List<PurchaseRecord>>
  val salesFlow: Flow<List<SaleRecord>>
  val scrapProcessingsFlow: Flow<List<ScrapProcessingRecord>>
  val scrapProcessesFlow: Flow<List<ScrapProcessingRecord>>
    get() = scrapProcessingsFlow
  val securitySettingsFlow: Flow<SecuritySettings>
  val auditLogsFlow: Flow<List<AuditLogEntry>>
  val remindersFlow: Flow<List<ShopReminder>>

  suspend fun saveBusinessProfile(profile: BusinessProfile)
  suspend fun saveMetalRate(rate: MetalRate)
  suspend fun saveCustomer(customer: Customer)
  suspend fun saveCustomersBatch(customers: List<Customer>)
  suspend fun findCustomerByMobileLocal(mobile: String): Customer?
  suspend fun deleteCustomer(customerId: String)
  suspend fun saveTransaction(transaction: Transaction)
  suspend fun saveInvoice(invoice: Invoice)
  suspend fun getInvoiceByIdOrNumber(invoiceIdOrNumber: String): Invoice?
  suspend fun saveTransactionAndInvoice(transaction: Transaction, invoice: Invoice)
  suspend fun saveTransactionsBatch(transactions: List<Transaction>)
  suspend fun saveInvoicesBatch(invoices: List<Invoice>)
  suspend fun updateTransactionSyncStatus(transactionId: String, syncStatus: SyncStatus)
  suspend fun cancelTransaction(
    transactionId: String,
    cancelledBy: String,
    cancellationReason: String,
    cancelledAt: Long = System.currentTimeMillis(),
  ): Transaction?
  suspend fun deleteTransaction(transactionId: String)
  suspend fun deleteInvoice(invoiceNumber: String)
  suspend fun getNextInvoiceNumber(): String

  // Stage 5 Inventory, Vendors, Purchases, Sales & Scrap Processing
  suspend fun getOpeningStockOnce(metal: MetalType): OpeningStockBalance
  suspend fun saveOpeningStock(balance: OpeningStockBalance)
  suspend fun saveOpeningStocksBatch(stocks: List<OpeningStockBalance>)
  suspend fun getInventoryMovementsOnce(): List<InventoryMovement>
  suspend fun saveInventoryMovement(movement: InventoryMovement)
  suspend fun saveInventoryMovementsBatch(movements: List<InventoryMovement>)
  suspend fun saveInventoryReconciliation(record: InventoryReconciliationRecord)
  suspend fun saveReconciliation(record: InventoryReconciliationRecord) =
    saveInventoryReconciliation(record)
  suspend fun saveVendor(vendor: Vendor)
  suspend fun saveVendorsBatch(vendors: List<Vendor>)
  suspend fun deleteVendor(vendorId: String)
  suspend fun savePurchase(purchase: PurchaseRecord, movement: InventoryMovement? = null)
  suspend fun savePurchasesBatch(purchases: List<PurchaseRecord>)
  suspend fun cancelPurchase(purchaseId: String, reason: String): PurchaseRecord?
  suspend fun saveSale(sale: SaleRecord, movement: InventoryMovement? = null)
  suspend fun saveSalesBatch(sales: List<SaleRecord>)
  suspend fun cancelSale(saleId: String, reason: String): SaleRecord?
  suspend fun saveScrapProcessing(record: ScrapProcessingRecord)
  suspend fun saveScrapProcess(record: ScrapProcessingRecord) = saveScrapProcessing(record)

  // Stage 6 Security, Audit, Reminders, Backup & Recovery
  suspend fun saveSecuritySettings(settings: SecuritySettings)
  suspend fun saveAuditLog(entry: AuditLogEntry)
  suspend fun recordAuditLog(entry: AuditLogEntry) = saveAuditLog(entry)
  suspend fun saveReminder(reminder: ShopReminder)
  suspend fun deleteReminder(reminderId: String)
  suspend fun exportFullBackupJson(): String
  suspend fun importFullBackupJson(json: String): Result<Int>
  suspend fun restoreFromBackupJson(json: String): Result<Int> = importFullBackupJson(json)
  suspend fun verifyDataIntegrity(): Pair<Int, List<String>>
  suspend fun runDataRecoveryAndIntegrityCheck(): Int = verifyDataIntegrity().first

  // Google Sheets Sync Queue operations
  suspend fun enqueueSyncRecord(record: PendingSyncRecord)
  suspend fun getPendingSyncRecords(): List<PendingSyncRecord>
  suspend fun markSyncRecordCompleted(recordId: String)
  suspend fun markSyncRecordFailed(recordId: String, errorMessage: String)

  // Google Drive Upload Queue operations (Stage 4)
  suspend fun enqueueDriveUpload(upload: PendingDriveUpload)
  suspend fun getPendingDriveUploads(): List<PendingDriveUpload>
  suspend fun markDriveUploadCompleted(
    uploadId: String,
    driveFileId: String,
    driveFileName: String,
    driveFileUrl: String,
  )
  suspend fun markDriveUploadFailed(uploadId: String, errorMessage: String)

  // Google Account & Sheet/Drive Connection Persistence
  suspend fun getGoogleAccountStateOnce(): GoogleAccountState
  suspend fun saveGoogleAccountState(state: GoogleAccountState)

  suspend fun seedInitialDataIfNeeded()
  suspend fun resetDemoData()
}

/**
 * Service responsible for generating and formatting Invoices from Transactions (Stage 3 & Stage 4).
 * Includes Deductions, Net Amount, Optional GST, Customer Balance (separate Money/Gold/Silver),
 * Configurable Terms & Conditions, and Multiple Transaction Items.
 */
class InvoiceService(private val calculationEngine: CalculationEngine = CalculationEngine()) {

  fun createInvoiceFromTransaction(
    transaction: Transaction,
    customer: Customer?,
    businessProfile: BusinessProfile,
    previousLedger: CustomerLedgerSummary? = null,
    afterLedger: CustomerLedgerSummary? = null,
    existingInvoice: Invoice? = null,
  ): Invoice {
    val itemDescription =
      when (transaction.transactionType) {
        TransactionType.MONEY_TO_GOLD ->
          "Gold Purchase (${calculationEngine.formatWeight(transaction.fineWeight)} g @ ₹${calculationEngine.formatMoney(transaction.rate)} ${transaction.rateUnit.shortLabel})"
        TransactionType.MONEY_TO_SILVER ->
          "Silver Purchase (${calculationEngine.formatWeight(transaction.fineWeight)} g @ ₹${calculationEngine.formatMoney(transaction.rate)} ${transaction.rateUnit.shortLabel})"
        TransactionType.GOLD_PAYMENT ->
          "Gold Received (${transaction.purityMode.displayName}, Tunch ${calculationEngine.formatTunch(transaction.tunch)}%)"
        TransactionType.SILVER_PAYMENT ->
          "Silver Received (${transaction.purityMode.displayName}, Tunch ${calculationEngine.formatTunch(transaction.tunch)}%)"
        TransactionType.SCRAP_GOLD ->
          "Gold Scrap (Gross ${calculationEngine.formatWeight(transaction.grossWeight)} g @ Tunch ${calculationEngine.formatTunch(transaction.tunch)}%)"
        TransactionType.SCRAP_SILVER ->
          "Silver Scrap (Gross ${calculationEngine.formatWeight(transaction.grossWeight)} g @ Tunch ${calculationEngine.formatTunch(transaction.tunch)}%)"
        TransactionType.GOLD_ADJUSTMENT ->
          "Gold Adjustment (${calculationEngine.formatWeight(transaction.fineWeight)} g Fine)"
        TransactionType.SILVER_ADJUSTMENT ->
          "Silver Adjustment (${calculationEngine.formatWeight(transaction.fineWeight)} g Fine)"
      }

    val lineItems =
      if (transaction.items.isNotEmpty()) {
        transaction.items.mapIndexed { idx, item ->
          item.copy(
            id = item.id.ifBlank { "TXNI-${transaction.transactionId.removePrefix("TXN-")}-${idx + 1}" },
            transactionId = transaction.transactionId,
          )
        }
      } else {
        listOf(
          TransactionItem(
            id = "TXNI-${transaction.transactionId.removePrefix("TXN-")}-1",
            transactionId = transaction.transactionId,
            itemName = transaction.transactionType.title,
            description = itemDescription,
            metalType = transaction.metalType,
            grossWeight = transaction.grossWeight,
            tunch = transaction.tunch,
            purity = transaction.purity.ifBlank { transaction.purityMode.displayName },
            fineWeight = transaction.fineWeight,
            rate = transaction.rate,
            rateUnit = transaction.rateUnit,
            metalValue = transaction.effectiveMetalValue,
            amount = transaction.effectiveMetalValue,
          )
        )
      }

    // Permanent Invoice ID & Invoice Number tied to the transaction
    val permanentInvoiceNumber =
      existingInvoice?.invoiceNumber?.ifBlank { transaction.invoiceNumber }
        ?: transaction.invoiceNumber
    val invoiceId =
      existingInvoice?.invoiceId?.ifBlank { "INV-${transaction.transactionId.removePrefix("TXN-")}" }
        ?: "INV-${transaction.transactionId.removePrefix("TXN-")}"

    val invoiceStatusEnum =
      when {
        transaction.status == TransactionStatus.CANCELLED -> InvoiceStatus.CANCELLED
        transaction.status == TransactionStatus.DRAFT -> InvoiceStatus.DRAFT
        existingInvoice != null && existingInvoice.isSavedToDrive -> InvoiceStatus.DRIVE_SAVED
        existingInvoice != null && existingInvoice.hasPdfGenerated -> InvoiceStatus.PDF_GENERATED
        else -> InvoiceStatus.COMPLETED
      }

    val grossMetalVal = transaction.effectiveMetalValue
    val deductionVal = transaction.deductions
    val netTaxableVal = transaction.amount

    // GST Calculation: strictly optional, ONLY applied when businessProfile.gstEnabled is true
    val gstActive = businessProfile.gstEnabled && businessProfile.gstTaxTreatment != "EXEMPT_NONE"
    val hundred = BigDecimal("100")
    val isInterState =
      businessProfile.gstTaxTreatment == "INTER_STATE_IGST" ||
        (customer != null &&
          customer.state.isNotBlank() &&
          businessProfile.state.isNotBlank() &&
          !customer.state.trim().equals(businessProfile.state.trim(), ignoreCase = true))
    val cgstPct =
      if (gstActive && !isInterState) businessProfile.cgstRatePercent.max(BigDecimal.ZERO)
      else BigDecimal.ZERO
    val sgstPct =
      if (gstActive && !isInterState) businessProfile.sgstRatePercent.max(BigDecimal.ZERO)
      else BigDecimal.ZERO
    val igstPct =
      if (gstActive) {
        if (isInterState) {
          businessProfile.igstRatePercent
            .takeIf { it > BigDecimal.ZERO }
            ?: businessProfile.defaultGstRatePercent.max(BigDecimal.ZERO)
        } else {
          businessProfile.igstRatePercent.max(BigDecimal.ZERO)
        }
      } else BigDecimal.ZERO

    val cgstAmt =
      if (gstActive && cgstPct > BigDecimal.ZERO) {
        netTaxableVal.multiply(cgstPct).divide(hundred, 2, RoundingMode.HALF_UP)
      } else BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)

    val sgstAmt =
      if (gstActive && sgstPct > BigDecimal.ZERO) {
        netTaxableVal.multiply(sgstPct).divide(hundred, 2, RoundingMode.HALF_UP)
      } else BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)

    val igstAmt =
      if (gstActive && igstPct > BigDecimal.ZERO) {
        netTaxableVal.multiply(igstPct).divide(hundred, 2, RoundingMode.HALF_UP)
      } else BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)

    val totalTaxAmt = cgstAmt.add(sgstAmt).add(igstAmt).setScale(2, RoundingMode.HALF_UP)
    val grandTotal = netTaxableVal.add(totalTaxAmt).setScale(2, RoundingMode.HALF_UP)

    val receivedAmt =
      when {
        transaction.cashReceived > BigDecimal.ZERO -> transaction.cashReceived
        transaction.remainingBalance > BigDecimal.ZERO ->
          grandTotal.subtract(transaction.remainingBalance).max(BigDecimal.ZERO)
        else -> grandTotal
      }
    val pendingAmt =
      when {
        transaction.remainingBalance > BigDecimal.ZERO -> transaction.remainingBalance
        receivedAmt < grandTotal -> grandTotal.subtract(receivedAmt)
        else -> BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP)
      }

    return Invoice(
      invoiceId = invoiceId,
      invoiceNumber = permanentInvoiceNumber,
      transactionId = transaction.transactionId,
      date = transaction.date,
      time = transaction.time,
      timestamp = transaction.timestamp,
      shopName = businessProfile.shopName,
      ownerName = businessProfile.ownerName,
      shopMobile = businessProfile.mobileNumber,
      shopAddress = businessProfile.formattedFullAddress(),
      shopPan = businessProfile.panNumber,
      shopGst =
        if (businessProfile.showGstOnInvoice && businessProfile.gstNumber.isNotBlank())
          businessProfile.gstNumber
        else "",
      bankName = businessProfile.formattedBankNameWithBranch(),
      bankAccountNumber = businessProfile.bankAccountNumber,
      ifsc = businessProfile.ifsc,
      upiId = businessProfile.upiId,
      customerId = transaction.customerId,
      customerName = customer?.name ?: transaction.customerName,
      customerMobile = customer?.mobileNumber ?: transaction.customerMobile,
      customerAddress = customer?.formattedFullAddress ?: customer?.address ?: "",
      customerPan = customer?.panNumber ?: "",
      customerGst = customer?.gstNumber ?: "",
      transactionType = transaction.transactionType,
      metalType = transaction.metalType,
      purityMode = transaction.purityMode,
      grossWeight = transaction.grossWeight,
      tunch = transaction.tunch,
      fineWeight = transaction.fineWeight,
      rate = transaction.rate,
      rateUnit = transaction.rateUnit,
      amount = grossMetalVal,
      subtotal = grossMetalVal,
      deductions = deductionVal,
      netAmount = netTaxableVal,
      taxableAmount = netTaxableVal,
      gstEnabled = gstActive,
      cgstPercent = cgstPct,
      sgstPercent = sgstPct,
      igstPercent = igstPct,
      cgstAmount = cgstAmt,
      sgstAmount = sgstAmt,
      igstAmount = igstAmt,
      taxAmount = totalTaxAmt,
      discount = deductionVal,
      paymentMode = transaction.paymentMode,
      amountReceived = receivedAmt,
      amountPending = pendingAmt,
      previousMoneyBalance = previousLedger?.netMoneyBalance ?: BigDecimal.ZERO,
      previousGoldBalanceGrams = previousLedger?.netGoldBalanceGrams ?: BigDecimal.ZERO,
      previousSilverBalanceGrams = previousLedger?.netSilverBalanceGrams ?: BigDecimal.ZERO,
      afterMoneyBalance = afterLedger?.netMoneyBalance ?: BigDecimal.ZERO,
      afterGoldBalanceGrams = afterLedger?.netGoldBalanceGrams ?: BigDecimal.ZERO,
      afterSilverBalanceGrams = afterLedger?.netSilverBalanceGrams ?: BigDecimal.ZERO,
      showCustomerBalance = businessProfile.showCustomerBalanceOnInvoice,
      status = invoiceStatusEnum.name,
      invoiceStatus = invoiceStatusEnum,
      notes = transaction.notes,
      termsAndConditions = businessProfile.termsAndConditions,
      invoiceFooter = businessProfile.invoiceFooter,
      businessLogoUri = businessProfile.businessLogoUri,
      logoPosition = businessProfile.logoPosition,
      showBankDetails = businessProfile.showBankDetailsOnInvoice,
      showUpi = businessProfile.showUpiOnInvoice,
      showPan = businessProfile.showPanOnInvoice,
      showGst = businessProfile.showGstOnInvoice && businessProfile.gstNumber.isNotBlank(),
      showCustomerPan = businessProfile.showCustomerPanOnInvoice,
      showCustomerGst = businessProfile.showCustomerGstOnInvoice,
      items = lineItems,
      totalAmount = grandTotal,
      localPdfPath = existingInvoice?.localPdfPath ?: "",
      pdfFileName = "Invoice_$permanentInvoiceNumber.pdf",
      driveFileId = existingInvoice?.driveFileId ?: "",
      driveFileName = existingInvoice?.driveFileName ?: "",
      driveFileUrl = existingInvoice?.driveFileUrl ?: "",
      driveSavedAt = existingInvoice?.driveSavedAt ?: 0L,
      createdAt = existingInvoice?.createdAt ?: transaction.timestamp,
      updatedAt = System.currentTimeMillis(),
    )
  }

  fun formatPrintableInvoiceText(invoice: Invoice): String {
    return buildString {
      appendLine("========================================")
      if (invoice.isCancelled) {
        appendLine("        *** CANCELLED INVOICE ***       ")
        appendLine("========================================")
      }
      appendLine("      ${invoice.shopName.uppercase()}")
      if (invoice.ownerName.isNotBlank()) appendLine("Prop: ${invoice.ownerName}")
      if (invoice.shopAddress.isNotBlank()) appendLine(invoice.shopAddress)
      if (invoice.shopMobile.isNotBlank()) appendLine("Mobile: ${invoice.shopMobile}")
      if (invoice.showPan && invoice.shopPan.isNotBlank()) appendLine("PAN: ${invoice.shopPan}")
      if (invoice.showGst && invoice.shopGst.isNotBlank()) appendLine("GSTIN: ${invoice.shopGst}")
      appendLine("========================================")
      appendLine("INVOICE")
      appendLine("Invoice No      : ${invoice.invoiceNumber}")
      appendLine("Date            : ${invoice.date}   Time: ${invoice.time}")
      appendLine("Transaction Type: ${invoice.transactionType.title.uppercase()}")
      appendLine("Status          : ${invoice.invoiceStatus.displayName}")
      appendLine("----------------------------------------")
      appendLine("CUSTOMER DETAILS:")
      appendLine("Name   : ${invoice.customerName}")
      if (invoice.customerMobile.isNotBlank()) appendLine("Mobile : ${invoice.customerMobile}")
      if (invoice.customerAddress.isNotBlank()) appendLine("Address: ${invoice.customerAddress}")
      if (invoice.showCustomerPan && invoice.customerPan.isNotBlank()) {
        appendLine("PAN    : ${invoice.customerPan}")
      }
      if (invoice.showCustomerGst && invoice.customerGst.isNotBlank()) {
        appendLine("GSTIN  : ${invoice.customerGst}")
      }
      appendLine("----------------------------------------")
      if (
        invoice.transactionType == TransactionType.MONEY_TO_GOLD ||
          invoice.transactionType == TransactionType.MONEY_TO_SILVER
      ) {
        appendLine("Description   : ${invoice.metalType.displayName} Purchase")
        appendLine("Metal         : ${invoice.metalType.displayName}")
        appendLine("Amount        : ₹${calculationEngine.formatMoney(invoice.amount)}")
        appendLine("Rate          : ₹${calculationEngine.formatMoney(invoice.rate)} ${invoice.rateUnit.shortLabel}")
        appendLine("Quantity      : ${calculationEngine.formatWeight(invoice.fineWeight)} g")
      } else {
        if (invoice.items.size > 1) {
          appendLine("ITEMS (${invoice.items.size}):")
          invoice.items.forEachIndexed { index, item ->
            appendLine(
              "  Item ${index + 1}: ${item.description} — ${invoice.metalType.displayName} — Gross ${calculationEngine.formatWeight(item.grossWeight)}g — Tunch ${calculationEngine.formatTunch(item.tunch)} — Fine ${calculationEngine.formatWeight(item.fineWeight)}g — ₹${calculationEngine.formatMoney(item.amount)}"
            )
          }
          appendLine("Total Gross Weight: ${calculationEngine.formatWeight(invoice.grossWeight)} g")
          appendLine("Total Fine Weight : ${calculationEngine.formatWeight(invoice.fineWeight)} g")
        } else {
          appendLine("Description   : ${invoice.items.firstOrNull()?.description ?: invoice.transactionType.title}")
          appendLine("Metal         : ${invoice.metalType.displayName}")
          appendLine("Gross Weight  : ${calculationEngine.formatWeight(invoice.grossWeight)} g")
          appendLine("Tunch/Purity  : ${calculationEngine.formatTunch(invoice.tunch)}")
          appendLine("Fine Weight   : ${calculationEngine.formatWeight(invoice.fineWeight)} g")
          appendLine("Rate          : ₹${calculationEngine.formatMoney(invoice.rate)} ${invoice.rateUnit.shortLabel}")
        }
      }
      appendLine("----------------------------------------")
      if (invoice.deductions > BigDecimal.ZERO) {
        appendLine("Gross Metal Value : ₹${calculationEngine.formatMoney(invoice.subtotal)}")
        appendLine("Deductions        : -₹${calculationEngine.formatMoney(invoice.deductions)}")
        appendLine("Net Metal Value   : ₹${calculationEngine.formatMoney(invoice.netAmount)}")
      } else {
        appendLine("Total Value       : ₹${calculationEngine.formatMoney(invoice.netAmount)}")
      }
      if (invoice.gstEnabled && invoice.taxAmount > BigDecimal.ZERO) {
        appendLine("Taxable Amount    : ₹${calculationEngine.formatMoney(invoice.taxableAmount)}")
        if (invoice.cgstAmount > BigDecimal.ZERO) {
          appendLine("CGST (${calculationEngine.formatTunch(invoice.cgstPercent)}%)      : ₹${calculationEngine.formatMoney(invoice.cgstAmount)}")
        }
        if (invoice.sgstAmount > BigDecimal.ZERO) {
          appendLine("SGST (${calculationEngine.formatTunch(invoice.sgstPercent)}%)      : ₹${calculationEngine.formatMoney(invoice.sgstAmount)}")
        }
        if (invoice.igstAmount > BigDecimal.ZERO) {
          appendLine("IGST (${calculationEngine.formatTunch(invoice.igstPercent)}%)      : ₹${calculationEngine.formatMoney(invoice.igstAmount)}")
        }
        appendLine("Total Tax         : ₹${calculationEngine.formatMoney(invoice.taxAmount)}")
      }
      appendLine("GRAND TOTAL       : ₹${calculationEngine.formatMoney(invoice.totalAmount)}")
      appendLine("Payment Mode      : ${invoice.paymentMode.displayName}")
      appendLine("Amount Received   : ₹${calculationEngine.formatMoney(invoice.amountReceived)}")
      if (invoice.amountPending > BigDecimal.ZERO) {
        appendLine("Amount Pending    : ₹${calculationEngine.formatMoney(invoice.amountPending)}")
      }
      if (invoice.notes.isNotBlank()) {
        appendLine("Notes             : ${invoice.notes}")
      }
      if (invoice.showBankDetails && invoice.bankName.isNotBlank()) {
        appendLine("Bank: ${invoice.bankName} | A/C: ${invoice.bankAccountNumber} | IFSC: ${invoice.ifsc}")
      }
      if (invoice.showUpi && invoice.upiId.isNotBlank()) {
        appendLine("UPI ID: ${invoice.upiId}")
      }
      if (invoice.termsAndConditions.isNotBlank()) {
        appendLine("----------------------------------------")
        appendLine("Terms & Conditions: ${invoice.termsAndConditions}")
      }
      appendLine("----------------------------------------")
      appendLine("Customer Signature          Authorized Signature")
      appendLine("========================================")
    }
  }
}

interface GoogleAuthService {
  val isConfiguredForCurrentStage: Boolean
  val stageStatusMessage: String

  suspend fun signInWithGoogle(
    context: Context?,
    selectedEmailOverride: String? = null,
    accessTokenOverride: String? = null,
  ): Result<GoogleAccountState>

  suspend fun reconnectGoogleAccount(context: Context?): Result<GoogleAccountState>

  suspend fun disconnectGoogleAccount(): Result<GoogleAccountState>
}

interface GoogleSheetsService {
  val isConfiguredForCurrentStage: Boolean
  val stageStatusMessage: String

  suspend fun connect(spreadsheetId: String, spreadsheetName: String): Result<SpreadsheetInfo>
  suspend fun disconnect()
  suspend fun listAvailableSpreadsheets(): Result<List<SpreadsheetInfo>>
  suspend fun createSpreadsheet(
    title: String = WorksheetSchemas.DEFAULT_DATABASE_NAME
  ): Result<SpreadsheetInfo>
  suspend fun initializeSheets(spreadsheetId: String): Result<List<String>>

  suspend fun getBusinessProfile(spreadsheetId: String): Result<BusinessProfile?>
  suspend fun saveBusinessProfile(spreadsheetId: String, profile: BusinessProfile): Result<Unit>

  suspend fun getCustomers(spreadsheetId: String): Result<List<Customer>>
  suspend fun searchCustomerByMobile(spreadsheetId: String, mobile: String): Result<Customer?>
  suspend fun saveCustomer(spreadsheetId: String, customer: Customer): Result<Unit>
  suspend fun updateCustomer(spreadsheetId: String, customer: Customer): Result<Unit>

  suspend fun getRates(spreadsheetId: String): Result<MetalRate?>
  suspend fun saveRate(spreadsheetId: String, rate: MetalRate): Result<Unit>

  suspend fun getTransactions(spreadsheetId: String): Result<List<Transaction>>
  suspend fun saveTransaction(spreadsheetId: String, transaction: Transaction): Result<Unit>

  suspend fun getInvoices(spreadsheetId: String): Result<List<Invoice>>
  suspend fun saveInvoice(spreadsheetId: String, invoice: Invoice): Result<Unit>

  suspend fun saveSetting(spreadsheetId: String, key: String, value: String): Result<Unit>
  suspend fun saveDailySummary(spreadsheetId: String, summary: DailySummaryRecord): Result<Unit>

  // Stage 5 & 6 Google Sheets Synchronization
  suspend fun saveInventoryMovement(spreadsheetId: String, movement: InventoryMovement): Result<Unit> =
    Result.success(Unit)
  suspend fun getInventoryMovements(spreadsheetId: String): Result<List<InventoryMovement>> =
    Result.success(emptyList())
  suspend fun saveOpeningStock(spreadsheetId: String, balance: OpeningStockBalance): Result<Unit> =
    Result.success(Unit)
  suspend fun getOpeningStocks(spreadsheetId: String): Result<List<OpeningStockBalance>> =
    Result.success(emptyList())
  suspend fun saveVendor(spreadsheetId: String, vendor: Vendor): Result<Unit> = Result.success(Unit)
  suspend fun getVendors(spreadsheetId: String): Result<List<Vendor>> = Result.success(emptyList())
  suspend fun savePurchase(spreadsheetId: String, purchase: PurchaseRecord): Result<Unit> =
    Result.success(Unit)
  suspend fun getPurchases(spreadsheetId: String): Result<List<PurchaseRecord>> =
    Result.success(emptyList())
  suspend fun saveSale(spreadsheetId: String, sale: SaleRecord): Result<Unit> = Result.success(Unit)
  suspend fun getSales(spreadsheetId: String): Result<List<SaleRecord>> =
    Result.success(emptyList())
  suspend fun saveCustomerLedgerSummary(
    spreadsheetId: String,
    ledger: CustomerLedgerSummary,
  ): Result<Unit> = Result.success(Unit)
  suspend fun saveAuditLog(spreadsheetId: String, audit: AuditLogEntry): Result<Unit> =
    Result.success(Unit)

  suspend fun syncPendingTransactions(
    spreadsheetId: String,
    pendingRecords: List<PendingSyncRecord>,
  ): Result<Int>

  suspend fun getWorksheetRows(spreadsheetId: String, worksheetName: String): List<List<String>>
}

data class DriveFolderHierarchy(
  val rootFolderId: String,
  val invoicesFolderId: String,
  val backupsFolderId: String,
  val reportsFolderId: String,
  val invoiceFolderPath: String = "Jewellery Business Manager / Invoices",
)

data class DriveSavedFileResult(
  val driveFileId: String,
  val driveFileName: String,
  val driveFileUrl: String,
  val savedAtTimestamp: Long,
  val reusedExistingFile: Boolean = false,
)

/**
 * Google Drive PDF Storage & Folder Management Service (Stage 4).
 * Uses the connected Google account, ensures "Jewellery Business Manager / Invoices" exists
 * without duplicates, keeps files private by default, and prevents duplicate uploads.
 */
interface GoogleDriveService {
  val isConfiguredForCurrentStage: Boolean
  val stageStatusMessage: String

  suspend fun ensureDriveFolders(): Result<DriveFolderHierarchy>

  suspend fun saveInvoicePdfToDrive(
    invoice: Invoice,
    pdfFile: File,
    forceRegenerate: Boolean = false,
  ): Result<DriveSavedFileResult>

  suspend fun saveReportPdfToDrive(
    reportTitle: String,
    pdfFile: File,
  ): Result<DriveSavedFileResult> =
    Result.failure(IllegalStateException("Google Drive report upload not configured"))

  suspend fun syncPendingDriveUploads(
    pendingUploads: List<PendingDriveUpload>,
  ): Result<Int>
}
