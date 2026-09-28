package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.example.domain.model.InvoicePrintFormat
import com.example.domain.model.ReminderStatus
import com.example.domain.model.ReminderType
import com.example.domain.model.UserAccessMode
import com.example.ui.i18n.AppStrings
import com.example.ui.viewmodel.JewelleryViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GstAndPrintingConfigurationSection(
  viewModel: JewelleryViewModel,
  onBack: () -> Unit,
) {
  val profile by viewModel.businessProfile.collectAsState()
  val strings = AppStrings.forLanguage(profile.language)

  var gstEnabled by remember(profile) { mutableStateOf(profile.gstEnabled) }
  var gstNumber by remember(profile) { mutableStateOf(profile.gstNumber) }
  var gstStateName by remember(profile) { mutableStateOf(profile.state.ifBlank { profile.gstStateName }) }
  var gstRegistrationType by remember(profile) { mutableStateOf(profile.gstRegistrationType) }
  var gstTaxTreatment by remember(profile) { mutableStateOf(profile.gstTaxTreatment) }
  var defaultGstStr by remember(profile) { mutableStateOf(profile.defaultGstRatePercent.toPlainString()) }
  var cgstStr by remember(profile) { mutableStateOf(profile.cgstRatePercent.toPlainString()) }
  var sgstStr by remember(profile) { mutableStateOf(profile.sgstRatePercent.toPlainString()) }
  var igstStr by remember(profile) { mutableStateOf(profile.igstRatePercent.toPlainString()) }
  var applyToGold by remember(profile) { mutableStateOf(profile.gstApplyToGold) }
  var applyToSilver by remember(profile) { mutableStateOf(profile.gstApplyToSilver) }
  var applyToMaking by remember(profile) { mutableStateOf(profile.gstApplyToMakingCharges) }
  var defaultPaperSize by remember(profile) { mutableStateOf(profile.defaultInvoicePaperSize) }
  var fastDashboard by remember(profile) { mutableStateOf(profile.fastDashboardLoading) }
  var compactRows by remember(profile) { mutableStateOf(profile.compactTransactionRows) }
  var gstMessage by remember { mutableStateOf<String?>(null) }
  var gstError by remember { mutableStateOf<String?>(null) }

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxSize().testTag("gst_configuration_section"),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backToDashboard)
        }
        Text(
          text = "${strings.gstConfiguration} & Printing",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    item {
      Card(
        modifier = Modifier.fillMaxWidth().testTag("gst_professional_disclaimer_card"),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer),
      ) {
        Text(
          text = com.example.domain.calculation.ValidationService.GST_PROFESSIONAL_DISCLAIMER,
          style = MaterialTheme.typography.bodySmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSecondaryContainer,
          modifier = Modifier.padding(12.dp),
        )
      }
    }

    gstError?.let { err ->
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        ) {
          Text(
            text = err,
            color = MaterialTheme.colorScheme.onErrorContainer,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(12.dp),
          )
        }
      }
    }

    gstMessage?.let { msg ->
      item {
        Card(
          modifier = Modifier.fillMaxWidth().testTag("gst_local_validation_banner"),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
          Text(
            text = msg,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(12.dp),
          )
        }
      }
    }

    item {
      ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("GST Enabled: ${if (gstEnabled) "ON" else "OFF"}", fontWeight = FontWeight.Bold)
              Text(
                "When disabled, GST is hidden from invoices and no GST is added to totals",
                style = MaterialTheme.typography.bodySmall,
              )
            }
            Switch(
              checked = gstEnabled,
              onCheckedChange = { gstEnabled = it },
              modifier = Modifier.testTag("switch_enable_gst"),
            )
          }

          OutlinedTextField(
            value = gstNumber,
            onValueChange = {
              gstNumber = it.uppercase()
              gstError = null
              val clean = it.trim().uppercase()
              if (clean.isNotBlank()) {
                val v = com.example.domain.calculation.ValidationService.validateGstinFormat(clean)
                gstMessage =
                  if (v is com.example.domain.calculation.ValidationResult.Valid) {
                    com.example.domain.calculation.ValidationService.GSTIN_LOCAL_VALIDATION_NOTICE
                  } else null
              } else {
                gstMessage = null
              }
            },
            label = { Text("Shop GST Number (GSTIN — Optional)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_gstin"),
          )

          Text(
            text = com.example.domain.calculation.ValidationService.GSTIN_LOCAL_VALIDATION_NOTICE,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )

          if (gstNumber.isNotBlank() || gstEnabled) {
            OutlinedButton(
              onClick = {
                gstNumber = ""
                gstEnabled = false
                viewModel.removeGstinFromProfile()
                gstMessage = "GSTIN removed and GST disabled."
              },
              modifier = Modifier.fillMaxWidth().testTag("btn_remove_gstin"),
            ) {
              Text("Remove GSTIN & Disable GST")
            }
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = gstStateName,
              onValueChange = { gstStateName = it },
              label = { Text("Business State") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("input_gst_state"),
            )
            OutlinedTextField(
              value = gstRegistrationType,
              onValueChange = { gstRegistrationType = it },
              label = { Text("Registration Type") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("input_gst_registration_type"),
            )
          }

          Text("Default Tax Treatment:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
          FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = gstTaxTreatment != "INTER_STATE_IGST",
              onClick = { gstTaxTreatment = "INTRA_STATE_CGST_SGST" },
              label = { Text("Intra-State (CGST + SGST)") },
              modifier = Modifier.testTag("chip_tax_intrastate"),
            )
            FilterChip(
              selected = gstTaxTreatment == "INTER_STATE_IGST",
              onClick = { gstTaxTreatment = "INTER_STATE_IGST" },
              label = { Text("Inter-State (IGST)") },
              modifier = Modifier.testTag("chip_tax_interstate"),
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = defaultGstStr,
              onValueChange = { defaultGstStr = it },
              label = { Text("Default GST (%)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("input_default_gst"),
            )
            OutlinedTextField(
              value = cgstStr,
              onValueChange = { cgstStr = it },
              label = { Text("CGST (%)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = sgstStr,
              onValueChange = { sgstStr = it },
              label = { Text("SGST (%)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
              value = igstStr,
              onValueChange = { igstStr = it },
              label = { Text("IGST (%)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
          }

          HorizontalDivider()
          Text("Apply GST To:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = applyToGold, onCheckedChange = { applyToGold = it })
            Text(strings.gold)
            Spacer(modifier = Modifier.width(12.dp))
            Checkbox(checked = applyToSilver, onCheckedChange = { applyToSilver = it })
            Text(strings.silver)
            Spacer(modifier = Modifier.width(12.dp))
            Checkbox(checked = applyToMaking, onCheckedChange = { applyToMaking = it })
            Text(strings.makingCharges)
          }
        }
      }
    }

    item {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Default Invoice Paper Size & Performance",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
          )
          FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            InvoicePrintFormat.values().forEach { fmt ->
              FilterChip(
                selected = defaultPaperSize == fmt,
                onClick = { defaultPaperSize = fmt },
                label = { Text(fmt.displayName) },
                modifier = Modifier.testTag("chip_paper_${fmt.name}"),
              )
            }
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("Fast Dashboard Loading & Indexed Search")
            Switch(checked = fastDashboard, onCheckedChange = { fastDashboard = it })
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Text("Compact Transaction List Rows")
            Switch(checked = compactRows, onCheckedChange = { compactRows = it })
          }

          Button(
            onClick = {
              viewModel.saveGstConfiguration(
                gstEnabled = gstEnabled,
                gstNumber = gstNumber,
                defaultGstRateStr = defaultGstStr,
                cgstRateStr = cgstStr,
                sgstRateStr = sgstStr,
                igstRateStr = igstStr,
                applyToGold = applyToGold,
                applyToSilver = applyToSilver,
                applyToMakingCharges = applyToMaking,
                defaultInvoicePaperSize = defaultPaperSize,
                fastDashboardLoading = fastDashboard,
                compactTransactionRows = compactRows,
                gstStateName = gstStateName,
                gstRegistrationType = gstRegistrationType,
                gstTaxTreatment = gstTaxTreatment,
                onError = { err -> gstError = err },
              )
              if (gstNumber.isNotBlank()) {
                val res = com.example.domain.calculation.ValidationService.validateGstinFormat(gstNumber)
                if (res is com.example.domain.calculation.ValidationResult.Valid) {
                  gstMessage = com.example.domain.calculation.ValidationService.GSTIN_LOCAL_VALIDATION_NOTICE
                }
              } else {
                gstMessage = "GST settings saved."
              }
            },
            modifier = Modifier.fillMaxWidth().testTag("btn_save_gst_config"),
          ) {
            Text(strings.save)
          }
        }
      }
    }
  }
}

@Composable
fun SecurityAndPermissionsSection(
  viewModel: JewelleryViewModel,
  onBack: () -> Unit,
) {
  val profile by viewModel.businessProfile.collectAsState()
  val strings = AppStrings.forLanguage(profile.language)
  val security by viewModel.securitySettings.collectAsState()

  var pinEnabled by remember(security) { mutableStateOf(security.pinEnabled) }
  var pinCode by remember(security) { mutableStateOf(security.pinCode) }
  var timeoutStr by remember(security) { mutableStateOf(security.lockTimeoutMinutes.toString()) }
  var accessMode by remember(security) { mutableStateOf(security.accessMode) }
  var staffCancel by remember(security) { mutableStateOf(security.staffCanCancelTransactions) }
  var staffAdjust by remember(security) { mutableStateOf(security.staffCanAdjustInventory) }
  var staffBackup by remember(security) { mutableStateOf(security.staffCanExportBackup) }
  var staffSettings by remember(security) { mutableStateOf(security.staffCanEditSettings) }

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxSize().testTag("security_pin_section"),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backToDashboard)
        }
        Text(
          text = "${strings.securityPin} & Access Mode",
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    item {
      ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text("Enable 4-Digit PIN Lock", fontWeight = FontWeight.Bold)
              Text(
                "Protects transaction cancellation, inventory adjustment, backup & settings",
                style = MaterialTheme.typography.bodySmall,
              )
            }
            Switch(
              checked = pinEnabled,
              onCheckedChange = { pinEnabled = it },
              modifier = Modifier.testTag("switch_enable_pin"),
            )
          }

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = pinCode,
              onValueChange = { if (it.length <= 6) pinCode = it.filter { ch -> ch.isDigit() } },
              label = { Text("4-Digit Security PIN") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("input_security_pin"),
            )
            OutlinedTextField(
              value = timeoutStr,
              onValueChange = { timeoutStr = it },
              label = { Text("Timeout (Min)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
          }
        }
      }
    }

    item {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text("Active User Access Mode", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
              selected = accessMode == UserAccessMode.OWNER,
              onClick = { accessMode = UserAccessMode.OWNER },
              label = { Text(strings.ownerMode) },
              modifier = Modifier.testTag("chip_owner_mode"),
            )
            FilterChip(
              selected = accessMode == UserAccessMode.STAFF,
              onClick = { accessMode = UserAccessMode.STAFF },
              label = { Text(strings.staffMode) },
              modifier = Modifier.testTag("chip_staff_mode"),
            )
          }

          HorizontalDivider()
          Text("Staff Mode Permissions:", fontWeight = FontWeight.Bold)
          Text(
            "Staff can always create transactions, customers, and view invoices.",
            style = MaterialTheme.typography.bodySmall,
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = staffCancel, onCheckedChange = { staffCancel = it })
            Text("Allow Staff to Cancel Transactions")
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = staffAdjust, onCheckedChange = { staffAdjust = it })
            Text("Allow Staff to Adjust Inventory")
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = staffBackup, onCheckedChange = { staffBackup = it })
            Text("Allow Staff to Export/Restore Backup")
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = staffSettings, onCheckedChange = { staffSettings = it })
            Text("Allow Staff to Edit Settings")
          }

          Button(
            onClick = {
              viewModel.saveSecuritySettings(
                pinEnabled = pinEnabled,
                pinCode = pinCode,
                lockTimeoutMinutes = timeoutStr.toIntOrNull() ?: 15,
                accessMode = accessMode,
                staffCanCancelTransactions = staffCancel,
                staffCanAdjustInventory = staffAdjust,
                staffCanExportBackup = staffBackup,
                staffCanEditSettings = staffSettings,
                requireConfirmationForCancel = security.requireConfirmationForCancel,
                requireReasonForCancel = security.requireReasonForCancel,
                requireReasonForAdjustment = security.requireReasonForAdjustment,
              )
            },
            modifier = Modifier.fillMaxWidth().testTag("btn_save_security_settings"),
          ) {
            Text(strings.save)
          }
        }
      }
    }
  }
}

