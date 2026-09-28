package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.calculation.CalculationEngine
import com.example.domain.model.BusinessProfile
import com.example.domain.model.Customer
import com.example.domain.model.DeductionType
import com.example.domain.model.MetalRate
import com.example.domain.model.MetalType
import com.example.domain.model.PaymentMode
import com.example.domain.model.PuritySelectionMode
import com.example.domain.model.RateUnit
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionItem
import com.example.domain.model.TransactionStatus
import com.example.domain.model.TransactionType
import com.example.ui.i18n.LocalizedStrings
import com.example.ui.theme.GoldContainerLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SilverContainerLight
import com.example.ui.theme.SilverPrimary
import com.example.ui.theme.SuccessGreen
import com.example.ui.viewmodel.MobileCustomerSearchResult
import com.example.ui.viewmodel.SavedTransactionSuccessState
import java.math.BigDecimal

@Composable
fun NewTransactionScreen(
  selectedType: TransactionType?,
  editingDraft: Transaction? = null,
  savedSuccessState: SavedTransactionSuccessState?,
  allTransactions: List<Transaction> = emptyList(),
  customers: List<Customer>,
  metalRate: MetalRate,
  businessProfile: BusinessProfile = BusinessProfile(),
  suggestedInvoiceNumber: String,
  calcEngine: CalculationEngine,
  strings: LocalizedStrings,
  mobileSearchResult: MobileCustomerSearchResult? = null,
  onSearchCustomerByMobile: (String) -> Unit = {},
  onClearMobileSearch: () -> Unit = {},
  onSelectType: (TransactionType?) -> Unit,
  onOpenDraftForEdit: (Transaction) -> Unit = {},
  onQuickCreateCustomer:
    (
      existingId: String?,
      name: String,
      mobile: String,
      address: String,
      pan: String,
      gst: String,
      notes: String,
      onCreated: (Customer) -> Unit,
      onError: (String) -> Unit,
    ) -> Unit = { _, _, _, _, _, _, _, _, _ -> },
  onQuickCreateFullCustomer:
    (
      existingId: String?,
      name: String,
      mobile: String,
      whatsapp: String,
      address: String,
      city: String,
      state: String,
      pin: String,
      pan: String,
      gst: String,
      email: String,
      notes: String,
      onCreated: (Customer) -> Unit,
      onError: (String) -> Unit,
    ) -> Unit = { id, name, mob, _, addr, _, _, _, pan, gst, _, notes, onCreated, onErr ->
      onQuickCreateCustomer(id, name, mob, addr, pan, gst, notes, onCreated, onErr)
    },
  onEnableDeductionInSettings: (Boolean) -> Unit = {},
  onSubmitTransaction:
    (
      type: TransactionType,
      customer: Customer?,
      purityMode: PuritySelectionMode,
      grossWeight: String,
      tunch: String,
      rate: String,
      rateUnit: RateUnit,
      moneyAmount: String,
      paymentMode: PaymentMode,
      invoiceNumber: String,
      notes: String,
      addPendingAmount: String,
      deductionType: DeductionType,
      deductionValueInput: String,
      cashPaidInput: String,
      cashReceivedInput: String,
      multiItems: List<TransactionItem>,
      saveAsDraft: Boolean,
      onError: (String) -> Unit,
    ) -> Unit,
  onViewInvoice: () -> Unit,
  onStartAnotherTransaction: () -> Unit,
  onGoToDashboard: () -> Unit,
) {
  if (savedSuccessState != null) {
    BackHandler { onGoToDashboard() }
    TransactionSavedSuccessView(
      savedState = savedSuccessState,
      calcEngine = calcEngine,
      strings = strings,
      onViewInvoice = onViewInvoice,
      onEditDraftAgain = { onOpenDraftForEdit(savedSuccessState.transaction) },
      onNewTransaction = onStartAnotherTransaction,
      onGoToDashboard = onGoToDashboard,
    )
    return
  }

  if (selectedType == null) {
    BackHandler { onGoToDashboard() }
    val draftTransactions =
      remember(allTransactions) {
        allTransactions.filter { it.status == TransactionStatus.DRAFT }
      }
    TransactionSelectionMenu(
      draftTransactions = draftTransactions,
      calcEngine = calcEngine,
      onSelectType = { onSelectType(it) },
      onOpenDraftForEdit = onOpenDraftForEdit,
    )
  } else {
    BackHandler { onSelectType(null) }
    TransactionFormView(
      transactionType = selectedType,
      editingDraft = editingDraft,
      customers = customers,
      metalRate = metalRate,
      businessProfile = businessProfile,
      suggestedInvoiceNumber = suggestedInvoiceNumber,
      calcEngine = calcEngine,
      mobileSearchResult = mobileSearchResult,
      onSearchCustomerByMobile = onSearchCustomerByMobile,
      onClearMobileSearch = onClearMobileSearch,
      onBackToSelection = { onSelectType(null) },
      onQuickCreateFullCustomer = onQuickCreateFullCustomer,
      onEnableDeductionInSettings = onEnableDeductionInSettings,
      onSubmitTransaction = onSubmitTransaction,
    )
  }
}

@Composable
private fun TransactionSelectionMenu(
  draftTransactions: List<Transaction>,
  calcEngine: CalculationEngine,
  onSelectType: (TransactionType) -> Unit,
  onOpenDraftForEdit: (Transaction) -> Unit,
) {
  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 840.dp).testTag("transaction_selection_list"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        Text(
          text = "Select Transaction Type",
          style = MaterialTheme.typography.headlineMedium,
          fontWeight = FontWeight.Bold,
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Tap an option below to open the jewellery calculation and billing form",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      if (draftTransactions.isNotEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth().testTag("draft_transactions_banner_card"),
            shape = RoundedCornerShape(16.dp),
            colors =
              CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.65f)
              ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.tertiary),
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.EditNote,
                  contentDescription = "Drafts",
                  tint = MaterialTheme.colorScheme.onTertiaryContainer,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Saved Draft Transactions (${draftTransactions.size}) — Editable Before Finalization",
                  style = MaterialTheme.typography.titleSmall,
                  fontWeight = FontWeight.Bold,
                )
              }
              draftTransactions.forEach { draft ->
                Card(
                  modifier =
                    Modifier.fillMaxWidth()
                      .clickable { onOpenDraftForEdit(draft) }
                      .testTag("draft_tx_card_${draft.transactionId}"),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                ) {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = "${draft.transactionType.code}. ${draft.transactionType.title} — ${draft.customerName}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                      )
                      Text(
                        text =
                          "ID: ${draft.transactionId} • Fine: ${calcEngine.formatWeight(draft.fineWeight)} g • Net: ${calcEngine.formatCurrency(draft.netValue)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                      )
                    }
                    OutlinedButton(
                      onClick = { onOpenDraftForEdit(draft) },
                      modifier = Modifier.testTag("edit_draft_btn_${draft.transactionId}"),
                    ) {
                      Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                      Spacer(modifier = Modifier.width(4.dp))
                      Text("Edit / Finalize")
                    }
                  }
                }
              }
            }
          }
        }
      }

      val options =
        listOf(
          Triple(
            TransactionType.MONEY_TO_GOLD,
            Icons.Default.CurrencyRupee,
            GoldContainerLight to GoldPrimary,
          ),
          Triple(
            TransactionType.MONEY_TO_SILVER,
            Icons.Default.CurrencyRupee,
            SilverContainerLight to SilverPrimary,
          ),
          Triple(
            TransactionType.GOLD_PAYMENT,
            Icons.Default.Scale,
            GoldContainerLight to GoldPrimary,
          ),
          Triple(
            TransactionType.SILVER_PAYMENT,
            Icons.Default.Scale,
            SilverContainerLight to SilverPrimary,
          ),
          Triple(
            TransactionType.SCRAP_GOLD,
            Icons.Default.Recycling,
            Color(0xFFFFF8E1) to Color(0xFF9C6500),
          ),
          Triple(
            TransactionType.SCRAP_SILVER,
            Icons.Default.Recycling,
            Color(0xFFECEFF1) to Color(0xFF37474F),
          ),
          Triple(
            TransactionType.GOLD_ADJUSTMENT,
            Icons.Default.Tune,
            Color(0xFFFFF3E0) to Color(0xFF8D6E63),
          ),
          Triple(
            TransactionType.SILVER_ADJUSTMENT,
            Icons.Default.Tune,
            Color(0xFFE8EAF6) to Color(0xFF3949AB),
          ),
        )

      items(options.size) { index ->
        val (type, icon, colors) = options[index]
        TransactionOptionCard(
          type = type,
          icon = icon,
          containerColor = colors.first,
          accentColor = colors.second,
          onClick = { onSelectType(type) },
        )
      }

      item { Spacer(modifier = Modifier.height(24.dp)) }
    }
  }
}

