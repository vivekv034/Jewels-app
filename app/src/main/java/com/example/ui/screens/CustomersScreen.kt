package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.calculation.CalculationEngine
import com.example.domain.model.Customer
import com.example.domain.model.CustomerLedgerSummary
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionStatus
import com.example.ui.theme.GoldContainerLight
import com.example.ui.theme.GoldPrimary
import com.example.ui.theme.SilverContainerLight
import com.example.ui.theme.SilverPrimary
import com.example.ui.viewmodel.MobileCustomerSearchResult
import java.math.BigDecimal

@Composable
fun CustomersScreen(
  customers: List<Customer>,
  calcEngine: CalculationEngine,
  mobileSearchResult: MobileCustomerSearchResult? = null,
  selectedCustomerForHistory: Customer? = null,
  onSelectCustomerForHistory: (Customer?) -> Unit = {},
  onGetCustomerLedger: (String, String) -> CustomerLedgerSummary = { id, name ->
    CustomerLedgerSummary(customerId = id, customerName = name)
  },
  onOpenDraftForEdit: (Transaction) -> Unit = {},
  onCancelTransaction: (String, String) -> Unit = { _, _ -> },
  onSearchByMobileInCloud: (String) -> Unit = {},
  onClearMobileSearch: () -> Unit = {},
  onSaveCustomer:
    (
      existingId: String?,
      name: String,
      mobile: String,
      address: String,
      pan: String,
      gst: String,
      pendingAmount: String,
      notes: String,
      onSuccess: (Customer) -> Unit,
      onError: (String) -> Unit,
    ) -> Unit = { _, _, _, _, _, _, _, _, _, _ -> },
  onSaveFullCustomer:
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
      pendingAmount: String,
      notes: String,
      onSuccess: (Customer) -> Unit,
      onError: (String) -> Unit,
    ) -> Unit = { id, name, mobile, _, addr, _, _, _, pan, gst, _, pending, notes, onSucc, onErr ->
      onSaveCustomer(id, name, mobile, addr, pan, gst, pending, notes, onSucc, onErr)
    },
  onDeleteCustomer: (String) -> Unit,
  onBackToDashboard: () -> Unit,
) {
  var searchQuery by remember { mutableStateOf("") }
  var editingCustomer by remember { mutableStateOf<Customer?>(null) }
  var isCreatingNew by remember { mutableStateOf(false) }

  if (selectedCustomerForHistory != null) {
    BackHandler { onSelectCustomerForHistory(null) }
    val ledgerSummary =
      onGetCustomerLedger(selectedCustomerForHistory.id, selectedCustomerForHistory.name)
    CustomerTransactionHistoryView(
      customer = selectedCustomerForHistory,
      ledger = ledgerSummary,
      calcEngine = calcEngine,
      onBack = { onSelectCustomerForHistory(null) },
      onOpenDraftForEdit = onOpenDraftForEdit,
      onCancelTransaction = onCancelTransaction,
    )
    return
  }

  if (isCreatingNew || editingCustomer != null) {
    BackHandler {
      isCreatingNew = false
      editingCustomer = null
    }
    CustomerFormView(
      existingCustomer = editingCustomer,
      onCancel = {
        isCreatingNew = false
        editingCustomer = null
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
        pending,
        notes,
        onError ->
        onSaveFullCustomer(
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
          pending,
          notes,
          {
            isCreatingNew = false
            editingCustomer = null
          },
          onError,
        )
      },
    )
    return
  }

  BackHandler { onBackToDashboard() }

  val filteredCustomers =
    remember(customers, searchQuery) {
      if (searchQuery.isBlank()) {
        customers
      } else {
        val q = searchQuery.trim().lowercase()
        customers.filter {
          it.name.lowercase().contains(q) ||
            it.mobileNumber.lowercase().contains(q) ||
            it.address.lowercase().contains(q) ||
            it.panNumber.lowercase().contains(q)
        }
      }
    }

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 840.dp).testTag("customers_list"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Customers Directory",
              style = MaterialTheme.typography.headlineMedium,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "${customers.size} registered customers • Tap Ledger for full Gold/Silver history",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Button(
            onClick = {
              editingCustomer = null
              isCreatingNew = true
            },
            modifier = Modifier.height(48.dp).testTag("add_new_customer_fab"),
            shape = RoundedCornerShape(14.dp),
          ) {
            Icon(Icons.Default.PersonAdd, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add Customer", fontWeight = FontWeight.Bold)
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          OutlinedTextField(
            value = searchQuery,
            onValueChange = {
              searchQuery = it
              if (it.isBlank()) onClearMobileSearch()
            },
            label = { Text("Search by Name, Mobile, PAN or City") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            trailingIcon =
              if (searchQuery.isNotEmpty()) {
                {
                  IconButton(
                    onClick = {
                      searchQuery = ""
                      onClearMobileSearch()
                    }
                  ) {
                    Icon(Icons.Default.Close, contentDescription = "Clear search")
                  }
                }
              } else null,
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.weight(1f).testTag("customer_search_input"),
          )
          OutlinedButton(
            onClick = { onSearchByMobileInCloud(searchQuery) },
            modifier = Modifier.height(54.dp).testTag("customer_cloud_mobile_search_button"),
            shape = RoundedCornerShape(14.dp),
          ) {
            Text("Search Mobile")
          }
        }
      }

      if (mobileSearchResult != null) {
        item {
          Surface(
            color = MaterialTheme.colorScheme.secondaryContainer,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("customer_mobile_lookup_banner"),
          ) {
            val found = mobileSearchResult.customer
            if (found != null) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "Mobile Match (${mobileSearchResult.source}): ${found.name}",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    text =
                      "Mobile: ${found.mobileNumber} • PAN: ${found.panNumber.ifBlank { "—" }} • GST: ${found.gstNumber.ifBlank { "Optional" }}",
                    style = MaterialTheme.typography.bodySmall,
                  )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  OutlinedButton(
                    onClick = { onSelectCustomerForHistory(found) },
                    modifier = Modifier.testTag("mobile_lookup_ledger_button"),
                  ) {
                    Text("Ledger")
                  }
                  TextButton(
                    onClick = { editingCustomer = found },
                    modifier = Modifier.testTag("mobile_lookup_edit_customer_button"),
                  ) {
                    Text("Edit")
                  }
                }
              }
            } else {
              Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(
                  text = "No customer found in Local Cache or Google Sheets for '${mobileSearchResult.queryMobile}'.",
                  style = MaterialTheme.typography.bodySmall,
                )
                TextButton(
                  onClick = {
                    editingCustomer = null
                    isCreatingNew = true
                  }
                ) {
                  Text("Add New")
                }
              }
            }
          }
        }
      }

      if (filteredCustomers.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
            shape = RoundedCornerShape(16.dp),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text =
                  if (customers.isEmpty()) "No customers added yet."
                  else "No matching customers found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
              )
              Text(
                text = "Tap 'Add Customer' to register a new customer.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }
      } else {
        items(filteredCustomers, key = { it.id }) { customer ->
          CustomerDirectoryCard(
            customer = customer,
            calcEngine = calcEngine,
            onViewLedger = { onSelectCustomerForHistory(customer) },
            onEdit = { editingCustomer = customer },
            onDelete = { onDeleteCustomer(customer.id) },
          )
        }
      }

      item { Spacer(modifier = Modifier.height(24.dp)) }
    }
  }
}

