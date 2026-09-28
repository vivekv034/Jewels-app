package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.AppLanguage
import com.example.domain.model.BusinessProfile
import com.example.domain.model.Customer
import com.example.domain.model.DeductionType
import com.example.domain.model.DriveUploadStatus
import com.example.domain.model.GoogleAccountState
import com.example.domain.model.GoogleConnectionStatus
import com.example.domain.model.Invoice
import com.example.domain.model.InvoiceStatus
import com.example.domain.model.LogoPosition
import com.example.domain.model.MetalRate
import com.example.domain.model.MetalType
import com.example.domain.model.PaymentMode
import com.example.domain.model.PendingDriveUpload
import com.example.domain.model.PendingSyncRecord
import com.example.domain.model.PuritySelectionMode
import com.example.domain.model.RateUnit
import com.example.domain.model.SyncOperation
import com.example.domain.model.SyncRecordType
import com.example.domain.model.SyncStatus
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionStatus
import com.example.domain.model.TransactionType
import java.math.BigDecimal
import org.json.JSONArray
import org.json.JSONObject

@Entity(tableName = "business_profile")
data class BusinessProfileEntity(
  @PrimaryKey val id: Int = 1,
  val businessId: String = "BIZ-001",
  val shopName: String,
  val ownerName: String,
  val mobileNumber: String,
  val whatsappNumber: String = "",
  val email: String = "",
  val address: String,
  val city: String = "",
  val district: String = "",
  val state: String = "Maharashtra",
  val pinCode: String = "",
  val panNumber: String,
  val gstNumber: String,
  val bankName: String,
  val branchName: String = "",
  val bankAccountNumber: String,
  val ifsc: String,
  val upiId: String,
  val invoicePrefix: String,
  val invoiceStartingNumber: Int,
  val weightDecimalPlaces: Int,
  val tunchDecimalPlaces: Int = 2,
  val moneyDecimalPlaces: Int,
  val purityDivisor: String,
  val maxTunchValue: String = "100.00",
  val allowEditingPure99Purity: Boolean = true,
  val enabledDeduction: Boolean = false,
  val enabledDeductionTypesCsv: String = DeductionType.entries.joinToString(",") { it.name },
  val businessLogoUri: String = "",
  val logoPosition: String = LogoPosition.LEFT.name,
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
  val defaultGstRatePercent: String = "3.00",
  val cgstRatePercent: String = "1.50",
  val sgstRatePercent: String = "1.50",
  val igstRatePercent: String = "0",
  val gstRegistrationType: String = "Regular GST Dealer",
  val gstStateName: String = "Maharashtra (27)",
  val gstTaxTreatment: String = "INTRA_STATE_CGST_SGST",
  val gstApplyToGold: Boolean = true,
  val gstApplyToSilver: Boolean = true,
  val gstApplyToMakingCharges: Boolean = true,
  val defaultInvoicePaperSize: String = "A4",
  val fastDashboardLoading: Boolean = true,
  val compactTransactionRows: Boolean = false,
  val languageCode: String,
  val createdAt: Long = 1711929600000L,
  val updatedAt: Long = System.currentTimeMillis(),
  val updatedBy: String = "Owner",
  val status: String = "ACTIVE",
) {
  fun toDomain(): BusinessProfile {
    val parsedDeductionTypes =
      enabledDeductionTypesCsv
        .split(",")
        .mapNotNull { name -> DeductionType.entries.find { it.name == name.trim() } }
        .toSet()
        .ifEmpty { DeductionType.entries.toSet() }

    return BusinessProfile(
      businessId = businessId.ifBlank { "BIZ-001" },
      shopName = shopName,
      ownerName = ownerName,
      mobileNumber = mobileNumber,
      whatsappNumber = whatsappNumber.ifBlank { mobileNumber },
      email = email,
      address = address,
      city = city,
      district = district,
      state = state,
      pinCode = pinCode,
      panNumber = panNumber,
      gstNumber = gstNumber,
      bankName = bankName,
      branchName = branchName,
      bankAccountNumber = bankAccountNumber,
      ifsc = ifsc,
      upiId = upiId,
      invoicePrefix = invoicePrefix,
      invoiceStartingNumber = invoiceStartingNumber,
      weightDecimalPlaces = weightDecimalPlaces,
      tunchDecimalPlaces = tunchDecimalPlaces,
      moneyDecimalPlaces = moneyDecimalPlaces,
      purityDivisor = purityDivisor.toBigDecimalOrNull() ?: BigDecimal("100"),
      maxTunchValue = maxTunchValue.toBigDecimalOrNull() ?: BigDecimal("100.00"),
      allowEditingPure99Purity = allowEditingPure99Purity,
      enabledDeduction = enabledDeduction,
      enabledDeductionTypes = parsedDeductionTypes,
      businessLogoUri = businessLogoUri,
      logoPosition = LogoPosition.entries.find { it.name == logoPosition } ?: LogoPosition.LEFT,
      invoiceFooter = invoiceFooter,
      termsAndConditions = termsAndConditions,
      showBankDetailsOnInvoice = showBankDetailsOnInvoice,
      showUpiOnInvoice = showUpiOnInvoice,
      showPanOnInvoice = showPanOnInvoice,
      showGstOnInvoice = showGstOnInvoice,
      showCustomerPanOnInvoice = showCustomerPanOnInvoice,
      showCustomerGstOnInvoice = showCustomerGstOnInvoice,
      showCustomerBalanceOnInvoice = showCustomerBalanceOnInvoice,
      gstEnabled = gstEnabled,
      defaultGstRatePercent = defaultGstRatePercent.toBigDecimalOrNull() ?: BigDecimal("3.00"),
      cgstRatePercent = cgstRatePercent.toBigDecimalOrNull() ?: BigDecimal("1.50"),
      sgstRatePercent = sgstRatePercent.toBigDecimalOrNull() ?: BigDecimal("1.50"),
      igstRatePercent = igstRatePercent.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      gstRegistrationType = gstRegistrationType.ifBlank { "Regular GST Dealer" },
      gstStateName = gstStateName.ifBlank { "Maharashtra (27)" },
      gstTaxTreatment = gstTaxTreatment.ifBlank { "INTRA_STATE_CGST_SGST" },
      gstApplyToGold = gstApplyToGold,
      gstApplyToSilver = gstApplyToSilver,
      gstApplyToMakingCharges = gstApplyToMakingCharges,
      defaultInvoicePaperSize =
        com.example.domain.model.InvoicePrintFormat.entries.find { it.name == defaultInvoicePaperSize }
          ?: com.example.domain.model.InvoicePrintFormat.A4,
      fastDashboardLoading = fastDashboardLoading,
      compactTransactionRows = compactTransactionRows,
      language = AppLanguage.entries.find { it.code == languageCode } ?: AppLanguage.ENGLISH,
      createdAt = createdAt,
      updatedAt = updatedAt,
      updatedBy = updatedBy.ifBlank { "Owner" },
      status = status.ifBlank { "ACTIVE" },
    )
  }

  companion object {
    fun fromDomain(profile: BusinessProfile): BusinessProfileEntity =
      BusinessProfileEntity(
        id = 1,
        businessId = profile.businessId.ifBlank { "BIZ-001" },
        shopName = profile.shopName,
        ownerName = profile.ownerName,
        mobileNumber = profile.mobileNumber,
        whatsappNumber = profile.whatsappNumber,
        email = profile.email,
        address = profile.address,
        city = profile.city,
        district = profile.district,
        state = profile.state,
        pinCode = profile.pinCode,
        panNumber = profile.panNumber,
        gstNumber = profile.gstNumber,
        bankName = profile.bankName,
        branchName = profile.branchName,
        bankAccountNumber = profile.bankAccountNumber,
        ifsc = profile.ifsc,
        upiId = profile.upiId,
        invoicePrefix = profile.invoicePrefix,
        invoiceStartingNumber = profile.invoiceStartingNumber,
        weightDecimalPlaces = profile.weightDecimalPlaces,
        tunchDecimalPlaces = profile.tunchDecimalPlaces,
        moneyDecimalPlaces = profile.moneyDecimalPlaces,
        purityDivisor = profile.purityDivisor.toPlainString(),
        maxTunchValue = profile.maxTunchValue.toPlainString(),
        allowEditingPure99Purity = profile.allowEditingPure99Purity,
        enabledDeduction = profile.enabledDeduction,
        enabledDeductionTypesCsv = profile.enabledDeductionTypes.joinToString(",") { it.name },
        businessLogoUri = profile.businessLogoUri,
        logoPosition = profile.logoPosition.name,
        invoiceFooter = profile.invoiceFooter,
        termsAndConditions = profile.termsAndConditions,
        showBankDetailsOnInvoice = profile.showBankDetailsOnInvoice,
        showUpiOnInvoice = profile.showUpiOnInvoice,
        showPanOnInvoice = profile.showPanOnInvoice,
        showGstOnInvoice = profile.showGstOnInvoice,
        showCustomerPanOnInvoice = profile.showCustomerPanOnInvoice,
        showCustomerGstOnInvoice = profile.showCustomerGstOnInvoice,
        showCustomerBalanceOnInvoice = profile.showCustomerBalanceOnInvoice,
        gstEnabled = profile.gstEnabled,
        defaultGstRatePercent = profile.defaultGstRatePercent.toPlainString(),
        cgstRatePercent = profile.cgstRatePercent.toPlainString(),
        sgstRatePercent = profile.sgstRatePercent.toPlainString(),
        igstRatePercent = profile.igstRatePercent.toPlainString(),
        gstRegistrationType = profile.gstRegistrationType,
        gstStateName = profile.gstStateName,
        gstTaxTreatment = profile.gstTaxTreatment,
        gstApplyToGold = profile.gstApplyToGold,
        gstApplyToSilver = profile.gstApplyToSilver,
        gstApplyToMakingCharges = profile.gstApplyToMakingCharges,
        defaultInvoicePaperSize = profile.defaultInvoicePaperSize.name,
        fastDashboardLoading = profile.fastDashboardLoading,
        compactTransactionRows = profile.compactTransactionRows,
        languageCode = profile.language.code,
        createdAt = profile.createdAt,
        updatedAt = profile.updatedAt,
        updatedBy = profile.updatedBy,
        status = profile.status,
      )
  }
}