@Composable
private fun TransactionOptionCard(
  type: TransactionType,
  icon: ImageVector,
  containerColor: Color,
  accentColor: Color,
  onClick: () -> Unit,
) {
  ElevatedCard(
    modifier =
      Modifier.fillMaxWidth()
        .clickable(onClick = onClick)
        .testTag("select_tx_type_${type.name.lowercase()}"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(18.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.weight(1f),
      ) {
        Surface(
          color = containerColor,
          shape = RoundedCornerShape(14.dp),
          border = BorderStroke(1.dp, accentColor.copy(alpha = 0.35f)),
          modifier = Modifier.size(56.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = icon,
              contentDescription = type.title,
              tint = accentColor,
              modifier = Modifier.size(28.dp),
            )
          }
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
              color = accentColor.copy(alpha = 0.14f),
              shape = RoundedCornerShape(6.dp),
            ) {
              Text(
                text = "Option ${type.code}",
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = type.metalType.displayName,
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = type.title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = type.subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Icon(
        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
        contentDescription = "Open ${type.title}",
        tint = MaterialTheme.colorScheme.primary,
      )
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TransactionFormView(
  transactionType: TransactionType,
  editingDraft: Transaction? = null,
  customers: List<Customer>,
  metalRate: MetalRate,
  businessProfile: BusinessProfile,
  suggestedInvoiceNumber: String,
  calcEngine: CalculationEngine,
  mobileSearchResult: MobileCustomerSearchResult? = null,
  onSearchCustomerByMobile: (String) -> Unit = {},
  onClearMobileSearch: () -> Unit = {},
  onBackToSelection: () -> Unit,
  onQuickCreateFullCustomer:
    (
      existingId: String?,
      name: String,
      mobile: String,
      whatsapp: String,
      address: String,
      city: String,
      state: String,
      pin: String,
      pan: String,
      gst: String,
      email: String,
      notes: String,
      onCreated: (Customer) -> Unit,
      onError: (String) -> Unit,
    ) -> Unit,
  onEnableDeductionInSettings: (Boolean) -> Unit = {},
  onSubmitTransaction:
    (
      type: TransactionType,
      customer: Customer?,
      purityMode: PuritySelectionMode,
      grossWeight: String,
      tunch: String,
      rate: String,
      rateUnit: RateUnit,
      moneyAmount: String,
      paymentMode: PaymentMode,
      invoiceNumber: String,
      notes: String,
      addPendingAmount: String,
      deductionType: DeductionType,
      deductionValueInput: String,
      cashPaidInput: String,
      cashReceivedInput: String,
      multiItems: List<TransactionItem>,
      saveAsDraft: Boolean,
      onError: (String) -> Unit,
    ) -> Unit,
) {
  val isGold = transactionType.metalType == MetalType.GOLD
  val defaultRateValue =
    if (isGold) metalRate.goldRate.toPlainString() else metalRate.silverRate.toPlainString()
  val defaultRateUnit = if (isGold) metalRate.goldRateUnit else metalRate.silverRateUnit

  var selectedCustomer by remember(customers, editingDraft) {
    mutableStateOf(
      editingDraft?.let { d -> customers.find { it.id == d.customerId } } ?: customers.firstOrNull()
    )
  }
  var customerDropdownExpanded by remember { mutableStateOf(false) }
  var showQuickCustomerDialog by remember { mutableStateOf(false) }
  var editingSearchedCustomer by remember { mutableStateOf<Customer?>(null) }
  var mobileSearchQuery by remember { mutableStateOf("") }

  var purityMode by remember(transactionType, editingDraft) {
    mutableStateOf(
      editingDraft?.purityMode
        ?: when (transactionType) {
          TransactionType.GOLD_PAYMENT -> PuritySelectionMode.PURE_99
          else -> PuritySelectionMode.CUSTOM_TUNCH
        }
    )
  }

  var grossWeightInput by remember(transactionType, editingDraft) {
    mutableStateOf(
      editingDraft?.grossWeight?.toPlainString()
        ?: when (transactionType) {
          TransactionType.GOLD_PAYMENT,
          TransactionType.SILVER_PAYMENT,
          TransactionType.SCRAP_GOLD,
          TransactionType.SCRAP_SILVER,
          TransactionType.GOLD_ADJUSTMENT,
          TransactionType.SILVER_ADJUSTMENT -> "10.000"
          else -> ""
        }
    )
  }

  var tunchInput by remember(transactionType, purityMode, editingDraft) {
    mutableStateOf(
      editingDraft?.tunch?.toPlainString()
        ?: when (transactionType) {
          TransactionType.MONEY_TO_GOLD,
          TransactionType.MONEY_TO_SILVER -> "100.00"
          TransactionType.GOLD_PAYMENT ->
            if (purityMode == PuritySelectionMode.PURE_99) "99.00" else "80.00"
          TransactionType.SILVER_PAYMENT ->
            if (purityMode == PuritySelectionMode.PURE_99) "99.00" else "70.00"
          TransactionType.SCRAP_GOLD,
          TransactionType.GOLD_ADJUSTMENT -> "80.00"
          TransactionType.SCRAP_SILVER,
          TransactionType.SILVER_ADJUSTMENT -> "70.00"
        }
    )
  }

  var rateInput by remember(transactionType, defaultRateValue, editingDraft) {
    mutableStateOf(editingDraft?.rate?.toPlainString() ?: defaultRateValue)
  }
  var rateUnit by remember(transactionType, defaultRateUnit, editingDraft) {
    mutableStateOf(editingDraft?.rateUnit ?: defaultRateUnit)
  }

  var moneyAmountInput by remember(transactionType, editingDraft) {
    mutableStateOf(
      editingDraft?.cashAmount?.toPlainString()
        ?: when (transactionType) {
          TransactionType.MONEY_TO_GOLD -> "50000"
          TransactionType.MONEY_TO_SILVER -> "10000"
          else -> ""
        }
    )
  }

  var deductionType by remember(transactionType, editingDraft) {
    mutableStateOf(editingDraft?.deductionType ?: DeductionType.NONE)
  }
  var deductionValueInput by remember(transactionType, editingDraft) {
    mutableStateOf(
      editingDraft?.deductionValue?.takeIf { it > BigDecimal.ZERO }?.toPlainString() ?: ""
    )
  }

  var cashPaidInput by remember(transactionType, editingDraft) {
    mutableStateOf(editingDraft?.cashPaid?.takeIf { it > BigDecimal.ZERO }?.toPlainString() ?: "")
  }
  var cashReceivedInput by remember(transactionType, editingDraft) {
    mutableStateOf(
      editingDraft?.cashReceived?.takeIf { it > BigDecimal.ZERO }?.toPlainString() ?: ""
    )
  }

  val multiItems = remember(transactionType, editingDraft) {
    mutableStateListOf<TransactionItem>().apply {
      if (editingDraft != null && editingDraft.items.size > 1) {
        addAll(editingDraft.items)
      }
    }
  }
  var showMultiItemsBuilder by remember(editingDraft) {
    mutableStateOf(editingDraft != null && editingDraft.items.size > 1)
  }

  var paymentMode by remember(editingDraft) {
    mutableStateOf(editingDraft?.paymentMode ?: PaymentMode.CASH)
  }
  var invoiceNumberInput by remember(suggestedInvoiceNumber, editingDraft) {
    mutableStateOf(editingDraft?.invoiceNumber?.ifBlank { suggestedInvoiceNumber } ?: suggestedInvoiceNumber)
  }
  var addPendingInput by remember { mutableStateOf("") }
  var notesInput by remember(editingDraft) { mutableStateOf(editingDraft?.notes ?: "") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(suggestedInvoiceNumber) {
    if (editingDraft == null && invoiceNumberInput.isBlank()) {
      invoiceNumberInput = suggestedInvoiceNumber
    }
  }

  // Real-time calculation preview
  val parsedGross = calcEngine.parseSafeBigDecimal(grossWeightInput) ?: BigDecimal.ZERO
  val parsedTunch =
    if (
      (transactionType == TransactionType.GOLD_PAYMENT ||
        transactionType == TransactionType.SILVER_PAYMENT) &&
        purityMode == PuritySelectionMode.PURE_99 &&
        tunchInput.isBlank()
    ) {
      CalculationEngine.PURE_99_TUNCH
    } else {
      calcEngine.parseSafeBigDecimal(tunchInput) ?: BigDecimal.ZERO
    }
  val parsedRate = calcEngine.parseSafeBigDecimal(rateInput) ?: BigDecimal.ZERO
  val parsedMoney = calcEngine.parseSafeBigDecimal(moneyAmountInput) ?: BigDecimal.ZERO
  val parsedDeductionVal =
    calcEngine.parseSafeBigDecimal(deductionValueInput)?.coerceAtLeast(BigDecimal.ZERO)
      ?: BigDecimal.ZERO
  val parsedCashPaid = calcEngine.parseSafeBigDecimal(cashPaidInput) ?: BigDecimal.ZERO
  val parsedCashReceived = calcEngine.parseSafeBigDecimal(cashReceivedInput) ?: BigDecimal.ZERO

  val livePurityCalc =
    remember(parsedGross, parsedTunch, calcEngine) {
      calcEngine.calculateFineWeight(parsedGross, parsedTunch)
    }

  val liveMoneyToMetalCalc =
    remember(parsedMoney, parsedRate, rateUnit, parsedTunch, calcEngine) {
      val optTunch = if (parsedTunch > BigDecimal.ZERO) parsedTunch else BigDecimal("100.00")
      calcEngine.calculateMoneyToMetal(parsedMoney, parsedRate, rateUnit, optTunch)
    }

  val liveScrapCalc =
    remember(parsedGross, parsedTunch, parsedRate, rateUnit, deductionType, parsedDeductionVal, calcEngine) {
      calcEngine.calculateScrapMetal(
        grossWeight = parsedGross,
        tunch = parsedTunch,
        rate = parsedRate,
        unit = rateUnit,
        deductionType = deductionType,
        deductionValue = parsedDeductionVal,
      )
    }

  val livePaymentMetalValue =
    remember(livePurityCalc.fineWeight, parsedRate, rateUnit, parsedMoney, calcEngine) {
      if (parsedRate > BigDecimal.ZERO) {
        calcEngine.calculateMetalValue(livePurityCalc.fineWeight, parsedRate, rateUnit)
      } else {
        parsedMoney
      }
    }

  val livePaymentAdjustment =
    remember(livePaymentMetalValue, parsedCashPaid, parsedCashReceived, calcEngine) {
      calcEngine.calculatePaymentAdjustment(
        metalValue = livePaymentMetalValue,
        cashPaid = parsedCashPaid,
        cashReceived = parsedCashReceived,
      )
    }

  val liveMultiCalc =
    remember(multiItems.toList(), deductionType, parsedDeductionVal, parsedRate, rateUnit, calcEngine) {
      if (multiItems.isNotEmpty()) {
        calcEngine.calculateMultipleItemsTotals(
          items = multiItems.toList(),
          deductionType = deductionType,
          deductionValue = parsedDeductionVal,
          defaultRate = parsedRate,
          defaultRateUnit = rateUnit,
        )
      } else {
        null
      }
    }

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 800.dp).testTag("transaction_form_scroll"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      // Top Header with Back Button
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = onBackToSelection,
            modifier = Modifier.size(48.dp).testTag("back_to_tx_selection_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to transaction types",
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Option ${transactionType.code} • ${transactionType.metalType.displayName}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
              )
              if (editingDraft != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                  color = MaterialTheme.colorScheme.tertiaryContainer,
                  shape = RoundedCornerShape(6.dp),
                ) {
                  Text(
                    text = "EDITING DRAFT (${editingDraft.transactionId})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  )
                }
              }
            }
            Text(
              text = transactionType.title,
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }

      // Error Banner if any
      if (errorMessage != null) {
        item {
          Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().testTag("tx_form_error_banner"),
          ) {
            Text(
              text = errorMessage!!,
              color = MaterialTheme.colorScheme.onErrorContainer,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(14.dp),
            )
          }
        }
      }

      // 1. Customer Selection & Mobile Search Card
      item {
        ElevatedCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "1. Customer Details",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
              OutlinedButton(
                onClick = {
                  editingSearchedCustomer = null
                  showQuickCustomerDialog = true
                },
                modifier = Modifier.testTag("quick_add_customer_button"),
              ) {
                Icon(
                  imageVector = Icons.Default.PersonAdd,
                  contentDescription = "Add New Customer",
                  modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("+ Add New Customer")
              }
            }

            // Search Customer by Mobile Number (Local Cache -> Google Sheets)
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              OutlinedTextField(
                value = mobileSearchQuery,
                onValueChange = {
                  mobileSearchQuery = it
                  if (it.isBlank()) onClearMobileSearch()
                },
                label = { Text("Search Customer by Mobile Number") },
                placeholder = { Text("Enter mobile (local cache + Google Sheets)") },
                leadingIcon = { Icon(Icons.Default.PersonSearch, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.weight(1f).testTag("tx_mobile_customer_search_input"),
              )
              Button(
                onClick = { onSearchCustomerByMobile(mobileSearchQuery) },
                modifier = Modifier.height(54.dp).testTag("tx_mobile_customer_search_button"),
              ) {
                Icon(Icons.Default.Search, contentDescription = "Search Mobile")
                Spacer(modifier = Modifier.width(4.dp))
                Text("Find")
              }
            }

            if (mobileSearchResult != null) {
              Surface(
                color =
                  if (mobileSearchResult.customer != null) {
                    MaterialTheme.colorScheme.secondaryContainer
                  } else {
                    MaterialTheme.colorScheme.surfaceVariant
                  },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().testTag("tx_mobile_search_result_card"),
              ) {
                val found = mobileSearchResult.customer
                if (found != null) {
                  Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically,
                    ) {
                      Text(
                        text = "Found (${mobileSearchResult.source}): ${found.name}",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                      )
                      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        TextButton(
                          onClick = {
                            selectedCustomer = found
                            onClearMobileSearch()
                          },
                          modifier = Modifier.testTag("use_searched_customer_button"),
                        ) {
                          Text("Select")
                        }
                        TextButton(
                          onClick = {
                            editingSearchedCustomer = found
                            showQuickCustomerDialog = true
                          },
                          modifier = Modifier.testTag("edit_searched_customer_button"),
                        ) {
                          Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(16.dp),
                          )
                          Spacer(modifier = Modifier.width(4.dp))
                          Text("Edit")
                        }
                      }
                    }
                    Text(
                      text =
                        "Mobile: ${found.mobileNumber} • PAN: ${found.panNumber.ifBlank { "—" }} • GST: ${found.gstNumber.ifBlank { "Optional" }}",
                      style = MaterialTheme.typography.bodySmall,
                    )
                    if (found.address.isNotBlank()) {
                      Text(
                        text = "Address: ${found.address}",
                        style = MaterialTheme.typography.bodySmall,
                      )
                    }
                  }
                } else {
                  Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                  ) {
                    Text(
                      text = "No customer found for '${mobileSearchResult.queryMobile}'.",
                      style = MaterialTheme.typography.bodySmall,
                    )
                    TextButton(
                      onClick = {
                        editingSearchedCustomer = null
                        showQuickCustomerDialog = true
                      },
                      modifier = Modifier.testTag("add_missing_mobile_customer_button"),
                    ) {
                      Text("+ Add New Customer", fontWeight = FontWeight.Bold)
                    }
                  }
                }
              }
            }

            ExposedDropdownMenuBox(
              expanded = customerDropdownExpanded,
              onExpandedChange = { customerDropdownExpanded = !customerDropdownExpanded },
            ) {
              OutlinedTextField(
                value =
                  selectedCustomer?.let { "${it.name} (${it.mobileNumber})" }
                    ?: "Select Customer",
                onValueChange = {},
                readOnly = true,
                label = { Text("Customer") },
                trailingIcon = {
                  ExposedDropdownMenuDefaults.TrailingIcon(expanded = customerDropdownExpanded)
                },
                modifier =
                  Modifier.fillMaxWidth()
                    .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                    .testTag("customer_dropdown_field"),
              )
              ExposedDropdownMenu(
                expanded = customerDropdownExpanded,
                onDismissRequest = { customerDropdownExpanded = false },
              ) {
                customers.forEach { customer ->
                  DropdownMenuItem(
                    text = {
                      Column {
                        Text(customer.name, fontWeight = FontWeight.SemiBold)
                        Text(
                          "${customer.mobileNumber} • ${customer.address.ifBlank { "No address" }}",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                      }
                    },
                    onClick = {
                      selectedCustomer = customer
                      customerDropdownExpanded = false
                      errorMessage = null
                    },
                  )
                }
              }
            }

            selectedCustomer?.let { cust ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text(
                  text = "Mobile: ${cust.mobileNumber}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (cust.pendingAmount > BigDecimal.ZERO) {
                  Text(
                    text = "Existing Pending: ${calcEngine.formatCurrency(cust.pendingAmount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.SemiBold,
                  )
                }
              }
            }
          }
        }
      }

      // 2. Weight, Purity/Tunch, Rate & Money Inputs
      item {
        ElevatedCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "2. Metal, Tunch & Rate Parameters",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
              if (
                transactionType != TransactionType.MONEY_TO_GOLD &&
                  transactionType != TransactionType.MONEY_TO_SILVER
              ) {
                FilterChip(
                  selected = showMultiItemsBuilder || multiItems.isNotEmpty(),
                  onClick = { showMultiItemsBuilder = !showMultiItemsBuilder },
                  label = {
                    Text(
                      if (multiItems.isNotEmpty()) "Multi-Items (${multiItems.size})"
                      else "Multiple Items"
                    )
                  },
                  leadingIcon = {
                    Icon(
                      Icons.Default.Layers,
                      contentDescription = null,
                      modifier = Modifier.size(16.dp),
                    )
                  },
                  modifier = Modifier.testTag("toggle_multi_items_mode_chip"),
                )
              }
            }

            // Purity Mode Selector for GOLD_PAYMENT and SILVER_PAYMENT
            if (
              transactionType == TransactionType.GOLD_PAYMENT ||
                transactionType == TransactionType.SILVER_PAYMENT
            ) {
              Text(
                text = "Select Purity / Tunch Mode",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
              ) {
                PuritySelectionMode.entries.forEach { mode ->
                  val isSelected = purityMode == mode
                  FilterChip(
                    selected = isSelected,
                    onClick = {
                      purityMode = mode
                      if (mode == PuritySelectionMode.PURE_99) {
                        tunchInput = "99.00"
                      }
                      errorMessage = null
                    },
                    label = {
                      Text(
                        text = mode.displayName,
                        modifier = Modifier.padding(vertical = 6.dp),
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                      )
                    },
                    leadingIcon = {
                      Icon(
                        imageVector =
                          if (mode == PuritySelectionMode.PURE_99) Icons.Default.Verified
                          else Icons.Default.AutoAwesome,
                        contentDescription = mode.displayName,
                        modifier = Modifier.size(18.dp),
                      )
                    },
                    modifier =
                      Modifier.weight(1f).testTag("purity_mode_${mode.name.lowercase()}"),
                  )
                }
              }
            }

            // Multiple Items Builder if toggled
            if (
              (showMultiItemsBuilder || multiItems.isNotEmpty()) &&
                transactionType != TransactionType.MONEY_TO_GOLD &&
                transactionType != TransactionType.MONEY_TO_SILVER
            ) {
              MultiItemsBuilderSection(
                metalType = transactionType.metalType,
                defaultRateInput = rateInput,
                defaultRateUnit = rateUnit,
                items = multiItems,
                multiCalcResult = liveMultiCalc,
                calcEngine = calcEngine,
                onAddItem = { newItem -> multiItems.add(newItem) },
                onRemoveItem = { idx ->
                  if (idx in multiItems.indices) multiItems.removeAt(idx)
                },
                onClearAllItems = {
                  multiItems.clear()
                  showMultiItemsBuilder = false
                },
              )
            }

            when (transactionType) {
              TransactionType.MONEY_TO_GOLD,
              TransactionType.MONEY_TO_SILVER -> {
                OutlinedTextField(
                  value = moneyAmountInput,
                  onValueChange = {
                    moneyAmountInput = it
                    errorMessage = null
                  },
                  label = { Text("Money Amount (₹)") },
                  placeholder = { Text("e.g., 50000") },
                  leadingIcon = {
                    Icon(Icons.Default.CurrencyRupee, contentDescription = "Rupees")
                  },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth().testTag("input_money_amount"),
                )

                RateAndUnitInputs(
                  metalName = transactionType.metalType.displayName,
                  rateInput = rateInput,
                  onRateChange = {
                    rateInput = it
                    errorMessage = null
                  },
                  selectedUnit = rateUnit,
                  onUnitChange = { rateUnit = it },
                )

                OutlinedTextField(
                  value = tunchInput,
                  onValueChange = {
                    tunchInput = it
                    errorMessage = null
                  },
                  label = { Text("Optional Tunch / Purity (%)") },
                  placeholder = { Text("Default 100.00%") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth().testTag("input_tunch"),
                )
              }

              TransactionType.GOLD_PAYMENT,
              TransactionType.SILVER_PAYMENT -> {
                if (multiItems.size <= 1) {
                  OutlinedTextField(
                    value = grossWeightInput,
                    onValueChange = {
                      grossWeightInput = it
                      errorMessage = null
                    },
                    label = { Text("Gross Weight (grams)") },
                    placeholder = { Text("e.g., 10.000") },
                    leadingIcon = { Icon(Icons.Default.Scale, contentDescription = "Weight") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_gross_weight"),
                  )

                  if (purityMode == PuritySelectionMode.PURE_99) {
                    OutlinedTextField(
                      value = tunchInput,
                      onValueChange = {
                        if (businessProfile.allowEditingPure99Purity) {
                          tunchInput = it
                          errorMessage = null
                        }
                      },
                      readOnly = !businessProfile.allowEditingPure99Purity,
                      label = {
                        Text(
                          if (businessProfile.allowEditingPure99Purity)
                            "99% Pure Mode Purity (%) — Editable"
                          else "Purity (99% Fixed)"
                        )
                      },
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                      singleLine = true,
                      modifier = Modifier.fillMaxWidth().testTag("input_tunch"),
                    )
                  } else {
                    OutlinedTextField(
                      value = tunchInput,
                      onValueChange = {
                        tunchInput = it
                        errorMessage = null
                      },
                      label = { Text("Custom Tunch / Purity (%)") },
                      placeholder = { Text(if (isGold) "e.g., 91.60 or 80.00" else "e.g., 92.50 or 70.00") },
                      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                      singleLine = true,
                      modifier = Modifier.fillMaxWidth().testTag("input_tunch"),
                    )
                    QuickTunchChipsRow(
                      metalType = transactionType.metalType,
                      currentTunch = tunchInput,
                      onSelectTunch = { tunchInput = it },
                    )
                  }
                }

                RateAndUnitInputs(
                  metalName = "${transactionType.metalType.displayName} (for valuation)",
                  rateInput = rateInput,
                  onRateChange = {
                    rateInput = it
                    errorMessage = null
                  },
                  selectedUnit = rateUnit,
                  onUnitChange = { rateUnit = it },
                )

                PaymentAdjustmentSection(
                  metalValueFormatted = calcEngine.formatCurrency(livePaymentMetalValue),
                  cashPaidInput = cashPaidInput,
                  onCashPaidChange = { cashPaidInput = it },
                  cashReceivedInput = cashReceivedInput,
                  onCashReceivedChange = { cashReceivedInput = it },
                  remainingBalanceFormatted = calcEngine.formatCurrency(livePaymentAdjustment.remainingBalance),
                )
              }

              TransactionType.SCRAP_GOLD,
              TransactionType.SCRAP_SILVER -> {
                if (multiItems.size <= 1) {
                  OutlinedTextField(
                    value = grossWeightInput,
                    onValueChange = {
                      grossWeightInput = it
                      errorMessage = null
                    },
                    label = { Text("Scrap Gross Weight (grams)") },
                    placeholder = { Text("e.g., 10.000") },
                    leadingIcon = {
                      Icon(Icons.Default.Scale, contentDescription = "Scrap Weight")
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_gross_weight"),
                  )

                  OutlinedTextField(
                    value = tunchInput,
                    onValueChange = {
                      tunchInput = it
                      errorMessage = null
                    },
                    label = { Text("Scrap Tunch / Purity (%)") },
                    placeholder = { Text(if (isGold) "e.g., 80.00" else "e.g., 70.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("input_tunch"),
                  )

                  QuickTunchChipsRow(
                    metalType = transactionType.metalType,
                    currentTunch = tunchInput,
                    onSelectTunch = { tunchInput = it },
                  )
                }

                RateAndUnitInputs(
                  metalName = transactionType.metalType.displayName,
                  rateInput = rateInput,
                  onRateChange = {
                    rateInput = it
                    errorMessage = null
                  },
                  selectedUnit = rateUnit,
                  onUnitChange = { rateUnit = it },
                )

                ScrapDeductionSection(
                  enabledInSettings = businessProfile.enabledDeduction,
                  deductionType = deductionType,
                  onDeductionTypeChange = { deductionType = it },
                  deductionValueInput = deductionValueInput,
                  onDeductionValueChange = { deductionValueInput = it },
                  onEnableInSettingsToggle = onEnableDeductionInSettings,
                )
              }

              TransactionType.GOLD_ADJUSTMENT,
              TransactionType.SILVER_ADJUSTMENT -> {
                OutlinedTextField(
                  value = grossWeightInput,
                  onValueChange = {
                    grossWeightInput = it
                    errorMessage = null
                  },
                  label = { Text("Adjustment Gross Weight (grams)") },
                  placeholder = { Text("e.g., 5.000") },
                  leadingIcon = { Icon(Icons.Default.Scale, contentDescription = "Weight") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth().testTag("input_gross_weight"),
                )

                OutlinedTextField(
                  value = tunchInput,
                  onValueChange = {
                    tunchInput = it
                    errorMessage = null
                  },
                  label = { Text("Adjustment Tunch / Purity (%)") },
                  placeholder = { Text("e.g., 91.60 or 100.00") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.fillMaxWidth().testTag("input_tunch"),
                )

                RateAndUnitInputs(
                  metalName = "${transactionType.metalType.displayName} (Optional Rate)",
                  rateInput = rateInput,
                  onRateChange = {
                    rateInput = it
                    errorMessage = null
                  },
                  selectedUnit = rateUnit,
                  onUnitChange = { rateUnit = it },
                )
              }
            }
          }
        }
      }

      // 3. Prominent Live Calculation Preview Box
      item {
        LiveCalculationPreviewCard(
          transactionType = transactionType,
          purityMode = purityMode,
          grossWeight = parsedGross,
          tunch = parsedTunch,
          fineWeight =
            when {
              liveMultiCalc != null && multiItems.size > 1 -> liveMultiCalc.totalFineWeight
              transactionType == TransactionType.MONEY_TO_GOLD ||
                transactionType == TransactionType.MONEY_TO_SILVER ->
                liveMoneyToMetalCalc.fineWeightGrams
              transactionType == TransactionType.SCRAP_GOLD ||
                transactionType == TransactionType.SCRAP_SILVER -> liveScrapCalc.fineWeight
              else -> livePurityCalc.fineWeight
            },
          rate = parsedRate,
          rateUnit = rateUnit,
          metalValue =
            when {
              liveMultiCalc != null && multiItems.size > 1 -> liveMultiCalc.totalMetalValue
              transactionType == TransactionType.MONEY_TO_GOLD ||
                transactionType == TransactionType.MONEY_TO_SILVER -> parsedMoney
              transactionType == TransactionType.SCRAP_GOLD ||
                transactionType == TransactionType.SCRAP_SILVER -> liveScrapCalc.metalValue
              else -> livePaymentMetalValue
            },
          deductionAmount =
            when {
              liveMultiCalc != null && multiItems.size > 1 -> liveMultiCalc.deductionAmount
              transactionType == TransactionType.SCRAP_GOLD ||
                transactionType == TransactionType.SCRAP_SILVER -> liveScrapCalc.deductionAmount
              else -> BigDecimal.ZERO
            },
          totalAmount =
            when {
              liveMultiCalc != null && multiItems.size > 1 -> liveMultiCalc.netValue
              transactionType == TransactionType.MONEY_TO_GOLD ||
                transactionType == TransactionType.MONEY_TO_SILVER -> parsedMoney
              transactionType == TransactionType.SCRAP_GOLD ||
                transactionType == TransactionType.SCRAP_SILVER -> liveScrapCalc.netValue
              else -> livePaymentMetalValue
            },
          calcEngine = calcEngine,
        )
      }

      // 4. Payment Mode, Invoice Number & Notes
      item {
        ElevatedCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
          ) {
            Text(
              text = "3. Payment Mode & Invoice Details",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )

            Text(
              text = "Payment Mode",
              style = MaterialTheme.typography.labelLarge,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            FlowRow(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              PaymentMode.entries.forEach { mode ->
                val selected = paymentMode == mode
                FilterChip(
                  selected = selected,
                  onClick = { paymentMode = mode },
                  label = { Text(mode.displayName) },
                  leadingIcon =
                    if (selected) {
                      {
                        Icon(
                          imageVector = Icons.Default.CheckCircle,
                          contentDescription = null,
                          modifier = Modifier.size(FilterChipDefaults.IconSize),
                        )
                      }
                    } else null,
                  modifier = Modifier.testTag("payment_mode_${mode.name.lowercase()}"),
                )
              }
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              OutlinedTextField(
                value = invoiceNumberInput,
                onValueChange = {
                  invoiceNumberInput = it
                  errorMessage = null
                },
                label = { Text("Invoice Number") },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("input_invoice_number"),
              )

              OutlinedTextField(
                value = addPendingInput,
                onValueChange = {
                  addPendingInput = it
                  errorMessage = null
                },
                label = { Text("Add Pending Due (₹)") },
                placeholder = { Text("0.00") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("input_pending_due"),
              )
            }

            OutlinedTextField(
              value = notesInput,
              onValueChange = { notesInput = it },
              label = { Text("Notes / Ornament Description (Optional)") },
              placeholder = { Text("e.g., Old necklace exchange, 22K hallmark") },
              modifier = Modifier.fillMaxWidth().testTag("input_transaction_notes"),
              minLines = 2,
            )
          }
        }
      }

      // 5. Save as Draft OR Finalize & Generate Invoice Buttons
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          OutlinedButton(
            onClick = {
              errorMessage = null
              onSubmitTransaction(
                transactionType,
                selectedCustomer,
                purityMode,
                grossWeightInput,
                tunchInput,
                rateInput,
                rateUnit,
                moneyAmountInput,
                paymentMode,
                invoiceNumberInput,
                notesInput,
                addPendingInput,
                deductionType,
                deductionValueInput,
                cashPaidInput,
                cashReceivedInput,
                multiItems.toList(),
                true, // saveAsDraft = true
              ) { err ->
                errorMessage = err
              }
            },
            modifier = Modifier.weight(1f).height(58.dp).testTag("save_draft_transaction_button"),
            shape = RoundedCornerShape(16.dp),
          ) {
            Icon(Icons.Default.EditNote, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "Save Draft",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
          }

          Button(
            onClick = {
              errorMessage = null
              onSubmitTransaction(
                transactionType,
                selectedCustomer,
                purityMode,
                grossWeightInput,
                tunchInput,
                rateInput,
                rateUnit,
                moneyAmountInput,
                paymentMode,
                invoiceNumberInput,
                notesInput,
                addPendingInput,
                deductionType,
                deductionValueInput,
                cashPaidInput,
                cashReceivedInput,
                multiItems.toList(),
                false, // saveAsDraft = false (Finalize!)
              ) { err ->
                errorMessage = err
              }
            },
            modifier = Modifier.weight(1.6f).height(58.dp).testTag("submit_transaction_button"),
            shape = RoundedCornerShape(16.dp),
          ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text =
                if (editingDraft != null) "FINALIZE & GENERATE INVOICE"
                else "SAVE TRANSACTION",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
          }
        }
        Spacer(modifier = Modifier.height(28.dp))
      }
    }
  }

  if (showQuickCustomerDialog) {
    QuickAddCustomerDialog(
      initialCustomer = editingSearchedCustomer,
      prefillMobile = mobileSearchQuery,
      onDismiss = {
        showQuickCustomerDialog = false
        editingSearchedCustomer = null
      },
      onSave = {
        existingId,
        name,
        mobile,
        whatsapp,
        address,
        city,
        state,
        pin,
        pan,
        gst,
        email,
        notes,
        onError ->
        onQuickCreateFullCustomer(
          existingId,
          name,
          mobile,
          whatsapp,
          address,
          city,
          state,
          pin,
          pan,
          gst,
          email,
          notes,
          { created ->
            selectedCustomer = created
            showQuickCustomerDialog = false
            editingSearchedCustomer = null
            onClearMobileSearch()
          },
          onError,
        )
      },
    )
  }
}

