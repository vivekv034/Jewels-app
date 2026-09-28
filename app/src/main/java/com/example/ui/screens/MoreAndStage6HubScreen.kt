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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.domain.model.AppLanguage
import com.example.ui.i18n.AppStrings
import com.example.ui.viewmodel.JewelleryViewModel
import com.example.ui.viewmodel.MainNavTab
import com.example.ui.viewmodel.SettingsSection
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MoreMenuScreen(viewModel: JewelleryViewModel) {
  val profile by viewModel.businessProfile.collectAsState()
  val strings = AppStrings.forLanguage(profile.language)
  val goldSummary by viewModel.goldInventorySummary.collectAsState()
  val silverSummary by viewModel.silverInventorySummary.collectAsState()
  val vendors by viewModel.vendors.collectAsState()
  val purchases by viewModel.purchases.collectAsState()
  val sales by viewModel.sales.collectAsState()
  val security by viewModel.securitySettings.collectAsState()

  BackHandler { viewModel.selectTab(MainNavTab.DASHBOARD) }

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxSize().testTag("more_menu_screen"),
  ) {
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column {
          Text(
            text = "${strings.navMore} — ${profile.shopName}",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Stage 5 & 6 Complete Business Modules (${security.accessMode.displayName})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        OutlinedButton(
          onClick = {
            val nextLang =
              if (profile.language == AppLanguage.ENGLISH) AppLanguage.HINDI else AppLanguage.ENGLISH
            viewModel.setAppLanguage(nextLang)
          },
          modifier = Modifier.testTag("more_quick_language_toggle"),
        ) {
          Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(modifier = Modifier.width(6.dp))
          Text(if (profile.language == AppLanguage.ENGLISH) "हिन्दी" else "English")
        }
      }
    }

    item {
      MoreMenuCard(
        title = strings.inventory,
        subtitle = "${strings.gold}: ${goldSummary.currentFineWeight.toPlainString()} g fine • ${strings.silver}: ${silverSummary.currentFineWeight.toPlainString()} g fine",
        icon = Icons.Default.Inventory2,
        tag = "more_item_inventory",
        onClick = { viewModel.selectTab(MainNavTab.INVENTORY) },
      )
    }

    item {
      MoreMenuCard(
        title = strings.purchases,
        subtitle = "${purchases.size} ${strings.purchases} • Record Gold & Silver Bullion Purchases",
        icon = Icons.Default.ShoppingCart,
        tag = "more_item_purchases",
        onClick = { viewModel.selectTab(MainNavTab.PURCHASES) },
      )
    }

    item {
      MoreMenuCard(
        title = strings.sales,
        subtitle = "${sales.size} ${strings.sales} • Record Jewellery & Metal Sales with GST",
        icon = Icons.Default.ShoppingBag,
        tag = "more_item_sales",
        onClick = { viewModel.selectTab(MainNavTab.SALES) },
      )
    }

    item {
      MoreMenuCard(
        title = strings.vendors,
        subtitle = "${vendors.size} ${strings.vendors} • Bullion Suppliers & Vendor Ledgers",
        icon = Icons.Default.Business,
        tag = "more_item_vendors",
        onClick = { viewModel.selectTab(MainNavTab.VENDORS) },
      )
    }

    item {
      MoreMenuCard(
        title = strings.reports,
        subtitle = "Daily, Monthly, Metal Value, Stock Movement & CSV Exports",
        icon = Icons.Default.Assessment,
        tag = "more_item_reports",
        onClick = { viewModel.selectTab(MainNavTab.REPORTS) },
      )
    }

    item {
      MoreMenuCard(
        title = strings.globalSearch,
        subtitle = "Search Customers, Vendors, Transactions, Invoices, Purchases & Sales",
        icon = Icons.Default.Search,
        tag = "more_item_global_search",
        onClick = { viewModel.selectTab(MainNavTab.GLOBAL_SEARCH) },
      )
    }

    item {
      MoreMenuCard(
        title = "${strings.backupNow} & ${strings.restoreBackup}",
        subtitle = "Google Sheets & Drive Backup, JSON Export/Restore & Integrity Check",
        icon = Icons.Default.Backup,
        tag = "more_item_backup_recovery",
        onClick = { viewModel.selectTab(MainNavTab.BACKUP_RECOVERY) },
      )
    }

    item {
      MoreMenuCard(
        title = strings.settings,
        subtitle = "Language, GST, PIN Security, User Permissions, Reminders & App Health",
        icon = Icons.Default.Settings,
        tag = "more_item_settings",
        onClick = {
          viewModel.openSettingsSection(SettingsSection.MENU)
          viewModel.selectTab(MainNavTab.SETTINGS)
        },
      )
    }
  }
}

@Composable
private fun MoreMenuCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  tag: String,
  onClick: () -> Unit,
) {
  ElevatedCard(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag(tag),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.SpaceBetween,
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
          Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(10.dp),
          )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column {
          Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
      Icon(
        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }
  }
}

