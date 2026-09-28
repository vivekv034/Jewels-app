package com.example.domain.calculation

import com.example.domain.model.CustomerLedgerEntry
import com.example.domain.model.CustomerLedgerSummary
import com.example.domain.model.DeductionType
import com.example.domain.model.LedgerEffectCategory
import com.example.domain.model.RateUnit
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionStatus
import com.example.domain.model.TransactionType
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

data class CalculationConfig(
  val weightScale: Int = 3,
  val moneyScale: Int = 2,
  val tunchScale: Int = 2,
  val intermediateScale: Int = 10,
  val purityDivisor: BigDecimal = BigDecimal("100"),
  val maxTunchValue: BigDecimal = BigDecimal("100.00"),
  val roundingMode: RoundingMode = RoundingMode.HALF_UP,
)

data class MetalWeightCalculationResult(
  val amount: BigDecimal,
  val rate: BigDecimal,
  val rateUnit: RateUnit,
  val normalizedRatePerGram: BigDecimal,
  val metalWeight: BigDecimal,
  val formulaBreakdown: String,
)

data class MetalConversionResult(
  val moneyAmount: BigDecimal,
  val enteredRate: BigDecimal,
  val rateUnit: RateUnit,
  val effectiveRatePerGram: BigDecimal,
  val metalQuantityGrams: BigDecimal,
  val tunch: BigDecimal = BigDecimal("100.00"),
  val fineWeightGrams: BigDecimal = metalQuantityGrams,
  val formulaBreakdown: String,
)

data class PurityCalculationResult(
  val grossWeight: BigDecimal,
  val tunch: BigDecimal,
  val divisor: BigDecimal,
  val fineWeight: BigDecimal,
  val formulaBreakdown: String,
) {
  fun toPlainString(): String = fineWeight.toPlainString()
}

data class MetalValueCalculationResult(
  val fineWeight: BigDecimal,
  val rate: BigDecimal,
  val rateUnit: RateUnit,
  val normalizedRatePerGram: BigDecimal,
  val metalValue: BigDecimal,
  val formulaBreakdown: String,
)

data class DeductionCalculationResult(
  val deductionType: DeductionType,
  val deductionInput: BigDecimal,
  val metalValue: BigDecimal,
  val deductionAmount: BigDecimal,
  val netValue: BigDecimal,
  val deductionBreakdown: String,
)

data class ScrapCalculationResult(
  val grossWeight: BigDecimal,
  val tunch: BigDecimal,
  val fineWeight: BigDecimal,
  val enteredRate: BigDecimal,
  val rateUnit: RateUnit,
  val effectiveRatePerGram: BigDecimal,
  val calculatedValue: BigDecimal,
  val metalValue: BigDecimal = calculatedValue,
  val deductionType: DeductionType = DeductionType.NONE,
  val deductionInput: BigDecimal = BigDecimal.ZERO,
  val deductions: BigDecimal = BigDecimal.ZERO,
  val deductionAmount: BigDecimal = deductions,
  val netValue: BigDecimal = calculatedValue,
  val fineWeightFormula: String,
  val valueFormula: String,
  val netValueFormula: String = "₹${calculatedValue.toPlainString()}",
)

data class MultipleItemsCalculationResult(
  val items: List<TransactionItem>,
  val calculatedItems: List<TransactionItem> = items,
  val totalGrossWeight: BigDecimal,
  val effectiveAverageTunch: BigDecimal,
  val weightedAverageTunch: BigDecimal = effectiveAverageTunch,
  val totalFineWeight: BigDecimal,
  val totalMetalValue: BigDecimal,
  val deductions: BigDecimal,
  val deductionAmount: BigDecimal = deductions,
  val netValue: BigDecimal,
)

typealias MultiItemSummaryResult = MultipleItemsCalculationResult

data class PaymentAdjustmentResult(
  val metalValue: BigDecimal,
  val cashPaid: BigDecimal,
  val cashReceived: BigDecimal,
  val remainingBalance: BigDecimal,
)

sealed class ValidationResult {
  data object Valid : ValidationResult()

  data class Invalid(val message: String) : ValidationResult()
}

/**
 * Central Jewellery Calculation & Purity Engine (Stage 3).
 * Uses BigDecimal exclusively for all weight, tunch, rate, and money calculations.
 * Never duplicates formulas across screens and never applies unconfigured wastage/charges.
 */
class CalculationEngine(val config: CalculationConfig = CalculationConfig()) {

  companion object {
    val PURE_99_TUNCH: BigDecimal = BigDecimal("99.00")
    private val HUNDRED: BigDecimal = BigDecimal("100")
  }

  /**
   * Central rate normalization function:
   * Supported units: PER_GRAM (GRAM), PER_10_GRAMS (10_GRAM), PER_KILOGRAM (KILOGRAM).
   */
  fun normalizeRate(rate: BigDecimal, unit: RateUnit = RateUnit.PER_GRAM): BigDecimal {
    if (rate <= BigDecimal.ZERO) {
      return BigDecimal.ZERO.setScale(config.moneyScale, config.roundingMode)
    }
    val unitGrams = BigDecimal(unit.gramsInUnit)
    val divided = rate.divide(unitGrams, config.intermediateScale, config.roundingMode)
    val stripped = divided.stripTrailingZeros()
    val targetScale = maxOf(config.moneyScale, stripped.scale())
    return divided.setScale(targetScale, config.roundingMode)
  }

  fun normalizeRatePerGram(rate: BigDecimal, unit: RateUnit = RateUnit.PER_GRAM): BigDecimal {
    return normalizeRate(rate, unit)
  }

  fun convertRateBetweenUnits(
    rate: BigDecimal,
    fromUnit: RateUnit,
    toUnit: RateUnit,
  ): BigDecimal {
    if (rate <= BigDecimal.ZERO) return BigDecimal.ZERO
    val perGram = normalizeRate(rate, fromUnit)
    return perGram
      .multiply(BigDecimal(toUnit.gramsInUnit))
      .setScale(config.moneyScale, config.roundingMode)
  }

