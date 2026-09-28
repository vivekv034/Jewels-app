package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.automirrored.outlined.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.google.GoogleAuthServiceImpl
import com.example.data.google.GoogleSheetsServiceImpl
import com.example.data.local.JewelleryDatabase
import com.example.data.local.LocalStorageService
import com.example.domain.model.AppLanguage
import com.example.domain.model.SyncStatus
import com.example.ui.i18n.AppStrings
import com.example.ui.screens.BackupAndRecoveryScreen
import com.example.ui.screens.CustomersScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.GlobalSearchScreen
import com.example.ui.screens.InventoryScreen
import com.example.ui.screens.InvoicesScreen
import com.example.ui.screens.MoreMenuScreen
import com.example.ui.screens.NewTransactionScreen
import com.example.ui.screens.PurchasesScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.SalesScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.VendorsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.RoyalGoldShimmer
import com.example.ui.theme.RoyalObsidian
import com.example.ui.viewmodel.JewelleryViewModel
import com.example.ui.viewmodel.MainNavTab
import com.example.ui.viewmodel.SettingsSection

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        val context = LocalContext.current
        val services = remember {
          val db = JewelleryDatabase.getInstance(context)
          val storage = LocalStorageService(db.jewelleryDao())
          val auth = GoogleAuthServiceImpl(context.applicationContext, storage)
          val sheets = GoogleSheetsServiceImpl(context.applicationContext, storage)
          Triple(storage, auth, sheets)
        }
        val viewModel: JewelleryViewModel =
          viewModel(
            factory =
              JewelleryViewModel.provideFactory(
                storageService = services.first,
                googleAuthService = services.second,
                googleSheetsService = services.third,
              )
          )

        JewelleryAppRoot(viewModel = viewModel)
      }
    }
  }
}

