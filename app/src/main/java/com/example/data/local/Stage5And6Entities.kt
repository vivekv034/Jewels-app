package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.AppSecuritySettings
import com.example.domain.model.AuditActionType
import com.example.domain.model.AuditLogEntry
import com.example.domain.model.CustomerReminder
import com.example.domain.model.InventoryMovement
import com.example.domain.model.InventoryMovementDirection
import com.example.domain.model.InventoryMovementType
import com.example.domain.model.MetalType
import com.example.domain.model.OpeningStock
import com.example.domain.model.PaymentMode
import com.example.domain.model.PurchaseRecord
import com.example.domain.model.RateUnit
import com.example.domain.model.ReminderType
import com.example.domain.model.SaleRecord
import com.example.domain.model.ScrapProcessRecord
import com.example.domain.model.StockReconciliationRecord
import com.example.domain.model.TransactionStatus
import com.example.domain.model.UserAccessMode
import com.example.domain.model.Vendor
import java.math.BigDecimal

@Entity(tableName = "opening_stock")
data class OpeningStockEntity(
  @PrimaryKey val metal: String,
  val grossWeight: String,
  val tunch: String,
  val fineWeight: String,
  val referenceRate: String,
  val date: String = "2025-04-01",
  val notes: String,
  val updatedAt: Long,
) {
  fun toDomain(): OpeningStock =
    OpeningStock(
      metal = MetalType.entries.find { it.name == metal } ?: MetalType.GOLD,
      grossWeight = grossWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      tunch = tunch.toBigDecimalOrNull() ?: BigDecimal("99.50"),
      fineWeight = fineWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      referenceRatePerGram = referenceRate.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      date = date,
      notes = notes,
      updatedAt = updatedAt,
    )

  companion object {
    fun fromDomain(domain: OpeningStock): OpeningStockEntity =
      OpeningStockEntity(
        metal = domain.metal.name,
        grossWeight = domain.grossWeight.toPlainString(),
        tunch = domain.tunch.toPlainString(),
        fineWeight = domain.fineWeight.toPlainString(),
        referenceRate = domain.referenceRatePerGram.toPlainString(),
        date = domain.date,
        notes = domain.notes,
        updatedAt = domain.updatedAt,
      )
  }
}

@Entity(tableName = "inventory_movements")
data class InventoryMovementEntity(
  @PrimaryKey val movementId: String,
  val date: String,
  val transactionId: String,
  val invoiceNumber: String,
  val movementType: String,
  val metal: String,
  val grossWeight: String,
  val tunch: String,
  val fineWeight: String,
  val rate: String,
  val value: String,
  val direction: String,
  val notes: String,
  val createdAt: Long,
) {
  fun toDomain(): InventoryMovement =
    InventoryMovement(
      movementId = movementId,
      date = date,
      transactionId = transactionId,
      invoiceNumber = invoiceNumber,
      movementType =
        InventoryMovementType.entries.find { it.name == movementType }
          ?: InventoryMovementType.ADJUSTMENT,
      metal = MetalType.entries.find { it.name == metal } ?: MetalType.GOLD,
      grossWeight = grossWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      tunch = tunch.toBigDecimalOrNull() ?: BigDecimal("100.00"),
      fineWeight = fineWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      rate = rate.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      value = value.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      direction =
        InventoryMovementDirection.entries.find { it.name == direction }
          ?: InventoryMovementDirection.IN,
      notes = notes,
      createdAt = createdAt,
    )

  companion object {
    fun fromDomain(domain: InventoryMovement): InventoryMovementEntity =
      InventoryMovementEntity(
        movementId = domain.movementId,
        date = domain.date,
        transactionId = domain.transactionId,
        invoiceNumber = domain.invoiceNumber,
        movementType = domain.movementType.name,
        metal = domain.metal.name,
        grossWeight = domain.grossWeight.toPlainString(),
        tunch = domain.tunch.toPlainString(),
        fineWeight = domain.fineWeight.toPlainString(),
        rate = domain.rate.toPlainString(),
        value = domain.value.toPlainString(),
        direction = domain.direction.name,
        notes = domain.notes,
        createdAt = domain.createdAt,
      )
  }
}