  /**
   * Central Fine Weight Calculation Engine:
   * Formula: fineWeight = grossWeight * tunch / 100 (or configured divisor)
   */
  fun calculateFineWeight(
    grossWeight: BigDecimal,
    tunch: BigDecimal,
    customDivisor: BigDecimal = config.purityDivisor,
    weightScale: Int = config.weightScale,
  ): PurityCalculationResult {
    val safeGross = grossWeight.max(BigDecimal.ZERO).setScale(weightScale, config.roundingMode)
    val safeTunch = tunch.max(BigDecimal.ZERO).setScale(config.tunchScale, config.roundingMode)
    val divisor = if (customDivisor > BigDecimal.ZERO) customDivisor else HUNDRED

    val fineWeight =
      safeGross
        .multiply(safeTunch)
        .divide(divisor, weightScale, config.roundingMode)

    val breakdown =
      "${formatWeight(safeGross, weightScale)} g × ${formatTunch(safeTunch)} ÷ ${divisor.stripTrailingZeros().toPlainString()} = ${formatWeight(fineWeight, weightScale)} g"

    return PurityCalculationResult(
      grossWeight = safeGross,
      tunch = safeTunch,
      divisor = divisor,
      fineWeight = fineWeight,
      formulaBreakdown = breakdown,
    )
  }

  /**
   * Central Metal Value Calculation Engine (Detailed):
   * Formula: metalValue = fineWeight * rate (normalized per gram)
   */
  fun calculateMetalValueDetailed(
    fineWeight: BigDecimal,
    rate: BigDecimal,
    unit: RateUnit = RateUnit.PER_GRAM,
    moneyScale: Int = config.moneyScale,
    weightScale: Int = config.weightScale,
  ): MetalValueCalculationResult {
    val safeFine = fineWeight.max(BigDecimal.ZERO).setScale(weightScale, config.roundingMode)
    val safeRate = rate.max(BigDecimal.ZERO).setScale(moneyScale, config.roundingMode)
    val normalizedPerGram = normalizeRate(safeRate, unit)

    val metalValue =
      if (safeFine > BigDecimal.ZERO && normalizedPerGram > BigDecimal.ZERO) {
        safeFine.multiply(normalizedPerGram).setScale(moneyScale, config.roundingMode)
      } else {
        BigDecimal.ZERO.setScale(moneyScale, config.roundingMode)
      }

    val displayPerGram = normalizedPerGram.setScale(moneyScale, config.roundingMode)
    val unitLabel =
      if (unit == RateUnit.PER_GRAM) {
        "₹${formatMoney(displayPerGram)}/g"
      } else {
        "₹${formatMoney(displayPerGram)}/g (₹${formatMoney(safeRate)} ${unit.shortLabel})"
      }

    val breakdown =
      "${formatWeight(safeFine, weightScale)} g × $unitLabel = ₹${formatMoney(metalValue, moneyScale)}"

    return MetalValueCalculationResult(
      fineWeight = safeFine,
      rate = safeRate,
      rateUnit = unit,
      normalizedRatePerGram = displayPerGram,
      metalValue = metalValue,
      formulaBreakdown = breakdown,
    )
  }

  /**
   * Central Metal Value Calculation Engine:
   * Formula: metalValue = fineWeight * rate (normalized per gram)
   */
  fun calculateMetalValue(
    fineWeight: BigDecimal,
    rate: BigDecimal,
    unit: RateUnit = RateUnit.PER_GRAM,
    moneyScale: Int = config.moneyScale,
    weightScale: Int = config.weightScale,
  ): BigDecimal {
    return calculateMetalValueDetailed(fineWeight, rate, unit, moneyScale, weightScale).metalValue
  }

  fun calculateMetalValue(
    purityResult: PurityCalculationResult,
    rate: BigDecimal,
    unit: RateUnit = RateUnit.PER_GRAM,
    moneyScale: Int = config.moneyScale,
    weightScale: Int = config.weightScale,
  ): BigDecimal {
    return calculateMetalValue(purityResult.fineWeight, rate, unit, moneyScale, weightScale)
  }

  fun calculateFineWeightValue(
    fineWeight: BigDecimal,
    rate: BigDecimal,
    unit: RateUnit = RateUnit.PER_GRAM,
    moneyScale: Int = config.moneyScale,
  ): BigDecimal {
    return calculateMetalValue(fineWeight, rate, unit, moneyScale)
  }

  /**
   * Central Money -> Metal Weight Calculation Engine:
   * Formula: metalWeight = amount / rate (normalized per gram)
   */
  fun calculateMetalWeight(
    amount: BigDecimal,
    rate: BigDecimal,
    unit: RateUnit = RateUnit.PER_GRAM,
    weightScale: Int = config.weightScale,
    moneyScale: Int = config.moneyScale,
  ): MetalWeightCalculationResult {
    require(rate > BigDecimal.ZERO) { "Metal rate must be greater than zero." }
    val safeAmount = amount.max(BigDecimal.ZERO).setScale(moneyScale, config.roundingMode)
    val safeRate = rate.setScale(moneyScale, config.roundingMode)
    val normalizedPerGram = normalizeRate(safeRate, unit)

    val metalWeight =
      safeAmount.divide(normalizedPerGram, weightScale, config.roundingMode)

    val displayPerGram = normalizedPerGram.setScale(moneyScale, config.roundingMode)
    val unitExplanation =
      if (unit == RateUnit.PER_GRAM) {
        "₹${formatMoney(safeRate)}/g"
      } else {
        "₹${formatMoney(safeRate)} ${unit.shortLabel} (₹${formatMoney(displayPerGram)}/g)"
      }

    val breakdown =
      "₹${formatMoney(safeAmount)} ÷ ₹${formatMoney(displayPerGram)}/g = ${formatWeight(metalWeight, weightScale)} g [$unitExplanation]"

    return MetalWeightCalculationResult(
      amount = safeAmount,
      rate = safeRate,
      rateUnit = unit,
      normalizedRatePerGram = displayPerGram,
      metalWeight = metalWeight,
      formulaBreakdown = breakdown,
    )
  }

