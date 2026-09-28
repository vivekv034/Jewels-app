package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CurrencyRupee
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Diamond
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.Recycling
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.domain.calculation.CalculationEngine
import com.example.domain.model.BusinessProfile
import com.example.domain.model.CustomerReminder
import com.example.domain.model.DashboardSummary
import com.example.domain.model.Invoice
import com.example.domain.model.MetalInventorySummary
import com.example.domain.model.MetalRate
import com.example.domain.model.MetalType
import com.example.domain.model.RateUnit
import com.example.domain.model.Transaction
import com.example.domain.model.TransactionType
import com.example.ui.i18n.LocalizedStrings
import com.example.ui.theme.GoldAccentBg
import com.example.ui.theme.GoldAccentBorder
import com.example.ui.theme.GoldAccentText
import com.example.ui.theme.PendingAmberBg
import com.example.ui.theme.PendingAmberBorder
import com.example.ui.theme.PendingAmberText
import com.example.ui.theme.RoyalGoldShimmer
import com.example.ui.theme.RoyalObsidian
import com.example.ui.theme.SilverAccentBg
import com.example.ui.theme.SilverAccentBorder
import com.example.ui.theme.SilverAccentText

@Composable
fun DashboardScreen(
  strings: LocalizedStrings,
  businessProfile: BusinessProfile,
  metalRate: MetalRate,
  summary: DashboardSummary,
  recentTransactions: List<Transaction>,
  invoices: List<Invoice>,
  calculationEngine: CalculationEngine,
  onQuickActionTransaction: (TransactionType) -> Unit,
  onCreateInvoiceClick: () -> Unit,
  onOpenInvoicePreview: (Invoice) -> Unit,
  onDeleteTransaction: (String) -> Unit,
  onUpdateRates: (String, RateUnit, String, RateUnit, (String) -> Unit) -> Unit,
  goldInventorySummary: MetalInventorySummary = MetalInventorySummary(metal = MetalType.GOLD),
  silverInventorySummary: MetalInventorySummary = MetalInventorySummary(metal = MetalType.SILVER),
  reminders: List<CustomerReminder> = emptyList(),
  onOpenInventory: () -> Unit = {},
  onOpenCustomers: () -> Unit = {},
  onOpenInvoicesList: () -> Unit = {},
  onOpenGlobalSearch: () -> Unit = {},
  onOpenReminders: () -> Unit = {},
  modifier: Modifier = Modifier,
) {
  var showRateDialog by remember { mutableStateOf(false) }
  var transactionToDelete by remember { mutableStateOf<Transaction?>(null) }

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("dashboard_screen"),
    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp),
  ) {
    // 1. Luxury Hero Banner with Shop Info & Live Metal Rates
    item {
      HeroBannerWithRatesCard(
        businessProfile = businessProfile,
        metalRate = metalRate,
        calculationEngine = calculationEngine,
        onEditRatesClick = { showRateDialog = true },
      )
    }

    // Stage 6 Section 5: Quick Action Bar at the Top of Dashboard
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("dashboard_quick_action_bar"),
        shape = RoundedCornerShape(14.dp),
        colors =
          CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
          ),
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = strings.quickActions,
              style = MaterialTheme.typography.labelLarge,
              fontWeight = FontWeight.Bold,
            )
            TextButton(
              onClick = onOpenGlobalSearch,
              modifier = Modifier.testTag("dashboard_btn_global_search"),
            ) {
              Text(strings.globalSearch, fontWeight = FontWeight.Bold)
            }
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            OutlinedButton(
              onClick = { onQuickActionTransaction(TransactionType.MONEY_TO_GOLD) },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier.weight(1f).testTag("top_bar_new_tx"),
            ) {
              Text(strings.newTransaction, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
              onClick = onOpenCustomers,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier.weight(1f).testTag("top_bar_customers"),
            ) {
              Text(strings.customers, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
              onClick = onOpenInvoicesList,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier.weight(1f).testTag("top_bar_invoices"),
            ) {
              Text(strings.invoices, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
              onClick = onOpenInventory,
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier.weight(1f).testTag("top_bar_inventory"),
            ) {
              Text(strings.inventory, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
            }
            OutlinedButton(
              onClick = { showRateDialog = true },
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
              modifier = Modifier.weight(1f).testTag("top_bar_update_rate"),
            ) {
              Text(strings.rate, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelSmall)
            }
          }
        }
      }
    }

    // Stage 5 Section 18: Live Gold & Silver Inventory Summary on Dashboard
    item {
      Card(
        modifier =
          Modifier.fillMaxWidth()
            .clickable { onOpenInventory() }
            .testTag("dashboard_inventory_summary_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text(
              text = "${strings.inventory} — ${strings.currentStock}",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "${strings.gold} + ${strings.silver}",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Surface(
              color = GoldAccentBg,
              border = BorderStroke(1.dp, GoldAccentBorder),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f).testTag("dashboard_gold_stock_box"),
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = strings.goldInventory,
                  style = MaterialTheme.typography.labelMedium,
                  color = GoldAccentText,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "${goldInventorySummary.currentFineWeight.toPlainString()} g fine",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = GoldAccentText,
                )
                Text(
                  text = "${goldInventorySummary.currentGrossWeight.toPlainString()} g gross",
                  style = MaterialTheme.typography.bodySmall,
                  color = GoldAccentText.copy(alpha = 0.85f),
                )
                Text(
                  text = "Est: ₹${goldInventorySummary.estimatedValue.toPlainString()}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = GoldAccentText,
                )
              }
            }
            Surface(
              color = SilverAccentBg,
              border = BorderStroke(1.dp, SilverAccentBorder),
              shape = RoundedCornerShape(12.dp),
              modifier = Modifier.weight(1f).testTag("dashboard_silver_stock_box"),
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = strings.silverInventory,
                  style = MaterialTheme.typography.labelMedium,
                  color = SilverAccentText,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "${silverInventorySummary.currentFineWeight.toPlainString()} g fine",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.ExtraBold,
                  color = SilverAccentText,
                )
                Text(
                  text = "${silverInventorySummary.currentGrossWeight.toPlainString()} g gross",
                  style = MaterialTheme.typography.bodySmall,
                  color = SilverAccentText.copy(alpha = 0.85f),
                )
                Text(
                  text = "Est: ₹${silverInventorySummary.estimatedValue.toPlainString()}",
                  style = MaterialTheme.typography.labelSmall,
                  fontWeight = FontWeight.SemiBold,
                  color = SilverAccentText,
                )
              }
            }
          }
          if (goldInventorySummary.isLowStock || silverInventorySummary.isLowStock) {
            Surface(
              color = MaterialTheme.colorScheme.errorContainer,
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.fillMaxWidth().testTag("dashboard_low_stock_alert_card"),
            ) {
              val lowStockText = buildList {
                if (goldInventorySummary.isLowStock) {
                  add("Gold (${goldInventorySummary.currentFineWeight.toPlainString()}g < ${goldInventorySummary.lowStockThresholdFineGrams.toPlainString()}g)")
                }
                if (silverInventorySummary.isLowStock) {
                  add("Silver (${silverInventorySummary.currentFineWeight.toPlainString()}g < ${silverInventorySummary.lowStockThresholdFineGrams.toPlainString()}g)")
                }
              }.joinToString(" • ")
              Text(
                text = "Low Stock Alert: $lowStockText",
                color = MaterialTheme.colorScheme.onErrorContainer,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
              )
            }
          }
        }
      }
    }

    // Stage 6 Section 19: Due / Overdue Customer Reminders Card
    val activeReminders = reminders.filter { !it.isCompleted }
    if (activeReminders.isNotEmpty()) {
      item {
        Card(
          modifier =
            Modifier.fillMaxWidth()
              .clickable { onOpenReminders() }
              .testTag("dashboard_reminders_card"),
          colors = CardDefaults.cardColors(containerColor = PendingAmberBg),
          border = BorderStroke(1.dp, PendingAmberBorder),
          shape = RoundedCornerShape(14.dp),
        ) {
          Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = "${strings.reminders} (${activeReminders.size})",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PendingAmberText,
              )
              Text(
                text = strings.pendingCustomerAmount,
                style = MaterialTheme.typography.labelSmall,
                color = PendingAmberText,
              )
            }
            activeReminders.take(2).forEach { rem ->
              Text(
                text = "• ${rem.customerName} (${rem.customerMobile}): ₹${rem.pendingAmount.toPlainString()} — Due ${rem.dueDate}",
                style = MaterialTheme.typography.bodySmall,
                color = PendingAmberText,
                fontWeight = FontWeight.Medium,
              )
            }
          }
        }
      }
    }

    // 2. Quick Actions Grid (7 Required Quick Action Buttons)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = strings.quickActions,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          QuickActionButtonCard(
            title = strings.newGoldTransaction,
            subtitle = "Money → Gold",
            icon = Icons.Default.Diamond,
            containerColor = GoldAccentBg,
            borderColor = GoldAccentBorder,
            contentColor = GoldAccentText,
            testTag = "quick_action_new_gold",
            onClick = { onQuickActionTransaction(TransactionType.MONEY_TO_GOLD) },
            modifier = Modifier.weight(1f),
          )
          QuickActionButtonCard(
            title = strings.newSilverTransaction,
            subtitle = "Money → Silver",
            icon = Icons.Default.AutoAwesome,
            containerColor = SilverAccentBg,
            borderColor = SilverAccentBorder,
            contentColor = SilverAccentText,
            testTag = "quick_action_new_silver",
            onClick = { onQuickActionTransaction(TransactionType.MONEY_TO_SILVER) },
            modifier = Modifier.weight(1f),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          QuickActionButtonCard(
            title = strings.receiveGold,
            subtitle = "99% Pure / Tunch",
            icon = Icons.Default.Savings,
            containerColor = GoldAccentBg,
            borderColor = GoldAccentBorder,
            contentColor = GoldAccentText,
            testTag = "quick_action_receive_gold",
            onClick = { onQuickActionTransaction(TransactionType.GOLD_PAYMENT) },
            modifier = Modifier.weight(1f),
          )
          QuickActionButtonCard(
            title = strings.receiveSilver,
            subtitle = "99% Pure / Tunch",
            icon = Icons.Default.Scale,
            containerColor = SilverAccentBg,
            borderColor = SilverAccentBorder,
            contentColor = SilverAccentText,
            testTag = "quick_action_receive_silver",
            onClick = { onQuickActionTransaction(TransactionType.SILVER_PAYMENT) },
            modifier = Modifier.weight(1f),
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          QuickActionButtonCard(
            title = strings.scrapGold,
            subtitle = "Gross × Tunch ÷ 100",
            icon = Icons.Default.Recycling,
            containerColor = GoldAccentBg,
            borderColor = GoldAccentBorder,
            contentColor = GoldAccentText,
            testTag = "quick_action_scrap_gold",
            onClick = { onQuickActionTransaction(TransactionType.SCRAP_GOLD) },
            modifier = Modifier.weight(1f),
          )
          QuickActionButtonCard(
            title = strings.scrapSilver,
            subtitle = "Gross × Tunch ÷ 100",
            icon = Icons.Default.Recycling,
            containerColor = SilverAccentBg,
            borderColor = SilverAccentBorder,
            contentColor = SilverAccentText,
            testTag = "quick_action_scrap_silver",
            onClick = { onQuickActionTransaction(TransactionType.SCRAP_SILVER) },
            modifier = Modifier.weight(1f),
          )
        }

        Button(
          onClick = onCreateInvoiceClick,
          modifier =
            Modifier.fillMaxWidth()
              .heightIn(min = 56.dp)
              .testTag("quick_action_create_invoice"),
          shape = RoundedCornerShape(14.dp),
          colors =
            ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary,
            ),
        ) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = strings.createInvoice,
            modifier = Modifier.size(22.dp),
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text(
            text = strings.createInvoice,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }

    // 3. Today's Dashboard Summary Cards (All 9 Required Metrics)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = strings.todayOverview,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )

        // Row 1: Today's Total Transactions & Total Money Received
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          MetricSummaryCard(
            label = strings.todayTotalTransactions,
            value = "${summary.todayTotalTransactionsCount}",
            subValue = "Today's Sales: ₹${calculationEngine.formatMoney(summary.todayTotalTransactionsAmount)}",
            icon = Icons.Default.TrendingUp,
            accentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f).testTag("metric_today_total_tx"),
          )
          MetricSummaryCard(
            label = strings.totalMoneyReceived,
            value = "₹${calculationEngine.formatMoney(summary.totalMoneyReceived)}",
            subValue = "Cash / UPI / Bank",
            icon = Icons.Default.CurrencyRupee,
            accentColor = Color(0xFF15803D),
            modifier = Modifier.weight(1f).testTag("metric_total_money_received"),
          )
        }

        // Row 2: Today's Gold Transactions & Today's Silver Transactions
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          MetricSummaryCard(
            label = strings.todayGoldTransactions,
            value = "${summary.todayGoldTransactionsCount}",
            subValue = "Gold entries today",
            icon = Icons.Default.Diamond,
            accentColor = Color(0xFFB45309),
            modifier = Modifier.weight(1f).testTag("metric_today_gold_tx"),
          )
          MetricSummaryCard(
            label = strings.todaySilverTransactions,
            value = "${summary.todaySilverTransactionsCount}",
            subValue = "Silver entries today",
            icon = Icons.Default.AutoAwesome,
            accentColor = Color(0xFF475569),
            modifier = Modifier.weight(1f).testTag("metric_today_silver_tx"),
          )
        }

        // Row 3: Gold Received & Silver Received
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          MetricSummaryCard(
            label = strings.goldReceived,
            value = "${calculationEngine.formatWeight(summary.goldReceivedGrams)} g",
            subValue = "Fine weight received",
            icon = Icons.Default.Savings,
            accentColor = Color(0xFFB45309),
            modifier = Modifier.weight(1f).testTag("metric_gold_received"),
          )
          MetricSummaryCard(
            label = strings.silverReceived,
            value = "${calculationEngine.formatWeight(summary.silverReceivedGrams)} g",
            subValue = "Fine weight received",
            icon = Icons.Default.Scale,
            accentColor = Color(0xFF475569),
            modifier = Modifier.weight(1f).testTag("metric_silver_received"),
          )
        }

        // Row 4: Scrap Gold Received & Scrap Silver Received
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          MetricSummaryCard(
            label = strings.scrapGoldReceived,
            value = "${calculationEngine.formatWeight(summary.scrapGoldReceivedGrams)} g",
            subValue = "Fine scrap gold",
            icon = Icons.Default.Recycling,
            accentColor = Color(0xFF92400E),
            modifier = Modifier.weight(1f).testTag("metric_scrap_gold_received"),
          )
          MetricSummaryCard(
            label = strings.scrapSilverReceived,
            value = "${calculationEngine.formatWeight(summary.scrapSilverReceivedGrams)} g",
            subValue = "Fine scrap silver",
            icon = Icons.Default.Recycling,
            accentColor = Color(0xFF334155),
            modifier = Modifier.weight(1f).testTag("metric_scrap_silver_received"),
          )
        }

        // Row 5: Pending Customer Amount (Full Width Highlight Card)
        Card(
          modifier = Modifier.fillMaxWidth().testTag("metric_pending_customer_amount"),
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = PendingAmberBg),
          border = BorderStroke(1.dp, PendingAmberBorder),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              Surface(
                shape = CircleShape,
                color = PendingAmberBorder.copy(alpha = 0.2f),
                modifier = Modifier.size(44.dp),
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    imageVector = Icons.Default.PendingActions,
                    contentDescription = strings.pendingCustomerAmount,
                    tint = PendingAmberText,
                  )
                }
              }
              Column {
                Text(
                  text = strings.pendingCustomerAmount,
                  style = MaterialTheme.typography.titleMedium,
                  color = PendingAmberText,
                  fontWeight = FontWeight.Bold,
                )
                Text(
                  text = "Total receivable across all customer accounts",
                  style = MaterialTheme.typography.bodyMedium,
                  color = PendingAmberText.copy(alpha = 0.8f),
                )
              }
            }
            Text(
              text = "₹${calculationEngine.formatMoney(summary.pendingCustomerAmount)}",
              fontSize = 22.sp,
              fontWeight = FontWeight.ExtraBold,
              color = PendingAmberText,
            )
          }
        }
      }
    }

    // 4. Recent Transactions List
    item {
      Text(
        text = "Recent Transactions (${recentTransactions.size})",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
      )
    }

    if (recentTransactions.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth().testTag("dashboard_empty_transactions_card"),
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = strings.noTransactionsYet,
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
            )
            Text(
              text = "Tap any Quick Action above to record a Gold or Silver transaction.",
              style = MaterialTheme.typography.bodyMedium,
            )
          }
        }
      }
    } else {
      items(recentTransactions.take(8), key = { it.transactionId }) { tx ->
        TransactionSummaryRowCard(
          transaction = tx,
          calculationEngine = calculationEngine,
          onViewInvoice = {
            val matchingInv = invoices.find { it.transactionId == tx.transactionId }
            if (matchingInv != null) {
              onOpenInvoicePreview(matchingInv)
            }
          },
          onDeleteClick = { transactionToDelete = tx },
        )
      }
    }

    // 5. Recent Invoices Section (Stage 7 Section 16)
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Text(
          text = "Recent Invoices (${invoices.size})",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
        TextButton(onClick = onOpenInvoicesList) {
          Text("View All")
        }
      }
    }

    if (invoices.isEmpty()) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth().testTag("dashboard_empty_invoices_card"),
          colors =
            CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = strings.noInvoicesGeneratedYet,
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.SemiBold,
            )
          }
        }
      }
    } else {
      items(invoices.take(5), key = { "dash_inv_${it.invoiceNumber}" }) { inv ->
        Card(
          modifier =
            Modifier.fillMaxWidth()
              .clickable { onOpenInvoicePreview(inv) }
              .testTag("dashboard_recent_invoice_${inv.invoiceNumber}"),
          shape = RoundedCornerShape(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "${inv.invoiceNumber} • ${inv.customerName}",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyLarge,
              )
              Text(
                text = "${inv.date} ${inv.time} • ${inv.transactionType.title}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Text(
              text = "₹${calculationEngine.formatMoney(inv.totalAmount)}",
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }
      }
    }
  }

  if (showRateDialog) {
    QuickRateEditDialog(
      currentRate = metalRate,
      onDismiss = { showRateDialog = false },
      onSave = { gRate, gUnit, sRate, sUnit, onErr ->
        onUpdateRates(gRate, gUnit, sRate, sUnit, onErr)
        showRateDialog = false
      },
    )
  }

  transactionToDelete?.let { tx ->
    AlertDialog(
      onDismissRequest = { transactionToDelete = null },
      title = { Text("Delete Transaction?") },
      text = {
        Text(
          "Are you sure you want to delete invoice ${tx.invoiceNumber} (${tx.transactionType.title} for ${tx.customerName})? This action cannot be undone."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteTransaction(tx.transactionId)
            transactionToDelete = null
          },
          colors =
            ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { transactionToDelete = null }) { Text("Cancel") }
      },
    )
  }
}

