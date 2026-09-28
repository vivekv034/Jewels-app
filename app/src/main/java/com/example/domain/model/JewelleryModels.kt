package com.example.domain.model

import java.math.BigDecimal
import java.math.RoundingMode

enum class AppLanguage(val code: String, val displayName: String, val nativeName: String = displayName) {
  ENGLISH("en", "English", "English"),
  HINDI("hi", "हिन्दी (Hindi)", "हिन्दी"),
}

enum class MetalType(val displayName: String, val symbol: String) {
  GOLD("Gold", "Au"),
  SILVER("Silver", "Ag"),
}

enum class PuritySelectionMode(val displayName: String, val defaultTunch: BigDecimal) {
  PURE_99("99% Pure", BigDecimal("99.00")),
  CUSTOM_TUNCH("Custom Tunch", BigDecimal("91.60")),
}

enum class RateUnit(val displayName: String, val shortLabel: String, val gramsInUnit: Int) {
  PER_GRAM("Per 1 Gram (1g)", "/g", 1),
  PER_10_GRAMS("Per 10 Grams (10g)", "/10g", 10),
  PER_KG("Per 1 Kilogram (1kg)", "/kg", 1000);

  val shortUnit: String
    get() = shortLabel

  val divisorGrams: BigDecimal
    get() = BigDecimal(gramsInUnit)

  companion object {
    val PER_1_GRAM = PER_GRAM
    val PER_KILOGRAM = PER_KG

    fun fromString(value: String): RateUnit {
      val clean = value.trim()
      return entries.find {
        it.name.equals(clean, ignoreCase = true) ||
          it.displayName.equals(clean, ignoreCase = true) ||
          it.shortLabel.equals(clean, ignoreCase = true)
      } ?: PER_GRAM
    }
  }
}

enum class DeductionType(val displayName: String, val shortLabel: String) {
  NONE("No Deduction", "None"),
  AMOUNT_DEDUCTION("Amount Deduction (₹)", "₹ Deduction"),
  PERCENTAGE_DEDUCTION("Percentage Deduction (%)", "% Deduction"),
  WEIGHT_DEDUCTION("Weight Deduction (g)", "g Deduction"),
  FIXED_AMOUNT("Fixed Amount (₹)", "₹ Fixed"),
  PERCENTAGE("Percentage (%)", "%"),
  WEIGHT("Weight (g)", "g"),
  CUSTOM("Custom Deduction", "Custom"),
}

enum class PaymentMode(val displayName: String) {
  CASH("Cash"),
  UPI("UPI"),
  BANK_TRANSFER("Bank Transfer"),
  METAL_EXCHANGE("Metal Exchange"),
  ADJUSTMENT("Account / Metal Settlement"),
  PENDING_BAKAYA("Pending / Bakaya"),
}

enum class TransactionType(
  val title: String,
  val subtitle: String,
  val shortLabel: String,
  val metalType: MetalType,
) {
  MONEY_TO_GOLD(
    title = "Money -> Gold",
    subtitle = "Convert customer money into fine gold weight",
    shortLabel = "Money to Gold",
    metalType = MetalType.GOLD,
  ),
  MONEY_TO_SILVER(
    title = "Money -> Silver",
    subtitle = "Convert customer money into fine silver weight",
    shortLabel = "Money to Silver",
    metalType = MetalType.SILVER,
  ),
  GOLD_PAYMENT(
    title = "Gold Received",
    subtitle = "Customer pays or deposits gold (99% or Custom Tunch)",
    shortLabel = "Gold Received",
    metalType = MetalType.GOLD,
  ),
  SILVER_PAYMENT(
    title = "Silver Received",
    subtitle = "Customer pays or deposits silver by custom Tunch",
    shortLabel = "Silver Received",
    metalType = MetalType.SILVER,
  ),
  SCRAP_GOLD(
    title = "Scrap Gold",
    subtitle = "Old / scrap gold exchange with Tunch & optional deduction",
    shortLabel = "Scrap Gold",
    metalType = MetalType.GOLD,
  ),
  SCRAP_SILVER(
    title = "Scrap Silver",
    subtitle = "Old / scrap silver exchange with Tunch & optional deduction",
    shortLabel = "Scrap Silver",
    metalType = MetalType.SILVER,
  ),
  GOLD_ADJUSTMENT(
    title = "Gold Given / Settlement",
    subtitle = "Record gold given to customer or settlement entry",
    shortLabel = "Gold Settlement",
    metalType = MetalType.GOLD,
  ),
  SILVER_ADJUSTMENT(
    title = "Silver Given / Settlement",
    subtitle = "Record silver given to customer or settlement entry",
    shortLabel = "Silver Settlement",
    metalType = MetalType.SILVER,
  );

  val code: String
    get() = ('A' + ordinal).toString()
}

enum class TransactionStatus(val displayName: String) {
  DRAFT("Draft"),
  COMPLETED("Completed"),
  PENDING_SYNC("Pending Sync"),
  CANCELLED("Cancelled"),
}

enum class SyncStatus(val displayName: String) {
  SYNCED("Synced"),
  PENDING_SYNC("Sync Pending"),
  SYNCING("Syncing..."),
  SYNC_FAILED("Sync Failed"),
  LOCAL_ONLY("Local Only"),
  SYNC_ERROR("Sync Error"),
}