@Entity(tableName = "metal_rates")
data class MetalRateEntity(
  @PrimaryKey val id: Int = 1,
  val rateId: String = "RATE-DEFAULT",
  val goldRate: String,
  val goldRateUnit: String,
  val silverRate: String,
  val silverRateUnit: String,
  val enteredBy: String = "Owner",
  val updatedAt: Long,
) {
  fun toDomain(): MetalRate =
    MetalRate(
      rateId = rateId.ifBlank { "RATE-DEFAULT" },
      goldRate = goldRate.toBigDecimalOrNull() ?: BigDecimal("7450.00"),
      goldRateUnit = RateUnit.fromString(goldRateUnit),
      silverRate = silverRate.toBigDecimalOrNull() ?: BigDecimal("92.50"),
      silverRateUnit = RateUnit.fromString(silverRateUnit),
      enteredBy = enteredBy,
      updatedAt = updatedAt,
    )

  companion object {
    fun fromDomain(rate: MetalRate): MetalRateEntity =
      MetalRateEntity(
        id = 1,
        rateId = rate.rateId,
        goldRate = rate.goldRate.toPlainString(),
        goldRateUnit = rate.goldRateUnit.name,
        silverRate = rate.silverRate.toPlainString(),
        silverRateUnit = rate.silverRateUnit.name,
        enteredBy = rate.enteredBy,
        updatedAt = rate.updatedAt,
      )
  }
}