  fun calculateWeightFromMoney(
    amount: BigDecimal,
    rate: BigDecimal,
    unit: RateUnit = RateUnit.PER_GRAM,
    weightScale: Int = config.weightScale,
  ): BigDecimal {
    if (rate <= BigDecimal.ZERO) {
      return BigDecimal.ZERO.setScale(weightScale, config.roundingMode)
    }
    return calculateMetalWeight(amount, rate, unit, weightScale, config.moneyScale).metalWeight
  }

  /**
   * Safe wrapper for UI preview and Money -> Gold/Silver transactions.
   */
  fun calculateMoneyToMetal(
    moneyAmount: BigDecimal = BigDecimal.ZERO,
    rate: BigDecimal,
    unit: RateUnit = RateUnit.PER_GRAM,
    optionalTunch: BigDecimal = BigDecimal("100.00"),
    weightScale: Int = config.weightScale,
    amount: BigDecimal = moneyAmount,
  ): MetalConversionResult {
    val safeMoney = amount.max(BigDecimal.ZERO).setScale(config.moneyScale, config.roundingMode)
    val safeTunch = optionalTunch.max(BigDecimal.ZERO).setScale(config.tunchScale, config.roundingMode)
    if (rate <= BigDecimal.ZERO) {
      val zeroWt = BigDecimal.ZERO.setScale(weightScale, config.roundingMode)
      return MetalConversionResult(
        moneyAmount = safeMoney,
        enteredRate = BigDecimal.ZERO.setScale(config.moneyScale, config.roundingMode),
        rateUnit = unit,
        effectiveRatePerGram = BigDecimal.ZERO.setScale(config.moneyScale, config.roundingMode),
        metalQuantityGrams = zeroWt,
        tunch = safeTunch,
        fineWeightGrams = zeroWt,
        formulaBreakdown = "Enter a valid positive metal rate to calculate weight.",
      )
    }
    val res = calculateMetalWeight(safeMoney, rate, unit, weightScale, config.moneyScale)
    val fineWt =
      if (safeTunch > BigDecimal.ZERO && safeTunch.compareTo(config.purityDivisor) != 0) {
        calculateFineWeight(res.metalWeight, safeTunch, config.purityDivisor, weightScale).fineWeight
      } else {
        res.metalWeight
      }
    return MetalConversionResult(
      moneyAmount = res.amount,
      enteredRate = res.rate,
      rateUnit = res.rateUnit,
      effectiveRatePerGram = res.normalizedRatePerGram,
      metalQuantityGrams = res.metalWeight,
      tunch = safeTunch,
      fineWeightGrams = fineWt,
      formulaBreakdown = res.formulaBreakdown,
    )
  }

  fun calculateTunchFromWeights(
    grossWeight: BigDecimal,
    fineWeight: BigDecimal,
    customDivisor: BigDecimal = config.purityDivisor,
  ): BigDecimal {
    if (grossWeight <= BigDecimal.ZERO || fineWeight <= BigDecimal.ZERO) {
      return BigDecimal.ZERO.setScale(config.tunchScale, config.roundingMode)
    }
    val divisor = if (customDivisor > BigDecimal.ZERO) customDivisor else HUNDRED
    return fineWeight
      .multiply(divisor)
      .divide(grossWeight, config.tunchScale, config.roundingMode)
  }

  /**
   * Calculates configurable Scrap Deductions:
   * Never automatically invents a deduction percentage.
   */
  fun calculateDeduction(
    metalValue: BigDecimal,
    normalizedRatePerGram: BigDecimal,
    deductionType: DeductionType,
    deductionInput: BigDecimal,
    moneyScale: Int = config.moneyScale,
  ): DeductionCalculationResult {
    val safeMetalValue = metalValue.max(BigDecimal.ZERO).setScale(moneyScale, config.roundingMode)
    val safeInput = deductionInput.max(BigDecimal.ZERO)

    val rawDeduction =
      when (deductionType) {
        DeductionType.NONE -> BigDecimal.ZERO
        DeductionType.AMOUNT_DEDUCTION,
        DeductionType.FIXED_AMOUNT,
        DeductionType.CUSTOM -> safeInput
        DeductionType.PERCENTAGE_DEDUCTION,
        DeductionType.PERCENTAGE ->
          safeMetalValue
            .multiply(safeInput)
            .divide(HUNDRED, config.intermediateScale, config.roundingMode)
        DeductionType.WEIGHT_DEDUCTION,
        DeductionType.WEIGHT ->
          safeInput.multiply(normalizedRatePerGram.max(BigDecimal.ZERO))
      }.setScale(moneyScale, config.roundingMode)

    val clampedDeduction = rawDeduction.min(safeMetalValue)
    val netValue = safeMetalValue.subtract(clampedDeduction).setScale(moneyScale, config.roundingMode)

    val breakdown =
      when (deductionType) {
        DeductionType.NONE -> "No Deduction (₹0.00)"
        DeductionType.AMOUNT_DEDUCTION,
        DeductionType.FIXED_AMOUNT -> "Fixed Deduction: ₹${formatMoney(clampedDeduction)}"
        DeductionType.PERCENTAGE_DEDUCTION,
        DeductionType.PERCENTAGE ->
          "${formatTunch(safeInput)}% of ₹${formatMoney(safeMetalValue)} = ₹${formatMoney(clampedDeduction)}"
        DeductionType.WEIGHT_DEDUCTION,
        DeductionType.WEIGHT ->
          "${formatWeight(safeInput)} g × ₹${formatMoney(normalizedRatePerGram)}/g = ₹${formatMoney(clampedDeduction)}"
        DeductionType.CUSTOM -> "Custom Deduction: ₹${formatMoney(clampedDeduction)}"
      }

    return DeductionCalculationResult(
      deductionType = deductionType,
      deductionInput = safeInput,
      metalValue = safeMetalValue,
      deductionAmount = clampedDeduction,
      netValue = netValue,
      deductionBreakdown = breakdown,
    )
  }