@Entity(tableName = "inventory_reconciliations")
data class InventoryReconciliationEntity(
  @PrimaryKey val reconciliationId: String,
  val date: String,
  val createdAt: Long = System.currentTimeMillis(),
  val metal: String,
  val systemGrossWeight: String,
  val systemFineWeight: String,
  val physicalGrossWeight: String,
  val physicalFineWeight: String,
  val differenceGrossWeight: String,
  val differenceFineWeight: String,
  val adjustmentMovementId: String = "",
  val notes: String,
) {
  fun toDomain(): StockReconciliationRecord =
    StockReconciliationRecord(
      reconciliationId = reconciliationId,
      date = date,
      timestamp = createdAt,
      metal = MetalType.entries.find { it.name == metal } ?: MetalType.GOLD,
      systemGrossWeight = systemGrossWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      systemFineWeight = systemFineWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      physicalGrossWeight = physicalGrossWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      physicalFineWeight = physicalFineWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      differenceGrossWeight = differenceGrossWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      differenceFineWeight = differenceFineWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      adjustmentMovementId = adjustmentMovementId,
      notes = notes,
    )

  companion object {
    fun fromDomain(domain: StockReconciliationRecord): InventoryReconciliationEntity =
      InventoryReconciliationEntity(
        reconciliationId = domain.reconciliationId,
        date = domain.date,
        createdAt = domain.timestamp,
        metal = domain.metal.name,
        systemGrossWeight = domain.systemGrossWeight.toPlainString(),
        systemFineWeight = domain.systemFineWeight.toPlainString(),
        physicalGrossWeight = domain.physicalGrossWeight.toPlainString(),
        physicalFineWeight = domain.physicalFineWeight.toPlainString(),
        differenceGrossWeight = domain.differenceGrossWeight.toPlainString(),
        differenceFineWeight = domain.differenceFineWeight.toPlainString(),
        adjustmentMovementId = domain.adjustmentMovementId,
        notes = domain.notes,
      )
  }
}

@Entity(tableName = "vendors")
data class VendorEntity(
  @PrimaryKey val vendorId: String,
  val name: String,
  val mobileNumber: String,
  val companyName: String = "",
  val address: String,
  val panNumber: String,
  val gstNumber: String,
  val pendingPayableAmount: String,
  val pendingGoldFineGrams: String = "0.000",
  val pendingSilverFineGrams: String = "0.000",
  val notes: String,
  val createdAt: Long,
  val updatedAt: Long,
) {
  fun toDomain(): Vendor =
    Vendor(
      vendorId = vendorId,
      name = name,
      mobile = mobileNumber,
      companyName = companyName,
      address = address,
      panNumber = panNumber,
      gstNumber = gstNumber,
      pendingMoney = pendingPayableAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      pendingGoldFineGrams = pendingGoldFineGrams.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      pendingSilverFineGrams = pendingSilverFineGrams.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      notes = notes,
      createdAt = createdAt,
      updatedAt = updatedAt,
    )

  companion object {
    fun fromDomain(domain: Vendor): VendorEntity =
      VendorEntity(
        vendorId = domain.vendorId,
        name = domain.name,
        mobileNumber = domain.mobile,
        companyName = domain.companyName,
        address = domain.address,
        panNumber = domain.panNumber,
        gstNumber = domain.gstNumber,
        pendingPayableAmount = domain.pendingMoney.toPlainString(),
        pendingGoldFineGrams = domain.pendingGoldFineGrams.toPlainString(),
        pendingSilverFineGrams = domain.pendingSilverFineGrams.toPlainString(),
        notes = domain.notes,
        createdAt = domain.createdAt,
        updatedAt = domain.updatedAt,
      )
  }
}

