package com.example.domain.model

import java.math.BigDecimal

// ==================== STAGE 5: INVENTORY, VENDOR, PURCHASE, SALE, SCRAP & REPORTS MODELS ====================

enum class InventoryMovementType(val displayName: String, val hindiName: String) {
  OPENING_BALANCE("Opening Balance", "प्रारंभिक स्टॉक"),
  PURCHASE("Purchase", "खरीद"),
  SALE("Sale", "बिक्री"),
  CUSTOMER_RECEIVED("Customer Received", "ग्राहक से प्राप्त"),
  CUSTOMER_GIVEN("Customer Given", "ग्राहक को दिया"),
  SCRAP_RECEIVED("Scrap Received", "स्क्रैप प्राप्त"),
  SCRAP_PROCESSED("Scrap Processed", "स्क्रैप गलाया"),
  ADJUSTMENT("Adjustment", "समायोजन"),
  REVERSAL("Reversal", "रिवर्सल"),
}

enum class InventoryMovementDirection(val displayName: String) {
  IN("IN (+)"),
  OUT("OUT (-)"),
}

data class InventoryMovement(
  val movementId: String,
  val date: String,
  val transactionId: String = "",
  val invoiceNumber: String = "",
  val movementType: InventoryMovementType,
  val metal: MetalType,
  val grossWeight: BigDecimal,
  val tunch: BigDecimal,
  val fineWeight: BigDecimal,
  val rate: BigDecimal = BigDecimal.ZERO,
  val value: BigDecimal = BigDecimal.ZERO,
  val direction: InventoryMovementDirection,
  val notes: String = "",
  val createdAt: Long = System.currentTimeMillis(),
)

data class OpeningStock(
  val metal: MetalType,
  val grossWeight: BigDecimal = BigDecimal.ZERO,
  val tunch: BigDecimal = BigDecimal("99.50"),
  val fineWeight: BigDecimal = BigDecimal.ZERO,
  val referenceRatePerGram: BigDecimal = BigDecimal.ZERO,
  val date: String = "2025-04-01",
  val notes: String = "Configured Opening Stock",
  val updatedAt: Long = System.currentTimeMillis(),
) {
  val referenceRate: BigDecimal
    get() = referenceRatePerGram
}

typealias OpeningStockBalance = OpeningStock

data class MetalInventorySummary(
  val metal: MetalType,
  val openingGrossWeight: BigDecimal = BigDecimal.ZERO,
  val openingFineWeight: BigDecimal = BigDecimal.ZERO,
  val purchasedGrossWeight: BigDecimal = BigDecimal.ZERO,
  val purchasedFineWeight: BigDecimal = BigDecimal.ZERO,
  val customerReceivedGrossWeight: BigDecimal = BigDecimal.ZERO,
  val customerReceivedFineWeight: BigDecimal = BigDecimal.ZERO,
  val adjustmentInGrossWeight: BigDecimal = BigDecimal.ZERO,
  val adjustmentInFineWeight: BigDecimal = BigDecimal.ZERO,
  val soldGrossWeight: BigDecimal = BigDecimal.ZERO,
  val soldFineWeight: BigDecimal = BigDecimal.ZERO,
  val customerGivenGrossWeight: BigDecimal = BigDecimal.ZERO,
  val customerGivenFineWeight: BigDecimal = BigDecimal.ZERO,
  val scrapReceivedGrossWeight: BigDecimal = BigDecimal.ZERO,
  val scrapReceivedFineWeight: BigDecimal = BigDecimal.ZERO,
  val scrapProcessedGrossWeight: BigDecimal = BigDecimal.ZERO,
  val scrapProcessedFineWeight: BigDecimal = BigDecimal.ZERO,
  val adjustmentOutGrossWeight: BigDecimal = BigDecimal.ZERO,
  val adjustmentOutFineWeight: BigDecimal = BigDecimal.ZERO,
  val currentGrossWeight: BigDecimal = BigDecimal.ZERO,
  val currentFineWeight: BigDecimal = BigDecimal.ZERO,
  val currentRatePerGram: BigDecimal = BigDecimal.ZERO,
  val estimatedValue: BigDecimal = BigDecimal.ZERO,
  val movementsCount: Int = 0,
) {
  val referenceRatePerGram: BigDecimal
    get() = currentRatePerGram

  val averageReferenceRatePerGram: BigDecimal
    get() = currentRatePerGram

  val estimatedCurrentValue: BigDecimal
    get() = estimatedValue

  val receivedFromCustomersGrossWeight: BigDecimal
    get() = customerReceivedGrossWeight

  val receivedFromCustomersFineWeight: BigDecimal
    get() = customerReceivedFineWeight

  val addedByAdjustmentGrossWeight: BigDecimal
    get() = adjustmentInGrossWeight

  val addedByAdjustmentFineWeight: BigDecimal
    get() = adjustmentInFineWeight

  val usedOrSoldGrossWeight: BigDecimal
    get() = soldGrossWeight

  val usedOrSoldFineWeight: BigDecimal
    get() = soldFineWeight

  val givenToCustomersGrossWeight: BigDecimal
    get() = customerGivenGrossWeight

  val givenToCustomersFineWeight: BigDecimal
    get() = customerGivenFineWeight

  val netAdjustmentGrossWeight: BigDecimal
    get() = adjustmentInGrossWeight.subtract(adjustmentOutGrossWeight)

  val netAdjustmentFineWeight: BigDecimal
    get() = adjustmentInFineWeight.subtract(adjustmentOutFineWeight)

  val lowStockThresholdFineGrams: BigDecimal
    get() = if (metal == MetalType.GOLD) BigDecimal("10.000") else BigDecimal("250.000")

  val isLowStock: Boolean
    get() = currentFineWeight < lowStockThresholdFineGrams
}