  /**
   * Calculates Scrap Gold / Scrap Silver using central calculateFineWeight & calculateMetalValue:
   * Fine Weight = Gross Weight * Tunch / 100
   * Metal Value = Fine Weight * Normalized Rate
   * Net Value = Metal Value - Deductions
   */
  fun calculateScrapMetal(
    grossWeight: BigDecimal,
    tunch: BigDecimal,
    rate: BigDecimal,
    unit: RateUnit = RateUnit.PER_GRAM,
    deductionType: DeductionType = DeductionType.NONE,
    deductionValue: BigDecimal = BigDecimal.ZERO,
    deductionInput: BigDecimal = deductionValue,
    customDivisor: BigDecimal = config.purityDivisor,
    weightScale: Int = config.weightScale,
    moneyScale: Int = config.moneyScale,
  ): ScrapCalculationResult {
    val purityResult = calculateFineWeight(grossWeight, tunch, customDivisor, weightScale)
    val valueResult =
      calculateMetalValueDetailed(
        fineWeight = purityResult.fineWeight,
        rate = rate,
        unit = unit,
        moneyScale = moneyScale,
        weightScale = weightScale,
      )
    val deductionResult =
      calculateDeduction(
        metalValue = valueResult.metalValue,
        normalizedRatePerGram = valueResult.normalizedRatePerGram,
        deductionType = deductionType,
        deductionInput = deductionInput,
        moneyScale = moneyScale,
      )

    val netFormula =
      if (deductionResult.deductionAmount > BigDecimal.ZERO) {
        "₹${formatMoney(valueResult.metalValue)} - ₹${formatMoney(deductionResult.deductionAmount)} = ₹${formatMoney(deductionResult.netValue)}"
      } else {
        "₹${formatMoney(valueResult.metalValue)} (No Deductions)"
      }

    return ScrapCalculationResult(
      grossWeight = purityResult.grossWeight,
      tunch = purityResult.tunch,
      fineWeight = purityResult.fineWeight,
      enteredRate = valueResult.rate,
      rateUnit = unit,
      effectiveRatePerGram = valueResult.normalizedRatePerGram,
      calculatedValue = valueResult.metalValue,
      metalValue = valueResult.metalValue,
      deductionType = deductionType,
      deductionInput = deductionInput,
      deductions = deductionResult.deductionAmount,
      deductionAmount = deductionResult.deductionAmount,
      netValue = deductionResult.netValue,
      fineWeightFormula = purityResult.formulaBreakdown,
      valueFormula = valueResult.formulaBreakdown,
      netValueFormula = netFormula,
    )
  }

  /**
   * Payment Adjustment calculation for Gold/Silver Payment Received.
   */
  fun calculatePaymentAdjustment(
    metalValue: BigDecimal,
    cashPaid: BigDecimal = BigDecimal.ZERO,
    cashReceived: BigDecimal = BigDecimal.ZERO,
  ): PaymentAdjustmentResult {
    val safeMetalVal = metalValue.max(BigDecimal.ZERO).setScale(config.moneyScale, config.roundingMode)
    val safePaid = cashPaid.max(BigDecimal.ZERO).setScale(config.moneyScale, config.roundingMode)
    val safeRec = cashReceived.max(BigDecimal.ZERO).setScale(config.moneyScale, config.roundingMode)
    val remaining =
      safeMetalVal.add(safeRec).subtract(safePaid).setScale(config.moneyScale, config.roundingMode)
    return PaymentAdjustmentResult(
      metalValue = safeMetalVal,
      cashPaid = safePaid,
      cashReceived = safeRec,
      remainingBalance = remaining,
    )
  }

  /**
   * Calculates each item separately and totals Gross Weight, Fine Weight, and Metal Value
   * (Multiple Items in One Transaction).
   */
  fun calculateMultipleItemsTotals(
    items: List<TransactionItem>,
    deductionType: DeductionType = DeductionType.NONE,
    deductionValue: BigDecimal = BigDecimal.ZERO,
    deductionInput: BigDecimal = deductionValue,
    defaultRate: BigDecimal = BigDecimal.ZERO,
    defaultRateUnit: RateUnit = RateUnit.PER_GRAM,
    paymentAdjustment: BigDecimal = BigDecimal.ZERO,
  ): MultipleItemsCalculationResult {
    if (items.isEmpty()) {
      val zeroWt = BigDecimal.ZERO.setScale(config.weightScale, config.roundingMode)
      val zeroMoney = BigDecimal.ZERO.setScale(config.moneyScale, config.roundingMode)
      return MultipleItemsCalculationResult(
        items = emptyList(),
        calculatedItems = emptyList(),
        totalGrossWeight = zeroWt,
        effectiveAverageTunch = BigDecimal.ZERO.setScale(config.tunchScale, config.roundingMode),
        weightedAverageTunch = BigDecimal.ZERO.setScale(config.tunchScale, config.roundingMode),
        totalFineWeight = zeroWt,
        totalMetalValue = zeroMoney,
        deductions = zeroMoney,
        deductionAmount = zeroMoney,
        netValue = zeroMoney,
      )
    }

    val recalculatedItems =
      items.map { item ->
        val purityRes = calculateFineWeight(item.grossWeight, item.tunch)
        val effectiveRate = if (item.rate > BigDecimal.ZERO) item.rate else defaultRate
        val effectiveUnit = if (item.rate > BigDecimal.ZERO) item.rateUnit else defaultRateUnit
        val valRes =
          calculateMetalValueDetailed(purityRes.fineWeight, effectiveRate, effectiveUnit)
        item.copy(
          grossWeight = purityRes.grossWeight,
          tunch = purityRes.tunch,
          purity = "${purityRes.tunch.toPlainString()}%",
          fineWeight = purityRes.fineWeight,
          rate = effectiveRate,
          rateUnit = effectiveUnit,
          metalValue = valRes.metalValue,
          amount = valRes.metalValue,
        )
      }

    val totalGross =
      recalculatedItems
        .fold(BigDecimal.ZERO) { acc, it -> acc.add(it.grossWeight) }
        .setScale(config.weightScale, config.roundingMode)

    val totalFine =
      recalculatedItems
        .fold(BigDecimal.ZERO) { acc, it -> acc.add(it.fineWeight) }
        .setScale(config.weightScale, config.roundingMode)

    val totalMetalVal =
      recalculatedItems
        .fold(BigDecimal.ZERO) { acc, it -> acc.add(it.amount) }
        .setScale(config.moneyScale, config.roundingMode)

    val avgTunch =
      if (totalGross > BigDecimal.ZERO) {
        calculateTunchFromWeights(totalGross, totalFine)
      } else {
        BigDecimal.ZERO.setScale(config.tunchScale, config.roundingMode)
      }

    val primaryItem = recalculatedItems.first()
    val normRate = normalizeRate(primaryItem.rate, primaryItem.rateUnit)
    val deductionRes =
      calculateDeduction(totalMetalVal, normRate, deductionType, deductionInput)

    val adjustedNet =
      deductionRes.netValue
        .add(paymentAdjustment)
        .max(BigDecimal.ZERO)
        .setScale(config.moneyScale, config.roundingMode)

    return MultipleItemsCalculationResult(
      items = recalculatedItems,
      calculatedItems = recalculatedItems,
      totalGrossWeight = totalGross,
      effectiveAverageTunch = avgTunch,
      weightedAverageTunch = avgTunch,
      totalFineWeight = totalFine,
      totalMetalValue = totalMetalVal,
      deductions = deductionRes.deductionAmount,
      deductionAmount = deductionRes.deductionAmount,
      netValue = adjustedNet,
    )
  }