private data class NavDestinationItem(
  val tab: MainNavTab,
  val label: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JewelleryAppRoot(
  viewModel: JewelleryViewModel,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val businessProfile by viewModel.businessProfile.collectAsStateWithLifecycle()
  val metalRate by viewModel.metalRate.collectAsStateWithLifecycle()
  val customers by viewModel.customers.collectAsStateWithLifecycle()
  val transactions by viewModel.transactions.collectAsStateWithLifecycle()
  val invoices by viewModel.invoices.collectAsStateWithLifecycle()
  val pendingSyncQueue by viewModel.pendingSyncQueue.collectAsStateWithLifecycle()
  val pendingDriveUploads by viewModel.pendingDriveUploads.collectAsStateWithLifecycle()
  val googleAccountState by viewModel.googleAccountState.collectAsStateWithLifecycle()
  val availableSpreadsheets by viewModel.availableSpreadsheets.collectAsStateWithLifecycle()
  val showGoogleOAuthConsentSheet by viewModel.showGoogleOAuthConsentSheet.collectAsStateWithLifecycle()
  val mobileSearchResult by viewModel.mobileSearchResult.collectAsStateWithLifecycle()

  val currentTab by viewModel.currentTab.collectAsStateWithLifecycle()
  val selectedTxType by viewModel.selectedTransactionType.collectAsStateWithLifecycle()
  val editingDraft by viewModel.editingDraftTransaction.collectAsStateWithLifecycle()
  val savedTxSuccess by viewModel.savedTransactionSuccess.collectAsStateWithLifecycle()
  val selectedInvoice by viewModel.selectedInvoiceForPreview.collectAsStateWithLifecycle()
  val invoicePrintFormat by viewModel.invoicePrintFormat.collectAsStateWithLifecycle()
  val invoiceTypeFilter by viewModel.invoiceTypeFilter.collectAsStateWithLifecycle()
  val invoiceDateFilter by viewModel.invoiceDateFilter.collectAsStateWithLifecycle()
  val invoiceCustomStart by viewModel.invoiceCustomStartDate.collectAsStateWithLifecycle()
  val invoiceCustomEnd by viewModel.invoiceCustomEndDate.collectAsStateWithLifecycle()
  val selectedCustomerForHistory by viewModel.selectedCustomerForHistory.collectAsStateWithLifecycle()
  val activeSettingsSection by viewModel.activeSettingsSection.collectAsStateWithLifecycle()
  val nextInvoiceNumber by viewModel.nextSuggestedInvoiceNumber.collectAsStateWithLifecycle()
  val dashboardSummary by viewModel.dashboardSummary.collectAsStateWithLifecycle()
  val statusBanner by viewModel.statusBannerMessage.collectAsStateWithLifecycle()

  val reportDateFilter by viewModel.reportDateFilter.collectAsStateWithLifecycle()
  val customStartDate by viewModel.customStartDate.collectAsStateWithLifecycle()
  val customEndDate by viewModel.customEndDate.collectAsStateWithLifecycle()
  val selectedReportCustomerId by viewModel.selectedReportCustomerId.collectAsStateWithLifecycle()

  val goldInventorySummary by viewModel.goldInventorySummary.collectAsStateWithLifecycle()
  val silverInventorySummary by viewModel.silverInventorySummary.collectAsStateWithLifecycle()
  val inventoryMovements by viewModel.inventoryMovements.collectAsStateWithLifecycle()
  val purchases by viewModel.purchases.collectAsStateWithLifecycle()
  val sales by viewModel.sales.collectAsStateWithLifecycle()
  val reminders by viewModel.reminders.collectAsStateWithLifecycle()
  val vendors by viewModel.vendors.collectAsStateWithLifecycle()

  val strings = AppStrings.forLanguage(businessProfile.language)
  val calculationEngine =
    remember(businessProfile) { viewModel.getCalculationEngine() }

  val navItems =
    listOf(
      NavDestinationItem(
        tab = MainNavTab.DASHBOARD,
        label = strings.navDashboard,
        selectedIcon = Icons.Filled.Dashboard,
        unselectedIcon = Icons.Outlined.Dashboard,
        testTag = "nav_dashboard",
      ),
      NavDestinationItem(
        tab = MainNavTab.NEW_TRANSACTION,
        label = strings.navNewTransaction,
        selectedIcon = Icons.Filled.AddCircle,
        unselectedIcon = Icons.Outlined.AddCircleOutline,
        testTag = "nav_new_transaction",
      ),
      NavDestinationItem(
        tab = MainNavTab.CUSTOMERS,
        label = strings.navCustomers,
        selectedIcon = Icons.Filled.People,
        unselectedIcon = Icons.Outlined.People,
        testTag = "nav_customers",
      ),
      NavDestinationItem(
        tab = MainNavTab.INVOICES,
        label = strings.navInvoices,
        selectedIcon = Icons.AutoMirrored.Filled.ReceiptLong,
        unselectedIcon = Icons.AutoMirrored.Outlined.ReceiptLong,
        testTag = "nav_invoices",
      ),
      NavDestinationItem(
        tab = MainNavTab.MORE,
        label = strings.navMore,
        selectedIcon = Icons.Filled.Menu,
        unselectedIcon = Icons.Outlined.Menu,
        testTag = "nav_more",
      ),
    )

  BoxWithConstraints(modifier = modifier.fillMaxSize()) {
    val isExpandedScreen = maxWidth >= 700.dp

    Scaffold(
      contentWindowInsets = WindowInsets.safeDrawing,
      topBar = {
        TopAppBar(
          title = {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Icon(
                imageVector = Icons.Default.Diamond,
                contentDescription = null,
                tint = RoyalGoldShimmer,
                modifier = Modifier.size(24.dp),
              )
              Column {
                Text(
                  text = strings.appName,
                  color = Color.White,
                  fontSize = 17.sp,
                  fontWeight = FontWeight.ExtraBold,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Text(
                  text =
                    "Au ₹${calculationEngine.formatMoney(metalRate.goldRate)}${metalRate.goldRateUnit.shortLabel} • Ag ₹${calculationEngine.formatMoney(metalRate.silverRate)}${metalRate.silverRateUnit.shortLabel}",
                  color = RoyalGoldShimmer,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  maxLines = 1,
                )
              }
            }
          },
          actions = {
            IconButton(
              onClick = { viewModel.selectTab(MainNavTab.GLOBAL_SEARCH) },
              modifier = Modifier.testTag("top_bar_global_search"),
            ) {
              Icon(
                imageVector = Icons.Default.Search,
                contentDescription = strings.globalSearch,
                tint = Color.White,
              )
            }
            IconButton(
              onClick = {
                viewModel.openSettingsSection(SettingsSection.MENU)
                viewModel.selectTab(MainNavTab.SETTINGS)
              },
              modifier = Modifier.testTag("nav_settings"),
            ) {
              Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = strings.settings,
                tint = Color.White,
              )
            }
            // Live Sync Status Badge in Top Bar
            Surface(
              onClick = {
                if (googleAccountState.isConnected) {
                  viewModel.openSettingsSection(SettingsSection.SYNC_STATUS)
                } else {
                  viewModel.openSettingsSection(SettingsSection.GOOGLE_ACCOUNT)
                }
              },
              shape = RoundedCornerShape(10.dp),
              color =
                when {
                  googleAccountState.isOfflineMode -> Color(0xFF9A3412)
                  !googleAccountState.isConnected -> Color.White.copy(alpha = 0.14f)
                  googleAccountState.syncStatus == SyncStatus.SYNCED -> Color(0xFF15803D)
                  else -> Color(0xFFB45309)
                },
              modifier = Modifier.padding(end = 6.dp).testTag("top_bar_sync_status_badge"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector =
                    when {
                      googleAccountState.isOfflineMode -> Icons.Default.CloudOff
                      googleAccountState.isConnected -> Icons.Default.CloudDone
                      else -> Icons.Default.CloudSync
                    },
                  contentDescription = "Sync Status",
                  tint = Color.White,
                  modifier = Modifier.size(15.dp),
                )
                Text(
                  text =
                    when {
                      googleAccountState.isOfflineMode -> "Offline"
                      !googleAccountState.isConnected -> "Connect"
                      else -> googleAccountState.syncStatus.displayName
                    },
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                )
              }
            }

            // Language Switch Button
            Surface(
              onClick = {
                val nextLang =
                  if (businessProfile.language == AppLanguage.ENGLISH) AppLanguage.HINDI
                  else AppLanguage.ENGLISH
                viewModel.switchLanguage(nextLang)
              },
              shape = RoundedCornerShape(10.dp),
              color = Color.White.copy(alpha = 0.12f),
              border = BorderStroke(1.dp, RoyalGoldShimmer.copy(alpha = 0.6f)),
              modifier = Modifier.padding(end = 10.dp).testTag("top_bar_language_toggle"),
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                Icon(
                  imageVector = Icons.Default.Language,
                  contentDescription = "Switch Language",
                  tint = RoyalGoldShimmer,
                  modifier = Modifier.size(15.dp),
                )
                Text(
                  text = if (businessProfile.language == AppLanguage.ENGLISH) "EN/हिं" else "हिं/EN",
                  color = Color.White,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                )
              }
            }
          },
          colors =
            TopAppBarDefaults.topAppBarColors(
              containerColor = RoyalObsidian,
              titleContentColor = Color.White,
            ),
        )
      },
      bottomBar = {
        if (!isExpandedScreen) {
          NavigationBar(
            modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
          ) {
            navItems.forEach { item ->
              val selected = currentTab == item.tab
              NavigationBarItem(
                selected = selected,
                onClick = {
                  if (item.tab == MainNavTab.SETTINGS) {
                    viewModel.openSettingsSection(SettingsSection.MENU)
                  } else if (item.tab == MainNavTab.NEW_TRANSACTION) {
                    viewModel.openNewTransactionType(null)
                  } else {
                    viewModel.selectTab(item.tab)
                  }
                },
                icon = {
                  Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                  )
                },
                label = {
                  Text(
                    text = item.label,
                    fontSize = 10.sp,
                    fontWeight = if (selected) FontWeight.ExtraBold else FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                },
                colors =
                  NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                  ),
                modifier = Modifier.testTag(item.testTag),
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Row(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
        if (isExpandedScreen) {
          NavigationRail(
            modifier = Modifier.fillMaxHeight(),
            containerColor = MaterialTheme.colorScheme.surface,
          ) {
            navItems.forEach { item ->
              val selected = currentTab == item.tab
              NavigationRailItem(
                selected = selected,
                onClick = {
                  if (item.tab == MainNavTab.SETTINGS) {
                    viewModel.openSettingsSection(SettingsSection.MENU)
                  } else if (item.tab == MainNavTab.NEW_TRANSACTION) {
                    viewModel.openNewTransactionType(null)
                  } else {
                    viewModel.selectTab(item.tab)
                  }
                },
                icon = {
                  Icon(
                    imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                  )
                },
                label = { Text(item.label, fontSize = 11.sp) },
                modifier = Modifier.testTag(item.testTag),
              )
            }
          }
        }

        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.TopCenter,
        ) {
          Column(
            modifier = Modifier.fillMaxSize().widthIn(max = 900.dp),
          ) {
            AnimatedVisibility(visible = statusBanner != null) {
              statusBanner?.let { msg ->
                Surface(
                  color = Color(0xFF14532D),
                  modifier = Modifier.fillMaxWidth().testTag("status_banner"),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Text(
                      text = msg,
                      color = Color.White,
                      fontWeight = FontWeight.Bold,
                      fontSize = 14.sp,
                      modifier = Modifier.weight(1f),
                    )
                    IconButton(onClick = { viewModel.clearStatusBanner() }) {
                      Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss notification",
                        tint = Color.White,
                      )
                    }
                  }
                }
              }
            }

            when (currentTab) {
              MainNavTab.DASHBOARD ->
                DashboardScreen(
                  strings = strings,
                  businessProfile = businessProfile,
                  metalRate = metalRate,
                  summary = dashboardSummary,
                  recentTransactions = transactions,
                  invoices = invoices,
                  calculationEngine = calculationEngine,
                  onQuickActionTransaction = { txType -> viewModel.openNewTransactionType(txType) },
                  onCreateInvoiceClick = { viewModel.openCreateInvoiceFlow() },
                  onOpenInvoicePreview = { inv -> viewModel.openInvoicePreview(inv) },
                  onDeleteTransaction = { txId -> viewModel.deleteTransaction(txId) },
                  onUpdateRates = { gRate, gUnit, sRate, sUnit, onErr ->
                    viewModel.saveMetalRates(gRate, gUnit, sRate, sUnit, onError = onErr)
                  },
                  goldInventorySummary = goldInventorySummary,
                  silverInventorySummary = silverInventorySummary,
                  reminders = reminders,
                  onOpenInventory = { viewModel.selectTab(MainNavTab.INVENTORY) },
                  onOpenCustomers = { viewModel.selectTab(MainNavTab.CUSTOMERS) },
                  onOpenInvoicesList = { viewModel.selectTab(MainNavTab.INVOICES) },
                  onOpenGlobalSearch = { viewModel.selectTab(MainNavTab.GLOBAL_SEARCH) },
                  onOpenReminders = {
                    viewModel.openSettingsSection(SettingsSection.REMINDERS)
                    viewModel.selectTab(MainNavTab.SETTINGS)
                  },
                )

              MainNavTab.NEW_TRANSACTION ->
                NewTransactionScreen(
                  selectedType = selectedTxType,
                  editingDraft = editingDraft,
                  savedSuccessState = savedTxSuccess,
                  allTransactions = transactions,
                  customers = customers,
                  metalRate = metalRate,
                  businessProfile = businessProfile,
                  suggestedInvoiceNumber = nextInvoiceNumber,
                  calcEngine = calculationEngine,
                  strings = strings,
                  mobileSearchResult = mobileSearchResult,
                  onSearchCustomerByMobile = { viewModel.searchCustomerByMobileNumber(it) },
                  onClearMobileSearch = { viewModel.clearMobileCustomerSearch() },
                  onSelectType = { viewModel.openNewTransactionType(it) },
                  onOpenDraftForEdit = { viewModel.openDraftTransactionForEdit(it) },
                  onQuickCreateCustomer = { id, name, mob, addr, pan, gst, notes, onSuccess, onErr ->
                    viewModel.saveCustomer(
                      existingId = id,
                      name = name,
                      mobile = mob,
                      address = addr,
                      pan = pan,
                      gst = gst,
                      pendingAmountStr = "0.00",
                      notes = notes,
                      onSuccess = onSuccess,
                      onError = onErr,
                    )
                  },
                  onQuickCreateFullCustomer = {
                    id,
                    name,
                    mob,
                    whatsapp,
                    addr,
                    city,
                    state,
                    pin,
                    pan,
                    gst,
                    email,
                    notes,
                    onSuccess,
                    onErr ->
                    viewModel.saveCustomer(
                      existingId = id,
                      name = name,
                      mobile = mob,
                      whatsapp = whatsapp,
                      address = addr,
                      city = city,
                      state = state,
                      pin = pin,
                      pan = pan,
                      gst = gst,
                      email = email,
                      pendingAmountStr = "0.00",
                      notes = notes,
                      onSuccess = onSuccess,
                      onError = onErr,
                    )
                  },
                  onEnableDeductionInSettings = { enabled ->
                    viewModel.saveBusinessProfile(
                      businessProfile.copy(enabledDeduction = enabled)
                    )
                  },
                  onSubmitTransaction = {
                    type,
                    cust,
                    pMode,
                    gross,
                    tunch,
                    rate,
                    rUnit,
                    money,
                    payMode,
                    invNum,
                    notes,
                    pendingAdd,
                    dedType,
                    dedVal,
                    cashPaid,
                    cashRec,
                    multiItems,
                    saveAsDraft,
                    onErr ->
                    viewModel.submitTransaction(
                      transactionType = type,
                      customer = cust,
                      purityMode = pMode,
                      grossWeightInput = gross,
                      tunchInput = tunch,
                      rateInput = rate,
                      rateUnit = rUnit,
                      moneyAmountInput = money,
                      paymentMode = payMode,
                      invoiceNumberInput = invNum,
                      notesInput = notes,
                      addPendingInput = pendingAdd,
                      deductionType = dedType,
                      deductionValueInput = dedVal,
                      cashPaidInput = cashPaid,
                      cashReceivedInput = cashRec,
                      multiItems = multiItems,
                      saveAsDraft = saveAsDraft,
                      onError = onErr,
                    )
                  },
                  onViewInvoice = {
                    val inv = savedTxSuccess?.invoice
                    viewModel.dismissSavedTransactionSuccessAndReset()
                    if (inv != null) {
                      viewModel.openInvoicePreview(inv)
                    }
                  },
                  onStartAnotherTransaction = {
                    viewModel.dismissSavedTransactionSuccessAndReset()
                    viewModel.openNewTransactionType(null)
                  },
                  onGoToDashboard = {
                    viewModel.dismissSavedTransactionSuccessAndReset()
                    viewModel.selectTab(MainNavTab.DASHBOARD)
                  },
                )

              MainNavTab.CUSTOMERS ->
                CustomersScreen(
                  customers = customers,
                  calcEngine = calculationEngine,
                  mobileSearchResult = mobileSearchResult,
                  selectedCustomerForHistory = selectedCustomerForHistory,
                  onSelectCustomerForHistory = { viewModel.openCustomerTransactionHistory(it) },
                  onGetCustomerLedger = { id, name -> viewModel.getCustomerLedgerSummary(id, name) },
                  onOpenDraftForEdit = { viewModel.openDraftTransactionForEdit(it) },
                  onCancelTransaction = { txId, reason ->
                    viewModel.cancelTransaction(
                      transactionId = txId,
                      cancellationReason = reason,
                    )
                  },
                  onSearchByMobileInCloud = { viewModel.searchCustomerByMobileNumber(it) },
                  onClearMobileSearch = { viewModel.clearMobileCustomerSearch() },
                  onSaveCustomer = { id, name, mob, addr, pan, gst, pending, notes, onSucc, onErr ->
                    viewModel.saveCustomer(
                      existingId = id,
                      name = name,
                      mobile = mob,
                      address = addr,
                      pan = pan,
                      gst = gst,
                      pendingAmountStr = pending,
                      notes = notes,
                      onSuccess = onSucc,
                      onError = onErr,
                    )
                  },
                  onSaveFullCustomer = {
                    id,
                    name,
                    mob,
                    whatsapp,
                    addr,
                    city,
                    state,
                    pin,
                    pan,
                    gst,
                    email,
                    pending,
                    notes,
                    onSucc,
                    onErr ->
                    viewModel.saveCustomer(
                      existingId = id,
                      name = name,
                      mobile = mob,
                      whatsapp = whatsapp,
                      address = addr,
                      city = city,
                      state = state,
                      pin = pin,
                      pan = pan,
                      gst = gst,
                      email = email,
                      pendingAmountStr = pending,
                      notes = notes,
                      onSuccess = onSucc,
                      onError = onErr,
                    )
                  },
                  onDeleteCustomer = { viewModel.deleteCustomer(it) },
                  onBackToDashboard = { viewModel.selectTab(MainNavTab.DASHBOARD) },
                )

              MainNavTab.INVOICES -> {
                val dateAndTypeFilteredInvoices =
                  remember(
                    invoices,
                    invoiceTypeFilter,
                    invoiceDateFilter,
                    invoiceCustomStart,
                    invoiceCustomEnd,
                  ) {
                    viewModel.filterInvoices(
                      allInvoices = invoices,
                      searchQuery = "",
                      typeFilter = invoiceTypeFilter,
                      dateFilter = invoiceDateFilter,
                    )
                  }
                InvoicesScreen(
                  invoices = dateAndTypeFilteredInvoices,
                  selectedInvoice = selectedInvoice,
                  calculationEngine = calculationEngine,
                  printFormat = invoicePrintFormat,
                  typeFilter = invoiceTypeFilter,
                  dateFilter = invoiceDateFilter,
                  customStartDate = invoiceCustomStart,
                  customEndDate = invoiceCustomEnd,
                  pendingDriveUploads = pendingDriveUploads,
                  onSelectPrintFormat = { viewModel.setInvoicePrintFormat(it) },
                  onSelectTypeFilter = { viewModel.setInvoiceTypeFilter(it) },
                  onSelectDateFilter = { viewModel.setInvoiceDateFilter(it) },
                  onCustomDateRangeChange = { start, end ->
                    viewModel.setInvoiceCustomDateRange(start, end)
                  },
                  onSelectInvoice = { viewModel.openInvoicePreview(it) },
                  onCreateNewInvoice = { viewModel.openCreateInvoiceFlow() },
                  onDeleteInvoice = { viewModel.deleteInvoice(it) },
                  onGeneratePdf = { inv, force ->
                    viewModel.generateInvoicePdf(
                      context = context,
                      invoice = inv,
                      format = invoicePrintFormat,
                      forceRegenerate = force,
                    )
                  },
                  onSaveToDrive = { inv, force ->
                    viewModel.saveInvoiceToGoogleDrive(
                      context = context,
                      invoice = inv,
                      format = invoicePrintFormat,
                      forceRegenerate = force,
                    )
                  },
                  onRetryPendingDriveUploads = { viewModel.syncPendingDriveUploadsNow() },
                  onPrintInvoice = { inv ->
                    viewModel.printInvoice(
                      context = context,
                      invoice = inv,
                      format = invoicePrintFormat,
                    )
                  },
                  onSharePdf = { inv ->
                    viewModel.shareInvoice(
                      context = context,
                      invoice = inv,
                      shareAsPdf = true,
                      format = invoicePrintFormat,
                    )
                  },
                  onShareText = { inv ->
                    viewModel.shareInvoice(
                      context = context,
                      invoice = inv,
                      shareAsPdf = false,
                      format = invoicePrintFormat,
                    )
                  },
                  onFormatShareText = { viewModel.formatShareableInvoice(it) },
                  onBackToDashboard = { viewModel.selectTab(MainNavTab.DASHBOARD) },
                )
              }

              MainNavTab.REPORTS -> {
                val filteredTxs =
                  remember(
                    transactions,
                    reportDateFilter,
                    customStartDate,
                    customEndDate,
                    selectedReportCustomerId,
                  ) {
                    viewModel.filterTransactionsForReport(transactions)
                  }
                ReportsScreen(
                  allTransactions = transactions,
                  filteredTransactions = filteredTxs,
                  customers = customers,
                  invoices = invoices,
                  activeDateFilter = reportDateFilter,
                  customStartDate = customStartDate,
                  customEndDate = customEndDate,
                  selectedCustomerId = selectedReportCustomerId,
                  calculationEngine = calculationEngine,
                  onSelectDateFilter = { viewModel.setReportDateFilter(it) },
                  onApplyCustomDateRange = { s, e -> viewModel.setCustomDateRange(s, e) },
                  onSelectCustomerFilter = { viewModel.setSelectedReportCustomer(it) },
                  onOpenInvoicePreview = { viewModel.openInvoicePreview(it) },
                  onDeleteTransaction = { viewModel.deleteTransaction(it) },
                  onBackToDashboard = { viewModel.selectTab(MainNavTab.DASHBOARD) },
                  businessProfile = businessProfile,
                  vendors = vendors,
                  strings = strings,
                  goldInventorySummary = goldInventorySummary,
                  silverInventorySummary = silverInventorySummary,
                  inventoryMovements = inventoryMovements,
                  purchases = purchases,
                  sales = sales,
                  onExportCsv = { reportType, shareAfter ->
                    viewModel.exportReportToCsv(
                      context = context,
                      reportType = reportType,
                      onSuccess = { file ->
                        if (shareAfter) {
                          viewModel.shareCsvFile(context, file)
                        }
                      },
                    )
                  },
                  onExportPdf = { reportType, saveToDrive, shareAfter ->
                    viewModel.exportReportToPdf(
                      context = context,
                      reportType = reportType,
                      saveToDrive = saveToDrive,
                      shareAfterExport = shareAfter,
                    )
                  },
                )
              }

              MainNavTab.MORE -> MoreMenuScreen(viewModel = viewModel)

              MainNavTab.INVENTORY -> InventoryScreen(viewModel = viewModel)

              MainNavTab.PURCHASES -> PurchasesScreen(viewModel = viewModel)

              MainNavTab.SALES -> SalesScreen(viewModel = viewModel)

              MainNavTab.VENDORS -> VendorsScreen(viewModel = viewModel)

              MainNavTab.GLOBAL_SEARCH -> GlobalSearchScreen(viewModel = viewModel)

              MainNavTab.BACKUP_RECOVERY -> BackupAndRecoveryScreen(viewModel = viewModel)

              MainNavTab.SETTINGS ->
                SettingsScreen(
                  activeSection = activeSettingsSection,
                  businessProfile = businessProfile,
                  metalRate = metalRate,
                  googleAccountState = googleAccountState,
                  availableSpreadsheets = availableSpreadsheets,
                  pendingSyncQueue = pendingSyncQueue,
                  customerCount = customers.size,
                  transactionCount = transactions.size,
                  invoiceCount = invoices.size,
                  calculationEngine = calculationEngine,
                  onSelectSection = { viewModel.openSettingsSection(it) },
                  onSaveBusinessProfile = { prof, onSucc, onErr ->
                    viewModel.saveBusinessProfile(prof, onSucc, onErr)
                  },
                  onSaveMetalRates = { gRate, gUnit, sRate, sUnit, onSucc, onErr ->
                    viewModel.saveMetalRates(gRate, gUnit, sRate, sUnit, onSucc, onErr)
                  },
                  onSwitchLanguage = { viewModel.switchLanguage(it) },
                  onConnectGoogleAccount = { viewModel.startGoogleAccountConnection(context) },
                  onDisconnectGoogleAccount = { viewModel.disconnectGoogleAccount() },
                  onReconnectGoogleAccount = { viewModel.reconnectGoogleAccount(context) },
                  onCreateOrSelectDatabase = { title ->
                    viewModel.createOrSelectDatabaseSpreadsheet(title)
                  },
                  onSelectExistingSpreadsheet = { info ->
                    viewModel.selectExistingSpreadsheet(info)
                  },
                  onSyncNow = { viewModel.syncNow() },
                  onToggleOfflineMode = { viewModel.setOfflineMode(it) },
                  onLoadWorksheetRows = { wsName ->
                    viewModel.getWorksheetRowsForInspection(wsName)
                  },
                  onResetDemoData = { viewModel.resetDemoData() },
                  onBackToDashboard = { viewModel.selectTab(MainNavTab.DASHBOARD) },
                  viewModel = viewModel,
                )
            }
          }
        }
      }
    }

    if (showGoogleOAuthConsentSheet) {
      GoogleOAuthConsentDialog(
        defaultEmail = "vivek.v@digitallogics.in",
        onConfirm = { email -> viewModel.confirmGoogleOAuthSheetConnection(email) },
        onCancel = { viewModel.cancelGoogleOAuthSheetConnection() },
      )
    }
  }
}