@Entity(tableName = "customers")
data class CustomerEntity(
  @PrimaryKey val id: String,
  val name: String,
  val mobileNumber: String,
  val whatsappNumber: String = "",
  val address: String,
  val city: String = "",
  val state: String = "",
  val pinCode: String = "",
  val panNumber: String,
  val gstNumber: String,
  val email: String = "",
  val pendingAmount: String,
  val notes: String,
  val createdAt: Long,
  val updatedAt: Long = createdAt,
) {
  fun toDomain(): Customer =
    Customer(
      id = id,
      name = name,
      mobileNumber = mobileNumber,
      whatsappNumber = whatsappNumber.ifBlank { mobileNumber },
      address = address,
      city = city,
      state = state,
      pinCode = pinCode,
      panNumber = panNumber,
      gstNumber = gstNumber,
      email = email,
      pendingAmount = pendingAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      notes = notes,
      createdAt = createdAt,
      updatedAt = updatedAt,
    )

  companion object {
    fun fromDomain(customer: Customer): CustomerEntity =
      CustomerEntity(
        id = customer.id,
        name = customer.name,
        mobileNumber = customer.mobileNumber,
        whatsappNumber = customer.whatsappNumber,
        address = customer.address,
        city = customer.city,
        state = customer.state,
        pinCode = customer.pinCode,
        panNumber = customer.panNumber,
        gstNumber = customer.gstNumber,
        email = customer.email,
        pendingAmount = customer.pendingAmount.toPlainString(),
        notes = customer.notes,
        createdAt = customer.createdAt,
        updatedAt = customer.updatedAt,
      )
  }
}

internal object TransactionItemsSerializer {
  fun encode(items: List<TransactionItem>): String {
    if (items.isEmpty()) return ""
    val arr = JSONArray()
    items.forEach { item ->
      arr.put(
        JSONObject().apply {
          put("id", item.id)
          put("transactionId", item.transactionId)
          put("itemName", item.itemName.ifBlank { item.description })
          put("description", item.description.ifBlank { item.itemName })
          put("metalType", item.metalType.name)
          put("grossWeight", item.grossWeight.toPlainString())
          put("tunch", item.tunch.toPlainString())
          put("purity", item.purity)
          put("fineWeight", item.fineWeight.toPlainString())
          put("rate", item.rate.toPlainString())
          put("rateUnit", item.rateUnit.name)
          put("metalValue", item.metalValue.toPlainString())
          put("amount", item.amount.toPlainString())
        }
      )
    }
    return arr.toString()
  }

  fun decode(json: String): List<TransactionItem> {
    if (json.isBlank()) return emptyList()
    return try {
      val arr = JSONArray(json)
      val list = mutableListOf<TransactionItem>()
      for (i in 0 until arr.length()) {
        val obj = arr.optJSONObject(i) ?: continue
        val desc = obj.optString("description", obj.optString("itemName", "Jewellery Item"))
        val name = obj.optString("itemName", desc)
        val amt = obj.optString("amount", "0").toBigDecimalOrNull() ?: BigDecimal.ZERO
        val mv = obj.optString("metalValue", amt.toPlainString()).toBigDecimalOrNull() ?: amt
        list.add(
          TransactionItem(
            id = obj.optString("id", "ITEM-${i + 1}"),
            transactionId = obj.optString("transactionId", ""),
            itemName = name,
            description = desc,
            metalType =
              MetalType.entries.find { it.name == obj.optString("metalType") } ?: MetalType.GOLD,
            grossWeight = obj.optString("grossWeight", "0").toBigDecimalOrNull() ?: BigDecimal.ZERO,
            tunch = obj.optString("tunch", "100").toBigDecimalOrNull() ?: BigDecimal("100.00"),
            purity = obj.optString("purity", ""),
            fineWeight = obj.optString("fineWeight", "0").toBigDecimalOrNull() ?: BigDecimal.ZERO,
            rate = obj.optString("rate", "0").toBigDecimalOrNull() ?: BigDecimal.ZERO,
            rateUnit = RateUnit.fromString(obj.optString("rateUnit", "PER_GRAM")),
            metalValue = mv,
            amount = amt,
          )
        )
      }
      list
    } catch (_: Exception) {
      emptyList()
    }
  }
}