  /**
   * Builds the Customer Balance / Ledger:
   * Creates separate totals for Money, Gold, and Silver.
   * Excludes CANCELLED and DRAFT transactions from active ledger totals while preserving all in `transactions`.
   */
  fun buildCustomerLedger(
    customerId: String,
    customerName: String,
    transactions: List<Transaction>,
  ): CustomerLedgerSummary {
    val customerTxs =
      transactions
        .filter { it.customerId == customerId }
        .sortedByDescending { it.timestamp }

    val entries =
      customerTxs.map { tx ->
        val effects = mutableListOf<LedgerEffectCategory>()
        var moneyPaid = BigDecimal.ZERO
        var moneyReceived = BigDecimal.ZERO
        var goldGiven = BigDecimal.ZERO
        var goldReceived = BigDecimal.ZERO
        var silverGiven = BigDecimal.ZERO
        var silverReceived = BigDecimal.ZERO

        when (tx.transactionType) {
          TransactionType.MONEY_TO_GOLD -> {
            effects.add(LedgerEffectCategory.CUSTOMER_PAYS_MONEY)
            effects.add(LedgerEffectCategory.CUSTOMER_RECEIVES_GOLD)
            moneyPaid = tx.amount
            goldReceived = tx.fineWeight
          }
          TransactionType.MONEY_TO_SILVER -> {
            effects.add(LedgerEffectCategory.CUSTOMER_PAYS_MONEY)
            effects.add(LedgerEffectCategory.CUSTOMER_RECEIVES_SILVER)
            moneyPaid = tx.amount
            silverReceived = tx.fineWeight
          }
          TransactionType.GOLD_PAYMENT -> {
            effects.add(LedgerEffectCategory.CUSTOMER_GIVES_GOLD)
            goldGiven = tx.fineWeight
            if (tx.paymentAdjustment > BigDecimal.ZERO) {
              effects.add(LedgerEffectCategory.CUSTOMER_PAYS_MONEY)
              moneyPaid = tx.paymentAdjustment
            }
          }
          TransactionType.SILVER_PAYMENT -> {
            effects.add(LedgerEffectCategory.CUSTOMER_GIVES_SILVER)
            silverGiven = tx.fineWeight
            if (tx.paymentAdjustment > BigDecimal.ZERO) {
              effects.add(LedgerEffectCategory.CUSTOMER_PAYS_MONEY)
              moneyPaid = tx.paymentAdjustment
            }
          }
          TransactionType.SCRAP_GOLD -> {
            effects.add(LedgerEffectCategory.CUSTOMER_GIVES_GOLD)
            effects.add(LedgerEffectCategory.CUSTOMER_RECEIVES_MONEY)
            goldGiven = tx.fineWeight
            moneyReceived = tx.amount
          }
          TransactionType.SCRAP_SILVER -> {
            effects.add(LedgerEffectCategory.CUSTOMER_GIVES_SILVER)
            effects.add(LedgerEffectCategory.CUSTOMER_RECEIVES_MONEY)
            silverGiven = tx.fineWeight
            moneyReceived = tx.amount
          }
          TransactionType.GOLD_ADJUSTMENT -> {
            if (tx.fineWeight >= BigDecimal.ZERO) {
              effects.add(LedgerEffectCategory.CUSTOMER_GIVES_GOLD)
              goldGiven = tx.fineWeight
            } else {
              effects.add(LedgerEffectCategory.CUSTOMER_RECEIVES_GOLD)
              goldReceived = tx.fineWeight.abs()
            }
            if (tx.amount > BigDecimal.ZERO) {
              effects.add(LedgerEffectCategory.CUSTOMER_PAYS_MONEY)
              moneyPaid = tx.amount
            }
          }
          TransactionType.SILVER_ADJUSTMENT -> {
            if (tx.fineWeight >= BigDecimal.ZERO) {
              effects.add(LedgerEffectCategory.CUSTOMER_GIVES_SILVER)
              silverGiven = tx.fineWeight
            } else {
              effects.add(LedgerEffectCategory.CUSTOMER_RECEIVES_SILVER)
              silverReceived = tx.fineWeight.abs()
            }
            if (tx.amount > BigDecimal.ZERO) {
              effects.add(LedgerEffectCategory.CUSTOMER_PAYS_MONEY)
              moneyPaid = tx.amount
            }
          }
        }

        CustomerLedgerEntry(
          transactionId = tx.transactionId,
          invoiceNumber = tx.invoiceNumber,
          date = tx.date,
          time = tx.time,
          transactionType = tx.transactionType,
          status = tx.status,
          effects = effects,
          moneyPaidByCustomer = moneyPaid.setScale(config.moneyScale, config.roundingMode),
          moneyReceivedByCustomer = moneyReceived.setScale(config.moneyScale, config.roundingMode),
          goldGivenByCustomerGrams = goldGiven.setScale(config.weightScale, config.roundingMode),
          goldReceivedByCustomerGrams = goldReceived.setScale(config.weightScale, config.roundingMode),
          silverGivenByCustomerGrams = silverGiven.setScale(config.weightScale, config.roundingMode),
          silverReceivedByCustomerGrams = silverReceived.setScale(config.weightScale, config.roundingMode),
          notes = tx.notes,
        )
      }

    // Active totals exclude CANCELLED and DRAFT transactions
    val activeTxs =
      customerTxs.filter {
        it.status == TransactionStatus.COMPLETED || it.status == TransactionStatus.PENDING_SYNC
      }
    val activeEntries =
      entries.filter {
        it.status == TransactionStatus.COMPLETED || it.status == TransactionStatus.PENDING_SYNC
      }

    val totalMoneyPaid =
      activeEntries
        .fold(BigDecimal.ZERO) { acc, e -> acc.add(e.moneyPaidByCustomer) }
        .setScale(config.moneyScale, config.roundingMode)
    val totalMoneyRec =
      activeEntries
        .fold(BigDecimal.ZERO) { acc, e -> acc.add(e.moneyReceivedByCustomer) }
        .setScale(config.moneyScale, config.roundingMode)
    val netMoney =
      totalMoneyPaid.subtract(totalMoneyRec).setScale(config.moneyScale, config.roundingMode)

    val totalGoldGivenByCust =
      activeEntries
        .fold(BigDecimal.ZERO) { acc, e -> acc.add(e.goldGivenByCustomerGrams) }
        .setScale(config.weightScale, config.roundingMode)
    val totalGoldRecByCust =
      activeEntries
        .fold(BigDecimal.ZERO) { acc, e -> acc.add(e.goldReceivedByCustomerGrams) }
        .setScale(config.weightScale, config.roundingMode)
    val netGold =
      totalGoldGivenByCust.subtract(totalGoldRecByCust).setScale(config.weightScale, config.roundingMode)

    val totalSilverGivenByCust =
      activeEntries
        .fold(BigDecimal.ZERO) { acc, e -> acc.add(e.silverGivenByCustomerGrams) }
        .setScale(config.weightScale, config.roundingMode)
    val totalSilverRecByCust =
      activeEntries
        .fold(BigDecimal.ZERO) { acc, e -> acc.add(e.silverReceivedByCustomerGrams) }
        .setScale(config.weightScale, config.roundingMode)
    val netSilver =
      totalSilverGivenByCust.subtract(totalSilverRecByCust).setScale(config.weightScale, config.roundingMode)

    val scrapGold =
      activeTxs
        .filter { it.transactionType == TransactionType.SCRAP_GOLD }
        .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.fineWeight) }
        .setScale(config.weightScale, config.roundingMode)
    val scrapSilver =
      activeTxs
        .filter { it.transactionType == TransactionType.SCRAP_SILVER }
        .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.fineWeight) }
        .setScale(config.weightScale, config.roundingMode)

    return CustomerLedgerSummary(
      customerId = customerId,
      customerName = customerName,
      transactions = customerTxs,
      entries = entries,
      goldGivenGrams = totalGoldRecByCust,
      goldReceivedGrams = totalGoldGivenByCust,
      silverGivenGrams = totalSilverRecByCust,
      silverReceivedGrams = totalSilverGivenByCust,
      scrapGoldGrams = scrapGold,
      scrapSilverGrams = scrapSilver,
      moneyReceived = totalMoneyPaid,
      netGoldBalanceGrams = netGold,
      netSilverBalanceGrams = netSilver,
      netMoneyBalance = netMoney,
      totalMoneyPaidByCustomer = totalMoneyPaid,
      totalMoneyReceivedByCustomer = totalMoneyRec,
      netMoneyFlow = netMoney,
      totalGoldGivenByCustomerGrams = totalGoldGivenByCust,
      totalGoldReceivedByCustomerGrams = totalGoldRecByCust,
      netGoldGrams = netGold,
      totalSilverGivenByCustomerGrams = totalSilverGivenByCust,
      totalSilverReceivedByCustomerGrams = totalSilverRecByCust,
      netSilverGrams = netSilver,
    )
  }

  fun calculateCustomerLedger(
    customerId: String,
    customerName: String,
    transactions: List<Transaction>,
  ): CustomerLedgerSummary = buildCustomerLedger(customerId, customerName, transactions)

  fun parseSafeBigDecimal(input: String): BigDecimal? {
    val cleaned = input.trim().replace(",", "")
    if (cleaned.isEmpty()) return null
    return try {
      BigDecimal(cleaned)
    } catch (e: NumberFormatException) {
      null
    }
  }

  fun formatMoney(amount: BigDecimal, scale: Int = config.moneyScale): String {
    val scaled = amount.setScale(scale, config.roundingMode)
    val pattern = if (scale > 0) "#,##,##0." + "0".repeat(scale) else "#,##,##0"
    return DecimalFormat(pattern).format(scaled)
  }

  fun formatCurrency(amount: BigDecimal, scale: Int = config.moneyScale): String {
    return "₹${formatMoney(amount, scale)}"
  }

  fun formatWeight(weight: BigDecimal, scale: Int = config.weightScale): String {
    val scaled = weight.setScale(scale, config.roundingMode)
    val pattern = if (scale > 0) "#,##0." + "0".repeat(scale) else "#,##0"
    return DecimalFormat(pattern).format(scaled)
  }

  fun formatTunch(tunch: BigDecimal, scale: Int = config.tunchScale): String {
    val scaled = tunch.setScale(scale, config.roundingMode)
    val pattern = if (scale > 0) "0." + "0".repeat(scale) else "0"
    return DecimalFormat(pattern).format(scaled)
  }
}

