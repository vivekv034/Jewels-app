package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.google.GoogleAuthServiceImpl
import com.example.data.google.GoogleSheetsServiceImpl
import com.example.data.local.JewelleryDatabase
import com.example.data.local.LocalStorageService
import com.example.domain.model.AppLanguage
import com.example.domain.model.Customer
import com.example.domain.model.CustomerReminder
import com.example.domain.model.DeductionType
import com.example.domain.model.InventoryMovementType
import com.example.domain.model.Invoice
import com.example.domain.model.InvoicePrintFormat
import com.example.domain.model.InvoiceStatus
import com.example.domain.model.MetalRate
import com.example.domain.model.MetalType
import com.example.domain.model.OpeningStock
import com.example.domain.model.PaymentMode
import com.example.domain.model.PendingSyncRecord
import com.example.domain.model.PurchaseRecord
import com.example.domain.model.PuritySelectionMode
import com.example.domain.model.RateUnit
import com.example.domain.model.ReminderType
import com.example.domain.model.SaleRecord
import com.example.domain.model.SyncOperation
import com.example.domain.model.SyncRecordType
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionStatus
import com.example.domain.model.TransactionType
import com.example.domain.model.WorksheetSchemas
import com.example.domain.service.InventoryService
import com.example.domain.service.InvoicePdfService
import com.example.domain.service.InvoiceService
import com.example.ui.i18n.AppStrings
import java.math.BigDecimal
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var context: Context
  private lateinit var db: JewelleryDatabase
  private lateinit var storageService: LocalStorageService
  private lateinit var authService: GoogleAuthServiceImpl
  private lateinit var sheetsService: GoogleSheetsServiceImpl

  @Before
  fun setUp() {
    context = ApplicationProvider.getApplicationContext()
    db =
      Room.inMemoryDatabaseBuilder(context, JewelleryDatabase::class.java)
        .allowMainThreadQueries()
        .build()
    storageService = LocalStorageService(db.jewelleryDao())
    authService = GoogleAuthServiceImpl(context, storageService)
    sheetsService = GoogleSheetsServiceImpl(context, storageService)
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `read app_name from context matches Jewellery Business Manager`() {
    val appName = context.getString(R.string.app_name)
    assertEquals("Jewellery Business Manager", appName)
  }

  @Test
  fun `stage 2 google auth reads provisioned oAuthClientId and connects account`() = runBlocking {
    assertTrue(authService.isConfiguredForCurrentStage)
    val clientId = authService.readProvisionedClientId()
    assertTrue(clientId.endsWith(".apps.googleusercontent.com"))

    val connectRes =
      authService.signInWithGoogle(
        context = null,
        selectedEmailOverride = "vivek.v@digitallogics.in",
      )
    assertTrue(connectRes.isSuccess)
    val state = storageService.getGoogleAccountStateOnce()
    assertTrue(state.isConnected)
    assertEquals("vivek.v@digitallogics.in", state.connectedEmail)
  }

  @Test
  fun `stage 2 and 5 google sheets initializes all required worksheets without duplicate headers or duplicate spreadsheets`() =
    runBlocking {
      val createFirst = sheetsService.createSpreadsheet(WorksheetSchemas.DEFAULT_DATABASE_NAME)
      assertTrue(createFirst.isSuccess)
      val firstInfo = createFirst.getOrThrow()
      assertEquals(WorksheetSchemas.DEFAULT_DATABASE_NAME, firstInfo.name)
      assertEquals(WorksheetSchemas.REQUIRED_WORKSHEETS.size, firstInfo.worksheets.size)

      // Calling createSpreadsheet again with same name must reuse existing spreadsheet
      val createSecond = sheetsService.createSpreadsheet(WorksheetSchemas.DEFAULT_DATABASE_NAME)
      assertTrue(createSecond.isSuccess)
      assertEquals(firstInfo.spreadsheetId, createSecond.getOrThrow().spreadsheetId)

      // Re-initializing sheets must not duplicate header rows
      sheetsService.initializeSheets(firstInfo.spreadsheetId)
      WorksheetSchemas.REQUIRED_WORKSHEETS.forEach { wsName ->
        val rows = sheetsService.getWorksheetRows(firstInfo.spreadsheetId, wsName)
        assertEquals("Worksheet $wsName should have 1 header row", 1, rows.size)
        assertEquals(WorksheetSchemas.HEADERS_MAP[wsName], rows.first())
      }
    }

  @Test
  fun `stage 2 customer search by mobile and duplicate-safe transaction sync work end to end`() =
    runBlocking {
      val sheetInfo =
        sheetsService.createSpreadsheet(WorksheetSchemas.DEFAULT_DATABASE_NAME).getOrThrow()
      val sheetId = sheetInfo.spreadsheetId

      val customer =
        Customer(
          id = "CUST-9001",
          name = "Aarav Zaveri",
          mobileNumber = "9820011223",
          address = "Zaveri Bazaar, Mumbai",
          panNumber = "ABCDE9999K",
          gstNumber = "",
          notes = "Regular bullion buyer",
        )
      sheetsService.saveCustomer(sheetId, customer)
      sheetsService.saveCustomer(sheetId, customer.copy(notes = "VIP customer"))
      val allCustomers = sheetsService.getCustomers(sheetId).getOrThrow()
      assertEquals(1, allCustomers.count { it.id == "CUST-9001" })

      val foundByMobile = sheetsService.searchCustomerByMobile(sheetId, "9820011223").getOrNull()
      assertNotNull(foundByMobile)
      assertEquals("Aarav Zaveri", foundByMobile?.name)

      val tx =
        Transaction(
          transactionId = "TXN-50001",
          invoiceNumber = "HGR-000101",
          date = "2026-09-28",
          time = "14:30",
          timestamp = 1790565690000L,
          customerId = customer.id,
          customerName = customer.name,
          customerMobile = customer.mobileNumber,
          transactionType = TransactionType.SCRAP_GOLD,
          metalType = MetalType.GOLD,
          purityMode = PuritySelectionMode.CUSTOM_TUNCH,
          grossWeight = BigDecimal("10.000"),
          tunch = BigDecimal("75.00"),
          fineWeight = BigDecimal("7.500"),
          rate = BigDecimal("7200.00"),
          rateUnit = RateUnit.PER_GRAM,
          amount = BigDecimal("54000.00"),
          paymentMode = PaymentMode.CASH,
          notes = "Scrap exchange",
        )
      sheetsService.saveTransaction(sheetId, tx)

      sheetsService.saveRate(
        sheetId,
        MetalRate(
          rateId = "RATE-200",
          goldRate = BigDecimal("7900.00"),
          silverRate = BigDecimal("98.00"),
        ),
      )

      val savedTxs = sheetsService.getTransactions(sheetId).getOrThrow()
      val matchedTx = savedTxs.find { it.transactionId == "TXN-50001" }
      assertNotNull(matchedTx)
      assertEquals(BigDecimal("7200.00"), matchedTx?.rate)
      assertEquals(BigDecimal("54000.00"), matchedTx?.amount)

      val payload =
        JSONObject()
          .apply {
            put("transactionId", tx.transactionId)
            put("invoiceNumber", tx.invoiceNumber)
            put("date", tx.date)
            put("time", tx.time)
            put("timestamp", tx.timestamp)
            put("customerId", tx.customerId)
            put("customerName", tx.customerName)
            put("customerMobile", tx.customerMobile)
            put("transactionType", tx.transactionType.name)
            put("metalType", tx.metalType.name)
            put("purityMode", tx.purityMode.name)
            put("grossWeight", tx.grossWeight.toPlainString())
            put("tunch", tx.tunch.toPlainString())
            put("fineWeight", tx.fineWeight.toPlainString())
            put("rate", tx.rate.toPlainString())
            put("rateUnit", tx.rateUnit.name)
            put("amount", tx.amount.toPlainString())
            put("paymentMode", tx.paymentMode.name)
            put("notes", tx.notes)
          }
          .toString()

      val syncRecord =
        PendingSyncRecord(
          recordId = "SYNC-TX-50001",
          recordType = SyncRecordType.TRANSACTION,
          operation = SyncOperation.CREATE,
          payload = payload,
        )
      storageService.enqueueSyncRecord(syncRecord)
      val syncedCount =
        sheetsService.syncPendingTransactions(sheetId, listOf(syncRecord)).getOrThrow()
      assertEquals(1, syncedCount)

      val txsAfterRetry = sheetsService.getTransactions(sheetId).getOrThrow()
      assertEquals(1, txsAfterRetry.count { it.transactionId == "TXN-50001" })
    }

  @Test
  fun `stage 3 draft editing multi-items and transaction cancellation preserve historical records`() =
    runBlocking {
      val sheetInfo =
        sheetsService.createSpreadsheet(WorksheetSchemas.DEFAULT_DATABASE_NAME).getOrThrow()
      val sheetId = sheetInfo.spreadsheetId

      val multiItems =
        listOf(
          TransactionItem(
            id = "TXNI-70001-1",
            transactionId = "TXN-70001",
            itemName = "Gold Chain",
            description = "Gold Chain",
            metalType = MetalType.GOLD,
            grossWeight = BigDecimal("10.000"),
            tunch = BigDecimal("80.00"),
            purity = "80.00%",
            fineWeight = BigDecimal("8.000"),
            rate = BigDecimal("10000.00"),
            metalValue = BigDecimal("80000.00"),
            amount = BigDecimal("80000.00"),
          ),
          TransactionItem(
            id = "TXNI-70001-2",
            transactionId = "TXN-70001",
            itemName = "Gold Ring",
            description = "Gold Ring",
            metalType = MetalType.GOLD,
            grossWeight = BigDecimal("5.000"),
            tunch = BigDecimal("75.00"),
            purity = "75.00%",
            fineWeight = BigDecimal("3.750"),
            rate = BigDecimal("10000.00"),
            metalValue = BigDecimal("37500.00"),
            amount = BigDecimal("37500.00"),
          ),
        )

      // 1. Save as DRAFT first
      val draftTx =
        Transaction(
          transactionId = "TXN-70001",
          invoiceNumber = "HGR-000201",
          date = "2026-09-28",
          time = "15:00",
          timestamp = 1790566000000L,
          customerId = "CUST-9001",
          customerName = "Aarav Zaveri",
          customerMobile = "9820011223",
          transactionType = TransactionType.SCRAP_GOLD,
          metalType = MetalType.GOLD,
          purityMode = PuritySelectionMode.CUSTOM_TUNCH,
          grossWeight = BigDecimal("15.000"),
          tunch = BigDecimal("78.33"),
          fineWeight = BigDecimal("11.750"),
          rate = BigDecimal("10000.00"),
          metalValue = BigDecimal("117500.00"),
          deductionType = DeductionType.AMOUNT_DEDUCTION,
          deductionValue = BigDecimal("500.00"),
          deductionAmount = BigDecimal("500.00"),
          netValue = BigDecimal("117000.00"),
          amount = BigDecimal("117000.00"),
          status = TransactionStatus.DRAFT,
          items = multiItems,
        )
      storageService.saveTransaction(draftTx)

      val storedDraft = storageService.transactionsFlow.first().find { it.transactionId == "TXN-70001" }
      assertNotNull(storedDraft)
      assertEquals(TransactionStatus.DRAFT, storedDraft?.status)
      assertEquals(2, storedDraft?.items?.size)

      // 2. Finalize draft transaction and sync multiple items to TransactionItems sheet
      val finalizedTx = draftTx.copy(status = TransactionStatus.COMPLETED)
      storageService.saveTransaction(finalizedTx)
      sheetsService.saveTransaction(sheetId, finalizedTx)

      val txItemRows = sheetsService.getWorksheetRows(sheetId, WorksheetSchemas.SHEET_TRANSACTION_ITEMS)
      // Header + 2 item rows = 3 rows
      assertEquals(3, txItemRows.size)

      // 3. Cancel transaction without deleting historical record
      val cancelled =
        storageService.cancelTransaction(
          transactionId = "TXN-70001",
          cancelledBy = "Shop Owner",
          cancellationReason = "Customer requested weight re-verification",
        )
      assertNotNull(cancelled)
      assertEquals(TransactionStatus.CANCELLED, cancelled?.status)
      assertEquals("Shop Owner", cancelled?.cancelledBy)
      assertEquals("Customer requested weight re-verification", cancelled?.cancellationReason)

      // Historical record is still in database!
      val allTxsAfterCancel = storageService.transactionsFlow.first()
      assertTrue(allTxsAfterCancel.any { it.transactionId == "TXN-70001" })
    }

  @Test
  fun `stage 4 invoice pdf generation google drive folder upload and sheets sync work without duplicates`() =
    runBlocking {
      val sheetInfo =
        sheetsService.createSpreadsheet(WorksheetSchemas.DEFAULT_DATABASE_NAME).getOrThrow()
      val sheetId = sheetInfo.spreadsheetId
      val profile = storageService.businessProfileFlow.first()
      val invoiceService = InvoiceService()
      val pdfService = InvoicePdfService()

      val tx =
        Transaction(
          transactionId = "TXN-88001",
          invoiceNumber = "HGR-000801",
          date = "2026-09-28",
          time = "16:45",
          timestamp = 1790570000000L,
          customerId = "CUST-9001",
          customerName = "Aarav Zaveri",
          customerMobile = "9820011223",
          transactionType = TransactionType.MONEY_TO_GOLD,
          metalType = MetalType.GOLD,
          grossWeight = BigDecimal("5.000"),
          tunch = BigDecimal("100.00"),
          purity = "100.00%",
          fineWeight = BigDecimal("5.000"),
          rate = BigDecimal("10000.00"),
          metalValue = BigDecimal("50000.00"),
          netValue = BigDecimal("50000.00"),
          amount = BigDecimal("50000.00"),
          paymentMode = PaymentMode.UPI,
          status = TransactionStatus.COMPLETED,
        )
      val invoice =
        invoiceService.createInvoiceFromTransaction(
          transaction = tx,
          customer = null,
          businessProfile = profile,
        )
      storageService.saveTransactionAndInvoice(tx, invoice)

      // 1. Generate real PDF files (A4 and Thermal 80mm)
      val pdfFileA4 =
        pdfService
          .generateInvoicePdf(
            context = context,
            invoice = invoice,
            format = InvoicePrintFormat.A4,
          )
          .getOrThrow()
      assertTrue(pdfFileA4.exists())
      assertTrue(pdfFileA4.length() > 0L)
      assertEquals("Invoice_HGR-000801.pdf", pdfFileA4.name)

      val pdfFileThermal =
        pdfService
          .generateInvoicePdf(
            context = context,
            invoice = invoice,
            format = InvoicePrintFormat.THERMAL_80MM,
          )
          .getOrThrow()
      assertTrue(pdfFileThermal.exists())
      assertTrue(pdfFileThermal.length() > 0L)

      // 2. Upload to Google Drive (verifies folder hierarchy Jewellery Business Manager / Invoices)
      val invoiceWithPdf =
        invoice.copy(
          localPdfPath = pdfFileA4.absolutePath,
          status = InvoiceStatus.PDF_GENERATED.name,
          invoiceStatus = InvoiceStatus.PDF_GENERATED,
        )
      storageService.saveInvoice(invoiceWithPdf)

      val driveUploadFirst =
        sheetsService
          .saveInvoicePdfToDrive(
            invoice = invoiceWithPdf,
            pdfFile = pdfFileA4,
            forceRegenerate = false,
          )
          .getOrThrow()
      assertTrue(driveUploadFirst.driveFileId.isNotBlank())
      assertTrue(driveUploadFirst.driveFileUrl.contains(driveUploadFirst.driveFileId))
      assertEquals("Invoice_HGR-000801.pdf", driveUploadFirst.driveFileName)

      // Uploading again without forceRegenerate must NOT create a duplicate Drive file
      val updatedInvoice = storageService.getInvoiceByIdOrNumber("HGR-000801")!!
      assertTrue(updatedInvoice.isSavedToDrive)
      val driveUploadSecond =
        sheetsService
          .saveInvoicePdfToDrive(
            invoice = updatedInvoice,
            pdfFile = pdfFileA4,
            forceRegenerate = false,
          )
          .getOrThrow()
      assertEquals(driveUploadFirst.driveFileId, driveUploadSecond.driveFileId)
      assertTrue(driveUploadSecond.reusedExistingFile)

      // 3. Sync invoice to Google Sheets Invoices sheet and verify
      sheetsService.saveInvoice(sheetId, updatedInvoice)
      val sheetInvoices = sheetsService.getInvoices(sheetId).getOrThrow()
      val syncedInv = sheetInvoices.find { it.invoiceNumber == "HGR-000801" }
      assertNotNull(syncedInv)
      assertEquals("TXN-88001", syncedInv?.transactionId)
      assertEquals(driveUploadFirst.driveFileId, syncedInv?.driveFileId)
      assertTrue(syncedInv?.isSavedToDrive == true)

      // 4. Cancelling transaction marks invoice as CANCELLED while keeping permanent invoice number
      storageService.cancelTransaction("TXN-88001", "Shop Owner", "Weight correction")
      val cancelledInvoice = storageService.getInvoiceByIdOrNumber("HGR-000801")
      assertNotNull(cancelledInvoice)
      assertEquals("HGR-000801", cancelledInvoice?.invoiceNumber)
      assertEquals(InvoiceStatus.CANCELLED, cancelledInvoice?.invoiceStatus)
      assertTrue(cancelledInvoice?.isCancelled == true)
    }

  @Test
  fun `stage 5 gold and silver inventory movement ledger purchase sale scrap and reversal calculations`() =
    runBlocking {
      val inventoryService = InventoryService()

      // 1. Set Opening Gold Stock: 100.000 g gross @ 99.00% = 99.000 g fine
      val goldOpening =
        OpeningStock(
          metal = MetalType.GOLD,
          grossWeight = BigDecimal("100.000"),
          tunch = BigDecimal("99.00"),
          fineWeight = BigDecimal("99.000"),
          referenceRatePerGram = BigDecimal("10000.00"),
          date = "2026-09-01",
        )
      storageService.saveOpeningStock(goldOpening)

      // 2. Customer Gold Received: 10.000 g gross @ 99.00% = 9.900 g fine (IN)
      val rxTx =
        Transaction(
          transactionId = "TXN-S5-001",
          invoiceNumber = "HGR-000901",
          date = "2026-09-28",
          time = "11:00 AM",
          timestamp = 1790571000000L,
          customerId = "CUST-9001",
          customerName = "Aarav Zaveri",
          customerMobile = "9820011223",
          transactionType = TransactionType.GOLD_PAYMENT,
          metalType = MetalType.GOLD,
          grossWeight = BigDecimal("10.000"),
          tunch = BigDecimal("99.00"),
          fineWeight = BigDecimal("9.900"),
          rate = BigDecimal("10000.00"),
          amount = BigDecimal("99000.00"),
          status = TransactionStatus.COMPLETED,
        )
      val rxInv =
        InvoiceService().createInvoiceFromTransaction(
          rxTx,
          null,
          storageService.businessProfileFlow.first(),
        )
      storageService.saveTransactionAndInvoice(rxTx, rxInv)

      val movementsAfterRx = storageService.inventoryMovementsFlow.first()
      val goldStockAfterRx =
        inventoryService.calculateCurrentGoldStock(
          openingStock = goldOpening,
          movements = movementsAfterRx,
          currentRatePerGram = BigDecimal("10000.00"),
        )
      // 100.000 + 10.000 = 110.000 g gross; 99.000 + 9.900 = 108.900 g fine
      assertEquals(BigDecimal("110.000"), goldStockAfterRx.currentGrossWeight)
      assertEquals(BigDecimal("108.900"), goldStockAfterRx.currentFineWeight)

      // 3. Cancel transaction -> creates REVERSAL movement (OUT) without deleting original movement
      storageService.cancelTransaction("TXN-S5-001", "Owner", "Customer cancelled")
      val movementsAfterCancel = storageService.inventoryMovementsFlow.first()
      assertTrue(movementsAfterCancel.any { it.movementType == InventoryMovementType.REVERSAL })
      val goldStockAfterCancel =
        inventoryService.calculateCurrentGoldStock(
          openingStock = goldOpening,
          movements = movementsAfterCancel,
          currentRatePerGram = BigDecimal("10000.00"),
        )
      assertEquals(BigDecimal("100.000"), goldStockAfterCancel.currentGrossWeight)
      assertEquals(BigDecimal("99.000"), goldStockAfterCancel.currentFineWeight)

      // 4. Vendor Purchase (+IN) and Customer Sale (-OUT)
      val purchase =
        PurchaseRecord(
          purchaseId = "PUR-S5-01",
          date = "2026-09-28",
          time = "12:00 PM",
          timestamp = 1790572000000L,
          vendorId = "VEND-001",
          vendorName = "Zaveri Bullion House",
          vendorMobile = "9820099881",
          metal = MetalType.GOLD,
          description = "Gold Bullion Bar",
          grossWeight = BigDecimal("20.000"),
          tunch = BigDecimal("100.00"),
          fineWeight = BigDecimal("20.000"),
          rate = BigDecimal("10000.00"),
          totalAmount = BigDecimal("200000.00"),
          amountPaid = BigDecimal("150000.00"),
          balancePending = BigDecimal("50000.00"),
        )
      storageService.savePurchase(purchase, inventoryService.createMovementFromPurchase(purchase))

      val sale =
        SaleRecord(
          saleId = "SALE-S5-01",
          invoiceNumber = "HGR-000902",
          date = "2026-09-28",
          time = "01:00 PM",
          timestamp = 1790573000000L,
          customerId = "CUST-9001",
          customerName = "Aarav Zaveri",
          customerMobile = "9820011223",
          metal = MetalType.GOLD,
          description = "22K Gold Chain",
          grossWeight = BigDecimal("10.000"),
          tunch = BigDecimal("91.60"),
          fineWeight = BigDecimal("9.160"),
          rate = BigDecimal("10000.00"),
          makingCharges = BigDecimal("2000.00"),
          gstPercent = BigDecimal("3.00"),
          cgstAmount = BigDecimal("1404.00"),
          sgstAmount = BigDecimal("1404.00"),
          totalAmount = BigDecimal("96408.00"),
          amountReceived = BigDecimal("90000.00"),
          balancePending = BigDecimal("6408.00"),
        )
      storageService.saveSale(sale, inventoryService.createMovementFromSale(sale))

      val finalMovements = storageService.inventoryMovementsFlow.first()
      val finalGoldStock =
        inventoryService.calculateCurrentGoldStock(
          openingStock = goldOpening,
          movements = finalMovements,
          currentRatePerGram = BigDecimal("10000.00"),
        )
      // 99.000 + 20.000 - 9.160 = 109.840 g fine
      assertEquals(BigDecimal("109.840"), finalGoldStock.currentFineWeight)
    }

  @Test
  fun `stage 6 hindi language persistence backup restore reminders and data integrity verification`() =
    runBlocking {
      // 1. Hindi translation keys & Language persistence
      val hindiStrings = AppStrings.forLanguage(AppLanguage.HINDI)
      assertEquals("डैशबोर्ड", hindiStrings.dashboard)
      assertEquals("ग्राहक", hindiStrings.customer)
      assertEquals("विक्रेता", hindiStrings.vendor)
      assertEquals("सोना", hindiStrings.gold)
      assertEquals("चाँदी", hindiStrings.silver)
      assertEquals("बिल", hindiStrings.invoice)
      assertEquals("लेन-देन", hindiStrings.transaction)
      assertEquals("खरीद", hindiStrings.purchase)
      assertEquals("बिक्री", hindiStrings.sale)
      assertEquals("वजन", hindiStrings.weight)
      assertEquals("टंच", hindiStrings.tunch)
      assertEquals("शुद्ध वजन", hindiStrings.fineWeight)
      assertEquals("भाव", hindiStrings.rate)
      assertEquals("राशि", hindiStrings.amount)
      assertEquals("बकाया", hindiStrings.pending)
      assertEquals("सेव करें", hindiStrings.save)
      assertEquals("रद्द करें", hindiStrings.cancel)
      assertEquals("खोजें", hindiStrings.search)

      val currentProfile = storageService.businessProfileFlow.first()
      storageService.saveBusinessProfile(
        currentProfile.copy(
          language = AppLanguage.HINDI,
          gstEnabled = true,
          defaultGstRatePercent = BigDecimal("3.00"),
        )
      )
      val reloadedProfile = storageService.businessProfileFlow.first()
      assertEquals(AppLanguage.HINDI, reloadedProfile.language)
      assertTrue(reloadedProfile.gstEnabled)

      // 2. Customer Reminder creation
      val reminder =
        CustomerReminder(
          reminderId = "REM-TEST-01",
          customerId = "CUST-001",
          customerName = "Rajesh Kumar",
          customerMobile = "9876543210",
          reminderType = ReminderType.PENDING_MONEY,
          dueDate = "2026-09-30",
          pendingAmount = BigDecimal("12500.00"),
          notes = "Pending balance follow-up",
        )
      storageService.saveReminder(reminder)
      val savedReminders = storageService.remindersFlow.first()
      assertTrue(savedReminders.any { it.reminderId == "REM-TEST-01" })

      // 3. Full JSON Backup & Restore + Data Integrity Check
      val jsonBackup = storageService.exportFullBackupJson()
      assertTrue(jsonBackup.contains("backupVersion"))
      val restoreRes = storageService.restoreFromBackupJson(jsonBackup)
      assertTrue(restoreRes.isSuccess)

      val (issuesCount, _) = storageService.verifyDataIntegrity()
      assertEquals(0, issuesCount)
    }

  @Test
  fun `stage 7 shop owner profile gstin update historical invoice protection customer and pdf verification`() =
    runBlocking {
      val invoiceService = InvoiceService()
      val pdfService = InvoicePdfService()

      // 1 & 2. Edit Shop Owner Name, GSTIN, and full Business Profile fields
      val initialProfile = storageService.businessProfileFlow.first()
      val updatedProfile1 =
        initialProfile.copy(
          businessId = "BP-001",
          shopName = "Shree Ganesh Jewellers",
          ownerName = "Rameshwar Prasad Soni",
          mobileNumber = "9876543210",
          whatsappNumber = "9876543210",
          email = "contact@shreeganesh.in",
          address = "124 Johari Bazar, Near Hawa Mahal",
          city = "Jaipur",
          district = "Jaipur",
          state = "Rajasthan",
          pinCode = "302003",
          panNumber = "ABCDE1234F",
          gstNumber = "08ABCDE1234F1Z5",
          gstEnabled = true,
          showGstOnInvoice = true,
          defaultGstRatePercent = BigDecimal("3.00"),
          bankName = "HDFC Bank",
          branchName = "Johari Bazar Branch",
          bankAccountNumber = "50200012345678",
          ifsc = "HDFC0001234",
          upiId = "shreeganesh@hdfcbank",
          invoicePrefix = "SGJ-",
          updatedAt = 1727500000000L,
          updatedBy = "Rameshwar Prasad Soni",
        )
      storageService.saveBusinessProfile(updatedProfile1)
      val savedBp1 = storageService.businessProfileFlow.first()
      assertEquals("Rameshwar Prasad Soni", savedBp1.ownerName)
      assertEquals("08ABCDE1234F1Z5", savedBp1.gstNumber)
      assertTrue(savedBp1.formattedFullAddress().contains("Near Hawa Mahal"))
      assertTrue(savedBp1.formattedFullAddress().contains("Jaipur"))

      // 3. Create new customer with full 11 fields & prevent duplicate by mobile
      val customer1 =
        Customer(
          id = "CUST-S7-01",
          name = "Suresh Khandelwal",
          mobileNumber = "9123456789",
          whatsappNumber = "9123456789",
          address = "45 MI Road",
          city = "Jaipur",
          state = "Rajasthan",
          pinCode = "302001",
          panNumber = "FGHIJ5678K",
          gstNumber = "08FGHIJ5678K1Z2",
          email = "suresh@example.com",
          pendingAmount = BigDecimal("1000.00"),
          notes = "VIP Gold Customer",
        )
      storageService.saveCustomer(customer1)
      val duplicateAttempt =
        Customer(
          id = "CUST-S7-999",
          name = "Suresh Khandelwal Updated",
          mobileNumber = "9123456789",
          city = "Udaipur",
        )
      storageService.saveCustomer(duplicateAttempt)
      val allCusts = storageService.customersFlow.first()
      assertEquals(1, allCusts.count { it.mobileNumber == "9123456789" })
      assertEquals("CUST-S7-01", allCusts.first { it.mobileNumber == "9123456789" }.id)

      // 4 & 5. Create Gold Transaction & Invoice #1 under Owner 1 / GSTIN 1
      val tx1 =
        Transaction(
          transactionId = "TXN-S7-01",
          invoiceNumber = "SGJ-0001",
          date = "2026-09-28",
          time = "11:00:00",
          timestamp = 1790580000000L,
          customerId = customer1.id,
          customerName = customer1.name,
          customerMobile = customer1.mobileNumber,
          transactionType = TransactionType.MONEY_TO_GOLD,
          metalType = MetalType.GOLD,
          purityMode = PuritySelectionMode.CUSTOM_TUNCH,
          grossWeight = BigDecimal("10.000"),
          tunch = BigDecimal("99.00"),
          fineWeight = BigDecimal("9.900"),
          rate = BigDecimal("10000.00"),
          rateUnit = RateUnit.PER_GRAM,
          amount = BigDecimal("99000.00"),
          paymentMode = PaymentMode.UPI,
        )
      val inv1 =
        invoiceService.createInvoiceFromTransaction(
          transaction = tx1,
          customer = customer1,
          businessProfile = savedBp1,
        )
      storageService.saveTransactionAndInvoice(tx1, inv1)

      // 6, 7 & 8. Edit Shop Owner Name and GSTIN again -> Verify Old Invoice remains unchanged, New Invoice uses updated details
      val updatedProfile2 =
        savedBp1.copy(
          ownerName = "Vikramaditya Soni",
          gstNumber = "",
          gstEnabled = false,
          showGstOnInvoice = false,
        )
      storageService.saveBusinessProfile(updatedProfile2)
      val savedBp2 = storageService.businessProfileFlow.first()

      val tx2 =
        tx1.copy(
          transactionId = "TXN-S7-02",
          invoiceNumber = "SGJ-0002",
          metalType = MetalType.SILVER,
          transactionType = TransactionType.SCRAP_SILVER,
          grossWeight = BigDecimal("100.000"),
          tunch = BigDecimal("80.00"),
          fineWeight = BigDecimal("80.000"),
        )
      val inv2 =
        invoiceService.createInvoiceFromTransaction(
          transaction = tx2,
          customer = customer1,
          businessProfile = savedBp2,
        )
      storageService.saveTransactionAndInvoice(tx2, inv2)

      val storedInvoices = storageService.invoicesFlow.first()
      val historicalInv1 = storedInvoices.first { it.invoiceId == inv1.invoiceId }
      val newInv2 = storedInvoices.first { it.invoiceId == inv2.invoiceId }

      // Historical invoice #1 must preserve original owner and GSTIN
      assertEquals("Rameshwar Prasad Soni", historicalInv1.ownerName)
      assertEquals("08ABCDE1234F1Z5", historicalInv1.shopGst)
      assertTrue(historicalInv1.gstEnabled)

      // New invoice #2 must reflect new owner and blank/disabled GST
      assertEquals("Vikramaditya Soni", newInv2.ownerName)
      assertEquals("", newInv2.shopGst)
      assertTrue(!newInv2.gstEnabled)
      assertTrue(!newInv2.showGst)

      // 9. Generate PDF and verify file creation
      val pdfResult = pdfService.generateInvoicePdf(context, historicalInv1)
      assertTrue(pdfResult.isSuccess)
      val pdfFile = pdfResult.getOrNull()!!
      assertTrue(pdfFile.exists())
      assertTrue(pdfFile.length() > 0L)
    }
}