@Composable
fun GlobalSearchScreen(viewModel: JewelleryViewModel) {
  val profile by viewModel.businessProfile.collectAsState()
  val strings = AppStrings.forLanguage(profile.language)
  val query by viewModel.globalSearchQuery.collectAsState()
  val results by viewModel.globalSearchResults.collectAsState()

  BackHandler { viewModel.selectTab(MainNavTab.DASHBOARD) }

  Column(modifier = Modifier.fillMaxSize().testTag("global_search_screen")) {
    Surface(
      color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(onClick = { viewModel.selectTab(MainNavTab.DASHBOARD) }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backToDashboard)
          }
          Text(
            text = strings.globalSearch,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
          value = query,
          onValueChange = { viewModel.updateGlobalSearchQuery(it) },
          label = {
            Text("${strings.search} (Customer, Vendor, Invoice No, Transaction ID, Mobile, Date)")
          },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("global_search_input"),
        )
      }
    }

    LazyColumn(
      contentPadding = PaddingValues(16.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
      modifier = Modifier.fillMaxSize(),
    ) {
      if (query.isBlank()) {
        item {
          Card(modifier = Modifier.fillMaxWidth()) {
            Text(
              text = "Type any Customer Name, Mobile Number, Invoice Number (e.g. HGR-000001), Transaction ID (e.g. TXN-2026-0001), Vendor Name, Purchase ID, or Date to search across the entire database.",
              modifier = Modifier.padding(16.dp),
              style = MaterialTheme.typography.bodyMedium,
            )
          }
        }
      } else {
        item {
          Text(
            text = "Found ${results.totalMatches} matching records for \"$query\"",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
          )
        }

        if (results.customers.isNotEmpty()) {
          item {
            Text(
              "${strings.customers} (${results.customers.size})",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleMedium,
            )
          }
          items(results.customers, key = { "cust_${it.id}" }) { c ->
            Card(
              modifier =
                Modifier.fillMaxWidth().clickable {
                  viewModel.openCustomerTransactionHistory(c)
                },
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column {
                  Text(c.name, fontWeight = FontWeight.Bold)
                  Text("ID: ${c.id} • Mobile: ${c.mobile}", style = MaterialTheme.typography.bodySmall)
                }
                Text(
                  "${strings.pending}: ₹${c.pendingAmount.toPlainString()}",
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.error,
                )
              }
            }
          }
        }

        if (results.vendors.isNotEmpty()) {
          item {
            Text(
              "${strings.vendors} (${results.vendors.size})",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleMedium,
            )
          }
          items(results.vendors, key = { "vend_${it.vendorId}" }) { v ->
            Card(
              modifier =
                Modifier.fillMaxWidth().clickable {
                  viewModel.openVendorDetail(v)
                },
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column {
                  Text("${v.name} (${v.companyName})", fontWeight = FontWeight.Bold)
                  Text("ID: ${v.vendorId} • Mobile: ${v.mobile}", style = MaterialTheme.typography.bodySmall)
                }
                Text("${strings.pending}: ₹${v.pendingMoney.toPlainString()}", fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        if (results.invoices.isNotEmpty()) {
          item {
            Text(
              "${strings.invoices} (${results.invoices.size})",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleMedium,
            )
          }
          items(results.invoices, key = { "inv_${it.invoiceNumber}" }) { inv ->
            Card(
              modifier =
                Modifier.fillMaxWidth().clickable {
                  viewModel.openInvoicePreview(inv)
                },
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column {
                  Text("${inv.invoiceNumber} • ${inv.customerName}", fontWeight = FontWeight.Bold)
                  Text("${inv.transactionType.title} • ${inv.date}", style = MaterialTheme.typography.bodySmall)
                }
                Text("₹${inv.grandTotal.toPlainString()}", fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        if (results.transactions.isNotEmpty()) {
          item {
            Text(
              "${strings.transactions} (${results.transactions.size})",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleMedium,
            )
          }
          items(results.transactions, key = { "tx_${it.transactionId}" }) { tx ->
            Card(
              modifier =
                Modifier.fillMaxWidth().clickable {
                  viewModel.openInvoiceForTransaction(tx)
                },
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column {
                  Text("${tx.transactionId} (${tx.invoiceNumber})", fontWeight = FontWeight.Bold)
                  Text("${tx.customerName} • ${tx.transactionType.title} • ${tx.date}", style = MaterialTheme.typography.bodySmall)
                }
                Text("₹${tx.amount.toPlainString()}", fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        if (results.purchases.isNotEmpty()) {
          item {
            Text(
              "${strings.purchases} (${results.purchases.size})",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleMedium,
            )
          }
          items(results.purchases, key = { "pur_${it.purchaseId}" }) { pur ->
            Card(
              modifier = Modifier.fillMaxWidth().clickable { viewModel.selectTab(MainNavTab.PURCHASES) }
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column {
                  Text("${pur.purchaseId} • ${pur.vendorName}", fontWeight = FontWeight.Bold)
                  Text("${pur.metal.displayName} ${pur.fineWeight.toPlainString()} g fine • ${pur.date}", style = MaterialTheme.typography.bodySmall)
                }
                Text("₹${pur.totalAmount.toPlainString()}", fontWeight = FontWeight.Bold)
              }
            }
          }
        }

        if (results.sales.isNotEmpty()) {
          item {
            Text(
              "${strings.sales} (${results.sales.size})",
              fontWeight = FontWeight.Bold,
              style = MaterialTheme.typography.titleMedium,
            )
          }
          items(results.sales, key = { "sale_${it.saleId}" }) { sale ->
            Card(
              modifier = Modifier.fillMaxWidth().clickable { viewModel.selectTab(MainNavTab.SALES) }
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column {
                  Text("${sale.saleId} (${sale.invoiceNumber}) • ${sale.customerName}", fontWeight = FontWeight.Bold)
                  Text("${sale.metal.displayName} ${sale.fineWeight.toPlainString()} g fine • ${sale.date}", style = MaterialTheme.typography.bodySmall)
                }
                Text("₹${sale.totalAmount.toPlainString()}", fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }
    }
  }
}

@Composable
fun BackupAndRecoveryScreen(viewModel: JewelleryViewModel) {
  val context = LocalContext.current
  val profile by viewModel.businessProfile.collectAsState()
  val strings = AppStrings.forLanguage(profile.language)
  val health by viewModel.healthStatus.collectAsState()
  val security by viewModel.securitySettings.collectAsState()

  LaunchedEffect(Unit) { viewModel.refreshHealthStatus() }

  BackHandler { viewModel.selectTab(MainNavTab.DASHBOARD) }

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxSize().testTag("backup_recovery_screen"),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = { viewModel.selectTab(MainNavTab.DASHBOARD) }) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backToDashboard)
        }
        Column {
          Text(
            text = "${strings.backupNow} & ${strings.restoreBackup}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Google Drive Backup • Google Sheets Restore • Data Integrity Verification",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    item {
      ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CloudDone, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Google Drive & Local Backup", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
          Text(
            text = "Folder: Jewellery Business Manager / Backups / Jewellery_Backup_${JewelleryViewModel.getTodayDateString()}.json",
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
          )
          if (security.lastBackupFileName.isNotBlank()) {
            val formattedDate =
              SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US).format(
                Date(maxOf(security.lastLocalBackupTimestamp, security.lastDriveBackupTimestamp))
              )
            Text(
              text = "Last Backup: ${security.lastBackupFileName} ($formattedDate)",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.primary,
            )
          }
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
              onClick = { viewModel.createGoogleDriveBackup(context) },
              modifier = Modifier.weight(1f).testTag("btn_drive_backup_now"),
            ) {
              Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Google Drive Backup")
            }
            OutlinedButton(
              onClick = { viewModel.createLocalJsonBackupFile(context) },
              modifier = Modifier.weight(1f).testTag("btn_local_json_backup"),
            ) {
              Text("Export JSON Backup")
            }
          }
        }
      }
    }

    item {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Restore, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Data Recovery & Restore", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
          Text(
            text = "Recover data if local storage is cleared or when switching to a new phone:",
            style = MaterialTheme.typography.bodySmall,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
            Button(
              onClick = { viewModel.restoreFromGoogleSheetsBackup() },
              modifier = Modifier.weight(1f).testTag("btn_restore_from_sheets"),
            ) {
              Text("Restore from Google Sheets")
            }
            OutlinedButton(
              onClick = { viewModel.restoreFromLocalBackupFile(context) },
              modifier = Modifier.weight(1f).testTag("btn_restore_from_json"),
            ) {
              Text("Restore Local JSON")
            }
          }
        }
      }
    }

    item {
      Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
        modifier = Modifier.fillMaxWidth().testTag("integrity_verification_card"),
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(strings.dataIntegrity, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
          }
          Text(
            text = "Checks for duplicate Transaction IDs, duplicate Invoice Numbers, broken customer references, and inventory calculation consistency.",
            style = MaterialTheme.typography.bodySmall,
          )
          Text(
            text = "Status: ${health.integritySummary}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Total Verified Local Records: ${health.localDatabaseRecordCount}",
            style = MaterialTheme.typography.bodySmall,
          )
          OutlinedButton(
            onClick = {
              viewModel.refreshHealthStatus()
              viewModel.showBanner("Data integrity verified: ${health.integritySummary}")
            },
            modifier = Modifier.testTag("btn_verify_integrity"),
          ) {
            Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Verify Data Integrity Now")
          }
        }
      }
    }
  }
}
