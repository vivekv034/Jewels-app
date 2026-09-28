package com.example.domain.service

import com.example.domain.calculation.CalculationEngine
import com.example.domain.model.Customer
import com.example.domain.model.CustomerLedgerEntry
import com.example.domain.model.CustomerLedgerSummary
import com.example.domain.model.DailyReportData
import com.example.domain.model.InventoryMovement
import com.example.domain.model.InventoryMovementDirection
import com.example.domain.model.InventoryMovementType
import com.example.domain.model.MetalInventorySummary
import com.example.domain.model.MetalType
import com.example.domain.model.MonthlyReportData
import com.example.domain.model.OpeningStock
import com.example.domain.model.PurchaseRecord
import com.example.domain.model.SaleRecord
import com.example.domain.model.ScrapProcessRecord
import com.example.domain.model.StockReconciliationRecord
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionStatus
import com.example.domain.model.TransactionType
import com.example.domain.model.Vendor
import com.example.domain.model.VendorLedgerSummary
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Central Inventory, Ledger, Scrap Processing, Stock Reconciliation & Business Report Service (Stage 5 & 6).
 *
 * Strictly adheres to:
 * - Current Stock = Opening Balance + IN movements - OUT movements
 * - Gross Weight and Fine Weight tracked and calculated separately
 * - Gold and Silver inventories and ledgers kept strictly separate
 * - Cancelled transactions generate REVERSAL movements without deleting historical records
 */