@Composable
private fun HeroBannerWithRatesCard(
  businessProfile: BusinessProfile,
  metalRate: MetalRate,
  calculationEngine: CalculationEngine,
  onEditRatesClick: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(18.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
  ) {
    Box(modifier = Modifier.fillMaxWidth()) {
      Image(
        painter = painterResource(id = R.drawable.img_jewellery_banner_1790565703165),
        contentDescription = "Jewellery showroom banner",
        contentScale = ContentScale.Crop,
        modifier = Modifier.fillMaxWidth().height(215.dp),
      )
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(215.dp)
            .background(
              Brush.verticalGradient(
                colors =
                  listOf(
                    RoyalObsidian.copy(alpha = 0.78f),
                    RoyalObsidian.copy(alpha = 0.93f),
                  )
              )
            )
      )

      Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = businessProfile.shopName,
              color = RoyalGoldShimmer,
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
            Text(
              text =
                buildString {
                  append(businessProfile.ownerName)
                  if (businessProfile.gstNumber.isNotBlank()) {
                    append(" • GSTIN: ${businessProfile.gstNumber}")
                  }
                },
              color = Color.White.copy(alpha = 0.85f),
              style = MaterialTheme.typography.bodyMedium,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis,
            )
          }

          OutlinedButton(
            onClick = onEditRatesClick,
            modifier = Modifier.testTag("edit_rates_button"),
            border = BorderStroke(1.dp, RoyalGoldShimmer),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalGoldShimmer),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
          ) {
            Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit metal rates",
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Set Rates", fontWeight = FontWeight.Bold, fontSize = 13.sp)
          }
        }

        // Gold & Silver Live Rate Display Pills with explicit Rate Unit
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF2E2111),
            border = BorderStroke(1.dp, RoyalGoldShimmer.copy(alpha = 0.6f)),
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "GOLD RATE (${metalRate.goldRateUnit.displayName.uppercase()})",
                color = RoyalGoldShimmer,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "₹${calculationEngine.formatMoney(metalRate.goldRate)}",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
              )
              Text(
                text = "Unit: ${metalRate.goldRateUnit.displayName}",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
              )
            }
          }

          Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B),
            border = BorderStroke(1.dp, Color(0xFFCBD5E1).copy(alpha = 0.6f)),
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "SILVER RATE (${metalRate.silverRateUnit.displayName.uppercase()})",
                color = Color(0xFFE2E8F0),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = "₹${calculationEngine.formatMoney(metalRate.silverRate)}",
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.ExtraBold,
              )
              Text(
                text = "Unit: ${metalRate.silverRateUnit.displayName}",
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 12.sp,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun QuickActionButtonCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  containerColor: Color,
  borderColor: Color,
  contentColor: Color,
  testTag: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier =
      modifier
        .heightIn(min = 76.dp)
        .clip(RoundedCornerShape(14.dp))
        .clickable(onClick = onClick)
        .testTag(testTag),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = containerColor),
    border = BorderStroke(1.2.dp, borderColor),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(12.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      Surface(
        shape = CircleShape,
        color = borderColor.copy(alpha = 0.2f),
        modifier = Modifier.size(40.dp),
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = contentColor,
            modifier = Modifier.size(22.dp),
          )
        }
      }
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          color = contentColor,
          fontWeight = FontWeight.Bold,
          fontSize = 14.sp,
          maxLines = 2,
          lineHeight = 18.sp,
        )
        Text(
          text = subtitle,
          color = contentColor.copy(alpha = 0.78f),
          fontSize = 12.sp,
          maxLines = 1,
        )
      }
    }
  }
}