object ValidationService {
  const val GSTIN_LOCAL_VALIDATION_NOTICE =
    "GSTIN format validated locally. Government verification is not performed by this app."
  const val GSTIN_LOCAL_VALIDATION_MESSAGE = GSTIN_LOCAL_VALIDATION_NOTICE
  const val GST_PROFESSIONAL_DISCLAIMER =
    "Please verify applicable GST rates and tax treatment with your tax professional."

  private val gstinRegex = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$")
  private val panRegex = Regex("^[A-Z]{5}[0-9]{4}[A-Z]{1}$")
  private val ifscRegex = Regex("^[A-Z]{4}0[A-Z0-9]{6}$")
  private val pinRegex = Regex("^[1-9][0-9]{5}$")

  fun validateGstinFormat(gstin: String): ValidationResult {
    val clean = gstin.trim().uppercase()
    if (clean.isEmpty()) return ValidationResult.Valid
    return if (gstinRegex.matches(clean)) {
      ValidationResult.Valid
    } else {
      ValidationResult.Invalid(
        "Invalid GSTIN format. Must be 15 characters (e.g., 27AAECS4912F1Z9) or left blank."
      )
    }
  }

  fun validateGstinFormatLocally(gstin: String): ValidationResult = validateGstinFormat(gstin)

