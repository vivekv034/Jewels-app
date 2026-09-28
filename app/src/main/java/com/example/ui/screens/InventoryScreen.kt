package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Balance
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.model.InventoryMovement
import com.example.domain.model.InventoryMovementDirection
import com.example.domain.model.InventoryMovementType
import com.example.domain.model.MetalInventorySummary
import com.example.domain.model.MetalType
import com.example.ui.i18n.AppStrings
import com.example.ui.viewmodel.JewelleryViewModel
import com.example.ui.viewmodel.MainNavTab
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InventoryScreen(viewModel: JewelleryViewModel) {
  val profile by viewModel.businessProfile.collectAsState()
  val strings = AppStrings.forLanguage(profile.language)
  val goldSummary by viewModel.goldInventorySummary.collectAsState()
  val silverSummary by viewModel.silverInventorySummary.collectAsState()
  val movements by viewModel.inventoryMovements.collectAsState()
  val openingStocks by viewModel.openingStocks.collectAsState()
  val scrapProcesses by viewModel.scrapProcesses.collectAsState()
  val reconciliations by viewModel.reconciliations.collectAsState()
  val rates by viewModel.metalRate.collectAsState()

  var selectedTabIndex by remember { mutableIntStateOf(0) }
  var movementMetalFilter by remember { mutableStateOf<MetalType?>(null) }

  // Dialog states
  var showOpeningDialogForMetal by remember { mutableStateOf<MetalType?>(null) }
  var showAdjustmentDialogForMetal by remember { mutableStateOf<MetalType?>(null) }
  var showScrapProcessDialogForMetal by remember { mutableStateOf<MetalType?>(null) }
  var showReconcileDialogForMetal by remember { mutableStateOf<MetalType?>(null) }

  BackHandler { viewModel.selectTab(MainNavTab.DASHBOARD) }

  val tabs =
    listOf(
      strings.goldInventory,
      strings.silverInventory,
      strings.goldLedger,
      strings.silverLedger,
      strings.movementHistory,
      strings.scrapProcessing,
      strings.stockReconciliation,
    )

  Column(modifier = Modifier.fillMaxSize().testTag("inventory_screen")) {
    // Top Header Bar
    Surface(
      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
              onClick = { viewModel.selectTab(MainNavTab.DASHBOARD) },
              modifier = Modifier.testTag("inventory_back_button"),
            ) {
              Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backToDashboard)
            }
            Column {
              Text(
                text = strings.inventory,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = "${strings.gold}: ${goldSummary.currentFineWeight.toPlainString()} g fine • ${strings.silver}: ${silverSummary.currentFineWeight.toPlainString()} g fine",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Quick Action Chips
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          AssistChip(
            onClick = { showAdjustmentDialogForMetal = MetalType.GOLD },
            label = { Text("+/- ${strings.gold} ${strings.stockAdjustment}") },
            leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.testTag("btn_gold_adjustment"),
          )
          AssistChip(
            onClick = { showAdjustmentDialogForMetal = MetalType.SILVER },
            label = { Text("+/- ${strings.silver} ${strings.stockAdjustment}") },
            leadingIcon = { Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.testTag("btn_silver_adjustment"),
          )
          AssistChip(
            onClick = { showScrapProcessDialogForMetal = MetalType.GOLD },
            label = { Text(strings.scrapProcessing) },
            leadingIcon = { Icon(Icons.Default.Recycling, contentDescription = null, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.testTag("btn_scrap_processing"),
          )
          AssistChip(
            onClick = { showReconcileDialogForMetal = MetalType.GOLD },
            label = { Text(strings.stockReconciliation) },
            leadingIcon = { Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(16.dp)) },
            modifier = Modifier.testTag("btn_stock_reconciliation"),
          )
        }
      }
    }

    ScrollableTabRow(
      selectedTabIndex = selectedTabIndex,
      edgePadding = 12.dp,
      modifier = Modifier.fillMaxWidth(),
    ) {
      tabs.forEachIndexed { index, title ->
        Tab(
          selected = selectedTabIndex == index,
          onClick = { selectedTabIndex = index },
          text = { Text(title, fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal) },
          modifier = Modifier.testTag("inventory_tab_$index"),
        )
      }
    }

    when (selectedTabIndex) {
      0 -> {
        MetalInventoryDetailView(
          summary = goldSummary,
          strings = strings,
          onEditOpening = { showOpeningDialogForMetal = MetalType.GOLD },
          onAddAdjustment = { showAdjustmentDialogForMetal = MetalType.GOLD },
          onProcessScrap = { showScrapProcessDialogForMetal = MetalType.GOLD },
          onReconcile = { showReconcileDialogForMetal = MetalType.GOLD },
        )
      }
      1 -> {
        MetalInventoryDetailView(
          summary = silverSummary,
          strings = strings,
          onEditOpening = { showOpeningDialogForMetal = MetalType.SILVER },
          onAddAdjustment = { showAdjustmentDialogForMetal = MetalType.SILVER },
          onProcessScrap = { showScrapProcessDialogForMetal = MetalType.SILVER },
          onReconcile = { showReconcileDialogForMetal = MetalType.SILVER },
        )
      }
      2 -> {
        val goldEntries = remember(openingStocks, movements) { viewModel.getGoldLedgerEntries() }
        MetalLedgerView(
          metal = MetalType.GOLD,
          summary = goldSummary,
          entries = goldEntries,
          strings = strings,
        )
      }
      3 -> {
        val silverEntries = remember(openingStocks, movements) { viewModel.getSilverLedgerEntries() }
        MetalLedgerView(
          metal = MetalType.SILVER,
          summary = silverSummary,
          entries = silverEntries,
          strings = strings,
        )
      }
      4 -> {
        val allHistory =
          remember(openingStocks, movements, movementMetalFilter) {
            viewModel.inventoryService.getInventoryHistory(
              openingStocks = openingStocks,
              movements = movements,
              metalFilter = movementMetalFilter,
            )
          }
        MovementHistoryView(
          entries = allHistory,
          selectedMetal = movementMetalFilter,
          onSelectMetal = { movementMetalFilter = it },
          strings = strings,
        )
      }
      5 -> {
        ScrapProcessingListView(
          records = scrapProcesses,
          strings = strings,
          onNewGoldScrapProcess = { showScrapProcessDialogForMetal = MetalType.GOLD },
          onNewSilverScrapProcess = { showScrapProcessDialogForMetal = MetalType.SILVER },
        )
      }
      6 -> {
        ReconciliationListView(
          records = reconciliations,
          strings = strings,
          onReconcileGold = { showReconcileDialogForMetal = MetalType.GOLD },
          onReconcileSilver = { showReconcileDialogForMetal = MetalType.SILVER },
        )
      }
    }
  }

  // 1. Opening Stock Configuration Dialog
  showOpeningDialogForMetal?.let { metal ->
    val existing = openingStocks.find { it.metal == metal }
    var grossStr by remember(metal) { mutableStateOf(existing?.grossWeight?.toPlainString() ?: "100.000") }
    var tunchStr by remember(metal) { mutableStateOf(existing?.tunch?.toPlainString() ?: "99.50") }
    var rateStr by remember(metal) {
      mutableStateOf(
        existing?.referenceRatePerGram?.toPlainString()
          ?: if (metal == MetalType.GOLD) rates.goldRatePerGram.toPlainString()
          else rates.silverRatePerGram.toPlainString()
      )
    }
    var notes by remember(metal) { mutableStateOf(existing?.notes ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val calcFine =
      remember(grossStr, tunchStr) {
        val g = grossStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val t = tunchStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
        viewModel.getCalculationEngine().calculateFineWeight(g, t)
      }

    AlertDialog(
      onDismissRequest = { showOpeningDialogForMetal = null },
      title = { Text("${strings.openingStock} — ${metal.displayName}") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          errorMsg?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }
          OutlinedTextField(
            value = grossStr,
            onValueChange = { grossStr = it },
            label = { Text("${strings.grossWeight} (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_opening_gross"),
          )
          OutlinedTextField(
            value = tunchStr,
            onValueChange = { tunchStr = it },
            label = { Text("${strings.tunch} (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_opening_tunch"),
          )
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = "${strings.fineWeight}: ${calcFine.toPlainString()} g fine",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(10.dp),
            )
          }
          OutlinedTextField(
            value = rateStr,
            onValueChange = { rateStr = it },
            label = { Text("${strings.rate} (₹/g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(strings.notes) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.saveOpeningStock(
              metal = metal,
              grossWeightStr = grossStr,
              tunchStr = tunchStr,
              rateStr = rateStr,
              notes = notes,
              onError = { errorMsg = it },
              onSuccess = { showOpeningDialogForMetal = null },
            )
          },
          modifier = Modifier.testTag("btn_save_opening_stock"),
        ) {
          Text(strings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showOpeningDialogForMetal = null }) { Text(strings.cancel) }
      },
    )
  }

  // 2. Manual Inventory Adjustment Dialog
  showAdjustmentDialogForMetal?.let { initialMetal ->
    var selectedMetal by remember { mutableStateOf(initialMetal) }
    var direction by remember { mutableStateOf(InventoryMovementDirection.IN) }
    var grossStr by remember { mutableStateOf("") }
    var tunchStr by remember { mutableStateOf("99.50") }
    var rateStr by remember(selectedMetal) {
      mutableStateOf(
        if (selectedMetal == MetalType.GOLD) rates.goldRatePerGram.toPlainString()
        else rates.silverRatePerGram.toPlainString()
      )
    }
    var reason by remember { mutableStateOf("Stock verification adjustment") }
    var notes by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val calcFine =
      remember(grossStr, tunchStr) {
        val g = grossStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val t = tunchStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
        viewModel.getCalculationEngine().calculateFineWeight(g, t)
      }

    AlertDialog(
      onDismissRequest = { showAdjustmentDialogForMetal = null },
      title = { Text("${strings.stockAdjustment} (${selectedMetal.displayName})") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          errorMsg?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = selectedMetal == MetalType.GOLD,
              onClick = { selectedMetal = MetalType.GOLD },
              label = { Text(strings.gold) },
            )
            FilterChip(
              selected = selectedMetal == MetalType.SILVER,
              onClick = { selectedMetal = MetalType.SILVER },
              label = { Text(strings.silver) },
            )
          }
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = direction == InventoryMovementDirection.IN,
              onClick = { direction = InventoryMovementDirection.IN },
              label = { Text("IN (+ Add Stock)") },
              modifier = Modifier.testTag("chip_adjust_in"),
            )
            FilterChip(
              selected = direction == InventoryMovementDirection.OUT,
              onClick = { direction = InventoryMovementDirection.OUT },
              label = { Text("OUT (- Reduce Stock)") },
              modifier = Modifier.testTag("chip_adjust_out"),
            )
          }
          OutlinedTextField(
            value = grossStr,
            onValueChange = { grossStr = it },
            label = { Text("${strings.grossWeight} (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_adjust_gross"),
          )
          OutlinedTextField(
            value = tunchStr,
            onValueChange = { tunchStr = it },
            label = { Text("${strings.tunch} (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_adjust_tunch"),
          )
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = "${strings.fineWeight}: ${calcFine.toPlainString()} g fine",
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(10.dp),
            )
          }
          OutlinedTextField(
            value = reason,
            onValueChange = { reason = it },
            label = { Text("Adjustment Reason *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_adjust_reason"),
          )
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(strings.notes) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.recordManualInventoryAdjustment(
              metal = selectedMetal,
              direction = direction,
              grossWeightStr = grossStr,
              tunchStr = tunchStr,
              rateStr = rateStr,
              reason = reason,
              notes = notes,
              onError = { errorMsg = it },
              onSuccess = { showAdjustmentDialogForMetal = null },
            )
          },
          modifier = Modifier.testTag("btn_confirm_adjustment"),
        ) {
          Text(strings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAdjustmentDialogForMetal = null }) { Text(strings.cancel) }
      },
    )
  }

  // 3. Scrap Processing / Recovery Dialog
  showScrapProcessDialogForMetal?.let { initialMetal ->
    var selectedMetal by remember { mutableStateOf(initialMetal) }
    var scrapGrossStr by remember { mutableStateOf("10.000") }
    var scrapFineStr by remember { mutableStateOf("7.500") }
    var meltedGrossStr by remember { mutableStateOf("7.450") }
    var outputTunchStr by remember { mutableStateOf("99.50") }
    var notes by remember { mutableStateOf("Refining & melting recovery") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val outputFine =
      remember(meltedGrossStr, outputTunchStr) {
        val m = meltedGrossStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val t = outputTunchStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
        viewModel.getCalculationEngine().calculateFineWeight(m, t)
      }
    val meltingLoss =
      remember(scrapGrossStr, meltedGrossStr) {
        val s = scrapGrossStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val m = meltedGrossStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
        s.subtract(m).max(BigDecimal.ZERO).setScale(3, RoundingMode.HALF_UP)
      }

    AlertDialog(
      onDismissRequest = { showScrapProcessDialogForMetal = null },
      title = { Text("${strings.scrapProcessing} (${selectedMetal.displayName})") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          errorMsg?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = selectedMetal == MetalType.GOLD,
              onClick = { selectedMetal = MetalType.GOLD },
              label = { Text(strings.gold) },
            )
            FilterChip(
              selected = selectedMetal == MetalType.SILVER,
              onClick = { selectedMetal = MetalType.SILVER },
              label = { Text(strings.silver) },
            )
          }
          OutlinedTextField(
            value = scrapGrossStr,
            onValueChange = { scrapGrossStr = it },
            label = { Text("Scrap Gross Weight (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_scrap_gross"),
          )
          OutlinedTextField(
            value = scrapFineStr,
            onValueChange = { scrapFineStr = it },
            label = { Text("Scrap Fine Weight (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_scrap_fine"),
          )
          OutlinedTextField(
            value = meltedGrossStr,
            onValueChange = { meltedGrossStr = it },
            label = { Text("Melted Output Weight (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_melted_gross"),
          )
          OutlinedTextField(
            value = outputTunchStr,
            onValueChange = { outputTunchStr = it },
            label = { Text("Output Tunch (%)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_output_tunch"),
          )
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = "Output Fine Weight: ${outputFine.toPlainString()} g",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
              )
              Text(
                text = "Melting Loss: ${meltingLoss.toPlainString()} g gross",
                style = MaterialTheme.typography.bodySmall,
              )
            }
          }
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(strings.notes) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.processScrapMetal(
              metal = selectedMetal,
              scrapGrossWeightStr = scrapGrossStr,
              scrapFineWeightStr = scrapFineStr,
              meltedGrossWeightStr = meltedGrossStr,
              outputTunchStr = outputTunchStr,
              rateStr = "",
              notes = notes,
              onError = { errorMsg = it },
              onSuccess = { showScrapProcessDialogForMetal = null },
            )
          },
          modifier = Modifier.testTag("btn_confirm_scrap_process"),
        ) {
          Text(strings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showScrapProcessDialogForMetal = null }) { Text(strings.cancel) }
      },
    )
  }

  // 4. Stock Reconciliation Dialog
  showReconcileDialogForMetal?.let { initialMetal ->
    var selectedMetal by remember { mutableStateOf(initialMetal) }
    val sysSummary = if (selectedMetal == MetalType.GOLD) goldSummary else silverSummary
    var physGrossStr by remember(selectedMetal) { mutableStateOf(sysSummary.currentGrossWeight.toPlainString()) }
    var physFineStr by remember(selectedMetal) { mutableStateOf(sysSummary.currentFineWeight.toPlainString()) }
    var notes by remember { mutableStateOf("Physical vault verification") }
    var createAdjustment by remember { mutableStateOf(true) }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    val diffGross =
      remember(physGrossStr, sysSummary) {
        (physGrossStr.toBigDecimalOrNull() ?: BigDecimal.ZERO)
          .subtract(sysSummary.currentGrossWeight)
          .setScale(3, RoundingMode.HALF_UP)
      }
    val diffFine =
      remember(physFineStr, sysSummary) {
        (physFineStr.toBigDecimalOrNull() ?: BigDecimal.ZERO)
          .subtract(sysSummary.currentFineWeight)
          .setScale(3, RoundingMode.HALF_UP)
      }

    AlertDialog(
      onDismissRequest = { showReconcileDialogForMetal = null },
      title = { Text("${strings.stockReconciliation} (${selectedMetal.displayName})") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          errorMsg?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = selectedMetal == MetalType.GOLD,
              onClick = { selectedMetal = MetalType.GOLD },
              label = { Text(strings.gold) },
            )
            FilterChip(
              selected = selectedMetal == MetalType.SILVER,
              onClick = { selectedMetal = MetalType.SILVER },
              label = { Text(strings.silver) },
            )
          }
          Surface(
            color = MaterialTheme.colorScheme.surfaceVariant,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text("System Stock:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
              Text(
                "${sysSummary.currentGrossWeight.toPlainString()} g gross • ${sysSummary.currentFineWeight.toPlainString()} g fine",
                style = MaterialTheme.typography.bodyMedium,
              )
            }
          }
          OutlinedTextField(
            value = physGrossStr,
            onValueChange = { physGrossStr = it },
            label = { Text("Physical Gross Weight (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_reconcile_gross"),
          )
          OutlinedTextField(
            value = physFineStr,
            onValueChange = { physFineStr = it },
            label = { Text("Physical Fine Weight (g)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_reconcile_fine"),
          )
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Column(modifier = Modifier.padding(10.dp)) {
              Text(
                text = "Difference: ${if (diffGross >= BigDecimal.ZERO) "+" else ""}${diffGross.toPlainString()} g gross | ${if (diffFine >= BigDecimal.ZERO) "+" else ""}${diffFine.toPlainString()} g fine",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium,
              )
            }
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = createAdjustment, onCheckedChange = { createAdjustment = it })
            Text("Auto-create adjustment movement for difference", style = MaterialTheme.typography.bodySmall)
          }
          OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text(strings.notes) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            viewModel.reconcileStock(
              metal = selectedMetal,
              physicalGrossWeightStr = physGrossStr,
              physicalFineWeightStr = physFineStr,
              notes = notes,
              createAdjustmentMovement = createAdjustment,
              onError = { errorMsg = it },
              onSuccess = { showReconcileDialogForMetal = null },
            )
          },
          modifier = Modifier.testTag("btn_confirm_reconciliation"),
        ) {
          Text(strings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showReconcileDialogForMetal = null }) { Text(strings.cancel) }
      },
    )
  }
}