@Entity(tableName = "purchases")
data class PurchaseEntity(
  @PrimaryKey val purchaseId: String,
  val date: String,
  val time: String,
  val timestamp: Long,
  val vendorId: String,
  val vendorName: String,
  val vendorMobile: String,
  val metalType: String,
  val description: String,
  val grossWeight: String,
  val tunch: String,
  val fineWeight: String,
  val rate: String,
  val rateUnit: String,
  val gstPercent: String = "0",
  val gstAmount: String = "0",
  val totalAmount: String,
  val amountPaid: String,
  val pendingAmount: String,
  val paymentMode: String,
  val status: String,
  val notes: String,
  val createdAt: Long,
) {
  fun toDomain(): PurchaseRecord =
    PurchaseRecord(
      purchaseId = purchaseId,
      date = date,
      time = time,
      timestamp = timestamp,
      vendorId = vendorId,
      vendorName = vendorName,
      vendorMobile = vendorMobile,
      metal = MetalType.entries.find { it.name == metalType } ?: MetalType.GOLD,
      description = description,
      grossWeight = grossWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      tunch = tunch.toBigDecimalOrNull() ?: BigDecimal("99.50"),
      fineWeight = fineWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      rate = rate.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      rateUnit = RateUnit.fromString(rateUnit),
      gstPercent = gstPercent.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      gstAmount = gstAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      totalAmount = totalAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      amountPaid = amountPaid.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      balancePending = pendingAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      paymentMode = PaymentMode.entries.find { it.name == paymentMode } ?: PaymentMode.BANK_TRANSFER,
      status =
        TransactionStatus.entries.find { it.name == status } ?: TransactionStatus.COMPLETED,
      notes = notes,
      createdAt = createdAt,
    )

  companion object {
    fun fromDomain(domain: PurchaseRecord): PurchaseEntity =
      PurchaseEntity(
        purchaseId = domain.purchaseId,
        date = domain.date,
        time = domain.time,
        timestamp = domain.timestamp,
        vendorId = domain.vendorId,
        vendorName = domain.vendorName,
        vendorMobile = domain.vendorMobile,
        metalType = domain.metal.name,
        description = domain.description,
        grossWeight = domain.grossWeight.toPlainString(),
        tunch = domain.tunch.toPlainString(),
        fineWeight = domain.fineWeight.toPlainString(),
        rate = domain.rate.toPlainString(),
        rateUnit = domain.rateUnit.name,
        gstPercent = domain.gstPercent.toPlainString(),
        gstAmount = domain.gstAmount.toPlainString(),
        totalAmount = domain.totalAmount.toPlainString(),
        amountPaid = domain.amountPaid.toPlainString(),
        pendingAmount = domain.balancePending.toPlainString(),
        paymentMode = domain.paymentMode.name,
        status = domain.status.name,
        notes = domain.notes,
        createdAt = domain.createdAt,
      )
  }
}

@Entity(tableName = "sales")
data class SaleEntity(
  @PrimaryKey val saleId: String,
  val invoiceNumber: String,
  val date: String,
  val time: String,
  val timestamp: Long,
  val customerId: String,
  val customerName: String,
  val customerMobile: String,
  val metalType: String,
  val description: String,
  val grossWeight: String,
  val tunch: String,
  val fineWeight: String,
  val makingCharges: String,
  val rate: String,
  val rateUnit: String,
  val gstPercent: String = "0",
  val cgstAmount: String,
  val sgstAmount: String,
  val igstAmount: String,
  val totalAmount: String,
  val amountReceived: String,
  val pendingAmount: String,
  val paymentMode: String,
  val status: String,
  val notes: String,
  val createdAt: Long,
) {
  fun toDomain(): SaleRecord =
    SaleRecord(
      saleId = saleId,
      invoiceNumber = invoiceNumber,
      date = date,
      time = time,
      timestamp = timestamp,
      customerId = customerId,
      customerName = customerName,
      customerMobile = customerMobile,
      metal = MetalType.entries.find { it.name == metalType } ?: MetalType.GOLD,
      description = description,
      grossWeight = grossWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      tunch = tunch.toBigDecimalOrNull() ?: BigDecimal("91.60"),
      fineWeight = fineWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      rate = rate.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      rateUnit = RateUnit.fromString(rateUnit),
      makingCharges = makingCharges.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      gstPercent = gstPercent.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      cgstAmount = cgstAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      sgstAmount = sgstAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      igstAmount = igstAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      totalAmount = totalAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      amountReceived = amountReceived.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      balancePending = pendingAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      paymentMode = PaymentMode.entries.find { it.name == paymentMode } ?: PaymentMode.CASH,
      status =
        TransactionStatus.entries.find { it.name == status } ?: TransactionStatus.COMPLETED,
      notes = notes,
      createdAt = createdAt,
    )

  companion object {
    fun fromDomain(domain: SaleRecord): SaleEntity =
      SaleEntity(
        saleId = domain.saleId,
        invoiceNumber = domain.invoiceNumber,
        date = domain.date,
        time = domain.time,
        timestamp = domain.timestamp,
        customerId = domain.customerId,
        customerName = domain.customerName,
        customerMobile = domain.customerMobile,
        metalType = domain.metal.name,
        description = domain.description,
        grossWeight = domain.grossWeight.toPlainString(),
        tunch = domain.tunch.toPlainString(),
        fineWeight = domain.fineWeight.toPlainString(),
        makingCharges = domain.makingCharges.toPlainString(),
        rate = domain.rate.toPlainString(),
        rateUnit = domain.rateUnit.name,
        gstPercent = domain.gstPercent.toPlainString(),
        cgstAmount = domain.cgstAmount.toPlainString(),
        sgstAmount = domain.sgstAmount.toPlainString(),
        igstAmount = domain.igstAmount.toPlainString(),
        totalAmount = domain.totalAmount.toPlainString(),
        amountReceived = domain.amountReceived.toPlainString(),
        pendingAmount = domain.balancePending.toPlainString(),
        paymentMode = domain.paymentMode.name,
        status = domain.status.name,
        notes = domain.notes,
        createdAt = domain.createdAt,
      )
  }
}