@Entity(tableName = "transactions")
data class TransactionEntity(
  @PrimaryKey val transactionId: String,
  val date: String,
  val time: String,
  val timestamp: Long,
  val customerId: String,
  val customerName: String,
  val customerMobile: String,
  val transactionType: String,
  val metalType: String,
  val purityMode: String,
  val purity: String = "",
  val grossWeight: String,
  val tunch: String,
  val fineWeight: String,
  val rate: String,
  val rateUnit: String,
  val normalizedRatePerGram: String = "",
  val metalValue: String = "0",
  val deductionType: String = DeductionType.NONE.name,
  val deductionInput: String = "0",
  val deductions: String = "0",
  val paymentAdjustment: String = "0",
  val cashPaid: String = "0",
  val cashReceived: String = "0",
  val remainingBalance: String = "0",
  val amount: String,
  val cashAmount: String = amount,
  val paymentMode: String,
  val notes: String,
  val invoiceNumber: String,
  val status: String = TransactionStatus.COMPLETED.name,
  val syncStatus: String = SyncStatus.SYNCED.name,
  val cancelledAt: Long = 0L,
  val cancelledBy: String = "",
  val cancellationReason: String = "",
  val parentTransactionId: String = "",
  val itemsJson: String = "",
  val createdAt: Long = timestamp,
  val updatedAt: Long = timestamp,
) {
  fun toDomain(): Transaction {
    val parsedRate = rate.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedUnit = RateUnit.fromString(rateUnit)
    val parsedNormRate =
      normalizedRatePerGram.toBigDecimalOrNull()
        ?: if (parsedUnit.gramsInUnit > 0 && parsedRate > BigDecimal.ZERO) {
          parsedRate.divide(BigDecimal(parsedUnit.gramsInUnit), 2, java.math.RoundingMode.HALF_UP)
        } else {
          parsedRate
        }
    val parsedAmount = amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedDeductions = deductions.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedDeductionInput = deductionInput.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedMetalValue =
      metalValue.toBigDecimalOrNull()?.takeIf { it > BigDecimal.ZERO }
        ?: parsedAmount.add(parsedDeductions)
    val parsedPurityMode =
      PuritySelectionMode.entries.find { it.name == purityMode }
        ?: PuritySelectionMode.CUSTOM_TUNCH

    return Transaction(
      transactionId = transactionId,
      date = date,
      time = time,
      timestamp = timestamp,
      customerId = customerId,
      customerName = customerName,
      customerMobile = customerMobile,
      transactionType =
        TransactionType.entries.find { it.name == transactionType } ?: TransactionType.MONEY_TO_GOLD,
      metalType = MetalType.entries.find { it.name == metalType } ?: MetalType.GOLD,
      purityMode = parsedPurityMode,
      purity = purity.ifBlank { parsedPurityMode.displayName },
      grossWeight = grossWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      tunch = tunch.toBigDecimalOrNull() ?: BigDecimal("100.00"),
      fineWeight = fineWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      rate = parsedRate,
      rateUnit = parsedUnit,
      normalizedRatePerGram = parsedNormRate,
      metalValue = parsedMetalValue,
      deductionType =
        DeductionType.entries.find { it.name == deductionType } ?: DeductionType.NONE,
      deductionValue = parsedDeductionInput,
      deductionInput = parsedDeductionInput,
      deductionAmount = parsedDeductions,
      deductions = parsedDeductions,
      paymentAdjustment = paymentAdjustment.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      cashPaid = cashPaid.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      cashReceived = cashReceived.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      remainingBalance = remainingBalance.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      amount = parsedAmount,
      netValue = parsedAmount,
      cashAmount = cashAmount.toBigDecimalOrNull() ?: parsedAmount,
      paymentMode = PaymentMode.entries.find { it.name == paymentMode } ?: PaymentMode.CASH,
      notes = notes,
      invoiceNumber = invoiceNumber,
      status =
        TransactionStatus.entries.find { it.name == status } ?: TransactionStatus.COMPLETED,
      syncStatus = SyncStatus.entries.find { it.name == syncStatus } ?: SyncStatus.SYNCED,
      cancelledAt = cancelledAt,
      cancelledBy = cancelledBy,
      cancellationReason = cancellationReason,
      parentTransactionId = parentTransactionId,
      items = TransactionItemsSerializer.decode(itemsJson),
      createdAt = createdAt,
      updatedAt = updatedAt,
    )
  }

  companion object {
    fun fromDomain(tx: Transaction): TransactionEntity =
      TransactionEntity(
        transactionId = tx.transactionId,
        date = tx.date,
        time = tx.time,
        timestamp = tx.timestamp,
        customerId = tx.customerId,
        customerName = tx.customerName,
        customerMobile = tx.customerMobile,
        transactionType = tx.transactionType.name,
        metalType = tx.metalType.name,
        purityMode = tx.purityMode.name,
        purity = tx.purity.ifBlank { tx.purityMode.displayName },
        grossWeight = tx.grossWeight.toPlainString(),
        tunch = tx.tunch.toPlainString(),
        fineWeight = tx.fineWeight.toPlainString(),
        rate = tx.rate.toPlainString(),
        rateUnit = tx.rateUnit.name,
        normalizedRatePerGram = tx.normalizedRatePerGram.toPlainString(),
        metalValue = tx.effectiveMetalValue.toPlainString(),
        deductionType = tx.deductionType.name,
        deductionInput = tx.deductionInput.toPlainString(),
        deductions = tx.deductions.toPlainString(),
        paymentAdjustment = tx.paymentAdjustment.toPlainString(),
        cashPaid = tx.cashPaid.toPlainString(),
        cashReceived = tx.cashReceived.toPlainString(),
        remainingBalance = tx.remainingBalance.toPlainString(),
        amount = tx.amount.toPlainString(),
        cashAmount = tx.cashAmount.toPlainString(),
        paymentMode = tx.paymentMode.name,
        notes = tx.notes,
        invoiceNumber = tx.invoiceNumber,
        status = tx.status.name,
        syncStatus = tx.syncStatus.name,
        cancelledAt = tx.cancelledAt,
        cancelledBy = tx.cancelledBy,
        cancellationReason = tx.cancellationReason,
        parentTransactionId = tx.parentTransactionId,
        itemsJson = TransactionItemsSerializer.encode(tx.items),
        createdAt = tx.createdAt,
        updatedAt = tx.updatedAt,
      )
  }
}

