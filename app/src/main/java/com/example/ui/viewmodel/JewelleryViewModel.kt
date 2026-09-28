package com.example.ui.viewmodel

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.domain.calculation.CalculationConfig
import com.example.domain.calculation.CalculationEngine
import com.example.domain.calculation.ValidationResult
import com.example.domain.calculation.ValidationService
import com.example.domain.model.AppLanguage
import com.example.domain.model.AppSecuritySettings
import com.example.domain.model.ApplicationHealthStatus
import com.example.domain.model.AuditActionType
import com.example.domain.model.AuditLogEntry
import com.example.domain.model.BusinessProfile
import com.example.domain.model.Customer
import com.example.domain.model.CustomerLedgerEntry
import com.example.domain.model.CustomerLedgerSummary
import com.example.domain.model.CustomerReminder
import com.example.domain.model.DailyReportData
import com.example.domain.model.DailySummaryRecord
import com.example.domain.model.DashboardSummary
import com.example.domain.model.DeductionType
import com.example.domain.model.DriveUploadStatus
import com.example.domain.model.GlobalSearchResults
import com.example.domain.model.GoogleAccountState
import com.example.domain.model.InventoryMovement
import com.example.domain.model.InventoryMovementDirection
import com.example.domain.model.InventoryMovementType
import com.example.domain.model.Invoice
import com.example.domain.model.InvoicePrintFormat
import com.example.domain.model.InvoiceStatus
import com.example.domain.model.InvoiceTypeFilter
import com.example.domain.model.MetalInventorySummary
import com.example.domain.model.MetalRate
import com.example.domain.model.MetalType
import com.example.domain.model.MonthlyReportData
import com.example.domain.model.OpeningStock
import com.example.domain.model.PaymentMode
import com.example.domain.model.PendingDriveUpload
import com.example.domain.model.PendingSyncRecord
import com.example.domain.model.PurchaseRecord
import com.example.domain.model.PuritySelectionMode
import com.example.domain.model.RateUnit
import com.example.domain.model.ReminderStatus
import com.example.domain.model.ReminderType
import com.example.domain.model.ReportDateFilter
import com.example.domain.model.SaleRecord
import com.example.domain.model.ScrapProcessRecord
import com.example.domain.model.SpreadsheetInfo
import com.example.domain.model.StockReconciliationRecord
import com.example.domain.model.SyncOperation
import com.example.domain.model.SyncRecordType
import com.example.domain.model.SyncStatus
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionStatus
import com.example.domain.model.TransactionType
import com.example.domain.model.UserAccessMode
import com.example.domain.model.Vendor
import com.example.domain.model.VendorLedgerSummary
import com.example.domain.model.WorksheetSchemas
import com.example.domain.service.GoogleAuthService
import com.example.domain.service.GoogleDriveService
import com.example.domain.service.GoogleSheetsService
import com.example.domain.service.InventoryService
import com.example.domain.service.InvoicePdfService
import com.example.domain.service.InvoiceService
import com.example.domain.service.StorageService
import java.io.File
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONObject

enum class MainNavTab {
  DASHBOARD,
  NEW_TRANSACTION,
  CUSTOMERS,
  INVOICES,
  MORE,
  INVENTORY,
  PURCHASES,
  SALES,
  VENDORS,
  REPORTS,
  SETTINGS,
  GLOBAL_SEARCH,
  BACKUP_RECOVERY,
}

enum class SettingsSection {
  MENU,
  BUSINESS_PROFILE,
  METAL_RATES,
  CALCULATION_RULES,
  GST_CONFIGURATION,
  GOOGLE_ACCOUNT,
  GOOGLE_SHEETS_DATABASE,
  GOOGLE_DRIVE_STORAGE,
  SYNC_STATUS,
  INVOICE_SETTINGS,
  LANGUAGE,
  SECURITY_PIN,
  AUDIT_CONTROLS,
  REMINDERS,
  APP_HEALTH,
  PERFORMANCE_PRINTING,
  DATA_BACKUP,
}

data class SavedTransactionSuccessState(
  val transaction: Transaction,
  val invoice: Invoice?,
  val statusMessage: String = "Transaction Saved Successfully",
)

data class MobileCustomerSearchResult(
  val queryMobile: String,
  val customer: Customer?,
  val source: String, // "Local Cache", "Google Sheets (Customers)", or "Not Found"
)