@Composable
fun AuditControlsSection(
  viewModel: JewelleryViewModel,
  onBack: () -> Unit,
) {
  val profile by viewModel.businessProfile.collectAsState()
  val strings = AppStrings.forLanguage(profile.language)
  val security by viewModel.securitySettings.collectAsState()
  val auditLogs by viewModel.auditLogs.collectAsState()

  var reqConfirmCancel by remember(security) { mutableStateOf(security.requireConfirmationForCancel) }
  var reqReasonCancel by remember(security) { mutableStateOf(security.requireReasonForCancel) }
  var reqReasonAdjust by remember(security) { mutableStateOf(security.requireReasonForAdjustment) }

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(10.dp),
    modifier = Modifier.fillMaxSize().testTag("audit_controls_section"),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backToDashboard)
        }
        Text(
          text = strings.auditControls,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    item {
      Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Audit Enforcement Rules", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = reqConfirmCancel, onCheckedChange = { reqConfirmCancel = it })
            Text("Require confirmation before cancelling transactions")
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = reqReasonCancel, onCheckedChange = { reqReasonCancel = it })
            Text("Require mandatory reason for transaction cancellation")
          }
          Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = reqReasonAdjust, onCheckedChange = { reqReasonAdjust = it })
            Text("Require mandatory reason for inventory adjustment")
          }
          Button(
            onClick = {
              viewModel.saveSecuritySettings(
                pinEnabled = security.pinEnabled,
                pinCode = security.pinCode,
                lockTimeoutMinutes = security.lockTimeoutMinutes,
                accessMode = security.accessMode,
                staffCanCancelTransactions = security.staffCanCancelTransactions,
                staffCanAdjustInventory = security.staffCanAdjustInventory,
                staffCanExportBackup = security.staffCanExportBackup,
                staffCanEditSettings = security.staffCanEditSettings,
                requireConfirmationForCancel = reqConfirmCancel,
                requireReasonForCancel = reqReasonCancel,
                requireReasonForAdjustment = reqReasonAdjust,
              )
            },
            modifier = Modifier.fillMaxWidth().testTag("btn_save_audit_rules"),
          ) {
            Text(strings.save)
          }
        }
      }
    }

    item {
      Text(
        text = "Immutable Audit Trail (${auditLogs.size} events)",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
      )
    }

    items(auditLogs, key = { it.actionId }) { log ->
      Card(modifier = Modifier.fillMaxWidth().testTag("audit_log_${log.actionId}")) {
        Column(modifier = Modifier.padding(12.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(log.actionType.displayName, fontWeight = FontWeight.Bold)
            Text("${log.date} ${log.time}", style = MaterialTheme.typography.labelSmall)
          }
          Text(log.description, style = MaterialTheme.typography.bodySmall)
          Text(
            "Entity: ${log.entityId} • By: ${log.performedBy}${if (log.reason.isNotBlank()) " • Reason: ${log.reason}" else ""}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CustomerRemindersSection(
  viewModel: JewelleryViewModel,
  onBack: () -> Unit,
) {
  val profile by viewModel.businessProfile.collectAsState()
  val strings = AppStrings.forLanguage(profile.language)
  val reminders by viewModel.reminders.collectAsState()
  val customers by viewModel.customers.collectAsState()
  val vendors by viewModel.vendors.collectAsState()

  val stage7Categories =
    listOf(
      ReminderType.CUSTOMER_PAYMENT,
      ReminderType.CUSTOMER_FOLLOW_UP,
      ReminderType.VENDOR_PAYMENT,
      ReminderType.PURCHASE,
      ReminderType.STOCK_PURCHASE,
      ReminderType.OTHER,
    )

  var selectedCategory by remember { mutableStateOf(ReminderType.CUSTOMER_PAYMENT) }
  var title by remember { mutableStateOf("Customer Payment Follow-Up") }
  var description by remember { mutableStateOf("Follow up for pending payment") }
  var customerName by remember { mutableStateOf(customers.firstOrNull()?.name ?: "") }
  var customerMobile by remember { mutableStateOf(customers.firstOrNull()?.mobile ?: "") }
  var vendorName by remember { mutableStateOf(vendors.firstOrNull()?.name ?: "") }
  var dueDate by remember { mutableStateOf(JewelleryViewModel.getTodayDateString()) }
  var pendingAmountStr by remember {
    mutableStateOf(customers.firstOrNull()?.pendingAmount?.toPlainString() ?: "5000.00")
  }
  var pendingMetalStr by remember { mutableStateOf("0.000") }
  var selectedFilter by remember { mutableStateOf("ALL") }

  val todayStr = JewelleryViewModel.getTodayDateString()
  val filteredReminders =
    remember(reminders, selectedFilter, todayStr) {
      when (selectedFilter) {
        "TODAY" -> reminders.filter { it.dueDate == todayStr && it.status == ReminderStatus.PENDING }
        "OVERDUE" -> reminders.filter { it.dueDate < todayStr && it.status == ReminderStatus.PENDING }
        "UPCOMING" -> reminders.filter { it.dueDate > todayStr && it.status == ReminderStatus.PENDING }
        "COMPLETED" -> reminders.filter { it.isCompleted || it.status == ReminderStatus.COMPLETED }
        else -> reminders
      }
    }

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxSize().testTag("reminders_section"),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backToDashboard)
        }
        Text(
          text = strings.reminders,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    item {
      ElevatedCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text("Create Reminder (Customer / Vendor / Stock)", fontWeight = FontWeight.Bold)
          Text("Category:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
          FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            stage7Categories.forEach { cat ->
              FilterChip(
                selected = selectedCategory == cat,
                onClick = {
                  selectedCategory = cat
                  title = cat.displayName
                },
                label = { Text(cat.displayName) },
                modifier = Modifier.testTag("reminder_cat_${cat.name}"),
              )
            }
          }

          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Reminder Title *") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_reminder_title"),
          )

          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = customerName,
              onValueChange = { customerName = it },
              label = { Text("Customer Name (Optional)") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("input_reminder_customer"),
            )
            OutlinedTextField(
              value = vendorName,
              onValueChange = { vendorName = it },
              label = { Text("Vendor Name (Optional)") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("input_reminder_vendor"),
            )
          }
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
              value = dueDate,
              onValueChange = { dueDate = it },
              label = { Text("Due Date (YYYY-MM-DD)") },
              singleLine = true,
              modifier = Modifier.weight(1f).testTag("input_reminder_due_date"),
            )
            OutlinedTextField(
              value = pendingAmountStr,
              onValueChange = { pendingAmountStr = it },
              label = { Text("${strings.pending} (₹)") },
              keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
              singleLine = true,
              modifier = Modifier.weight(1f),
            )
          }
          OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description / ${strings.notes}") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth().testTag("input_reminder_description"),
          )
          Button(
            onClick = {
              viewModel.addCustomerReminder(
                customerId = customers.find { it.name.equals(customerName, ignoreCase = true) }?.id ?: "",
                customerName = customerName,
                customerMobile = customerMobile,
                reminderType = selectedCategory,
                dueDate = dueDate,
                pendingAmountStr = pendingAmountStr,
                pendingMetalGramsStr = pendingMetalStr,
                notes = description,
                title = title,
                description = description,
                vendorId = vendors.find { it.name.equals(vendorName, ignoreCase = true) }?.vendorId ?: "",
                vendorName = vendorName,
                status = ReminderStatus.PENDING,
              )
            },
            modifier = Modifier.fillMaxWidth().testTag("btn_add_reminder"),
          ) {
            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Add Reminder")
          }
        }
      }
    }

    item {
      FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(
          "ALL" to "All (${reminders.size})",
          "TODAY" to "Today's Reminders",
          "OVERDUE" to "Overdue Reminders",
          "UPCOMING" to "Upcoming Reminders",
          "COMPLETED" to "Completed Reminders",
        ).forEach { (key, label) ->
          FilterChip(
            selected = selectedFilter == key,
            onClick = { selectedFilter = key },
            label = { Text(label) },
            modifier = Modifier.testTag("reminder_filter_$key"),
          )
        }
      }
    }

    items(filteredReminders, key = { it.reminderId }) { rem ->
      Card(modifier = Modifier.fillMaxWidth().testTag("reminder_card_${rem.reminderId}")) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = rem.effectiveTitle,
                fontWeight = FontWeight.Bold,
              )
              val partyLine =
                buildList {
                  add("Category: ${rem.reminderType.displayName}")
                  if (rem.customerName.isNotBlank()) add("Customer: ${rem.customerName}")
                  if (rem.vendorName.isNotBlank()) add("Vendor: ${rem.vendorName}")
                  add("Status: ${rem.status.displayName}")
                }.joinToString(" • ")
              Text(
                text = partyLine,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = "Due: ${rem.dueDate} • ${strings.pending}: ₹${rem.pendingAmount.toPlainString()}",
                style = MaterialTheme.typography.bodySmall,
                color =
                  if (rem.dueDate < todayStr && rem.status == ReminderStatus.PENDING)
                    MaterialTheme.colorScheme.error
                  else MaterialTheme.colorScheme.primary,
              )
              if (rem.effectiveDescription.isNotBlank()) {
                Text(rem.effectiveDescription, style = MaterialTheme.typography.bodySmall)
              }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Checkbox(
                checked = rem.isCompleted || rem.status == ReminderStatus.COMPLETED,
                onCheckedChange = { viewModel.toggleReminderCompleted(rem) },
              )
              IconButton(onClick = { viewModel.deleteReminder(rem.reminderId) }) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
              }
            }
          }
          Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ReminderStatus.entries.forEach { st ->
              FilterChip(
                selected = rem.status == st,
                onClick = { viewModel.updateReminderStatus(rem, st) },
                label = { Text(st.displayName) },
                modifier = Modifier.testTag("rem_status_${rem.reminderId}_${st.name}"),
              )
            }
          }
        }
      }
    }
  }
}