enum class GoogleConnectionStatus(val displayName: String) {
  DISCONNECTED("Not Connected"),
  CONNECTING("Connecting..."),
  CONNECTED("Connected"),
  ERROR("Connection Error"),
}

enum class LogoPosition(val displayName: String) {
  LEFT("Left"),
  CENTER("Center"),
  RIGHT("Right"),
  HIDDEN("Hidden"),
}

enum class InvoiceStatus(val displayName: String) {
  DRAFT("Draft"),
  GENERATED("Generated"),
  COMPLETED("Completed"),
  PDF_GENERATED("PDF Generated"),
  PDF_READY("PDF Ready"),
  DRIVE_SAVED("Saved to Drive"),
  PENDING_UPLOAD("Pending Drive Upload"),
  CANCELLED("Cancelled");

  companion object {
    fun fromString(raw: String): InvoiceStatus {
      val clean = raw.trim().uppercase()
      return when (clean) {
        "DRAFT" -> DRAFT
        "GENERATED" -> GENERATED
        "COMPLETED", "PAID" -> COMPLETED
        "PDF_GENERATED", "PDF_READY" -> PDF_GENERATED
        "DRIVE_SAVED", "UPLOADED" -> DRIVE_SAVED
        "PENDING_UPLOAD" -> PENDING_UPLOAD
        "CANCELLED" -> CANCELLED
        else -> entries.find { it.name == clean } ?: COMPLETED
      }
    }
  }
}

enum class InvoicePrintFormat(
  val displayName: String,
  val description: String,
  val pageWidthPt: Int,
  val pageHeightPt: Int,
) {
  A4(
    displayName = "A4 Full Invoice",
    description = "Standard A4 Tax / Estimate Invoice (595 × 842 pt)",
    pageWidthPt = 595,
    pageHeightPt = 842,
  ),
  THERMAL_80MM(
    displayName = "Compact Slip (80mm)",
    description = "Counter Thermal Receipt Slip (226 × 620 pt)",
    pageWidthPt = 226,
    pageHeightPt = 620,
  );

  companion object {
    val A4_FULL = A4
    val COMPACT_SLIP = THERMAL_80MM
  }
}

enum class InvoiceTypeFilter(val displayName: String) {
  ALL("All Invoices"),
  GOLD("Gold Only"),
  SILVER("Silver Only"),
  SCRAP("Scrap Only"),
  MONEY_TO_GOLD("Money → Gold"),
  MONEY_TO_SILVER("Money → Silver"),
}

enum class ReportDateFilter(val displayName: String) {
  TODAY("Today"),
  YESTERDAY("Yesterday"),
  THIS_WEEK("Last 7 Days"),
  THIS_MONTH("This Month"),
  ALL_TIME("All Time"),
  CUSTOM_RANGE("Custom Range"),
}

enum class DriveUploadStatus(val displayName: String) {
  PENDING("Pending Drive Upload"),
  UPLOADING("Uploading..."),
  UPLOADED("Saved to Drive"),
  FAILED("Upload Failed"),
}

enum class LedgerEffectCategory(val displayName: String) {
  CUSTOMER_PAYS_MONEY("Customer Pays Money"),
  CUSTOMER_RECEIVES_MONEY("Customer Receives Money"),
  CUSTOMER_GIVES_GOLD("Customer Gives Gold"),
  CUSTOMER_RECEIVES_GOLD("Customer Receives Gold"),
  CUSTOMER_GIVES_SILVER("Customer Gives Silver"),
  CUSTOMER_RECEIVES_SILVER("Customer Receives Silver"),
}

data class CustomerLedgerEntry(
  val entryId: String = "",
  val transactionId: String = "",
  val invoiceNumber: String = "",
  val date: String = "",
  val time: String = "",
  val timestamp: Long = 0L,
  val typeLabel: String = "",
  val description: String = "",
  val transactionType: TransactionType = TransactionType.MONEY_TO_GOLD,
  val status: TransactionStatus = TransactionStatus.COMPLETED,
  val effects: List<LedgerEffectCategory> = emptyList(),
  val moneyPaidByCustomer: BigDecimal = BigDecimal.ZERO,
  val moneyReceivedByCustomer: BigDecimal = BigDecimal.ZERO,
  val goldGivenByCustomerGrams: BigDecimal = BigDecimal.ZERO,
  val goldReceivedByCustomerGrams: BigDecimal = BigDecimal.ZERO,
  val silverGivenByCustomerGrams: BigDecimal = BigDecimal.ZERO,
  val silverReceivedByCustomerGrams: BigDecimal = BigDecimal.ZERO,
  val moneyIn: BigDecimal = moneyPaidByCustomer,
  val moneyOut: BigDecimal = moneyReceivedByCustomer,
  val goldInFineGrams: BigDecimal = goldGivenByCustomerGrams,
  val goldOutFineGrams: BigDecimal = goldReceivedByCustomerGrams,
  val silverInFineGrams: BigDecimal = silverGivenByCustomerGrams,
  val silverOutFineGrams: BigDecimal = silverReceivedByCustomerGrams,
  val isCancelled: Boolean = status == TransactionStatus.CANCELLED,
  val notes: String = "",
) {
  val moneyDebit: BigDecimal
    get() = moneyReceivedByCustomer

  val moneyCredit: BigDecimal
    get() = moneyPaidByCustomer

  val goldDebit: BigDecimal
    get() = goldReceivedByCustomerGrams

  val goldCredit: BigDecimal
    get() = goldGivenByCustomerGrams

  val silverDebit: BigDecimal
    get() = silverReceivedByCustomerGrams

  val silverCredit: BigDecimal
    get() = silverGivenByCustomerGrams
}