  fun validatePanFormat(pan: String): ValidationResult {
    val clean = pan.trim().uppercase()
    if (clean.isEmpty()) return ValidationResult.Valid
    return if (panRegex.matches(clean)) {
      ValidationResult.Valid
    } else {
      ValidationResult.Invalid("Invalid PAN format. Must be 10 characters (e.g., AAECS4912F) or left blank.")
    }
  }

  fun validateIfscFormat(ifsc: String): ValidationResult {
    val clean = ifsc.trim().uppercase()
    if (clean.isEmpty()) return ValidationResult.Valid
    return if (ifscRegex.matches(clean)) {
      ValidationResult.Valid
    } else {
      ValidationResult.Invalid("Invalid IFSC format. Must be 11 characters (e.g., HDFC0000142) or left blank.")
    }
  }

  fun validatePinCodeFormat(pinCode: String): ValidationResult {
    val clean = pinCode.trim()
    if (clean.isEmpty()) return ValidationResult.Valid
    return if (pinRegex.matches(clean)) {
      ValidationResult.Valid
    } else {
      ValidationResult.Invalid("Invalid PIN Code. Must be a 6-digit postal code (e.g., 400002) or left blank.")
    }
  }

  fun validateCustomer(
    name: String,
    mobile: String,
    whatsapp: String = "",
    pin: String = "",
    pan: String = "",
    gst: String = "",
    email: String = "",
  ): ValidationResult {
    if (name.trim().isEmpty()) {
      return ValidationResult.Invalid("Please enter the customer's name.")
    }
    val digitsOnly = mobile.trim().filter { it.isDigit() }
    if (digitsOnly.length < 10 || digitsOnly.length > 15) {
      return ValidationResult.Invalid("Please enter a valid 10-digit mobile number.")
    }
    val waDigits = whatsapp.trim().filter { it.isDigit() }
    if (whatsapp.isNotBlank() && (waDigits.length < 10 || waDigits.length > 15)) {
      return ValidationResult.Invalid("Please enter a valid WhatsApp number or leave blank.")
    }
    val pinVal = validatePinCodeFormat(pin)
    if (pinVal is ValidationResult.Invalid) return pinVal
    val panVal = validatePanFormat(pan)
    if (panVal is ValidationResult.Invalid) return panVal
    val gstVal = validateGstinFormat(gst)
    if (gstVal is ValidationResult.Invalid) return gstVal
    return ValidationResult.Valid
  }

  fun validateTunch(
    tunch: BigDecimal?,
    maxTunch: BigDecimal = BigDecimal("100.00"),
  ): ValidationResult {
    if (tunch == null) {
      return ValidationResult.Invalid("Please enter a valid numeric Tunch / Purity value.")
    }
    if (tunch <= BigDecimal.ZERO) {
      return ValidationResult.Invalid("Tunch / Purity must be greater than 0.")
    }
    if (tunch > maxTunch) {
      return ValidationResult.Invalid(
        "Tunch / Purity cannot exceed configured maximum (${maxTunch.stripTrailingZeros().toPlainString()})."
      )
    }
    return ValidationResult.Valid
  }

  fun validateMoneyToMetalTransaction(
    customerName: String,
    amount: BigDecimal?,
    rate: BigDecimal?,
    invoiceNumber: String,
    optionalTunch: BigDecimal? = null,
    maxTunch: BigDecimal = BigDecimal("100.00"),
  ): ValidationResult {
    if (customerName.trim().isEmpty()) {
      return ValidationResult.Invalid("Please select or create a customer for this transaction.")
    }
    if (invoiceNumber.trim().isEmpty()) {
      return ValidationResult.Invalid("Please enter a valid invoice number.")
    }
    if (amount == null || amount <= BigDecimal.ZERO) {
      return ValidationResult.Invalid("Please enter a positive money amount greater than ₹0.")
    }
    if (rate == null || rate <= BigDecimal.ZERO) {
      return ValidationResult.Invalid("Rate must be greater than zero.")
    }
    if (optionalTunch != null) {
      val tunchVal = validateTunch(optionalTunch, maxTunch)
      if (tunchVal is ValidationResult.Invalid) return tunchVal
    }
    return ValidationResult.Valid
  }