@Composable
private fun MetricSummaryCard(
  label: String,
  value: String,
  subValue: String,
  icon: ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(14.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        Icon(
          imageVector = icon,
          contentDescription = label,
          tint = accentColor,
          modifier = Modifier.size(20.dp),
        )
        Text(
          text = label,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          fontWeight = FontWeight.SemiBold,
          maxLines = 2,
        )
      }
      Text(
        text = value,
        fontSize = 22.sp,
        fontWeight = FontWeight.ExtraBold,
        color = MaterialTheme.colorScheme.onSurface,
      )
      Text(
        text = subValue,
        fontSize = 12.sp,
        color = accentColor,
        fontWeight = FontWeight.Medium,
      )
    }
  }
}

@Composable
fun TransactionSummaryRowCard(
  transaction: Transaction,
  calculationEngine: CalculationEngine,
  onViewInvoice: () -> Unit,
  onDeleteClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val isGold = transaction.metalType == MetalType.GOLD
  val badgeBg = if (isGold) GoldAccentBg else SilverAccentBg
  val badgeText = if (isGold) GoldAccentText else SilverAccentText
  val badgeBorder = if (isGold) GoldAccentBorder else SilverAccentBorder

  Card(
    modifier = modifier.fillMaxWidth().clickable(onClick = onViewInvoice),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp),
  ) {
    Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = badgeBg,
          border = BorderStroke(1.dp, badgeBorder),
        ) {
          Text(
            text = "${transaction.transactionType.title} • ${transaction.invoiceNumber}",
            color = badgeText,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
          )
        }
        Text(
          text = "${transaction.date} ${transaction.time}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = transaction.customerName,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text =
              "Gross: ${calculationEngine.formatWeight(transaction.grossWeight)}g | Tunch: ${calculationEngine.formatTunch(transaction.tunch)}% | Fine: ${calculationEngine.formatWeight(transaction.fineWeight)}g",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "₹${calculationEngine.formatMoney(transaction.amount)}",
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
          )
          Text(
            text = transaction.paymentMode.displayName,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }

      HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        TextButton(onClick = onViewInvoice) {
          Icon(
            imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
            contentDescription = "View Invoice",
            modifier = Modifier.size(18.dp),
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text("View Invoice")
        }
        IconButton(onClick = onDeleteClick) {
          Icon(
            imageVector = Icons.Default.DeleteOutline,
            contentDescription = "Delete transaction",
            tint = MaterialTheme.colorScheme.error,
          )
        }
      }
    }
  }
}