@Composable
private fun RateAndUnitInputs(
  metalName: String,
  rateInput: String,
  onRateChange: (String) -> Unit,
  selectedUnit: RateUnit,
  onUnitChange: (RateUnit) -> Unit,
) {
  Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
    OutlinedTextField(
      value = rateInput,
      onValueChange = onRateChange,
      label = { Text("$metalName Rate (₹)") },
      leadingIcon = { Icon(Icons.Default.CurrencyRupee, contentDescription = "Rate") },
      keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
      singleLine = true,
      modifier = Modifier.fillMaxWidth().testTag("input_metal_rate"),
    )

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      RateUnit.entries.forEach { unit ->
        FilterChip(
          selected = selectedUnit == unit,
          onClick = { onUnitChange(unit) },
          label = { Text(unit.displayName) },
          modifier = Modifier.testTag("rate_unit_${unit.name.lowercase()}"),
        )
      }
    }
  }
}

@Composable
private fun LiveCalculationPreviewCard(
  transactionType: TransactionType,
  purityMode: PuritySelectionMode,
  grossWeight: BigDecimal,
  tunch: BigDecimal,
  fineWeight: BigDecimal,
  rate: BigDecimal,
  rateUnit: RateUnit,
  metalValue: BigDecimal = BigDecimal.ZERO,
  deductionAmount: BigDecimal = BigDecimal.ZERO,
  totalAmount: BigDecimal,
  calcEngine: CalculationEngine,
) {
  val isGold = transactionType.metalType == MetalType.GOLD
  val containerColor = if (isGold) GoldContainerLight else SilverContainerLight
  val borderColor = if (isGold) GoldPrimary else SilverPrimary

  Card(
    modifier = Modifier.fillMaxWidth().testTag("live_calculation_preview_card"),
    shape = RoundedCornerShape(18.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = BorderStroke(1.5.dp, borderColor.copy(alpha = 0.5f)),
  ) {
    Column(
      modifier = Modifier.padding(18.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Default.Calculate,
          contentDescription = "Live Calculation",
          tint = borderColor,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
          text = "Live Jewellery Calculation Engine",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = Color(0xFF1F1B13),
        )
      }

      HorizontalDivider(color = borderColor.copy(alpha = 0.25f))

      when (transactionType) {
        TransactionType.MONEY_TO_GOLD,
        TransactionType.MONEY_TO_SILVER -> {
          CalculationRow(
            label = "Money Amount",
            value = calcEngine.formatCurrency(totalAmount),
          )
          CalculationRow(
            label = "${transactionType.metalType.displayName} Rate",
            value = "${calcEngine.formatCurrency(rate)} (${rateUnit.shortLabel})",
          )
          if (tunch < BigDecimal("100.00") && tunch > BigDecimal.ZERO) {
            CalculationRow(
              label = "Tunch / Purity",
              value = "${calcEngine.formatTunch(tunch)}%",
            )
          }
          HorizontalDivider(color = borderColor.copy(alpha = 0.25f))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "${transactionType.metalType.displayName} Quantity:",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1F1B13),
            )
            Text(
              text = "${calcEngine.formatWeight(fineWeight)} g",
              style =
                MaterialTheme.typography.headlineMedium.copy(
                  fontWeight = FontWeight.ExtraBold,
                  fontSize = 26.sp,
                ),
              color = borderColor,
              modifier = Modifier.testTag("calculated_fine_weight_text"),
            )
          }
        }

        else -> {
          CalculationRow(
            label = "Gross Weight",
            value = "${calcEngine.formatWeight(grossWeight)} g",
          )
          CalculationRow(
            label = "Tunch / Purity",
            value =
              if (
                (transactionType == TransactionType.GOLD_PAYMENT ||
                  transactionType == TransactionType.SILVER_PAYMENT) &&
                  purityMode == PuritySelectionMode.PURE_99
              ) {
                "99% Pure (${calcEngine.formatTunch(tunch)}%)"
              } else {
                "${calcEngine.formatTunch(tunch)}%"
              },
          )
          CalculationRow(
            label = "Formula (Gross × Tunch ÷ 100)",
            value = "${calcEngine.formatWeight(fineWeight)} g Fine",
          )
          CalculationRow(
            label = "Applied Rate",
            value = "${calcEngine.formatCurrency(rate)} (${rateUnit.shortLabel})",
          )
          if (deductionAmount > BigDecimal.ZERO) {
            CalculationRow(
              label = "Gross Metal Value",
              value = calcEngine.formatCurrency(metalValue),
            )
            CalculationRow(
              label = "Less: Scrap Deduction",
              value = "- ${calcEngine.formatCurrency(deductionAmount)}",
            )
          }
          HorizontalDivider(color = borderColor.copy(alpha = 0.25f))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column {
              Text(
                text = "Fine Weight",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF4E4637),
              )
              Text(
                text = "${calcEngine.formatWeight(fineWeight)} g",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF1F1B13),
                modifier = Modifier.testTag("calculated_fine_weight_text"),
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(
                text = if (deductionAmount > BigDecimal.ZERO) "Net Payable Value" else "Calculated Metal Value",
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF4E4637),
              )
              Text(
                text = calcEngine.formatCurrency(totalAmount),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.ExtraBold,
                color = borderColor,
                modifier = Modifier.testTag("calculated_amount_text"),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun CalculationRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = Color(0xFF4E4637),
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      color = Color(0xFF1F1B13),
    )
  }
}