class InventoryService(
  private val calculationEngine: CalculationEngine = CalculationEngine()
) {

  fun getOpeningBalance(
    metal: MetalType,
    openingStocks: List<OpeningStock>,
  ): OpeningStock {
    return openingStocks.find { it.metal == metal } ?: OpeningStock(metal = metal)
  }

  fun calculateCurrentGoldStock(
    openingStock: OpeningStock?,
    movements: List<InventoryMovement>,
    currentRatePerGram: BigDecimal,
  ): MetalInventorySummary {
    return calculateStockForMetal(
      metal = MetalType.GOLD,
      openingStock = openingStock,
      movements = movements,
      currentRatePerGram = currentRatePerGram,
    )
  }

  fun calculateCurrentSilverStock(
    openingStock: OpeningStock?,
    movements: List<InventoryMovement>,
    currentRatePerGram: BigDecimal,
  ): MetalInventorySummary {
    return calculateStockForMetal(
      metal = MetalType.SILVER,
      openingStock = openingStock,
      movements = movements,
      currentRatePerGram = currentRatePerGram,
    )
  }

  fun calculateStockForMetal(
    metal: MetalType,
    openingStock: OpeningStock?,
    movements: List<InventoryMovement>,
    currentRatePerGram: BigDecimal,
  ): MetalInventorySummary {
    val openGross = (openingStock?.grossWeight ?: BigDecimal.ZERO).setScale(3, RoundingMode.HALF_UP)
    val openFine = (openingStock?.fineWeight ?: BigDecimal.ZERO).setScale(3, RoundingMode.HALF_UP)

    val metalMovements = movements.filter { it.metal == metal }

    var purchasedGross = BigDecimal.ZERO
    var purchasedFine = BigDecimal.ZERO
    var custRecGross = BigDecimal.ZERO
    var custRecFine = BigDecimal.ZERO
    var adjInGross = BigDecimal.ZERO
    var adjInFine = BigDecimal.ZERO
    var soldGross = BigDecimal.ZERO
    var soldFine = BigDecimal.ZERO
    var custGivenGross = BigDecimal.ZERO
    var custGivenFine = BigDecimal.ZERO
    var scrapRecGross = BigDecimal.ZERO
    var scrapRecFine = BigDecimal.ZERO
    var scrapProcGross = BigDecimal.ZERO
    var scrapProcFine = BigDecimal.ZERO
    var adjOutGross = BigDecimal.ZERO
    var adjOutFine = BigDecimal.ZERO

    var netInGross = BigDecimal.ZERO
    var netInFine = BigDecimal.ZERO
    var netOutGross = BigDecimal.ZERO
    var netOutFine = BigDecimal.ZERO

    for (m in metalMovements) {
      if (m.movementType == InventoryMovementType.OPENING_BALANCE) {
        continue
      }
      if (m.direction == InventoryMovementDirection.IN) {
        netInGross = netInGross.add(m.grossWeight)
        netInFine = netInFine.add(m.fineWeight)
      } else {
        netOutGross = netOutGross.add(m.grossWeight)
        netOutFine = netOutFine.add(m.fineWeight)
      }

      when (m.movementType) {
        InventoryMovementType.OPENING_BALANCE -> Unit
        InventoryMovementType.PURCHASE -> {
          if (m.direction == InventoryMovementDirection.IN) {
            purchasedGross = purchasedGross.add(m.grossWeight)
            purchasedFine = purchasedFine.add(m.fineWeight)
          } else {
            purchasedGross = purchasedGross.subtract(m.grossWeight)
            purchasedFine = purchasedFine.subtract(m.fineWeight)
          }
        }
        InventoryMovementType.CUSTOMER_RECEIVED -> {
          if (m.direction == InventoryMovementDirection.IN) {
            custRecGross = custRecGross.add(m.grossWeight)
            custRecFine = custRecFine.add(m.fineWeight)
          } else {
            custRecGross = custRecGross.subtract(m.grossWeight)
            custRecFine = custRecFine.subtract(m.fineWeight)
          }
        }
        InventoryMovementType.SCRAP_RECEIVED -> {
          if (m.direction == InventoryMovementDirection.IN) {
            scrapRecGross = scrapRecGross.add(m.grossWeight)
            scrapRecFine = scrapRecFine.add(m.fineWeight)
          } else {
            scrapRecGross = scrapRecGross.subtract(m.grossWeight)
            scrapRecFine = scrapRecFine.subtract(m.fineWeight)
          }
        }
        InventoryMovementType.SCRAP_PROCESSED -> {
          if (m.direction == InventoryMovementDirection.OUT) {
            scrapProcGross = scrapProcGross.add(m.grossWeight)
            scrapProcFine = scrapProcFine.add(m.fineWeight)
          } else {
            adjInGross = adjInGross.add(m.grossWeight)
            adjInFine = adjInFine.add(m.fineWeight)
          }
        }
        InventoryMovementType.SALE -> {
          if (m.direction == InventoryMovementDirection.OUT) {
            soldGross = soldGross.add(m.grossWeight)
            soldFine = soldFine.add(m.fineWeight)
          } else {
            soldGross = soldGross.subtract(m.grossWeight)
            soldFine = soldFine.subtract(m.fineWeight)
          }
        }
        InventoryMovementType.CUSTOMER_GIVEN -> {
          if (m.direction == InventoryMovementDirection.OUT) {
            custGivenGross = custGivenGross.add(m.grossWeight)
            custGivenFine = custGivenFine.add(m.fineWeight)
          } else {
            custGivenGross = custGivenGross.subtract(m.grossWeight)
            custGivenFine = custGivenFine.subtract(m.fineWeight)
          }
        }
        InventoryMovementType.ADJUSTMENT,
        InventoryMovementType.REVERSAL -> {
          if (m.direction == InventoryMovementDirection.IN) {
            adjInGross = adjInGross.add(m.grossWeight)
            adjInFine = adjInFine.add(m.fineWeight)
          } else {
            adjOutGross = adjOutGross.add(m.grossWeight)
            adjOutFine = adjOutFine.add(m.fineWeight)
          }
        }
      }
    }

    val currentGross = openGross.add(netInGross).subtract(netOutGross).setScale(3, RoundingMode.HALF_UP)
    val currentFine = openFine.add(netInFine).subtract(netOutFine).setScale(3, RoundingMode.HALF_UP)
    val safeRate = currentRatePerGram.max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
    val estValue =
      currentFine.max(BigDecimal.ZERO).multiply(safeRate).setScale(2, RoundingMode.HALF_UP)

    return MetalInventorySummary(
      metal = metal,
      openingGrossWeight = openGross,
      openingFineWeight = openFine,
      purchasedGrossWeight = purchasedGross.setScale(3, RoundingMode.HALF_UP),
      purchasedFineWeight = purchasedFine.setScale(3, RoundingMode.HALF_UP),
      customerReceivedGrossWeight = custRecGross.setScale(3, RoundingMode.HALF_UP),
      customerReceivedFineWeight = custRecFine.setScale(3, RoundingMode.HALF_UP),
      adjustmentInGrossWeight = adjInGross.setScale(3, RoundingMode.HALF_UP),
      adjustmentInFineWeight = adjInFine.setScale(3, RoundingMode.HALF_UP),
      soldGrossWeight = soldGross.setScale(3, RoundingMode.HALF_UP),
      soldFineWeight = soldFine.setScale(3, RoundingMode.HALF_UP),
      customerGivenGrossWeight = custGivenGross.setScale(3, RoundingMode.HALF_UP),
      customerGivenFineWeight = custGivenFine.setScale(3, RoundingMode.HALF_UP),
      scrapReceivedGrossWeight = scrapRecGross.setScale(3, RoundingMode.HALF_UP),
      scrapReceivedFineWeight = scrapRecFine.setScale(3, RoundingMode.HALF_UP),
      scrapProcessedGrossWeight = scrapProcGross.setScale(3, RoundingMode.HALF_UP),
      scrapProcessedFineWeight = scrapProcFine.setScale(3, RoundingMode.HALF_UP),
      adjustmentOutGrossWeight = adjOutGross.setScale(3, RoundingMode.HALF_UP),
      adjustmentOutFineWeight = adjOutFine.setScale(3, RoundingMode.HALF_UP),
      currentGrossWeight = currentGross,
      currentFineWeight = currentFine,
      currentRatePerGram = safeRate,
      estimatedValue = estValue,
      movementsCount = metalMovements.size,
    )
  }

  fun getInventoryHistory(
    openingStocks: List<OpeningStock> = emptyList(),
    movements: List<InventoryMovement>,
    metalFilter: MetalType? = null,
  ): List<InventoryMovement> {
    val filteredMovements =
      if (metalFilter != null) movements.filter { it.metal == metalFilter } else movements

    val syntheticOpenings =
      openingStocks
        .filter { (metalFilter == null || it.metal == metalFilter) && it.grossWeight > BigDecimal.ZERO }
        .filter { os ->
          filteredMovements.none {
            it.movementType == InventoryMovementType.OPENING_BALANCE && it.metal == os.metal
          }
        }
        .map { os ->
          InventoryMovement(
            movementId = "MOV-OPEN-${os.metal.name}",
            date = os.date,
            transactionId = "OPENING-${os.metal.name}",
            invoiceNumber = "",
            movementType = InventoryMovementType.OPENING_BALANCE,
            metal = os.metal,
            grossWeight = os.grossWeight,
            tunch = os.tunch,
            fineWeight = os.fineWeight,
            rate = os.referenceRatePerGram,
            value =
              os.fineWeight
                .multiply(os.referenceRatePerGram)
                .setScale(2, RoundingMode.HALF_UP),
            direction = InventoryMovementDirection.IN,
            notes = os.notes,
            createdAt = 0L,
          )
        }

    return (filteredMovements + syntheticOpenings).sortedByDescending { it.createdAt }
  }

  /**
   * Maps a finalized customer Transaction to its corresponding InventoryMovement:
   * - MONEY_TO_GOLD / MONEY_TO_SILVER -> CUSTOMER_GIVEN (OUT)
   * - GOLD_PAYMENT / SILVER_PAYMENT -> CUSTOMER_RECEIVED (IN)
   * - SCRAP_GOLD / SCRAP_SILVER -> SCRAP_RECEIVED (IN)
   * - GOLD_ADJUSTMENT / SILVER_ADJUSTMENT -> CUSTOMER_GIVEN (OUT)
   */
  fun createMovementFromTransaction(transaction: Transaction): InventoryMovement? {
    if (
      transaction.status == TransactionStatus.DRAFT ||
        transaction.status == TransactionStatus.CANCELLED ||
        transaction.grossWeight <= BigDecimal.ZERO
    ) {
      return null
    }

    val (movType, direction) =
      when (transaction.transactionType) {
        TransactionType.MONEY_TO_GOLD,
        TransactionType.MONEY_TO_SILVER ->
          InventoryMovementType.CUSTOMER_GIVEN to InventoryMovementDirection.OUT
        TransactionType.GOLD_PAYMENT,
        TransactionType.SILVER_PAYMENT ->
          InventoryMovementType.CUSTOMER_RECEIVED to InventoryMovementDirection.IN
        TransactionType.SCRAP_GOLD,
        TransactionType.SCRAP_SILVER ->
          InventoryMovementType.SCRAP_RECEIVED to InventoryMovementDirection.IN
        TransactionType.GOLD_ADJUSTMENT,
        TransactionType.SILVER_ADJUSTMENT ->
          InventoryMovementType.CUSTOMER_GIVEN to InventoryMovementDirection.OUT
      }

    return InventoryMovement(
      movementId = "MOV-${transaction.transactionId.removePrefix("TXN-")}",
      date = transaction.date,
      transactionId = transaction.transactionId,
      invoiceNumber = transaction.invoiceNumber,
      movementType = movType,
      metal = transaction.metalType,
      grossWeight = transaction.grossWeight.setScale(3, RoundingMode.HALF_UP),
      tunch = transaction.tunch.setScale(2, RoundingMode.HALF_UP),
      fineWeight = transaction.fineWeight.setScale(3, RoundingMode.HALF_UP),
      rate = transaction.normalizedRatePerGram.setScale(2, RoundingMode.HALF_UP),
      value = transaction.amount.setScale(2, RoundingMode.HALF_UP),
      direction = direction,
      notes = "${transaction.transactionType.title} — ${transaction.customerName}",
      createdAt = transaction.timestamp,
    )
  }

  /**
   * Creates a reversal movement when a transaction, purchase, or sale is cancelled.
   * Never deletes the original movement.
   */
  fun reverseInventoryMovement(
    originalMovement: InventoryMovement,
    reversalDate: String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
    reason: String = "Transaction Cancelled",
  ): InventoryMovement {
    val oppositeDir =
      if (originalMovement.direction == InventoryMovementDirection.IN) {
        InventoryMovementDirection.OUT
      } else {
        InventoryMovementDirection.IN
      }
    return InventoryMovement(
      movementId = "REV-${originalMovement.movementId}-${UUID.randomUUID().toString().take(4).uppercase()}",
      date = reversalDate,
      transactionId = originalMovement.transactionId,
      invoiceNumber = originalMovement.invoiceNumber,
      movementType = InventoryMovementType.REVERSAL,
      metal = originalMovement.metal,
      grossWeight = originalMovement.grossWeight,
      tunch = originalMovement.tunch,
      fineWeight = originalMovement.fineWeight,
      rate = originalMovement.rate,
      value = originalMovement.value,
      direction = oppositeDir,
      notes = "REVERSAL of ${originalMovement.movementId}: $reason",
      createdAt = System.currentTimeMillis(),
    )
  }

  fun createReversalMovementsForCancelledTransaction(
    transactionId: String,
    existingMovements: List<InventoryMovement>,
    reversalDate: String,
    reason: String,
  ): List<InventoryMovement> {
    val originals =
      existingMovements.filter {
        it.transactionId == transactionId && it.movementType != InventoryMovementType.REVERSAL
      }
    val alreadyReversed =
      existingMovements.any {
        it.transactionId == transactionId && it.movementType == InventoryMovementType.REVERSAL
      }
    if (alreadyReversed) return emptyList()
    return originals.map { reverseInventoryMovement(it, reversalDate, reason) }
  }

  fun createMovementFromPurchase(purchase: PurchaseRecord): InventoryMovement {
    val normRate = calculationEngine.normalizeRatePerGram(purchase.rate, purchase.rateUnit)
    return InventoryMovement(
      movementId = "MOV-${purchase.purchaseId}",
      date = purchase.date,
      transactionId = purchase.purchaseId,
      invoiceNumber = purchase.purchaseId,
      movementType = InventoryMovementType.PURCHASE,
      metal = purchase.metal,
      grossWeight = purchase.grossWeight.setScale(3, RoundingMode.HALF_UP),
      tunch = purchase.tunch.setScale(2, RoundingMode.HALF_UP),
      fineWeight = purchase.fineWeight.setScale(3, RoundingMode.HALF_UP),
      rate = normRate,
      value = purchase.totalAmount.setScale(2, RoundingMode.HALF_UP),
      direction = InventoryMovementDirection.IN,
      notes = "Purchase from ${purchase.vendorName}: ${purchase.description}",
      createdAt = purchase.timestamp,
    )
  }

  fun createMovementFromSale(sale: SaleRecord): InventoryMovement {
    val normRate = calculationEngine.normalizeRatePerGram(sale.rate, sale.rateUnit)
    return InventoryMovement(
      movementId = "MOV-${sale.saleId}",
      date = sale.date,
      transactionId = sale.saleId,
      invoiceNumber = sale.invoiceNumber,
      movementType = InventoryMovementType.SALE,
      metal = sale.metal,
      grossWeight = sale.grossWeight.setScale(3, RoundingMode.HALF_UP),
      tunch = sale.tunch.setScale(2, RoundingMode.HALF_UP),
      fineWeight = sale.fineWeight.setScale(3, RoundingMode.HALF_UP),
      rate = normRate,
      value = sale.totalAmount.setScale(2, RoundingMode.HALF_UP),
      direction = InventoryMovementDirection.OUT,
      notes = "Sale to ${sale.customerName}: ${sale.description}",
      createdAt = sale.timestamp,
    )
  }

  fun createManualAdjustmentMovement(
    metal: MetalType,
    direction: InventoryMovementDirection,
    grossWeight: BigDecimal,
    tunch: BigDecimal,
    ratePerGram: BigDecimal,
    date: String,
    reason: String,
    notes: String,
  ): InventoryMovement {
    val safeGross = grossWeight.setScale(3, RoundingMode.HALF_UP)
    val safeTunch = tunch.setScale(2, RoundingMode.HALF_UP)
    val fine = calculationEngine.calculateFineWeight(safeGross, safeTunch).fineWeight
    val value = fine.multiply(ratePerGram).setScale(2, RoundingMode.HALF_UP)
    val id = "MOV-ADJ-${UUID.randomUUID().toString().take(6).uppercase()}"
    return InventoryMovement(
      movementId = id,
      date = date,
      transactionId = id,
      invoiceNumber = "",
      movementType = InventoryMovementType.ADJUSTMENT,
      metal = metal,
      grossWeight = safeGross,
      tunch = safeTunch,
      fineWeight = fine,
      rate = ratePerGram.setScale(2, RoundingMode.HALF_UP),
      value = value,
      direction = direction,
      notes = "${reason.trim()}${if (notes.isNotBlank()) " — ${notes.trim()}" else ""}",
      createdAt = System.currentTimeMillis(),
    )
  }

  /**
   * Scrap Processing (Stage 5 Section 11):
   * 1. Deducts Scrap Pool via SCRAP_PROCESSED OUT movement
   * 2. Adds Melted/Refined Fine Metal via SCRAP_PROCESSED IN movement
   * 3. Records Melting Loss
   */
  fun createScrapProcessingMovements(
    metal: MetalType,
    scrapGrossWeight: BigDecimal,
    scrapFineWeight: BigDecimal,
    meltedGrossWeight: BigDecimal,
    outputTunch: BigDecimal,
    referenceRatePerGram: BigDecimal,
    date: String,
    notes: String,
  ): Pair<ScrapProcessRecord, List<InventoryMovement>> {
    val safeScrapGross = scrapGrossWeight.setScale(3, RoundingMode.HALF_UP)
    val safeScrapFine =
      if (scrapFineWeight > BigDecimal.ZERO) {
        scrapFineWeight.setScale(3, RoundingMode.HALF_UP)
      } else {
        calculationEngine.calculateFineWeight(safeScrapGross, outputTunch).fineWeight
      }
    val safeMeltedGross = meltedGrossWeight.setScale(3, RoundingMode.HALF_UP)
    val safeOutTunch = outputTunch.setScale(2, RoundingMode.HALF_UP)
    val outputFine = calculationEngine.calculateFineWeight(safeMeltedGross, safeOutTunch).fineWeight

    val lossGross = safeScrapGross.subtract(safeMeltedGross).max(BigDecimal.ZERO).setScale(3, RoundingMode.HALF_UP)
    val lossFine = safeScrapFine.subtract(outputFine).max(BigDecimal.ZERO).setScale(3, RoundingMode.HALF_UP)

    val processId = "SCRAP-PROC-${UUID.randomUUID().toString().take(6).uppercase()}"
    val now = System.currentTimeMillis()
    val record =
      ScrapProcessRecord(
        processId = processId,
        date = date,
        timestamp = now,
        metal = metal,
        scrapGrossWeight = safeScrapGross,
        scrapFineWeight = safeScrapFine,
        meltedGrossWeight = safeMeltedGross,
        outputTunch = safeOutTunch,
        outputFineWeight = outputFine,
        meltingLossGrossWeight = lossGross,
        meltingLossFineWeight = lossFine,
        referenceRatePerGram = referenceRatePerGram.setScale(2, RoundingMode.HALF_UP),
        notes = notes.trim(),
      )

    val scrapInputTunch =
      if (safeScrapGross > BigDecimal.ZERO) {
        safeScrapFine.multiply(BigDecimal("100")).divide(safeScrapGross, 2, RoundingMode.HALF_UP)
      } else safeOutTunch

    val outMov =
      InventoryMovement(
        movementId = "MOV-$processId-OUT",
        date = date,
        transactionId = processId,
        invoiceNumber = "",
        movementType = InventoryMovementType.SCRAP_PROCESSED,
        metal = metal,
        grossWeight = safeScrapGross,
        tunch = scrapInputTunch,
        fineWeight = safeScrapFine,
        rate = referenceRatePerGram,
        value = safeScrapFine.multiply(referenceRatePerGram).setScale(2, RoundingMode.HALF_UP),
        direction = InventoryMovementDirection.OUT,
        notes = "Scrap Sent for Melting/Refining ($notes)".trim(),
        createdAt = now,
      )

    val inMov =
      InventoryMovement(
        movementId = "MOV-$processId-IN",
        date = date,
        transactionId = processId,
        invoiceNumber = "",
        movementType = InventoryMovementType.SCRAP_PROCESSED,
        metal = metal,
        grossWeight = safeMeltedGross,
        tunch = safeOutTunch,
        fineWeight = outputFine,
        rate = referenceRatePerGram,
        value = outputFine.multiply(referenceRatePerGram).setScale(2, RoundingMode.HALF_UP),
        direction = InventoryMovementDirection.IN,
        notes = "Refined ${metal.displayName} Output (Loss: ${lossGross.toPlainString()} g gross)",
        createdAt = now + 1,
      )

    return record to listOf(outMov, inMov)
  }

  fun reconcileStock(
    currentSummary: MetalInventorySummary,
    physicalGrossWeight: BigDecimal,
    physicalFineWeight: BigDecimal,
    date: String,
    notes: String,
  ): StockReconciliationRecord {
    val diffGross =
      physicalGrossWeight.subtract(currentSummary.currentGrossWeight).setScale(3, RoundingMode.HALF_UP)
    val diffFine =
      physicalFineWeight.subtract(currentSummary.currentFineWeight).setScale(3, RoundingMode.HALF_UP)
    return StockReconciliationRecord(
      reconciliationId = "REC-${UUID.randomUUID().toString().take(6).uppercase()}",
      date = date,
      timestamp = System.currentTimeMillis(),
      metal = currentSummary.metal,
      systemGrossWeight = currentSummary.currentGrossWeight,
      systemFineWeight = currentSummary.currentFineWeight,
      physicalGrossWeight = physicalGrossWeight,
      physicalFineWeight = physicalFineWeight,
      differenceGrossWeight = diffGross,
      differenceFineWeight = diffFine,
      notes = notes.trim(),
    )
  }

  fun calculateVendorLedger(
    vendor: Vendor,
    purchases: List<PurchaseRecord>,
  ): VendorLedgerSummary {
    val active =
      purchases.filter {
        it.vendorId == vendor.vendorId && it.status != TransactionStatus.CANCELLED
      }
    val goldFine =
      active
        .filter { it.metal == MetalType.GOLD }
        .fold(BigDecimal.ZERO) { acc, p -> acc.add(p.fineWeight) }
        .setScale(3, RoundingMode.HALF_UP)
    val silverFine =
      active
        .filter { it.metal == MetalType.SILVER }
        .fold(BigDecimal.ZERO) { acc, p -> acc.add(p.fineWeight) }
        .setScale(3, RoundingMode.HALF_UP)
    val totalAmt =
      active
        .fold(BigDecimal.ZERO) { acc, p -> acc.add(p.totalAmount) }
        .setScale(2, RoundingMode.HALF_UP)
    val paidAmt =
      active
        .fold(BigDecimal.ZERO) { acc, p -> acc.add(p.amountPaid) }
        .setScale(2, RoundingMode.HALF_UP)
    val unpaidFromPurchases =
      active
        .fold(BigDecimal.ZERO) { acc, p -> acc.add(p.balancePending) }
        .setScale(2, RoundingMode.HALF_UP)

    return VendorLedgerSummary(
      vendorId = vendor.vendorId,
      vendorName = vendor.name,
      totalPurchasesCount = active.size,
      totalGoldPurchasedFineGrams = goldFine,
      totalSilverPurchasedFineGrams = silverFine,
      totalPurchaseAmount = totalAmt,
      totalAmountPaid = paidAmt,
      pendingMoneyBalance = vendor.pendingMoney.max(unpaidFromPurchases),
      pendingGoldFineGrams = vendor.pendingGoldFineGrams,
      pendingSilverFineGrams = vendor.pendingSilverFineGrams,
    )
  }

  /**
   * Calculates full Customer Ledger across Transactions and Direct Sales.
   * Keeps Money, Gold, and Silver strictly separate.
   */
  fun calculateCustomerFullLedger(
    customer: Customer,
    transactions: List<Transaction>,
    sales: List<SaleRecord>,
  ): CustomerLedgerSummary {
    val base =
      calculationEngine.calculateCustomerLedger(
        customerId = customer.id,
        customerName = customer.name,
        transactions = transactions,
      )
    val activeSales =
      sales.filter {
        it.customerId == customer.id && it.status != TransactionStatus.CANCELLED
      }
    val saleGoldOut =
      activeSales
        .filter { it.metal == MetalType.GOLD }
        .fold(BigDecimal.ZERO) { acc, s -> acc.add(s.fineWeight) }
    val saleSilverOut =
      activeSales
        .filter { it.metal == MetalType.SILVER }
        .fold(BigDecimal.ZERO) { acc, s -> acc.add(s.fineWeight) }
    val saleMoneyReceived =
      activeSales.fold(BigDecimal.ZERO) { acc, s -> acc.add(s.amountReceived) }
    val saleTotalValue =
      activeSales.fold(BigDecimal.ZERO) { acc, s -> acc.add(s.totalAmount) }

    val totalGoldRec = base.goldReceivedGrams.setScale(3, RoundingMode.HALF_UP)
    val totalGoldGiven = base.goldGivenGrams.add(saleGoldOut).setScale(3, RoundingMode.HALF_UP)
    val netGold = totalGoldRec.subtract(totalGoldGiven).setScale(3, RoundingMode.HALF_UP)

    val totalSilverRec = base.silverReceivedGrams.setScale(3, RoundingMode.HALF_UP)
    val totalSilverGiven = base.silverGivenGrams.add(saleSilverOut).setScale(3, RoundingMode.HALF_UP)
    val netSilver = totalSilverRec.subtract(totalSilverGiven).setScale(3, RoundingMode.HALF_UP)

    val totalMoneyPaid =
      base.totalMoneyPaidByCustomer.add(saleMoneyReceived).setScale(2, RoundingMode.HALF_UP)
    val totalTxVal =
      base.transactions
        .filter { it.status != TransactionStatus.CANCELLED && it.status != TransactionStatus.DRAFT }
        .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
        .add(saleTotalValue)
        .setScale(2, RoundingMode.HALF_UP)

    val latestDate =
      (base.transactions.map { it.date } + activeSales.map { it.date }).maxOrNull() ?: ""

    return base.copy(
      customerMobile = customer.mobileNumber,
      totalTransactionsCount =
        base.transactions.count { it.status != TransactionStatus.CANCELLED && it.status != TransactionStatus.DRAFT } +
          activeSales.size,
      goldGivenGrams = totalGoldGiven,
      goldReceivedGrams = totalGoldRec,
      silverGivenGrams = totalSilverGiven,
      silverReceivedGrams = totalSilverRec,
      moneyReceived = totalMoneyPaid,
      netGoldBalanceGrams = netGold,
      netSilverBalanceGrams = netSilver,
      totalMoneyPaidByCustomer = totalMoneyPaid,
      totalGoldReceivedFineGrams = totalGoldRec,
      totalGoldGivenFineGrams = totalGoldGiven,
      netGoldBalanceFineGrams = netGold,
      totalSilverReceivedFineGrams = totalSilverRec,
      totalSilverGivenFineGrams = totalSilverGiven,
      netSilverBalanceFineGrams = netSilver,
      totalTransactionValue = totalTxVal,
      pendingMoneyBalance = customer.pendingAmount.setScale(2, RoundingMode.HALF_UP),
      runningPendingBalance = customer.pendingAmount.setScale(2, RoundingMode.HALF_UP),
      lastTransactionDate = latestDate,
    )
  }

  fun buildCustomerLedgerEntries(
    customer: Customer,
    transactions: List<Transaction>,
    sales: List<SaleRecord>,
  ): List<CustomerLedgerEntry> {
    val txEntries =
      transactions
        .filter { it.customerId == customer.id && it.status != TransactionStatus.DRAFT }
        .map { tx ->
          val (mIn, mOut) =
            when (tx.transactionType) {
              TransactionType.MONEY_TO_GOLD,
              TransactionType.MONEY_TO_SILVER -> tx.amount to BigDecimal.ZERO
              TransactionType.SCRAP_GOLD,
              TransactionType.SCRAP_SILVER -> BigDecimal.ZERO to tx.amount
              else -> tx.cashReceived to tx.cashPaid
            }
          val (gIn, gOut) =
            when (tx.transactionType) {
              TransactionType.GOLD_PAYMENT,
              TransactionType.SCRAP_GOLD -> tx.fineWeight to BigDecimal.ZERO
              TransactionType.MONEY_TO_GOLD,
              TransactionType.GOLD_ADJUSTMENT -> BigDecimal.ZERO to tx.fineWeight
              else -> BigDecimal.ZERO to BigDecimal.ZERO
            }
          val (sIn, sOut) =
            when (tx.transactionType) {
              TransactionType.SILVER_PAYMENT,
              TransactionType.SCRAP_SILVER -> tx.fineWeight to BigDecimal.ZERO
              TransactionType.MONEY_TO_SILVER,
              TransactionType.SILVER_ADJUSTMENT -> BigDecimal.ZERO to tx.fineWeight
              else -> BigDecimal.ZERO to BigDecimal.ZERO
            }
          CustomerLedgerEntry(
            entryId = tx.transactionId,
            transactionId = tx.transactionId,
            invoiceNumber = tx.invoiceNumber,
            date = tx.date,
            time = tx.time,
            timestamp = tx.timestamp,
            typeLabel = tx.transactionType.title,
            description = tx.notes.ifBlank { tx.transactionType.title },
            transactionType = tx.transactionType,
            status = tx.status,
            moneyPaidByCustomer = mIn,
            moneyReceivedByCustomer = mOut,
            goldGivenByCustomerGrams = gIn,
            goldReceivedByCustomerGrams = gOut,
            silverGivenByCustomerGrams = sIn,
            silverReceivedByCustomerGrams = sOut,
            moneyIn = mIn,
            moneyOut = mOut,
            goldInFineGrams = gIn,
            goldOutFineGrams = gOut,
            silverInFineGrams = sIn,
            silverOutFineGrams = sOut,
            isCancelled = tx.status == TransactionStatus.CANCELLED,
            notes = tx.notes,
          )
        }

    val saleEntries =
      sales
        .filter { it.customerId == customer.id }
        .map { sale ->
          val gOut = if (sale.metal == MetalType.GOLD) sale.fineWeight else BigDecimal.ZERO
          val sOut = if (sale.metal == MetalType.SILVER) sale.fineWeight else BigDecimal.ZERO
          CustomerLedgerEntry(
            entryId = sale.saleId,
            transactionId = sale.saleId,
            invoiceNumber = sale.invoiceNumber,
            date = sale.date,
            time = sale.time,
            timestamp = sale.timestamp,
            typeLabel = "${sale.metal.displayName} Sale",
            description = sale.description,
            status = sale.status,
            moneyPaidByCustomer = sale.amountReceived,
            moneyReceivedByCustomer = BigDecimal.ZERO,
            goldGivenByCustomerGrams = BigDecimal.ZERO,
            goldReceivedByCustomerGrams = gOut,
            silverGivenByCustomerGrams = BigDecimal.ZERO,
            silverReceivedByCustomerGrams = sOut,
            moneyIn = sale.amountReceived,
            moneyOut = BigDecimal.ZERO,
            goldInFineGrams = BigDecimal.ZERO,
            goldOutFineGrams = gOut,
            silverInFineGrams = BigDecimal.ZERO,
            silverOutFineGrams = sOut,
            isCancelled = sale.status == TransactionStatus.CANCELLED,
            notes = sale.notes,
          )
        }

    return (txEntries + saleEntries).sortedByDescending { it.timestamp }
  }

  fun generateDailyReport(
    date: String,
    transactions: List<Transaction>,
    purchases: List<PurchaseRecord>,
    sales: List<SaleRecord>,
    customers: List<Customer>,
    goldStock: MetalInventorySummary,
    silverStock: MetalInventorySummary,
  ): DailyReportData {
    val dayTxs =
      transactions.filter {
        it.date == date &&
          it.status != TransactionStatus.CANCELLED &&
          it.status != TransactionStatus.DRAFT
      }
    val dayPurchases =
      purchases.filter { it.date == date && it.status != TransactionStatus.CANCELLED }
    val daySales =
      sales.filter { it.date == date && it.status != TransactionStatus.CANCELLED }

    val moneyInFromTxs =
      dayTxs
        .filter {
          it.transactionType == TransactionType.MONEY_TO_GOLD ||
            it.transactionType == TransactionType.MONEY_TO_SILVER
        }
        .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
    val moneyInFromSales =
      daySales.fold(BigDecimal.ZERO) { acc, s -> acc.add(s.amountReceived) }

    val moneyOutFromScrap =
      dayTxs
        .filter {
          it.transactionType == TransactionType.SCRAP_GOLD ||
            it.transactionType == TransactionType.SCRAP_SILVER
        }
        .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
    val moneyOutFromPurchases =
      dayPurchases.fold(BigDecimal.ZERO) { acc, p -> acc.add(p.amountPaid) }

    val goldRec =
      dayTxs
        .filter { it.transactionType == TransactionType.GOLD_PAYMENT }
        .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.fineWeight) }
    val goldGiven =
      dayTxs
        .filter {
          it.transactionType == TransactionType.MONEY_TO_GOLD ||
            it.transactionType == TransactionType.GOLD_ADJUSTMENT
        }
        .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.fineWeight) }

    val silverRec =
      dayTxs
        .filter { it.transactionType == TransactionType.SILVER_PAYMENT }
        .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.fineWeight) }
    val silverGiven =
      dayTxs
        .filter {
          it.transactionType == TransactionType.MONEY_TO_SILVER ||
            it.transactionType == TransactionType.SILVER_ADJUSTMENT
        }
        .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.fineWeight) }

    val scrapGold =
      dayTxs
        .filter { it.transactionType == TransactionType.SCRAP_GOLD }
        .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.fineWeight) }
    val scrapSilver =
      dayTxs
        .filter { it.transactionType == TransactionType.SCRAP_SILVER }
        .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.fineWeight) }

    val goldPur =
      dayPurchases
        .filter { it.metal == MetalType.GOLD }
        .fold(BigDecimal.ZERO) { acc, p -> acc.add(p.fineWeight) }
    val silverPur =
      dayPurchases
        .filter { it.metal == MetalType.SILVER }
        .fold(BigDecimal.ZERO) { acc, p -> acc.add(p.fineWeight) }

    val goldSold =
      daySales
        .filter { it.metal == MetalType.GOLD }
        .fold(BigDecimal.ZERO) { acc, s -> acc.add(s.fineWeight) }
    val silverSold =
      daySales
        .filter { it.metal == MetalType.SILVER }
        .fold(BigDecimal.ZERO) { acc, s -> acc.add(s.fineWeight) }

    val pendingCustomer =
      customers.fold(BigDecimal.ZERO) { acc, c -> acc.add(c.pendingAmount) }

    return DailyReportData(
      date = date,
      totalTransactionsCount = dayTxs.size,
      totalPurchasesCount = dayPurchases.size,
      totalSalesCount = daySales.size,
      moneyReceived = moneyInFromTxs.add(moneyInFromSales).setScale(2, RoundingMode.HALF_UP),
      moneyPaid = moneyOutFromScrap.add(moneyOutFromPurchases).setScale(2, RoundingMode.HALF_UP),
      goldReceivedFineGrams = goldRec.setScale(3, RoundingMode.HALF_UP),
      goldGivenFineGrams = goldGiven.setScale(3, RoundingMode.HALF_UP),
      silverReceivedFineGrams = silverRec.setScale(3, RoundingMode.HALF_UP),
      silverGivenFineGrams = silverGiven.setScale(3, RoundingMode.HALF_UP),
      scrapGoldFineGrams = scrapGold.setScale(3, RoundingMode.HALF_UP),
      scrapSilverFineGrams = scrapSilver.setScale(3, RoundingMode.HALF_UP),
      goldPurchasedFineGrams = goldPur.setScale(3, RoundingMode.HALF_UP),
      silverPurchasedFineGrams = silverPur.setScale(3, RoundingMode.HALF_UP),
      goldSoldFineGrams = goldSold.setScale(3, RoundingMode.HALF_UP),
      silverSoldFineGrams = silverSold.setScale(3, RoundingMode.HALF_UP),
      currentGoldStock = goldStock,
      currentSilverStock = silverStock,
      pendingCustomerMoney = pendingCustomer.setScale(2, RoundingMode.HALF_UP),
    )
  }

  fun generateMonthlyReport(
    yearMonth: String,
    transactions: List<Transaction>,
    purchases: List<PurchaseRecord>,
    sales: List<SaleRecord>,
    movements: List<InventoryMovement>,
    goldRatePerGram: BigDecimal,
    silverRatePerGram: BigDecimal,
  ): MonthlyReportData {
    val monthTxs =
      transactions.filter {
        it.date.startsWith(yearMonth) &&
          it.status != TransactionStatus.CANCELLED &&
          it.status != TransactionStatus.DRAFT
      }
    val monthPurchases =
      purchases.filter {
        it.date.startsWith(yearMonth) && it.status != TransactionStatus.CANCELLED
      }
    val monthSales =
      sales.filter {
        it.date.startsWith(yearMonth) && it.status != TransactionStatus.CANCELLED
      }
    val monthMovs = movements.filter { it.date.startsWith(yearMonth) }

    val moneyIn =
      monthTxs
        .filter {
          it.transactionType == TransactionType.MONEY_TO_GOLD ||
            it.transactionType == TransactionType.MONEY_TO_SILVER
        }
        .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
        .add(monthSales.fold(BigDecimal.ZERO) { acc, s -> acc.add(s.amountReceived) })
        .setScale(2, RoundingMode.HALF_UP)

    val moneyOut =
      monthTxs
        .filter {
          it.transactionType == TransactionType.SCRAP_GOLD ||
            it.transactionType == TransactionType.SCRAP_SILVER
        }
        .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
        .add(monthPurchases.fold(BigDecimal.ZERO) { acc, p -> acc.add(p.amountPaid) })
        .setScale(2, RoundingMode.HALF_UP)

    val goldIn =
      monthMovs
        .filter { it.metal == MetalType.GOLD && it.direction == InventoryMovementDirection.IN }
        .fold(BigDecimal.ZERO) { acc, m -> acc.add(m.fineWeight) }
        .setScale(3, RoundingMode.HALF_UP)
    val goldOut =
      monthMovs
        .filter { it.metal == MetalType.GOLD && it.direction == InventoryMovementDirection.OUT }
        .fold(BigDecimal.ZERO) { acc, m -> acc.add(m.fineWeight) }
        .setScale(3, RoundingMode.HALF_UP)
    val netGold = goldIn.subtract(goldOut).setScale(3, RoundingMode.HALF_UP)

    val silverIn =
      monthMovs
        .filter { it.metal == MetalType.SILVER && it.direction == InventoryMovementDirection.IN }
        .fold(BigDecimal.ZERO) { acc, m -> acc.add(m.fineWeight) }
        .setScale(3, RoundingMode.HALF_UP)
    val silverOut =
      monthMovs
        .filter { it.metal == MetalType.SILVER && it.direction == InventoryMovementDirection.OUT }
        .fold(BigDecimal.ZERO) { acc, m -> acc.add(m.fineWeight) }
        .setScale(3, RoundingMode.HALF_UP)
    val netSilver = silverIn.subtract(silverOut).setScale(3, RoundingMode.HALF_UP)

    val makingEarned =
      monthSales
        .fold(BigDecimal.ZERO) { acc, s -> acc.add(s.makingCharges) }
        .setScale(2, RoundingMode.HALF_UP)
    val gstCollected =
      monthSales
        .fold(BigDecimal.ZERO) { acc, s -> acc.add(s.gstAmount) }
        .setScale(2, RoundingMode.HALF_UP)

    val netCash = moneyIn.subtract(moneyOut).setScale(2, RoundingMode.HALF_UP)
    val metalDeltaValue =
      netGold
        .multiply(goldRatePerGram)
        .add(netSilver.multiply(silverRatePerGram))
        .setScale(2, RoundingMode.HALF_UP)

    return MonthlyReportData(
      yearMonth = yearMonth,
      totalTransactionsCount = monthTxs.size,
      totalPurchasesCount = monthPurchases.size,
      totalSalesCount = monthSales.size,
      totalMoneyInflow = moneyIn,
      totalMoneyOutflow = moneyOut,
      netCashFlow = netCash,
      totalGoldInFineGrams = goldIn,
      totalGoldOutFineGrams = goldOut,
      netGoldChangeFineGrams = netGold,
      totalSilverInFineGrams = silverIn,
      totalSilverOutFineGrams = silverOut,
      netSilverChangeFineGrams = netSilver,
      totalMakingChargesEarned = makingEarned,
      totalGstCollected = gstCollected,
      estimatedNetValueCreated = netCash.add(metalDeltaValue).setScale(2, RoundingMode.HALF_UP),
    )
  }
}