  fun validateMetalPaymentTransaction(
    customerName: String,
    grossWeight: BigDecimal?,
    tunch: BigDecimal?,
    rate: BigDecimal?,
    invoiceNumber: String,
    maxTunch: BigDecimal = BigDecimal("100.00"),
  ): ValidationResult {
    if (customerName.trim().isEmpty()) {
      return ValidationResult.Invalid("Please select or create a customer for this transaction.")
    }
    if (invoiceNumber.trim().isEmpty()) {
      return ValidationResult.Invalid("Please enter a valid invoice number.")
    }
    if (grossWeight == null || grossWeight <= BigDecimal.ZERO) {
      return ValidationResult.Invalid("Gross weight must be greater than 0 grams.")
    }
    val tunchValidation = validateTunch(tunch, maxTunch)
    if (tunchValidation is ValidationResult.Invalid) {
      return tunchValidation
    }
    if (rate == null || rate < BigDecimal.ZERO) {
      return ValidationResult.Invalid("Metal rate cannot be negative.")
    }
    return ValidationResult.Valid
  }

  fun validateScrapTransaction(
    customerName: String,
    grossWeight: BigDecimal?,
    tunch: BigDecimal?,
    rate: BigDecimal?,
    invoiceNumber: String,
    deductionType: DeductionType = DeductionType.NONE,
    deductionValue: BigDecimal? = BigDecimal.ZERO,
    deductionInput: BigDecimal? = deductionValue,
    maxTunch: BigDecimal = BigDecimal("100.00"),
  ): ValidationResult {
    if (customerName.trim().isEmpty()) {
      return ValidationResult.Invalid("Please select or create a customer for this scrap transaction.")
    }
    if (invoiceNumber.trim().isEmpty()) {
      return ValidationResult.Invalid("Please enter a valid invoice number.")
    }
    if (grossWeight == null || grossWeight <= BigDecimal.ZERO) {
      return ValidationResult.Invalid("Gross weight must be greater than 0 grams.")
    }
    val tunchValidation = validateTunch(tunch, maxTunch)
    if (tunchValidation is ValidationResult.Invalid) {
      return tunchValidation
    }
    if (rate == null || rate <= BigDecimal.ZERO) {
      return ValidationResult.Invalid("Please enter a positive metal rate greater than zero.")
    }
    if (deductionInput != null && deductionInput < BigDecimal.ZERO) {
      return ValidationResult.Invalid("Deduction cannot be negative.")
    }
    if (
      (deductionType == DeductionType.PERCENTAGE_DEDUCTION ||
        deductionType == DeductionType.PERCENTAGE) &&
        deductionInput != null &&
        deductionInput > BigDecimal("100")
    ) {
      return ValidationResult.Invalid("Percentage deduction cannot exceed 100%.")
    }
    return ValidationResult.Valid
  }

  fun validateAdjustmentTransaction(
    customerName: String,
    grossWeight: BigDecimal?,
    tunch: BigDecimal?,
    rate: BigDecimal?,
    invoiceNumber: String,
    maxTunch: BigDecimal = BigDecimal("100.00"),
  ): ValidationResult {
    if (customerName.trim().isEmpty()) {
      return ValidationResult.Invalid("Please select or create a customer for this adjustment.")
    }
    if (invoiceNumber.trim().isEmpty()) {
      return ValidationResult.Invalid("Please enter a valid invoice number.")
    }
    if (grossWeight == null || grossWeight <= BigDecimal.ZERO) {
      return ValidationResult.Invalid("Adjustment gross weight must be greater than 0 grams.")
    }
    val tunchValidation = validateTunch(tunch, maxTunch)
    if (tunchValidation is ValidationResult.Invalid) {
      return tunchValidation
    }
    if (rate != null && rate < BigDecimal.ZERO) {
      return ValidationResult.Invalid("Rate cannot be negative.")
    }
    return ValidationResult.Valid
  }

  fun validateMetalRates(goldRate: BigDecimal?, silverRate: BigDecimal?): ValidationResult {
    if (goldRate == null || goldRate <= BigDecimal.ZERO) {
      return ValidationResult.Invalid("Gold rate must be a positive number greater than 0.")
    }
    if (silverRate == null || silverRate <= BigDecimal.ZERO) {
      return ValidationResult.Invalid("Silver rate must be a positive number greater than 0.")
    }
    return ValidationResult.Valid
  }

  fun validateBusinessProfile(
    shopName: String,
    ownerName: String = "",
    mobile: String,
    whatsapp: String = "",
    email: String = "",
    pinCode: String = "",
    pan: String = "",
    gstin: String = "",
    ifsc: String = "",
    invoicePrefix: String,
    startingNumber: Int?,
  ): ValidationResult {
    if (shopName.trim().isEmpty()) {
      return ValidationResult.Invalid("Shop Name is required.")
    }
    val digits = mobile.trim().filter { it.isDigit() }
    if (digits.isNotEmpty() && (digits.length < 10 || digits.length > 15)) {
      return ValidationResult.Invalid("Please enter a valid 10-digit shop mobile number.")
    }
    val waDigits = whatsapp.trim().filter { it.isDigit() }
    if (whatsapp.isNotBlank() && (waDigits.length < 10 || waDigits.length > 15)) {
      return ValidationResult.Invalid("Please enter a valid WhatsApp number or leave blank.")
    }
    val pinVal = validatePinCodeFormat(pinCode)
    if (pinVal is ValidationResult.Invalid) return pinVal
    val panVal = validatePanFormat(pan)
    if (panVal is ValidationResult.Invalid) return panVal
    val gstVal = validateGstinFormat(gstin)
    if (gstVal is ValidationResult.Invalid) return gstVal
    val ifscVal = validateIfscFormat(ifsc)
    if (ifscVal is ValidationResult.Invalid) return ifscVal
    if (invoicePrefix.trim().isEmpty()) {
      return ValidationResult.Invalid("Invoice prefix cannot be empty (e.g., HGR-).")
    }
    if (startingNumber == null || startingNumber <= 0) {
      return ValidationResult.Invalid("Invoice starting number must be a positive integer.")
    }
    return ValidationResult.Valid
  }
}