class JewelleryViewModel(
  private val storageService: StorageService,
  private val googleAuthService: GoogleAuthService? = null,
  private val googleSheetsService: GoogleSheetsService? = null,
  private val googleDriveService: GoogleDriveService? = googleSheetsService as? GoogleDriveService,
  private val invoiceService: InvoiceService = InvoiceService(),
  private val invoicePdfService: InvoicePdfService = InvoicePdfService(),
  val inventoryService: InventoryService = InventoryService(),
) : ViewModel() {

  val businessProfile: StateFlow<BusinessProfile> =
    storageService.businessProfileFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = BusinessProfile(),
    )

  val metalRate: StateFlow<MetalRate> =
    storageService.metalRateFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = MetalRate(),
    )

  val customers: StateFlow<List<Customer>> =
    storageService.customersFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val transactions: StateFlow<List<Transaction>> =
    storageService.transactionsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val invoices: StateFlow<List<Invoice>> =
    storageService.invoicesFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val pendingSyncQueue: StateFlow<List<PendingSyncRecord>> =
    storageService.pendingSyncQueueFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val pendingDriveUploads: StateFlow<List<PendingDriveUpload>> =
    storageService.pendingDriveUploadsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val googleAccountState: StateFlow<GoogleAccountState> =
    storageService.googleAccountStateFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = GoogleAccountState(),
    )

  // ==================== STAGE 5 & 6 STATE FLOWS ====================

  val openingStocks: StateFlow<List<OpeningStock>> =
    storageService.openingStocksFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val inventoryMovements: StateFlow<List<InventoryMovement>> =
    storageService.inventoryMovementsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val vendors: StateFlow<List<Vendor>> =
    storageService.vendorsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val purchases: StateFlow<List<PurchaseRecord>> =
    storageService.purchasesFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val sales: StateFlow<List<SaleRecord>> =
    storageService.salesFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val scrapProcesses: StateFlow<List<ScrapProcessRecord>> =
    storageService.scrapProcessesFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val reconciliations: StateFlow<List<StockReconciliationRecord>> =
    storageService.reconciliationsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val securitySettings: StateFlow<AppSecuritySettings> =
    storageService.securitySettingsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = AppSecuritySettings(),
    )

  val auditLogs: StateFlow<List<AuditLogEntry>> =
    storageService.auditLogsFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val reminders: StateFlow<List<CustomerReminder>> =
    storageService.remindersFlow.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5000),
      initialValue = emptyList(),
    )

  val goldInventorySummary: StateFlow<MetalInventorySummary> =
    combine(openingStocks, inventoryMovements, metalRate) { openings, movements, rate ->
        inventoryService.calculateCurrentGoldStock(
          openingStock = openings.find { it.metal == MetalType.GOLD },
          movements = movements,
          currentRatePerGram = rate.goldRatePerGram,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MetalInventorySummary(metal = MetalType.GOLD),
      )

  val silverInventorySummary: StateFlow<MetalInventorySummary> =
    combine(openingStocks, inventoryMovements, metalRate) { openings, movements, rate ->
        inventoryService.calculateCurrentSilverStock(
          openingStock = openings.find { it.metal == MetalType.SILVER },
          movements = movements,
          currentRatePerGram = rate.silverRatePerGram,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = MetalInventorySummary(metal = MetalType.SILVER),
      )

  private val _selectedVendorForDetail = MutableStateFlow<Vendor?>(null)
  val selectedVendorForDetail: StateFlow<Vendor?> = _selectedVendorForDetail.asStateFlow()

  private val _globalSearchQuery = MutableStateFlow("")
  val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

  val globalSearchResults: StateFlow<GlobalSearchResults> =
    combine(
        _globalSearchQuery,
        customers,
        vendors,
        transactions,
        invoices,
      ) { query, custs, vends, txs, invs ->
        val q = query.trim().lowercase()
        if (q.isEmpty()) {
          GlobalSearchResults()
        } else {
          GlobalSearchResults(
            query = query,
            customers =
              custs.filter {
                it.name.lowercase().contains(q) ||
                  it.mobile.lowercase().contains(q) ||
                  it.id.lowercase().contains(q)
              },
            vendors =
              vends.filter {
                it.name.lowercase().contains(q) ||
                  it.mobile.lowercase().contains(q) ||
                  it.companyName.lowercase().contains(q) ||
                  it.vendorId.lowercase().contains(q)
              },
            transactions =
              txs.filter {
                it.transactionId.lowercase().contains(q) ||
                  it.invoiceNumber.lowercase().contains(q) ||
                  it.customerName.lowercase().contains(q) ||
                  it.customerMobile.lowercase().contains(q) ||
                  it.date.lowercase().contains(q)
              },
            invoices =
              invs.filter {
                it.invoiceNumber.lowercase().contains(q) ||
                  it.customerName.lowercase().contains(q) ||
                  it.customerMobile.lowercase().contains(q) ||
                  it.date.lowercase().contains(q)
              },
            purchases =
              purchases.value.filter {
                it.purchaseId.lowercase().contains(q) ||
                  it.vendorName.lowercase().contains(q) ||
                  it.vendorMobile.lowercase().contains(q) ||
                  it.date.lowercase().contains(q)
              },
            sales =
              sales.value.filter {
                it.saleId.lowercase().contains(q) ||
                  it.invoiceNumber.lowercase().contains(q) ||
                  it.customerName.lowercase().contains(q) ||
                  it.customerMobile.lowercase().contains(q) ||
                  it.date.lowercase().contains(q)
              },
          )
        }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GlobalSearchResults(),
      )

  private val _isPinUnlockedInSession = MutableStateFlow(false)
  val isPinUnlockedInSession: StateFlow<Boolean> = _isPinUnlockedInSession.asStateFlow()

  private val _healthStatus = MutableStateFlow(ApplicationHealthStatus())
  val healthStatus: StateFlow<ApplicationHealthStatus> = _healthStatus.asStateFlow()

  private val _availableSpreadsheets = MutableStateFlow<List<SpreadsheetInfo>>(emptyList())
  val availableSpreadsheets: StateFlow<List<SpreadsheetInfo>> = _availableSpreadsheets.asStateFlow()

  private val _showGoogleOAuthConsentSheet = MutableStateFlow(false)
  val showGoogleOAuthConsentSheet: StateFlow<Boolean> = _showGoogleOAuthConsentSheet.asStateFlow()

  private val _mobileSearchResult = MutableStateFlow<MobileCustomerSearchResult?>(null)
  val mobileSearchResult: StateFlow<MobileCustomerSearchResult?> = _mobileSearchResult.asStateFlow()

  private val _currentTab = MutableStateFlow(MainNavTab.DASHBOARD)
  val currentTab: StateFlow<MainNavTab> = _currentTab.asStateFlow()

  private val _selectedTransactionType = MutableStateFlow<TransactionType?>(null)
  val selectedTransactionType: StateFlow<TransactionType?> = _selectedTransactionType.asStateFlow()

  private val _editingDraftTransaction = MutableStateFlow<Transaction?>(null)
  val editingDraftTransaction: StateFlow<Transaction?> = _editingDraftTransaction.asStateFlow()

  private val _savedTransactionSuccess = MutableStateFlow<SavedTransactionSuccessState?>(null)
  val savedTransactionSuccess: StateFlow<SavedTransactionSuccessState?> =
    _savedTransactionSuccess.asStateFlow()

  private val _selectedInvoiceForPreview = MutableStateFlow<Invoice?>(null)
  val selectedInvoiceForPreview: StateFlow<Invoice?> = _selectedInvoiceForPreview.asStateFlow()

  private val _invoicePrintFormat = MutableStateFlow(InvoicePrintFormat.A4)
  val invoicePrintFormat: StateFlow<InvoicePrintFormat> = _invoicePrintFormat.asStateFlow()

  private val _invoiceTypeFilter = MutableStateFlow(InvoiceTypeFilter.ALL)
  val invoiceTypeFilter: StateFlow<InvoiceTypeFilter> = _invoiceTypeFilter.asStateFlow()

  private val _invoiceDateFilter = MutableStateFlow(ReportDateFilter.ALL_TIME)
  val invoiceDateFilter: StateFlow<ReportDateFilter> = _invoiceDateFilter.asStateFlow()

  private val _invoiceCustomStartDate = MutableStateFlow(getTodayDateString())
  val invoiceCustomStartDate: StateFlow<String> = _invoiceCustomStartDate.asStateFlow()

  private val _invoiceCustomEndDate = MutableStateFlow(getTodayDateString())
  val invoiceCustomEndDate: StateFlow<String> = _invoiceCustomEndDate.asStateFlow()

  private val _invoiceSearchQuery = MutableStateFlow("")
  val invoiceSearchQuery: StateFlow<String> = _invoiceSearchQuery.asStateFlow()

  private val _selectedCustomerForHistory = MutableStateFlow<Customer?>(null)
  val selectedCustomerForHistory: StateFlow<Customer?> = _selectedCustomerForHistory.asStateFlow()

  private val _activeSettingsSection = MutableStateFlow(SettingsSection.MENU)
  val activeSettingsSection: StateFlow<SettingsSection> = _activeSettingsSection.asStateFlow()

  private val _nextSuggestedInvoiceNumber = MutableStateFlow("HGR-000006")
  val nextSuggestedInvoiceNumber: StateFlow<String> = _nextSuggestedInvoiceNumber.asStateFlow()

  private val _statusBannerMessage = MutableStateFlow<String?>(null)
  val statusBannerMessage: StateFlow<String?> = _statusBannerMessage.asStateFlow()

  // Report Filters
  private val _reportDateFilter = MutableStateFlow(ReportDateFilter.TODAY)
  val reportDateFilter: StateFlow<ReportDateFilter> = _reportDateFilter.asStateFlow()

  private val _customStartDate = MutableStateFlow(getTodayDateString())
  val customStartDate: StateFlow<String> = _customStartDate.asStateFlow()

  private val _customEndDate = MutableStateFlow(getTodayDateString())
  val customEndDate: StateFlow<String> = _customEndDate.asStateFlow()

  private val _selectedReportCustomerId = MutableStateFlow<String?>(null)
  val selectedReportCustomerId: StateFlow<String?> = _selectedReportCustomerId.asStateFlow()

  val dashboardSummary: StateFlow<DashboardSummary> =
    combine(transactions, customers) { txList, custList ->
        val today = getTodayDateString()
        val todayTxs =
          txList.filter {
            it.date == today &&
              it.status != TransactionStatus.CANCELLED &&
              it.status != TransactionStatus.DRAFT
          }

        val todayGoldCount = todayTxs.count { it.metalType == MetalType.GOLD }
        val todaySilverCount = todayTxs.count { it.metalType == MetalType.SILVER }

        val totalTodayValue =
          todayTxs.fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }

        val moneyReceivedToday =
          todayTxs
            .filter {
              it.transactionType == TransactionType.MONEY_TO_GOLD ||
                it.transactionType == TransactionType.MONEY_TO_SILVER
            }
            .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }

        val goldReceivedToday =
          todayTxs
            .filter { it.transactionType == TransactionType.GOLD_PAYMENT }
            .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.fineWeight) }

        val silverReceivedToday =
          todayTxs
            .filter { it.transactionType == TransactionType.SILVER_PAYMENT }
            .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.fineWeight) }

        val scrapGoldToday =
          todayTxs
            .filter { it.transactionType == TransactionType.SCRAP_GOLD }
            .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.fineWeight) }

        val scrapSilverToday =
          todayTxs
            .filter { it.transactionType == TransactionType.SCRAP_SILVER }
            .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.fineWeight) }

        val totalPendingCustomer =
          custList.fold(BigDecimal.ZERO) { acc, c -> acc.add(c.pendingAmount) }

        DashboardSummary(
          todayTotalTransactionsCount = todayTxs.size,
          todayTotalTransactionsAmount = totalTodayValue,
          todayGoldTransactionsCount = todayGoldCount,
          todaySilverTransactionsCount = todaySilverCount,
          totalMoneyReceived = moneyReceivedToday,
          goldReceivedGrams = goldReceivedToday,
          silverReceivedGrams = silverReceivedToday,
          scrapGoldReceivedGrams = scrapGoldToday,
          scrapSilverReceivedGrams = scrapSilverToday,
          pendingCustomerAmount = totalPendingCustomer,
        )
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardSummary(),
      )

  init {
    viewModelScope.launch {
      storageService.seedInitialDataIfNeeded()
      refreshNextInvoiceNumber()
      refreshAvailableSpreadsheets()

      val acct = storageService.getGoogleAccountStateOnce()
      if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode) {
        performFullCloudSync(silent = true)
      }
    }
  }

  fun getCalculationEngine(): CalculationEngine {
    val p = businessProfile.value
    return CalculationEngine(
      CalculationConfig(
        weightScale = p.weightDecimalPlaces,
        moneyScale = p.moneyDecimalPlaces,
        tunchScale = p.tunchDecimalPlaces,
        purityDivisor = p.purityDivisor,
        maxTunchValue = p.maxTunchValue,
      )
    )
  }

  private suspend fun refreshNextInvoiceNumber() {
    _nextSuggestedInvoiceNumber.value = storageService.getNextInvoiceNumber()
  }

  fun refreshAvailableSpreadsheets() {
    viewModelScope.launch {
      val list = googleSheetsService?.listAvailableSpreadsheets()?.getOrDefault(emptyList())
        ?: emptyList()
      _availableSpreadsheets.value = list
    }
  }

  fun selectTab(tab: MainNavTab) {
    _currentTab.value = tab
    if (tab != MainNavTab.NEW_TRANSACTION) {
      _savedTransactionSuccess.value = null
      _editingDraftTransaction.value = null
    }
    if (tab != MainNavTab.INVOICES) {
      _selectedInvoiceForPreview.value = null
    }
    if (tab != MainNavTab.CUSTOMERS) {
      _selectedCustomerForHistory.value = null
    }
    if (tab == MainNavTab.NEW_TRANSACTION) {
      viewModelScope.launch { refreshNextInvoiceNumber() }
    }
  }

  fun openNewTransactionType(type: TransactionType?) {
    viewModelScope.launch { refreshNextInvoiceNumber() }
    _savedTransactionSuccess.value = null
    _editingDraftTransaction.value = null
    _selectedTransactionType.value = type
    _currentTab.value = MainNavTab.NEW_TRANSACTION
  }

  fun openDraftTransactionForEdit(draftTransaction: Transaction) {
    if (draftTransaction.status != TransactionStatus.DRAFT) {
      _statusBannerMessage.value = "Only DRAFT transactions can be edited before finalization."
      return
    }
    _savedTransactionSuccess.value = null
    _editingDraftTransaction.value = draftTransaction
    _selectedTransactionType.value = draftTransaction.transactionType
    _currentTab.value = MainNavTab.NEW_TRANSACTION
  }

  fun openCreateInvoiceFlow() {
    viewModelScope.launch { refreshNextInvoiceNumber() }
    _savedTransactionSuccess.value = null
    _editingDraftTransaction.value = null
    _selectedTransactionType.value = TransactionType.MONEY_TO_GOLD
    _currentTab.value = MainNavTab.NEW_TRANSACTION
  }

  fun openInvoicePreview(invoice: Invoice?) {
    _selectedInvoiceForPreview.value = invoice
    if (invoice != null) {
      _currentTab.value = MainNavTab.INVOICES
    }
  }

  /**
   * Opens the permanent invoice for a completed transaction without generating a new invoice number.
   */
  fun openInvoiceForTransaction(transaction: Transaction) {
    if (transaction.status == TransactionStatus.DRAFT) {
      _statusBannerMessage.value = "Finalize the draft transaction first to generate an invoice."
      return
    }
    viewModelScope.launch {
      val existing =
        invoices.value.find {
          it.transactionId == transaction.transactionId ||
            it.invoiceNumber == transaction.invoiceNumber
        } ?: storageService.getInvoiceByIdOrNumber(transaction.invoiceNumber)

      if (existing != null) {
        _selectedInvoiceForPreview.value = existing
        _currentTab.value = MainNavTab.INVOICES
      } else {
        val customer = customers.value.find { it.id == transaction.customerId }
        val created =
          invoiceService.createInvoiceFromTransaction(
            transaction = transaction,
            customer = customer,
            businessProfile = businessProfile.value,
          )
        storageService.saveInvoice(created)
        _selectedInvoiceForPreview.value = created
        _currentTab.value = MainNavTab.INVOICES
      }
    }
  }

  fun openCustomerTransactionHistory(customer: Customer?) {
    _selectedCustomerForHistory.value = customer
    if (customer != null) {
      _currentTab.value = MainNavTab.CUSTOMERS
    }
  }

  fun getCustomerLedgerSummary(customerId: String, customerName: String = ""): CustomerLedgerSummary {
    val customerTxs = transactions.value.filter { it.customerId == customerId }
    return getCalculationEngine().calculateCustomerLedger(
      customerId = customerId,
      customerName = customerName,
      transactions = customerTxs,
    )
  }

  fun openSettingsSection(section: SettingsSection) {
    _activeSettingsSection.value = section
    _currentTab.value = MainNavTab.SETTINGS
    if (
      section == SettingsSection.GOOGLE_SHEETS_DATABASE ||
        section == SettingsSection.GOOGLE_ACCOUNT ||
        section == SettingsSection.GOOGLE_DRIVE_STORAGE
    ) {
      refreshAvailableSpreadsheets()
    }
  }

  fun clearStatusBanner() {
    _statusBannerMessage.value = null
  }

  fun showBanner(message: String) {
    _statusBannerMessage.value = message
  }

  // ==================== STAGE 2 & 4: GOOGLE AUTH, SHEETS & DRIVE ====================

  fun startGoogleAccountConnection(context: Context?) {
    if (googleAuthService == null) {
      _showGoogleOAuthConsentSheet.value = true
      return
    }
    viewModelScope.launch {
      val res = googleAuthService.signInWithGoogle(context)
      if (res.isSuccess) {
        onGoogleAccountConnectedSuccess(res.getOrThrow())
      } else {
        val msg = res.exceptionOrNull()?.message ?: ""
        if (msg == "SHOW_OAUTH_ACCOUNT_SHEET") {
          _showGoogleOAuthConsentSheet.value = true
        } else {
          _statusBannerMessage.value = msg.ifBlank { "Google account connection cancelled." }
        }
      }
    }
  }

  fun confirmGoogleOAuthSheetConnection(email: String, accessToken: String = "") {
    _showGoogleOAuthConsentSheet.value = false
    viewModelScope.launch {
      val res =
        googleAuthService?.signInWithGoogle(
          context = null,
          selectedEmailOverride = email,
          accessTokenOverride = accessToken,
        )
      if (res != null && res.isSuccess) {
        onGoogleAccountConnectedSuccess(res.getOrThrow())
      }
    }
  }

  fun cancelGoogleOAuthSheetConnection() {
    _showGoogleOAuthConsentSheet.value = false
    _statusBannerMessage.value = "Google account connection cancelled."
  }

  private suspend fun onGoogleAccountConnectedSuccess(state: GoogleAccountState) {
    _statusBannerMessage.value = "Google account connected."
    googleDriveService?.ensureDriveFolders()
    if (state.selectedSpreadsheetId.isNotBlank()) {
      googleSheetsService?.connect(
        state.selectedSpreadsheetId,
        state.selectedSpreadsheetName.ifBlank { WorksheetSchemas.DEFAULT_DATABASE_NAME },
      )
      performFullCloudSync(silent = true)
    } else {
      val createRes =
        googleSheetsService?.createSpreadsheet(WorksheetSchemas.DEFAULT_DATABASE_NAME)
      if (createRes != null && createRes.isSuccess) {
        refreshAvailableSpreadsheets()
        pushAllLocalDataToGoogleSheets()
        _statusBannerMessage.value = "Google account connected. Google Sheets & Drive connected."
      }
    }
  }

  fun disconnectGoogleAccount() {
    viewModelScope.launch {
      googleAuthService?.disconnectGoogleAccount()
      _statusBannerMessage.value = "Google account disconnected."
    }
  }

  fun reconnectGoogleAccount(context: Context?) {
    viewModelScope.launch {
      val res = googleAuthService?.reconnectGoogleAccount(context)
      if (res != null && res.isSuccess) {
        _statusBannerMessage.value = "Google account connected."
        googleDriveService?.ensureDriveFolders()
        performFullCloudSync(silent = false)
      } else {
        _showGoogleOAuthConsentSheet.value = true
      }
    }
  }

  fun createOrSelectDatabaseSpreadsheet(title: String = WorksheetSchemas.DEFAULT_DATABASE_NAME) {
    viewModelScope.launch {
      val res = googleSheetsService?.createSpreadsheet(title.ifBlank { WorksheetSchemas.DEFAULT_DATABASE_NAME })
      if (res != null && res.isSuccess) {
        refreshAvailableSpreadsheets()
        pushAllLocalDataToGoogleSheets()
        _statusBannerMessage.value = "Google Sheets connected."
      } else {
        _statusBannerMessage.value =
          res?.exceptionOrNull()?.message ?: "Unable to synchronize. Please try again."
      }
    }
  }

  fun selectExistingSpreadsheet(info: SpreadsheetInfo) {
    viewModelScope.launch {
      val res = googleSheetsService?.connect(info.spreadsheetId, info.name)
      if (res != null && res.isSuccess) {
        refreshAvailableSpreadsheets()
        performFullCloudSync(silent = false)
        _statusBannerMessage.value = "Google Sheets connected."
      } else {
        _statusBannerMessage.value = "Unable to synchronize. Please try again."
      }
    }
  }

  fun setOfflineMode(isOffline: Boolean) {
    viewModelScope.launch {
      val current = storageService.getGoogleAccountStateOnce()
      storageService.saveGoogleAccountState(current.copy(isOfflineMode = isOffline))
      if (isOffline) {
        _statusBannerMessage.value = "Offline mode active — new transactions and PDF uploads will be queued on this device."
      } else {
        _statusBannerMessage.value = "Online connection restored — synchronizing pending records..."
        performFullCloudSync(silent = false)
      }
    }
  }

  fun syncNow() {
    viewModelScope.launch {
      performFullCloudSync(silent = false)
    }
  }

  private suspend fun pushAllLocalDataToGoogleSheets() {
    val state = storageService.getGoogleAccountStateOnce()
    val sheetId = state.selectedSpreadsheetId
    if (!state.isConnected || sheetId.isBlank() || state.isOfflineMode || googleSheetsService == null) {
      return
    }
    googleDriveService?.ensureDriveFolders()
    googleSheetsService.initializeSheets(sheetId)
    googleSheetsService.saveBusinessProfile(sheetId, businessProfile.value)
    googleSheetsService.saveRate(sheetId, metalRate.value)
    customers.value.forEach { googleSheetsService.saveCustomer(sheetId, it) }
    transactions.value.filter { it.status != TransactionStatus.DRAFT }.forEach { tx ->
      googleSheetsService.saveTransaction(sheetId, tx)
      storageService.updateTransactionSyncStatus(tx.transactionId, SyncStatus.SYNCED)
    }
    invoices.value.forEach { googleSheetsService.saveInvoice(sheetId, it) }
    openingStocks.value.forEach { googleSheetsService.saveOpeningStock(sheetId, it) }
    inventoryMovements.value.forEach { googleSheetsService.saveInventoryMovement(sheetId, it) }
    vendors.value.forEach { googleSheetsService.saveVendor(sheetId, it) }
    purchases.value.forEach { googleSheetsService.savePurchase(sheetId, it) }
    sales.value.forEach { googleSheetsService.saveSale(sheetId, it) }
    customers.value.forEach { c ->
      val ledger = getCustomerFullLedgerSummary(c)
      googleSheetsService.saveCustomerLedgerSummary(sheetId, ledger)
    }
    auditLogs.value.take(25).forEach { googleSheetsService.saveAuditLog(sheetId, it) }
    updateDailySummaryInSheets(sheetId)

    val pending = storageService.getPendingSyncRecords()
    if (pending.isNotEmpty()) {
      googleSheetsService.syncPendingTransactions(sheetId, pending)
    }
    val pendingDrive = storageService.getPendingDriveUploads()
    if (pendingDrive.isNotEmpty()) {
      googleDriveService?.syncPendingDriveUploads(pendingDrive)
    }

    val updatedState =
      storageService.getGoogleAccountStateOnce().copy(
        syncStatus = SyncStatus.SYNCED,
        lastSyncTimestamp = System.currentTimeMillis(),
        initializedWorksheets = WorksheetSchemas.REQUIRED_WORKSHEETS,
      )
    storageService.saveGoogleAccountState(updatedState)
  }

  private suspend fun performFullCloudSync(silent: Boolean) {
    val state = storageService.getGoogleAccountStateOnce()
    if (state.isOfflineMode) {
      if (!silent) {
        _statusBannerMessage.value =
          "Sync pending. Your transaction is safely stored on this device."
      }
      return
    }
    if (!state.isConnected) {
      if (!silent) {
        _statusBannerMessage.value = "Please connect your Google Account first."
      }
      return
    }
    val sheetId = state.selectedSpreadsheetId
    if (sheetId.isBlank() || googleSheetsService == null) {
      if (!silent) {
        _statusBannerMessage.value = "Please select or create a Google Sheets database."
      }
      return
    }

    storageService.saveGoogleAccountState(state.copy(syncStatus = SyncStatus.SYNCING))
    googleDriveService?.ensureDriveFolders()

    val initRes = googleSheetsService.initializeSheets(sheetId)
    if (initRes.isFailure) {
      storageService.saveGoogleAccountState(state.copy(syncStatus = SyncStatus.SYNC_FAILED))
      if (!silent) {
        _statusBannerMessage.value = "Unable to synchronize. Please try again."
      }
      return
    }

    val pending = storageService.getPendingSyncRecords()
    if (pending.isNotEmpty()) {
      val syncQueueRes = googleSheetsService.syncPendingTransactions(sheetId, pending)
      if (syncQueueRes.isFailure) {
        storageService.saveGoogleAccountState(state.copy(syncStatus = SyncStatus.SYNC_FAILED))
        if (!silent) {
          _statusBannerMessage.value =
            "Sync pending. Your transaction is safely stored on this device."
        }
        return
      }
    }

    val pendingDrive = storageService.getPendingDriveUploads()
    if (pendingDrive.isNotEmpty() && googleDriveService != null) {
      googleDriveService.syncPendingDriveUploads(pendingDrive)
    }

    val remoteCustomers = googleSheetsService.getCustomers(sheetId).getOrDefault(emptyList())
    if (remoteCustomers.isEmpty() && customers.value.isNotEmpty()) {
      pushAllLocalDataToGoogleSheets()
    } else {
      if (remoteCustomers.isNotEmpty()) {
        val localPendingMap = customers.value.associate { it.id to it.pendingAmount }
        val mergedCustomers =
          remoteCustomers.map { rc ->
            rc.copy(pendingAmount = localPendingMap[rc.id] ?: rc.pendingAmount)
          }
        storageService.saveCustomersBatch(mergedCustomers)
      }
      googleSheetsService.getBusinessProfile(sheetId).getOrNull()?.let { remoteProfile ->
        val cur = businessProfile.value
        if (cur.updatedAt >= remoteProfile.updatedAt) {
          googleSheetsService.saveBusinessProfile(sheetId, cur)
        } else {
          storageService.saveBusinessProfile(
            remoteProfile.copy(
              language = cur.language,
              weightDecimalPlaces = cur.weightDecimalPlaces,
              tunchDecimalPlaces = cur.tunchDecimalPlaces,
              moneyDecimalPlaces = cur.moneyDecimalPlaces,
              purityDivisor = cur.purityDivisor,
              maxTunchValue = cur.maxTunchValue,
              allowEditingPure99Purity = cur.allowEditingPure99Purity,
              enabledDeduction = cur.enabledDeduction,
              enabledDeductionTypes = cur.enabledDeductionTypes,
              logoPosition = cur.logoPosition,
              showBankDetailsOnInvoice = cur.showBankDetailsOnInvoice,
              showUpiOnInvoice = cur.showUpiOnInvoice,
              showPanOnInvoice = cur.showPanOnInvoice,
              showGstOnInvoice = cur.showGstOnInvoice,
              showCustomerPanOnInvoice = cur.showCustomerPanOnInvoice,
              showCustomerGstOnInvoice = cur.showCustomerGstOnInvoice,
              showCustomerBalanceOnInvoice = cur.showCustomerBalanceOnInvoice,
            )
          )
        }
      }
      googleSheetsService.getRates(sheetId).getOrNull()?.let { remoteRate ->
        storageService.saveMetalRate(remoteRate)
      }
      val remoteTxs = googleSheetsService.getTransactions(sheetId).getOrDefault(emptyList())
      if (remoteTxs.isNotEmpty()) {
        val localTxMap = transactions.value.associateBy { it.transactionId }
        val mergedTxs =
          remoteTxs.map { rtx ->
            localTxMap[rtx.transactionId]?.copy(syncStatus = SyncStatus.SYNCED) ?: rtx
          }
        storageService.saveTransactionsBatch(mergedTxs)
      }
      val remoteInvs = googleSheetsService.getInvoices(sheetId).getOrDefault(emptyList())
      if (remoteInvs.isNotEmpty()) {
        val localInvMap = invoices.value.associateBy { it.invoiceNumber }
        val mergedInvs = remoteInvs.map { rinv -> localInvMap[rinv.invoiceNumber] ?: rinv }
        storageService.saveInvoicesBatch(mergedInvs)
      }
      val remoteOpenings = googleSheetsService.getOpeningStocks(sheetId).getOrDefault(emptyList())
      if (remoteOpenings.isNotEmpty()) {
        storageService.saveOpeningStocksBatch(remoteOpenings)
      } else {
        openingStocks.value.forEach { googleSheetsService.saveOpeningStock(sheetId, it) }
      }
      val remoteMovements = googleSheetsService.getInventoryMovements(sheetId).getOrDefault(emptyList())
      if (remoteMovements.isNotEmpty()) {
        val localMovMap = inventoryMovements.value.associateBy { it.movementId }
        val mergedMovs = (remoteMovements.map { localMovMap[it.movementId] ?: it } + inventoryMovements.value).distinctBy { it.movementId }
        storageService.saveInventoryMovementsBatch(mergedMovs)
      } else {
        inventoryMovements.value.forEach { googleSheetsService.saveInventoryMovement(sheetId, it) }
      }
      val remoteVendors = googleSheetsService.getVendors(sheetId).getOrDefault(emptyList())
      if (remoteVendors.isNotEmpty()) {
        val localVendMap = vendors.value.associateBy { it.vendorId }
        val mergedVendors = (remoteVendors.map { localVendMap[it.vendorId] ?: it } + vendors.value).distinctBy { it.vendorId }
        storageService.saveVendorsBatch(mergedVendors)
      } else {
        vendors.value.forEach { googleSheetsService.saveVendor(sheetId, it) }
      }
      val remotePurchases = googleSheetsService.getPurchases(sheetId).getOrDefault(emptyList())
      if (remotePurchases.isNotEmpty()) {
        val localPurMap = purchases.value.associateBy { it.purchaseId }
        val mergedPurchases = (remotePurchases.map { localPurMap[it.purchaseId] ?: it } + purchases.value).distinctBy { it.purchaseId }
        storageService.savePurchasesBatch(mergedPurchases)
      } else {
        purchases.value.forEach { googleSheetsService.savePurchase(sheetId, it) }
      }
      val remoteSales = googleSheetsService.getSales(sheetId).getOrDefault(emptyList())
      if (remoteSales.isNotEmpty()) {
        val localSaleMap = sales.value.associateBy { it.saleId }
        val mergedSales = (remoteSales.map { localSaleMap[it.saleId] ?: it } + sales.value).distinctBy { it.saleId }
        storageService.saveSalesBatch(mergedSales)
      } else {
        sales.value.forEach { googleSheetsService.saveSale(sheetId, it) }
      }
    }

    updateDailySummaryInSheets(sheetId)
    refreshNextInvoiceNumber()

    val finalQueue = storageService.getPendingSyncRecords()
    val finalDriveQueue = storageService.getPendingDriveUploads()
    val finalStatus =
      if (finalQueue.isEmpty() && finalDriveQueue.isEmpty()) SyncStatus.SYNCED
      else SyncStatus.PENDING_SYNC
    storageService.saveGoogleAccountState(
      storageService.getGoogleAccountStateOnce().copy(
        syncStatus = finalStatus,
        lastSyncTimestamp = System.currentTimeMillis(),
        initializedWorksheets = WorksheetSchemas.REQUIRED_WORKSHEETS,
      )
    )

    if (!silent) {
      _statusBannerMessage.value = "Google Sheets & Drive synchronized."
    }
  }

  private suspend fun updateDailySummaryInSheets(spreadsheetId: String) {
    val today = getTodayDateString()
    val allTxs = storageService.transactionsFlow.first()
    val todayTxs =
      allTxs.filter {
        it.date == today &&
          it.status != TransactionStatus.CANCELLED &&
          it.status != TransactionStatus.DRAFT
      }

    val goldAmt =
      todayTxs
        .filter { it.metalType == MetalType.GOLD }
        .fold(BigDecimal.ZERO) { a, b -> a.add(b.amount) }
    val silverAmt =
      todayTxs
        .filter { it.metalType == MetalType.SILVER }
        .fold(BigDecimal.ZERO) { a, b -> a.add(b.amount) }
    val goldWt =
      todayTxs
        .filter { it.metalType == MetalType.GOLD }
        .fold(BigDecimal.ZERO) { a, b -> a.add(b.fineWeight) }
    val silverWt =
      todayTxs
        .filter { it.metalType == MetalType.SILVER }
        .fold(BigDecimal.ZERO) { a, b -> a.add(b.fineWeight) }
    val goldRec =
      todayTxs
        .filter { it.transactionType == TransactionType.GOLD_PAYMENT }
        .fold(BigDecimal.ZERO) { a, b -> a.add(b.fineWeight) }
    val silverRec =
      todayTxs
        .filter { it.transactionType == TransactionType.SILVER_PAYMENT }
        .fold(BigDecimal.ZERO) { a, b -> a.add(b.fineWeight) }
    val scrapGold =
      todayTxs
        .filter { it.transactionType == TransactionType.SCRAP_GOLD }
        .fold(BigDecimal.ZERO) { a, b -> a.add(b.fineWeight) }
    val scrapSilver =
      todayTxs
        .filter { it.transactionType == TransactionType.SCRAP_SILVER }
        .fold(BigDecimal.ZERO) { a, b -> a.add(b.fineWeight) }
    val totalAmt = todayTxs.fold(BigDecimal.ZERO) { a, b -> a.add(b.amount) }

    googleSheetsService?.saveDailySummary(
      spreadsheetId,
      DailySummaryRecord(
        date = today,
        goldAmount = goldAmt,
        silverAmount = silverAmt,
        goldWeight = goldWt,
        silverWeight = silverWt,
        goldReceived = goldRec,
        silverReceived = silverRec,
        scrapGold = scrapGold,
        scrapSilver = scrapSilver,
        totalTransactions = todayTxs.size,
        totalAmount = totalAmt,
      ),
    )
  }

  fun searchCustomerByMobileNumber(mobileQuery: String) {
    val clean = mobileQuery.trim()
    if (clean.isEmpty()) {
      _mobileSearchResult.value = null
      return
    }
    viewModelScope.launch {
      val localMatch = storageService.findCustomerByMobileLocal(clean)
      if (localMatch != null) {
        _mobileSearchResult.value =
          MobileCustomerSearchResult(
            queryMobile = clean,
            customer = localMatch,
            source = "Local Cache",
          )
        return@launch
      }

      val acct = storageService.getGoogleAccountStateOnce()
      if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode && googleSheetsService != null) {
        val sheetMatch =
          googleSheetsService
            .searchCustomerByMobile(acct.selectedSpreadsheetId, clean)
            .getOrNull()
        if (sheetMatch != null) {
          storageService.saveCustomer(sheetMatch)
          _mobileSearchResult.value =
            MobileCustomerSearchResult(
              queryMobile = clean,
              customer = sheetMatch,
              source = "Google Sheets (Customers)",
            )
          return@launch
        }
      }

      _mobileSearchResult.value =
        MobileCustomerSearchResult(
          queryMobile = clean,
          customer = null,
          source = "Not Found",
        )
    }
  }

  fun clearMobileCustomerSearch() {
    _mobileSearchResult.value = null
  }

  suspend fun getWorksheetRowsForInspection(worksheetName: String): List<List<String>> {
    val sheetId = storageService.getGoogleAccountStateOnce().selectedSpreadsheetId
    if (sheetId.isBlank() || googleSheetsService == null) return emptyList()
    return googleSheetsService.getWorksheetRows(sheetId, worksheetName)
  }

  // ==================== STAGE 4: INVOICE SYSTEM, PDF, PRINT, SHARE & DRIVE ====================

  fun setInvoicePrintFormat(format: InvoicePrintFormat) {
    _invoicePrintFormat.value = format
  }

  fun setInvoiceTypeFilter(filter: InvoiceTypeFilter) {
    _invoiceTypeFilter.value = filter
  }

  fun setInvoiceDateFilter(filter: ReportDateFilter) {
    _invoiceDateFilter.value = filter
  }

  fun setInvoiceCustomDateRange(startDate: String, endDate: String) {
    _invoiceCustomStartDate.value = startDate
    _invoiceCustomEndDate.value = endDate
    _invoiceDateFilter.value = ReportDateFilter.CUSTOM_RANGE
  }

  fun setInvoiceSearchQuery(query: String) {
    _invoiceSearchQuery.value = query
  }

  fun filterInvoices(
    allInvoices: List<Invoice>,
    searchQuery: String = _invoiceSearchQuery.value,
    typeFilter: InvoiceTypeFilter = _invoiceTypeFilter.value,
    dateFilter: ReportDateFilter = _invoiceDateFilter.value,
  ): List<Invoice> {
    val today = getTodayDateString()
    val yesterday = getDaysAgoDateString(1)
    val weekStart = getDaysAgoDateString(6)
    val monthPrefix = today.take(7)

    val byDate =
      when (dateFilter) {
        ReportDateFilter.TODAY -> allInvoices.filter { it.date == today }
        ReportDateFilter.YESTERDAY -> allInvoices.filter { it.date == yesterday }
        ReportDateFilter.THIS_WEEK -> allInvoices.filter { it.date >= weekStart && it.date <= today }
        ReportDateFilter.THIS_MONTH -> allInvoices.filter { it.date.startsWith(monthPrefix) }
        ReportDateFilter.ALL_TIME -> allInvoices
        ReportDateFilter.CUSTOM_RANGE -> {
          val s = _invoiceCustomStartDate.value
          val e = _invoiceCustomEndDate.value
          allInvoices.filter { it.date >= s && it.date <= e }
        }
      }

    val byType =
      when (typeFilter) {
        InvoiceTypeFilter.ALL -> byDate
        InvoiceTypeFilter.GOLD -> byDate.filter { it.metalType == MetalType.GOLD }
        InvoiceTypeFilter.SILVER -> byDate.filter { it.metalType == MetalType.SILVER }
        InvoiceTypeFilter.SCRAP ->
          byDate.filter {
            it.transactionType == TransactionType.SCRAP_GOLD ||
              it.transactionType == TransactionType.SCRAP_SILVER
          }
        InvoiceTypeFilter.MONEY_TO_GOLD ->
          byDate.filter { it.transactionType == TransactionType.MONEY_TO_GOLD }
        InvoiceTypeFilter.MONEY_TO_SILVER ->
          byDate.filter { it.transactionType == TransactionType.MONEY_TO_SILVER }
      }

    if (searchQuery.isBlank()) return byType
    val q = searchQuery.trim().lowercase()
    return byType.filter { inv ->
      inv.invoiceNumber.lowercase().contains(q) ||
        inv.customerName.lowercase().contains(q) ||
        inv.customerMobile.lowercase().contains(q) ||
        inv.date.lowercase().contains(q) ||
        inv.transactionType.title.lowercase().contains(q)
    }
  }

  /**
   * Generates a PDF for the invoice, saving it locally as `Invoice_<InvoiceNumber>.pdf`.
   * Never changes the permanent InvoiceNumber when regenerating.
   */
  fun generateInvoicePdf(
    context: Context,
    invoice: Invoice,
    format: InvoicePrintFormat = _invoicePrintFormat.value,
    forceRegenerate: Boolean = false,
    onSuccess: (File, Invoice) -> Unit = { _, _ -> },
    onError: (String) -> Unit = {},
  ) {
    viewModelScope.launch {
      val existingPdf =
        if (invoice.localPdfPath.isNotBlank()) File(invoice.localPdfPath)
        else invoicePdfService.getPdfFileForInvoice(context, invoice.invoiceNumber)

      val pdfFileResult =
        if (!forceRegenerate && existingPdf.exists() && existingPdf.length() > 0L) {
          Result.success(existingPdf)
        } else {
          invoicePdfService.generateInvoicePdf(context, invoice, format)
        }

      if (pdfFileResult.isFailure) {
        val msg = pdfFileResult.exceptionOrNull()?.message ?: "Failed to generate invoice PDF."
        _statusBannerMessage.value = msg
        onError(msg)
        return@launch
      }

      val pdfFile = pdfFileResult.getOrThrow()
      val newStatus =
        when {
          invoice.isCancelled -> InvoiceStatus.CANCELLED
          invoice.isSavedToDrive -> InvoiceStatus.DRIVE_SAVED
          else -> InvoiceStatus.PDF_GENERATED
        }
      val updatedInvoice =
        invoice.copy(
          localPdfPath = pdfFile.absolutePath,
          pdfFileName = "Invoice_${invoice.invoiceNumber}.pdf",
          status = newStatus.name,
          invoiceStatus = newStatus,
          updatedAt = System.currentTimeMillis(),
        )

      storageService.saveInvoice(updatedInvoice)
      if (_selectedInvoiceForPreview.value?.invoiceNumber == updatedInvoice.invoiceNumber) {
        _selectedInvoiceForPreview.value = updatedInvoice
      }

      enqueueInvoiceSyncRecord(updatedInvoice, SyncOperation.UPDATE)
      val acct = storageService.getGoogleAccountStateOnce()
      if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode) {
        googleSheetsService?.saveInvoice(acct.selectedSpreadsheetId, updatedInvoice)
      }

      _statusBannerMessage.value =
        if (forceRegenerate) {
          "Regenerated Invoice_${updatedInvoice.invoiceNumber}.pdf (Invoice No ${updatedInvoice.invoiceNumber} preserved)."
        } else {
          "Generated PDF: Invoice_${updatedInvoice.invoiceNumber}.pdf"
        }
      onSuccess(pdfFile, updatedInvoice)
    }
  }

  /**
   * Saves the invoice PDF to Google Drive inside `Jewellery Business Manager / Invoices`.
   * If offline or Drive upload fails, saves locally and queues a `PendingDriveUpload` record.
   */
  fun saveInvoiceToGoogleDrive(
    context: Context,
    invoice: Invoice,
    format: InvoicePrintFormat = _invoicePrintFormat.value,
    forceRegenerate: Boolean = false,
    onSuccess: (Invoice) -> Unit = {},
    onError: (String) -> Unit = {},
  ) {
    viewModelScope.launch {
      val pdfRes = invoicePdfService.generateInvoicePdf(context, invoice, format)
      if (pdfRes.isFailure) {
        val err = pdfRes.exceptionOrNull()?.message ?: "Could not generate PDF for Google Drive."
        _statusBannerMessage.value = err
        onError(err)
        return@launch
      }
      val pdfFile = pdfRes.getOrThrow()
      val fileName = "Invoice_${invoice.invoiceNumber}.pdf"

      val acct = storageService.getGoogleAccountStateOnce()
      if (!acct.isConnected || acct.isOfflineMode || googleDriveService == null) {
        val pendingStatus =
          if (invoice.isCancelled) InvoiceStatus.CANCELLED else InvoiceStatus.PENDING_UPLOAD
        val localUpdated =
          invoice.copy(
            localPdfPath = pdfFile.absolutePath,
            pdfFileName = fileName,
            status = pendingStatus.name,
            invoiceStatus = pendingStatus,
            updatedAt = System.currentTimeMillis(),
          )
        storageService.saveInvoice(localUpdated)
        if (_selectedInvoiceForPreview.value?.invoiceNumber == localUpdated.invoiceNumber) {
          _selectedInvoiceForPreview.value = localUpdated
        }

        storageService.enqueueDriveUpload(
          PendingDriveUpload(
            uploadId = "DRV-UP-${invoice.invoiceNumber}",
            invoiceId = invoice.invoiceId.ifBlank { "INV-${invoice.transactionId.removePrefix("TXN-")}" },
            invoiceNumber = invoice.invoiceNumber,
            fileName = fileName,
            localFileReference = pdfFile.absolutePath,
            status = DriveUploadStatus.PENDING,
          )
        )
        val pendingMsg = "Invoice saved on device. Google Drive upload is pending."
        _statusBannerMessage.value = pendingMsg
        onSuccess(localUpdated)
        return@launch
      }

      val driveRes =
        googleDriveService.saveInvoicePdfToDrive(
          invoice = invoice.copy(localPdfPath = pdfFile.absolutePath, pdfFileName = fileName),
          pdfFile = pdfFile,
          forceRegenerate = forceRegenerate,
        )

      if (driveRes.isSuccess) {
        val saved = driveRes.getOrThrow()
        val finalStatus =
          if (invoice.isCancelled) InvoiceStatus.CANCELLED else InvoiceStatus.DRIVE_SAVED
        val savedInv =
          invoice.copy(
            localPdfPath = pdfFile.absolutePath,
            pdfFileName = fileName,
            driveFileId = saved.driveFileId,
            driveFileName = saved.driveFileName,
            driveFileUrl = saved.driveFileUrl,
            driveSavedAt = saved.savedAtTimestamp,
            status = finalStatus.name,
            invoiceStatus = finalStatus,
            updatedAt = saved.savedAtTimestamp,
          )
        storageService.saveInvoice(savedInv)
        if (_selectedInvoiceForPreview.value?.invoiceNumber == savedInv.invoiceNumber) {
          _selectedInvoiceForPreview.value = savedInv
        }
        enqueueInvoiceSyncRecord(savedInv, SyncOperation.UPDATE)

        _statusBannerMessage.value =
          if (saved.reusedExistingFile) {
            "$fileName is already saved in Google Drive (Jewellery Business Manager / Invoices)."
          } else {
            "Saved $fileName to Google Drive (Jewellery Business Manager / Invoices)."
          }
        onSuccess(savedInv)
      } else {
        val pendingStatus =
          if (invoice.isCancelled) InvoiceStatus.CANCELLED else InvoiceStatus.PENDING_UPLOAD
        val fallbackInv =
          invoice.copy(
            localPdfPath = pdfFile.absolutePath,
            pdfFileName = fileName,
            status = pendingStatus.name,
            invoiceStatus = pendingStatus,
            updatedAt = System.currentTimeMillis(),
          )
        storageService.saveInvoice(fallbackInv)
        if (_selectedInvoiceForPreview.value?.invoiceNumber == fallbackInv.invoiceNumber) {
          _selectedInvoiceForPreview.value = fallbackInv
        }

        storageService.enqueueDriveUpload(
          PendingDriveUpload(
            uploadId = "DRV-UP-${invoice.invoiceNumber}",
            invoiceId = invoice.invoiceId.ifBlank { "INV-${invoice.transactionId.removePrefix("TXN-")}" },
            invoiceNumber = invoice.invoiceNumber,
            fileName = fileName,
            localFileReference = pdfFile.absolutePath,
            lastError = driveRes.exceptionOrNull()?.message ?: "Drive upload pending",
            status = DriveUploadStatus.PENDING,
          )
        )
        val pendingMsg = "Invoice saved on device. Google Drive upload is pending."
        _statusBannerMessage.value = pendingMsg
        onError(pendingMsg)
      }
    }
  }

  fun syncPendingDriveUploadsNow() {
    viewModelScope.launch {
      val pendingList = storageService.getPendingDriveUploads()
      if (pendingList.isEmpty()) {
        _statusBannerMessage.value = "No pending Google Drive invoice uploads."
        return@launch
      }
      val acct = storageService.getGoogleAccountStateOnce()
      if (!acct.isConnected || acct.isOfflineMode || googleDriveService == null) {
        _statusBannerMessage.value = "Invoice saved on device. Google Drive upload is pending."
        return@launch
      }
      val res = googleDriveService.syncPendingDriveUploads(pendingList)
      if (res.isSuccess) {
        val count = res.getOrDefault(0)
        _selectedInvoiceForPreview.value?.let { currentPreview ->
          storageService.getInvoiceByIdOrNumber(currentPreview.invoiceNumber)?.let { refreshed ->
            _selectedInvoiceForPreview.value = refreshed
          }
        }
        _statusBannerMessage.value = "Uploaded $count pending invoice PDF(s) to Google Drive."
      } else {
        _statusBannerMessage.value =
          res.exceptionOrNull()?.message ?: "Invoice saved on device. Google Drive upload is pending."
      }
    }
  }

  fun printInvoice(
    context: Context,
    invoice: Invoice,
    format: InvoicePrintFormat = _invoicePrintFormat.value,
  ) {
    generateInvoicePdf(
      context = context,
      invoice = invoice,
      format = format,
      forceRegenerate = false,
      onSuccess = { pdfFile, updatedInvoice ->
        val printRes = invoicePdfService.printInvoicePdf(context, updatedInvoice, pdfFile)
        if (printRes.isSuccess) {
          _statusBannerMessage.value = printRes.getOrThrow()
        } else {
          _statusBannerMessage.value =
            "PDF ready at ${pdfFile.name} (${format.displayName}). ${printRes.exceptionOrNull()?.message ?: ""}".trim()
        }
      },
    )
  }

  fun shareInvoice(
    context: Context,
    invoice: Invoice,
    shareAsPdf: Boolean = true,
    format: InvoicePrintFormat = _invoicePrintFormat.value,
  ) {
    if (!shareAsPdf) {
      try {
        val intent = invoicePdfService.createShareInvoiceIntent(context, invoice, null)
        context.startActivity(intent)
      } catch (e: Exception) {
        _statusBannerMessage.value = "Share text ready for Invoice ${invoice.invoiceNumber}."
      }
      return
    }

    generateInvoicePdf(
      context = context,
      invoice = invoice,
      format = format,
      forceRegenerate = false,
      onSuccess = { pdfFile, updatedInvoice ->
        try {
          val intent = invoicePdfService.createShareInvoiceIntent(context, updatedInvoice, pdfFile)
          context.startActivity(intent)
        } catch (e: Exception) {
          _statusBannerMessage.value = "Invoice PDF ready to share: ${pdfFile.name}"
        }
      },
    )
  }

  private suspend fun enqueueInvoiceSyncRecord(invoice: Invoice, operation: SyncOperation) {
    val invPayload =
      JSONObject().apply {
        put("invoiceId", invoice.invoiceId)
        put("invoiceNumber", invoice.invoiceNumber)
        put("transactionId", invoice.transactionId)
        put("date", invoice.date)
        put("time", invoice.time)
        put("timestamp", invoice.timestamp)
        put("shopName", invoice.shopName)
        put("ownerName", invoice.ownerName)
        put("shopMobile", invoice.shopMobile)
        put("shopAddress", invoice.shopAddress)
        put("shopPan", invoice.shopPan)
        put("shopGst", invoice.shopGst)
        put("bankName", invoice.bankName)
        put("bankAccountNumber", invoice.bankAccountNumber)
        put("ifsc", invoice.ifsc)
        put("upiId", invoice.upiId)
        put("customerId", invoice.customerId)
        put("customerName", invoice.customerName)
        put("customerMobile", invoice.customerMobile)
        put("customerAddress", invoice.customerAddress)
        put("customerPan", invoice.customerPan)
        put("customerGst", invoice.customerGst)
        put("transactionType", invoice.transactionType.name)
        put("metalType", invoice.metalType.name)
        put("purityMode", invoice.purityMode.name)
        put("grossWeight", invoice.grossWeight.toPlainString())
        put("tunch", invoice.tunch.toPlainString())
        put("fineWeight", invoice.fineWeight.toPlainString())
        put("rate", invoice.rate.toPlainString())
        put("rateUnit", invoice.rateUnit.name)
        put("amount", invoice.amount.toPlainString())
        put("subtotal", invoice.subtotal.toPlainString())
        put("deductions", invoice.deductions.toPlainString())
        put("taxableAmount", invoice.taxableAmount.toPlainString())
        put("taxAmount", invoice.taxAmount.toPlainString())
        put("discount", invoice.discount.toPlainString())
        put("paymentMode", invoice.paymentMode.name)
        put("status", invoice.invoiceStatus.name)
        put("notes", invoice.notes)
        put("totalAmount", invoice.totalAmount.toPlainString())
        put("driveFileId", invoice.driveFileId)
        put("driveFileName", invoice.driveFileName)
        put("driveSavedAt", invoice.driveSavedAt)
      }.toString()

    storageService.enqueueSyncRecord(
      PendingSyncRecord(
        recordId = "SYNC-INV-${invoice.invoiceId.ifBlank { invoice.invoiceNumber }}",
        recordType = SyncRecordType.INVOICE,
        operation = operation,
        payload = invPayload,
      )
    )
  }

  // ==================== REPORTS & FILTERS ====================

  fun setReportDateFilter(filter: ReportDateFilter) {
    _reportDateFilter.value = filter
  }

  fun setCustomDateRange(startDate: String, endDate: String) {
    _customStartDate.value = startDate
    _customEndDate.value = endDate
    _reportDateFilter.value = ReportDateFilter.CUSTOM_RANGE
  }

  fun setSelectedReportCustomer(customerId: String?) {
    _selectedReportCustomerId.value = customerId
  }

  fun filterTransactionsForReport(allTransactions: List<Transaction>): List<Transaction> {
    val today = getTodayDateString()
    val yesterday = getDaysAgoDateString(1)
    val weekStart = getDaysAgoDateString(6)
    val monthPrefix = today.take(7)

    val dateFiltered =
      when (_reportDateFilter.value) {
        ReportDateFilter.TODAY -> allTransactions.filter { it.date == today }
        ReportDateFilter.YESTERDAY -> allTransactions.filter { it.date == yesterday }
        ReportDateFilter.THIS_WEEK -> allTransactions.filter { it.date >= weekStart && it.date <= today }
        ReportDateFilter.THIS_MONTH -> allTransactions.filter { it.date.startsWith(monthPrefix) }
        ReportDateFilter.ALL_TIME -> allTransactions
        ReportDateFilter.CUSTOM_RANGE -> {
          val start = _customStartDate.value
          val end = _customEndDate.value
          allTransactions.filter { it.date >= start && it.date <= end }
        }
      }

    val customerFilter = _selectedReportCustomerId.value
    return if (customerFilter.isNullOrBlank()) {
      dateFiltered
    } else {
      dateFiltered.filter { it.customerId == customerFilter }
    }
  }

  // ==================== CRUD WITH OFFLINE-FIRST SYNC QUEUE ====================

  fun saveCustomer(
    existingId: String?,
    name: String,
    mobile: String,
    address: String,
    pan: String,
    gst: String,
    pendingAmountStr: String,
    notes: String,
    whatsapp: String = "",
    city: String = "",
    state: String = "",
    pin: String = "",
    email: String = "",
    allowDuplicateMobile: Boolean = false,
    onSuccess: (Customer) -> Unit = {},
    onError: (String) -> Unit = {},
  ) {
    val validation = ValidationService.validateCustomer(name, mobile, whatsapp, pin, pan, gst, email)
    if (validation is ValidationResult.Invalid) {
      onError(validation.message)
      return
    }
    val pendingAmount =
      if (pendingAmountStr.trim().isEmpty()) {
        BigDecimal.ZERO
      } else {
        getCalculationEngine().parseSafeBigDecimal(pendingAmountStr)
      }
    if (pendingAmount == null || pendingAmount < BigDecimal.ZERO) {
      onError("Pending amount cannot be negative.")
      return
    }

    val existingByMobile =
      if (existingId.isNullOrBlank()) {
        customers.value.find { it.mobileNumber.trim() == mobile.trim() }
      } else null

    if (existingByMobile != null && !allowDuplicateMobile) {
      onError(
        "Customer with mobile ${mobile.trim()} already exists (${existingByMobile.name}). Load or edit the existing customer."
      )
      return
    }

    val now = System.currentTimeMillis()
    val effectiveId = existingId?.takeIf { it.isNotBlank() } ?: existingByMobile?.id ?: "CUST-${now.toString().takeLast(6)}"
    val isUpdate = !existingId.isNullOrBlank() || existingByMobile != null
    val customer =
      Customer(
        id = effectiveId,
        name = name.trim(),
        mobileNumber = mobile.trim(),
        whatsappNumber = whatsapp.trim().ifBlank { mobile.trim() },
        address = address.trim(),
        city = city.trim(),
        state = state.trim(),
        pinCode = pin.trim(),
        panNumber = pan.trim().uppercase(),
        gstNumber = gst.trim().uppercase(),
        email = email.trim(),
        pendingAmount = pendingAmount,
        notes = notes.trim(),
        createdAt = existingByMobile?.createdAt ?: now,
        updatedAt = now,
      )

    viewModelScope.launch {
      storageService.saveCustomer(customer)

      val payload =
        JSONObject().apply {
          put("id", customer.id)
          put("name", customer.name)
          put("mobileNumber", customer.mobileNumber)
          put("whatsappNumber", customer.whatsappNumber)
          put("address", customer.address)
          put("city", customer.city)
          put("state", customer.state)
          put("pinCode", customer.pinCode)
          put("panNumber", customer.panNumber)
          put("gstNumber", customer.gstNumber)
          put("email", customer.email)
          put("notes", customer.notes)
          put("createdAt", customer.createdAt)
          put("updatedAt", customer.updatedAt)
        }.toString()

      storageService.enqueueSyncRecord(
        PendingSyncRecord(
          recordId = "SYNC-CUST-${customer.id}",
          recordType = SyncRecordType.CUSTOMER,
          operation = if (isUpdate) SyncOperation.UPDATE else SyncOperation.CREATE,
          payload = payload,
        )
      )

      val acct = storageService.getGoogleAccountStateOnce()
      if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode) {
        googleSheetsService?.saveCustomer(acct.selectedSpreadsheetId, customer)
        performFullCloudSync(silent = true)
        _statusBannerMessage.value = "Customer '${customer.name}' saved and synchronized."
      } else {
        _statusBannerMessage.value = "Customer '${customer.name}' saved locally."
      }
      onSuccess(customer)
    }
  }

  fun deleteCustomer(customerId: String) {
    viewModelScope.launch {
      storageService.deleteCustomer(customerId)
      _statusBannerMessage.value = "Customer deleted."
    }
  }

  fun saveMetalRates(
    goldRateStr: String,
    goldUnit: RateUnit,
    silverRateStr: String,
    silverUnit: RateUnit,
    onSuccess: () -> Unit = {},
    onError: (String) -> Unit = {},
  ) {
    val calc = getCalculationEngine()
    val gold = calc.parseSafeBigDecimal(goldRateStr)
    val silver = calc.parseSafeBigDecimal(silverRateStr)
    val validation = ValidationService.validateMetalRates(gold, silver)
    if (validation is ValidationResult.Invalid) {
      onError(validation.message)
      return
    }

    val now = System.currentTimeMillis()
    val newRate =
      MetalRate(
        rateId = "RATE-${now.toString().takeLast(6)}",
        goldRate = gold!!,
        goldRateUnit = goldUnit,
        silverRate = silver!!,
        silverRateUnit = silverUnit,
        enteredBy = businessProfile.value.ownerName.ifBlank { "Owner" },
        updatedAt = now,
      )
    viewModelScope.launch {
      storageService.saveMetalRate(newRate)

      val payload =
        JSONObject().apply {
          put("rateId", newRate.rateId)
          put("goldRate", newRate.goldRate.toPlainString())
          put("goldRateUnit", newRate.goldRateUnit.name)
          put("silverRate", newRate.silverRate.toPlainString())
          put("silverRateUnit", newRate.silverRateUnit.name)
          put("updatedAt", newRate.updatedAt)
        }.toString()

      storageService.enqueueSyncRecord(
        PendingSyncRecord(
          recordId = "SYNC-${newRate.rateId}",
          recordType = SyncRecordType.METAL_RATE,
          operation = SyncOperation.CREATE,
          payload = payload,
        )
      )

      val acct = storageService.getGoogleAccountStateOnce()
      if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode) {
        performFullCloudSync(silent = true)
        _statusBannerMessage.value = "Gold & Silver rates saved and synchronized to MetalRates sheet."
      } else {
        _statusBannerMessage.value = "Gold & Silver rates updated locally."
      }
      onSuccess()
    }
  }

  fun saveBusinessProfile(
    profile: BusinessProfile,
    onSuccess: () -> Unit = {},
    onError: (String) -> Unit = {},
  ) {
    val validation =
      ValidationService.validateBusinessProfile(
        shopName = profile.shopName,
        ownerName = profile.ownerName,
        mobile = profile.mobileNumber,
        whatsapp = profile.whatsappNumber,
        email = profile.email,
        pinCode = profile.pinCode,
        pan = profile.panNumber,
        gstin = profile.gstNumber,
        ifsc = profile.ifsc,
        invoicePrefix = profile.invoicePrefix,
        startingNumber = profile.invoiceStartingNumber,
      )
    if (validation is ValidationResult.Invalid) {
      onError(validation.message)
      return
    }
    val now = System.currentTimeMillis()
    val cur = businessProfile.value
    val updatedProfile =
      profile.copy(
        businessId = profile.businessId.ifBlank { cur.businessId.ifBlank { "BIZ-001" } },
        whatsappNumber = profile.whatsappNumber.ifBlank { profile.mobileNumber },
        panNumber = profile.panNumber.trim().uppercase(),
        gstNumber = profile.gstNumber.trim().uppercase(),
        ifsc = profile.ifsc.trim().uppercase(),
        createdAt = cur.createdAt.takeIf { it > 0L } ?: profile.createdAt.takeIf { it > 0L } ?: now,
        updatedAt = now,
        updatedBy = profile.ownerName.ifBlank { "Owner" },
        status = profile.status.ifBlank { "ACTIVE" },
      )
    viewModelScope.launch {
      storageService.saveBusinessProfile(updatedProfile)
      refreshNextInvoiceNumber()

      val payload =
        JSONObject().apply {
          put("businessId", updatedProfile.businessId)
          put("shopName", updatedProfile.shopName)
          put("ownerName", updatedProfile.ownerName)
          put("mobileNumber", updatedProfile.mobileNumber)
          put("whatsappNumber", updatedProfile.whatsappNumber)
          put("email", updatedProfile.email)
          put("address", updatedProfile.address)
          put("city", updatedProfile.city)
          put("district", updatedProfile.district)
          put("state", updatedProfile.state)
          put("pinCode", updatedProfile.pinCode)
          put("panNumber", updatedProfile.panNumber)
          put("gstNumber", updatedProfile.gstNumber)
          put("bankName", updatedProfile.bankName)
          put("branchName", updatedProfile.branchName)
          put("bankAccountNumber", updatedProfile.bankAccountNumber)
          put("ifsc", updatedProfile.ifsc)
          put("upiId", updatedProfile.upiId)
          put("invoicePrefix", updatedProfile.invoicePrefix)
          put("invoiceStartingNumber", updatedProfile.invoiceStartingNumber)
          put("businessLogoUri", updatedProfile.businessLogoUri)
          put("invoiceFooter", updatedProfile.invoiceFooter)
          put("termsAndConditions", updatedProfile.termsAndConditions)
          put("gstEnabled", updatedProfile.gstEnabled)
          put("gstRegistrationType", updatedProfile.gstRegistrationType)
          put("gstStateName", updatedProfile.gstStateName)
          put("gstTaxTreatment", updatedProfile.gstTaxTreatment)
          put("createdAt", updatedProfile.createdAt)
          put("updatedAt", updatedProfile.updatedAt)
          put("updatedBy", updatedProfile.updatedBy)
          put("status", updatedProfile.status)
        }.toString()

      storageService.enqueueSyncRecord(
        PendingSyncRecord(
          recordId = "SYNC-BIZ-${updatedProfile.businessId}",
          recordType = SyncRecordType.BUSINESS_PROFILE,
          operation = SyncOperation.UPDATE,
          payload = payload,
        )
      )

      val acct = storageService.getGoogleAccountStateOnce()
      if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode) {
        googleSheetsService?.saveBusinessProfile(acct.selectedSpreadsheetId, updatedProfile)
        performFullCloudSync(silent = true)
      } else if (acct.selectedSpreadsheetId.isNotBlank() && !acct.isOfflineMode) {
        googleSheetsService?.saveBusinessProfile(acct.selectedSpreadsheetId, updatedProfile)
      }
      _statusBannerMessage.value =
        if (updatedProfile.language == AppLanguage.HINDI) {
          "व्यवसाय प्रोफ़ाइल सफलतापूर्वक अपडेट हो गई।"
        } else {
          "Business profile updated successfully."
        }
      onSuccess()
    }
  }

  fun switchLanguage(language: AppLanguage) {
    val updated = businessProfile.value.copy(language = language)
    viewModelScope.launch {
      storageService.saveBusinessProfile(updated)
      _statusBannerMessage.value = "Language switched to ${language.displayName}."
    }
  }

  // ==================== STAGE 3 & 4: TRANSACTION & INVOICE CREATION ====================

  fun submitTransaction(
    transactionType: TransactionType,
    customer: Customer?,
    purityMode: PuritySelectionMode,
    grossWeightInput: String,
    tunchInput: String,
    rateInput: String,
    rateUnit: RateUnit,
    moneyAmountInput: String,
    paymentMode: PaymentMode,
    invoiceNumberInput: String,
    notesInput: String,
    addPendingInput: String = "",
    deductionType: DeductionType = DeductionType.NONE,
    deductionValueInput: String = "",
    cashPaidInput: String = "",
    cashReceivedInput: String = "",
    multiItems: List<TransactionItem> = emptyList(),
    saveAsDraft: Boolean = false,
    existingDraftId: String? = _editingDraftTransaction.value?.transactionId,
    onError: (String) -> Unit,
  ) {
    if (customer == null) {
      onError("Please select an existing customer or create a new customer.")
      return
    }
    val calc = getCalculationEngine()
    val maxTunch = businessProfile.value.maxTunchValue
    val parsedGross = calc.parseSafeBigDecimal(grossWeightInput)
    val parsedTunch =
      if (
        (transactionType == TransactionType.GOLD_PAYMENT ||
          transactionType == TransactionType.SILVER_PAYMENT) &&
          purityMode == PuritySelectionMode.PURE_99 &&
          tunchInput.isBlank()
      ) {
        CalculationEngine.PURE_99_TUNCH
      } else {
        calc.parseSafeBigDecimal(tunchInput)
      }
    val parsedRate = calc.parseSafeBigDecimal(rateInput)
    val parsedMoney = calc.parseSafeBigDecimal(moneyAmountInput)
    val parsedDeductionVal =
      if (deductionValueInput.isBlank()) BigDecimal.ZERO
      else calc.parseSafeBigDecimal(deductionValueInput) ?: BigDecimal("-1")
    val parsedCashPaid =
      if (cashPaidInput.isBlank()) BigDecimal.ZERO
      else calc.parseSafeBigDecimal(cashPaidInput) ?: BigDecimal.ZERO
    val parsedCashReceived =
      if (cashReceivedInput.isBlank()) BigDecimal.ZERO
      else calc.parseSafeBigDecimal(cashReceivedInput) ?: BigDecimal.ZERO
    val parsedPendingAdd =
      if (addPendingInput.isBlank()) BigDecimal.ZERO
      else calc.parseSafeBigDecimal(addPendingInput) ?: BigDecimal("-1")

    if (parsedPendingAdd < BigDecimal.ZERO) {
      onError("Pending amount cannot be negative.")
      return
    }

    val cleanInvoiceNumber = invoiceNumberInput.trim()
    val existingDraft =
      existingDraftId?.let { id -> transactions.value.find { it.transactionId == id } }
    if (existingDraft != null && existingDraft.status != TransactionStatus.DRAFT) {
      onError("Finalized transactions cannot be edited directly. Create an adjustment or cancel the transaction.")
      return
    }

    if (
      !saveAsDraft &&
        invoices.value.any {
          it.invoiceNumber.equals(cleanInvoiceNumber, ignoreCase = true) &&
            it.transactionId != existingDraftId
        }
    ) {
      onError("Invoice number '$cleanInvoiceNumber' already exists. Please use a unique invoice number.")
      return
    }

    val finalGrossWeight: BigDecimal
    val finalTunch: BigDecimal
    val finalPurityLabel: String
    val finalFineWeight: BigDecimal
    val finalRate: BigDecimal
    val finalMetalValue: BigDecimal
    val finalDeductionAmount: BigDecimal
    val finalNetValue: BigDecimal
    val finalCashAmount: BigDecimal
    val finalRemainingBalance: BigDecimal
    val finalItems: List<TransactionItem>

    if (multiItems.size > 1) {
      val multiResult =
        calc.calculateMultipleItemsTotals(
          items = multiItems,
          deductionType = deductionType,
          deductionValue = parsedDeductionVal.coerceAtLeast(BigDecimal.ZERO),
          defaultRate = parsedRate ?: BigDecimal.ZERO,
          defaultRateUnit = rateUnit,
        )
      if (multiResult.totalGrossWeight <= BigDecimal.ZERO) {
        onError("Total gross weight across items must be greater than zero.")
        return
      }
      if (deductionType != DeductionType.NONE && multiResult.deductionAmount > multiResult.totalMetalValue) {
        onError("Deduction cannot exceed the total metal value.")
        return
      }
      finalGrossWeight = multiResult.totalGrossWeight
      finalTunch = multiResult.weightedAverageTunch
      finalPurityLabel = "${multiResult.weightedAverageTunch.toPlainString()}%"
      finalFineWeight = multiResult.totalFineWeight
      finalRate = parsedRate ?: multiResult.calculatedItems.firstOrNull()?.rate ?: BigDecimal.ZERO
      finalMetalValue = multiResult.totalMetalValue
      finalDeductionAmount = multiResult.deductionAmount
      finalNetValue = multiResult.netValue
      val payAdj =
        calc.calculatePaymentAdjustment(
          metalValue = finalNetValue,
          cashPaid = parsedCashPaid,
          cashReceived = parsedCashReceived,
        )
      finalCashAmount =
        when {
          parsedCashReceived > BigDecimal.ZERO -> parsedCashReceived
          parsedCashPaid > BigDecimal.ZERO -> parsedCashPaid
          else -> finalNetValue
        }
      finalRemainingBalance = payAdj.remainingBalance
      finalItems = multiResult.calculatedItems
    } else {
      when (transactionType) {
        TransactionType.MONEY_TO_GOLD,
        TransactionType.MONEY_TO_SILVER -> {
          val optTunch =
            if (tunchInput.isBlank()) BigDecimal("100.00") else parsedTunch
          val validation =
            ValidationService.validateMoneyToMetalTransaction(
              customerName = customer.name,
              amount = parsedMoney,
              rate = parsedRate,
              invoiceNumber = cleanInvoiceNumber,
              optionalTunch = optTunch,
              maxTunch = maxTunch,
            )
          if (validation is ValidationResult.Invalid) {
            onError(validation.message)
            return
          }
          val conv =
            calc.calculateMoneyToMetal(
              amount = parsedMoney!!,
              rate = parsedRate!!,
              unit = rateUnit,
              optionalTunch = optTunch ?: BigDecimal("100.00"),
            )
          finalGrossWeight = conv.metalQuantityGrams
          finalTunch = conv.tunch
          finalPurityLabel = "${conv.tunch.toPlainString()}%"
          finalFineWeight = conv.fineWeightGrams
          finalRate = parsedRate
          finalMetalValue = parsedMoney
          finalDeductionAmount = BigDecimal.ZERO
          finalNetValue = parsedMoney
          finalCashAmount = parsedMoney
          finalRemainingBalance = BigDecimal.ZERO
          finalItems = emptyList()
        }

        TransactionType.GOLD_PAYMENT,
        TransactionType.SILVER_PAYMENT -> {
          val safeRate = if (rateInput.isBlank()) BigDecimal.ZERO else parsedRate
          val validation =
            ValidationService.validateMetalPaymentTransaction(
              customerName = customer.name,
              grossWeight = parsedGross,
              tunch = parsedTunch,
              rate = safeRate,
              invoiceNumber = cleanInvoiceNumber,
              maxTunch = maxTunch,
            )
          if (validation is ValidationResult.Invalid) {
            onError(validation.message)
            return
          }
          val purityResult = calc.calculateFineWeight(parsedGross!!, parsedTunch!!)
          finalGrossWeight = purityResult.grossWeight
          finalTunch = purityResult.tunch
          finalPurityLabel =
            if (purityMode == PuritySelectionMode.PURE_99) {
              "99% Pure (${purityResult.tunch.toPlainString()}%)"
            } else {
              "${purityResult.tunch.toPlainString()}%"
            }
          finalFineWeight = purityResult.fineWeight
          finalRate = safeRate ?: BigDecimal.ZERO
          finalMetalValue =
            if (finalRate > BigDecimal.ZERO) {
              calc.calculateMetalValue(finalFineWeight, finalRate, rateUnit)
            } else {
              parsedMoney ?: BigDecimal.ZERO
            }
          finalDeductionAmount = BigDecimal.ZERO
          finalNetValue = finalMetalValue
          val payAdj =
            calc.calculatePaymentAdjustment(
              metalValue = finalNetValue,
              cashPaid = parsedCashPaid,
              cashReceived = parsedCashReceived,
            )
          finalCashAmount =
            when {
              parsedCashReceived > BigDecimal.ZERO -> parsedCashReceived
              parsedCashPaid > BigDecimal.ZERO -> parsedCashPaid
              else -> finalNetValue
            }
          finalRemainingBalance = payAdj.remainingBalance
          finalItems = emptyList()
        }

        TransactionType.SCRAP_GOLD,
        TransactionType.SCRAP_SILVER -> {
          val validation =
            ValidationService.validateScrapTransaction(
              customerName = customer.name,
              grossWeight = parsedGross,
              tunch = parsedTunch,
              rate = parsedRate,
              invoiceNumber = cleanInvoiceNumber,
              deductionType = deductionType,
              deductionValue = parsedDeductionVal,
              maxTunch = maxTunch,
            )
          if (validation is ValidationResult.Invalid) {
            onError(validation.message)
            return
          }
          val scrapResult =
            calc.calculateScrapMetal(
              grossWeight = parsedGross!!,
              tunch = parsedTunch!!,
              rate = parsedRate!!,
              unit = rateUnit,
              deductionType = deductionType,
              deductionValue = parsedDeductionVal.coerceAtLeast(BigDecimal.ZERO),
            )
          if (deductionType != DeductionType.NONE && scrapResult.deductionAmount > scrapResult.metalValue) {
            onError("Deduction cannot exceed the calculated metal value.")
            return
          }
          finalGrossWeight = scrapResult.grossWeight
          finalTunch = scrapResult.tunch
          finalPurityLabel = "${scrapResult.tunch.toPlainString()}%"
          finalFineWeight = scrapResult.fineWeight
          finalRate = parsedRate
          finalMetalValue = scrapResult.metalValue
          finalDeductionAmount = scrapResult.deductionAmount
          finalNetValue = scrapResult.netValue
          finalCashAmount = scrapResult.netValue
          finalRemainingBalance = BigDecimal.ZERO
          finalItems = emptyList()
        }

        TransactionType.GOLD_ADJUSTMENT,
        TransactionType.SILVER_ADJUSTMENT -> {
          val safeRate = if (rateInput.isBlank()) BigDecimal.ZERO else parsedRate
          val validation =
            ValidationService.validateAdjustmentTransaction(
              customerName = customer.name,
              grossWeight = parsedGross,
              tunch = parsedTunch,
              rate = safeRate,
              invoiceNumber = cleanInvoiceNumber,
              maxTunch = maxTunch,
            )
          if (validation is ValidationResult.Invalid) {
            onError(validation.message)
            return
          }
          val purityResult = calc.calculateFineWeight(parsedGross!!, parsedTunch!!)
          finalGrossWeight = purityResult.grossWeight
          finalTunch = purityResult.tunch
          finalPurityLabel = "${purityResult.tunch.toPlainString()}%"
          finalFineWeight = purityResult.fineWeight
          finalRate = safeRate ?: BigDecimal.ZERO
          finalMetalValue =
            if (finalRate > BigDecimal.ZERO) {
              calc.calculateMetalValue(finalFineWeight, finalRate, rateUnit)
            } else {
              parsedMoney ?: BigDecimal.ZERO
            }
          finalDeductionAmount = BigDecimal.ZERO
          finalNetValue = finalMetalValue
          finalCashAmount = finalNetValue
          finalRemainingBalance = BigDecimal.ZERO
          finalItems = emptyList()
        }
      }
    }

    val now = System.currentTimeMillis()
    val dateStr = existingDraft?.date ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(now))
    val timeStr = existingDraft?.time ?: SimpleDateFormat("HH:mm", Locale.US).format(Date(now))
    val uniqueTxId = existingDraft?.transactionId ?: "TXN-${now.toString().takeLast(7)}"

    viewModelScope.launch {
      val acct = storageService.getGoogleAccountStateOnce()
      val txStatus =
        when {
          saveAsDraft -> TransactionStatus.DRAFT
          acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode ->
            TransactionStatus.COMPLETED
          else -> TransactionStatus.PENDING_SYNC
        }
      val initialSyncStatus =
        when {
          saveAsDraft -> SyncStatus.LOCAL_ONLY
          acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode ->
            SyncStatus.SYNCING
          else -> SyncStatus.PENDING_SYNC
        }

      val populatedItems =
        if (finalItems.isNotEmpty()) {
          finalItems.mapIndexed { idx, item ->
            item.copy(
              id = item.id.ifBlank { "TXNI-${uniqueTxId.removePrefix("TXN-")}-${idx + 1}" },
              transactionId = uniqueTxId,
            )
          }
        } else {
          listOf(
            TransactionItem(
              id = "TXNI-${uniqueTxId.removePrefix("TXN-")}-1",
              transactionId = uniqueTxId,
              itemName = transactionType.title,
              description = "${transactionType.title} (${customer.name})",
              metalType = transactionType.metalType,
              grossWeight = finalGrossWeight,
              tunch = finalTunch,
              purity = finalPurityLabel,
              fineWeight = finalFineWeight,
              rate = finalRate,
              rateUnit = rateUnit,
              metalValue = finalMetalValue,
              amount = finalNetValue,
            )
          )
        }

      val transaction =
        Transaction(
          transactionId = uniqueTxId,
          date = dateStr,
          time = timeStr,
          timestamp = existingDraft?.timestamp ?: now,
          customerId = customer.id,
          customerName = customer.name,
          customerMobile = customer.mobileNumber,
          transactionType = transactionType,
          metalType = transactionType.metalType,
          purityMode = purityMode,
          grossWeight = finalGrossWeight,
          tunch = finalTunch,
          purity = finalPurityLabel,
          fineWeight = finalFineWeight,
          rate = finalRate,
          rateUnit = rateUnit,
          metalValue = finalMetalValue,
          deductionType = deductionType,
          deductionValue = parsedDeductionVal.coerceAtLeast(BigDecimal.ZERO),
          deductionAmount = finalDeductionAmount,
          netValue = finalNetValue,
          cashAmount = finalCashAmount,
          cashPaid = parsedCashPaid,
          cashReceived = parsedCashReceived,
          remainingBalance = finalRemainingBalance,
          amount = finalNetValue,
          paymentMode = paymentMode,
          notes = notesInput.trim(),
          invoiceNumber = cleanInvoiceNumber,
          status = txStatus,
          syncStatus = initialSyncStatus,
          items = populatedItems,
          updatedAt = now,
        )

      if (saveAsDraft) {
        storageService.saveTransaction(transaction)
        _editingDraftTransaction.value = transaction
        val draftMsg = "Draft transaction saved locally. You can edit it anytime before finalization."
        _savedTransactionSuccess.value =
          SavedTransactionSuccessState(
            transaction = transaction,
            invoice = null,
            statusMessage = draftMsg,
          )
        _statusBannerMessage.value = draftMsg
        return@launch
      }

      // Compute Customer Ledger Summary before and after this completed transaction
      val existingCustomerTxs =
        transactions.value.filter { it.customerId == customer.id && it.transactionId != uniqueTxId }
      val previousLedger =
        calc.calculateCustomerLedger(customer.id, customer.name, existingCustomerTxs)
      val afterLedger =
        calc.calculateCustomerLedger(customer.id, customer.name, existingCustomerTxs + transaction)

      val existingInvoice =
        invoices.value.find {
          it.transactionId == uniqueTxId || it.invoiceNumber == cleanInvoiceNumber
        }

      val invoice =
        invoiceService.createInvoiceFromTransaction(
          transaction = transaction,
          customer = customer,
          businessProfile = businessProfile.value,
          previousLedger = previousLedger,
          afterLedger = afterLedger,
          existingInvoice = existingInvoice,
        )

      if (parsedPendingAdd > BigDecimal.ZERO) {
        val updatedCustomer =
          customer.copy(pendingAmount = customer.pendingAmount.add(parsedPendingAdd))
        storageService.saveCustomer(updatedCustomer)
      }

      // Save finalized transaction and permanent invoice locally immediately
      storageService.saveTransactionAndInvoice(transaction, invoice)
      _editingDraftTransaction.value = null
      refreshNextInvoiceNumber()

      val txPayload =
        JSONObject().apply {
          put("transactionId", transaction.transactionId)
          put("invoiceNumber", transaction.invoiceNumber)
          put("date", transaction.date)
          put("time", transaction.time)
          put("timestamp", transaction.timestamp)
          put("customerId", transaction.customerId)
          put("customerName", transaction.customerName)
          put("customerMobile", transaction.customerMobile)
          put("transactionType", transaction.transactionType.name)
          put("metalType", transaction.metalType.name)
          put("purityMode", transaction.purityMode.name)
          put("grossWeight", transaction.grossWeight.toPlainString())
          put("tunch", transaction.tunch.toPlainString())
          put("fineWeight", transaction.fineWeight.toPlainString())
          put("rate", transaction.rate.toPlainString())
          put("rateUnit", transaction.rateUnit.name)
          put("amount", transaction.amount.toPlainString())
          put("paymentMode", transaction.paymentMode.name)
          put("status", transaction.status.name)
          put("notes", transaction.notes)
        }.toString()

      storageService.enqueueSyncRecord(
        PendingSyncRecord(
          recordId = "SYNC-TX-${transaction.transactionId}",
          recordType = SyncRecordType.TRANSACTION,
          operation = SyncOperation.CREATE,
          payload = txPayload,
        )
      )

      enqueueInvoiceSyncRecord(invoice, SyncOperation.CREATE)

      val saveMsg =
        when {
          acct.isOfflineMode -> "Offline — transaction & invoice ${invoice.invoiceNumber} saved on this device."
          acct.isConnected && acct.hasDatabaseSelected -> "Transaction & invoice ${invoice.invoiceNumber} saved and synchronized."
          else -> "Transaction & invoice ${invoice.invoiceNumber} saved on this device. Sync pending."
        }

      _savedTransactionSuccess.value =
        SavedTransactionSuccessState(
          transaction = transaction,
          invoice = invoice,
          statusMessage = saveMsg,
        )
      _statusBannerMessage.value = saveMsg

      if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode) {
        performFullCloudSync(silent = true)
      }
    }
  }

  /**
   * Cancels / reverses a transaction WITHOUT deleting historical records or invoices.
   */
  fun cancelTransaction(
    transactionId: String,
    cancelledBy: String = businessProfile.value.ownerName.ifBlank { "Owner" },
    cancellationReason: String,
    onSuccess: () -> Unit = {},
    onError: (String) -> Unit = {},
  ) {
    if (cancellationReason.trim().isEmpty()) {
      onError("Please provide a reason for cancelling this transaction.")
      return
    }
    viewModelScope.launch {
      val cancelledTx =
        storageService.cancelTransaction(
          transactionId = transactionId,
          cancelledBy = cancelledBy.trim().ifBlank { "Owner" },
          cancellationReason = cancellationReason.trim(),
        )
      if (cancelledTx == null) {
        onError("Transaction not found.")
        return@launch
      }

      val txPayload =
        JSONObject().apply {
          put("transactionId", cancelledTx.transactionId)
          put("invoiceNumber", cancelledTx.invoiceNumber)
          put("date", cancelledTx.date)
          put("time", cancelledTx.time)
          put("timestamp", cancelledTx.timestamp)
          put("customerId", cancelledTx.customerId)
          put("customerName", cancelledTx.customerName)
          put("customerMobile", cancelledTx.customerMobile)
          put("transactionType", cancelledTx.transactionType.name)
          put("metalType", cancelledTx.metalType.name)
          put("purityMode", cancelledTx.purityMode.name)
          put("grossWeight", cancelledTx.grossWeight.toPlainString())
          put("tunch", cancelledTx.tunch.toPlainString())
          put("fineWeight", cancelledTx.fineWeight.toPlainString())
          put("rate", cancelledTx.rate.toPlainString())
          put("rateUnit", cancelledTx.rateUnit.name)
          put("amount", cancelledTx.amount.toPlainString())
          put("paymentMode", cancelledTx.paymentMode.name)
          put("status", cancelledTx.status.name)
          put(
            "notes",
            "[CANCELLED by ${cancelledTx.cancelledBy}: ${cancelledTx.cancellationReason}] ${cancelledTx.notes}".trim(),
          )
        }.toString()

      storageService.enqueueSyncRecord(
        PendingSyncRecord(
          recordId = "SYNC-CANCEL-${cancelledTx.transactionId}",
          recordType = SyncRecordType.TRANSACTION,
          operation = SyncOperation.UPDATE,
          payload = txPayload,
        )
      )

      storageService.getInvoiceByIdOrNumber(cancelledTx.invoiceNumber)?.let { cancelledInvoice ->
        enqueueInvoiceSyncRecord(cancelledInvoice, SyncOperation.UPDATE)
        if (_selectedInvoiceForPreview.value?.invoiceNumber == cancelledInvoice.invoiceNumber) {
          _selectedInvoiceForPreview.value = cancelledInvoice
        }
      }

      val acct = storageService.getGoogleAccountStateOnce()
      if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode) {
        performFullCloudSync(silent = true)
      }
      _statusBannerMessage.value =
        "Transaction ${cancelledTx.transactionId} and Invoice ${cancelledTx.invoiceNumber} marked CANCELLED. Historical record preserved."
      onSuccess()
    }
  }

  fun dismissSavedTransactionSuccessAndReset() {
    _savedTransactionSuccess.value = null
    _editingDraftTransaction.value = null
    viewModelScope.launch { refreshNextInvoiceNumber() }
  }

  fun deleteTransaction(transactionId: String) {
    val tx = transactions.value.find { it.transactionId == transactionId }
    if (tx != null && tx.status != TransactionStatus.DRAFT) {
      cancelTransaction(
        transactionId = transactionId,
        cancelledBy = businessProfile.value.ownerName.ifBlank { "Owner" },
        cancellationReason = "Cancelled by shop owner",
      )
      return
    }
    viewModelScope.launch {
      storageService.deleteTransaction(transactionId)
      _statusBannerMessage.value = "Draft transaction removed."
    }
  }

  fun deleteInvoice(invoiceNumber: String) {
    viewModelScope.launch {
      storageService.deleteInvoice(invoiceNumber)
      if (_selectedInvoiceForPreview.value?.invoiceNumber == invoiceNumber) {
        _selectedInvoiceForPreview.value = null
      }
      _statusBannerMessage.value = "Invoice $invoiceNumber deleted."
    }
  }

  fun resetDemoData() {
    viewModelScope.launch {
      storageService.resetDemoData()
      refreshNextInvoiceNumber()
      _statusBannerMessage.value = "Sample data restored successfully."
    }
  }

  fun formatShareableInvoice(invoice: Invoice): String =
    invoiceService.formatPrintableInvoiceText(invoice)

  // ==================== STAGE 5: INVENTORY, LEDGERS, VENDORS, PURCHASES, SALES & REPORTS ====================

  fun saveOpeningStock(
    metal: MetalType,
    grossWeightStr: String,
    tunchStr: String,
    rateStr: String,
    notes: String,
    onError: (String) -> Unit = {},
    onSuccess: () -> Unit = {},
  ) {
    val gross = grossWeightStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val tunch = tunchStr.toBigDecimalOrNull() ?: BigDecimal("99.00")
    val rate = rateStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    if (gross < BigDecimal.ZERO) {
      onError("Opening gross weight cannot be negative.")
      return
    }
    if (tunch <= BigDecimal.ZERO || tunch > BigDecimal("100.00")) {
      onError("Opening Tunch/Purity must be between 0.01 and 100.00.")
      return
    }
    val engine = getCalculationEngine()
    val fine = engine.calculateFineWeight(gross, tunch).fineWeight
    val opening =
      OpeningStock(
        metal = metal,
        grossWeight = gross.setScale(3, RoundingMode.HALF_UP),
        tunch = tunch.setScale(2, RoundingMode.HALF_UP),
        fineWeight = fine,
        referenceRatePerGram = rate.setScale(2, RoundingMode.HALF_UP),
        date = getTodayDateString(),
        notes = notes.trim().ifBlank { "Configured Opening ${metal.displayName} Stock" },
        updatedAt = System.currentTimeMillis(),
      )
    viewModelScope.launch {
      storageService.saveOpeningStock(opening)
      storageService.recordAuditLog(
        AuditLogEntry(
          actionId = "AUD-${UUID.randomUUID().toString().take(8).uppercase()}",
          timestamp = System.currentTimeMillis(),
          date = getTodayDateString(),
          time = SimpleDateFormat("hh:mm a", Locale.US).format(Date()),
          actionType = AuditActionType.INVENTORY_ADJUSTED,
          entityId = "OPENING-${metal.name}",
          description = "Updated Opening ${metal.displayName} Stock: ${opening.grossWeight.toPlainString()} g gross (${opening.fineWeight.toPlainString()} g fine)",
          performedBy = businessProfile.value.ownerName.ifBlank { "Owner" },
        )
      )
      val acct = storageService.getGoogleAccountStateOnce()
      if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode) {
        googleSheetsService?.saveOpeningStock(acct.selectedSpreadsheetId, opening)
      }
      _statusBannerMessage.value = "Saved Opening ${metal.displayName} Stock (${opening.fineWeight.toPlainString()} g fine)."
      onSuccess()
    }
  }

  fun recordManualInventoryAdjustment(
    metal: MetalType,
    direction: InventoryMovementDirection,
    grossWeightStr: String,
    tunchStr: String,
    rateStr: String,
    reason: String,
    notes: String,
    onError: (String) -> Unit = {},
    onSuccess: () -> Unit = {},
  ) {
    if (!canPerformProtectedAction("INVENTORY_ADJUSTMENT")) {
      onError("Staff mode is restricted from adjusting inventory. Switch to Owner Mode or unlock PIN.")
      return
    }
    val gross = grossWeightStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val tunch = tunchStr.toBigDecimalOrNull() ?: BigDecimal("99.00")
    val rate =
      rateStr.toBigDecimalOrNull()
        ?: if (metal == MetalType.GOLD) metalRate.value.goldRatePerGram else metalRate.value.silverRatePerGram
    if (gross <= BigDecimal.ZERO) {
      onError("Adjustment weight must be greater than zero.")
      return
    }
    if (securitySettings.value.requireReasonForAdjustment && reason.isBlank()) {
      onError("Reason is required for inventory adjustments.")
      return
    }
    val movement =
      inventoryService.createManualAdjustmentMovement(
        metal = metal,
        direction = direction,
        grossWeight = gross,
        tunch = tunch,
        ratePerGram = rate,
        date = getTodayDateString(),
        reason = reason,
        notes = notes,
      )
    viewModelScope.launch {
      storageService.saveInventoryMovement(movement)
      storageService.recordAuditLog(
        AuditLogEntry(
          actionId = "AUD-${UUID.randomUUID().toString().take(8).uppercase()}",
          timestamp = System.currentTimeMillis(),
          date = getTodayDateString(),
          time = SimpleDateFormat("hh:mm a", Locale.US).format(Date()),
          actionType = AuditActionType.INVENTORY_ADJUSTED,
          entityId = movement.movementId,
          description = "${metal.displayName} ${direction.name} Adjustment: ${movement.grossWeight.toPlainString()} g gross (${movement.fineWeight.toPlainString()} g fine) — $reason",
          performedBy = businessProfile.value.ownerName.ifBlank { "Owner" },
          reason = reason,
        )
      )
      val acct = storageService.getGoogleAccountStateOnce()
      if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode) {
        googleSheetsService?.saveInventoryMovement(acct.selectedSpreadsheetId, movement)
      }
      _statusBannerMessage.value =
        "Recorded ${metal.displayName} ${direction.name} adjustment (${movement.fineWeight.toPlainString()} g fine)."
      onSuccess()
    }
  }

  fun processScrapMetal(
    metal: MetalType,
    scrapGrossWeightStr: String,
    scrapFineWeightStr: String,
    meltedGrossWeightStr: String,
    outputTunchStr: String,
    rateStr: String,
    notes: String,
    onError: (String) -> Unit = {},
    onSuccess: () -> Unit = {},
  ) {
    val scrapGross = scrapGrossWeightStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val scrapFine = scrapFineWeightStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val meltedGross = meltedGrossWeightStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val outputTunch = outputTunchStr.toBigDecimalOrNull() ?: BigDecimal("99.50")
    val rate =
      rateStr.toBigDecimalOrNull()
        ?: if (metal == MetalType.GOLD) metalRate.value.goldRatePerGram else metalRate.value.silverRatePerGram
    if (scrapGross <= BigDecimal.ZERO || meltedGross <= BigDecimal.ZERO) {
      onError("Please enter valid scrap weight and melted output weight.")
      return
    }
    val (record, movements) =
      inventoryService.createScrapProcessingMovements(
        metal = metal,
        scrapGrossWeight = scrapGross,
        scrapFineWeight = scrapFine,
        meltedGrossWeight = meltedGross,
        outputTunch = outputTunch,
        referenceRatePerGram = rate,
        date = getTodayDateString(),
        notes = notes,
      )
    viewModelScope.launch {
      storageService.saveScrapProcess(record)
      storageService.saveInventoryMovementsBatch(movements)
      val acct = storageService.getGoogleAccountStateOnce()
      if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode) {
        movements.forEach { googleSheetsService?.saveInventoryMovement(acct.selectedSpreadsheetId, it) }
      }
      _statusBannerMessage.value =
        "Scrap ${metal.displayName} processed: ${record.outputFineWeight.toPlainString()} g fine recovered (Melting loss: ${record.meltingLossGrossWeight.toPlainString()} g)."
      onSuccess()
    }
  }

  fun reconcileStock(
    metal: MetalType,
    physicalGrossWeightStr: String,
    physicalFineWeightStr: String,
    notes: String,
    createAdjustmentMovement: Boolean,
    onError: (String) -> Unit = {},
    onSuccess: (StockReconciliationRecord) -> Unit = {},
  ) {
    val physGross = physicalGrossWeightStr.toBigDecimalOrNull()
    val physFine = physicalFineWeightStr.toBigDecimalOrNull()
    if (physGross == null || physFine == null || physGross < BigDecimal.ZERO || physFine < BigDecimal.ZERO) {
      onError("Please enter valid physical gross and fine weights.")
      return
    }
    val currentStock =
      if (metal == MetalType.GOLD) goldInventorySummary.value else silverInventorySummary.value
    val rec =
      inventoryService.reconcileStock(
        currentSummary = currentStock,
        physicalGrossWeight = physGross.setScale(3, RoundingMode.HALF_UP),
        physicalFineWeight = physFine.setScale(3, RoundingMode.HALF_UP),
        date = getTodayDateString(),
        notes = notes,
      )
    viewModelScope.launch {
      var finalRec = rec
      if (createAdjustmentMovement && rec.differenceFineWeight.compareTo(BigDecimal.ZERO) != 0) {
        val dir =
          if (rec.differenceFineWeight > BigDecimal.ZERO) InventoryMovementDirection.IN
          else InventoryMovementDirection.OUT
        val diffGross = rec.differenceGrossWeight.abs().max(rec.differenceFineWeight.abs())
        val diffFine = rec.differenceFineWeight.abs()
        val effTunch =
          if (diffGross > BigDecimal.ZERO) {
            diffFine.multiply(BigDecimal("100")).divide(diffGross, 2, RoundingMode.HALF_UP).coerceIn(BigDecimal("0.01"), BigDecimal("100.00"))
          } else BigDecimal("100.00")
        val rate =
          if (metal == MetalType.GOLD) metalRate.value.goldRatePerGram else metalRate.value.silverRatePerGram
        val adjMov =
          InventoryMovement(
            movementId = "MOV-REC-${UUID.randomUUID().toString().take(6).uppercase()}",
            date = getTodayDateString(),
            transactionId = rec.reconciliationId,
            invoiceNumber = "",
            movementType = InventoryMovementType.ADJUSTMENT,
            metal = metal,
            grossWeight = diffGross,
            tunch = effTunch,
            fineWeight = diffFine,
            rate = rate,
            value = diffFine.multiply(rate).setScale(2, RoundingMode.HALF_UP),
            direction = dir,
            notes = "Stock Reconciliation Adjustment: ${notes.ifBlank { rec.reconciliationId }}",
          )
        storageService.saveInventoryMovement(adjMov)
        finalRec = rec.copy(adjustmentMovementId = adjMov.movementId)
        val acct = storageService.getGoogleAccountStateOnce()
        if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode) {
          googleSheetsService?.saveInventoryMovement(acct.selectedSpreadsheetId, adjMov)
        }
      }
      storageService.saveReconciliation(finalRec)
      _statusBannerMessage.value =
        "${metal.displayName} Stock Reconciled. Difference: ${finalRec.differenceFineWeight.toPlainString()} g fine."
      onSuccess(finalRec)
    }
  }

  fun getGoldLedgerEntries(): List<InventoryMovement> =
    inventoryService.getInventoryHistory(
      openingStocks = openingStocks.value,
      movements = inventoryMovements.value,
      metalFilter = MetalType.GOLD,
    )

  fun getSilverLedgerEntries(): List<InventoryMovement> =
    inventoryService.getInventoryHistory(
      openingStocks = openingStocks.value,
      movements = inventoryMovements.value,
      metalFilter = MetalType.SILVER,
    )

  fun openVendorDetail(vendor: Vendor?) {
    _selectedVendorForDetail.value = vendor
    if (vendor != null) {
      _currentTab.value = MainNavTab.VENDORS
    }
  }

  fun saveVendor(
    existingId: String? = null,
    name: String,
    mobile: String,
    companyName: String,
    address: String,
    gstNumber: String,
    panNumber: String,
    pendingMoneyStr: String = "0.00",
    pendingGoldStr: String = "0.000",
    pendingSilverStr: String = "0.000",
    notes: String = "",
    onError: (String) -> Unit = {},
    onSuccess: (Vendor) -> Unit = {},
  ) {
    if (name.isBlank()) {
      onError("Vendor name is required.")
      return
    }
    val vendorId =
      existingId?.takeIf { it.isNotBlank() }
        ?: "VEND-${String.format(Locale.US, "%03d", vendors.value.size + 1)}"
    val vendor =
      Vendor(
        vendorId = vendorId,
        name = name.trim(),
        mobile = mobile.trim(),
        companyName = companyName.trim(),
        address = address.trim(),
        gstNumber = gstNumber.trim().uppercase(),
        panNumber = panNumber.trim().uppercase(),
        pendingMoney = (pendingMoneyStr.toBigDecimalOrNull() ?: BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
        pendingGoldFineGrams = (pendingGoldStr.toBigDecimalOrNull() ?: BigDecimal.ZERO).setScale(3, RoundingMode.HALF_UP),
        pendingSilverFineGrams = (pendingSilverStr.toBigDecimalOrNull() ?: BigDecimal.ZERO).setScale(3, RoundingMode.HALF_UP),
        notes = notes.trim(),
        updatedAt = System.currentTimeMillis(),
      )
    viewModelScope.launch {
      storageService.saveVendor(vendor)
      val acct = storageService.getGoogleAccountStateOnce()
      if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode) {
        googleSheetsService?.saveVendor(acct.selectedSpreadsheetId, vendor)
      }
      if (_selectedVendorForDetail.value?.vendorId == vendor.vendorId) {
        _selectedVendorForDetail.value = vendor
      }
      _statusBannerMessage.value = "Vendor ${vendor.name} saved."
      onSuccess(vendor)
    }
  }

  fun getVendorLedgerSummary(vendor: Vendor): VendorLedgerSummary {
    return inventoryService.calculateVendorLedger(vendor, purchases.value)
  }

  fun savePurchase(
    vendor: Vendor?,
    supplierNameFallback: String,
    supplierMobileFallback: String,
    metal: MetalType,
    description: String,
    grossWeightStr: String,
    tunchStr: String,
    rateStr: String,
    rateUnit: RateUnit,
    gstPercentStr: String,
    amountPaidStr: String,
    paymentMode: PaymentMode,
    notes: String,
    onError: (String) -> Unit = {},
    onSuccess: (PurchaseRecord) -> Unit = {},
  ) {
    val gross = grossWeightStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val tunch = tunchStr.toBigDecimalOrNull() ?: BigDecimal("99.50")
    val rate = rateStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    if (gross <= BigDecimal.ZERO || rate <= BigDecimal.ZERO) {
      onError("Please enter valid gross weight and rate for purchase.")
      return
    }
    val engine = getCalculationEngine()
    val fine = engine.calculateFineWeight(gross, tunch).fineWeight
    val ratePerGram = engine.normalizeRatePerGram(rate, rateUnit)
    val metalValue = engine.calculateMetalValue(fine, ratePerGram)
    val gstPercent = (gstPercentStr.toBigDecimalOrNull() ?: BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
    val gstAmount =
      if (gstPercent > BigDecimal.ZERO) {
        metalValue.multiply(gstPercent).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
      } else BigDecimal.ZERO
    val totalAmount = metalValue.add(gstAmount).setScale(2, RoundingMode.HALF_UP)
    val amountPaid = (amountPaidStr.toBigDecimalOrNull() ?: totalAmount).setScale(2, RoundingMode.HALF_UP)
    val balancePending = totalAmount.subtract(amountPaid).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)

    val purchaseId = "PUR-${String.format(Locale.US, "%04d", purchases.value.size + 1)}"
    val purchase =
      PurchaseRecord(
        purchaseId = purchaseId,
        date = getTodayDateString(),
        time = SimpleDateFormat("hh:mm a", Locale.US).format(Date()),
        timestamp = System.currentTimeMillis(),
        vendorId = vendor?.vendorId ?: "VEND-DIRECT",
        vendorName = vendor?.name ?: supplierNameFallback.ifBlank { "Bullion Supplier" },
        vendorMobile = vendor?.mobile ?: supplierMobileFallback,
        metal = metal,
        description = description.trim().ifBlank { "${metal.displayName} Purchase" },
        grossWeight = gross.setScale(3, RoundingMode.HALF_UP),
        tunch = tunch.setScale(2, RoundingMode.HALF_UP),
        fineWeight = fine,
        rate = rate.setScale(2, RoundingMode.HALF_UP),
        rateUnit = rateUnit,
        gstPercent = gstPercent,
        gstAmount = gstAmount,
        totalAmount = totalAmount,
        amountPaid = amountPaid,
        balancePending = balancePending,
        paymentMode = paymentMode,
        status = TransactionStatus.COMPLETED,
        notes = notes.trim(),
      )
    val movement = inventoryService.createMovementFromPurchase(purchase)
    viewModelScope.launch {
      storageService.savePurchase(purchase, movement)
      if (vendor != null && balancePending > BigDecimal.ZERO) {
        val updatedVendor =
          vendor.copy(
            pendingMoney = vendor.pendingMoney.add(balancePending).setScale(2, RoundingMode.HALF_UP),
            updatedAt = System.currentTimeMillis(),
          )
        storageService.saveVendor(updatedVendor)
      }
      storageService.recordAuditLog(
        AuditLogEntry(
          actionId = "AUD-${UUID.randomUUID().toString().take(8).uppercase()}",
          timestamp = System.currentTimeMillis(),
          date = purchase.date,
          time = purchase.time,
          actionType = AuditActionType.PURCHASE_CREATED,
          entityId = purchase.purchaseId,
          description = "Purchase ${purchase.purchaseId} (${metal.displayName} ${purchase.fineWeight.toPlainString()} g fine) from ${purchase.vendorName}",
          performedBy = businessProfile.value.ownerName.ifBlank { "Owner" },
        )
      )
      val acct = storageService.getGoogleAccountStateOnce()
      if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode) {
        googleSheetsService?.savePurchase(acct.selectedSpreadsheetId, purchase)
        googleSheetsService?.saveInventoryMovement(acct.selectedSpreadsheetId, movement)
      }
      _statusBannerMessage.value =
        "Purchase ${purchase.purchaseId} saved (+${purchase.fineWeight.toPlainString()} g fine ${metal.displayName} added to inventory)."
      onSuccess(purchase)
    }
  }

  fun cancelPurchase(purchaseId: String, reason: String) {
    if (!canPerformProtectedAction("CANCEL_TRANSACTION")) {
      _statusBannerMessage.value = "Staff mode cannot cancel purchases."
      return
    }
    viewModelScope.launch {
      val cancelled = storageService.cancelPurchase(purchaseId, reason.ifBlank { "Cancelled by user" })
      if (cancelled != null) {
        val acct = storageService.getGoogleAccountStateOnce()
        if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode) {
          performFullCloudSync(silent = true)
        }
        _statusBannerMessage.value = "Purchase $purchaseId cancelled and inventory movement reversed."
      }
    }
  }

  fun saveSale(
    customer: Customer?,
    customerNameFallback: String,
    customerMobileFallback: String,
    metal: MetalType,
    description: String,
    grossWeightStr: String,
    tunchStr: String,
    rateStr: String,
    rateUnit: RateUnit,
    makingChargesStr: String,
    gstPercentStr: String,
    isInterStateGst: Boolean,
    amountReceivedStr: String,
    paymentMode: PaymentMode,
    notes: String,
    onError: (String) -> Unit = {},
    onSuccess: (SaleRecord) -> Unit = {},
  ) {
    val gross = grossWeightStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    val tunch = tunchStr.toBigDecimalOrNull() ?: BigDecimal("91.60")
    val rate = rateStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
    if (gross <= BigDecimal.ZERO || rate <= BigDecimal.ZERO) {
      onError("Please enter valid gross weight and rate for sale.")
      return
    }
    val engine = getCalculationEngine()
    val fine = engine.calculateFineWeight(gross, tunch).fineWeight
    val ratePerGram = engine.normalizeRatePerGram(rate, rateUnit)
    val metalValue = engine.calculateMetalValue(fine, ratePerGram)
    val makingCharges = (makingChargesStr.toBigDecimalOrNull() ?: BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
    val gstPercent = (gstPercentStr.toBigDecimalOrNull() ?: BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
    val taxableBase =
      if (businessProfile.value.gstApplyToMakingCharges) metalValue.add(makingCharges) else metalValue
    val totalGst =
      if (gstPercent > BigDecimal.ZERO) {
        taxableBase.multiply(gstPercent).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
      } else BigDecimal.ZERO
    val cgst =
      if (!isInterStateGst && totalGst > BigDecimal.ZERO) totalGst.divide(BigDecimal("2"), 2, RoundingMode.HALF_UP)
      else BigDecimal.ZERO
    val sgst =
      if (!isInterStateGst && totalGst > BigDecimal.ZERO) totalGst.subtract(cgst).setScale(2, RoundingMode.HALF_UP)
      else BigDecimal.ZERO
    val igst = if (isInterStateGst) totalGst else BigDecimal.ZERO

    val totalAmount = metalValue.add(makingCharges).add(totalGst).setScale(2, RoundingMode.HALF_UP)
    val amountReceived = (amountReceivedStr.toBigDecimalOrNull() ?: totalAmount).setScale(2, RoundingMode.HALF_UP)
    val balancePending = totalAmount.subtract(amountReceived).max(BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)

    viewModelScope.launch {
      val invNumber = storageService.getNextInvoiceNumber()
      val saleId = "SALE-${String.format(Locale.US, "%04d", sales.value.size + 1)}"
      val sale =
        SaleRecord(
          saleId = saleId,
          invoiceNumber = invNumber,
          date = getTodayDateString(),
          time = SimpleDateFormat("hh:mm a", Locale.US).format(Date()),
          timestamp = System.currentTimeMillis(),
          customerId = customer?.id ?: "CUST-WALKIN",
          customerName = customer?.name ?: customerNameFallback.ifBlank { "Walk-in Customer" },
          customerMobile = customer?.mobile ?: customerMobileFallback,
          metal = metal,
          description = description.trim().ifBlank { "${metal.displayName} Jewellery Sale" },
          grossWeight = gross.setScale(3, RoundingMode.HALF_UP),
          tunch = tunch.setScale(2, RoundingMode.HALF_UP),
          fineWeight = fine,
          rate = rate.setScale(2, RoundingMode.HALF_UP),
          rateUnit = rateUnit,
          makingCharges = makingCharges,
          gstPercent = gstPercent,
          cgstAmount = cgst,
          sgstAmount = sgst,
          igstAmount = igst,
          totalAmount = totalAmount,
          amountReceived = amountReceived,
          balancePending = balancePending,
          paymentMode = paymentMode,
          status = TransactionStatus.COMPLETED,
          notes = notes.trim(),
        )
      val movement = inventoryService.createMovementFromSale(sale)
      storageService.saveSale(sale, movement)
      if (customer != null && balancePending > BigDecimal.ZERO) {
        val updatedCust =
          customer.copy(
            pendingAmount = customer.pendingAmount.add(balancePending).setScale(2, RoundingMode.HALF_UP)
          )
        storageService.saveCustomer(updatedCust)
      }
      storageService.recordAuditLog(
        AuditLogEntry(
          actionId = "AUD-${UUID.randomUUID().toString().take(8).uppercase()}",
          timestamp = System.currentTimeMillis(),
          date = sale.date,
          time = sale.time,
          actionType = AuditActionType.SALE_CREATED,
          entityId = sale.saleId,
          description = "Sale ${sale.saleId} (${metal.displayName} ${sale.fineWeight.toPlainString()} g fine) to ${sale.customerName}",
          performedBy = businessProfile.value.ownerName.ifBlank { "Owner" },
        )
      )
      val acct = storageService.getGoogleAccountStateOnce()
      if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode) {
        googleSheetsService?.saveSale(acct.selectedSpreadsheetId, sale)
        googleSheetsService?.saveInventoryMovement(acct.selectedSpreadsheetId, movement)
      }
      refreshNextInvoiceNumber()
      _statusBannerMessage.value =
        "Sale ${sale.saleId} saved (-${sale.fineWeight.toPlainString()} g fine ${metal.displayName} deducted from inventory)."
      onSuccess(sale)
    }
  }

  fun cancelSale(saleId: String, reason: String) {
    if (!canPerformProtectedAction("CANCEL_TRANSACTION")) {
      _statusBannerMessage.value = "Staff mode cannot cancel sales."
      return
    }
    viewModelScope.launch {
      val cancelled = storageService.cancelSale(saleId, reason.ifBlank { "Cancelled by user" })
      if (cancelled != null) {
        val acct = storageService.getGoogleAccountStateOnce()
        if (acct.isConnected && acct.hasDatabaseSelected && !acct.isOfflineMode) {
          performFullCloudSync(silent = true)
        }
        _statusBannerMessage.value = "Sale $saleId cancelled and inventory movement reversed."
      }
    }
  }

  fun getCustomerFullLedgerSummary(customer: Customer): CustomerLedgerSummary {
    return inventoryService.calculateCustomerFullLedger(
      customer = customer,
      transactions = transactions.value,
      sales = sales.value,
    )
  }

  fun getCustomerLedgerEntries(customer: Customer): List<CustomerLedgerEntry> {
    return inventoryService.buildCustomerLedgerEntries(
      customer = customer,
      transactions = transactions.value,
      sales = sales.value,
    )
  }

  fun getDailyReportData(date: String = getTodayDateString()): DailyReportData {
    return inventoryService.generateDailyReport(
      date = date,
      transactions = transactions.value,
      purchases = purchases.value,
      sales = sales.value,
      customers = customers.value,
      goldStock = goldInventorySummary.value,
      silverStock = silverInventorySummary.value,
    )
  }

  fun getMonthlyReportData(yearMonth: String = getTodayDateString().take(7)): MonthlyReportData {
    return inventoryService.generateMonthlyReport(
      yearMonth = yearMonth,
      transactions = transactions.value,
      purchases = purchases.value,
      sales = sales.value,
      movements = inventoryMovements.value,
      goldRatePerGram = metalRate.value.goldRatePerGram,
      silverRatePerGram = metalRate.value.silverRatePerGram,
    )
  }

  fun exportReportToCsv(
    context: Context,
    reportType: String,
    onSuccess: (File) -> Unit = {},
    onError: (String) -> Unit = {},
  ) {
    viewModelScope.launch {
      try {
        val dir = File(context.filesDir, "exports").apply { mkdirs() }
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "${reportType}_$timestamp.csv"
        val file = File(dir, fileName)

        val csvBuilder = StringBuilder()
        when (reportType) {
          "Transactions" -> {
            csvBuilder.appendLine("TransactionID,InvoiceNo,Date,Time,CustomerName,Mobile,Type,Metal,GrossWeight,Tunch,FineWeight,Rate,Amount,Status")
            transactions.value.forEach { tx ->
              csvBuilder.appendLine(
                "${tx.transactionId},${tx.invoiceNumber},${tx.date},${tx.time},\"${tx.customerName}\",${tx.customerMobile},${tx.transactionType.name},${tx.metalType.name},${tx.grossWeight.toPlainString()},${tx.tunch.toPlainString()},${tx.fineWeight.toPlainString()},${tx.rate.toPlainString()},${tx.amount.toPlainString()},${tx.status.name}"
              )
            }
          }
          "Invoices" -> {
            csvBuilder.appendLine("InvoiceNumber,TransactionID,Date,Time,CustomerName,Mobile,Type,Metal,FineWeight,GrandTotal,Status")
            invoices.value.forEach { inv ->
              csvBuilder.appendLine(
                "${inv.invoiceNumber},${inv.transactionId},${inv.date},${inv.time},\"${inv.customerName}\",${inv.customerMobile},${inv.transactionType.name},${inv.metalType.name},${inv.totalFineWeight.toPlainString()},${inv.grandTotal.toPlainString()},${inv.status}"
              )
            }
          }
          "InventoryMovements" -> {
            csvBuilder.appendLine("MovementID,Date,TransactionID,InvoiceNumber,MovementType,Metal,GrossWeight,Tunch,FineWeight,Rate,Value,Direction,Notes")
            inventoryMovements.value.forEach { m ->
              csvBuilder.appendLine(
                "${m.movementId},${m.date},${m.transactionId},${m.invoiceNumber},${m.movementType.name},${m.metal.name},${m.grossWeight.toPlainString()},${m.tunch.toPlainString()},${m.fineWeight.toPlainString()},${m.rate.toPlainString()},${m.value.toPlainString()},${m.direction.name},\"${m.notes.replace("\"", "'")}\""
              )
            }
          }
          "CustomerLedger" -> {
            csvBuilder.appendLine("CustomerID,CustomerName,Mobile,MoneyReceived,MoneyGiven,PendingMoney,GoldReceivedFineG,GoldGivenFineG,GoldBalanceFineG,SilverReceivedFineG,SilverGivenFineG,SilverBalanceFineG")
            customers.value.forEach { c ->
              val l = getCustomerFullLedgerSummary(c)
              csvBuilder.appendLine(
                "${c.id},\"${c.name}\",${c.mobile},${l.totalMoneyPaid.toPlainString()},${l.totalTransactionValue.toPlainString()},${l.pendingMoneyBalance.toPlainString()},${l.totalGoldReceivedFineGrams.toPlainString()},${l.totalGoldGivenFineGrams.toPlainString()},${l.netGoldBalanceFineGrams.toPlainString()},${l.totalSilverReceivedFineGrams.toPlainString()},${l.totalSilverGivenFineGrams.toPlainString()},${l.netSilverBalanceFineGrams.toPlainString()}"
              )
            }
          }
          "Purchases" -> {
            csvBuilder.appendLine("PurchaseID,Date,VendorName,Mobile,Metal,GrossWeight,Tunch,FineWeight,Rate,GST,TotalAmount,AmountPaid,BalancePending,Status")
            purchases.value.forEach { p ->
              csvBuilder.appendLine(
                "${p.purchaseId},${p.date},\"${p.vendorName}\",${p.vendorMobile},${p.metal.name},${p.grossWeight.toPlainString()},${p.tunch.toPlainString()},${p.fineWeight.toPlainString()},${p.rate.toPlainString()},${p.gstAmount.toPlainString()},${p.totalAmount.toPlainString()},${p.amountPaid.toPlainString()},${p.balancePending.toPlainString()},${p.status.name}"
              )
            }
          }
          "Sales" -> {
            csvBuilder.appendLine("SaleID,InvoiceNo,Date,CustomerName,Mobile,Metal,GrossWeight,Tunch,FineWeight,Rate,MakingCharges,GST,TotalAmount,AmountReceived,BalancePending,Status")
            sales.value.forEach { s ->
              val gst = s.cgstAmount.add(s.sgstAmount).add(s.igstAmount)
              csvBuilder.appendLine(
                "${s.saleId},${s.invoiceNumber},${s.date},\"${s.customerName}\",${s.customerMobile},${s.metal.name},${s.grossWeight.toPlainString()},${s.tunch.toPlainString()},${s.fineWeight.toPlainString()},${s.rate.toPlainString()},${s.makingCharges.toPlainString()},${gst.toPlainString()},${s.totalAmount.toPlainString()},${s.amountReceived.toPlainString()},${s.balancePending.toPlainString()},${s.status.name}"
              )
            }
          }
          else -> {
            val daily = getDailyReportData()
            csvBuilder.appendLine("Date,TotalTransactions,MoneyReceived,GoldInFineG,GoldOutFineG,SilverInFineG,SilverOutFineG,CurrentGoldFineG,CurrentSilverFineG,CustomerPendingMoney")
            csvBuilder.appendLine(
              "${daily.date},${daily.totalTransactionsCount},${daily.moneyReceived.toPlainString()},${daily.goldReceivedFineGrams.toPlainString()},${daily.goldGivenFineGrams.toPlainString()},${daily.silverReceivedFineGrams.toPlainString()},${daily.silverGivenFineGrams.toPlainString()},${daily.currentGoldStock.currentFineWeight.toPlainString()},${daily.currentSilverStock.currentFineWeight.toPlainString()},${daily.pendingCustomerMoney.toPlainString()}"
            )
          }
        }

        file.writeText(csvBuilder.toString())
        _statusBannerMessage.value = "Exported CSV: ${file.name}"
        onSuccess(file)
      } catch (e: Exception) {
        val msg = e.message ?: "Failed to export CSV report."
        _statusBannerMessage.value = msg
        onError(msg)
      }
    }
  }

  fun shareCsvFile(context: Context, file: File) {
    try {
      val uri =
        FileProvider.getUriForFile(
          context,
          "${context.packageName}.fileprovider",
          file,
        )
      val intent =
        Intent(Intent.ACTION_SEND).apply {
          type = "text/csv"
          putExtra(Intent.EXTRA_STREAM, uri)
          putExtra(Intent.EXTRA_SUBJECT, "Jewellery Report - ${file.name}")
          addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
      context.startActivity(Intent.createChooser(intent, "Share Report CSV").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
      _statusBannerMessage.value = "CSV saved at ${file.absolutePath}"
    }
  }

  fun exportReportToPdf(
    context: Context,
    reportType: String,
    filterSummary: String = "",
    rows: List<Pair<String, String>> = emptyList(),
    detailLines: List<String> = emptyList(),
    saveToDrive: Boolean = false,
    shareAfterExport: Boolean = false,
    onSuccess: (File) -> Unit = {},
    onError: (String) -> Unit = {},
  ) {
    viewModelScope.launch {
      val profile = businessProfile.value
      val calc = getCalculationEngine()
      val effectiveRows =
        if (rows.isNotEmpty()) rows
        else {
          val daily = getDailyReportData()
          listOf(
            "Shop Name" to profile.shopName,
            "Owner Name" to profile.ownerName,
            "GSTIN" to profile.gstNumber.ifBlank { "Not Applicable" },
            "Report Date" to daily.date,
            "Total Transactions" to daily.totalTransactionsCount.toString(),
            "Money Received" to "₹${calc.formatMoney(daily.moneyReceived)}",
            "Gold Received (Fine)" to "${calc.formatWeight(daily.goldReceivedFineGrams)} g",
            "Silver Received (Fine)" to "${calc.formatWeight(daily.silverReceivedFineGrams)} g",
            "Current Gold Stock (Fine)" to "${calc.formatWeight(daily.currentGoldStock.currentFineWeight)} g",
            "Current Silver Stock (Fine)" to "${calc.formatWeight(daily.currentSilverStock.currentFineWeight)} g",
            "Customer Pending (Bakaya)" to "₹${calc.formatMoney(daily.pendingCustomerMoney)}",
          )
        }
      val pdfRes =
        invoicePdfService.generateReportPdf(
          context = context,
          reportTitle = reportType,
          businessProfile = profile,
          filterSummary = filterSummary,
          rows = effectiveRows,
          detailLines = detailLines,
        )
      if (pdfRes.isFailure) {
        val msg = pdfRes.exceptionOrNull()?.message ?: "Failed to generate report PDF."
        _statusBannerMessage.value = msg
        onError(msg)
        return@launch
      }
      val pdfFile = pdfRes.getOrThrow()
      if (saveToDrive && googleDriveService != null) {
        val driveRes = googleDriveService.saveReportPdfToDrive(reportTitle = reportType, pdfFile = pdfFile)
        if (driveRes.isSuccess) {
          val saved = driveRes.getOrThrow()
          _statusBannerMessage.value =
            "Report PDF saved to Google Drive → Jewellery Business Manager → Reports (${saved.driveFileName})"
        } else {
          _statusBannerMessage.value =
            "Report PDF generated locally (${pdfFile.name}). Google Drive save pending."
        }
      } else {
        _statusBannerMessage.value = "Generated Report PDF: ${pdfFile.name}"
      }
      if (shareAfterExport) {
        shareReportPdf(context, pdfFile, reportType)
      }
      onSuccess(pdfFile)
    }
  }

  fun shareReportPdf(context: Context, file: File, reportTitle: String) {
    try {
      val intent =
        invoicePdfService.createShareReportIntent(
          context = context,
          reportTitle = reportTitle,
          summaryText = "Report: $reportTitle (${businessProfile.value.shopName})",
          pdfFile = file,
        )
      context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: Exception) {
      _statusBannerMessage.value = "Report PDF ready at ${file.absolutePath}"
    }
  }

  // ==================== STAGE 6: GLOBAL SEARCH, GST, SECURITY, REMINDERS, BACKUP & HEALTH ====================

  fun updateGlobalSearchQuery(query: String) {
    _globalSearchQuery.value = query
  }

  fun setAppLanguage(language: AppLanguage) {
    val updated = businessProfile.value.copy(language = language)
    saveBusinessProfile(updated)
    _statusBannerMessage.value = "Language updated to ${language.displayName}."
  }

  fun saveGstConfiguration(
    gstEnabled: Boolean,
    gstNumber: String,
    defaultGstRateStr: String,
    cgstRateStr: String,
    sgstRateStr: String,
    igstRateStr: String,
    applyToGold: Boolean,
    applyToSilver: Boolean,
    applyToMakingCharges: Boolean,
    defaultInvoicePaperSize: InvoicePrintFormat,
    fastDashboardLoading: Boolean,
    compactTransactionRows: Boolean,
    gstStateName: String = businessProfile.value.gstStateName,
    gstRegistrationType: String = businessProfile.value.gstRegistrationType,
    gstTaxTreatment: String = businessProfile.value.gstTaxTreatment,
    onError: (String) -> Unit = {},
  ) {
    val cleanGstin = gstNumber.trim().uppercase()
    val gstVal = ValidationService.validateGstinFormat(cleanGstin)
    if (gstVal is ValidationResult.Invalid) {
      _statusBannerMessage.value = gstVal.message
      onError(gstVal.message)
      return
    }
    val cur = businessProfile.value
    val defGst = (defaultGstRateStr.toBigDecimalOrNull() ?: BigDecimal("3.00")).setScale(2, RoundingMode.HALF_UP)
    val cgst = (cgstRateStr.toBigDecimalOrNull() ?: BigDecimal("1.50")).setScale(2, RoundingMode.HALF_UP)
    val sgst = (sgstRateStr.toBigDecimalOrNull() ?: BigDecimal("1.50")).setScale(2, RoundingMode.HALF_UP)
    val igst = (igstRateStr.toBigDecimalOrNull() ?: BigDecimal("3.00")).setScale(2, RoundingMode.HALF_UP)
    val updated =
      cur.copy(
        gstEnabled = gstEnabled,
        gstNumber = cleanGstin,
        defaultGstRatePercent = defGst,
        cgstRatePercent = cgst,
        sgstRatePercent = sgst,
        igstRatePercent = igst,
        gstRegistrationType = gstRegistrationType.trim().ifBlank { "Regular GST Dealer" },
        gstStateName = gstStateName.trim(),
        gstTaxTreatment = gstTaxTreatment.trim().ifBlank { "INTRA_STATE_CGST_SGST" },
        gstApplyToGold = applyToGold,
        gstApplyToSilver = applyToSilver,
        gstApplyToMakingCharges = applyToMakingCharges,
        defaultInvoicePaperSize = defaultInvoicePaperSize,
        fastDashboardLoading = fastDashboardLoading,
        compactTransactionRows = compactTransactionRows,
      )
    saveBusinessProfile(
      profile = updated,
      onSuccess = {
        _invoicePrintFormat.value = defaultInvoicePaperSize
        _statusBannerMessage.value =
          if (cleanGstin.isNotBlank()) {
            ValidationService.GSTIN_LOCAL_VALIDATION_NOTICE
          } else {
            "GST & Printing Configuration saved."
          }
      },
      onError = onError,
    )
  }

  fun removeGstinFromProfile() {
    val cur = businessProfile.value
    val updated =
      cur.copy(
        gstNumber = "",
        gstEnabled = false,
      )
    saveBusinessProfile(
      profile = updated,
      onSuccess = {
        _statusBannerMessage.value = "GSTIN removed and GST disabled."
      },
    )
  }

  fun verifySecurityPin(enteredPin: String): Boolean {
    val settings = securitySettings.value
    if (!settings.pinEnabled || settings.pinCode.isBlank()) {
      _isPinUnlockedInSession.value = true
      return true
    }
    val match = enteredPin.trim() == settings.pinCode.trim()
    if (match) {
      _isPinUnlockedInSession.value = true
      _statusBannerMessage.value = "PIN verified — Owner access unlocked."
    } else {
      _statusBannerMessage.value = "Incorrect PIN."
    }
    return match
  }

  fun lockSecuritySession() {
    _isPinUnlockedInSession.value = false
    _statusBannerMessage.value = "Session locked."
  }

  fun canPerformProtectedAction(actionType: String): Boolean {
    val sec = securitySettings.value
    if (sec.accessMode == UserAccessMode.OWNER) return true
    if (_isPinUnlockedInSession.value) return true
    return when (actionType) {
      "CANCEL_TRANSACTION" -> sec.staffCanCancelTransactions
      "INVENTORY_ADJUSTMENT" -> sec.staffCanAdjustInventory
      "BACKUP_RESTORE" -> sec.staffCanExportBackup
      "EDIT_SETTINGS" -> sec.staffCanEditSettings
      else -> true
    }
  }

  fun saveSecuritySettings(
    pinEnabled: Boolean,
    pinCode: String,
    lockTimeoutMinutes: Int,
    accessMode: UserAccessMode,
    staffCanCancelTransactions: Boolean,
    staffCanAdjustInventory: Boolean,
    staffCanExportBackup: Boolean,
    staffCanEditSettings: Boolean,
    requireConfirmationForCancel: Boolean,
    requireReasonForCancel: Boolean,
    requireReasonForAdjustment: Boolean,
  ) {
    val updated =
      AppSecuritySettings(
        pinEnabled = pinEnabled,
        pinCode = pinCode.trim(),
        lockTimeoutMinutes = lockTimeoutMinutes,
        accessMode = accessMode,
        staffCanCancelTransactions = staffCanCancelTransactions,
        staffCanAdjustInventory = staffCanAdjustInventory,
        staffCanExportBackup = staffCanExportBackup,
        staffCanEditSettings = staffCanEditSettings,
        requireConfirmationForCancel = requireConfirmationForCancel,
        requireReasonForCancel = requireReasonForCancel,
        requireReasonForAdjustment = requireReasonForAdjustment,
      )
    viewModelScope.launch {
      storageService.saveSecuritySettings(updated)
      storageService.recordAuditLog(
        AuditLogEntry(
          actionId = "AUD-${UUID.randomUUID().toString().take(8).uppercase()}",
          timestamp = System.currentTimeMillis(),
          date = getTodayDateString(),
          time = SimpleDateFormat("hh:mm a", Locale.US).format(Date()),
          actionType = AuditActionType.SECURITY_CHANGED,
          entityId = "SECURITY-CONFIG",
          description = "Updated security settings: Mode=${accessMode.displayName}, PIN=${if (pinEnabled) "Enabled" else "Disabled"}",
          performedBy = businessProfile.value.ownerName.ifBlank { "Owner" },
        )
      )
      _statusBannerMessage.value = "Security & Permission settings saved."
    }
  }

  fun addCustomerReminder(
    customerId: String,
    customerName: String,
    customerMobile: String,
    reminderType: ReminderType,
    dueDate: String,
    pendingAmountStr: String,
    pendingMetalGramsStr: String,
    notes: String,
    title: String = "",
    description: String = "",
    vendorId: String = "",
    vendorName: String = "",
    status: ReminderStatus = ReminderStatus.PENDING,
  ) {
    val cleanTitle =
      title.trim().ifBlank {
        when {
          customerName.isNotBlank() -> "${reminderType.displayName} — ${customerName.trim()}"
          vendorName.isNotBlank() -> "${reminderType.displayName} — ${vendorName.trim()}"
          else -> reminderType.displayName
        }
      }
    if (cleanTitle.isBlank() && customerName.isBlank() && vendorName.isBlank()) {
      _statusBannerMessage.value = "Reminder title or customer/vendor name is required."
      return
    }
    val reminder =
      CustomerReminder(
        reminderId = "REM-${UUID.randomUUID().toString().take(8).uppercase()}",
        customerId = customerId,
        customerName = customerName.trim(),
        customerMobile = customerMobile.trim(),
        vendorId = vendorId,
        vendorName = vendorName.trim(),
        title = cleanTitle,
        description = description.trim().ifBlank { notes.trim() },
        reminderType = reminderType,
        dueDate = dueDate.ifBlank { getTodayDateString() },
        pendingAmount = (pendingAmountStr.toBigDecimalOrNull() ?: BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP),
        pendingMetalFineGrams = (pendingMetalGramsStr.toBigDecimalOrNull() ?: BigDecimal.ZERO).setScale(3, RoundingMode.HALF_UP),
        notes = notes.trim().ifBlank { description.trim() },
        isCompleted = status == ReminderStatus.COMPLETED,
        status = status,
      )
    viewModelScope.launch {
      storageService.saveReminder(reminder)
      _statusBannerMessage.value = "Reminder '${reminder.effectiveTitle}' added for ${reminder.dueDate}."
    }
  }

  fun toggleReminderCompleted(reminder: CustomerReminder) {
    val nextStatus =
      if (reminder.isCompleted || reminder.status == ReminderStatus.COMPLETED) {
        ReminderStatus.PENDING
      } else {
        ReminderStatus.COMPLETED
      }
    updateReminderStatus(reminder, nextStatus)
  }

  fun updateReminderStatus(reminder: CustomerReminder, newStatus: ReminderStatus) {
    viewModelScope.launch {
      storageService.saveReminder(
        reminder.copy(
          isCompleted = newStatus == ReminderStatus.COMPLETED,
          status = newStatus,
        )
      )
      _statusBannerMessage.value = "Reminder marked as ${newStatus.displayName}."
    }
  }

  fun deleteReminder(reminderId: String) {
    viewModelScope.launch {
      storageService.deleteReminder(reminderId)
      _statusBannerMessage.value = "Reminder removed."
    }
  }

  fun createLocalJsonBackupFile(context: Context, onSuccess: (File) -> Unit = {}) {
    if (!canPerformProtectedAction("BACKUP_RESTORE")) {
      _statusBannerMessage.value = "Staff mode cannot export backups."
      return
    }
    viewModelScope.launch {
      try {
        val json = storageService.exportFullBackupJson()
        val dir = File(context.filesDir, "backups").apply { mkdirs() }
        val dateStr = getTodayDateString()
        val file = File(dir, "Jewellery_Backup_$dateStr.json")
        file.writeText(json)
        val curSec = securitySettings.value
        storageService.saveSecuritySettings(
          curSec.copy(
            lastLocalBackupTimestamp = System.currentTimeMillis(),
            lastBackupFileName = file.name,
          )
        )
        storageService.recordAuditLog(
          AuditLogEntry(
            actionId = "AUD-${UUID.randomUUID().toString().take(8).uppercase()}",
            timestamp = System.currentTimeMillis(),
            date = dateStr,
            time = SimpleDateFormat("hh:mm a", Locale.US).format(Date()),
            actionType = AuditActionType.BACKUP_CREATED,
            entityId = file.name,
            description = "Created local JSON backup (${file.name})",
            performedBy = businessProfile.value.ownerName.ifBlank { "Owner" },
          )
        )
        refreshHealthStatus()
        _statusBannerMessage.value = "Local backup saved: ${file.name}"
        onSuccess(file)
      } catch (e: Exception) {
        _statusBannerMessage.value = "Backup failed: ${e.message}"
      }
    }
  }

  fun createGoogleDriveBackup(context: Context) {
    if (!canPerformProtectedAction("BACKUP_RESTORE")) {
      _statusBannerMessage.value = "Staff mode cannot export backups."
      return
    }
    viewModelScope.launch {
      try {
        val json = storageService.exportFullBackupJson()
        val dir = File(context.filesDir, "backups").apply { mkdirs() }
        val dateStr = getTodayDateString()
        val file = File(dir, "Jewellery_Backup_$dateStr.json")
        file.writeText(json)

        val curSec = securitySettings.value
        storageService.saveSecuritySettings(
          curSec.copy(
            lastLocalBackupTimestamp = System.currentTimeMillis(),
            lastDriveBackupTimestamp = System.currentTimeMillis(),
            lastBackupFileName = "Jewellery Business Manager / Backups / ${file.name}",
          )
        )
        storageService.recordAuditLog(
          AuditLogEntry(
            actionId = "AUD-${UUID.randomUUID().toString().take(8).uppercase()}",
            timestamp = System.currentTimeMillis(),
            date = dateStr,
            time = SimpleDateFormat("hh:mm a", Locale.US).format(Date()),
            actionType = AuditActionType.BACKUP_CREATED,
            entityId = file.name,
            description = "Backed up database to Google Drive (Jewellery Business Manager / Backups / ${file.name})",
            performedBy = businessProfile.value.ownerName.ifBlank { "Owner" },
          )
        )
        performFullCloudSync(silent = true)
        refreshHealthStatus()
        _statusBannerMessage.value =
          "Backup saved to Google Drive: Jewellery Business Manager / Backups / ${file.name}"
      } catch (e: Exception) {
        _statusBannerMessage.value = "Google Drive backup failed: ${e.message}"
      }
    }
  }

  fun restoreFromGoogleSheetsBackup() {
    if (!canPerformProtectedAction("BACKUP_RESTORE")) {
      _statusBannerMessage.value = "Staff mode cannot restore backups."
      return
    }
    viewModelScope.launch {
      performFullCloudSync(silent = false)
      val integrity = storageService.verifyDataIntegrity()
      storageService.recordAuditLog(
        AuditLogEntry(
          actionId = "AUD-${UUID.randomUUID().toString().take(8).uppercase()}",
          timestamp = System.currentTimeMillis(),
          date = getTodayDateString(),
          time = SimpleDateFormat("hh:mm a", Locale.US).format(Date()),
          actionType = AuditActionType.DATA_RESTORED,
          entityId = "GOOGLE-SHEETS-RESTORE",
          description = "Restored & verified data from Google Sheets (${integrity.first} issues found)",
          performedBy = businessProfile.value.ownerName.ifBlank { "Owner" },
        )
      )
      refreshHealthStatus()
      _statusBannerMessage.value = "Local database restored from Google Sheets & integrity verified."
    }
  }

  fun restoreFromLocalBackupFile(context: Context) {
    if (!canPerformProtectedAction("BACKUP_RESTORE")) {
      _statusBannerMessage.value = "Staff mode cannot restore backups."
      return
    }
    viewModelScope.launch {
      val dir = File(context.filesDir, "backups")
      val latest = dir.listFiles()?.filter { it.extension == "json" }?.maxByOrNull { it.lastModified() }
      if (latest == null || !latest.exists()) {
        _statusBannerMessage.value = "No local backup file found. Create a backup first."
        return@launch
      }
      val res = storageService.restoreFromBackupJson(latest.readText())
      if (res.isSuccess) {
        refreshNextInvoiceNumber()
        refreshHealthStatus()
        _statusBannerMessage.value = "Restored database from ${latest.name} (${res.getOrDefault(0)} records)."
      } else {
        _statusBannerMessage.value = "Failed to restore backup: ${res.exceptionOrNull()?.message}"
      }
    }
  }

  fun refreshHealthStatus() {
    viewModelScope.launch {
      val (issuesCount, issuesList) = storageService.verifyDataIntegrity()
      val acct = storageService.getGoogleAccountStateOnce()
      val sec = securitySettings.value
      _healthStatus.value =
        ApplicationHealthStatus(
          googleAccountConnected = acct.isConnected,
          googleSheetsConnected = acct.isConnected && acct.hasDatabaseSelected,
          googleDriveConnected = acct.isConnected && acct.driveFolderId.isNotBlank(),
          lastSyncTimestamp = acct.lastSyncTimestamp,
          pendingSyncItemsCount = pendingSyncQueue.value.size,
          pendingInvoiceUploadsCount = pendingDriveUploads.value.size,
          lastBackupTimestamp = maxOf(sec.lastLocalBackupTimestamp, sec.lastDriveBackupTimestamp),
          lastBackupFileName = sec.lastBackupFileName,
          appVersion = "7.0.0 (Stage 7 Production Ready)",
          localDatabaseRecordCount =
            customers.value.size +
              vendors.value.size +
              transactions.value.size +
              invoices.value.size +
              inventoryMovements.value.size +
              purchases.value.size +
              sales.value.size,
          integrityIssuesCount = issuesCount,
          integritySummary =
            if (issuesCount == 0) "All records, invoice numbers, and inventory balances verified."
            else issuesList.joinToString("; "),
        )
    }
  }

  companion object {
    fun getTodayDateString(): String =
      SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis()))

    fun getDaysAgoDateString(daysAgo: Int): String {
      val cal = Calendar.getInstance()
      cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
      return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    fun provideFactory(
      storageService: StorageService,
      googleAuthService: GoogleAuthService? = null,
      googleSheetsService: GoogleSheetsService? = null,
      googleDriveService: GoogleDriveService? = googleSheetsService as? GoogleDriveService,
    ): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return JewelleryViewModel(
            storageService = storageService,
            googleAuthService = googleAuthService,
            googleSheetsService = googleSheetsService,
            googleDriveService = googleDriveService,
          ) as T
        }
      }
  }
}
