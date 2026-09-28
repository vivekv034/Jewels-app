package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.TableChart
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.calculation.CalculationEngine
import com.example.domain.model.AppLanguage
import com.example.domain.model.BusinessProfile
import com.example.domain.model.GoogleAccountState
import com.example.domain.model.LogoPosition
import com.example.domain.model.MetalRate
import com.example.domain.model.PendingSyncRecord
import com.example.domain.model.RateUnit
import com.example.domain.model.SpreadsheetInfo
import com.example.domain.model.SyncStatus
import com.example.domain.model.WorksheetSchemas
import com.example.ui.theme.GoldAccentBg
import com.example.ui.theme.GoldAccentBorder
import com.example.ui.theme.GoldAccentText
import com.example.ui.theme.PendingAmberBg
import com.example.ui.theme.PendingAmberBorder
import com.example.ui.theme.PendingAmberText
import com.example.ui.theme.RoyalGoldShimmer
import com.example.ui.theme.RoyalObsidian
import com.example.ui.viewmodel.JewelleryViewModel
import com.example.ui.viewmodel.SettingsSection
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SettingsScreen(
  activeSection: SettingsSection,
  businessProfile: BusinessProfile,
  metalRate: MetalRate,
  googleAccountState: GoogleAccountState,
  availableSpreadsheets: List<SpreadsheetInfo>,
  pendingSyncQueue: List<PendingSyncRecord>,
  customerCount: Int,
  transactionCount: Int,
  invoiceCount: Int,
  calculationEngine: CalculationEngine,
  onSelectSection: (SettingsSection) -> Unit,
  onSaveBusinessProfile: (BusinessProfile, () -> Unit, (String) -> Unit) -> Unit,
  onSaveMetalRates: (String, RateUnit, String, RateUnit, () -> Unit, (String) -> Unit) -> Unit,
  onSwitchLanguage: (AppLanguage) -> Unit,
  onConnectGoogleAccount: () -> Unit,
  onDisconnectGoogleAccount: () -> Unit,
  onReconnectGoogleAccount: () -> Unit,
  onCreateOrSelectDatabase: (String) -> Unit,
  onSelectExistingSpreadsheet: (SpreadsheetInfo) -> Unit,
  onSyncNow: () -> Unit,
  onToggleOfflineMode: (Boolean) -> Unit,
  onLoadWorksheetRows: suspend (String) -> List<List<String>>,
  onResetDemoData: () -> Unit,
  onBackToDashboard: () -> Unit,
  viewModel: JewelleryViewModel? = null,
  modifier: Modifier = Modifier,
) {
  if (activeSection == SettingsSection.MENU) {
    BackHandler { onBackToDashboard() }
    SettingsMenuList(
      businessProfile = businessProfile,
      metalRate = metalRate,
      googleAccountState = googleAccountState,
      pendingSyncCount = pendingSyncQueue.size,
      calculationEngine = calculationEngine,
      onSelectSection = onSelectSection,
      onConnectGoogleAccount = onConnectGoogleAccount,
      onDisconnectGoogleAccount = onDisconnectGoogleAccount,
      onChangeDatabaseClick = { onSelectSection(SettingsSection.GOOGLE_SHEETS_DATABASE) },
      onSyncNow = onSyncNow,
      modifier = modifier,
    )
  } else {
    BackHandler { onSelectSection(SettingsSection.MENU) }
    when (activeSection) {
      SettingsSection.BUSINESS_PROFILE ->
        BusinessProfileSettingsForm(
          profile = businessProfile,
          onBack = { onSelectSection(SettingsSection.MENU) },
          onSave = onSaveBusinessProfile,
          modifier = modifier,
        )
      SettingsSection.METAL_RATES ->
        MetalRateSettingsForm(
          metalRate = metalRate,
          calculationEngine = calculationEngine,
          onBack = { onSelectSection(SettingsSection.MENU) },
          onSave = onSaveMetalRates,
          modifier = modifier,
        )
      SettingsSection.CALCULATION_RULES ->
        CalculationRulesSettingsForm(
          profile = businessProfile,
          onBack = { onSelectSection(SettingsSection.MENU) },
          onSave = onSaveBusinessProfile,
          modifier = modifier,
        )
      SettingsSection.GOOGLE_ACCOUNT ->
        GoogleAccountSettingsSection(
          accountState = googleAccountState,
          onBack = { onSelectSection(SettingsSection.MENU) },
          onConnect = onConnectGoogleAccount,
          onDisconnect = onDisconnectGoogleAccount,
          onReconnect = onReconnectGoogleAccount,
          onOpenSheetsDatabase = { onSelectSection(SettingsSection.GOOGLE_SHEETS_DATABASE) },
          modifier = modifier,
        )
      SettingsSection.GOOGLE_SHEETS_DATABASE ->
        GoogleSheetsDatabaseSection(
          accountState = googleAccountState,
          availableSpreadsheets = availableSpreadsheets,
          onBack = { onSelectSection(SettingsSection.MENU) },
          onConnectAccountFirst = onConnectGoogleAccount,
          onCreateDatabase = onCreateOrSelectDatabase,
          onSelectDatabase = onSelectExistingSpreadsheet,
          onLoadWorksheetRows = onLoadWorksheetRows,
          modifier = modifier,
        )
      SettingsSection.GOOGLE_DRIVE_STORAGE,
      SettingsSection.SYNC_STATUS ->
        SyncStatusSettingsSection(
          accountState = googleAccountState,
          pendingRecords = pendingSyncQueue,
          onBack = { onSelectSection(SettingsSection.MENU) },
          onSyncNow = onSyncNow,
          onToggleOfflineMode = onToggleOfflineMode,
          modifier = modifier,
        )
      SettingsSection.INVOICE_SETTINGS ->
        InvoiceSettingsForm(
          profile = businessProfile,
          onBack = { onSelectSection(SettingsSection.MENU) },
          onSave = onSaveBusinessProfile,
          modifier = modifier,
        )
      SettingsSection.LANGUAGE ->
        LanguageSettingsSection(
          currentLanguage = businessProfile.language,
          onBack = { onSelectSection(SettingsSection.MENU) },
          onSwitchLanguage = onSwitchLanguage,
          modifier = modifier,
        )
      SettingsSection.DATA_BACKUP ->
        DataAndBackupSection(
          customerCount = customerCount,
          transactionCount = transactionCount,
          invoiceCount = invoiceCount,
          onBack = { onSelectSection(SettingsSection.MENU) },
          onResetDemoData = onResetDemoData,
          viewModel = viewModel,
          modifier = modifier,
        )
      SettingsSection.GST_CONFIGURATION,
      SettingsSection.PERFORMANCE_PRINTING ->
        if (viewModel != null) {
          GstAndPrintingConfigurationSection(
            viewModel = viewModel,
            onBack = { onSelectSection(SettingsSection.MENU) },
          )
        }
      SettingsSection.SECURITY_PIN ->
        if (viewModel != null) {
          SecurityAndPermissionsSection(
            viewModel = viewModel,
            onBack = { onSelectSection(SettingsSection.MENU) },
          )
        }
      SettingsSection.AUDIT_CONTROLS ->
        if (viewModel != null) {
          AuditControlsSection(
            viewModel = viewModel,
            onBack = { onSelectSection(SettingsSection.MENU) },
          )
        }
      SettingsSection.REMINDERS ->
        if (viewModel != null) {
          CustomerRemindersSection(
            viewModel = viewModel,
            onBack = { onSelectSection(SettingsSection.MENU) },
          )
        }
      SettingsSection.APP_HEALTH ->
        if (viewModel != null) {
          ApplicationHealthSection(
            viewModel = viewModel,
            onBack = { onSelectSection(SettingsSection.MENU) },
          )
        }
      SettingsSection.MENU -> Unit
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SettingsMenuList(
  businessProfile: BusinessProfile,
  metalRate: MetalRate,
  googleAccountState: GoogleAccountState,
  pendingSyncCount: Int,
  calculationEngine: CalculationEngine,
  onSelectSection: (SettingsSection) -> Unit,
  onConnectGoogleAccount: () -> Unit,
  onDisconnectGoogleAccount: () -> Unit,
  onChangeDatabaseClick: () -> Unit,
  onSyncNow: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var showChangeDbWarning by remember { mutableStateOf(false) }

  val lastSyncFormatted =
    remember(googleAccountState.lastSyncTimestamp) {
      if (googleAccountState.lastSyncTimestamp <= 0L) "Never"
      else
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)
          .format(Date(googleAccountState.lastSyncTimestamp))
    }

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("settings_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Text(
        text = "Settings & Cloud Configuration",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "Manage Business Profile, Rates, Google Account, Google Sheets Database & Sync",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
      )
    }

    // Top Cloud Connection & Sync Overview Card as required by Stage 2 prompt
    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_cloud_summary_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = RoyalObsidian),
        border = BorderStroke(1.2.dp, RoyalGoldShimmer),
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
              text = "GOOGLE CLOUD & SHEETS STATUS",
              color = RoyalGoldShimmer,
              fontSize = 12.sp,
              fontWeight = FontWeight.ExtraBold,
            )
            Surface(
              shape = RoundedCornerShape(8.dp),
              color =
                if (googleAccountState.isOfflineMode) Color(0xFF9A3412)
                else if (googleAccountState.syncStatus == SyncStatus.SYNCED) Color(0xFF15803D)
                else Color(0xFFB45309),
            ) {
              Text(
                text =
                  if (googleAccountState.isOfflineMode) "Offline"
                  else googleAccountState.syncStatus.displayName,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
              )
            }
          }

          Text(
            text =
              "Connected Account: ${googleAccountState.connectedEmail.ifBlank { "Not Connected" }}",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            modifier = Modifier.testTag("settings_connected_email_text"),
          )
          Text(
            text =
              "Database: ${googleAccountState.selectedSpreadsheetName.ifBlank { "Not Selected" }}",
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 14.sp,
            modifier = Modifier.testTag("settings_database_name_text"),
          )
          Text(
            text = "Sync Status: ${googleAccountState.syncStatus.displayName}",
            color = RoyalGoldShimmer,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
          )
          Text(
            text = "Last Sync: $lastSyncFormatted",
            color = Color.White.copy(alpha = 0.8f),
            fontSize = 12.sp,
          )

          HorizontalDivider(color = RoyalGoldShimmer.copy(alpha = 0.3f))

          // 4 Required Quick Buttons: Connect Google Account, Change Database, Sync Now, Disconnect Google Account
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            if (!googleAccountState.isConnected) {
              Button(
                onClick = onConnectGoogleAccount,
                modifier = Modifier.testTag("btn_connect_google_account"),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = RoyalGoldShimmer,
                    contentColor = RoyalObsidian,
                  ),
              ) {
                Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Connect Google Account", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
            } else {
              OutlinedButton(
                onClick = { showChangeDbWarning = true },
                modifier = Modifier.testTag("btn_change_database"),
                border = BorderStroke(1.dp, RoyalGoldShimmer),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = RoyalGoldShimmer),
              ) {
                Icon(
                  Icons.Default.TableChart,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Change Database", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }

              Button(
                onClick = onSyncNow,
                modifier = Modifier.testTag("btn_sync_now"),
                colors =
                  ButtonDefaults.buttonColors(
                    containerColor = RoyalGoldShimmer,
                    contentColor = RoyalObsidian,
                  ),
              ) {
                Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Sync Now", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }

              OutlinedButton(
                onClick = onDisconnectGoogleAccount,
                modifier = Modifier.testTag("btn_disconnect_google_account"),
                border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFFCA5A5)),
              ) {
                Icon(
                  Icons.Default.LinkOff,
                  contentDescription = null,
                  modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Disconnect Google Account", fontWeight = FontWeight.Bold, fontSize = 13.sp)
              }
            }
          }
        }
      }
    }

    // Stage 7 Organized Settings Sections (A through K)
    item {
      SettingsCategoryHeader("A. Business Profile")
      SettingsSectionRowCard(
        title = "Business Profile (Edit Shop & Owner Details)",
        subtitle = "${businessProfile.shopName} • Prop: ${businessProfile.ownerName} • GSTIN: ${businessProfile.gstNumber.ifBlank { "Not Set" }}",
        icon = Icons.Default.Storefront,
        testTag = "settings_item_business_profile",
        onClick = { onSelectSection(SettingsSection.BUSINESS_PROFILE) },
      )
    }

    item {
      SettingsCategoryHeader("B. GST Settings")
      SettingsSectionRowCard(
        title = "GST Settings & Tax Treatment",
        subtitle =
          "GST: ${if (businessProfile.gstEnabled) "${businessProfile.defaultGstRatePercent}% Enabled" else "Disabled"} • State: ${businessProfile.state.ifBlank { businessProfile.gstStateName }} • CGST/SGST/IGST",
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        testTag = "settings_item_gst_configuration",
        onClick = { onSelectSection(SettingsSection.GST_CONFIGURATION) },
      )
    }

    item {
      SettingsCategoryHeader("C. Invoice Settings")
      SettingsSectionRowCard(
        title = "Invoice Settings",
        subtitle =
          "Prefix: ${businessProfile.invoicePrefix} • Start #: ${businessProfile.invoiceStartingNumber} • Paper: ${businessProfile.defaultInvoicePaperSize.displayName} • Terms & Footer",
        icon = Icons.AutoMirrored.Filled.ReceiptLong,
        testTag = "settings_item_invoice_settings",
        onClick = { onSelectSection(SettingsSection.INVOICE_SETTINGS) },
      )
    }

    item {
      SettingsCategoryHeader("D. Metal Rates & Calculation Rules")
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SettingsSectionRowCard(
          title = "Metal Rates (Gold & Silver)",
          subtitle =
            "Gold: ₹${calculationEngine.formatMoney(metalRate.goldRate)} (${metalRate.goldRateUnit.displayName}) • Silver: ₹${calculationEngine.formatMoney(metalRate.silverRate)} (${metalRate.silverRateUnit.displayName})",
          icon = Icons.Default.CurrencyExchange,
          testTag = "settings_item_metal_rates",
          onClick = { onSelectSection(SettingsSection.METAL_RATES) },
        )
        SettingsSectionRowCard(
          title = "Jewellery Calculation Rules",
          subtitle =
            "Weight: ${businessProfile.weightDecimalPlaces} dec • Tunch: ${businessProfile.tunchDecimalPlaces} dec • Divisor: ÷${businessProfile.purityDivisor.toPlainString()} • Scrap Deduction: ${if (businessProfile.enabledDeduction) "Enabled" else "Off"}",
          icon = Icons.Default.CurrencyExchange,
          testTag = "settings_item_calculation_rules",
          onClick = { onSelectSection(SettingsSection.CALCULATION_RULES) },
        )
      }
    }

    item {
      SettingsCategoryHeader("E. Reminders")
      SettingsSectionRowCard(
        title = "Reminders (Customer, Vendor & Stock Follow-Up)",
        subtitle = "Customer Payment, Customer Follow-up, Vendor Payment, Purchase, Stock Purchase & Other",
        icon = Icons.Default.Refresh,
        testTag = "settings_item_reminders",
        onClick = { onSelectSection(SettingsSection.REMINDERS) },
      )
    }

    item {
      SettingsCategoryHeader("F. Security & Audit")
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SettingsSectionRowCard(
          title = "Security (App Lock PIN, Owner & Staff Mode)",
          subtitle = "4-digit PIN lock, Owner Mode vs Staff Mode permissions",
          icon = Icons.Default.AccountCircle,
          testTag = "settings_item_security_pin",
          onClick = { onSelectSection(SettingsSection.SECURITY_PIN) },
        )
        SettingsSectionRowCard(
          title = "Audit Controls & Trail",
          subtitle = "Cancellation & inventory adjustment reason enforcement + audit history",
          icon = Icons.Default.TableChart,
          testTag = "settings_item_audit_controls",
          onClick = { onSelectSection(SettingsSection.AUDIT_CONTROLS) },
        )
      }
    }

    item {
      SettingsCategoryHeader("G. Google Account & Sync")
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SettingsSectionRowCard(
          title = "Google Account",
          subtitle =
            if (googleAccountState.isConnected)
              "Connected: ${googleAccountState.connectedEmail}"
            else "Disconnected • Tap to connect Google Account",
          icon = Icons.Default.AccountCircle,
          testTag = "settings_item_google_account",
          onClick = { onSelectSection(SettingsSection.GOOGLE_ACCOUNT) },
        )
        SettingsSectionRowCard(
          title = "Google Sheets Database",
          subtitle =
            if (googleAccountState.hasDatabaseSelected)
              "${googleAccountState.selectedSpreadsheetName} (${googleAccountState.initializedWorksheets.size} worksheets)"
            else "Select or create '${WorksheetSchemas.DEFAULT_DATABASE_NAME}'",
          icon = Icons.Default.TableChart,
          testTag = "settings_item_google_sheets_database",
          onClick = { onSelectSection(SettingsSection.GOOGLE_SHEETS_DATABASE) },
        )
        SettingsSectionRowCard(
          title = "Google Drive Invoice & Report Storage",
          subtitle =
            if (googleAccountState.isConnected)
              "Folder: ${googleAccountState.driveInvoiceFolderPath} (Private)"
            else "Connect Google Account to save invoice & report PDFs to Drive",
          icon = Icons.Default.CloudDone,
          testTag = "settings_item_google_drive_storage",
          onClick = { onSelectSection(SettingsSection.GOOGLE_DRIVE_STORAGE) },
        )
        SettingsSectionRowCard(
          title = "Sync Status",
          subtitle =
            "${googleAccountState.syncStatus.displayName} • Pending Queue: $pendingSyncCount • Last Sync: $lastSyncFormatted",
          icon = Icons.Default.CloudSync,
          testTag = "settings_item_sync_status",
          onClick = { onSelectSection(SettingsSection.SYNC_STATUS) },
        )
      }
    }

    item {
      SettingsCategoryHeader("H. Backup & Restore")
      SettingsSectionRowCard(
        title = "Backup & Restore",
        subtitle = "Local JSON Backup, Google Drive Backup, Restore & Sample Data Reset",
        icon = Icons.Default.Storage,
        testTag = "settings_item_data_backup",
        onClick = { onSelectSection(SettingsSection.DATA_BACKUP) },
      )
    }

    item {
      SettingsCategoryHeader("I. Language / भाषा")
      SettingsSectionRowCard(
        title = "Language / भाषा",
        subtitle = "Active: ${businessProfile.language.displayName} (English / हिन्दी)",
        icon = Icons.Default.Language,
        testTag = "settings_item_language",
        onClick = { onSelectSection(SettingsSection.LANGUAGE) },
      )
    }

    item {
      SettingsCategoryHeader("J. Data Health")
      SettingsSectionRowCard(
        title = "Data Health & Integrity Monitor",
        subtitle = "Cloud status, sync queue, backup status & database integrity check",
        icon = Icons.Default.CloudDone,
        testTag = "settings_item_app_health",
        onClick = { onSelectSection(SettingsSection.APP_HEALTH) },
      )
    }

    item {
      SettingsCategoryHeader("K. App Information")
      Card(
        modifier = Modifier.fillMaxWidth().testTag("settings_item_app_information"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = "Jewellery Business Manager — Stage 7 Production Ready",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleSmall,
          )
          Text(
            text = "Version 7.0.0 • Profile ID: ${businessProfile.businessId} • Status: ${businessProfile.status}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Text(
            text = "Offline-First Room SQLite + Google Sheets (16 Worksheets) + Google Drive PDFs & Backups",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }

  if (showChangeDbWarning) {
    AlertDialog(
      onDismissRequest = { showChangeDbWarning = false },
      title = { Text("Change Google Sheets Database?", fontWeight = FontWeight.Bold) },
      text = {
        Text(
          "Changing the active Google Sheets database will switch synchronization to the newly selected spreadsheet. Existing records in your current spreadsheet will remain untouched. Do you want to continue?"
        )
      },
      confirmButton = {
        Button(
          onClick = {
            showChangeDbWarning = false
            onChangeDatabaseClick()
          },
          modifier = Modifier.testTag("confirm_change_db_warning_btn"),
        ) {
          Text("Continue")
        }
      },
      dismissButton = {
        TextButton(onClick = { showChangeDbWarning = false }) { Text("Cancel") }
      },
    )
  }
}

@Composable
private fun GoogleAccountSettingsSection(
  accountState: GoogleAccountState,
  onBack: () -> Unit,
  onConnect: () -> Unit,
  onDisconnect: () -> Unit,
  onReconnect: () -> Unit,
  onOpenSheetsDatabase: () -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("google_account_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
          text = "Google Account",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(18.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Icon(
              imageVector =
                if (accountState.isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
              contentDescription = null,
              tint =
                if (accountState.isConnected) Color(0xFF15803D)
                else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(32.dp),
            )
            Column {
              Text(
                text = "Connection Status: ${accountState.connectionStatus.displayName}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.testTag("google_connection_status_text"),
              )
              Text(
                text =
                  "Connected Email: ${accountState.connectedEmail.ifBlank { "Not Connected" }}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.testTag("google_connected_email_text"),
              )
            }
          }

          Text(
            text =
              "Permissions requested: Minimum required Google Sheets (spreadsheets) & app-created Drive files (drive.file) only. Your Google password is never requested or stored.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          HorizontalDivider()

          // Required Buttons: Connect Google Account, Disconnect Google Account, Reconnect Google Account
          Button(
            onClick = onConnect,
            modifier =
              Modifier.fillMaxWidth()
                .heightIn(min = 50.dp)
                .testTag("google_account_connect_btn"),
          ) {
            Icon(Icons.Default.Link, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Connect Google Account", fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = onReconnect,
            modifier =
              Modifier.fillMaxWidth()
                .heightIn(min = 50.dp)
                .testTag("google_account_reconnect_btn"),
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reconnect Google Account", fontWeight = FontWeight.Bold)
          }

          OutlinedButton(
            onClick = onDisconnect,
            enabled = accountState.isConnected,
            modifier =
              Modifier.fillMaxWidth()
                .heightIn(min = 50.dp)
                .testTag("google_account_disconnect_btn"),
            colors =
              ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
          ) {
            Icon(Icons.Default.LinkOff, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Disconnect Google Account", fontWeight = FontWeight.Bold)
          }

          if (accountState.isConnected) {
            Button(
              onClick = onOpenSheetsDatabase,
              colors =
                ButtonDefaults.buttonColors(
                  containerColor = GoldAccentBg,
                  contentColor = GoldAccentText,
                ),
              modifier =
                Modifier.fillMaxWidth()
                  .heightIn(min = 50.dp)
                  .testTag("open_google_sheets_db_btn"),
            ) {
              Icon(Icons.Default.TableChart, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                "Google Sheets → Select/Create Jewellery Business Database",
                fontWeight = FontWeight.Bold,
              )
            }
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoogleSheetsDatabaseSection(
  accountState: GoogleAccountState,
  availableSpreadsheets: List<SpreadsheetInfo>,
  onBack: () -> Unit,
  onConnectAccountFirst: () -> Unit,
  onCreateDatabase: (String) -> Unit,
  onSelectDatabase: (SpreadsheetInfo) -> Unit,
  onLoadWorksheetRows: suspend (String) -> List<List<String>>,
  modifier: Modifier = Modifier,
) {
  var customDbTitle by remember { mutableStateOf(WorksheetSchemas.DEFAULT_DATABASE_NAME) }
  var pendingSwitchTarget by remember { mutableStateOf<SpreadsheetInfo?>(null) }
  var inspectedWorksheet by remember { mutableStateOf(WorksheetSchemas.SHEET_TRANSACTIONS) }
  var inspectedRows by remember { mutableStateOf<List<List<String>>>(emptyList()) }

  LaunchedEffect(accountState.selectedSpreadsheetId, inspectedWorksheet, accountState.lastSyncTimestamp) {
    if (accountState.hasDatabaseSelected) {
      inspectedRows = onLoadWorksheetRows(inspectedWorksheet)
    }
  }

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("google_sheets_database_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
          text = "Google Sheets Database",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    if (!accountState.isConnected) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = PendingAmberBg),
          border = BorderStroke(1.dp, PendingAmberBorder),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(
              text = "Connect your Google Account first to select or create 'Jewellery Business Database'.",
              fontWeight = FontWeight.Bold,
              color = PendingAmberText,
            )
            Button(onClick = onConnectAccountFirst) {
              Text("Connect Google Account")
            }
          }
        }
      }
    }

    // Current Database Card
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GoldAccentBg),
        border = BorderStroke(1.5.dp, GoldAccentBorder),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "ACTIVE CLOUD SPREADSHEET",
            fontSize = 12.sp,
            fontWeight = FontWeight.ExtraBold,
            color = GoldAccentText,
          )
          Text(
            text =
              accountState.selectedSpreadsheetName.ifBlank { "No Database Spreadsheet Selected" },
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            color = GoldAccentText,
          )
          if (accountState.selectedSpreadsheetId.isNotBlank()) {
            Text(
              text = "Spreadsheet ID: ${accountState.selectedSpreadsheetId}",
              fontSize = 12.sp,
              color = GoldAccentText.copy(alpha = 0.85f),
            )
          }
          Text(
            text =
              "Initialized Worksheets (${accountState.initializedWorksheets.size}/9): " +
                accountState.initializedWorksheets.joinToString(", ").ifBlank { "None" },
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = GoldAccentText,
          )

          Button(
            onClick = { onCreateDatabase(customDbTitle) },
            modifier =
              Modifier.fillMaxWidth()
                .heightIn(min = 50.dp)
                .testTag("select_or_create_default_db_btn"),
          ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              "Select / Create '${WorksheetSchemas.DEFAULT_DATABASE_NAME}'",
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    }

    // Existing Spreadsheets List (Change Database with Confirmation Warning)
    if (availableSpreadsheets.isNotEmpty()) {
      item {
        Text(
          text = "Available Spreadsheets (${availableSpreadsheets.size})",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
      }
      items(availableSpreadsheets, key = { it.spreadsheetId }) { sheet ->
        val isCurrent = sheet.spreadsheetId == accountState.selectedSpreadsheetId
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(sheet.name, fontWeight = FontWeight.Bold)
              Text(
                "ID: ${sheet.spreadsheetId} • ${sheet.worksheets.size} Worksheets",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            if (isCurrent) {
              Text(
                "ACTIVE",
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFF15803D),
              )
            } else {
              OutlinedButton(onClick = { pendingSwitchTarget = sheet }) {
                Text("Select")
              }
            }
          }
        }
      }
    }

    // Live Worksheet & Column Structure Inspector for the 9 Required Sheets
    if (accountState.hasDatabaseSelected) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth().testTag("worksheet_inspector_card"),
          shape = RoundedCornerShape(14.dp),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(
              text = "9 Required Database Worksheets & Live Columns",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
            )
            FlowRow(
              horizontalArrangement = Arrangement.spacedBy(6.dp),
              verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
              WorksheetSchemas.REQUIRED_WORKSHEETS.forEach { wsName ->
                FilterChip(
                  selected = inspectedWorksheet == wsName,
                  onClick = { inspectedWorksheet = wsName },
                  label = { Text(wsName, fontSize = 12.sp) },
                  modifier = Modifier.testTag("ws_chip_$wsName"),
                )
              }
            }

            val headers =
              WorksheetSchemas.HEADERS_MAP[inspectedWorksheet] ?: emptyList()
            Text(
              text = "Columns (${headers.size}): ${headers.joinToString(" | ")}",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary,
            )

            Text(
              text = "Stored Rows in '$inspectedWorksheet': ${maxOf(0, inspectedRows.size - 1)} data rows",
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
            )

            if (inspectedRows.size > 1) {
              Column(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp),
              ) {
                inspectedRows.take(6).forEachIndexed { idx, row ->
                  Text(
                    text =
                      if (idx == 0) "HEADER: ${row.joinToString(" | ")}"
                      else "ROW $idx: ${row.joinToString(" | ")}",
                    fontSize = 11.sp,
                    fontWeight = if (idx == 0) FontWeight.Bold else FontWeight.Normal,
                  )
                }
              }
            }
          }
        }
      }
    }
  }

  pendingSwitchTarget?.let { target ->
    AlertDialog(
      onDismissRequest = { pendingSwitchTarget = null },
      title = { Text("Confirm Change Database?") },
      text = {
        Text(
          "Switch active Google Sheets database to '${target.name}' (${target.spreadsheetId})? Existing records in the previous spreadsheet will not be deleted."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onSelectDatabase(target)
            pendingSwitchTarget = null
          }
        ) {
          Text("Switch Database")
        }
      },
      dismissButton = {
        TextButton(onClick = { pendingSwitchTarget = null }) { Text("Cancel") }
      },
    )
  }
}