@Composable
private fun GoogleOAuthConsentDialog(
  defaultEmail: String,
  onConfirm: (String) -> Unit,
  onCancel: () -> Unit,
) {
  var emailInput by remember { mutableStateOf(defaultEmail) }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onCancel,
    icon = {
      Icon(
        imageVector = Icons.Default.AccountCircle,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(38.dp),
      )
    },
    title = {
      Text(
        text = "Sign in with Google",
        fontWeight = FontWeight.Bold,
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Choose your Google Account to authorize 'Jewellery Business Manager' (Vivek Verma's Apps):",
          style = MaterialTheme.typography.bodyMedium,
        )
        errorMsg?.let {
          Text(it, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
        }
        OutlinedTextField(
          value = emailInput,
          onValueChange = {
            emailInput = it
            errorMsg = null
          },
          label = { Text("Google Account Email") },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("oauth_dialog_email_input"),
        )
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Text(
              text = "Minimum Permissions Requested:",
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp,
            )
            Text("• Google Sheets: Create & sync 'Jewellery Business Database'", fontSize = 12.sp)
            Text("• Google Drive (drive.file): Access only app-created spreadsheet", fontSize = 12.sp)
            Text("• No password is requested or stored.", fontSize = 11.sp)
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (emailInput.isBlank() || !emailInput.contains("@")) {
            errorMsg = "Please enter a valid Google email address."
          } else {
            onConfirm(emailInput.trim())
          }
        },
        modifier = Modifier.testTag("oauth_dialog_allow_btn"),
      ) {
        Text("Allow & Connect")
      }
    },
    dismissButton = {
      TextButton(
        onClick = onCancel,
        modifier = Modifier.testTag("oauth_dialog_cancel_btn"),
      ) {
        Text("Cancel")
      }
    },
  )
}