@Composable
private fun MetalInventoryDetailView(
  summary: MetalInventorySummary,
  strings: com.example.ui.i18n.LocalizedStrings,
  onEditOpening: () -> Unit,
  onAddAdjustment: () -> Unit,
  onProcessScrap: () -> Unit,
  onReconcile: () -> Unit,
) {
  val metalLabel = if (summary.metal == MetalType.GOLD) strings.gold else strings.silver

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxSize(),
  ) {
    item {
      ElevatedCard(
        colors =
          CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
          ),
        modifier = Modifier.fillMaxWidth().testTag("card_current_stock_${summary.metal.name}"),
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "${strings.currentStock} — $metalLabel",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
            Icon(Icons.Default.Inventory2, contentDescription = null)
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "${summary.currentFineWeight.toPlainString()} g fine",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            fontFamily = FontFamily.Monospace,
          )
          Text(
            text = "${summary.currentGrossWeight.toPlainString()} g gross",
            style = MaterialTheme.typography.titleSmall,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
          )
          Spacer(modifier = Modifier.height(8.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Column {
              Text("Reference Rate", style = MaterialTheme.typography.labelSmall)
              Text(
                "₹${summary.averageReferenceRatePerGram.toPlainString()}/g",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
              )
            }
            Column(horizontalAlignment = Alignment.End) {
              Text(strings.estimatedStockValue, style = MaterialTheme.typography.labelSmall)
              Text(
                "₹${summary.estimatedValue.toPlainString()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
              )
            }
          }
        }
      }
    }

    item {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "$metalLabel Stock Breakdown (Gross & Fine)",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
            TextButton(onClick = onEditOpening, modifier = Modifier.testTag("btn_edit_opening_${summary.metal.name}")) {
              Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(4.dp))
              Text(strings.openingStock)
            }
          }

          StockBreakdownRow(
            label = "Opening $metalLabel",
            gross = summary.openingGrossWeight,
            fine = summary.openingFineWeight,
            badge = "OPENING",
          )
          StockBreakdownRow(
            label = "$metalLabel Purchased (Vendors)",
            gross = summary.purchasedGrossWeight,
            fine = summary.purchasedFineWeight,
            badge = "+ IN",
          )
          StockBreakdownRow(
            label = "$metalLabel Received from Customers",
            gross = summary.receivedFromCustomersGrossWeight,
            fine = summary.receivedFromCustomersFineWeight,
            badge = "+ IN",
          )
          StockBreakdownRow(
            label = "$metalLabel Scrap Received",
            gross = summary.scrapReceivedGrossWeight,
            fine = summary.scrapReceivedFineWeight,
            badge = "+ IN",
          )
          StockBreakdownRow(
            label = "$metalLabel Added by Adjustment",
            gross = summary.addedByAdjustmentGrossWeight,
            fine = summary.addedByAdjustmentFineWeight,
            badge = "+ IN",
          )
          HorizontalDivider()
          StockBreakdownRow(
            label = "$metalLabel Used / Sold",
            gross = summary.usedOrSoldGrossWeight,
            fine = summary.usedOrSoldFineWeight,
            badge = "- OUT",
            isOut = true,
          )
          StockBreakdownRow(
            label = "$metalLabel Given to Customers",
            gross = summary.givenToCustomersGrossWeight,
            fine = summary.givenToCustomersFineWeight,
            badge = "- OUT",
            isOut = true,
          )
          StockBreakdownRow(
            label = "$metalLabel Scrap Processed",
            gross = summary.scrapProcessedGrossWeight,
            fine = summary.scrapProcessedFineWeight,
            badge = "REFINED",
          )
          StockBreakdownRow(
            label = "$metalLabel Net Adjustment",
            gross = summary.netAdjustmentGrossWeight,
            fine = summary.netAdjustmentFineWeight,
            badge = "NET ADJ",
          )
          HorizontalDivider()
          StockBreakdownRow(
            label = "Current $metalLabel Balance",
            gross = summary.currentGrossWeight,
            fine = summary.currentFineWeight,
            badge = "BALANCE",
            highlight = true,
          )
        }
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedButton(onClick = onAddAdjustment, modifier = Modifier.weight(1f)) {
          Text(strings.stockAdjustment)
        }
        OutlinedButton(onClick = onProcessScrap, modifier = Modifier.weight(1f)) {
          Text(strings.scrapProcessing)
        }
        OutlinedButton(onClick = onReconcile, modifier = Modifier.weight(1f)) {
          Text(strings.stockReconciliation)
        }
      }
    }
  }
}

