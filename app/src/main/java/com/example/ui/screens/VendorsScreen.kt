package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import com.example.domain.model.Vendor
import com.example.ui.i18n.AppStrings
import com.example.ui.viewmodel.JewelleryViewModel
import com.example.ui.viewmodel.MainNavTab

@Composable
fun VendorsScreen(viewModel: JewelleryViewModel) {
  val profile by viewModel.businessProfile.collectAsState()
  val strings = AppStrings.forLanguage(profile.language)
  val vendors by viewModel.vendors.collectAsState()
  val purchases by viewModel.purchases.collectAsState()
  val selectedVendor by viewModel.selectedVendorForDetail.collectAsState()

  var searchQuery by remember { mutableStateOf("") }
  var showAddEditVendorDialog by remember { mutableStateOf(false) }
  var editingVendor by remember { mutableStateOf<Vendor?>(null) }

  BackHandler {
    if (selectedVendor != null) {
      viewModel.openVendorDetail(null)
    } else {
      viewModel.selectTab(MainNavTab.DASHBOARD)
    }
  }

  val filteredVendors =
    remember(vendors, searchQuery) {
      val q = searchQuery.trim().lowercase()
      if (q.isEmpty()) vendors
      else
        vendors.filter {
          it.name.lowercase().contains(q) ||
            it.mobile.lowercase().contains(q) ||
            it.companyName.lowercase().contains(q) ||
            it.vendorId.lowercase().contains(q)
        }
    }

  Column(modifier = Modifier.fillMaxSize().testTag("vendors_screen")) {
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
          IconButton(
            onClick = {
              if (selectedVendor != null) viewModel.openVendorDetail(null)
              else viewModel.selectTab(MainNavTab.DASHBOARD)
            },
            modifier = Modifier.testTag("vendors_back_button"),
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backToDashboard)
          }
          Column {
            Text(
              text = selectedVendor?.name ?: strings.vendors,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text =
                if (selectedVendor != null)
                  "${selectedVendor?.vendorId} • ${selectedVendor?.companyName?.ifBlank { strings.vendor }}"
                else "${vendors.size} ${strings.vendors}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        if (selectedVendor == null) {
          Button(
            onClick = {
              editingVendor = null
              showAddEditVendorDialog = true
            },
            modifier = Modifier.testTag("btn_add_vendor"),
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("+ ${strings.vendor}")
          }
        } else {
          OutlinedButton(
            onClick = {
              editingVendor = selectedVendor
              showAddEditVendorDialog = true
            },
            modifier = Modifier.testTag("btn_edit_vendor"),
          ) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Edit")
          }
        }
      }
    }

    val currentVendor = selectedVendor
    if (currentVendor != null) {
      val ledger = remember(currentVendor, purchases) { viewModel.getVendorLedgerSummary(currentVendor) }
      val vendorPurchases =
        remember(currentVendor, purchases) {
          purchases.filter { it.vendorId == currentVendor.vendorId }
        }

      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize(),
      ) {
        item {
          ElevatedCard(
            colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
            modifier = Modifier.fillMaxWidth().testTag("vendor_ledger_card"),
          ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Text(
                text = "${strings.vendorLedger} — ${currentVendor.name}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
              )
              if (currentVendor.companyName.isNotBlank()) {
                Text(currentVendor.companyName, style = MaterialTheme.typography.bodyMedium)
              }
              Text(
                text = "Mobile: ${currentVendor.mobile.ifBlank { "N/A" }} • GST: ${currentVendor.gstNumber.ifBlank { "N/A" }} • PAN: ${currentVendor.panNumber.ifBlank { "N/A" }}",
                style = MaterialTheme.typography.bodySmall,
              )
              HorizontalDivider()
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                  Text("Money Paid", style = MaterialTheme.typography.labelSmall)
                  Text(
                    "₹${ledger.totalMoneyPaid.toPlainString()}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                  )
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text("${strings.pending} (${strings.bakaya})", style = MaterialTheme.typography.labelSmall)
                  Text(
                    "₹${ledger.moneyPending.toPlainString()}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.error,
                  )
                }
              }
              HorizontalDivider()
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                  Text("${strings.gold} Received (Fine)", style = MaterialTheme.typography.labelSmall)
                  Text(
                    "${ledger.goldReceivedFineGrams.toPlainString()} g",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    "Given: ${ledger.goldGivenFineGrams.toPlainString()} g | Net: ${ledger.netGoldBalanceFineGrams.toPlainString()} g",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                  )
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text("${strings.silver} Received (Fine)", style = MaterialTheme.typography.labelSmall)
                  Text(
                    "${ledger.silverReceivedFineGrams.toPlainString()} g",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    "Given: ${ledger.silverGivenFineGrams.toPlainString()} g | Net: ${ledger.netSilverBalanceFineGrams.toPlainString()} g",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                  )
                }
              }
            }
          }
        }

        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "${strings.purchases} (${vendorPurchases.size})",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
            Button(onClick = { viewModel.selectTab(MainNavTab.PURCHASES) }) {
              Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text(strings.newPurchase)
            }
          }
        }

        items(vendorPurchases, key = { it.purchaseId }) { pur ->
          Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${pur.purchaseId} • ${pur.metal.displayName}", fontWeight = FontWeight.Bold)
                Text("₹${pur.totalAmount.toPlainString()}", fontWeight = FontWeight.Bold)
              }
              Text(
                "Gross: ${pur.grossWeight.toPlainString()} g × ${pur.tunch.toPlainString()}% = ${pur.fineWeight.toPlainString()} g fine",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
              )
              Text(
                "Paid: ₹${pur.amountPaid.toPlainString()} • ${strings.pending}: ₹${pur.balancePending.toPlainString()} • ${pur.date}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      }
    } else {
      OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        label = { Text("${strings.search} ${strings.vendors} (Name, Mobile, Company, ID)") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        singleLine = true,
        modifier =
          Modifier.fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .testTag("vendor_search_input"),
      )

      LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.fillMaxSize(),
      ) {
        items(filteredVendors, key = { it.vendorId }) { vendor ->
          val ledger = remember(vendor, purchases) { viewModel.getVendorLedgerSummary(vendor) }
          Card(
            modifier =
              Modifier.fillMaxWidth()
                .clickable { viewModel.openVendorDetail(vendor) }
                .testTag("vendor_card_${vendor.vendorId}"),
          ) {
            Column(modifier = Modifier.padding(14.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = vendor.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                  )
                  if (vendor.companyName.isNotBlank()) {
                    Text(
                      text = vendor.companyName,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.primary,
                    )
                  }
                }
                Surface(
                  color = MaterialTheme.colorScheme.secondaryContainer,
                  shape = RoundedCornerShape(6.dp),
                ) {
                  Text(
                    text = "${strings.pending}: ₹${ledger.moneyPending.toPlainString()}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  )
                }
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "ID: ${vendor.vendorId} • Mobile: ${vendor.mobile} • GST: ${vendor.gstNumber.ifBlank { "N/A" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = "${strings.gold}: ${ledger.goldReceivedFineGrams.toPlainString()} g fine | ${strings.silver}: ${ledger.silverReceivedFineGrams.toPlainString()} g fine",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
              )
            }
          }
        }
      }
    }
  }

  if (showAddEditVendorDialog) {
    var name by remember(editingVendor) { mutableStateOf(editingVendor?.name ?: "") }
    var mobile by remember(editingVendor) { mutableStateOf(editingVendor?.mobile ?: "") }
    var company by remember(editingVendor) { mutableStateOf(editingVendor?.companyName ?: "") }
    var address by remember(editingVendor) { mutableStateOf(editingVendor?.address ?: "") }
    var gst by remember(editingVendor) { mutableStateOf(editingVendor?.gstNumber ?: "") }
    var pan by remember(editingVendor) { mutableStateOf(editingVendor?.panNumber ?: "") }
    var pendingMoney by remember(editingVendor) {
      mutableStateOf(editingVendor?.pendingMoney?.toPlainString() ?: "0.00")
    }
    var notes by remember(editingVendor) { mutableStateOf(editingVendor?.notes ?: "") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    AlertDialog(
      onDismissRequest = { showAddEditVendorDialog = false },
      title = { Text(if (editingVendor == null) "+ ${strings.vendor}" else "Edit ${strings.vendor}") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          errorMsg?.let {
            Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
          }
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("${strings.vendor} Name *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_vendor_name"),
          )
          OutlinedTextField(
            value = mobile,
            onValueChange = { mobile = it },
            label = { Text(strings.mobileNumber) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_vendor_mobile"),
          )
          OutlinedTextField(
            value = company,
            onValueChange = { company = it },
            label = { Text("Company / Firm Name") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_vendor_company"),
          )
          OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Address") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = gst,
              onValueChange = { gst = it },
              label = { Text("GSTIN") },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
              value = pan,
              onValueChange = { pan = it },
              label = { Text("PAN") },
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
          }
          OutlinedTextField(
            value = pendingMoney,
            onValueChange = { pendingMoney = it },
            label = { Text("Opening ${strings.pending} (₹)") },
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
            viewModel.saveVendor(
              existingId = editingVendor?.vendorId,
              name = name,
              mobile = mobile,
              companyName = company,
              address = address,
              gstNumber = gst,
              panNumber = pan,
              pendingMoneyStr = pendingMoney,
              notes = notes,
              onError = { errorMsg = it },
              onSuccess = { showAddEditVendorDialog = false },
            )
          },
          modifier = Modifier.testTag("btn_save_vendor"),
        ) {
          Text(strings.save)
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddEditVendorDialog = false }) { Text(strings.cancel) }
      },
    )
  }
}