data class CustomerLedgerSummary(
  val customerId: String,
  val customerName: String = "",
  val customerMobile: String = "",
  val totalTransactionsCount: Int = 0,
  val transactions: List<Transaction> = emptyList(),
  val entries: List<CustomerLedgerEntry> = emptyList(),
  val goldGivenGrams: BigDecimal = BigDecimal.ZERO,
  val goldReceivedGrams: BigDecimal = BigDecimal.ZERO,
  val silverGivenGrams: BigDecimal = BigDecimal.ZERO,
  val silverReceivedGrams: BigDecimal = BigDecimal.ZERO,
  val scrapGoldGrams: BigDecimal = BigDecimal.ZERO,
  val scrapSilverGrams: BigDecimal = BigDecimal.ZERO,
  val moneyReceived: BigDecimal = BigDecimal.ZERO,
  val netGoldBalanceGrams: BigDecimal = BigDecimal.ZERO,
  val netSilverBalanceGrams: BigDecimal = BigDecimal.ZERO,
  val netMoneyBalance: BigDecimal = BigDecimal.ZERO,
  val totalMoneyPaidByCustomer: BigDecimal = moneyReceived,
  val totalMoneyReceivedByCustomer: BigDecimal = BigDecimal.ZERO,
  val totalMoneyPaidToCustomer: BigDecimal = totalMoneyReceivedByCustomer,
  val netMoneyFlow: BigDecimal = netMoneyBalance,
  val netMoneyReceivedFromCustomer: BigDecimal = netMoneyBalance,
  val totalGoldGivenByCustomerGrams: BigDecimal = goldReceivedGrams,
  val totalGoldReceivedByCustomerGrams: BigDecimal = goldGivenGrams,
  val totalGoldReceivedGrossGrams: BigDecimal = goldReceivedGrams,
  val totalGoldReceivedFineGrams: BigDecimal = goldReceivedGrams,
  val totalGoldGivenFineGrams: BigDecimal = goldGivenGrams,
  val netGoldGrams: BigDecimal = netGoldBalanceGrams,
  val netGoldBalanceFineGrams: BigDecimal = netGoldBalanceGrams,
  val totalSilverGivenByCustomerGrams: BigDecimal = silverReceivedGrams,
  val totalSilverReceivedByCustomerGrams: BigDecimal = silverGivenGrams,
  val totalSilverReceivedGrossGrams: BigDecimal = silverReceivedGrams,
  val totalSilverReceivedFineGrams: BigDecimal = silverReceivedGrams,
  val totalSilverGivenFineGrams: BigDecimal = silverGivenGrams,
  val netSilverGrams: BigDecimal = netSilverBalanceGrams,
  val netSilverBalanceFineGrams: BigDecimal = netSilverBalanceGrams,
  val totalScrapGoldGrossGrams: BigDecimal = scrapGoldGrams,
  val totalScrapGoldFineGrams: BigDecimal = scrapGoldGrams,
  val totalScrapSilverGrossGrams: BigDecimal = scrapSilverGrams,
  val totalScrapSilverFineGrams: BigDecimal = scrapSilverGrams,
  val totalGrossWeightGrams: BigDecimal = BigDecimal.ZERO,
  val totalFineWeightGrams: BigDecimal = BigDecimal.ZERO,
  val totalTransactionValue: BigDecimal = BigDecimal.ZERO,
  val pendingMoneyBalance: BigDecimal = BigDecimal.ZERO,
  val runningPendingBalance: BigDecimal = pendingMoneyBalance,
  val lastTransactionDate: String = "",
) {
  val totalMoneyReceived: BigDecimal
    get() = totalMoneyPaidByCustomer.max(moneyReceived)

  val totalMoneyPaid: BigDecimal
    get() = totalMoneyPaidByCustomer.max(moneyReceived)
}

data class TransactionItem(
  val id: String = "",
  val transactionId: String = "",
  val itemName: String = "",
  val description: String = "",
  val metalType: MetalType = MetalType.GOLD,
  val grossWeight: BigDecimal = BigDecimal.ZERO,
  val tunch: BigDecimal = BigDecimal("100.00"),
  val purity: String = "100.00%",
  val fineWeight: BigDecimal = BigDecimal.ZERO,
  val rate: BigDecimal = BigDecimal.ZERO,
  val rateUnit: RateUnit = RateUnit.PER_GRAM,
  val metalValue: BigDecimal = BigDecimal.ZERO,
  val deductionAmount: BigDecimal = BigDecimal.ZERO,
  val amount: BigDecimal = BigDecimal.ZERO,
)

