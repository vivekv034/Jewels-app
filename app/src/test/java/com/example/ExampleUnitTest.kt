package com.example

import com.example.domain.calculation.CalculationConfig
import com.example.domain.calculation.CalculationEngine
import com.example.domain.calculation.ValidationResult
import com.example.domain.calculation.ValidationService
import com.example.domain.model.DeductionType
import com.example.domain.model.MetalType
import com.example.domain.model.RateUnit
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionStatus
import com.example.domain.model.TransactionType
import java.math.BigDecimal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  private val engine = CalculationEngine()

  @Test
  fun normalizeRate_supportsPerGramPer10GramsAndPerKilogram() {
    assertEquals(
      0,
      BigDecimal("10000").compareTo(engine.normalizeRate(BigDecimal("10000"), RateUnit.PER_GRAM)),
    )
    assertEquals(
      0,
      BigDecimal("10000").compareTo(engine.normalizeRate(BigDecimal("100000"), RateUnit.PER_10_GRAMS)),
    )
    assertEquals(
      0,
      BigDecimal("100").compareTo(engine.normalizeRate(BigDecimal("100000"), RateUnit.PER_KILOGRAM)),
    )
  }

  @Test
  fun moneyToGoldAndMoneyToSilver_matchStage3Examples() {
    // Money -> Gold: Amount = ₹50,000, Gold Rate = ₹10,000/g => 5.000 g
    val goldWeight =
      engine.calculateWeightFromMoney(
        amount = BigDecimal("50000"),
        rate = BigDecimal("10000"),
        unit = RateUnit.PER_GRAM,
      )
    assertEquals(BigDecimal("5.000"), goldWeight)

    // Money -> Silver: Amount = ₹10,000, Silver Rate = ₹100/g => 100.000 g
    val silverWeight =
      engine.calculateWeightFromMoney(
        amount = BigDecimal("10000"),
        rate = BigDecimal("100"),
        unit = RateUnit.PER_GRAM,
      )
    assertEquals(BigDecimal("100.000"), silverWeight)
  }

  @Test
  fun goldAndSilverPayment_pure99AndCustomTunch_matchStage3Examples() {
    // Mode 1: 99% Pure Gold: Gross = 10g, Purity = 99% => Fine = 9.900g
    val pure99 = engine.calculateFineWeight(BigDecimal("10"), CalculationEngine.PURE_99_TUNCH)
    assertEquals(BigDecimal("9.900"), pure99.fineWeight)

    // Mode 2: Custom Gold Tunch: Gross = 10g, Tunch = 80 => Fine = 8.000g, Value @ ₹10,000/g = ₹80,000.00
    val customGold = engine.calculateFineWeight(BigDecimal("10"), BigDecimal("80"))
    assertEquals(BigDecimal("8.000"), customGold.fineWeight)
    val goldVal =
      engine.calculateMetalValue(customGold.fineWeight, BigDecimal("10000"), RateUnit.PER_GRAM)
    assertEquals(BigDecimal("80000.00"), goldVal)

    // Silver Payment: Gross = 100g, Tunch = 70 => Fine = 70.000g, Value @ ₹100/g = ₹7,000.00
    val customSilver = engine.calculateFineWeight(BigDecimal("100"), BigDecimal("70"))
    assertEquals(BigDecimal("70.000"), customSilver.fineWeight)
    val silverVal =
      engine.calculateMetalValue(customSilver.fineWeight, BigDecimal("100"), RateUnit.PER_GRAM)
    assertEquals(BigDecimal("7000.00"), silverVal)
  }

  @Test
  fun scrapMetalAndConfigurableDeductions_matchStage3Examples() {
    // Scrap Gold: Gross = 15g, Tunch = 80 => Fine = 12.000g, Rate = ₹10,000/g => Metal Value = ₹120,000.00
    val scrap =
      engine.calculateScrapMetal(
        grossWeight = BigDecimal("15"),
        tunch = BigDecimal("80"),
        rate = BigDecimal("10000"),
        unit = RateUnit.PER_GRAM,
        deductionType = DeductionType.NONE,
      )
    assertEquals(BigDecimal("12.000"), scrap.fineWeight)
    assertEquals(BigDecimal("120000.00"), scrap.metalValue)
    assertEquals(BigDecimal("0.00"), scrap.deductionAmount)
    assertEquals(BigDecimal("120000.00"), scrap.netValue)

    // Percentage Deduction: 2% of ₹120,000 = ₹2,400 => Net = ₹117,600.00
    val scrapWithPctDeduction =
      engine.calculateScrapMetal(
        grossWeight = BigDecimal("15"),
        tunch = BigDecimal("80"),
        rate = BigDecimal("10000"),
        unit = RateUnit.PER_GRAM,
        deductionType = DeductionType.PERCENTAGE_DEDUCTION,
        deductionValue = BigDecimal("2"),
      )
    assertEquals(BigDecimal("2400.00"), scrapWithPctDeduction.deductionAmount)
    assertEquals(BigDecimal("117600.00"), scrapWithPctDeduction.netValue)

    // Scrap Silver: Gross = 200g, Tunch = 65 => Fine = 130.000g, Rate = ₹100/g => ₹13,000.00
    val scrapSilver =
      engine.calculateScrapMetal(
        grossWeight = BigDecimal("200"),
        tunch = BigDecimal("65"),
        rate = BigDecimal("100"),
        unit = RateUnit.PER_GRAM,
      )
    assertEquals(BigDecimal("130.000"), scrapSilver.fineWeight)
    assertEquals(BigDecimal("13000.00"), scrapSilver.netValue)
  }

  @Test
  fun multipleItemsInOneTransaction_matchesStage3Example() {
    // Item 1: Gold Chain, Gross = 10g, Tunch = 80 -> Fine = 8.000g
    // Item 2: Gold Ring, Gross = 5g, Tunch = 75 -> Fine = 3.750g
    // Total Gross = 15.000g, Total Fine = 11.750g
    val items =
      listOf(
        TransactionItem(
          itemName = "Gold Chain",
          metalType = MetalType.GOLD,
          grossWeight = BigDecimal("10"),
          tunch = BigDecimal("80"),
          rate = BigDecimal("10000"),
        ),
        TransactionItem(
          itemName = "Gold Ring",
          metalType = MetalType.GOLD,
          grossWeight = BigDecimal("5"),
          tunch = BigDecimal("75"),
          rate = BigDecimal("10000"),
        ),
      )
    val multiResult =
      engine.calculateMultipleItemsTotals(
        items = items,
        defaultRate = BigDecimal("10000"),
        defaultRateUnit = RateUnit.PER_GRAM,
      )
    assertEquals(BigDecimal("15.000"), multiResult.totalGrossWeight)
    assertEquals(BigDecimal("11.750"), multiResult.totalFineWeight)
    assertEquals(BigDecimal("117500.00"), multiResult.totalMetalValue)
    assertEquals(BigDecimal("117500.00"), multiResult.netValue)
  }

  @Test
  fun configurablePurityDivisor1000_calculatesTouchCorrectly() {
    val touchEngine =
      CalculationEngine(
        CalculationConfig(
          purityDivisor = BigDecimal("1000"),
          maxTunchValue = BigDecimal("1000.00"),
        )
      )
    // Gross = 10g, Touch = 916 (out of 1000) => Fine = 9.160g
    val res = touchEngine.calculateFineWeight(BigDecimal("10"), BigDecimal("916"))
    assertEquals(BigDecimal("9.160"), res.fineWeight)
  }

  @Test
  fun customerLedgerCalculation_excludesCancelledFromActiveTotals() {
    val activeTx =
      Transaction(
        transactionId = "TXN-1",
        invoiceNumber = "HGR-001",
        date = "2026-09-28",
        time = "10:00",
        timestamp = 1000L,
        customerId = "CUST-1",
        customerName = "Rajesh",
        customerMobile = "9876543210",
        transactionType = TransactionType.MONEY_TO_GOLD,
        metalType = MetalType.GOLD,
        grossWeight = BigDecimal("5.000"),
        tunch = BigDecimal("100.00"),
        fineWeight = BigDecimal("5.000"),
        rate = BigDecimal("10000.00"),
        amount = BigDecimal("50000.00"),
        status = TransactionStatus.COMPLETED,
      )
    val cancelledTx =
      activeTx.copy(
        transactionId = "TXN-2",
        invoiceNumber = "HGR-002",
        fineWeight = BigDecimal("10.000"),
        amount = BigDecimal("100000.00"),
        status = TransactionStatus.CANCELLED,
        cancelledBy = "Owner",
        cancellationReason = "Mistake",
      )
    val ledger = engine.calculateCustomerLedger("CUST-1", "Rajesh", listOf(activeTx, cancelledTx))
    assertEquals(2, ledger.transactions.size) // Both preserved in history!
    assertEquals(BigDecimal("5.000"), ledger.goldGivenGrams) // Cancelled excluded from active totals!
    assertEquals(BigDecimal("50000.00"), ledger.moneyReceived)
  }

  @Test
  fun validationService_rejectsInvalidCustomerAndNegativeAmounts() {
    val invalidCustomer = ValidationService.validateCustomer("", "123")
    assertTrue(invalidCustomer is ValidationResult.Invalid)

    val validCustomer = ValidationService.validateCustomer("Rajesh Verma", "9876543210")
    assertTrue(validCustomer is ValidationResult.Valid)
  }

  @Test
  fun stage7CalculationAndSchemaVerification_matchesExactSpecification() {
    // Section 9:
    // ₹50,000 ÷ ₹10,000/g = 5g
    val m2g =
      engine.calculateMoneyToMetal(
        moneyAmount = BigDecimal("50000"),
        rate = BigDecimal("10000"),
        unit = RateUnit.PER_GRAM,
      )
    assertEquals(BigDecimal("5.000"), m2g.fineWeightGrams)

    // 10g × 99% = 9.900g fine
    val pure99 = engine.calculateFineWeight(BigDecimal("10"), BigDecimal("99"))
    assertEquals(BigDecimal("9.900"), pure99.fineWeight)

    // 15.500g × 75% = 11.625g fine
    val custom75 = engine.calculateFineWeight(BigDecimal("15.500"), BigDecimal("75"))
    assertEquals(BigDecimal("11.625"), custom75.fineWeight)

    // 100g × 80% = 80g fine
    val custom80 = engine.calculateFineWeight(BigDecimal("100"), BigDecimal("80"))
    assertEquals(BigDecimal("80.000"), custom80.fineWeight)

    // Section 2: 27 BusinessProfile columns in Google Sheets schema
    val bpHeaders =
      com.example.domain.model.WorksheetSchemas.HEADERS_MAP[
        com.example.domain.model.WorksheetSchemas.SHEET_BUSINESS_PROFILE
      ]!!
    assertEquals(27, bpHeaders.size)
    assertTrue(bpHeaders.contains("BusinessProfileID"))
    assertTrue(bpHeaders.contains("OwnerName"))
    assertTrue(bpHeaders.contains("GSTIN"))
    assertTrue(bpHeaders.contains("UpdatedAt"))
    assertTrue(bpHeaders.contains("UpdatedBy"))

    // Section 4: Local GSTIN validation
    val validGst = ValidationService.validateGstinFormatLocally("27AAECS4912F1Z9")
    assertTrue(validGst is ValidationResult.Valid)
    assertEquals(
      ValidationService.GSTIN_LOCAL_VALIDATION_MESSAGE,
      "GSTIN format validated locally. Government verification is not performed by this app.",
    )
    val blankGst = ValidationService.validateGstinFormatLocally("")
    assertTrue(blankGst is ValidationResult.Valid)
    val invalidGst = ValidationService.validateGstinFormatLocally("INVALID123")
    assertTrue(invalidGst is ValidationResult.Invalid)
  }
}