@Composable
private fun TransactionSavedSuccessView(
  savedState: SavedTransactionSuccessState,
  calcEngine: CalculationEngine,
  strings: LocalizedStrings,
  onViewInvoice: () -> Unit,
  onEditDraftAgain: () -> Unit,
  onNewTransaction: () -> Unit,
  onGoToDashboard: () -> Unit,
) {
  val tx = savedState.transaction
  val isDraft = tx.status == TransactionStatus.DRAFT
  Box(
    modifier = Modifier.fillMaxSize().padding(16.dp),
    contentAlignment = Alignment.Center,
  ) {
    ElevatedCard(
      modifier = Modifier.fillMaxWidth().widthIn(max = 540.dp).testTag("transaction_saved_card"),
      shape = RoundedCornerShape(24.dp),
    ) {
      Column(
        modifier = Modifier.padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        Surface(
          color = SuccessGreen.copy(alpha = 0.14f),
          shape = RoundedCornerShape(50),
          modifier = Modifier.size(72.dp),
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = if (isDraft) Icons.Default.EditNote else Icons.Default.CheckCircle,
              contentDescription = "Success",
              tint = SuccessGreen,
              modifier = Modifier.size(44.dp),
            )
          }
        }

        Text(
          text = if (isDraft) "Draft Transaction Saved" else strings.transactionSavedSuccessfully,
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          textAlign = TextAlign.Center,
        )

        Surface(
          color = MaterialTheme.colorScheme.secondaryContainer,
          shape = RoundedCornerShape(10.dp),
        ) {
          Text(
            text = savedState.statusMessage,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
          )
        }

        Text(
          text =
            if (isDraft) "Tx ID: ${tx.transactionId} • Status: DRAFT"
            else "Invoice #${tx.invoiceNumber} • ${tx.customerName}",
          style = MaterialTheme.typography.bodyLarge,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Surface(
          color = MaterialTheme.colorScheme.surfaceVariant,
          shape = RoundedCornerShape(14.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Transaction Type", style = MaterialTheme.typography.bodyMedium)
              Text(tx.transactionType.title, fontWeight = FontWeight.Bold)
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Gross Weight", style = MaterialTheme.typography.bodyMedium)
              Text("${calcEngine.formatWeight(tx.grossWeight)} g", fontWeight = FontWeight.SemiBold)
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Fine Weight", style = MaterialTheme.typography.bodyMedium)
              Text("${calcEngine.formatWeight(tx.fineWeight)} g", fontWeight = FontWeight.Bold)
            }
            if (tx.deductionAmount > BigDecimal.ZERO) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Text("Deduction", style = MaterialTheme.typography.bodyMedium)
                Text("- ${calcEngine.formatCurrency(tx.deductionAmount)}", fontWeight = FontWeight.SemiBold)
              }
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Total Net Value", style = MaterialTheme.typography.bodyMedium)
              Text(
                calcEngine.formatCurrency(tx.amount),
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
              )
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text("Status", style = MaterialTheme.typography.bodyMedium)
              Text(tx.status.displayName, fontWeight = FontWeight.SemiBold)
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        if (isDraft) {
          Button(
            onClick = onEditDraftAgain,
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("continue_editing_draft_button"),
            shape = RoundedCornerShape(14.dp),
          ) {
            Icon(Icons.Default.Edit, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Edit / Finalize Draft", fontWeight = FontWeight.Bold)
          }
        } else {
          Button(
            onClick = onViewInvoice,
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("success_view_invoice_button"),
            shape = RoundedCornerShape(14.dp),
          ) {
            Icon(Icons.Default.ReceiptLong, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("GENERATE INVOICE / ${strings.viewInvoice}", fontWeight = FontWeight.Bold)
          }
        }

        OutlinedButton(
          onClick = onNewTransaction,
          modifier = Modifier.fillMaxWidth().height(52.dp).testTag("success_new_tx_button"),
          shape = RoundedCornerShape(14.dp),
        ) {
          Icon(Icons.Default.AddCircleOutline, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(strings.newTransaction, fontWeight = FontWeight.SemiBold)
        }

        TextButton(
          onClick = onGoToDashboard,
          modifier = Modifier.fillMaxWidth().height(48.dp).testTag("success_dashboard_button"),
        ) {
          Icon(Icons.Default.Home, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text(strings.goToDashboard)
        }
      }
    }
  }
}

@Composable
private fun QuickAddCustomerDialog(
  initialCustomer: Customer? = null,
  prefillMobile: String = "",
  onDismiss: () -> Unit,
  onSave:
    (
      existingId: String?,
      name: String,
      mobile: String,
      whatsapp: String,
      address: String,
      city: String,
      state: String,
      pin: String,
      pan: String,
      gst: String,
      email: String,
      notes: String,
      onError: (String) -> Unit,
    ) -> Unit,
) {
  var name by remember(initialCustomer) { mutableStateOf(initialCustomer?.name ?: "") }
  var mobile by remember(initialCustomer, prefillMobile) {
    mutableStateOf(initialCustomer?.mobileNumber ?: prefillMobile)
  }
  var whatsapp by remember(initialCustomer) {
    mutableStateOf(initialCustomer?.whatsappNumber ?: "")
  }
  var address by remember(initialCustomer) { mutableStateOf(initialCustomer?.address ?: "") }
  var city by remember(initialCustomer) { mutableStateOf(initialCustomer?.city ?: "") }
  var state by remember(initialCustomer) { mutableStateOf(initialCustomer?.state ?: "") }
  var pin by remember(initialCustomer) { mutableStateOf(initialCustomer?.pinCode ?: "") }
  var pan by remember(initialCustomer) { mutableStateOf(initialCustomer?.panNumber ?: "") }
  var gst by remember(initialCustomer) { mutableStateOf(initialCustomer?.gstNumber ?: "") }
  var email by remember(initialCustomer) { mutableStateOf(initialCustomer?.email ?: "") }
  var notes by remember(initialCustomer) { mutableStateOf(initialCustomer?.notes ?: "") }
  var error by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text(
        if (initialCustomer == null) "+ Add New Customer" else "Edit Customer",
        fontWeight = FontWeight.Bold,
      )
    },
    text = {
      LazyColumn(
        modifier = Modifier.fillMaxWidth().height(380.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        if (error != null) {
          item {
            Text(
              text = error!!,
              color = MaterialTheme.colorScheme.error,
              style = MaterialTheme.typography.bodySmall,
            )
          }
        }
        item {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Customer Name *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("dialog_customer_name"),
          )
        }
        item {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = mobile,
              onValueChange = { mobile = it },
              label = { Text("Mobile *") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("dialog_customer_mobile"),
            )
            OutlinedTextField(
              value = whatsapp,
              onValueChange = { whatsapp = it },
              label = { Text("WhatsApp") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("dialog_customer_whatsapp"),
            )
          }
        }
        item {
          OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("dialog_customer_address"),
          )
        }
        item {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = city,
              onValueChange = { city = it },
              label = { Text("City") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("dialog_customer_city"),
            )
            OutlinedTextField(
              value = state,
              onValueChange = { state = it },
              label = { Text("State") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("dialog_customer_state"),
            )
            OutlinedTextField(
              value = pin,
              onValueChange = { pin = it },
              label = { Text("PIN") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(0.8f).testTag("dialog_customer_pin"),
            )
          }
        }
        item {
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = pan,
              onValueChange = { pan = it },
              label = { Text("PAN") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("dialog_customer_pan"),
            )
            OutlinedTextField(
              value = gst,
              onValueChange = { gst = it },
              label = { Text("GSTIN (Optional)") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("dialog_customer_gst"),
            )
          }
        }
        item {
          OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("dialog_customer_email"),
          )
        }
        item {
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("Notes") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("dialog_customer_notes"),
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(
            initialCustomer?.id,
            name,
            mobile,
            whatsapp,
            address,
            city,
            state,
            pin,
            pan,
            gst,
            email,
            notes,
          ) { err ->
            error = err
          }
        },
        modifier = Modifier.testTag("dialog_save_customer_button"),
      ) {
        Text("Save Customer")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    },
  )
}