data class BusinessProfile(
  val businessId: String = "BIZ-001",
  val shopName: String = "Shree Swarnam Palace Jewellers",
  val ownerName: String = "Rajeshwar Soni",
  val mobileNumber: String = "9820112233",
  val whatsappNumber: String = "9820112233",
  val email: String = "contact@swarnampalace.in",
  val address: String = "142 Zaveri Bazaar, Kalbadevi Road, Mumbai 400002",
  val city: String = "Mumbai",
  val district: String = "Mumbai City",
  val state: String = "Maharashtra",
  val pinCode: String = "400002",
  val panNumber: String = "AAECS4912F",
  val gstNumber: String = "27AAECS4912F1Z9",
  val bankName: String = "HDFC Bank - Zaveri Bazaar Branch",
  val branchName: String = "Zaveri Bazaar Branch",
  val bankAccountNumber: String = "50200048291044",
  val ifsc: String = "HDFC0000142",
  val upiId: String = "swarnampalace@hdfcbank",
  val invoicePrefix: String = "HGR-",
  val invoiceStartingNumber: Int = 1,
  val weightDecimalPlaces: Int = 3,
  val tunchDecimalPlaces: Int = 2,
  val moneyDecimalPlaces: Int = 2,
  val purityDivisor: BigDecimal = BigDecimal("100"),
  val maxTunchValue: BigDecimal = BigDecimal("100.00"),
  val allowEditingPure99Purity: Boolean = true,
  val enabledDeduction: Boolean = false,
  val enabledDeductionTypes: Set<DeductionType> = DeductionType.entries.toSet(),
  val businessLogoUri: String = "",
  val logoPosition: LogoPosition = LogoPosition.LEFT,
  val invoiceFooter: String = "Thank you for your patronage! Hallmark of Trust & Purity.",
  val termsAndConditions: String = "Please verify all details before leaving the premises.",
  val showBankDetailsOnInvoice: Boolean = true,
  val showUpiOnInvoice: Boolean = true,
  val showPanOnInvoice: Boolean = true,
  val showGstOnInvoice: Boolean = true,
  val showCustomerPanOnInvoice: Boolean = true,
  val showCustomerGstOnInvoice: Boolean = true,
  val showCustomerBalanceOnInvoice: Boolean = true,
  val gstEnabled: Boolean = false,
  val defaultGstRatePercent: BigDecimal = BigDecimal("3.00"),
  val cgstRatePercent: BigDecimal = BigDecimal("1.50"),
  val sgstRatePercent: BigDecimal = BigDecimal("1.50"),
  val igstRatePercent: BigDecimal = BigDecimal("0.00"),
  val gstRegistrationType: String = "Regular GST Dealer",
  val gstStateName: String = "Maharashtra (27)",
  val gstTaxTreatment: String = "INTRA_STATE_CGST_SGST",
  val gstApplyToGold: Boolean = true,
  val gstApplyToSilver: Boolean = true,
  val gstApplyToMakingCharges: Boolean = true,
  val defaultInvoicePaperSize: InvoicePrintFormat = InvoicePrintFormat.A4,
  val fastDashboardLoading: Boolean = true,
  val compactTransactionRows: Boolean = false,
  val language: AppLanguage = AppLanguage.ENGLISH,
  val createdAt: Long = 1711929600000L,
  val updatedAt: Long = System.currentTimeMillis(),
  val updatedBy: String = "Owner",
  val status: String = "ACTIVE",
) {
  val businessProfileId: String
    get() = businessId

  val businessName: String
    get() = shopName

  val mobile: String
    get() = mobileNumber

  val whatsapp: String
    get() = whatsappNumber.ifBlank { mobileNumber }

  val pin: String
    get() = pinCode

  val pan: String
    get() = panNumber

  val gstin: String
    get() = gstNumber

  val branch: String
    get() = branchName

  val ifscCode: String
    get() = ifsc

  val startingInvoiceNumber: Int
    get() = invoiceStartingNumber

  fun formattedFullAddress(): String {
    val base = address.trim()
    val extraParts = mutableListOf<String>()
    if (city.isNotBlank() && !base.contains(city.trim(), ignoreCase = true)) {
      extraParts.add(city.trim())
    }
    if (
      district.isNotBlank() &&
        !district.equals(city.trim(), ignoreCase = true) &&
        !base.contains(district.trim(), ignoreCase = true)
    ) {
      extraParts.add(district.trim())
    }
    if (state.isNotBlank() && !base.contains(state.trim(), ignoreCase = true)) {
      extraParts.add(state.trim())
    }
    if (pinCode.isNotBlank() && !base.contains(pinCode.trim(), ignoreCase = true)) {
      extraParts.add(pinCode.trim())
    }
    return listOf(base).filter { it.isNotBlank() }.plus(extraParts).joinToString(", ")
  }

  fun formattedBankNameWithBranch(): String {
    val bName = bankName.trim()
    val brName = branchName.trim()
    return if (brName.isNotBlank() && !bName.contains(brName, ignoreCase = true)) {
      if (bName.isNotBlank()) "$bName - $brName" else brName
    } else {
      bName
    }
  }
}