@Entity(tableName = "invoices")
data class InvoiceEntity(
  @PrimaryKey val invoiceNumber: String,
  val invoiceId: String = "",
  val transactionId: String,
  val date: String,
  val time: String,
  val timestamp: Long,
  val shopName: String,
  val ownerName: String,
  val shopMobile: String,
  val shopAddress: String,
  val shopPan: String,
  val shopGst: String,
  val bankName: String,
  val bankAccountNumber: String,
  val ifsc: String,
  val upiId: String,
  val customerId: String,
  val customerName: String,
  val customerMobile: String,
  val customerAddress: String,
  val customerPan: String,
  val customerGst: String,
  val transactionType: String,
  val metalType: String,
  val purityMode: String,
  val grossWeight: String,
  val tunch: String,
  val fineWeight: String,
  val rate: String,
  val rateUnit: String,
  val amount: String,
  val subtotal: String = amount,
  val deductions: String = "0",
  val netAmount: String = amount,
  val taxableAmount: String = netAmount,
  val gstEnabled: Boolean = false,
  val cgstPercent: String = "0",
  val sgstPercent: String = "0",
  val igstPercent: String = "0",
  val cgstAmount: String = "0",
  val sgstAmount: String = "0",
  val igstAmount: String = "0",
  val taxAmount: String = "0",
  val discount: String = deductions,
  val paymentMode: String,
  val amountReceived: String = amount,
  val amountPending: String = "0",
  val previousMoneyBalance: String = "0",
  val previousGoldBalanceGrams: String = "0",
  val previousSilverBalanceGrams: String = "0",
  val afterMoneyBalance: String = "0",
  val afterGoldBalanceGrams: String = "0",
  val afterSilverBalanceGrams: String = "0",
  val showCustomerBalance: Boolean = false,
  val status: String = "COMPLETED",
  val notes: String,
  val termsAndConditions: String = "Please verify all details before leaving the premises.",
  val invoiceFooter: String = "Thank you for your patronage! Hallmark of Trust & Purity.",
  val businessLogoUri: String = "",
  val logoPosition: String = LogoPosition.LEFT.name,
  val showBankDetails: Boolean = true,
  val showUpi: Boolean = true,
  val showPan: Boolean = true,
  val showGst: Boolean = true,
  val showCustomerPan: Boolean = true,
  val showCustomerGst: Boolean = true,
  val itemDescription: String,
  val itemsJson: String = "",
  val totalAmount: String,
  val localPdfPath: String = "",
  val pdfFileName: String = "",
  val driveFileId: String = "",
  val driveFileName: String = "",
  val driveFileUrl: String = "",
  val driveSavedAt: Long = 0L,
  val createdAt: Long = timestamp,
  val updatedAt: Long = timestamp,
) {
  fun toDomain(): Invoice {
    val parsedMetal = MetalType.entries.find { it.name == metalType } ?: MetalType.GOLD
    val parsedGross = grossWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedTunch = tunch.toBigDecimalOrNull() ?: BigDecimal("100.00")
    val parsedFine = fineWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedRate = rate.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedRateUnit = RateUnit.fromString(rateUnit)
    val parsedAmount = amount.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedSubtotal = subtotal.toBigDecimalOrNull() ?: parsedAmount
    val parsedDeductions = deductions.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedNet = netAmount.toBigDecimalOrNull() ?: parsedAmount.subtract(parsedDeductions).max(BigDecimal.ZERO)
    val parsedTaxable = taxableAmount.toBigDecimalOrNull() ?: parsedNet
    val parsedCgstPct = cgstPercent.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedSgstPct = sgstPercent.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedIgstPct = igstPercent.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedCgstAmt = cgstAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedSgstAmt = sgstAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedIgstAmt = igstAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedTaxAmt = taxAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedDiscount = discount.toBigDecimalOrNull() ?: parsedDeductions
    val parsedTotal = totalAmount.toBigDecimalOrNull() ?: parsedNet.add(parsedTaxAmt)
    val parsedReceived = amountReceived.toBigDecimalOrNull() ?: parsedTotal
    val parsedPending = amountPending.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val parsedPurityMode =
      PuritySelectionMode.entries.find { it.name == purityMode }
        ?: PuritySelectionMode.CUSTOM_TUNCH

    val decodedItems = TransactionItemsSerializer.decode(itemsJson)
    val finalItems =
      if (decodedItems.isNotEmpty()) {
        decodedItems
      } else {
        listOf(
          TransactionItem(
            id = "TXNI-${transactionId.removePrefix("TXN-")}-1",
            transactionId = transactionId,
            itemName = itemDescription,
            description = itemDescription,
            metalType = parsedMetal,
            grossWeight = parsedGross,
            tunch = parsedTunch,
            purity = parsedPurityMode.displayName,
            fineWeight = parsedFine,
            rate = parsedRate,
            rateUnit = parsedRateUnit,
            metalValue = parsedAmount,
            amount = parsedAmount,
          )
        )
      }

    return Invoice(
      invoiceId = invoiceId.ifBlank { "INV-${transactionId.removePrefix("TXN-")}" },
      invoiceNumber = invoiceNumber,
      transactionId = transactionId,
      date = date,
      time = time,
      timestamp = timestamp,
      shopName = shopName,
      ownerName = ownerName,
      shopMobile = shopMobile,
      shopAddress = shopAddress,
      shopPan = shopPan,
      shopGst = shopGst,
      bankName = bankName,
      bankAccountNumber = bankAccountNumber,
      ifsc = ifsc,
      upiId = upiId,
      customerId = customerId,
      customerName = customerName,
      customerMobile = customerMobile,
      customerAddress = customerAddress,
      customerPan = customerPan,
      customerGst = customerGst,
      transactionType =
        TransactionType.entries.find { it.name == transactionType } ?: TransactionType.MONEY_TO_GOLD,
      metalType = parsedMetal,
      purityMode = parsedPurityMode,
      grossWeight = parsedGross,
      tunch = parsedTunch,
      fineWeight = parsedFine,
      rate = parsedRate,
      rateUnit = parsedRateUnit,
      amount = parsedAmount,
      subtotal = parsedSubtotal,
      deductions = parsedDeductions,
      netAmount = parsedNet,
      taxableAmount = parsedTaxable,
      gstEnabled = gstEnabled,
      cgstPercent = parsedCgstPct,
      sgstPercent = parsedSgstPct,
      igstPercent = parsedIgstPct,
      cgstAmount = parsedCgstAmt,
      sgstAmount = parsedSgstAmt,
      igstAmount = parsedIgstAmt,
      taxAmount = parsedTaxAmt,
      discount = parsedDiscount,
      paymentMode = PaymentMode.entries.find { it.name == paymentMode } ?: PaymentMode.CASH,
      amountReceived = parsedReceived,
      amountPending = parsedPending,
      previousMoneyBalance = previousMoneyBalance.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      previousGoldBalanceGrams = previousGoldBalanceGrams.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      previousSilverBalanceGrams = previousSilverBalanceGrams.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      afterMoneyBalance = afterMoneyBalance.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      afterGoldBalanceGrams = afterGoldBalanceGrams.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      afterSilverBalanceGrams = afterSilverBalanceGrams.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      showCustomerBalance = showCustomerBalance,
      status = status,
      invoiceStatus = InvoiceStatus.fromString(status),
      notes = notes,
      termsAndConditions = termsAndConditions,
      invoiceFooter = invoiceFooter,
      businessLogoUri = businessLogoUri,
      logoPosition = LogoPosition.entries.find { it.name == logoPosition } ?: LogoPosition.LEFT,
      showBankDetails = showBankDetails,
      showUpi = showUpi,
      showPan = showPan,
      showGst = showGst,
      showCustomerPan = showCustomerPan,
      showCustomerGst = showCustomerGst,
      items = finalItems,
      totalAmount = parsedTotal,
      localPdfPath = localPdfPath,
      pdfFileName = pdfFileName.ifBlank { "Invoice_$invoiceNumber.pdf" },
      driveFileId = driveFileId,
      driveFileName = driveFileName,
      driveFileUrl = driveFileUrl,
      driveSavedAt = driveSavedAt,
      createdAt = createdAt,
      updatedAt = updatedAt,
    )
  }

  companion object {
    fun fromDomain(invoice: Invoice): InvoiceEntity =
      InvoiceEntity(
        invoiceNumber = invoice.invoiceNumber,
        invoiceId = invoice.invoiceId.ifBlank { "INV-${invoice.transactionId.removePrefix("TXN-")}" },
        transactionId = invoice.transactionId,
        date = invoice.date,
        time = invoice.time,
        timestamp = invoice.timestamp,
        shopName = invoice.shopName,
        ownerName = invoice.ownerName,
        shopMobile = invoice.shopMobile,
        shopAddress = invoice.shopAddress,
        shopPan = invoice.shopPan,
        shopGst = invoice.shopGst,
        bankName = invoice.bankName,
        bankAccountNumber = invoice.bankAccountNumber,
        ifsc = invoice.ifsc,
        upiId = invoice.upiId,
        customerId = invoice.customerId,
        customerName = invoice.customerName,
        customerMobile = invoice.customerMobile,
        customerAddress = invoice.customerAddress,
        customerPan = invoice.customerPan,
        customerGst = invoice.customerGst,
        transactionType = invoice.transactionType.name,
        metalType = invoice.metalType.name,
        purityMode = invoice.purityMode.name,
        grossWeight = invoice.grossWeight.toPlainString(),
        tunch = invoice.tunch.toPlainString(),
        fineWeight = invoice.fineWeight.toPlainString(),
        rate = invoice.rate.toPlainString(),
        rateUnit = invoice.rateUnit.name,
        amount = invoice.amount.toPlainString(),
        subtotal = invoice.subtotal.toPlainString(),
        deductions = invoice.deductions.toPlainString(),
        netAmount = invoice.netAmount.toPlainString(),
        taxableAmount = invoice.taxableAmount.toPlainString(),
        gstEnabled = invoice.gstEnabled,
        cgstPercent = invoice.cgstPercent.toPlainString(),
        sgstPercent = invoice.sgstPercent.toPlainString(),
        igstPercent = invoice.igstPercent.toPlainString(),
        cgstAmount = invoice.cgstAmount.toPlainString(),
        sgstAmount = invoice.sgstAmount.toPlainString(),
        igstAmount = invoice.igstAmount.toPlainString(),
        taxAmount = invoice.taxAmount.toPlainString(),
        discount = invoice.discount.toPlainString(),
        paymentMode = invoice.paymentMode.name,
        amountReceived = invoice.amountReceived.toPlainString(),
        amountPending = invoice.amountPending.toPlainString(),
        previousMoneyBalance = invoice.previousMoneyBalance.toPlainString(),
        previousGoldBalanceGrams = invoice.previousGoldBalanceGrams.toPlainString(),
        previousSilverBalanceGrams = invoice.previousSilverBalanceGrams.toPlainString(),
        afterMoneyBalance = invoice.afterMoneyBalance.toPlainString(),
        afterGoldBalanceGrams = invoice.afterGoldBalanceGrams.toPlainString(),
        afterSilverBalanceGrams = invoice.afterSilverBalanceGrams.toPlainString(),
        showCustomerBalance = invoice.showCustomerBalance,
        status = invoice.invoiceStatus.name,
        notes = invoice.notes,
        termsAndConditions = invoice.termsAndConditions,
        invoiceFooter = invoice.invoiceFooter,
        businessLogoUri = invoice.businessLogoUri,
        logoPosition = invoice.logoPosition.name,
        showBankDetails = invoice.showBankDetails,
        showUpi = invoice.showUpi,
        showPan = invoice.showPan,
        showGst = invoice.showGst,
        showCustomerPan = invoice.showCustomerPan,
        showCustomerGst = invoice.showCustomerGst,
        itemDescription = invoice.items.firstOrNull()?.description ?: invoice.transactionType.title,
        itemsJson = TransactionItemsSerializer.encode(invoice.items),
        totalAmount = invoice.totalAmount.toPlainString(),
        localPdfPath = invoice.localPdfPath,
        pdfFileName = invoice.pdfFileName.ifBlank { "Invoice_${invoice.invoiceNumber}.pdf" },
        driveFileId = invoice.driveFileId,
        driveFileName = invoice.driveFileName,
        driveFileUrl = invoice.driveFileUrl,
        driveSavedAt = invoice.driveSavedAt,
        createdAt = invoice.createdAt,
        updatedAt = invoice.updatedAt,
      )
  }
}