@Composable
private fun SyncStatusSettingsSection(
  accountState: GoogleAccountState,
  pendingRecords: List<PendingSyncRecord>,
  onBack: () -> Unit,
  onSyncNow: () -> Unit,
  onToggleOfflineMode: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
) {
  val lastSyncStr =
    remember(accountState.lastSyncTimestamp) {
      if (accountState.lastSyncTimestamp <= 0L) "Never"
      else
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)
          .format(Date(accountState.lastSyncTimestamp))
    }

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("sync_status_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
          text = "Synchronization & Offline Queue",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text(
            text = "Current Sync Status: ${accountState.syncStatus.displayName}",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.testTag("sync_status_label"),
          )
          Text("Last Synchronized: $lastSyncStr")
          Text("Pending Sync Queue Count: ${pendingRecords.size}")

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Offline Mode (Simulate Disconnection)", fontWeight = FontWeight.Bold)
              Text(
                "When enabled, new transactions are saved locally to PendingSyncQueue and automatically synchronized when reconnected.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Switch(
              checked = accountState.isOfflineMode,
              onCheckedChange = onToggleOfflineMode,
              modifier = Modifier.testTag("offline_mode_switch"),
            )
          }

          Button(
            onClick = onSyncNow,
            modifier =
              Modifier.fillMaxWidth()
                .heightIn(min = 50.dp)
                .testTag("retry_sync_button"),
          ) {
            Icon(Icons.Default.Sync, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              if (pendingRecords.isNotEmpty()) "Retry Sync / Sync Now (${pendingRecords.size} Pending)"
              else "Sync Now",
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    }

    item {
      Text(
        text = "PendingSyncQueue (${pendingRecords.size})",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )
    }

    if (pendingRecords.isEmpty()) {
      item {
        Card(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "All local records are synchronized with Google Sheets.",
            modifier = Modifier.padding(16.dp),
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    } else {
      items(pendingRecords, key = { it.recordId }) { rec ->
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = PendingAmberBg),
          border = BorderStroke(1.dp, PendingAmberBorder),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
            ) {
              Text(
                text = "${rec.recordType.name} (${rec.operation.name})",
                fontWeight = FontWeight.Bold,
                color = PendingAmberText,
              )
              Text(
                text = rec.syncStatus.displayName,
                fontWeight = FontWeight.ExtraBold,
                color = PendingAmberText,
              )
            }
            Text("RecordID: ${rec.recordId}", fontSize = 12.sp, color = PendingAmberText)
            Text("RetryCount: ${rec.retryCount}", fontSize = 12.sp, color = PendingAmberText)
            if (rec.lastError.isNotBlank()) {
              Text("LastError: ${rec.lastError}", fontSize = 12.sp, color = MaterialTheme.colorScheme.error)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun SettingsSectionRowCard(
  title: String,
  subtitle: String,
  icon: ImageVector,
  testTag: String,
  onClick: () -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).testTag(testTag),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Icon(
        imageVector = icon,
        contentDescription = title,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(28.dp),
      )
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
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
private fun SettingsCategoryHeader(title: String) {
  Text(
    text = title.uppercase(),
    style = MaterialTheme.typography.labelLarge,
    fontWeight = FontWeight.ExtraBold,
    color = GoldAccentText,
    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
  )
}

@Composable
private fun BusinessProfileSettingsForm(
  profile: BusinessProfile,
  onBack: () -> Unit,
  onSave: (BusinessProfile, () -> Unit, (String) -> Unit) -> Unit,
  modifier: Modifier = Modifier,
) {
  val strings = com.example.ui.i18n.AppStrings.forLanguage(profile.language)
  var shopName by remember(profile) { mutableStateOf(profile.shopName) }
  var ownerName by remember(profile) { mutableStateOf(profile.ownerName) }
  var mobileNumber by remember(profile) { mutableStateOf(profile.mobileNumber) }
  var whatsappNumber by remember(profile) { mutableStateOf(profile.whatsappNumber.ifBlank { profile.mobileNumber }) }
  var email by remember(profile) { mutableStateOf(profile.email) }
  var address by remember(profile) { mutableStateOf(profile.address) }
  var city by remember(profile) { mutableStateOf(profile.city) }
  var district by remember(profile) { mutableStateOf(profile.district) }
  var state by remember(profile) { mutableStateOf(profile.state) }
  var pinCode by remember(profile) { mutableStateOf(profile.pinCode) }
  var panNumber by remember(profile) { mutableStateOf(profile.panNumber) }
  var gstNumber by remember(profile) { mutableStateOf(profile.gstNumber) }
  var gstEnabled by remember(profile) { mutableStateOf(profile.gstEnabled) }
  var bankAccountNumber by remember(profile) { mutableStateOf(profile.bankAccountNumber) }
  var bankName by remember(profile) { mutableStateOf(profile.bankName) }
  var branchName by remember(profile) { mutableStateOf(profile.branchName) }
  var ifsc by remember(profile) { mutableStateOf(profile.ifsc) }
  var upiId by remember(profile) { mutableStateOf(profile.upiId) }
  var invoicePrefix by remember(profile) { mutableStateOf(profile.invoicePrefix) }
  var invoiceStartingNumber by remember(profile) {
    mutableStateOf(profile.invoiceStartingNumber.toString())
  }
  var businessLogoUri by remember(profile) { mutableStateOf(profile.businessLogoUri) }
  var invoiceFooter by remember(profile) { mutableStateOf(profile.invoiceFooter) }
  var termsAndConditions by remember(profile) { mutableStateOf(profile.termsAndConditions) }

  var errorMessage by remember { mutableStateOf<String?>(null) }
  var successMessage by remember { mutableStateOf<String?>(null) }
  var gstValidationNotice by remember { mutableStateOf<String?>(null) }

  val updatedFormatted =
    remember(profile.updatedAt) {
      SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(profile.updatedAt))
    }

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("business_profile_form"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("bp_back_button")) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Column {
          Text(
            text = "EDIT BUSINESS PROFILE",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "${strings.editBusinessProfile} • ID: ${profile.businessId} • Updated: $updatedFormatted by ${profile.updatedBy}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    successMessage?.let { msg ->
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = Color(0xFFDCFCE7)),
          border = BorderStroke(1.dp, Color(0xFF15803D)),
          modifier = Modifier.fillMaxWidth().testTag("bp_success_banner"),
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = msg,
              color = Color(0xFF14532D),
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "व्यवसाय प्रोफ़ाइल सफलतापूर्वक अपडेट हो गई।",
              color = Color(0xFF166534),
              style = MaterialTheme.typography.bodySmall,
            )
          }
        }
      }
    }

    gstValidationNotice?.let { notice ->
      item {
        Card(
          colors = CardDefaults.cardColors(containerColor = GoldAccentBg),
          border = BorderStroke(1.dp, GoldAccentBorder),
          modifier = Modifier.fillMaxWidth().testTag("bp_gst_validation_notice"),
        ) {
          Text(
            text = notice,
            color = GoldAccentText,
            fontWeight = FontWeight.SemiBold,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp),
          )
        }
      }
    }

    errorMessage?.let { err ->
      item {
        Card(
          colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
          modifier = Modifier.fillMaxWidth().testTag("bp_error_banner"),
        ) {
          Text(
            text = err,
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(14.dp),
          )
        }
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text(
            text = "1. Shop & Owner Identity",
            fontWeight = FontWeight.ExtraBold,
            color = GoldAccentText,
          )
          OutlinedTextField(
            value = shopName,
            onValueChange = {
              shopName = it
              errorMessage = null
              successMessage = null
            },
            label = { Text("${strings.shopName} (Shop/Business Name) *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("bp_shop_name_input"),
          )
          OutlinedTextField(
            value = ownerName,
            onValueChange = {
              ownerName = it
              errorMessage = null
              successMessage = null
            },
            label = { Text("${strings.ownerName} (Shop Owner Name) *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("bp_owner_name_input"),
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedTextField(
              value = mobileNumber,
              onValueChange = {
                mobileNumber = it
                errorMessage = null
                successMessage = null
              },
              label = { Text("${strings.mobile} *") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("bp_mobile_input"),
            )
            OutlinedTextField(
              value = whatsappNumber,
              onValueChange = {
                whatsappNumber = it
                errorMessage = null
                successMessage = null
              },
              label = { Text(strings.whatsapp) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("bp_whatsapp_input"),
            )
          }
          OutlinedTextField(
            value = email,
            onValueChange = {
              email = it
              errorMessage = null
              successMessage = null
            },
            label = { Text(strings.email) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("bp_email_input"),
          )

          HorizontalDivider()
          Text(
            text = "2. Complete Shop Address & Location",
            fontWeight = FontWeight.ExtraBold,
            color = GoldAccentText,
          )
          OutlinedTextField(
            value = address,
            onValueChange = {
              address = it
              errorMessage = null
              successMessage = null
            },
            label = { Text("${strings.address} (Complete Address)") },
            modifier = Modifier.fillMaxWidth().testTag("bp_address_input"),
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedTextField(
              value = city,
              onValueChange = {
                city = it
                errorMessage = null
                successMessage = null
              },
              label = { Text(strings.city) },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("bp_city_input"),
            )
            OutlinedTextField(
              value = district,
              onValueChange = {
                district = it
                errorMessage = null
                successMessage = null
              },
              label = { Text(strings.district) },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("bp_district_input"),
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedTextField(
              value = state,
              onValueChange = {
                state = it
                errorMessage = null
                successMessage = null
              },
              label = { Text(strings.state) },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("bp_state_input"),
            )
            OutlinedTextField(
              value = pinCode,
              onValueChange = {
                pinCode = it
                errorMessage = null
                successMessage = null
              },
              label = { Text("${strings.pin} (PIN Code)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("bp_pin_input"),
            )
          }

          HorizontalDivider()
          Text(
            text = "3. Tax Identifiers (PAN & Editable GSTIN)",
            fontWeight = FontWeight.ExtraBold,
            color = GoldAccentText,
          )
          OutlinedTextField(
            value = panNumber,
            onValueChange = {
              panNumber = it.uppercase()
              errorMessage = null
              successMessage = null
            },
            label = { Text("${strings.pan} (PAN Number)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("bp_pan_input"),
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Enable GST for Business", fontWeight = FontWeight.Bold)
              Text(
                "GSTIN can be added, edited, or left blank when GST is not applicable.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Switch(
              checked = gstEnabled,
              onCheckedChange = { gstEnabled = it },
              modifier = Modifier.testTag("bp_gst_enabled_switch"),
            )
          }
          OutlinedTextField(
            value = gstNumber,
            onValueChange = {
              gstNumber = it.uppercase()
              errorMessage = null
              successMessage = null
              val clean = it.trim().uppercase()
              if (clean.isNotBlank()) {
                val res = com.example.domain.calculation.ValidationService.validateGstinFormat(clean)
                gstValidationNotice =
                  if (res is com.example.domain.calculation.ValidationResult.Valid) {
                    com.example.domain.calculation.ValidationService.GSTIN_LOCAL_VALIDATION_NOTICE
                  } else {
                    null
                  }
              } else {
                gstValidationNotice = null
              }
            },
            label = { Text("${strings.gstin} (GST Number / GSTIN — Optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("bp_gst_input"),
          )
          if (gstNumber.isNotBlank()) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Text(
                text = com.example.domain.calculation.ValidationService.GSTIN_LOCAL_VALIDATION_NOTICE,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f),
              )
              TextButton(
                onClick = {
                  gstNumber = ""
                  gstEnabled = false
                  gstValidationNotice = "GSTIN cleared. Save changes to apply."
                },
                modifier = Modifier.testTag("bp_remove_gstin_btn"),
              ) {
                Text("Remove GSTIN", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
              }
            }
          }

          HorizontalDivider()
          Text(
            text = "4. Bank Account & UPI Details",
            fontWeight = FontWeight.ExtraBold,
            color = GoldAccentText,
          )
          OutlinedTextField(
            value = bankAccountNumber,
            onValueChange = {
              bankAccountNumber = it
              errorMessage = null
              successMessage = null
            },
            label = { Text("${strings.bankAccount} (Bank Account Number)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("bp_bank_account_input"),
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedTextField(
              value = bankName,
              onValueChange = {
                bankName = it
                errorMessage = null
                successMessage = null
              },
              label = { Text(strings.bankName) },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("bp_bank_name_input"),
            )
            OutlinedTextField(
              value = branchName,
              onValueChange = {
                branchName = it
                errorMessage = null
                successMessage = null
              },
              label = { Text("${strings.branch} (Branch Name)") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("bp_branch_input"),
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedTextField(
              value = ifsc,
              onValueChange = {
                ifsc = it.uppercase()
                errorMessage = null
                successMessage = null
              },
              label = { Text("${strings.ifsc} (IFSC Code)") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("bp_ifsc_input"),
            )
            OutlinedTextField(
              value = upiId,
              onValueChange = {
                upiId = it
                errorMessage = null
                successMessage = null
              },
              label = { Text(strings.upiId) },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("bp_upi_input"),
            )
          }

          HorizontalDivider()
          Text(
            text = "5. Invoice Numbering, Logo, Footer & Terms",
            fontWeight = FontWeight.ExtraBold,
            color = GoldAccentText,
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedTextField(
              value = invoicePrefix,
              onValueChange = {
                invoicePrefix = it
                errorMessage = null
                successMessage = null
              },
              label = { Text("${strings.invoicePrefix} (e.g. HGR-)") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("bp_invoice_prefix_input"),
            )
            OutlinedTextField(
              value = invoiceStartingNumber,
              onValueChange = {
                invoiceStartingNumber = it
                errorMessage = null
                successMessage = null
              },
              label = { Text(strings.startingInvoiceNumber) },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("bp_invoice_start_input"),
            )
          }
          OutlinedTextField(
            value = businessLogoUri,
            onValueChange = {
              businessLogoUri = it
              errorMessage = null
              successMessage = null
            },
            label = { Text("Business Logo (Monogram / Logo URI)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("bp_logo_input"),
          )
          OutlinedTextField(
            value = invoiceFooter,
            onValueChange = {
              invoiceFooter = it
              errorMessage = null
              successMessage = null
            },
            label = { Text(strings.invoiceFooter) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("bp_invoice_footer_input"),
          )
          OutlinedTextField(
            value = termsAndConditions,
            onValueChange = {
              termsAndConditions = it
              errorMessage = null
              successMessage = null
            },
            label = { Text(strings.termsAndConditions) },
            modifier = Modifier.fillMaxWidth().testTag("bp_terms_input"),
          )

          Spacer(modifier = Modifier.height(6.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            OutlinedButton(
              onClick = onBack,
              modifier =
                Modifier.weight(1f)
                  .heightIn(min = 52.dp)
                  .testTag("cancel_business_profile_button"),
            ) {
              Text(strings.cancelButton, fontWeight = FontWeight.Bold)
            }
            Button(
              onClick = {
                val startNum = invoiceStartingNumber.trim().toIntOrNull()
                if (startNum == null || startNum <= 0) {
                  errorMessage = "Invoice Starting Number must be a valid positive number."
                  return@Button
                }
                val cleanGstin = gstNumber.trim().uppercase()
                if (cleanGstin.isNotBlank()) {
                  val gstRes = com.example.domain.calculation.ValidationService.validateGstinFormat(cleanGstin)
                  if (gstRes is com.example.domain.calculation.ValidationResult.Invalid) {
                    errorMessage = gstRes.message
                    return@Button
                  }
                  gstValidationNotice =
                    com.example.domain.calculation.ValidationService.GSTIN_LOCAL_VALIDATION_NOTICE
                }
                val updated =
                  profile.copy(
                    shopName = shopName.trim(),
                    ownerName = ownerName.trim(),
                    mobileNumber = mobileNumber.trim(),
                    whatsappNumber = whatsappNumber.trim().ifBlank { mobileNumber.trim() },
                    email = email.trim(),
                    address = address.trim(),
                    city = city.trim(),
                    district = district.trim(),
                    state = state.trim(),
                    pinCode = pinCode.trim(),
                    panNumber = panNumber.trim().uppercase(),
                    gstNumber = cleanGstin,
                    gstEnabled = gstEnabled,
                    bankName = bankName.trim(),
                    branchName = branchName.trim(),
                    bankAccountNumber = bankAccountNumber.trim(),
                    ifsc = ifsc.trim().uppercase(),
                    upiId = upiId.trim(),
                    invoicePrefix = invoicePrefix.trim(),
                    invoiceStartingNumber = startNum,
                    businessLogoUri = businessLogoUri.trim(),
                    invoiceFooter = invoiceFooter.trim(),
                    termsAndConditions = termsAndConditions.trim(),
                    updatedAt = System.currentTimeMillis(),
                    updatedBy = ownerName.trim().ifBlank { "Owner" },
                  )
                onSave(
                  updated,
                  {
                    successMessage = "Business profile updated successfully."
                    errorMessage = null
                  },
                  { err ->
                    errorMessage = err
                    successMessage = null
                  },
                )
              },
              modifier =
                Modifier.weight(1.4f)
                  .heightIn(min = 52.dp)
                  .testTag("save_business_profile_button"),
            ) {
              Icon(Icons.Default.Save, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text(strings.saveChanges, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MetalRateSettingsForm(
  metalRate: MetalRate,
  calculationEngine: CalculationEngine,
  onBack: () -> Unit,
  onSave: (String, RateUnit, String, RateUnit, () -> Unit, (String) -> Unit) -> Unit,
  modifier: Modifier = Modifier,
) {
  var goldRateInput by remember(metalRate) { mutableStateOf(metalRate.goldRate.toPlainString()) }
  var goldUnit by remember(metalRate) { mutableStateOf(metalRate.goldRateUnit) }
  var silverRateInput by remember(metalRate) {
    mutableStateOf(metalRate.silverRate.toPlainString())
  }
  var silverUnit by remember(metalRate) { mutableStateOf(metalRate.silverRateUnit) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  val parsedGold = calculationEngine.parseSafeBigDecimal(goldRateInput) ?: BigDecimal.ZERO
  val parsedSilver = calculationEngine.parseSafeBigDecimal(silverRateInput) ?: BigDecimal.ZERO
  val goldPerGram = calculationEngine.normalizeRatePerGram(parsedGold, goldUnit)
  val silverPerGram = calculationEngine.normalizeRatePerGram(parsedSilver, silverUnit)

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("metal_rates_settings_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
          text = "Gold & Silver Rate Management",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    errorMessage?.let { err ->
      item {
        Card(
          colors =
            CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = err,
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(14.dp),
          )
        }
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = GoldAccentBg),
        border = BorderStroke(1.2.dp, GoldAccentBorder),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text(
            text = "Gold Rate Configuration",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = GoldAccentText,
          )
          Text(
            text = "Currently Active Unit: ${goldUnit.displayName}",
            fontWeight = FontWeight.Bold,
            color = GoldAccentText,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RateUnit.entries.forEach { unit ->
              FilterChip(
                selected = goldUnit == unit,
                onClick = { goldUnit = unit },
                label = { Text(unit.displayName) },
                modifier = Modifier.testTag("gold_unit_${unit.name.lowercase()}"),
              )
            }
          }
          OutlinedTextField(
            value = goldRateInput,
            onValueChange = {
              goldRateInput = it
              errorMessage = null
            },
            label = { Text("Gold Rate (₹ ${goldUnit.displayName})") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("settings_gold_rate_input"),
          )
          Text(
            text = "Effective Per-Gram Rate: ₹${calculationEngine.formatMoney(goldPerGram)} / gram",
            fontWeight = FontWeight.SemiBold,
            color = GoldAccentText,
          )
        }
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text(
            text = "Silver Rate Configuration",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
          )
          Text(
            text = "Currently Active Unit: ${silverUnit.displayName}",
            fontWeight = FontWeight.Bold,
          )
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RateUnit.entries.forEach { unit ->
              FilterChip(
                selected = silverUnit == unit,
                onClick = { silverUnit = unit },
                label = { Text(unit.displayName) },
                modifier = Modifier.testTag("silver_unit_${unit.name.lowercase()}"),
              )
            }
          }
          OutlinedTextField(
            value = silverRateInput,
            onValueChange = {
              silverRateInput = it
              errorMessage = null
            },
            label = { Text("Silver Rate (₹ ${silverUnit.displayName})") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("settings_silver_rate_input"),
          )
          Text(
            text = "Effective Per-Gram Rate: ₹${calculationEngine.formatMoney(silverPerGram)} / gram",
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }

    item {
      Button(
        onClick = {
          onSave(
            goldRateInput,
            goldUnit,
            silverRateInput,
            silverUnit,
            { onBack() },
            { err -> errorMessage = err },
          )
        },
        modifier =
          Modifier.fillMaxWidth()
            .heightIn(min = 54.dp)
            .testTag("save_metal_rates_button"),
      ) {
        Icon(Icons.Default.Save, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Save Gold & Silver Rates", fontWeight = FontWeight.Bold, fontSize = 16.sp)
      }
    }
  }
}

@Composable
private fun InvoiceSettingsForm(
  profile: BusinessProfile,
  onBack: () -> Unit,
  onSave: (BusinessProfile, () -> Unit, (String) -> Unit) -> Unit,
  modifier: Modifier = Modifier,
) {
  var prefix by remember(profile) { mutableStateOf(profile.invoicePrefix) }
  var startNum by remember(profile) { mutableStateOf(profile.invoiceStartingNumber.toString()) }
  var weightDecimals by remember(profile) { mutableStateOf(profile.weightDecimalPlaces) }
  var divisorStr by remember(profile) { mutableStateOf(profile.purityDivisor.toPlainString()) }
  var logoText by remember(profile) { mutableStateOf(profile.businessLogoUri) }
  var logoPos by remember(profile) { mutableStateOf(profile.logoPosition) }
  var gstEnabled by remember(profile) { mutableStateOf(profile.gstEnabled) }
  var cgstStr by remember(profile) { mutableStateOf(profile.cgstRatePercent.toPlainString()) }
  var sgstStr by remember(profile) { mutableStateOf(profile.sgstRatePercent.toPlainString()) }
  var igstStr by remember(profile) { mutableStateOf(profile.igstRatePercent.toPlainString()) }
  var showBank by remember(profile) { mutableStateOf(profile.showBankDetailsOnInvoice) }
  var showUpi by remember(profile) { mutableStateOf(profile.showUpiOnInvoice) }
  var showPan by remember(profile) { mutableStateOf(profile.showPanOnInvoice) }
  var showGst by remember(profile) { mutableStateOf(profile.showGstOnInvoice) }
  var showCustomerPan by remember(profile) { mutableStateOf(profile.showCustomerPanOnInvoice) }
  var showCustomerGst by remember(profile) { mutableStateOf(profile.showCustomerGstOnInvoice) }
  var showCustomerBalance by remember(profile) { mutableStateOf(profile.showCustomerBalanceOnInvoice) }
  var terms by remember(profile) { mutableStateOf(profile.termsAndConditions) }
  var footer by remember(profile) { mutableStateOf(profile.invoiceFooter) }
  var errorMsg by remember { mutableStateOf<String?>(null) }

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("invoice_settings_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
          text = "Invoice, Branding & GST Settings",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    errorMsg?.let { err ->
      item {
        Text(err, color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
      }
    }

    item {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text("1. Invoice Numbering & Branding", fontWeight = FontWeight.ExtraBold)
          OutlinedTextField(
            value = prefix,
            onValueChange = { prefix = it },
            label = { Text("Invoice Number Prefix (e.g. HGR-)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("invoice_prefix_input"),
          )
          OutlinedTextField(
            value = startNum,
            onValueChange = { startNum = it },
            label = { Text("Invoice Starting Number (e.g. 1)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("invoice_starting_number_input"),
          )
          Text(
            text = "Permanent Format Preview: ${prefix.ifBlank { "HGR-" }}${String.format(Locale.US, "%06d", startNum.toIntOrNull() ?: 1)}",
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary,
          )

          OutlinedTextField(
            value = logoText,
            onValueChange = { logoText = it },
            label = { Text("Business Logo Monogram / URI (Optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("invoice_logo_input"),
          )

          Text("Header & Logo Alignment:", fontWeight = FontWeight.Bold)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LogoPosition.entries.forEach { pos ->
              FilterChip(
                selected = logoPos == pos,
                onClick = { logoPos = pos },
                label = { Text(pos.displayName) },
                modifier = Modifier.testTag("logo_pos_${pos.name.lowercase()}"),
              )
            }
          }

          HorizontalDivider()
          Text("2. Optional GST Configuration", fontWeight = FontWeight.ExtraBold)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Enable GST on Invoices", fontWeight = FontWeight.Bold)
              Text(
                "Only applies tax when enabled (default Off for non-GST receipts)",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            Switch(
              checked = gstEnabled,
              onCheckedChange = { gstEnabled = it },
              modifier = Modifier.testTag("invoice_gst_switch"),
            )
          }

          if (gstEnabled) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              OutlinedTextField(
                value = cgstStr,
                onValueChange = { cgstStr = it },
                label = { Text("CGST %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("invoice_cgst_input"),
              )
              OutlinedTextField(
                value = sgstStr,
                onValueChange = { sgstStr = it },
                label = { Text("SGST %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("invoice_sgst_input"),
              )
              OutlinedTextField(
                value = igstStr,
                onValueChange = { igstStr = it },
                label = { Text("IGST %") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("invoice_igst_input"),
              )
            }
          }

          HorizontalDivider()
          Text("3. Invoice Field Visibility", fontWeight = FontWeight.ExtraBold)

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("Show Customer Ledger Balance (Money/Gold/Silver)")
            Switch(
              checked = showCustomerBalance,
              onCheckedChange = { showCustomerBalance = it },
              modifier = Modifier.testTag("invoice_show_balance_switch"),
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("Show Shop PAN & GSTIN")
            Switch(
              checked = showPan && showGst,
              onCheckedChange = {
                showPan = it
                showGst = it
              },
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("Show Customer PAN & GSTIN")
            Switch(
              checked = showCustomerPan && showCustomerGst,
              onCheckedChange = {
                showCustomerPan = it
                showCustomerGst = it
              },
            )
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("Show Bank & UPI Settlement Details")
            Switch(
              checked = showBank && showUpi,
              onCheckedChange = {
                showBank = it
                showUpi = it
              },
            )
          }

          HorizontalDivider()
          Text("4. Terms & Conditions and Footer", fontWeight = FontWeight.ExtraBold)
          OutlinedTextField(
            value = terms,
            onValueChange = { terms = it },
            label = { Text("Terms & Conditions") },
            modifier = Modifier.fillMaxWidth().testTag("invoice_terms_input"),
          )
          OutlinedTextField(
            value = footer,
            onValueChange = { footer = it },
            label = { Text("Invoice Footer Message") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("invoice_footer_input"),
          )

          Button(
            onClick = {
              val parsedStart = startNum.trim().toIntOrNull()
              val parsedDivisor = divisorStr.trim().toBigDecimalOrNull()
              val parsedCgst = cgstStr.trim().toBigDecimalOrNull() ?: BigDecimal("1.50")
              val parsedSgst = sgstStr.trim().toBigDecimalOrNull() ?: BigDecimal("1.50")
              val parsedIgst = igstStr.trim().toBigDecimalOrNull() ?: BigDecimal.ZERO
              if (parsedStart == null || parsedStart <= 0) {
                errorMsg = "Please enter a valid positive starting number."
                return@Button
              }
              if (parsedDivisor == null || parsedDivisor <= BigDecimal.ZERO) {
                errorMsg = "Purity divisor must be greater than 0."
                return@Button
              }
              onSave(
                profile.copy(
                  invoicePrefix = prefix.trim(),
                  invoiceStartingNumber = parsedStart,
                  weightDecimalPlaces = weightDecimals,
                  purityDivisor = parsedDivisor,
                  businessLogoUri = logoText.trim(),
                  logoPosition = logoPos,
                  gstEnabled = gstEnabled,
                  cgstRatePercent = parsedCgst,
                  sgstRatePercent = parsedSgst,
                  igstRatePercent = parsedIgst,
                  showBankDetailsOnInvoice = showBank,
                  showUpiOnInvoice = showUpi,
                  showPanOnInvoice = showPan,
                  showGstOnInvoice = showGst,
                  showCustomerPanOnInvoice = showCustomerPan,
                  showCustomerGstOnInvoice = showCustomerGst,
                  showCustomerBalanceOnInvoice = showCustomerBalance,
                  termsAndConditions = terms.trim(),
                  invoiceFooter = footer.trim(),
                ),
                { onBack() },
                { err -> errorMsg = err },
              )
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp).testTag("save_invoice_settings_btn"),
          ) {
            Text("Save Invoice Settings", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

@Composable
private fun LanguageSettingsSection(
  currentLanguage: AppLanguage,
  onBack: () -> Unit,
  onSwitchLanguage: (AppLanguage) -> Unit,
  modifier: Modifier = Modifier,
) {
  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("language_settings_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
          text = "Application Language",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    item {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text(
            text = "Select Application Display Language:",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          AppLanguage.entries.forEach { lang ->
            Surface(
              shape = RoundedCornerShape(12.dp),
              color =
                if (currentLanguage == lang) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surfaceVariant,
              modifier =
                Modifier.fillMaxWidth()
                  .clickable { onSwitchLanguage(lang) }
                  .testTag("lang_option_${lang.code}"),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Text(lang.displayName, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                if (currentLanguage == lang) {
                  Text("ACTIVE", fontWeight = FontWeight.ExtraBold, color = MaterialTheme.colorScheme.primary)
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun DataAndBackupSection(
  customerCount: Int,
  transactionCount: Int,
  invoiceCount: Int,
  onBack: () -> Unit,
  onResetDemoData: () -> Unit,
  viewModel: JewelleryViewModel? = null,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  var showResetConfirm by remember { mutableStateOf(false) }

  val shareableAppInstallUrl =
    "https://ais-pre-v27yh25ryy44r37xngaup5-809184267579.asia-southeast1.run.app"

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("data_backup_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Text(
          text = "Backup, Restore & Data Management",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    if (viewModel != null) {
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
          ) {
            Text(
              text = "Full Database Backup & Restore",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.ExtraBold,
            )
            Text(
              text = "Backup or restore all Business Profile, Customers, Vendors, Transactions, Invoices, Inventory & Reminders.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Button(
                onClick = { viewModel.createLocalJsonBackupFile(context) },
                modifier = Modifier.weight(1f).testTag("settings_backup_local_btn"),
              ) {
                Text("Local JSON Backup")
              }
              Button(
                onClick = { viewModel.createGoogleDriveBackup(context) },
                modifier = Modifier.weight(1f).testTag("settings_backup_drive_btn"),
              ) {
                Text("Google Drive Backup")
              }
            }
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              OutlinedButton(
                onClick = { viewModel.restoreFromLocalBackupFile(context) },
                modifier = Modifier.weight(1f).testTag("settings_restore_local_btn"),
              ) {
                Text("Restore Local JSON")
              }
              OutlinedButton(
                onClick = { viewModel.restoreFromGoogleSheetsBackup() },
                modifier = Modifier.weight(1f).testTag("settings_restore_sheets_btn"),
              ) {
                Text("Restore from Sheets")
              }
            }
          }
        }
      }
    }

    item {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "Local Cache & Google Sheets Storage (Stage 2)",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          Text("• Stored Customers: $customerCount")
          Text("• Stored Transactions: $transactionCount")
          Text("• Stored Invoices: $invoiceCount")
          Text(
            text = "Local data is persisted via Room SQLite and synchronized with the 9 worksheets in your connected Google Sheet.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          Spacer(modifier = Modifier.height(6.dp))
          OutlinedButton(
            onClick = { showResetConfirm = true },
            modifier = Modifier.fillMaxWidth(),
          ) {
            Icon(Icons.Default.Restore, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Restore / Reset Sample Jewellery Data")
          }
        }
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = GoldAccentBg),
        border = BorderStroke(1.2.dp, GoldAccentBorder),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          Text(
            text = "Share App & APK Installation Link",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = GoldAccentText,
          )
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(
              text = shareableAppInstallUrl,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(10.dp),
            )
          }
          Button(
            onClick = {
              val intent =
                Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_SUBJECT, "Install Jewellery Business Manager")
                  putExtra(
                    Intent.EXTRA_TEXT,
                    "Install Jewellery Business Manager App: $shareableAppInstallUrl",
                  )
                }
              context.startActivity(Intent.createChooser(intent, "Share App Link"))
            },
            modifier = Modifier.fillMaxWidth().testTag("share_app_install_link_btn"),
          ) {
            Icon(Icons.Default.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Share Installation Link", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }

  if (showResetConfirm) {
    AlertDialog(
      onDismissRequest = { showResetConfirm = false },
      title = { Text("Restore Sample Data?") },
      text = {
        Text(
          "This will reset local transactions, customers, and invoices back to the initial demonstration dataset."
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onResetDemoData()
            showResetConfirm = false
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
          Text("Reset Data")
        }
      },
      dismissButton = {
        TextButton(onClick = { showResetConfirm = false }) { Text("Cancel") }
      },
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun CalculationRulesSettingsForm(
  profile: BusinessProfile,
  onBack: () -> Unit,
  onSave: (BusinessProfile, () -> Unit, (String) -> Unit) -> Unit,
  modifier: Modifier = Modifier,
) {
  var weightDec by remember(profile) { mutableStateOf(profile.weightDecimalPlaces) }
  var tunchDec by remember(profile) { mutableStateOf(profile.tunchDecimalPlaces) }
  var moneyDec by remember(profile) { mutableStateOf(profile.moneyDecimalPlaces) }
  var purityDivisor by remember(profile) { mutableStateOf(profile.purityDivisor) }
  var maxTunch by remember(profile) { mutableStateOf(profile.maxTunchValue) }
  var allowEditPure99 by remember(profile) { mutableStateOf(profile.allowEditingPure99Purity) }
  var enableScrapDeduction by remember(profile) { mutableStateOf(profile.enabledDeduction) }
  var statusMsg by remember { mutableStateOf<String?>(null) }

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("calculation_rules_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack, modifier = Modifier.testTag("calc_rules_back_button")) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
        }
        Column {
          Text(
            text = "Configurable Jewellery Calculation Rules",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Configure decimals, Tunch divisor (100 or 1000), 99% purity editing & scrap deductions",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }

    if (statusMsg != null) {
      item {
        Surface(
          color = MaterialTheme.colorScheme.secondaryContainer,
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth(),
        ) {
          Text(
            text = statusMsg!!,
            modifier = Modifier.padding(12.dp),
            fontWeight = FontWeight.SemiBold,
          )
        }
      }
    }

    item {
      Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text("Weight Decimal Places (grams)", fontWeight = FontWeight.Bold)
          FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(2, 3, 4).forEach { dec ->
              FilterChip(
                selected = weightDec == dec,
                onClick = { weightDec = dec },
                label = { Text("$dec decimals (e.g. 1.${"0".repeat(dec)} g)") },
                modifier = Modifier.testTag("weight_dec_$dec"),
              )
            }
          }

          Text("Tunch / Purity Decimal Places", fontWeight = FontWeight.Bold)
          FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(2, 3).forEach { dec ->
              FilterChip(
                selected = tunchDec == dec,
                onClick = { tunchDec = dec },
                label = { Text("$dec decimals") },
                modifier = Modifier.testTag("tunch_dec_$dec"),
              )
            }
          }

          Text("Purity Formula Divisor & Scale", fontWeight = FontWeight.Bold)
          FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = purityDivisor.compareTo(BigDecimal("100")) == 0,
              onClick = {
                purityDivisor = BigDecimal("100")
                maxTunch = BigDecimal("100.00")
              },
              label = { Text("Percentage (Gross × Tunch ÷ 100)") },
              modifier = Modifier.testTag("divisor_100"),
            )
            FilterChip(
              selected = purityDivisor.compareTo(BigDecimal("1000")) == 0,
              onClick = {
                purityDivisor = BigDecimal("1000")
                maxTunch = BigDecimal("1000.00")
              },
              label = { Text("Per-Thousand / Touch (Gross × Tunch ÷ 1000)") },
              modifier = Modifier.testTag("divisor_1000"),
            )
          }

          HorizontalDivider()

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Allow Editing 99% Pure Gold Purity", fontWeight = FontWeight.Bold)
              Text(
                "When enabled, 99% Pure mode defaults to 99.00% but can be edited (e.g. 99.50%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            androidx.compose.material3.Switch(
              checked = allowEditPure99,
              onCheckedChange = { allowEditPure99 = it },
              modifier = Modifier.testTag("switch_allow_edit_pure99"),
            )
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Enable Optional Scrap Deductions", fontWeight = FontWeight.Bold)
              Text(
                "Allows Weight, Percentage, or Amount deduction on Scrap Gold/Silver transactions",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
            }
            androidx.compose.material3.Switch(
              checked = enableScrapDeduction,
              onCheckedChange = { enableScrapDeduction = it },
              modifier = Modifier.testTag("switch_enable_scrap_deduction"),
            )
          }

          Button(
            onClick = {
              onSave(
                profile.copy(
                  weightDecimalPlaces = weightDec,
                  tunchDecimalPlaces = tunchDec,
                  moneyDecimalPlaces = moneyDec,
                  purityDivisor = purityDivisor,
                  maxTunchValue = maxTunch,
                  allowEditingPure99Purity = allowEditPure99,
                  enabledDeduction = enableScrapDeduction,
                ),
                { statusMsg = "Calculation rules updated." },
                { err -> statusMsg = err },
              )
            },
            modifier = Modifier.fillMaxWidth().height(52.dp).testTag("save_calc_rules_button"),
          ) {
            Text("Save Calculation Rules", fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}