data class Vendor(
  val vendorId: String,
  val name: String,
  val mobile: String,
  val companyName: String = "",
  val address: String = "",
  val gstNumber: String = "",
  val panNumber: String = "",
  val pendingMoney: BigDecimal = BigDecimal.ZERO,
  val pendingGoldFineGrams: BigDecimal = BigDecimal.ZERO,
  val pendingSilverFineGrams: BigDecimal = BigDecimal.ZERO,
  val notes: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
) {
  val vendorName: String
    get() = name

  val mobileNumber: String
    get() = mobile

  val pendingPayableAmount: BigDecimal
    get() = pendingMoney
}

data class VendorLedgerSummary(
  val vendorId: String,
  val vendorName: String,
  val totalPurchasesCount: Int = 0,
  val totalGoldPurchasedFineGrams: BigDecimal = BigDecimal.ZERO,
  val totalSilverPurchasedFineGrams: BigDecimal = BigDecimal.ZERO,
  val totalPurchaseAmount: BigDecimal = BigDecimal.ZERO,
  val totalAmountPaid: BigDecimal = BigDecimal.ZERO,
  val pendingMoneyBalance: BigDecimal = BigDecimal.ZERO,
  val pendingGoldFineGrams: BigDecimal = BigDecimal.ZERO,
  val pendingSilverFineGrams: BigDecimal = BigDecimal.ZERO,
) {
  val totalMoneyPaid: BigDecimal
    get() = totalAmountPaid

  val moneyPending: BigDecimal
    get() = pendingMoneyBalance

  val goldReceivedFineGrams: BigDecimal
    get() = totalGoldPurchasedFineGrams

  val goldGivenFineGrams: BigDecimal
    get() = BigDecimal.ZERO.setScale(3)

  val netGoldBalanceFineGrams: BigDecimal
    get() = pendingGoldFineGrams.max(totalGoldPurchasedFineGrams)

  val silverReceivedFineGrams: BigDecimal
    get() = totalSilverPurchasedFineGrams

  val silverGivenFineGrams: BigDecimal
    get() = BigDecimal.ZERO.setScale(3)

  val netSilverBalanceFineGrams: BigDecimal
    get() = pendingSilverFineGrams.max(totalSilverPurchasedFineGrams)
}