data class Customer(
  val id: String,
  val name: String,
  val mobileNumber: String,
  val whatsappNumber: String = "",
  val address: String = "",
  val city: String = "",
  val state: String = "",
  val pinCode: String = "",
  val panNumber: String = "",
  val gstNumber: String = "",
  val email: String = "",
  val pendingAmount: BigDecimal = BigDecimal.ZERO,
  val notes: String = "",
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = createdAt,
) {
  val mobile: String
    get() = mobileNumber

  val whatsapp: String
    get() = whatsappNumber.ifBlank { mobileNumber }

  val pin: String
    get() = pinCode

  val pan: String
    get() = panNumber

  val gstin: String
    get() = gstNumber

  val formattedFullAddress: String
    get() {
      val base = address.trim()
      val extraParts = mutableListOf<String>()
      if (city.isNotBlank() && !base.contains(city.trim(), ignoreCase = true)) {
        extraParts.add(city.trim())
      }
      if (state.isNotBlank() && !base.contains(state.trim(), ignoreCase = true)) {
        extraParts.add(state.trim())
      }
      if (pinCode.isNotBlank() && !base.contains(pinCode.trim(), ignoreCase = true)) {
        extraParts.add(pinCode.trim())
      }
      return listOf(base).filter { it.isNotBlank() }.plus(extraParts).joinToString(", ")
    }
}

data class MetalRate(
  val rateId: String = "RATE-DEFAULT",
  val goldRate: BigDecimal = BigDecimal("7450.00"),
  val goldRateUnit: RateUnit = RateUnit.PER_GRAM,
  val silverRate: BigDecimal = BigDecimal("92.50"),
  val silverRateUnit: RateUnit = RateUnit.PER_GRAM,
  val enteredBy: String = "Owner",
  val updatedAt: Long = System.currentTimeMillis(),
) {
  val goldRatePerGram: BigDecimal
    get() =
      if (goldRateUnit.gramsInUnit > 1) {
        goldRate.divide(BigDecimal(goldRateUnit.gramsInUnit), 2, RoundingMode.HALF_UP)
      } else {
        goldRate
      }

  val silverRatePerGram: BigDecimal
    get() =
      if (silverRateUnit.gramsInUnit > 1) {
        silverRate.divide(BigDecimal(silverRateUnit.gramsInUnit), 2, RoundingMode.HALF_UP)
      } else {
        silverRate
      }
}

data class Transaction(
  val transactionId: String,
  val date: String,
  val time: String,
  val timestamp: Long,
  val customerId: String,
  val customerName: String,
  val customerMobile: String,
  val transactionType: TransactionType,
  val metalType: MetalType,
  val purityMode: PuritySelectionMode = PuritySelectionMode.CUSTOM_TUNCH,
  val purity: String = purityMode.displayName,
  val grossWeight: BigDecimal,
  val tunch: BigDecimal,
  val fineWeight: BigDecimal,
  val rate: BigDecimal,
  val rateUnit: RateUnit = RateUnit.PER_GRAM,
  val normalizedRatePerGram: BigDecimal =
    if (rateUnit.gramsInUnit > 0 && rate > BigDecimal.ZERO) {
      rate.divide(BigDecimal(rateUnit.gramsInUnit), 2, RoundingMode.HALF_UP)
    } else {
      rate
    },
  val metalValue: BigDecimal = BigDecimal.ZERO,
  val deductionType: DeductionType = DeductionType.NONE,
  val deductionValue: BigDecimal = BigDecimal.ZERO,
  val deductionInput: BigDecimal = deductionValue,
  val deductionAmount: BigDecimal = BigDecimal.ZERO,
  val deductions: BigDecimal = deductionAmount,
  val paymentAdjustment: BigDecimal = BigDecimal.ZERO,
  val cashPaid: BigDecimal = BigDecimal.ZERO,
  val cashReceived: BigDecimal = BigDecimal.ZERO,
  val remainingBalance: BigDecimal = BigDecimal.ZERO,
  val amount: BigDecimal,
  val netValue: BigDecimal = amount,
  val cashAmount: BigDecimal = amount,
  val paymentMode: PaymentMode = PaymentMode.CASH,
  val notes: String = "",
  val invoiceNumber: String = "",
  val status: TransactionStatus = TransactionStatus.COMPLETED,
  val syncStatus: SyncStatus = SyncStatus.SYNCED,
  val cancelledAt: Long = 0L,
  val cancelledBy: String = "",
  val cancellationReason: String = "",
  val parentTransactionId: String = "",
  val items: List<TransactionItem> = emptyList(),
  val createdAt: Long = timestamp,
  val updatedAt: Long = timestamp,
) {
  val effectiveMetalValue: BigDecimal
    get() = if (metalValue > BigDecimal.ZERO) metalValue else amount.add(deductions)

  val isCancelled: Boolean
    get() = status == TransactionStatus.CANCELLED

  val isDraft: Boolean
    get() = status == TransactionStatus.DRAFT
}