@Entity(tableName = "pending_sync_queue")
data class PendingSyncQueueEntity(
  @PrimaryKey val recordId: String,
  val recordType: String,
  val operation: String,
  val payload: String,
  val createdAt: Long,
  val retryCount: Int,
  val lastError: String,
  val syncStatus: String,
) {
  fun toDomain(): PendingSyncRecord =
    PendingSyncRecord(
      recordId = recordId,
      recordType =
        SyncRecordType.entries.find { it.name == recordType } ?: SyncRecordType.TRANSACTION,
      operation = SyncOperation.entries.find { it.name == operation } ?: SyncOperation.CREATE,
      payload = payload,
      createdAt = createdAt,
      retryCount = retryCount,
      lastError = lastError,
      syncStatus = SyncStatus.entries.find { it.name == syncStatus } ?: SyncStatus.PENDING_SYNC,
    )

  companion object {
    fun fromDomain(record: PendingSyncRecord): PendingSyncQueueEntity =
      PendingSyncQueueEntity(
        recordId = record.recordId,
        recordType = record.recordType.name,
        operation = record.operation.name,
        payload = record.payload,
        createdAt = record.createdAt,
        retryCount = record.retryCount,
        lastError = record.lastError,
        syncStatus = record.syncStatus.name,
      )
  }
}