@Composable
private fun StockBreakdownRow(
  label: String,
  gross: BigDecimal,
  fine: BigDecimal,
  badge: String,
  isOut: Boolean = false,
  highlight: Boolean = false,
) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = label,
        style = if (highlight) MaterialTheme.typography.titleSmall else MaterialTheme.typography.bodyMedium,
        fontWeight = if (highlight) FontWeight.Bold else FontWeight.Medium,
      )
      Text(
        text = badge,
        style = MaterialTheme.typography.labelSmall,
        color =
          if (isOut) MaterialTheme.colorScheme.error
          else MaterialTheme.colorScheme.primary,
      )
    }
    Column(horizontalAlignment = Alignment.End) {
      Text(
        text = "${gross.toPlainString()} g gross",
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
      )
      Text(
        text = "${fine.toPlainString()} g fine",
        style = MaterialTheme.typography.bodyMedium,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace,
        color =
          when {
            highlight -> MaterialTheme.colorScheme.primary
            isOut -> MaterialTheme.colorScheme.error
            else -> MaterialTheme.colorScheme.onSurface
          },
      )
    }
  }
}

@Composable
private fun MetalLedgerView(
  metal: MetalType,
  summary: MetalInventorySummary,
  entries: List<InventoryMovement>,
  strings: com.example.ui.i18n.LocalizedStrings,
) {
  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(8.dp),
    modifier = Modifier.fillMaxSize(),
  ) {
    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth(),
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Text(
            text = "${metal.displayName} Running Ledger (${entries.size} entries)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Closing Balance: ${summary.currentGrossWeight.toPlainString()} g gross | ${summary.currentFineWeight.toPlainString()} g fine",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
          )
        }
      }
    }

    items(entries, key = { it.movementId }) { mov ->
      MovementRowCard(mov)
    }
  }
}