data class Invoice(
  val invoiceId: String = "",
  val invoiceNumber: String,
  val transactionId: String,
  val date: String,
  val time: String,
  val timestamp: Long,
  val shopName: String,
  val ownerName: String = "",
  val shopMobile: String,
  val shopAddress: String,
  val shopPan: String = "",
  val shopGst: String,
  val bankName: String = "",
  val bankAccountNumber: String = "",
  val ifsc: String = "",
  val upiId: String = "",
  val customerId: String,
  val customerName: String,
  val customerMobile: String,
  val customerAddress: String,
  val customerPan: String,
  val customerGst: String,
  val transactionType: TransactionType,
  val metalType: MetalType = transactionType.metalType,
  val purityMode: PuritySelectionMode = PuritySelectionMode.CUSTOM_TUNCH,
  val grossWeight: BigDecimal,
  val tunch: BigDecimal,
  val fineWeight: BigDecimal,
  val rate: BigDecimal,
  val rateUnit: RateUnit,
  val amount: BigDecimal,
  val subtotal: BigDecimal = amount,
  val deductions: BigDecimal = BigDecimal.ZERO,
  val netAmount: BigDecimal = amount.subtract(deductions).max(BigDecimal.ZERO),
  val taxableAmount: BigDecimal = netAmount,
  val gstEnabled: Boolean = false,
  val cgstPercent: BigDecimal = BigDecimal.ZERO,
  val sgstPercent: BigDecimal = BigDecimal.ZERO,
  val igstPercent: BigDecimal = BigDecimal.ZERO,
  val cgstAmount: BigDecimal = BigDecimal.ZERO,
  val sgstAmount: BigDecimal = BigDecimal.ZERO,
  val igstAmount: BigDecimal = BigDecimal.ZERO,
  val taxAmount: BigDecimal = BigDecimal.ZERO,
  val discount: BigDecimal = deductions,
  val paymentMode: PaymentMode,
  val amountReceived: BigDecimal = amount,
  val amountPending: BigDecimal = BigDecimal.ZERO,
  val previousMoneyBalance: BigDecimal = BigDecimal.ZERO,
  val previousGoldBalanceGrams: BigDecimal = BigDecimal.ZERO,
  val previousSilverBalanceGrams: BigDecimal = BigDecimal.ZERO,
  val afterMoneyBalance: BigDecimal = BigDecimal.ZERO,
  val afterGoldBalanceGrams: BigDecimal = BigDecimal.ZERO,
  val afterSilverBalanceGrams: BigDecimal = BigDecimal.ZERO,
  val showCustomerBalance: Boolean = false,
  val status: String = "COMPLETED",
  val invoiceStatus: InvoiceStatus = InvoiceStatus.fromString(status),
  val notes: String,
  val termsAndConditions: String = "Please verify all details before leaving the premises.",
  val invoiceFooter: String = "Thank you for your patronage! Hallmark of Trust & Purity.",
  val businessLogoUri: String = "",
  val logoPosition: LogoPosition = LogoPosition.LEFT,
  val showBankDetails: Boolean = true,
  val showUpi: Boolean = true,
  val showPan: Boolean = true,
  val showGst: Boolean = true,
  val showCustomerPan: Boolean = true,
  val showCustomerGst: Boolean = true,
  val items: List<TransactionItem> = emptyList(),
  val totalAmount: BigDecimal = amount,
  val localPdfPath: String = "",
  val pdfFileName: String = "Invoice_$invoiceNumber.pdf",
  val driveFileId: String = "",
  val driveFileName: String = "",
  val driveFileUrl: String = "",
  val driveSavedAt: Long = 0L,
  val createdAt: Long = timestamp,
  val updatedAt: Long = timestamp,
) {
  val isCancelled: Boolean
    get() =
      invoiceStatus == InvoiceStatus.CANCELLED ||
        status.equals("CANCELLED", ignoreCase = true)

  val isSavedToDrive: Boolean
    get() = driveFileId.isNotBlank() || invoiceStatus == InvoiceStatus.DRIVE_SAVED

  val hasPdfGenerated: Boolean
    get() =
      localPdfPath.isNotBlank() ||
        invoiceStatus == InvoiceStatus.PDF_GENERATED ||
        isSavedToDrive

  val totalFineWeight: BigDecimal
    get() = fineWeight

  val grandTotal: BigDecimal
    get() = totalAmount
}

data class PendingDriveUpload(
  val uploadId: String,
  val invoiceId: String,
  val invoiceNumber: String = "",
  val fileName: String,
  val localFileReference: String,
  val createdAt: Long = System.currentTimeMillis(),
  val retryCount: Int = 0,
  val lastError: String = "",
  val status: DriveUploadStatus = DriveUploadStatus.PENDING,
)

enum class SyncRecordType {
  BUSINESS_PROFILE,
  CUSTOMER,
  METAL_RATE,
  TRANSACTION,
  INVOICE,
  SETTING,
  DAILY_SUMMARY,
  OPENING_STOCK,
  INVENTORY_MOVEMENT,
  INVENTORY_RECONCILIATION,
  VENDOR,
  PURCHASE,
  SALE,
  SCRAP_PROCESSING,
  AUDIT_LOG,
  REMINDER,
}

enum class SyncOperation {
  CREATE,
  UPDATE,
  CANCEL,
}

data class PendingSyncRecord(
  val recordId: String,
  val recordType: SyncRecordType,
  val operation: SyncOperation,
  val payload: String,
  val createdAt: Long = System.currentTimeMillis(),
  val retryCount: Int = 0,
  val lastError: String = "",
  val syncStatus: SyncStatus = SyncStatus.PENDING_SYNC,
)