@Entity(tableName = "pending_drive_uploads")
data class PendingDriveUploadEntity(
  @PrimaryKey val uploadId: String,
  val invoiceId: String,
  val invoiceNumber: String = "",
  val fileName: String,
  val localFileReference: String,
  val createdAt: Long,
  val retryCount: Int,
  val lastError: String,
  val status: String,
) {
  fun toDomain(): PendingDriveUpload =
    PendingDriveUpload(
      uploadId = uploadId,
      invoiceId = invoiceId,
      invoiceNumber = invoiceNumber,
      fileName = fileName,
      localFileReference = localFileReference,
      createdAt = createdAt,
      retryCount = retryCount,
      lastError = lastError,
      status =
        DriveUploadStatus.entries.find { it.name == status } ?: DriveUploadStatus.PENDING,
    )

  companion object {
    fun fromDomain(upload: PendingDriveUpload): PendingDriveUploadEntity =
      PendingDriveUploadEntity(
        uploadId = upload.uploadId,
        invoiceId = upload.invoiceId,
        invoiceNumber = upload.invoiceNumber,
        fileName = upload.fileName,
        localFileReference = upload.localFileReference,
        createdAt = upload.createdAt,
        retryCount = upload.retryCount,
        lastError = upload.lastError,
        status = upload.status.name,
      )
  }
}

