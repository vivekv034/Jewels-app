package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.domain.model.Customer
import com.example.domain.model.MetalType
import com.example.domain.model.PaymentMode
import com.example.domain.model.RateUnit
import com.example.domain.model.TransactionStatus
import com.example.domain.model.Vendor
import com.example.ui.i18n.AppStrings
import com.example.ui.viewmodel.JewelleryViewModel
import com.example.ui.viewmodel.MainNavTab
import java.math.BigDecimal
import java.math.RoundingMode

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun PurchasesScreen(viewModel: JewelleryViewModel) {
  val profile by viewModel.businessProfile.collectAsState()
  val strings = AppStrings.forLanguage(profile.language)
  val vendors by viewModel.vendors.collectAsState()
  val purchases by viewModel.purchases.collectAsState()
  val rates by viewModel.metalRate.collectAsState()

  var showNewPurchaseForm by remember { mutableStateOf(false) }
  var selectedVendor by remember { mutableStateOf<Vendor?>(vendors.firstOrNull()) }
  var supplierFallbackName by remember { mutableStateOf("") }
  var supplierFallbackMobile by remember { mutableStateOf("") }
  var selectedMetal by remember { mutableStateOf(MetalType.GOLD) }
  var description by remember { mutableStateOf("Gold Bullion Bar Purchase") }
  var grossWeightStr by remember { mutableStateOf("20.000") }
  var tunchStr by remember { mutableStateOf("99.50") }
  var rateStr by remember(selectedMetal) {
    mutableStateOf(
      if (selectedMetal == MetalType.GOLD) rates.goldRatePerGram.toPlainString()
      else rates.silverRatePerGram.toPlainString()
    )
  }
  var gstPercentStr by remember {
    mutableStateOf(if (profile.gstEnabled) profile.defaultGstRatePercent.toPlainString() else "0.00")
  }
  var amountPaidStr by remember { mutableStateOf("") }
  var paymentMode by remember { mutableStateOf(PaymentMode.BANK_TRANSFER) }
  var notes by remember { mutableStateOf("") }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  val engine = viewModel.getCalculationEngine()
  val fineWeight =
    remember(grossWeightStr, tunchStr) {
      val g = grossWeightStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
      val t = tunchStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
      engine.calculateFineWeight(g, t)
    }
  val metalValue =
    remember(fineWeight, rateStr) {
      val r = rateStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
      engine.calculateMetalValue(fineWeight, r)
    }
  val gstAmount =
    remember(metalValue, gstPercentStr) {
      val gst = gstPercentStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
      if (gst > BigDecimal.ZERO) metalValue.multiply(gst).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
      else BigDecimal.ZERO
    }
  val totalAmount = remember(metalValue, gstAmount) { metalValue.add(gstAmount).setScale(2, RoundingMode.HALF_UP) }

  BackHandler { viewModel.selectTab(MainNavTab.DASHBOARD) }

  Column(modifier = Modifier.fillMaxSize().testTag("purchases_screen")) {
    Surface(
      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = { viewModel.selectTab(MainNavTab.DASHBOARD) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backToDashboard)
          }
          Column {
            Text(
              text = strings.purchases,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "${purchases.size} ${strings.purchases} • Bullion & Metal Inflow",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Button(
          onClick = { showNewPurchaseForm = !showNewPurchaseForm },
          modifier = Modifier.testTag("btn_toggle_new_purchase"),
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (showNewPurchaseForm) strings.cancel else strings.newPurchase)
        }
      }
    }

    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxSize(),
    ) {
      if (showNewPurchaseForm) {
        item {
          ElevatedCard(
            modifier = Modifier.fillMaxWidth().testTag("new_purchase_form_card"),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text(
                text = strings.newPurchase,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
              errorMsg?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
              }

              Text("Select ${strings.vendor}:", style = MaterialTheme.typography.labelMedium)
              FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                vendors.forEach { v ->
                  FilterChip(
                    selected = selectedVendor?.vendorId == v.vendorId,
                    onClick = { selectedVendor = v },
                    label = { Text(v.name) },
                  )
                }
              }

              if (selectedVendor == null) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  OutlinedTextField(
                    value = supplierFallbackName,
                    onValueChange = { supplierFallbackName = it },
                    label = { Text("Supplier Name") },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                  )
                  OutlinedTextField(
                    value = supplierFallbackMobile,
                    onValueChange = { supplierFallbackMobile = it },
                    label = { Text(strings.mobileNumber) },
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                  )
                }
              }

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                  selected = selectedMetal == MetalType.GOLD,
                  onClick = {
                    selectedMetal = MetalType.GOLD
                    rateStr = rates.goldRatePerGram.toPlainString()
                    description = "Gold Bullion Purchase"
                  },
                  label = { Text(strings.gold) },
                  modifier = Modifier.testTag("chip_purchase_gold"),
                )
                FilterChip(
                  selected = selectedMetal == MetalType.SILVER,
                  onClick = {
                    selectedMetal = MetalType.SILVER
                    rateStr = rates.silverRatePerGram.toPlainString()
                    description = "Silver Bullion Purchase"
                  },
                  label = { Text(strings.silver) },
                  modifier = Modifier.testTag("chip_purchase_silver"),
                )
              }

              OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Item / Bullion Description") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
              )

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = grossWeightStr,
                  onValueChange = { grossWeightStr = it },
                  label = { Text("${strings.grossWeight} (g)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.weight(1f).testTag("input_purchase_gross"),
                )
                OutlinedTextField(
                  value = tunchStr,
                  onValueChange = { tunchStr = it },
                  label = { Text("${strings.tunch} (%)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.weight(1f).testTag("input_purchase_tunch"),
                )
              }

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = rateStr,
                  onValueChange = { rateStr = it },
                  label = { Text("${strings.rate} (₹/g)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.weight(1f).testTag("input_purchase_rate"),
                )
                OutlinedTextField(
                  value = gstPercentStr,
                  onValueChange = { gstPercentStr = it },
                  label = { Text("GST (%)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.weight(1f),
                )
              }

              Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(
                    text = "${strings.fineWeight}: ${fineWeight.toPlainString()} g fine",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                  )
                  Text(
                    text = "Metal Value: ₹${metalValue.toPlainString()} + GST: ₹${gstAmount.toPlainString()} = Total: ₹${totalAmount.toPlainString()}",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                  )
                }
              }

              OutlinedTextField(
                value = amountPaidStr,
                onValueChange = { amountPaidStr = it },
                label = { Text("Amount Paid Now (₹) — Leave blank if Paid in Full") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("input_purchase_paid"),
              )

              OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(strings.notes) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
              )

              Button(
                onClick = {
                  viewModel.savePurchase(
                    vendor = selectedVendor,
                    supplierNameFallback = supplierFallbackName,
                    supplierMobileFallback = supplierFallbackMobile,
                    metal = selectedMetal,
                    description = description,
                    grossWeightStr = grossWeightStr,
                    tunchStr = tunchStr,
                    rateStr = rateStr,
                    rateUnit = RateUnit.PER_1_GRAM,
                    gstPercentStr = gstPercentStr,
                    amountPaidStr = amountPaidStr.ifBlank { totalAmount.toPlainString() },
                    paymentMode = paymentMode,
                    notes = notes,
                    onError = { errorMsg = it },
                    onSuccess = { showNewPurchaseForm = false },
                  )
                },
                modifier = Modifier.fillMaxWidth().testTag("btn_save_purchase"),
              ) {
                Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("${strings.save} ${strings.purchase}")
              }
            }
          }
        }
      }

      items(purchases, key = { it.purchaseId }) { pur ->
        val isCancelled = pur.status == TransactionStatus.CANCELLED
        Card(
          modifier = Modifier.fillMaxWidth().testTag("purchase_card_${pur.purchaseId}"),
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                Text(
                  text = "${pur.purchaseId} • ${pur.vendorName}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "${pur.metal.displayName} • ${pur.description} • ${pur.date}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Text(
                text = "₹${pur.totalAmount.toPlainString()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (isCancelled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Gross: ${pur.grossWeight.toPlainString()} g × Tunch: ${pur.tunch.toPlainString()}% = Fine: ${pur.fineWeight.toPlainString()} g",
              style = MaterialTheme.typography.bodySmall,
              fontFamily = FontFamily.Monospace,
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Paid: ₹${pur.amountPaid.toPlainString()} • ${strings.pending}: ₹${pur.balancePending.toPlainString()} • ${pur.status.name}",
                style = MaterialTheme.typography.bodySmall,
              )
              if (!isCancelled) {
                TextButton(
                  onClick = { viewModel.cancelPurchase(pur.purchaseId, "Cancelled from Purchases screen") },
                  modifier = Modifier.testTag("btn_cancel_purchase_${pur.purchaseId}"),
                ) {
                  Text(strings.cancelTransaction, color = MaterialTheme.colorScheme.error)
                }
              }
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SalesScreen(viewModel: JewelleryViewModel) {
  val profile by viewModel.businessProfile.collectAsState()
  val strings = AppStrings.forLanguage(profile.language)
  val customers by viewModel.customers.collectAsState()
  val sales by viewModel.sales.collectAsState()
  val rates by viewModel.metalRate.collectAsState()

  var showNewSaleForm by remember { mutableStateOf(false) }
  var selectedCustomer by remember { mutableStateOf<Customer?>(customers.firstOrNull()) }
  var customerFallbackName by remember { mutableStateOf("") }
  var customerFallbackMobile by remember { mutableStateOf("") }
  var selectedMetal by remember { mutableStateOf(MetalType.GOLD) }
  var description by remember { mutableStateOf("22K Gold Ornament Sale") }
  var grossWeightStr by remember { mutableStateOf("10.000") }
  var tunchStr by remember { mutableStateOf("91.60") }
  var rateStr by remember(selectedMetal) {
    mutableStateOf(
      if (selectedMetal == MetalType.GOLD) rates.goldRatePerGram.toPlainString()
      else rates.silverRatePerGram.toPlainString()
    )
  }
  var makingChargesStr by remember { mutableStateOf("1500.00") }
  var gstPercentStr by remember {
    mutableStateOf(if (profile.gstEnabled) profile.defaultGstRatePercent.toPlainString() else "0.00")
  }
  var isInterStateGst by remember { mutableStateOf(false) }
  var amountReceivedStr by remember { mutableStateOf("") }
  var paymentMode by remember { mutableStateOf(PaymentMode.UPI) }
  var notes by remember { mutableStateOf("") }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  val engine = viewModel.getCalculationEngine()
  val fineWeight =
    remember(grossWeightStr, tunchStr) {
      val g = grossWeightStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
      val t = tunchStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
      engine.calculateFineWeight(g, t)
    }
  val metalValue =
    remember(fineWeight, rateStr) {
      val r = rateStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
      engine.calculateMetalValue(fineWeight, r)
    }
  val makingCharges = remember(makingChargesStr) {
    (makingChargesStr.toBigDecimalOrNull() ?: BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP)
  }
  val gstAmount =
    remember(metalValue, makingCharges, gstPercentStr) {
      val gst = gstPercentStr.toBigDecimalOrNull() ?: BigDecimal.ZERO
      if (gst > BigDecimal.ZERO) {
        metalValue.add(makingCharges).multiply(gst).divide(BigDecimal("100"), 2, RoundingMode.HALF_UP)
      } else BigDecimal.ZERO
    }
  val totalAmount =
    remember(metalValue, makingCharges, gstAmount) {
      metalValue.add(makingCharges).add(gstAmount).setScale(2, RoundingMode.HALF_UP)
    }

  BackHandler { viewModel.selectTab(MainNavTab.DASHBOARD) }

  Column(modifier = Modifier.fillMaxSize().testTag("sales_screen")) {
    Surface(
      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = { viewModel.selectTab(MainNavTab.DASHBOARD) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backToDashboard)
          }
          Column {
            Text(
              text = strings.sales,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "${sales.size} ${strings.sales} • Jewellery & Bullion Outflow",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        Button(
          onClick = { showNewSaleForm = !showNewSaleForm },
          modifier = Modifier.testTag("btn_toggle_new_sale"),
        ) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (showNewSaleForm) strings.cancel else strings.newSale)
        }
      }
    }

    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
      modifier = Modifier.fillMaxSize(),
    ) {
      if (showNewSaleForm) {
        item {
          ElevatedCard(
            modifier = Modifier.fillMaxWidth().testTag("new_sale_form_card"),
          ) {
            Column(
              modifier = Modifier.padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              Text(
                text = strings.newSale,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
              errorMsg?.let {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
              }

              Text("Select ${strings.customer}:", style = MaterialTheme.typography.labelMedium)
              FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                customers.forEach { c ->
                  FilterChip(
                    selected = selectedCustomer?.id == c.id,
                    onClick = { selectedCustomer = c },
                    label = { Text(c.name) },
                  )
                }
              }

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                  selected = selectedMetal == MetalType.GOLD,
                  onClick = {
                    selectedMetal = MetalType.GOLD
                    rateStr = rates.goldRatePerGram.toPlainString()
                    description = "22K Gold Ornament Sale"
                  },
                  label = { Text(strings.gold) },
                  modifier = Modifier.testTag("chip_sale_gold"),
                )
                FilterChip(
                  selected = selectedMetal == MetalType.SILVER,
                  onClick = {
                    selectedMetal = MetalType.SILVER
                    rateStr = rates.silverRatePerGram.toPlainString()
                    description = "Silver Jewellery Sale"
                  },
                  label = { Text(strings.silver) },
                  modifier = Modifier.testTag("chip_sale_silver"),
                )
              }

              OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Jewellery Item Description") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
              )

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = grossWeightStr,
                  onValueChange = { grossWeightStr = it },
                  label = { Text("${strings.grossWeight} (g)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.weight(1f).testTag("input_sale_gross"),
                )
                OutlinedTextField(
                  value = tunchStr,
                  onValueChange = { tunchStr = it },
                  label = { Text("${strings.tunch} (%)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.weight(1f).testTag("input_sale_tunch"),
                )
              }

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                  value = rateStr,
                  onValueChange = { rateStr = it },
                  label = { Text("${strings.rate} (₹/g)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.weight(1f).testTag("input_sale_rate"),
                )
                OutlinedTextField(
                  value = makingChargesStr,
                  onValueChange = { makingChargesStr = it },
                  label = { Text("${strings.makingCharges} (₹)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.weight(1f).testTag("input_sale_making"),
                )
              }

              Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
              ) {
                OutlinedTextField(
                  value = gstPercentStr,
                  onValueChange = { gstPercentStr = it },
                  label = { Text("GST (%)") },
                  keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                  singleLine = true,
                  modifier = Modifier.weight(1f),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Checkbox(checked = isInterStateGst, onCheckedChange = { isInterStateGst = it })
                  Text("IGST (Inter-state)", style = MaterialTheme.typography.bodySmall)
                }
              }

              Surface(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Text(
                    text = "${strings.fineWeight}: ${fineWeight.toPlainString()} g fine",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                  )
                  Text(
                    text = "Metal: ₹${metalValue.toPlainString()} + Making: ₹${makingCharges.toPlainString()} + GST: ₹${gstAmount.toPlainString()}",
                    style = MaterialTheme.typography.bodySmall,
                  )
                  Text(
                    text = "${strings.total}: ₹${totalAmount.toPlainString()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                  )
                }
              }

              OutlinedTextField(
                value = amountReceivedStr,
                onValueChange = { amountReceivedStr = it },
                label = { Text("Amount Received Now (₹) — Blank if Received in Full") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag("input_sale_received"),
              )

              OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text(strings.notes) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
              )

              Button(
                onClick = {
                  viewModel.saveSale(
                    customer = selectedCustomer,
                    customerNameFallback = customerFallbackName,
                    customerMobileFallback = customerFallbackMobile,
                    metal = selectedMetal,
                    description = description,
                    grossWeightStr = grossWeightStr,
                    tunchStr = tunchStr,
                    rateStr = rateStr,
                    rateUnit = RateUnit.PER_1_GRAM,
                    makingChargesStr = makingChargesStr,
                    gstPercentStr = gstPercentStr,
                    isInterStateGst = isInterStateGst,
                    amountReceivedStr = amountReceivedStr.ifBlank { totalAmount.toPlainString() },
                    paymentMode = paymentMode,
                    notes = notes,
                    onError = { errorMsg = it },
                    onSuccess = { showNewSaleForm = false },
                  )
                },
                modifier = Modifier.fillMaxWidth().testTag("btn_save_sale"),
              ) {
                Icon(Icons.Default.ShoppingBag, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("${strings.save} ${strings.sale}")
              }
            }
          }
        }
      }

      items(sales, key = { it.saleId }) { sale ->
        val isCancelled = sale.status == TransactionStatus.CANCELLED
        val totalGst = sale.cgstAmount.add(sale.sgstAmount).add(sale.igstAmount)
        Card(
          modifier = Modifier.fillMaxWidth().testTag("sale_card_${sale.saleId}"),
        ) {
          Column(modifier = Modifier.padding(14.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column {
                Text(
                  text = "${sale.saleId} (${sale.invoiceNumber}) • ${sale.customerName}",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "${sale.metal.displayName} • ${sale.description} • ${sale.date}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }
              Text(
                text = "₹${sale.totalAmount.toPlainString()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = if (isCancelled) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "Gross: ${sale.grossWeight.toPlainString()} g × Tunch: ${sale.tunch.toPlainString()}% = Fine: ${sale.fineWeight.toPlainString()} g",
              style = MaterialTheme.typography.bodySmall,
              fontFamily = FontFamily.Monospace,
            )
            Text(
              text = "Making: ₹${sale.makingCharges.toPlainString()} • GST: ₹${totalGst.toPlainString()} • Received: ₹${sale.amountReceived.toPlainString()} • ${strings.pending}: ₹${sale.balancePending.toPlainString()}",
              style = MaterialTheme.typography.bodySmall,
            )
            if (!isCancelled) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                TextButton(
                  onClick = { viewModel.cancelSale(sale.saleId, "Cancelled from Sales screen") },
                  modifier = Modifier.testTag("btn_cancel_sale_${sale.saleId}"),
                ) {
                  Text(strings.cancelTransaction, color = MaterialTheme.colorScheme.error)
                }
              }
            }
          }
        }
      }
    }
  }
}