@Composable
fun QuickRateEditDialog(
  currentRate: MetalRate,
  onDismiss: () -> Unit,
  onSave: (String, RateUnit, String, RateUnit, (String) -> Unit) -> Unit,
) {
  var goldRateText by remember { mutableStateOf(currentRate.goldRate.toPlainString()) }
  var goldUnit by remember { mutableStateOf(currentRate.goldRateUnit) }
  var silverRateText by remember { mutableStateOf(currentRate.silverRate.toPlainString()) }
  var silverUnit by remember { mutableStateOf(currentRate.silverRateUnit) }
  var errorText by remember { mutableStateOf<String?>(null) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Update Gold & Silver Rates", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        errorText?.let { err ->
          Text(text = err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold)
        }

        Text("Gold Rate (₹)", fontWeight = FontWeight.Bold)
        OutlinedTextField(
          value = goldRateText,
          onValueChange = {
            goldRateText = it
            errorText = null
          },
          label = { Text("Gold Rate (${goldUnit.displayName})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("dialog_gold_rate_input"),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          RateUnit.entries.forEach { unit ->
            FilterChip(
              selected = goldUnit == unit,
              onClick = { goldUnit = unit },
              label = { Text(unit.displayName, fontSize = 12.sp) },
            )
          }
        }

        HorizontalDivider()

        Text("Silver Rate (₹)", fontWeight = FontWeight.Bold)
        OutlinedTextField(
          value = silverRateText,
          onValueChange = {
            silverRateText = it
            errorText = null
          },
          label = { Text("Silver Rate (${silverUnit.displayName})") },
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("dialog_silver_rate_input"),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          RateUnit.entries.forEach { unit ->
            FilterChip(
              selected = silverUnit == unit,
              onClick = { silverUnit = unit },
              label = { Text(unit.displayName, fontSize = 12.sp) },
            )
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(goldRateText, goldUnit, silverRateText, silverUnit) { err -> errorText = err }
        },
        modifier = Modifier.testTag("dialog_save_rates_button"),
      ) {
        Text("Save Rates")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    },
  )
}