data class PurchaseRecord(
  val purchaseId: String,
  val date: String,
  val time: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val vendorId: String,
  val vendorName: String,
  val vendorMobile: String = "",
  val metal: MetalType,
  val description: String,
  val grossWeight: BigDecimal,
  val tunch: BigDecimal,
  val fineWeight: BigDecimal,
  val rate: BigDecimal,
  val rateUnit: RateUnit = RateUnit.PER_GRAM,
  val gstPercent: BigDecimal = BigDecimal.ZERO,
  val gstAmount: BigDecimal = BigDecimal.ZERO,
  val totalAmount: BigDecimal,
  val amountPaid: BigDecimal = totalAmount,
  val balancePending: BigDecimal = BigDecimal.ZERO,
  val paymentMode: PaymentMode = PaymentMode.BANK_TRANSFER,
  val status: TransactionStatus = TransactionStatus.COMPLETED,
  val notes: String = "",
  val createdAt: Long = timestamp,
) {
  val billNumber: String
    get() = purchaseId

  val metalType: MetalType
    get() = metal

  val pendingAmount: BigDecimal
    get() = balancePending
}

data class SaleRecord(
  val saleId: String,
  val invoiceNumber: String,
  val date: String,
  val time: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val customerId: String,
  val customerName: String,
  val customerMobile: String = "",
  val metal: MetalType,
  val description: String,
  val grossWeight: BigDecimal,
  val tunch: BigDecimal,
  val fineWeight: BigDecimal,
  val rate: BigDecimal,
  val rateUnit: RateUnit = RateUnit.PER_GRAM,
  val makingCharges: BigDecimal = BigDecimal.ZERO,
  val gstPercent: BigDecimal = BigDecimal.ZERO,
  val cgstAmount: BigDecimal = BigDecimal.ZERO,
  val sgstAmount: BigDecimal = BigDecimal.ZERO,
  val igstAmount: BigDecimal = BigDecimal.ZERO,
  val totalAmount: BigDecimal,
  val amountReceived: BigDecimal = totalAmount,
  val balancePending: BigDecimal = BigDecimal.ZERO,
  val paymentMode: PaymentMode = PaymentMode.CASH,
  val status: TransactionStatus = TransactionStatus.COMPLETED,
  val notes: String = "",
  val createdAt: Long = timestamp,
) {
  val metalType: MetalType
    get() = metal

  val gstAmount: BigDecimal
    get() = cgstAmount.add(sgstAmount).add(igstAmount)

  val pendingAmount: BigDecimal
    get() = balancePending
}

data class ScrapProcessRecord(
  val processId: String,
  val date: String,
  val timestamp: Long = System.currentTimeMillis(),
  val metal: MetalType,
  val scrapGrossWeight: BigDecimal,
  val scrapFineWeight: BigDecimal,
  val meltedGrossWeight: BigDecimal,
  val outputTunch: BigDecimal,
  val outputFineWeight: BigDecimal,
  val meltingLossGrossWeight: BigDecimal,
  val meltingLossFineWeight: BigDecimal,
  val referenceRatePerGram: BigDecimal = BigDecimal.ZERO,
  val notes: String = "",
)

typealias ScrapProcessingRecord = ScrapProcessRecord

data class StockReconciliationRecord(
  val reconciliationId: String,
  val date: String,
  val timestamp: Long = System.currentTimeMillis(),
  val metal: MetalType,
  val systemGrossWeight: BigDecimal,
  val systemFineWeight: BigDecimal,
  val physicalGrossWeight: BigDecimal,
  val physicalFineWeight: BigDecimal,
  val differenceGrossWeight: BigDecimal,
  val differenceFineWeight: BigDecimal,
  val adjustmentMovementId: String = "",
  val notes: String = "",
)

typealias InventoryReconciliationRecord = StockReconciliationRecord

