package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.OutlinedButton
import com.example.domain.calculation.CalculationEngine
import com.example.domain.model.AppLanguage
import com.example.domain.model.BusinessProfile
import com.example.domain.model.Customer
import com.example.domain.model.InventoryMovement
import com.example.domain.model.Invoice
import com.example.domain.model.MetalInventorySummary
import com.example.domain.model.MetalType
import com.example.domain.model.PurchaseRecord
import com.example.domain.model.ReportDateFilter
import com.example.domain.model.SaleRecord
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionStatus
import com.example.domain.model.TransactionType
import com.example.domain.model.Vendor
import com.example.ui.i18n.AppStrings
import com.example.ui.i18n.LocalizedStrings
import com.example.ui.theme.GoldAccentBg
import com.example.ui.theme.GoldAccentBorder
import com.example.ui.theme.GoldAccentText
import com.example.ui.theme.RoyalGoldShimmer
import com.example.ui.theme.RoyalObsidian
import com.example.ui.theme.SilverAccentBg
import com.example.ui.theme.SilverAccentBorder
import com.example.ui.theme.SilverAccentText
import java.math.BigDecimal

enum class ReportTab(val title: String) {
  SUMMARY("Summaries"),
  DAILY("Daily Sales"),
  MONTHLY("Monthly Sales"),
  CUSTOMER("Customer Ledger"),
  VENDOR("Vendor Ledger"),
  GST("GST Transactions"),
  METAL_VALUE("Inventory & Valuation"),
  STOCK_MOVEMENT("Stock Movement"),
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ReportsScreen(
  allTransactions: List<Transaction>,
  filteredTransactions: List<Transaction>,
  customers: List<Customer>,
  invoices: List<Invoice>,
  activeDateFilter: ReportDateFilter,
  customStartDate: String,
  customEndDate: String,
  selectedCustomerId: String?,
  calculationEngine: CalculationEngine,
  onSelectDateFilter: (ReportDateFilter) -> Unit,
  onApplyCustomDateRange: (String, String) -> Unit,
  onSelectCustomerFilter: (String?) -> Unit,
  onOpenInvoicePreview: (Invoice) -> Unit,
  onDeleteTransaction: (String) -> Unit,
  onBackToDashboard: () -> Unit,
  businessProfile: BusinessProfile = BusinessProfile(),
  vendors: List<Vendor> = emptyList(),
  strings: LocalizedStrings = AppStrings.forLanguage(AppLanguage.ENGLISH),
  goldInventorySummary: MetalInventorySummary = MetalInventorySummary(metal = MetalType.GOLD),
  silverInventorySummary: MetalInventorySummary = MetalInventorySummary(metal = MetalType.SILVER),
  inventoryMovements: List<InventoryMovement> = emptyList(),
  purchases: List<PurchaseRecord> = emptyList(),
  sales: List<SaleRecord> = emptyList(),
  onExportCsv: (reportType: String, shareAfterExport: Boolean) -> Unit = { _, _ -> },
  onExportPdf: (reportType: String, saveToDrive: Boolean, shareAfterExport: Boolean) -> Unit =
    { _, _, _ -> },
  modifier: Modifier = Modifier,
) {
  BackHandler { onBackToDashboard() }

  var selectedReportTab by remember { mutableStateOf(ReportTab.SUMMARY) }
  var showCustomRangeDialog by remember { mutableStateOf(false) }
  var selectedMetalFilter by remember { mutableStateOf<MetalType?>(null) }
  var selectedTxTypeFilter by remember { mutableStateOf<TransactionType?>(null) }
  var selectedVendorId by remember { mutableStateOf<String?>(null) }

  val baseFilteredTransactions =
    remember(filteredTransactions, selectedMetalFilter, selectedTxTypeFilter) {
      filteredTransactions.filter { tx ->
        (selectedMetalFilter == null || tx.metalType == selectedMetalFilter) &&
          (selectedTxTypeFilter == null || tx.transactionType == selectedTxTypeFilter)
      }
    }

  val activeFilteredTransactions =
    remember(baseFilteredTransactions) {
      baseFilteredTransactions.filter {
        it.status != com.example.domain.model.TransactionStatus.CANCELLED &&
          it.status != com.example.domain.model.TransactionStatus.DRAFT
      }
    }

  val totalAmount =
    remember(activeFilteredTransactions) {
      activeFilteredTransactions.fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) }
    }
  val goldTxs =
    remember(activeFilteredTransactions) {
      activeFilteredTransactions.filter { it.metalType == MetalType.GOLD }
    }
  val silverTxs =
    remember(activeFilteredTransactions) {
      activeFilteredTransactions.filter { it.metalType == MetalType.SILVER }
    }
  val scrapTxs =
    remember(activeFilteredTransactions) {
      activeFilteredTransactions.filter {
        it.transactionType == TransactionType.SCRAP_GOLD ||
          it.transactionType == TransactionType.SCRAP_SILVER
      }
    }

  val totalGoldFineWeight =
    remember(goldTxs) { goldTxs.fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.fineWeight) } }
  val totalGoldAmount =
    remember(goldTxs) { goldTxs.fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) } }

  val totalSilverFineWeight =
    remember(silverTxs) { silverTxs.fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.fineWeight) } }
  val totalSilverAmount =
    remember(silverTxs) { silverTxs.fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) } }

  val scrapGoldFine =
    remember(scrapTxs) {
      scrapTxs
        .filter { it.transactionType == TransactionType.SCRAP_GOLD }
        .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.fineWeight) }
    }
  val scrapSilverFine =
    remember(scrapTxs) {
      scrapTxs
        .filter { it.transactionType == TransactionType.SCRAP_SILVER }
        .fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.fineWeight) }
    }
  val totalScrapAmount =
    remember(scrapTxs) { scrapTxs.fold(BigDecimal.ZERO) { acc, tx -> acc.add(tx.amount) } }

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("reports_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Text(
        text = "Business Reports & Analytics",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "Daily, Monthly, Gold, Silver, Scrap, Customer/Vendor Ledger & GST summaries",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    // Current Business Profile Header on Reports (Stage 7 Section 1 item 6)
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("report_business_profile_header"),
        shape = RoundedCornerShape(14.dp),
        colors =
          CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
          ),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = businessProfile.shopName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
          )
          Text(
            text =
              buildString {
                append("Owner: ${businessProfile.ownerName}")
                if (businessProfile.mobileNumber.isNotBlank()) {
                  append(" • Mob: ${businessProfile.mobileNumber}")
                }
                if (businessProfile.panNumber.isNotBlank()) {
                  append(" • PAN: ${businessProfile.panNumber}")
                }
                if (businessProfile.gstNumber.isNotBlank()) {
                  append(" • GSTIN: ${businessProfile.gstNumber}")
                }
              },
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
          )
          if (businessProfile.formattedFullAddress().isNotBlank()) {
            Text(
              text = businessProfile.formattedFullAddress(),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
      }
    }

    // 1. Date Filter Chips (Today, Yesterday, This Week, This Month, Custom Date Range)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text("Date Filter (Date From / Date To):", fontWeight = FontWeight.Bold, fontSize = 14.sp)
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          ReportDateFilter.entries.forEach { filter ->
            FilterChip(
              selected = activeDateFilter == filter,
              onClick = {
                if (filter == ReportDateFilter.CUSTOM_RANGE) {
                  showCustomRangeDialog = true
                } else {
                  onSelectDateFilter(filter)
                }
              },
              label = {
                Text(
                  if (
                    filter == ReportDateFilter.CUSTOM_RANGE &&
                      activeDateFilter == ReportDateFilter.CUSTOM_RANGE
                  ) {
                    "$customStartDate → $customEndDate"
                  } else {
                    filter.displayName
                  }
                )
              },
              modifier = Modifier.testTag("report_filter_${filter.name.lowercase()}"),
            )
          }
        }

        // Metal & Transaction Type Filters (Stage 7 Section 15)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text("Metal:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
          FilterChip(
            selected = selectedMetalFilter == null,
            onClick = { selectedMetalFilter = null },
            label = { Text("All") },
            modifier = Modifier.testTag("report_metal_filter_all"),
          )
          MetalType.entries.forEach { metal ->
            FilterChip(
              selected = selectedMetalFilter == metal,
              onClick = { selectedMetalFilter = metal },
              label = { Text(metal.displayName) },
              modifier = Modifier.testTag("report_metal_filter_${metal.name.lowercase()}"),
            )
          }
        }

        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          FilterChip(
            selected = selectedTxTypeFilter == null,
            onClick = { selectedTxTypeFilter = null },
            label = { Text("All Types", fontSize = 11.sp) },
            modifier = Modifier.testTag("report_tx_type_filter_all"),
          )
          TransactionType.entries.forEach { txType ->
            FilterChip(
              selected = selectedTxTypeFilter == txType,
              onClick = { selectedTxTypeFilter = txType },
              label = { Text(txType.title, fontSize = 11.sp) },
              modifier = Modifier.testTag("report_tx_type_filter_${txType.name.lowercase()}"),
            )
          }
        }
      }
    }

    // 2. Total Amount Highlight Banner
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("report_total_amount_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalObsidian),
        border = BorderStroke(1.2.dp, RoyalGoldShimmer),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Text(
            text = "TOTAL AMOUNT (${activeDateFilter.displayName.uppercase()})",
            color = RoyalGoldShimmer,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "₹${calculationEngine.formatMoney(totalAmount)}",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
          )
          Text(
            text =
              "${filteredTransactions.size} Transactions • Gold Fine: ${calculationEngine.formatWeight(totalGoldFineWeight)}g • Silver Fine: ${calculationEngine.formatWeight(totalSilverFineWeight)}g",
            color = Color.White.copy(alpha = 0.85f),
            fontSize = 13.sp,
          )
        }
      }
    }

    // 3. Report View Selector Tabs
    item {
      FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp),
      ) {
        ReportTab.entries.forEach { tab ->
          FilterChip(
            selected = selectedReportTab == tab,
            onClick = { selectedReportTab = tab },
            label = { Text(tab.title, fontWeight = FontWeight.SemiBold) },
            modifier = Modifier.testTag("report_tab_${tab.name.lowercase()}"),
          )
        }
      }
    }

    when (selectedReportTab) {
      ReportTab.SUMMARY -> {
        // Gold Summary Card
        item {
          SummarySectionCard(
            title = "Gold Summary",
            badgeText = "${goldTxs.size} Transactions",
            bgColor = GoldAccentBg,
            borderColor = GoldAccentBorder,
            textColor = GoldAccentText,
            rows =
              listOf(
                "Total Fine Gold Weight" to "${calculationEngine.formatWeight(totalGoldFineWeight)} grams",
                "Total Gold Transaction Value" to "₹${calculationEngine.formatMoney(totalGoldAmount)}",
                "Money → Gold Entries" to
                  "${goldTxs.count { it.transactionType == TransactionType.MONEY_TO_GOLD }}",
                "Gold Payment Received Entries" to
                  "${goldTxs.count { it.transactionType == TransactionType.GOLD_PAYMENT }}",
                "Scrap Gold Entries" to
                  "${goldTxs.count { it.transactionType == TransactionType.SCRAP_GOLD }}",
              ),
          )
        }

        // Silver Summary Card
        item {
          SummarySectionCard(
            title = "Silver Summary",
            badgeText = "${silverTxs.size} Transactions",
            bgColor = SilverAccentBg,
            borderColor = SilverAccentBorder,
            textColor = SilverAccentText,
            rows =
              listOf(
                "Total Fine Silver Weight" to "${calculationEngine.formatWeight(totalSilverFineWeight)} grams",
                "Total Silver Transaction Value" to "₹${calculationEngine.formatMoney(totalSilverAmount)}",
                "Money → Silver Entries" to
                  "${silverTxs.count { it.transactionType == TransactionType.MONEY_TO_SILVER }}",
                "Silver Payment Received Entries" to
                  "${silverTxs.count { it.transactionType == TransactionType.SILVER_PAYMENT }}",
                "Scrap Silver Entries" to
                  "${silverTxs.count { it.transactionType == TransactionType.SCRAP_SILVER }}",
              ),
          )
        }

        // Scrap Summary Card
        item {
          SummarySectionCard(
            title = "Scrap Summary (Gold & Silver)",
            badgeText = "${scrapTxs.size} Scrap Entries",
            bgColor = MaterialTheme.colorScheme.surface,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            textColor = MaterialTheme.colorScheme.onSurface,
            rows =
              listOf(
                "Scrap Gold Fine Weight" to "${calculationEngine.formatWeight(scrapGoldFine)} grams",
                "Scrap Silver Fine Weight" to "${calculationEngine.formatWeight(scrapSilverFine)} grams",
                "Total Scrap Value" to "₹${calculationEngine.formatMoney(totalScrapAmount)}",
              ),
          )
        }

        // Invoices, Payments, Purchases, Sales & GST Summary Card (Stage 7 Section 15)
        item {
          val activeInvoices = invoices.filter { !it.isCancelled }
          val gstInvoices = activeInvoices.filter { it.gstEnabled && it.taxAmount > BigDecimal.ZERO }
          val totalGstTax = gstInvoices.fold(BigDecimal.ZERO) { acc, inv -> acc.add(inv.taxAmount) }
          val activePurchases = purchases.filter { it.status != TransactionStatus.CANCELLED }
          val activeSales = sales.filter { it.status != TransactionStatus.CANCELLED }

          SummarySectionCard(
            title = "Invoices, Payments, Purchases, Sales & GST",
            badgeText = "${activeInvoices.size} Active Invoices",
            bgColor = MaterialTheme.colorScheme.surface,
            borderColor = MaterialTheme.colorScheme.primary,
            textColor = MaterialTheme.colorScheme.onSurface,
            rows =
              listOf(
                "Active Invoices" to "${activeInvoices.size}",
                "GST Invoices Count" to "${gstInvoices.size}",
                "Total GST Collected" to "₹${calculationEngine.formatMoney(totalGstTax)}",
                "Purchases Count" to "${activePurchases.size}",
                "Sales Count" to "${activeSales.size}",
              ),
          )
        }
      }

      ReportTab.DAILY -> {
        val groupedByDay = filteredTransactions.groupBy { it.date }
        if (groupedByDay.isEmpty()) {
          item {
            Text(
              "No transactions found for the selected date filter.",
              modifier = Modifier.padding(16.dp),
            )
          }
        } else {
          groupedByDay.forEach { (dateStr, dayTxs) ->
            item {
              val dayTotal = dayTxs.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth(),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(12.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Text(
                    "Date: $dateStr (${dayTxs.size} Txns)",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                  )
                  Text(
                    "₹${calculationEngine.formatMoney(dayTotal)}",
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                  )
                }
              }
            }
            items(dayTxs, key = { it.transactionId }) { tx ->
              TransactionSummaryRowCard(
                transaction = tx,
                calculationEngine = calculationEngine,
                onViewInvoice = {
                  invoices.find { it.transactionId == tx.transactionId }?.let(onOpenInvoicePreview)
                },
                onDeleteClick = { onDeleteTransaction(tx.transactionId) },
              )
            }
          }
        }
      }

      ReportTab.MONTHLY -> {
        val groupedByMonth = allTransactions.groupBy { it.date.take(7) }
        items(groupedByMonth.entries.toList(), key = { it.key }) { (monthKey, monthTxs) ->
          val monthTotal = monthTxs.fold(BigDecimal.ZERO) { acc, t -> acc.add(t.amount) }
          val monthGoldFine =
            monthTxs
              .filter { it.metalType == MetalType.GOLD }
              .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.fineWeight) }
          val monthSilverFine =
            monthTxs
              .filter { it.metalType == MetalType.SILVER }
              .fold(BigDecimal.ZERO) { acc, t -> acc.add(t.fineWeight) }

          SummarySectionCard(
            title = "Month: $monthKey",
            badgeText = "${monthTxs.size} Transactions",
            bgColor = MaterialTheme.colorScheme.surface,
            borderColor = MaterialTheme.colorScheme.primary,
            textColor = MaterialTheme.colorScheme.onSurface,
            rows =
              listOf(
                "Monthly Total Amount" to "₹${calculationEngine.formatMoney(monthTotal)}",
                "Monthly Fine Gold" to "${calculationEngine.formatWeight(monthGoldFine)} g",
                "Monthly Fine Silver" to "${calculationEngine.formatWeight(monthSilverFine)} g",
              ),
          )
        }
      }

      ReportTab.CUSTOMER -> {
        item {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Filter by Customer:", fontWeight = FontWeight.Bold)
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              FilterChip(
                selected = selectedCustomerId == null,
                onClick = { onSelectCustomerFilter(null) },
                label = { Text("All Customers") },
              )
              customers.forEach { c ->
                FilterChip(
                  selected = selectedCustomerId == c.id,
                  onClick = { onSelectCustomerFilter(c.id) },
                  label = { Text(c.name) },
                )
              }
            }
          }
        }

        if (filteredTransactions.isEmpty()) {
          item {
            Text(
              "No customer transactions match the current filter.",
              modifier = Modifier.padding(16.dp),
            )
          }
        } else {
          items(filteredTransactions, key = { it.transactionId }) { tx ->
            TransactionSummaryRowCard(
              transaction = tx,
              calculationEngine = calculationEngine,
              onViewInvoice = {
                invoices.find { it.transactionId == tx.transactionId }?.let(onOpenInvoicePreview)
              },
              onDeleteClick = { onDeleteTransaction(tx.transactionId) },
            )
          }
        }
      }

      ReportTab.VENDOR -> {
        item {
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Filter by Vendor:", fontWeight = FontWeight.Bold)
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              FilterChip(
                selected = selectedVendorId == null,
                onClick = { selectedVendorId = null },
                label = { Text("All Vendors (${vendors.size})") },
              )
              vendors.forEach { v ->
                FilterChip(
                  selected = selectedVendorId == v.vendorId,
                  onClick = { selectedVendorId = v.vendorId },
                  label = { Text(v.vendorName) },
                )
              }
            }
          }
        }
        val vendorPurchases =
          purchases.filter {
            it.status != TransactionStatus.CANCELLED &&
              (selectedVendorId == null || it.vendorId == selectedVendorId)
          }
        if (vendorPurchases.isEmpty()) {
          item {
            Text("No vendor purchases matching filter.", modifier = Modifier.padding(16.dp))
          }
        } else {
          items(vendorPurchases, key = { it.purchaseId }) { p ->
            Card(
              modifier = Modifier.fillMaxWidth().testTag("report_vendor_purchase_${p.purchaseId}"),
              shape = RoundedCornerShape(12.dp),
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = "${p.vendorName} • ${p.metal.displayName} (${p.date})",
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text =
                    "Fine: ${p.fineWeight.toPlainString()} g • Total: ₹${p.totalAmount.toPlainString()} • Paid: ₹${p.amountPaid.toPlainString()} • Pending: ₹${p.pendingAmount.toPlainString()}",
                  style = MaterialTheme.typography.bodySmall,
                )
              }
            }
          }
        }
      }

      ReportTab.GST -> {
        val gstInvs = invoices.filter { !it.isCancelled && (it.gstEnabled || it.taxAmount > BigDecimal.ZERO) }
        if (gstInvs.isEmpty()) {
          item {
            Text("No GST transactions recorded yet.", modifier = Modifier.padding(16.dp))
          }
        } else {
          items(gstInvs, key = { "gst_inv_${it.invoiceNumber}" }) { inv ->
            Card(
              modifier = Modifier.fillMaxWidth().testTag("report_gst_invoice_${inv.invoiceNumber}"),
              shape = RoundedCornerShape(12.dp),
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = "${inv.invoiceNumber} • ${inv.customerName} (${inv.date})",
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text =
                    "Taxable: ₹${calculationEngine.formatMoney(inv.taxableAmount)} • CGST: ₹${calculationEngine.formatMoney(inv.cgstAmount)} • SGST: ₹${calculationEngine.formatMoney(inv.sgstAmount)} • IGST: ₹${calculationEngine.formatMoney(inv.igstAmount)} • Total: ₹${calculationEngine.formatMoney(inv.totalAmount)}",
                  style = MaterialTheme.typography.bodySmall,
                )
              }
            }
          }
        }
      }

      ReportTab.METAL_VALUE -> {
        item {
          val totalPurchaseValue =
            purchases.filter { it.status != TransactionStatus.CANCELLED }
              .fold(BigDecimal.ZERO) { acc, p -> acc.add(p.totalAmount) }
          val totalSaleValue =
            sales.filter { it.status != TransactionStatus.CANCELLED }
              .fold(BigDecimal.ZERO) { acc, s -> acc.add(s.totalAmount) }
          val totalMakingCharges =
            sales.filter { it.status != TransactionStatus.CANCELLED }
              .fold(BigDecimal.ZERO) { acc, s -> acc.add(s.makingCharges) }
          val totalCombinedStockValue =
            goldInventorySummary.estimatedValue.add(silverInventorySummary.estimatedValue)

          SummarySectionCard(
            title = strings.metalValueReport,
            badgeText = "Fact-Based Valuation",
            bgColor = GoldAccentBg,
            borderColor = GoldAccentBorder,
            textColor = GoldAccentText,
            rows =
              listOf(
                "Gold Stock (${goldInventorySummary.currentFineWeight.toPlainString()} g fine)" to "₹${goldInventorySummary.estimatedValue.toPlainString()}",
                "Silver Stock (${silverInventorySummary.currentFineWeight.toPlainString()} g fine)" to "₹${silverInventorySummary.estimatedValue.toPlainString()}",
                strings.estimatedStockValue to "₹${totalCombinedStockValue.toPlainString()}",
                "Total Purchase Value" to "₹${totalPurchaseValue.toPlainString()}",
                "Total Sale Value" to "₹${totalSaleValue.toPlainString()}",
                "Making Charges Earned" to "₹${totalMakingCharges.toPlainString()}",
              ),
          )
        }
      }

      ReportTab.STOCK_MOVEMENT -> {
        if (inventoryMovements.isEmpty()) {
          item {
            Text("No stock movements recorded yet.", modifier = Modifier.padding(16.dp))
          }
        } else {
          items(inventoryMovements, key = { it.movementId }) { mov ->
            Card(
              modifier = Modifier.fillMaxWidth().testTag("report_movement_${mov.movementId}"),
              shape = RoundedCornerShape(12.dp),
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Text(
                    "${mov.metal.displayName} • ${mov.movementType.displayName}",
                    fontWeight = FontWeight.Bold,
                  )
                  Text(
                    "${mov.direction.name}: ${mov.fineWeight.toPlainString()} g fine",
                    fontWeight = FontWeight.Bold,
                  )
                }
                Text(
                  "Gross: ${mov.grossWeight.toPlainString()} g × Tunch: ${mov.tunch.toPlainString()}% • Date: ${mov.date}",
                  style = MaterialTheme.typography.bodySmall,
                )
              }
            }
          }
        }
      }
    }

    // Stage 5 & Stage 7 Section 15: CSV & PDF Export + Google Drive Reports Upload Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("csv_export_card"),
        shape = RoundedCornerShape(14.dp),
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text(
            text = "${strings.exportCsv} / ${strings.exportPdf} / ${strings.saveReportToDrive}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Button(
              onClick = { onExportCsv("Transactions", false) },
              modifier = Modifier.testTag("btn_export_csv_transactions"),
            ) {
              Text("${strings.exportCsv}: ${strings.transactions}")
            }
            OutlinedButton(
              onClick = { onExportCsv("InventoryMovements", false) },
              modifier = Modifier.testTag("btn_export_csv_inventory"),
            ) {
              Text("${strings.exportCsv}: ${strings.inventory}")
            }
            OutlinedButton(
              onClick = { onExportCsv("CustomerLedger", false) },
              modifier = Modifier.testTag("btn_export_csv_customer_ledger"),
            ) {
              Text("${strings.exportCsv}: ${strings.customerLedger}")
            }
            OutlinedButton(
              onClick = { onExportCsv("Invoices", true) },
              modifier = Modifier.testTag("btn_share_csv_invoices"),
            ) {
              Text("${strings.shareCsv}: ${strings.invoices}")
            }
          }

          HorizontalDivider()

          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Button(
              onClick = { onExportPdf(selectedReportTab.name, false, false) },
              modifier = Modifier.testTag("btn_export_pdf_report"),
            ) {
              Text("${strings.exportPdf} (${selectedReportTab.title})")
            }
            Button(
              onClick = { onExportPdf(selectedReportTab.name, true, false) },
              modifier = Modifier.testTag("btn_save_report_pdf_drive"),
            ) {
              Text(strings.saveReportToDrive)
            }
            OutlinedButton(
              onClick = { onExportPdf(selectedReportTab.name, false, true) },
              modifier = Modifier.testTag("btn_share_pdf_report"),
            ) {
              Text("Share Report PDF")
            }
          }
        }
      }
    }
  }

  if (showCustomRangeDialog) {
    var startInput by remember { mutableStateOf(customStartDate) }
    var endInput by remember { mutableStateOf(customEndDate) }
    AlertDialog(
      onDismissRequest = { showCustomRangeDialog = false },
      title = { Text("Custom Date Range (YYYY-MM-DD)", fontWeight = FontWeight.Bold) },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          OutlinedTextField(
            value = startInput,
            onValueChange = { startInput = it },
            label = { Text("Start Date (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
          OutlinedTextField(
            value = endInput,
            onValueChange = { endInput = it },
            label = { Text("End Date (YYYY-MM-DD)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            onApplyCustomDateRange(startInput.trim(), endInput.trim())
            showCustomRangeDialog = false
          }
        ) {
          Text("Apply Filter")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCustomRangeDialog = false }) { Text("Cancel") }
      },
    )
  }
}

@Composable
private fun SummarySectionCard(
  title: String,
  badgeText: String,
  bgColor: Color,
  borderColor: Color,
  textColor: Color,
  rows: List<Pair<String, String>>,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = bgColor),
    border = BorderStroke(1.2.dp, borderColor),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.ExtraBold,
          color = textColor,
        )
        Text(
          text = badgeText,
          fontSize = 12.sp,
          fontWeight = FontWeight.Bold,
          color = textColor.copy(alpha = 0.8f),
        )
      }
      HorizontalDivider(color = borderColor.copy(alpha = 0.4f))
      rows.forEach { (label, value) ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = label,
            color = textColor.copy(alpha = 0.85f),
            style = MaterialTheme.typography.bodyMedium,
          )
          Text(
            text = value,
            color = textColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyLarge,
          )
        }
      }
    }
  }
}