@Entity(tableName = "scrap_processings")
data class ScrapProcessingEntity(
  @PrimaryKey val processId: String,
  val date: String,
  val createdAt: Long = System.currentTimeMillis(),
  val metal: String,
  val scrapGrossWeight: String,
  val scrapFineWeight: String,
  val meltedGrossWeight: String,
  val outputTunch: String,
  val outputFineWeight: String,
  val meltingLossGrossWeight: String,
  val meltingLossFineWeight: String,
  val referenceRatePerGram: String = "0",
  val notes: String,
) {
  fun toDomain(): ScrapProcessRecord =
    ScrapProcessRecord(
      processId = processId,
      date = date,
      timestamp = createdAt,
      metal = MetalType.entries.find { it.name == metal } ?: MetalType.GOLD,
      scrapGrossWeight = scrapGrossWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      scrapFineWeight = scrapFineWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      meltedGrossWeight = meltedGrossWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      outputTunch = outputTunch.toBigDecimalOrNull() ?: BigDecimal("99.50"),
      outputFineWeight = outputFineWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      meltingLossGrossWeight = meltingLossGrossWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      meltingLossFineWeight = meltingLossFineWeight.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      referenceRatePerGram = referenceRatePerGram.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      notes = notes,
    )

  companion object {
    fun fromDomain(domain: ScrapProcessRecord): ScrapProcessingEntity =
      ScrapProcessingEntity(
        processId = domain.processId,
        date = domain.date,
        createdAt = domain.timestamp,
        metal = domain.metal.name,
        scrapGrossWeight = domain.scrapGrossWeight.toPlainString(),
        scrapFineWeight = domain.scrapFineWeight.toPlainString(),
        meltedGrossWeight = domain.meltedGrossWeight.toPlainString(),
        outputTunch = domain.outputTunch.toPlainString(),
        outputFineWeight = domain.outputFineWeight.toPlainString(),
        meltingLossGrossWeight = domain.meltingLossGrossWeight.toPlainString(),
        meltingLossFineWeight = domain.meltingLossFineWeight.toPlainString(),
        referenceRatePerGram = domain.referenceRatePerGram.toPlainString(),
        notes = domain.notes,
      )
  }
}