data class DailyReportData(
  val date: String,
  val totalTransactionsCount: Int = 0,
  val totalPurchasesCount: Int = 0,
  val totalSalesCount: Int = 0,
  val moneyReceived: BigDecimal = BigDecimal.ZERO,
  val moneyPaid: BigDecimal = BigDecimal.ZERO,
  val goldReceivedFineGrams: BigDecimal = BigDecimal.ZERO,
  val goldGivenFineGrams: BigDecimal = BigDecimal.ZERO,
  val silverReceivedFineGrams: BigDecimal = BigDecimal.ZERO,
  val silverGivenFineGrams: BigDecimal = BigDecimal.ZERO,
  val scrapGoldFineGrams: BigDecimal = BigDecimal.ZERO,
  val scrapSilverFineGrams: BigDecimal = BigDecimal.ZERO,
  val goldPurchasedFineGrams: BigDecimal = BigDecimal.ZERO,
  val silverPurchasedFineGrams: BigDecimal = BigDecimal.ZERO,
  val goldSoldFineGrams: BigDecimal = BigDecimal.ZERO,
  val silverSoldFineGrams: BigDecimal = BigDecimal.ZERO,
  val currentGoldStock: MetalInventorySummary = MetalInventorySummary(MetalType.GOLD),
  val currentSilverStock: MetalInventorySummary = MetalInventorySummary(MetalType.SILVER),
  val pendingCustomerMoney: BigDecimal = BigDecimal.ZERO,
)

data class MonthlyReportData(
  val yearMonth: String,
  val totalTransactionsCount: Int = 0,
  val totalPurchasesCount: Int = 0,
  val totalSalesCount: Int = 0,
  val totalMoneyInflow: BigDecimal = BigDecimal.ZERO,
  val totalMoneyOutflow: BigDecimal = BigDecimal.ZERO,
  val netCashFlow: BigDecimal = BigDecimal.ZERO,
  val totalGoldInFineGrams: BigDecimal = BigDecimal.ZERO,
  val totalGoldOutFineGrams: BigDecimal = BigDecimal.ZERO,
  val netGoldChangeFineGrams: BigDecimal = BigDecimal.ZERO,
  val totalSilverInFineGrams: BigDecimal = BigDecimal.ZERO,
  val totalSilverOutFineGrams: BigDecimal = BigDecimal.ZERO,
  val netSilverChangeFineGrams: BigDecimal = BigDecimal.ZERO,
  val totalMakingChargesEarned: BigDecimal = BigDecimal.ZERO,
  val totalGstCollected: BigDecimal = BigDecimal.ZERO,
  val estimatedNetValueCreated: BigDecimal = BigDecimal.ZERO,
)

// ==================== STAGE 6: SECURITY, AUDIT, REMINDERS, GLOBAL SEARCH & HEALTH ====================

enum class UserAccessMode(val displayName: String, val hindiName: String) {
  OWNER("Owner Mode (Full Access)", "मालिक मोड (पूर्ण अधिकार)"),
  STAFF("Staff Mode (Counter Entry)", "स्टाफ मोड (सीमित अधिकार)"),
}

data class AppSecuritySettings(
  val pinEnabled: Boolean = false,
  val pinCode: String = "1234",
  val lockTimeoutMinutes: Int = 5,
  val accessMode: UserAccessMode = UserAccessMode.OWNER,
  val staffCanCancelTransactions: Boolean = false,
  val staffCanAdjustInventory: Boolean = false,
  val staffCanExportBackup: Boolean = false,
  val staffCanEditSettings: Boolean = false,
  val requireConfirmationForCancel: Boolean = true,
  val requireReasonForCancel: Boolean = true,
  val requireReasonForAdjustment: Boolean = true,
  val lastLocalBackupTimestamp: Long = 0L,
  val lastDriveBackupTimestamp: Long = 0L,
  val lastBackupFileName: String = "",
)

typealias SecuritySettings = AppSecuritySettings

enum class AuditActionType(val displayName: String) {
  TRANSACTION_CANCELLED("Transaction Cancelled"),
  INVOICE_CANCELLED("Invoice Cancelled"),
  INVENTORY_ADJUSTED("Inventory Adjusted"),
  PURCHASE_CREATED("Purchase Recorded"),
  SALE_CREATED("Sale Recorded"),
  SECURITY_CHANGED("Security Changed"),
  BACKUP_CREATED("Backup Created"),
  DATA_RESTORED("Data Restored"),
}