data class GoogleAccountState(
  val connectionStatus: GoogleConnectionStatus = GoogleConnectionStatus.DISCONNECTED,
  val connectedEmail: String = "",
  val displayName: String = "",
  val accessToken: String = "",
  val selectedSpreadsheetId: String = "",
  val selectedSpreadsheetName: String = "",
  val syncStatus: SyncStatus = SyncStatus.SYNCED,
  val lastSyncTimestamp: Long = 0L,
  val pendingSyncCount: Int = 0,
  val initializedWorksheets: List<String> = emptyList(),
  val isOfflineMode: Boolean = false,
  val driveConnected: Boolean = false,
  val driveRootFolderId: String = "",
  val driveInvoicesFolderId: String = "",
  val driveBackupsFolderId: String = "",
  val driveReportsFolderId: String = "",
  val driveInvoiceFolderPath: String = "Jewellery Business Manager / Invoices",
  val lastDriveSyncTimestamp: Long = 0L,
) {
  val isConnected: Boolean
    get() = connectionStatus == GoogleConnectionStatus.CONNECTED

  val hasDatabaseSelected: Boolean
    get() = selectedSpreadsheetId.isNotBlank()

  val driveFolderId: String
    get() = driveInvoicesFolderId.ifBlank { driveRootFolderId }

  val email: String
    get() = connectedEmail

  val accountName: String
    get() = displayName
}

data class SpreadsheetInfo(
  val spreadsheetId: String,
  val name: String,
  val createdAt: String = "",
  val worksheets: List<String> = emptyList(),
)

data class DailySummaryRecord(
  val date: String,
  val goldAmount: BigDecimal = BigDecimal.ZERO,
  val silverAmount: BigDecimal = BigDecimal.ZERO,
  val goldWeight: BigDecimal = BigDecimal.ZERO,
  val silverWeight: BigDecimal = BigDecimal.ZERO,
  val goldReceived: BigDecimal = BigDecimal.ZERO,
  val silverReceived: BigDecimal = BigDecimal.ZERO,
  val scrapGold: BigDecimal = BigDecimal.ZERO,
  val scrapSilver: BigDecimal = BigDecimal.ZERO,
  val totalTransactions: Int = 0,
  val totalAmount: BigDecimal = BigDecimal.ZERO,
  val updatedAt: Long = System.currentTimeMillis(),
)

data class DashboardSummary(
  val todayTotalTransactionsCount: Int = 0,
  val todayTotalTransactionsAmount: BigDecimal = BigDecimal.ZERO,
  val todayGoldTransactionsCount: Int = 0,
  val todaySilverTransactionsCount: Int = 0,
  val totalMoneyReceived: BigDecimal = BigDecimal.ZERO,
  val goldReceivedGrams: BigDecimal = BigDecimal.ZERO,
  val silverReceivedGrams: BigDecimal = BigDecimal.ZERO,
  val scrapGoldReceivedGrams: BigDecimal = BigDecimal.ZERO,
  val scrapSilverReceivedGrams: BigDecimal = BigDecimal.ZERO,
  val pendingCustomerAmount: BigDecimal = BigDecimal.ZERO,
)

object WorksheetSchemas {
  const val DEFAULT_DATABASE_NAME = "Jewellery_Business_Database"

  const val SHEET_BUSINESS_PROFILE = "BusinessProfile"
  const val SHEET_CUSTOMERS = "Customers"
  const val SHEET_METAL_RATES = "MetalRates"
  const val SHEET_TRANSACTIONS = "Transactions"
  const val SHEET_TRANSACTION_ITEMS = "TransactionItems"
  const val SHEET_INVOICES = "Invoices"
  const val SHEET_INVOICE_ITEMS = "InvoiceItems"
  const val SHEET_SETTINGS = "Settings"
  const val SHEET_DAILY_SUMMARY = "DailySummary"

  // Stage 5 & 6 Worksheets
  const val SHEET_OPENING_STOCK = "OpeningStock"
  const val SHEET_INVENTORY_MOVEMENTS = "InventoryMovements"
  const val SHEET_VENDORS = "Vendors"
  const val SHEET_PURCHASES = "Purchases"
  const val SHEET_SALES = "Sales"
  const val SHEET_CUSTOMER_LEDGER = "CustomerLedger"
  const val SHEET_AUDIT_LOGS = "AuditLogs"

  val REQUIRED_WORKSHEETS: List<String> =
    listOf(
      SHEET_BUSINESS_PROFILE,
      SHEET_CUSTOMERS,
      SHEET_METAL_RATES,
      SHEET_TRANSACTIONS,
      SHEET_TRANSACTION_ITEMS,
      SHEET_INVOICES,
      SHEET_INVOICE_ITEMS,
      SHEET_SETTINGS,
      SHEET_DAILY_SUMMARY,
      SHEET_OPENING_STOCK,
      SHEET_INVENTORY_MOVEMENTS,
      SHEET_VENDORS,
      SHEET_PURCHASES,
      SHEET_SALES,
      SHEET_CUSTOMER_LEDGER,
      SHEET_AUDIT_LOGS,
    )