@Composable
private fun MovementHistoryView(
  entries: List<InventoryMovement>,
  selectedMetal: MetalType?,
  onSelectMetal: (MetalType?) -> Unit,
  strings: com.example.ui.i18n.LocalizedStrings,
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      FilterChip(
        selected = selectedMetal == null,
        onClick = { onSelectMetal(null) },
        label = { Text("All Metals") },
      )
      FilterChip(
        selected = selectedMetal == MetalType.GOLD,
        onClick = { onSelectMetal(MetalType.GOLD) },
        label = { Text(strings.gold) },
      )
      FilterChip(
        selected = selectedMetal == MetalType.SILVER,
        onClick = { onSelectMetal(MetalType.SILVER) },
        label = { Text(strings.silver) },
      )
    }

    LazyColumn(
      contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      modifier = Modifier.fillMaxSize(),
    ) {
      items(entries, key = { it.movementId }) { mov ->
        MovementRowCard(mov)
      }
    }
  }
}

@Composable
private fun MovementRowCard(mov: InventoryMovement) {
  val isOut = mov.direction == InventoryMovementDirection.OUT
  val sign = if (isOut) "-" else "+"
  Card(
    modifier = Modifier.fillMaxWidth().testTag("movement_card_${mov.movementId}"),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "${mov.metal.displayName} • ${mov.movementType.displayName}",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.Bold,
        )
        Surface(
          color =
            if (isOut) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer,
          shape = RoundedCornerShape(6.dp),
        ) {
          Text(
            text = "${mov.direction.name} $sign${mov.fineWeight.toPlainString()} g fine",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Gross: ${mov.grossWeight.toPlainString()} g × Tunch: ${mov.tunch.toPlainString()}% = Fine: ${mov.fineWeight.toPlainString()} g",
        style = MaterialTheme.typography.bodySmall,
        fontFamily = FontFamily.Monospace,
      )
      Text(
        text = "ID: ${mov.movementId} • Date: ${mov.date}${if (mov.invoiceNumber.isNotBlank()) " • Inv: ${mov.invoiceNumber}" else ""}${if (mov.transactionId.isNotBlank()) " • Ref: ${mov.transactionId}" else ""}",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
      if (mov.notes.isNotBlank()) {
        Text(
          text = mov.notes,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }
    }
  }
}

@Composable
private fun ScrapProcessingListView(
  records: List<com.example.domain.model.ScrapProcessRecord>,
  strings: com.example.ui.i18n.LocalizedStrings,
  onNewGoldScrapProcess: () -> Unit,
  onNewSilverScrapProcess: () -> Unit,
) {
  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    modifier = Modifier.fillMaxSize(),
  ) {
    item {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(onClick = onNewGoldScrapProcess, modifier = Modifier.weight(1f)) {
          Icon(Icons.Default.Recycling, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Process Gold Scrap")
        }
        OutlinedButton(onClick = onNewSilverScrapProcess, modifier = Modifier.weight(1f)) {
          Text("Process Silver Scrap")
        }
      }
    }

    if (records.isEmpty()) {
      item {
        Card(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "No scrap refining records yet. Use 'Process Gold/Silver Scrap' to convert scrap metal into melted refined bullion.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
          )
        }
      }
    } else {
      items(records, key = { it.processId }) { rec ->
        Card(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              "${rec.metal.displayName} Scrap Refined (${rec.processId}) • ${rec.date}",
              fontWeight = FontWeight.Bold,
            )
            Text(
              "Scrap Input: ${rec.scrapGrossWeight.toPlainString()} g gross (${rec.scrapFineWeight.toPlainString()} g fine)",
              style = MaterialTheme.typography.bodySmall,
              fontFamily = FontFamily.Monospace,
            )
            Text(
              "Melted Output: ${rec.meltedGrossWeight.toPlainString()} g @ ${rec.outputTunch.toPlainString()}% = ${rec.outputFineWeight.toPlainString()} g fine",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.SemiBold,
              fontFamily = FontFamily.Monospace,
            )
            Text(
              "Melting Loss: ${rec.meltingLossGrossWeight.toPlainString()} g gross",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.error,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ReconciliationListView(
  records: List<com.example.domain.model.StockReconciliationRecord>,
  strings: com.example.ui.i18n.LocalizedStrings,
  onReconcileGold: () -> Unit,
  onReconcileSilver: () -> Unit,
) {
  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    modifier = Modifier.fillMaxSize(),
  ) {
    item {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        Button(onClick = onReconcileGold, modifier = Modifier.weight(1f)) {
          Icon(Icons.Default.FactCheck, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text("Reconcile Gold")
        }
        OutlinedButton(onClick = onReconcileSilver, modifier = Modifier.weight(1f)) {
          Text("Reconcile Silver")
        }
      }
    }

    if (records.isEmpty()) {
      item {
        Card(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "No physical stock reconciliations recorded yet. Compare system weight with physical weight anytime.",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium,
          )
        }
      }
    } else {
      items(records, key = { it.reconciliationId }) { rec ->
        Card(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(12.dp)) {
            Text(
              "${rec.metal.displayName} Reconciliation (${rec.reconciliationId}) • ${rec.date}",
              fontWeight = FontWeight.Bold,
            )
            Text(
              "System: ${rec.systemGrossWeight.toPlainString()} g gross | ${rec.systemFineWeight.toPlainString()} g fine",
              style = MaterialTheme.typography.bodySmall,
              fontFamily = FontFamily.Monospace,
            )
            Text(
              "Physical: ${rec.physicalGrossWeight.toPlainString()} g gross | ${rec.physicalFineWeight.toPlainString()} g fine",
              style = MaterialTheme.typography.bodySmall,
              fontFamily = FontFamily.Monospace,
            )
            Text(
              "Difference: ${rec.differenceGrossWeight.toPlainString()} g gross | ${rec.differenceFineWeight.toPlainString()} g fine",
              style = MaterialTheme.typography.bodySmall,
              fontWeight = FontWeight.Bold,
              fontFamily = FontFamily.Monospace,
            )
          }
        }
      }
    }
  }
}