data class AuditLogEntry(
  val actionId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val date: String,
  val time: String,
  val actionType: AuditActionType,
  val entityId: String,
  val description: String,
  val performedBy: String = "Owner",
  val reason: String = "",
) {
  val auditId: String
    get() = actionId

  val actorName: String
    get() = performedBy

  val actorRole: UserAccessMode
    get() = UserAccessMode.OWNER

  val summary: String
    get() = if (reason.isNotBlank()) "$description (Reason: $reason)" else description
}

enum class ReminderType(val displayName: String) {
  PENDING_PAYMENT("Customer Payment"),
  CUSTOMER_PAYMENT("Customer Payment"),
  CUSTOMER_FOLLOW_UP("Customer Follow-up"),
  VENDOR_PAYMENT("Vendor Payment"),
  PURCHASE("Purchase"),
  STOCK_PURCHASE("Stock Purchase"),
  GOLD_DELIVERY("Gold Balance / Delivery"),
  SILVER_DELIVERY("Silver Balance / Delivery"),
  FOLLOW_UP("Customer Follow-up"),
  OTHER("Other");

  companion object {
    val PENDING_MONEY = PENDING_PAYMENT
  }
}

enum class ReminderStatus(val displayName: String) {
  PENDING("Pending"),
  COMPLETED("Completed"),
  CANCELLED("Cancelled");

  companion object {
    fun fromString(raw: String, fallbackCompleted: Boolean = false): ReminderStatus {
      val clean = raw.trim().uppercase()
      return entries.find { it.name == clean || it.displayName.uppercase() == clean }
        ?: if (fallbackCompleted) COMPLETED else PENDING
    }
  }
}

data class CustomerReminder(
  val reminderId: String,
  val customerId: String = "",
  val customerName: String = "",
  val customerMobile: String = "",
  val vendorId: String = "",
  val vendorName: String = "",
  val title: String = "",
  val description: String = "",
  val reminderType: ReminderType = ReminderType.PENDING_PAYMENT,
  val dueDate: String,
  val pendingAmount: BigDecimal = BigDecimal.ZERO,
  val pendingMetalFineGrams: BigDecimal = BigDecimal.ZERO,
  val notes: String = "",
  val isCompleted: Boolean = false,
  val status: ReminderStatus = if (isCompleted) ReminderStatus.COMPLETED else ReminderStatus.PENDING,
  val createdAt: Long = System.currentTimeMillis(),
) {
  val effectiveTitle: String
    get() =
      title.ifBlank {
        when {
          customerName.isNotBlank() -> "${reminderType.displayName} — $customerName"
          vendorName.isNotBlank() -> "${reminderType.displayName} — $vendorName"
          else -> reminderType.displayName
        }
      }

  val effectiveDescription: String
    get() = description.ifBlank { notes }
}

typealias ShopReminder = CustomerReminder

data class GlobalSearchResults(
  val query: String = "",
  val customers: List<Customer> = emptyList(),
  val vendors: List<Vendor> = emptyList(),
  val transactions: List<Transaction> = emptyList(),
  val invoices: List<Invoice> = emptyList(),
  val purchases: List<PurchaseRecord> = emptyList(),
  val sales: List<SaleRecord> = emptyList(),
) {
  val totalCount: Int
    get() =
      customers.size +
        vendors.size +
        transactions.size +
        invoices.size +
        purchases.size +
        sales.size

  val totalMatches: Int
    get() = totalCount
}

data class ApplicationHealthStatus(
  val googleAccountConnected: Boolean = false,
  val googleSheetsConnected: Boolean = false,
  val googleDriveConnected: Boolean = false,
  val lastSyncTimestamp: Long = 0L,
  val pendingSyncItemsCount: Int = 0,
  val pendingInvoiceUploadsCount: Int = 0,
  val lastBackupTimestamp: Long = 0L,
  val lastBackupFileName: String = "",
  val appVersion: String = "6.0.0 (Stage 6 Production)",
  val localDatabaseRecordCount: Int = 0,
  val integrityIssuesCount: Int = 0,
  val integritySummary: String = "All records, invoice numbers, and inventory balances verified.",
)