  val HEADERS_MAP: Map<String, List<String>> =
    mapOf(
      SHEET_BUSINESS_PROFILE to
        listOf(
          "BusinessProfileID",
          "ShopName",
          "OwnerName",
          "Mobile",
          "WhatsApp",
          "Email",
          "Address",
          "City",
          "District",
          "State",
          "PINCode",
          "PAN",
          "GSTIN",
          "BankAccount",
          "BankName",
          "BranchName",
          "IFSC",
          "UPI",
          "InvoicePrefix",
          "StartingInvoiceNumber",
          "Logo",
          "InvoiceFooter",
          "Terms",
          "CreatedAt",
          "UpdatedAt",
          "UpdatedBy",
          "Status",
        ),
      SHEET_CUSTOMERS to
        listOf(
          "CustomerID",
          "Name",
          "Mobile",
          "WhatsApp",
          "Address",
          "City",
          "State",
          "PIN",
          "PAN",
          "GSTNumber",
          "Email",
          "Notes",
          "CreatedAt",
          "UpdatedAt",
        ),
      SHEET_METAL_RATES to
        listOf(
          "RateID",
          "Date",
          "Metal",
          "Rate",
          "Unit",
          "Purity",
          "EnteredBy",
          "CreatedAt",
        ),
      SHEET_TRANSACTIONS to
        listOf(
          "TransactionID",
          "InvoiceNumber",
          "CustomerID",
          "Date",
          "Time",
          "TransactionType",
          "MetalType",
          "GrossWeight",
          "Tunch",
          "Purity",
          "FineWeight",
          "EnteredRate",
          "EnteredRateUnit",
          "NormalizedRatePerGram",
          "MetalValue",
          "Deductions",
          "NetValue",
          "CashAmount",
          "PaymentMode",
          "Status",
          "Notes",
          "CreatedAt",
          "UpdatedAt",
        ),
      SHEET_TRANSACTION_ITEMS to
        listOf(
          "TransactionItemID",
          "TransactionID",
          "Description",
          "MetalType",
          "Weight",
          "Tunch",
          "Purity",
          "FineWeight",
          "Rate",
          "Amount",
        ),
      SHEET_INVOICES to
        listOf(
          "InvoiceID",
          "InvoiceNumber",
          "TransactionID",
          "CustomerID",
          "InvoiceDate",
          "InvoiceTime",
          "Subtotal",
          "Discount",
          "Deduction",
          "TaxableAmount",
          "TaxAmount",
          "TotalAmount",
          "PaymentMode",
          "Status",
          "DriveFileID",
          "DriveFileName",
          "DriveSavedAt",
          "CreatedAt",
          "UpdatedAt",
        ),
      SHEET_INVOICE_ITEMS to
        listOf(
          "InvoiceItemID",
          "InvoiceID",
          "Description",
          "MetalType",
          "Weight",
          "Tunch",
          "Purity",
          "FineWeight",
          "Rate",
          "Amount",
        ),
      SHEET_SETTINGS to listOf("Key", "Value", "UpdatedAt"),
      SHEET_DAILY_SUMMARY to
        listOf(
          "Date",
          "GoldAmount",
          "SilverAmount",
          "GoldWeight",
          "SilverWeight",
          "GoldReceived",
          "SilverReceived",
          "ScrapGold",
          "ScrapSilver",
          "TotalTransactions",
          "TotalAmount",
          "UpdatedAt",
        ),
      SHEET_OPENING_STOCK to
        listOf(
          "Metal",
          "GrossWeight",
          "Tunch",
          "FineWeight",
          "ReferenceRatePerGram",
          "Notes",
          "UpdatedAt",
        ),
      SHEET_INVENTORY_MOVEMENTS to
        listOf(
          "MovementID",
          "Date",
          "TransactionID",
          "InvoiceNumber",
          "MovementType",
          "Metal",
          "GrossWeight",
          "Tunch",
          "FineWeight",
          "Rate",
          "Value",
          "Direction",
          "Notes",
          "CreatedAt",
        ),
      SHEET_VENDORS to
        listOf(
          "VendorID",
          "VendorName",
          "Mobile",
          "CompanyName",
          "Address",
          "PAN",
          "GSTNumber",
          "PendingMoney",
          "PendingGoldFineGrams",
          "PendingSilverFineGrams",
          "Notes",
          "CreatedAt",
          "UpdatedAt",
        ),
      SHEET_PURCHASES to
        listOf(
          "PurchaseID",
          "Date",
          "Time",
          "VendorID",
          "VendorName",
          "VendorMobile",
          "Metal",
          "Description",
          "GrossWeight",
          "Tunch",
          "FineWeight",
          "Rate",
          "RateUnit",
          "GSTPercent",
          "GSTAmount",
          "TotalAmount",
          "AmountPaid",
          "BalancePending",
          "PaymentMode",
          "Status",
          "Notes",
        ),
      SHEET_SALES to
        listOf(
          "SaleID",
          "InvoiceNumber",
          "Date",
          "Time",
          "CustomerID",
          "CustomerName",
          "CustomerMobile",
          "Metal",
          "Description",
          "GrossWeight",
          "Tunch",
          "FineWeight",
          "MakingCharges",
          "Rate",
          "RateUnit",
          "GSTAmount",
          "TotalAmount",
          "AmountReceived",
          "BalancePending",
          "PaymentMode",
          "Status",
          "Notes",
        ),
      SHEET_CUSTOMER_LEDGER to
        listOf(
          "CustomerID",
          "CustomerName",
          "MoneyPaid",
          "MoneyReceived",
          "NetMoneyBalance",
          "GoldGivenFineG",
          "GoldReceivedFineG",
          "NetGoldBalanceFineG",
          "SilverGivenFineG",
          "SilverReceivedFineG",
          "NetSilverBalanceFineG",
          "UpdatedAt",
        ),
      SHEET_AUDIT_LOGS to
        listOf(
          "ActionID",
          "Date",
          "Time",
          "ActionType",
          "EntityID",
          "Description",
          "PerformedBy",
          "Reason",
          "Timestamp",
        ),
    )
}