@Entity(tableName = "security_settings")
data class SecuritySettingsEntity(
  @PrimaryKey val id: Int = 1,
  val pinEnabled: Boolean,
  val pinCode: String,
  val lockTimeoutMinutes: Int,
  val accessMode: String,
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
) {
  fun toDomain(): AppSecuritySettings =
    AppSecuritySettings(
      pinEnabled = pinEnabled,
      pinCode = pinCode,
      lockTimeoutMinutes = lockTimeoutMinutes,
      accessMode =
        UserAccessMode.entries.find { it.name == accessMode } ?: UserAccessMode.OWNER,
      staffCanCancelTransactions = staffCanCancelTransactions,
      staffCanAdjustInventory = staffCanAdjustInventory,
      staffCanExportBackup = staffCanExportBackup,
      staffCanEditSettings = staffCanEditSettings,
      requireConfirmationForCancel = requireConfirmationForCancel,
      requireReasonForCancel = requireReasonForCancel,
      requireReasonForAdjustment = requireReasonForAdjustment,
      lastLocalBackupTimestamp = lastLocalBackupTimestamp,
      lastDriveBackupTimestamp = lastDriveBackupTimestamp,
      lastBackupFileName = lastBackupFileName,
    )

  companion object {
    fun fromDomain(domain: AppSecuritySettings): SecuritySettingsEntity =
      SecuritySettingsEntity(
        id = 1,
        pinEnabled = domain.pinEnabled,
        pinCode = domain.pinCode,
        lockTimeoutMinutes = domain.lockTimeoutMinutes,
        accessMode = domain.accessMode.name,
        staffCanCancelTransactions = domain.staffCanCancelTransactions,
        staffCanAdjustInventory = domain.staffCanAdjustInventory,
        staffCanExportBackup = domain.staffCanExportBackup,
        staffCanEditSettings = domain.staffCanEditSettings,
        requireConfirmationForCancel = domain.requireConfirmationForCancel,
        requireReasonForCancel = domain.requireReasonForCancel,
        requireReasonForAdjustment = domain.requireReasonForAdjustment,
        lastLocalBackupTimestamp = domain.lastLocalBackupTimestamp,
        lastDriveBackupTimestamp = domain.lastDriveBackupTimestamp,
        lastBackupFileName = domain.lastBackupFileName,
      )
  }
}

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
  @PrimaryKey val actionId: String,
  val timestamp: Long,
  val date: String,
  val time: String,
  val actionType: String,
  val entityId: String,
  val description: String,
  val performedBy: String,
  val reason: String,
) {
  fun toDomain(): AuditLogEntry =
    AuditLogEntry(
      actionId = actionId,
      timestamp = timestamp,
      date = date,
      time = time,
      actionType =
        AuditActionType.entries.find { it.name == actionType }
          ?: AuditActionType.INVENTORY_ADJUSTED,
      entityId = entityId,
      description = description,
      performedBy = performedBy,
      reason = reason,
    )

  companion object {
    fun fromDomain(domain: AuditLogEntry): AuditLogEntity =
      AuditLogEntity(
        actionId = domain.actionId,
        timestamp = domain.timestamp,
        date = domain.date,
        time = domain.time,
        actionType = domain.actionType.name,
        entityId = domain.entityId,
        description = domain.description,
        performedBy = domain.performedBy,
        reason = domain.reason,
      )
  }
}

@Entity(tableName = "shop_reminders")
data class ReminderEntity(
  @PrimaryKey val reminderId: String,
  val customerId: String,
  val customerName: String,
  val customerMobile: String,
  val vendorId: String = "",
  val vendorName: String = "",
  val title: String = "",
  val description: String = "",
  val reminderType: String,
  val dueDate: String,
  val pendingAmount: String,
  val pendingMetalFineGrams: String,
  val notes: String,
  val isCompleted: Boolean,
  val status: String = "PENDING",
  val createdAt: Long,
) {
  fun toDomain(): CustomerReminder {
    val parsedStatus =
      com.example.domain.model.ReminderStatus.fromString(status, fallbackCompleted = isCompleted)
    return CustomerReminder(
      reminderId = reminderId,
      customerId = customerId,
      customerName = customerName,
      customerMobile = customerMobile,
      vendorId = vendorId,
      vendorName = vendorName,
      title = title,
      description = description.ifBlank { notes },
      reminderType =
        ReminderType.entries.find { it.name == reminderType }
          ?: ReminderType.PENDING_PAYMENT,
      dueDate = dueDate,
      pendingAmount = pendingAmount.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      pendingMetalFineGrams = pendingMetalFineGrams.toBigDecimalOrNull() ?: BigDecimal.ZERO,
      notes = notes,
      isCompleted = isCompleted || parsedStatus == com.example.domain.model.ReminderStatus.COMPLETED,
      status = parsedStatus,
      createdAt = createdAt,
    )
  }

  companion object {
    fun fromDomain(domain: CustomerReminder): ReminderEntity =
      ReminderEntity(
        reminderId = domain.reminderId,
        customerId = domain.customerId,
        customerName = domain.customerName,
        customerMobile = domain.customerMobile,
        vendorId = domain.vendorId,
        vendorName = domain.vendorName,
        title = domain.title,
        description = domain.description.ifBlank { domain.notes },
        reminderType = domain.reminderType.name,
        dueDate = domain.dueDate,
        pendingAmount = domain.pendingAmount.toPlainString(),
        pendingMetalFineGrams = domain.pendingMetalFineGrams.toPlainString(),
        notes = domain.notes,
        isCompleted =
          domain.isCompleted || domain.status == com.example.domain.model.ReminderStatus.COMPLETED,
        status = domain.status.name,
        createdAt = domain.createdAt,
      )
  }
}