@Entity(tableName = "google_account_config")
data class GoogleAccountConfigEntity(
  @PrimaryKey val id: Int = 1,
  val connectionStatus: String,
  val connectedEmail: String,
  val displayName: String,
  val accessToken: String,
  val selectedSpreadsheetId: String,
  val selectedSpreadsheetName: String,
  val syncStatus: String,
  val lastSyncTimestamp: Long,
  val initializedWorksheetsCsv: String,
  val isOfflineMode: Boolean,
  val driveConnected: Boolean = false,
  val driveRootFolderId: String = "",
  val driveInvoicesFolderId: String = "",
  val driveBackupsFolderId: String = "",
  val driveReportsFolderId: String = "",
  val driveInvoiceFolderPath: String = "Jewellery Business Manager / Invoices",
  val lastDriveSyncTimestamp: Long = 0L,
) {
  fun toDomain(pendingCount: Int = 0): GoogleAccountState =
    GoogleAccountState(
      connectionStatus =
        GoogleConnectionStatus.entries.find { it.name == connectionStatus }
          ?: GoogleConnectionStatus.DISCONNECTED,
      connectedEmail = connectedEmail,
      displayName = displayName,
      accessToken = accessToken,
      selectedSpreadsheetId = selectedSpreadsheetId,
      selectedSpreadsheetName = selectedSpreadsheetName,
      syncStatus = SyncStatus.entries.find { it.name == syncStatus } ?: SyncStatus.SYNCED,
      lastSyncTimestamp = lastSyncTimestamp,
      pendingSyncCount = pendingCount,
      initializedWorksheets =
        if (initializedWorksheetsCsv.isBlank()) emptyList()
        else initializedWorksheetsCsv.split(",").filter { it.isNotBlank() },
      isOfflineMode = isOfflineMode,
      driveConnected = driveConnected,
      driveRootFolderId = driveRootFolderId,
      driveInvoicesFolderId = driveInvoicesFolderId,
      driveBackupsFolderId = driveBackupsFolderId,
      driveReportsFolderId = driveReportsFolderId,
      driveInvoiceFolderPath =
        driveInvoiceFolderPath.ifBlank { "Jewellery Business Manager / Invoices" },
      lastDriveSyncTimestamp = lastDriveSyncTimestamp,
    )

  companion object {
    fun fromDomain(state: GoogleAccountState): GoogleAccountConfigEntity =
      GoogleAccountConfigEntity(
        id = 1,
        connectionStatus = state.connectionStatus.name,
        connectedEmail = state.connectedEmail,
        displayName = state.displayName,
        accessToken = state.accessToken,
        selectedSpreadsheetId = state.selectedSpreadsheetId,
        selectedSpreadsheetName = state.selectedSpreadsheetName,
        syncStatus = state.syncStatus.name,
        lastSyncTimestamp = state.lastSyncTimestamp,
        initializedWorksheetsCsv = state.initializedWorksheets.joinToString(","),
        isOfflineMode = state.isOfflineMode,
        driveConnected = state.driveConnected,
        driveRootFolderId = state.driveRootFolderId,
        driveInvoicesFolderId = state.driveInvoicesFolderId,
        driveBackupsFolderId = state.driveBackupsFolderId,
        driveReportsFolderId = state.driveReportsFolderId,
        driveInvoiceFolderPath = state.driveInvoiceFolderPath,
        lastDriveSyncTimestamp = state.lastDriveSyncTimestamp,
      )
  }
}