@Composable
private fun CustomerDirectoryCard(
  customer: Customer,
  calcEngine: CalculationEngine,
  onViewLedger: () -> Unit,
  onEdit: () -> Unit,
  onDelete: () -> Unit,
) {
  ElevatedCard(
    modifier = Modifier.fillMaxWidth().testTag("customer_card_${customer.id}"),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f),
        ) {
          Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.size(46.dp),
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = Icons.Default.Person,
                contentDescription = customer.name,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
              )
            }
          }
          Spacer(modifier = Modifier.width(12.dp))
          Column {
            Text(
              text = customer.name,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Mobile",
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = customer.mobileNumber,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          OutlinedButton(
            onClick = onViewLedger,
            modifier = Modifier.testTag("view_customer_ledger_${customer.id}"),
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
          ) {
            Icon(
              imageVector = Icons.Default.History,
              contentDescription = "Ledger History",
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Ledger", style = MaterialTheme.typography.labelMedium)
          }
          IconButton(
            onClick = onEdit,
            modifier = Modifier.size(44.dp).testTag("edit_customer_${customer.id}"),
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit ${customer.name}",
              tint = MaterialTheme.colorScheme.primary,
            )
          }
          IconButton(
            onClick = onDelete,
            modifier = Modifier.size(44.dp).testTag("delete_customer_${customer.id}"),
          ) {
            Icon(
              imageVector = Icons.Default.DeleteOutline,
              contentDescription = "Delete ${customer.name}",
              tint = MaterialTheme.colorScheme.error,
            )
          }
        }
      }

      if (customer.formattedFullAddress.isNotBlank()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Address",
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = customer.formattedFullAddress,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text =
              buildString {
                append("PAN: ${customer.panNumber.ifBlank { "—" }}")
                append("  •  GST: ${customer.gstNumber.ifBlank { "Optional / None" }}")
              },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          if (customer.notes.isNotBlank()) {
            Text(
              text = "Note: ${customer.notes}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }

        Surface(
          color =
            if (customer.pendingAmount > BigDecimal.ZERO) {
              MaterialTheme.colorScheme.errorContainer
            } else {
              MaterialTheme.colorScheme.secondaryContainer
            },
          shape = RoundedCornerShape(8.dp),
        ) {
          Text(
            text =
              if (customer.pendingAmount > BigDecimal.ZERO) {
                "Pending: ${calcEngine.formatCurrency(customer.pendingAmount)}"
              } else {
                "Cleared (₹0)"
              },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color =
              if (customer.pendingAmount > BigDecimal.ZERO) {
                MaterialTheme.colorScheme.onErrorContainer
              } else {
                MaterialTheme.colorScheme.onSecondaryContainer
              },
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CustomerTransactionHistoryView(
  customer: Customer,
  ledger: CustomerLedgerSummary,
  calcEngine: CalculationEngine,
  onBack: () -> Unit,
  onOpenDraftForEdit: (Transaction) -> Unit,
  onCancelTransaction: (String, String) -> Unit,
) {
  var txToCancel by remember { mutableStateOf<Transaction?>(null) }
  var cancellationReason by remember { mutableStateOf("") }

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 840.dp).testTag("customer_ledger_screen"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp).testTag("back_from_customer_ledger_button"),
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to Customers")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "${customer.name} — Transaction History & Ledger",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "Mobile: ${customer.mobileNumber} • PAN: ${customer.panNumber.ifBlank { "—" }}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }

      // Gold, Silver & Money Summary Cards
      item {
        Card(
          modifier = Modifier.fillMaxWidth().testTag("customer_ledger_summary_card"),
          shape = RoundedCornerShape(18.dp),
          colors = CardDefaults.cardColors(containerColor = GoldContainerLight),
          border = BorderStroke(1.dp, GoldPrimary.copy(alpha = 0.45f)),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(
              text = "Gold, Silver & Money Ledger Summary (Active Transactions)",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF1F1B13),
            )
            HorizontalDivider(color = GoldPrimary.copy(alpha = 0.25f))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              LedgerMetricItem(
                label = "Gold Given",
                value = "${calcEngine.formatWeight(ledger.goldGivenGrams)} g",
                modifier = Modifier.weight(1f),
              )
              LedgerMetricItem(
                label = "Gold Received",
                value = "${calcEngine.formatWeight(ledger.goldReceivedGrams)} g",
                modifier = Modifier.weight(1f),
              )
              LedgerMetricItem(
                label = "Scrap Gold",
                value = "${calcEngine.formatWeight(ledger.scrapGoldGrams)} g",
                modifier = Modifier.weight(1f),
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              LedgerMetricItem(
                label = "Silver Given",
                value = "${calcEngine.formatWeight(ledger.silverGivenGrams)} g",
                modifier = Modifier.weight(1f),
              )
              LedgerMetricItem(
                label = "Silver Received",
                value = "${calcEngine.formatWeight(ledger.silverReceivedGrams)} g",
                modifier = Modifier.weight(1f),
              )
              LedgerMetricItem(
                label = "Scrap Silver",
                value = "${calcEngine.formatWeight(ledger.scrapSilverGrams)} g",
                modifier = Modifier.weight(1f),
              )
            }

            HorizontalDivider(color = GoldPrimary.copy(alpha = 0.25f))

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              LedgerMetricItem(
                label = "Money Received",
                value = calcEngine.formatCurrency(ledger.moneyReceived),
                modifier = Modifier.weight(1f),
              )
              LedgerMetricItem(
                label = "Net Gold Balance",
                value = "${calcEngine.formatWeight(ledger.netGoldBalanceGrams)} g",
                highlight = true,
                modifier = Modifier.weight(1f),
              )
              LedgerMetricItem(
                label = "Net Silver Balance",
                value = "${calcEngine.formatWeight(ledger.netSilverBalanceGrams)} g",
                highlight = true,
                modifier = Modifier.weight(1f),
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "Net Money Balance (Ledger):",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F1B13),
              )
              Text(
                text = calcEngine.formatCurrency(ledger.netMoneyBalance),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                color = GoldPrimary,
                modifier = Modifier.testTag("customer_net_money_balance"),
              )
            }
          }
        }
      }

      // Stage 7 Section 13: Customer Ledger Debit / Credit / Running Balance Statement
      if (ledger.entries.isNotEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth().testTag("customer_ledger_statement_table"),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Text(
                text = "Separate Money, Gold & Silver Ledger Statement",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
              )
              Text(
                text = "Date • Invoice/Tx No • Description • Money Dr/Cr • Gold Dr/Cr • Silver Dr/Cr • Running Balance",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              HorizontalDivider()

              var runMoney = BigDecimal.ZERO
              var runGold = BigDecimal.ZERO
              var runSilver = BigDecimal.ZERO

              ledger.entries
                .filter { !it.isCancelled }
                .sortedBy { it.timestamp }
                .forEach { entry ->
                  runMoney = runMoney.add(entry.moneyCredit).subtract(entry.moneyDebit)
                  runGold = runGold.add(entry.goldCredit).subtract(entry.goldDebit)
                  runSilver = runSilver.add(entry.silverCredit).subtract(entry.silverDebit)

                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth().testTag("ledger_entry_row_${entry.transactionId}"),
                  ) {
                    Column(
                      modifier = Modifier.padding(10.dp),
                      verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                      ) {
                        Text(
                          text = "${entry.date} • ${entry.invoiceNumber.ifBlank { entry.transactionId }}",
                          style = MaterialTheme.typography.labelMedium,
                          fontWeight = FontWeight.Bold,
                        )
                        Text(
                          text = entry.description.ifBlank { entry.typeLabel },
                          style = MaterialTheme.typography.labelSmall,
                          color = MaterialTheme.colorScheme.primary,
                          fontWeight = FontWeight.SemiBold,
                        )
                      }
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                      ) {
                        Text(
                          text = "Money Dr: ${calcEngine.formatCurrency(entry.moneyDebit)} | Cr: ${calcEngine.formatCurrency(entry.moneyCredit)}",
                          style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                          text = "Gold Dr: ${calcEngine.formatWeight(entry.goldDebit)}g | Cr: ${calcEngine.formatWeight(entry.goldCredit)}g",
                          style = MaterialTheme.typography.bodySmall,
                        )
                      }
                      Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                      ) {
                        Text(
                          text = "Silver Dr: ${calcEngine.formatWeight(entry.silverDebit)}g | Cr: ${calcEngine.formatWeight(entry.silverCredit)}g",
                          style = MaterialTheme.typography.bodySmall,
                        )
                        Text(
                          text = "Run Bal: ${calcEngine.formatCurrency(runMoney)} | Au ${calcEngine.formatWeight(runGold)}g | Ag ${calcEngine.formatWeight(runSilver)}g",
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.secondary,
                        )
                      }
                    }
                  }
                }
            }
          }
        }
      }

      item {
        Text(
          text = "All Customer Transactions (${ledger.transactions.size})",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
      }

      if (ledger.transactions.isEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
          ) {
            Text(
              text = "No transactions recorded for ${customer.name} yet.",
              style = MaterialTheme.typography.bodyMedium,
              modifier = Modifier.padding(20.dp),
            )
          }
        }
      } else {
        items(ledger.transactions, key = { it.transactionId }) { tx ->
          val isCancelled = tx.status == TransactionStatus.CANCELLED
          val isDraft = tx.status == TransactionStatus.DRAFT
          ElevatedCard(
            modifier = Modifier.fillMaxWidth().testTag("ledger_tx_card_${tx.transactionId}"),
            shape = RoundedCornerShape(14.dp),
          ) {
            Column(
              modifier = Modifier.padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "${tx.transactionType.title} (${tx.metalType.displayName})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    text = "${tx.date} ${tx.time} • ID: ${tx.transactionId} • Inv: ${tx.invoiceNumber}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Surface(
                  color =
                    when (tx.status) {
                      TransactionStatus.CANCELLED -> MaterialTheme.colorScheme.errorContainer
                      TransactionStatus.DRAFT -> MaterialTheme.colorScheme.tertiaryContainer
                      else -> MaterialTheme.colorScheme.secondaryContainer
                    },
                  shape = RoundedCornerShape(8.dp),
                ) {
                  Text(
                    text = tx.status.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                  )
                }
              }

              Text(
                text =
                  "Gross: ${calcEngine.formatWeight(tx.grossWeight)} g × Tunch: ${calcEngine.formatTunch(tx.tunch)}% = Fine: ${calcEngine.formatWeight(tx.fineWeight)} g • Net Value: ${calcEngine.formatCurrency(tx.netValue)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
              )

              if (tx.items.size > 1) {
                Text(
                  text = "Includes ${tx.items.size} items: " + tx.items.joinToString { "${it.itemName} (${calcEngine.formatWeight(it.fineWeight)}g fine)" },
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.primary,
                )
              }

              if (isCancelled) {
                Text(
                  text = "Cancelled by ${tx.cancelledBy}: ${tx.cancellationReason} (Excluded from active totals)",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.error,
                  fontWeight = FontWeight.SemiBold,
                )
              } else if (isDraft) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End,
                ) {
                  OutlinedButton(
                    onClick = { onOpenDraftForEdit(tx) },
                    modifier = Modifier.testTag("ledger_edit_draft_${tx.transactionId}"),
                  ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Edit / Finalize Draft")
                  }
                }
              } else {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.End,
                ) {
                  TextButton(
                    onClick = {
                      txToCancel = tx
                      cancellationReason = ""
                    },
                    colors =
                      ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                      ),
                    modifier = Modifier.testTag("ledger_cancel_tx_${tx.transactionId}"),
                  ) {
                    Icon(Icons.Default.Block, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Cancel / Reverse")
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  if (txToCancel != null) {
    AlertDialog(
      onDismissRequest = { txToCancel = null },
      title = { Text("Cancel / Reverse Transaction", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text =
              "Historical records are never deleted. Cancelling ${txToCancel!!.transactionId} will mark it as CANCELLED and exclude it from active totals.",
            style = MaterialTheme.typography.bodySmall,
          )
          OutlinedTextField(
            value = cancellationReason,
            onValueChange = { cancellationReason = it },
            label = { Text("Cancellation Reason *") },
            placeholder = { Text("e.g., Weight entry mistake, customer revised order") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_cancellation_reason"),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            val targetId = txToCancel!!.transactionId
            val reason = cancellationReason.ifBlank { "Reversed by shop owner" }
            onCancelTransaction(targetId, reason)
            txToCancel = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
          modifier = Modifier.testTag("confirm_cancel_tx_button"),
        ) {
          Text("Confirm Cancellation")
        }
      },
      dismissButton = {
        TextButton(onClick = { txToCancel = null }) {
          Text("Keep Active")
        }
      },
    )
  }
}

@Composable
private fun LedgerMetricItem(
  label: String,
  value: String,
  highlight: Boolean = false,
  modifier: Modifier = Modifier,
) {
  Column(modifier = modifier.padding(4.dp)) {
    Text(
      text = label,
      style = MaterialTheme.typography.labelSmall,
      color = Color(0xFF4E4637),
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = if (highlight) FontWeight.ExtraBold else FontWeight.Bold,
      color = if (highlight) GoldPrimary else Color(0xFF1F1B13),
    )
  }
}

@Composable
private fun CustomerFormView(
  existingCustomer: Customer?,
  onCancel: () -> Unit,
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
      pendingAmount: String,
      notes: String,
      onError: (String) -> Unit,
    ) -> Unit,
) {
  var name by remember(existingCustomer) { mutableStateOf(existingCustomer?.name ?: "") }
  var mobile by remember(existingCustomer) {
    mutableStateOf(existingCustomer?.mobileNumber ?: "")
  }
  var whatsapp by remember(existingCustomer) {
    mutableStateOf(existingCustomer?.whatsappNumber ?: "")
  }
  var address by remember(existingCustomer) { mutableStateOf(existingCustomer?.address ?: "") }
  var city by remember(existingCustomer) { mutableStateOf(existingCustomer?.city ?: "") }
  var state by remember(existingCustomer) { mutableStateOf(existingCustomer?.state ?: "") }
  var pin by remember(existingCustomer) { mutableStateOf(existingCustomer?.pinCode ?: "") }
  var pan by remember(existingCustomer) { mutableStateOf(existingCustomer?.panNumber ?: "") }
  var gst by remember(existingCustomer) { mutableStateOf(existingCustomer?.gstNumber ?: "") }
  var email by remember(existingCustomer) { mutableStateOf(existingCustomer?.email ?: "") }
  var pendingAmount by remember(existingCustomer) {
    mutableStateOf(existingCustomer?.pendingAmount?.toPlainString() ?: "0")
  }
  var notes by remember(existingCustomer) { mutableStateOf(existingCustomer?.notes ?: "") }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
    LazyColumn(
      modifier = Modifier.fillMaxSize().widthIn(max = 700.dp).testTag("customer_form_view"),
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          IconButton(
            onClick = onCancel,
            modifier = Modifier.size(48.dp).testTag("customer_form_back_button"),
          ) {
            Icon(
              imageVector = Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back to Customers",
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = if (existingCustomer == null) "Create New Customer" else "Edit Customer",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      if (errorMessage != null) {
        item {
          Surface(
            color = MaterialTheme.colorScheme.errorContainer,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = errorMessage!!,
              color = MaterialTheme.colorScheme.onErrorContainer,
              modifier = Modifier.padding(14.dp),
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      }

      item {
        ElevatedCard(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            OutlinedTextField(
              value = name,
              onValueChange = {
                name = it
                errorMessage = null
              },
              label = { Text("Customer Name *") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("customer_input_name"),
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              OutlinedTextField(
                value = mobile,
                onValueChange = {
                  mobile = it
                  errorMessage = null
                },
                label = { Text("Mobile Number *") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("customer_input_mobile"),
              )
              OutlinedTextField(
                value = whatsapp,
                onValueChange = { whatsapp = it },
                label = { Text("WhatsApp Number") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("customer_input_whatsapp"),
              )
            }

            OutlinedTextField(
              value = address,
              onValueChange = { address = it },
              label = { Text("Address") },
              modifier = Modifier.fillMaxWidth().testTag("customer_input_address"),
            )

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              OutlinedTextField(
                value = city,
                onValueChange = { city = it },
                label = { Text("City") },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("customer_input_city"),
              )
              OutlinedTextField(
                value = state,
                onValueChange = { state = it },
                label = { Text("State") },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("customer_input_state"),
              )
              OutlinedTextField(
                value = pin,
                onValueChange = { pin = it },
                label = { Text("PIN") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(0.8f).testTag("customer_input_pin"),
              )
            }

            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              OutlinedTextField(
                value = pan,
                onValueChange = { pan = it },
                label = { Text("PAN Number") },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("customer_input_pan"),
              )
              OutlinedTextField(
                value = gst,
                onValueChange = { gst = it },
                label = { Text("GSTIN (Optional)") },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("customer_input_gst"),
              )
            }

            OutlinedTextField(
              value = email,
              onValueChange = { email = it },
              label = { Text("Email") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("customer_input_email"),
            )

            OutlinedTextField(
              value = pendingAmount,
              onValueChange = {
                pendingAmount = it
                errorMessage = null
              },
              label = { Text("Pending Customer Balance (₹)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("customer_input_pending"),
            )

            OutlinedTextField(
              value = notes,
              onValueChange = { notes = it },
              label = { Text("Notes") },
              modifier = Modifier.fillMaxWidth().testTag("customer_input_notes"),
              minLines = 3,
            )
          }
        }
      }

      item {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          OutlinedButton(
            onClick = onCancel,
            modifier = Modifier.weight(1f).height(54.dp),
            shape = RoundedCornerShape(14.dp),
          ) {
            Text("Cancel")
          }
          Button(
            onClick = {
              onSave(
                existingCustomer?.id,
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
                pendingAmount,
                notes,
              ) { err ->
                errorMessage = err
              }
            },
            modifier = Modifier.weight(1f).height(54.dp).testTag("customer_save_button"),
            shape = RoundedCornerShape(14.dp),
          ) {
            Icon(Icons.Default.Save, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Save Customer", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