@Composable
fun ApplicationHealthSection(
  viewModel: JewelleryViewModel,
  onBack: () -> Unit,
) {
  val profile by viewModel.businessProfile.collectAsState()
  val strings = AppStrings.forLanguage(profile.language)
  val health by viewModel.healthStatus.collectAsState()

  LaunchedEffect(Unit) { viewModel.refreshHealthStatus() }

  val lastSyncStr =
    if (health.lastSyncTimestamp > 0L)
      SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US).format(Date(health.lastSyncTimestamp))
    else "Not synced yet"

  val lastBackupStr =
    if (health.lastBackupTimestamp > 0L)
      SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US).format(Date(health.lastBackupTimestamp))
    else "No backup yet"

  LazyColumn(
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    modifier = Modifier.fillMaxSize().testTag("app_health_section"),
  ) {
    item {
      Row(verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) {
          Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = strings.backToDashboard)
        }
        Text(
          text = strings.appHealth,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
        )
      }
    }

    item {
      ElevatedCard(modifier = Modifier.fillMaxWidth().testTag("app_health_card")) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          HealthRow("Google Account Status", if (health.googleAccountConnected) "Connected" else "Offline / Local Mode")
          HealthRow("Google Sheets Status", if (health.googleSheetsConnected) "Connected & Ready" else "Local Room Cache Active")
          HealthRow("Google Drive Status", if (health.googleDriveConnected) "Connected (Invoices & Backups)" else "Local PDF Storage Active")
          HealthRow("Last Sync Time", lastSyncStr)
          HealthRow("Pending Sync Items", "${health.pendingSyncItemsCount} items")
          HealthRow("Pending Invoice Uploads", "${health.pendingInvoiceUploadsCount} PDFs")
          HealthRow("Last Backup Date", "$lastBackupStr (${health.lastBackupFileName.ifBlank { "None" }})")
          HealthRow("Total Local Records", "${health.localDatabaseRecordCount} records")
          HealthRow("App Version", health.appVersion)
          HorizontalDivider()
          Text("Data Integrity Status:", fontWeight = FontWeight.Bold)
          Text(
            text = health.integritySummary,
            style = MaterialTheme.typography.bodySmall,
            color =
              if (health.integrityIssuesCount == 0) MaterialTheme.colorScheme.primary
              else MaterialTheme.colorScheme.error,
          )
          Button(
            onClick = { viewModel.refreshHealthStatus() },
            modifier = Modifier.fillMaxWidth().testTag("btn_refresh_app_health"),
          ) {
            Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Run Health & Integrity Check")
          }
        }
      }
    }
  }
}

@Composable
private fun HealthRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
  }
}
