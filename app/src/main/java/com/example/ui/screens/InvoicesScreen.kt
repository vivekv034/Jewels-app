package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.calculation.CalculationEngine
import com.example.domain.model.Invoice
import com.example.domain.model.InvoicePrintFormat
import com.example.domain.model.InvoiceStatus
import com.example.domain.model.InvoiceTypeFilter
import com.example.domain.model.LogoPosition
import com.example.domain.model.MetalType
import com.example.domain.model.PendingDriveUpload
import com.example.domain.model.ReportDateFilter
import com.example.domain.model.TransactionType
import com.example.ui.theme.GoldAccentBg
import com.example.ui.theme.GoldAccentBorder
import com.example.ui.theme.GoldAccentText
import com.example.ui.theme.RoyalGoldShimmer
import com.example.ui.theme.RoyalObsidian
import com.example.ui.theme.SilverAccentBg
import com.example.ui.theme.SilverAccentBorder
import com.example.ui.theme.SilverAccentText
import com.example.ui.theme.SuccessGreen
import java.math.BigDecimal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InvoicesScreen(
  invoices: List<Invoice>,
  selectedInvoice: Invoice?,
  calculationEngine: CalculationEngine,
  printFormat: InvoicePrintFormat = InvoicePrintFormat.A4,
  typeFilter: InvoiceTypeFilter = InvoiceTypeFilter.ALL,
  dateFilter: ReportDateFilter = ReportDateFilter.ALL_TIME,
  customStartDate: String = "",
  customEndDate: String = "",
  pendingDriveUploads: List<PendingDriveUpload> = emptyList(),
  onSelectPrintFormat: (InvoicePrintFormat) -> Unit = {},
  onSelectTypeFilter: (InvoiceTypeFilter) -> Unit = {},
  onSelectDateFilter: (ReportDateFilter) -> Unit = {},
  onCustomDateRangeChange: (String, String) -> Unit = { _, _ -> },
  onSelectInvoice: (Invoice?) -> Unit,
  onCreateNewInvoice: () -> Unit,
  onDeleteInvoice: (String) -> Unit,
  onGeneratePdf: (Invoice, Boolean) -> Unit = { _, _ -> },
  onSaveToDrive: (Invoice, Boolean) -> Unit = { _, _ -> },
  onRetryPendingDriveUploads: () -> Unit = {},
  onPrintInvoice: (Invoice) -> Unit = {},
  onSharePdf: (Invoice) -> Unit = {},
  onShareText: (Invoice) -> Unit = {},
  onFormatShareText: (Invoice) -> String,
  onBackToDashboard: () -> Unit,
  modifier: Modifier = Modifier,
) {
  var searchQuery by remember { mutableStateOf("") }
  var invoiceToDelete by remember { mutableStateOf<Invoice?>(null) }
  var startInput by remember(customStartDate) { mutableStateOf(customStartDate) }
  var endInput by remember(customEndDate) { mutableStateOf(customEndDate) }

  if (selectedInvoice != null) {
    BackHandler { onSelectInvoice(null) }
    InvoicePreviewDetailScreen(
      invoice = selectedInvoice,
      calculationEngine = calculationEngine,
      printFormat = printFormat,
      onSelectPrintFormat = onSelectPrintFormat,
      onBack = { onSelectInvoice(null) },
      onDelete = { invoiceToDelete = selectedInvoice },
      onGeneratePdf = { force -> onGeneratePdf(selectedInvoice, force) },
      onSaveToDrive = { force -> onSaveToDrive(selectedInvoice, force) },
      onPrintInvoice = { onPrintInvoice(selectedInvoice) },
      onSharePdf = { onSharePdf(selectedInvoice) },
      onShareText = { onShareText(selectedInvoice) },
      onFormatShareText = onFormatShareText,
      modifier = modifier,
    )
  } else {
    BackHandler { onBackToDashboard() }
    val filtered =
      remember(invoices, searchQuery, typeFilter) {
        val byType =
          when (typeFilter) {
            InvoiceTypeFilter.ALL -> invoices
            InvoiceTypeFilter.GOLD -> invoices.filter { it.metalType == MetalType.GOLD }
            InvoiceTypeFilter.SILVER -> invoices.filter { it.metalType == MetalType.SILVER }
            InvoiceTypeFilter.SCRAP ->
              invoices.filter {
                it.transactionType == TransactionType.SCRAP_GOLD ||
                  it.transactionType == TransactionType.SCRAP_SILVER
              }
            InvoiceTypeFilter.MONEY_TO_GOLD ->
              invoices.filter { it.transactionType == TransactionType.MONEY_TO_GOLD }
            InvoiceTypeFilter.MONEY_TO_SILVER ->
              invoices.filter { it.transactionType == TransactionType.MONEY_TO_SILVER }
          }
        if (searchQuery.isBlank()) byType
        else {
          val q = searchQuery.trim().lowercase()
          byType.filter {
            it.invoiceNumber.lowercase().contains(q) ||
              it.customerName.lowercase().contains(q) ||
              it.customerMobile.lowercase().contains(q) ||
              it.date.lowercase().contains(q) ||
              it.transactionType.title.lowercase().contains(q)
          }
        }
      }

    LazyColumn(
      modifier = modifier.fillMaxSize().testTag("invoices_screen"),
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
              text = "Invoices (${filtered.size})",
              style = MaterialTheme.typography.headlineSmall,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "Preview, generate PDF, save to Google Drive, print & share",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
          Button(
            onClick = onCreateNewInvoice,
            modifier = Modifier.heightIn(min = 48.dp).testTag("create_invoice_from_list_btn"),
            shape = RoundedCornerShape(12.dp),
          ) {
            Icon(Icons.Default.Add, contentDescription = "Create Invoice")
            Spacer(modifier = Modifier.width(6.dp))
            Text("Create Invoice", fontWeight = FontWeight.Bold)
          }
        }
      }

      if (pendingDriveUploads.isNotEmpty()) {
        item {
          Card(
            modifier = Modifier.fillMaxWidth().testTag("pending_drive_uploads_card"),
            shape = RoundedCornerShape(12.dp),
            colors =
              CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer
              ),
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "${pendingDriveUploads.size} Invoice PDF(s) Pending Drive Upload",
                  fontWeight = FontWeight.Bold,
                  style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                  text = "Invoice saved on device. Google Drive upload is pending.",
                  style = MaterialTheme.typography.bodySmall,
                )
              }
              OutlinedButton(
                onClick = onRetryPendingDriveUploads,
                modifier = Modifier.heightIn(min = 48.dp).testTag("retry_drive_uploads_btn"),
              ) {
                Icon(Icons.Default.Refresh, contentDescription = "Retry Drive Upload", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Upload Now")
              }
            }
          }
        }
      }

      item {
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { searchQuery = it },
          label = { Text("Search by Invoice No, Customer, Mobile or Date") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          singleLine = true,
          modifier = Modifier.fillMaxWidth().testTag("invoice_search_input"),
        )
      }

      // Invoice Type Filter Chips
      item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Filter by Type",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            InvoiceTypeFilter.entries.forEach { filter ->
              FilterChip(
                selected = typeFilter == filter,
                onClick = { onSelectTypeFilter(filter) },
                label = { Text(filter.displayName) },
                modifier = Modifier.testTag("invoice_type_filter_${filter.name.lowercase()}"),
              )
            }
          }
        }
      }

      // Invoice Date Filter Chips
      item {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "Filter by Date",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            listOf(
              ReportDateFilter.ALL_TIME,
              ReportDateFilter.TODAY,
              ReportDateFilter.YESTERDAY,
              ReportDateFilter.THIS_WEEK,
              ReportDateFilter.THIS_MONTH,
              ReportDateFilter.CUSTOM_RANGE,
            ).forEach { filter ->
              FilterChip(
                selected = dateFilter == filter,
                onClick = { onSelectDateFilter(filter) },
                label = { Text(filter.displayName) },
                modifier = Modifier.testTag("invoice_date_filter_${filter.name.lowercase()}"),
              )
            }
          }

          if (dateFilter == ReportDateFilter.CUSTOM_RANGE) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              OutlinedTextField(
                value = startInput,
                onValueChange = {
                  startInput = it
                  onCustomDateRangeChange(it, endInput)
                },
                label = { Text("Start (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("invoice_custom_start_input"),
              )
              OutlinedTextField(
                value = endInput,
                onValueChange = {
                  endInput = it
                  onCustomDateRangeChange(startInput, it)
                },
                label = { Text("End (YYYY-MM-DD)") },
                singleLine = true,
                modifier = Modifier.weight(1f).testTag("invoice_custom_end_input"),
              )
            }
          }
        }
      }

      if (filtered.isEmpty()) {
        item {
          Card(modifier = Modifier.fillMaxWidth()) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(24.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Text("No invoices found matching your search or filter.", fontWeight = FontWeight.Bold)
            }
          }
        }
      } else {
        items(filtered, key = { it.invoiceNumber }) { inv ->
          val isGold = inv.metalType == MetalType.GOLD
          Card(
            modifier =
              Modifier.fillMaxWidth()
                .clickable { onSelectInvoice(inv) }
                .testTag("invoice_card_${inv.invoiceNumber}"),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(14.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Row(
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isGold) GoldAccentBg else SilverAccentBg,
                    border = BorderStroke(1.dp, if (isGold) GoldAccentBorder else SilverAccentBorder),
                  ) {
                    Text(
                      text = inv.invoiceNumber,
                      color = if (isGold) GoldAccentText else SilverAccentText,
                      fontWeight = FontWeight.ExtraBold,
                      fontSize = 13.sp,
                      modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    )
                  }
                  InvoiceStatusBadge(invoice = inv)
                }
                Text(
                  text = "${inv.date} • ${inv.time}",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }

              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
              ) {
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = inv.customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                  )
                  if (inv.customerMobile.isNotBlank()) {
                    Text(
                      text = "Mob: ${inv.customerMobile}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                  }
                  Text(
                    text =
                      "${inv.transactionType.title} • Fine: ${calculationEngine.formatWeight(inv.fineWeight)}g (${calculationEngine.formatTunch(inv.tunch)}%)",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Column(horizontalAlignment = Alignment.End) {
                  Text(
                    text = "₹${calculationEngine.formatMoney(inv.totalAmount)}",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color =
                      if (inv.isCancelled) MaterialTheme.colorScheme.error
                      else MaterialTheme.colorScheme.primary,
                  )
                  Text(
                    text = inv.paymentMode.displayName,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }

              HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

              // Quick Action Buttons on Invoice History Card
              FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
              ) {
                OutlinedButton(
                  onClick = { onSelectInvoice(inv) },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier =
                    Modifier.heightIn(min = 38.dp).testTag("preview_invoice_btn_${inv.invoiceNumber}"),
                ) {
                  Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Preview", fontSize = 12.sp)
                }

                OutlinedButton(
                  onClick = { onGeneratePdf(inv, inv.hasPdfGenerated) },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier =
                    Modifier.heightIn(min = 38.dp).testTag("pdf_invoice_btn_${inv.invoiceNumber}"),
                ) {
                  Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(if (inv.hasPdfGenerated) "Regenerate PDF" else "Generate PDF", fontSize = 12.sp)
                }

                OutlinedButton(
                  onClick = { onSaveToDrive(inv, false) },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier =
                    Modifier.heightIn(min = 38.dp).testTag("drive_invoice_btn_${inv.invoiceNumber}"),
                ) {
                  Icon(
                    if (inv.isSavedToDrive) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                  )
                  Spacer(modifier = Modifier.width(4.dp))
                  Text(if (inv.isSavedToDrive) "In Drive" else "Save to Drive", fontSize = 12.sp)
                }

                OutlinedButton(
                  onClick = { onSharePdf(inv) },
                  contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                  modifier =
                    Modifier.heightIn(min = 38.dp).testTag("share_invoice_card_btn_${inv.invoiceNumber}"),
                ) {
                  Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Share", fontSize = 12.sp)
                }
              }
            }
          }
        }
      }
    }
  }

  invoiceToDelete?.let { inv ->
    AlertDialog(
      onDismissRequest = { invoiceToDelete = null },
      title = { Text("Delete Invoice ${inv.invoiceNumber}?") },
      text = {
        Text(
          "Are you sure you want to delete invoice ${inv.invoiceNumber} for ${inv.customerName}?"
        )
      },
      confirmButton = {
        Button(
          onClick = {
            onDeleteInvoice(inv.invoiceNumber)
            invoiceToDelete = null
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { invoiceToDelete = null }) { Text("Cancel") }
      },
    )
  }
}

@Composable
private fun InvoiceStatusBadge(invoice: Invoice) {
  val (bgColor, textColor, label) =
    when {
      invoice.isCancelled ->
        Triple(
          MaterialTheme.colorScheme.errorContainer,
          MaterialTheme.colorScheme.onErrorContainer,
          "CANCELLED",
        )
      invoice.isSavedToDrive ->
        Triple(
          SuccessGreen.copy(alpha = 0.16f),
          SuccessGreen,
          "Saved to Drive",
        )
      invoice.invoiceStatus == InvoiceStatus.PENDING_UPLOAD ->
        Triple(
          MaterialTheme.colorScheme.secondaryContainer,
          MaterialTheme.colorScheme.onSecondaryContainer,
          "Drive Pending",
        )
      invoice.hasPdfGenerated ->
        Triple(
          MaterialTheme.colorScheme.primaryContainer,
          MaterialTheme.colorScheme.onPrimaryContainer,
          "PDF Ready",
        )
      else ->
        Triple(
          MaterialTheme.colorScheme.surfaceVariant,
          MaterialTheme.colorScheme.onSurfaceVariant,
          invoice.invoiceStatus.displayName,
        )
    }

  Surface(
    shape = RoundedCornerShape(6.dp),
    color = bgColor,
  ) {
    Text(
      text = label,
      color = textColor,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
    )
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun InvoicePreviewDetailScreen(
  invoice: Invoice,
  calculationEngine: CalculationEngine,
  printFormat: InvoicePrintFormat,
  onSelectPrintFormat: (InvoicePrintFormat) -> Unit,
  onBack: () -> Unit,
  onDelete: () -> Unit,
  onGeneratePdf: (Boolean) -> Unit,
  onSaveToDrive: (Boolean) -> Unit,
  onPrintInvoice: () -> Unit,
  onSharePdf: () -> Unit,
  onShareText: () -> Unit,
  onFormatShareText: (Invoice) -> String,
  modifier: Modifier = Modifier,
) {
  val isThermal = printFormat == InvoicePrintFormat.THERMAL_80MM

  LazyColumn(
    modifier = modifier.fillMaxSize().testTag("invoice_preview_screen"),
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp),
  ) {
    // 1. Top Navigation & Quick Actions
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          IconButton(
            onClick = onBack,
            modifier = Modifier.testTag("back_from_invoice_preview_btn"),
          ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back to invoices")
          }
          Column {
            Text(
              text = "Invoice ${invoice.invoiceNumber}",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "${invoice.transactionType.title} • ${invoice.invoiceStatus.displayName}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        IconButton(onClick = onDelete, modifier = Modifier.testTag("delete_invoice_button")) {
          Icon(
            Icons.Default.DeleteOutline,
            contentDescription = "Delete Invoice",
            tint = MaterialTheme.colorScheme.error,
          )
        }
      }
    }

    // 2. Print Format Layout Selector (A4 / A5 / Thermal 80mm) & PDF / Drive / Print / Share Toolbar
    item {
      Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors =
          CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
          ),
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          Text(
            text = "Print & PDF Layout Format",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
          )
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
          ) {
            InvoicePrintFormat.entries.forEach { fmt ->
              FilterChip(
                selected = printFormat == fmt,
                onClick = { onSelectPrintFormat(fmt) },
                label = { Text(fmt.displayName) },
                modifier = Modifier.testTag("print_format_${fmt.name.lowercase()}"),
              )
            }
          }

          HorizontalDivider()

          FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            Button(
              onClick = { onGeneratePdf(false) },
              modifier = Modifier.heightIn(min = 48.dp).testTag("generate_pdf_button"),
              shape = RoundedCornerShape(10.dp),
            ) {
              Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Generate PDF")
            }

            Button(
              onClick = { onSaveToDrive(false) },
              modifier = Modifier.heightIn(min = 48.dp).testTag("save_to_drive_button"),
              shape = RoundedCornerShape(10.dp),
              colors =
                ButtonDefaults.buttonColors(
                  containerColor =
                    if (invoice.isSavedToDrive) SuccessGreen
                    else MaterialTheme.colorScheme.secondary
                ),
            ) {
              Icon(
                if (invoice.isSavedToDrive) Icons.Default.CloudDone else Icons.Default.CloudUpload,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(if (invoice.isSavedToDrive) "Saved in Drive" else "Save to Google Drive")
            }

            OutlinedButton(
              onClick = onPrintInvoice,
              modifier = Modifier.heightIn(min = 48.dp).testTag("print_invoice_button"),
              shape = RoundedCornerShape(10.dp),
            ) {
              Icon(Icons.Default.Print, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Print (${printFormat.displayName})")
            }

            OutlinedButton(
              onClick = onSharePdf,
              modifier = Modifier.heightIn(min = 48.dp).testTag("share_pdf_button"),
              shape = RoundedCornerShape(10.dp),
            ) {
              Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Share PDF")
            }

            OutlinedButton(
              onClick = onShareText,
              modifier = Modifier.heightIn(min = 48.dp).testTag("share_invoice_button"),
              shape = RoundedCornerShape(10.dp),
            ) {
              Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Share Text")
            }

            if (invoice.hasPdfGenerated) {
              TextButton(
                onClick = { onGeneratePdf(true) },
                modifier = Modifier.heightIn(min = 48.dp).testTag("regenerate_pdf_button"),
              ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Regenerate PDF")
              }
            }
          }

          // PDF & Google Drive Status Metadata Row
          if (invoice.hasPdfGenerated || invoice.isSavedToDrive || invoice.invoiceStatus == InvoiceStatus.PENDING_UPLOAD) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surface,
              border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
              modifier = Modifier.fillMaxWidth().testTag("invoice_cloud_status_box"),
            ) {
              Row(
                modifier = Modifier.fillMaxWidth().padding(10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
              ) {
                Icon(
                  imageVector =
                    if (invoice.isSavedToDrive) Icons.Default.CheckCircle
                    else Icons.Default.PictureAsPdf,
                  contentDescription = null,
                  tint =
                    if (invoice.isSavedToDrive) SuccessGreen
                    else MaterialTheme.colorScheme.primary,
                  modifier = Modifier.size(20.dp),
                )
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = "File: ${invoice.pdfFileName.ifBlank { "Invoice_${invoice.invoiceNumber}.pdf" }}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                  )
                  val subStatus =
                    when {
                      invoice.isSavedToDrive ->
                        "Google Drive: Jewellery Business Manager / Invoices (${invoice.driveFileId})"
                      invoice.invoiceStatus == InvoiceStatus.PENDING_UPLOAD ->
                        "Invoice saved on device. Google Drive upload is pending."
                      else -> "Saved on device (${invoice.localPdfPath.substringAfterLast("/")})"
                    }
                  Text(
                    text = subStatus,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }
            }
          }
        }
      }
    }

    // 3. Printable Jewellery Invoice Card (Adapts to A4 / A5 / Thermal 80mm)
    item {
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
      ) {
        val cardMaxWidth =
          when (printFormat) {
            InvoicePrintFormat.THERMAL_80MM -> 340.dp
            InvoicePrintFormat.A4 -> 680.dp
          }

        Card(
          modifier = Modifier.fillMaxWidth().widthIn(max = cardMaxWidth).testTag("printable_invoice_card"),
          shape = RoundedCornerShape(if (isThermal) 8.dp else 18.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          border = BorderStroke(1.5.dp, RoyalGoldShimmer),
          elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        ) {
          Column(modifier = Modifier.fillMaxWidth()) {
            // Cancelled Banner if cancelled
            if (invoice.isCancelled) {
              Surface(
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.fillMaxWidth().testTag("cancelled_invoice_banner"),
              ) {
                Text(
                  text = "*** CANCELLED INVOICE ***",
                  color = MaterialTheme.colorScheme.onError,
                  fontWeight = FontWeight.ExtraBold,
                  textAlign = TextAlign.Center,
                  modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                )
              }
            }

            // Top Royal Header with Shop Details & Logo Position
            Surface(
              color = RoyalObsidian,
              modifier = Modifier.fillMaxWidth(),
            ) {
              val headerAlign =
                when (invoice.logoPosition) {
                  LogoPosition.CENTER -> Alignment.CenterHorizontally
                  LogoPosition.RIGHT -> Alignment.End
                  LogoPosition.LEFT, LogoPosition.HIDDEN -> Alignment.Start
                }
              val textAlign =
                when (invoice.logoPosition) {
                  LogoPosition.CENTER -> TextAlign.Center
                  LogoPosition.RIGHT -> TextAlign.End
                  LogoPosition.LEFT, LogoPosition.HIDDEN -> TextAlign.Start
                }

              Column(
                modifier = Modifier.fillMaxWidth().padding(if (isThermal) 14.dp else 18.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = headerAlign,
              ) {
                if (invoice.businessLogoUri.isNotBlank()) {
                  Surface(
                    shape = CircleShape,
                    color = RoyalGoldShimmer.copy(alpha = 0.2f),
                    border = BorderStroke(1.dp, RoyalGoldShimmer),
                    modifier = Modifier.size(44.dp),
                  ) {
                    Box(contentAlignment = Alignment.Center) {
                      Text(
                        text = invoice.shopName.take(2).uppercase(),
                        color = RoyalGoldShimmer,
                        fontWeight = FontWeight.ExtraBold,
                      )
                    }
                  }
                }

                Text(
                  text = invoice.shopName,
                  color = RoyalGoldShimmer,
                  style =
                    if (isThermal) MaterialTheme.typography.titleMedium
                    else MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.ExtraBold,
                  textAlign = textAlign,
                )
                if (invoice.ownerName.isNotBlank()) {
                  Text(
                    text = "Prop: ${invoice.ownerName}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    textAlign = textAlign,
                  )
                }
                if (invoice.shopAddress.isNotBlank()) {
                  Text(
                    text = invoice.shopAddress,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 12.sp,
                    textAlign = textAlign,
                  )
                }
                val shopIds = buildList {
                  if (invoice.shopMobile.isNotBlank()) add("Mob: ${invoice.shopMobile}")
                  if (invoice.showPan && invoice.shopPan.isNotBlank()) add("PAN: ${invoice.shopPan}")
                  if (invoice.showGst && invoice.shopGst.isNotBlank()) add("GSTIN: ${invoice.shopGst}")
                }.joinToString(" • ")
                if (shopIds.isNotBlank()) {
                  Text(
                    text = shopIds,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 12.sp,
                    textAlign = textAlign,
                  )
                }

                HorizontalDivider(
                  modifier = Modifier.padding(vertical = 8.dp),
                  color = RoyalGoldShimmer.copy(alpha = 0.4f),
                )

                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                  Column {
                    Text(
                      "INVOICE NUMBER",
                      color = RoyalGoldShimmer,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                    )
                    Text(
                      text = invoice.invoiceNumber,
                      color = Color.White,
                      fontSize = if (isThermal) 15.sp else 18.sp,
                      fontWeight = FontWeight.ExtraBold,
                    )
                  }
                  Column(horizontalAlignment = Alignment.End) {
                    Text(
                      "DATE & TIME",
                      color = RoyalGoldShimmer,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                    )
                    Text(
                      text = "${invoice.date} • ${invoice.time}",
                      color = Color.White,
                      fontSize = if (isThermal) 13.sp else 15.sp,
                      fontWeight = FontWeight.Bold,
                    )
                  }
                }
              }
            }

            // Body: Customer Details, Itemized Table, Tax/Financial Summary, Balance, Bank, Signatures
            Column(
              modifier = Modifier.fillMaxWidth().padding(if (isThermal) 14.dp else 18.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
              // Customer Section
              Text(
                text = "CUSTOMER DETAILS",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
              )
              Surface(
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Column(
                  modifier = Modifier.padding(12.dp),
                  verticalArrangement = Arrangement.spacedBy(3.dp),
                ) {
                  Text(
                    text = invoice.customerName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                  )
                  if (invoice.customerMobile.isNotBlank()) {
                    Text("Mobile: ${invoice.customerMobile}")
                  }
                  if (invoice.customerAddress.isNotBlank()) {
                    Text("Address: ${invoice.customerAddress}")
                  }
                  Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    if (invoice.showCustomerPan && invoice.customerPan.isNotBlank()) {
                      Text("PAN: ${invoice.customerPan}", fontSize = 13.sp)
                    }
                    if (invoice.showCustomerGst && invoice.customerGst.isNotBlank()) {
                      Text("GSTIN: ${invoice.customerGst}", fontSize = 13.sp)
                    }
                  }
                }
              }

              // Itemized Transaction Breakdown
              Text(
                text = "TRANSACTION & METAL DETAILS (${invoice.transactionType.title.uppercase()})",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary,
              )

              if (
                invoice.transactionType == TransactionType.MONEY_TO_GOLD ||
                  invoice.transactionType == TransactionType.MONEY_TO_SILVER
              ) {
                InvoiceDetailRow("Transaction Type", invoice.transactionType.title)
                InvoiceDetailRow(
                  "Metal",
                  "${invoice.metalType.displayName} (${invoice.metalType.symbol})",
                )
                InvoiceDetailRow("Amount Paid", "₹${calculationEngine.formatMoney(invoice.amount)}")
                InvoiceDetailRow(
                  "Rate Applied",
                  "₹${calculationEngine.formatMoney(invoice.rate)} (${invoice.rateUnit.displayName})",
                )
                InvoiceDetailRow(
                  "Tunch / Purity",
                  "${calculationEngine.formatTunch(invoice.tunch)}% (${invoice.purityMode.displayName})",
                )
                InvoiceDetailRow(
                  "Allocated Fine Weight",
                  "${calculationEngine.formatWeight(invoice.fineWeight)} grams",
                )
              } else {
                // Itemized Table for Single or Multiple Items
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                  modifier = Modifier.fillMaxWidth().testTag("invoice_items_table"),
                ) {
                  Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                      modifier =
                        Modifier.fillMaxWidth()
                          .background(MaterialTheme.colorScheme.surfaceVariant)
                          .padding(horizontal = 10.dp, vertical = 8.dp),
                      horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                      Text(
                        "Item / Purity",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(0.40f),
                      )
                      Text(
                        "Gross / Fine",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        modifier = Modifier.weight(0.32f),
                      )
                      Text(
                        "Amount",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(0.28f),
                      )
                    }

                    invoice.items.forEachIndexed { index, item ->
                      if (index > 0) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                      }
                      Row(
                        modifier =
                          Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                      ) {
                        Column(modifier = Modifier.weight(0.40f)) {
                          Text(
                            text = "${index + 1}. ${item.description.ifBlank { item.itemName }}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                          )
                          Text(
                            text =
                              "${item.metalType.displayName} • Tunch ${calculationEngine.formatTunch(item.tunch)}%",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                          )
                        }
                        Column(modifier = Modifier.weight(0.32f)) {
                          Text(
                            text = "G: ${calculationEngine.formatWeight(item.grossWeight)}g",
                            fontSize = 12.sp,
                          )
                          Text(
                            text = "F: ${calculationEngine.formatWeight(item.fineWeight)}g",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.primary,
                          )
                        }
                        Column(
                          modifier = Modifier.weight(0.28f),
                          horizontalAlignment = Alignment.End,
                        ) {
                          Text(
                            text = "₹${calculationEngine.formatMoney(item.amount)}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                          )
                          if (item.rate > BigDecimal.ZERO) {
                            Text(
                              text = "@₹${calculationEngine.formatMoney(item.rate)}",
                              fontSize = 10.sp,
                              color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                          }
                        }
                      }
                    }
                  }
                }

                InvoiceDetailRow(
                  "Total Gross Weight",
                  "${calculationEngine.formatWeight(invoice.grossWeight)} grams",
                )
                InvoiceDetailRow(
                  "Average Tunch / Purity",
                  "${calculationEngine.formatTunch(invoice.tunch)}%",
                )
                InvoiceDetailRow(
                  "Total Fine Weight",
                  "${calculationEngine.formatWeight(invoice.fineWeight)} grams",
                )
              }

              // Subtotal, Deductions & Optional GST Breakdown
              if (invoice.deductions > BigDecimal.ZERO) {
                HorizontalDivider()
                InvoiceDetailRow(
                  "Gross Metal Value (Subtotal)",
                  "₹${calculationEngine.formatMoney(invoice.subtotal)}",
                )
                InvoiceDetailRow(
                  "Less: Deductions",
                  "-₹${calculationEngine.formatMoney(invoice.deductions)}",
                )
                InvoiceDetailRow(
                  "Net Taxable Value",
                  "₹${calculationEngine.formatMoney(invoice.netAmount)}",
                )
              }

              if (invoice.gstEnabled && invoice.taxAmount > BigDecimal.ZERO) {
                HorizontalDivider()
                Text(
                  text = "GST TAX BREAKDOWN",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary,
                )
                if (invoice.cgstAmount > BigDecimal.ZERO) {
                  InvoiceDetailRow(
                    "CGST (${calculationEngine.formatTunch(invoice.cgstPercent)}%)",
                    "₹${calculationEngine.formatMoney(invoice.cgstAmount)}",
                  )
                }
                if (invoice.sgstAmount > BigDecimal.ZERO) {
                  InvoiceDetailRow(
                    "SGST (${calculationEngine.formatTunch(invoice.sgstPercent)}%)",
                    "₹${calculationEngine.formatMoney(invoice.sgstAmount)}",
                  )
                }
                if (invoice.igstAmount > BigDecimal.ZERO) {
                  InvoiceDetailRow(
                    "IGST (${calculationEngine.formatTunch(invoice.igstPercent)}%)",
                    "₹${calculationEngine.formatMoney(invoice.igstAmount)}",
                  )
                }
                InvoiceDetailRow(
                  "Total GST Tax",
                  "₹${calculationEngine.formatMoney(invoice.taxAmount)}",
                )
              }

              InvoiceDetailRow("Payment Mode", invoice.paymentMode.displayName)
              InvoiceDetailRow(
                "Amount Received",
                "₹${calculationEngine.formatMoney(invoice.amountReceived)}",
              )
              if (invoice.amountPending > BigDecimal.ZERO) {
                InvoiceDetailRow(
                  "Pending Amount",
                  "₹${calculationEngine.formatMoney(invoice.amountPending)}",
                )
              }
              if (invoice.notes.isNotBlank()) {
                InvoiceDetailRow("Notes", invoice.notes)
              }

              HorizontalDivider()

              // Grand Total Box
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = GoldAccentBg,
                border = BorderStroke(1.5.dp, GoldAccentBorder),
                modifier = Modifier.fillMaxWidth(),
              ) {
                Row(
                  modifier = Modifier.fillMaxWidth().padding(16.dp),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Column {
                    Text(
                      text = "TOTAL INVOICE AMOUNT",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.Bold,
                      color = GoldAccentText,
                    )
                    Text(
                      text = "Payment via ${invoice.paymentMode.displayName}",
                      fontSize = 13.sp,
                      color = GoldAccentText.copy(alpha = 0.85f),
                    )
                  }
                  Text(
                    text = "₹${calculationEngine.formatMoney(invoice.totalAmount)}",
                    fontSize = if (isThermal) 20.sp else 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = GoldAccentText,
                  )
                }
              }

              // Optional Customer Ledger Balance Summary (Separate Money, Gold, Silver)
              if (invoice.showCustomerBalance) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                  modifier = Modifier.fillMaxWidth().testTag("invoice_customer_balance_section"),
                ) {
                  Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                  ) {
                    Text(
                      text = "CUSTOMER LEDGER BALANCE (SEPARATE MONEY & METAL)",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.ExtraBold,
                      color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                      text =
                        "Money Balance: ₹${calculationEngine.formatMoney(invoice.afterMoneyBalance)} (Prev: ₹${calculationEngine.formatMoney(invoice.previousMoneyBalance)})",
                      fontSize = 12.sp,
                    )
                    Text(
                      text =
                        "Gold Balance: ${calculationEngine.formatWeight(invoice.afterGoldBalanceGrams)} g  •  Silver Balance: ${calculationEngine.formatWeight(invoice.afterSilverBalanceGrams)} g",
                      fontSize = 12.sp,
                      fontWeight = FontWeight.SemiBold,
                    )
                  }
                }
              }

              // Shop Bank & UPI Footer
              if (
                (invoice.showBankDetails && invoice.bankName.isNotBlank()) ||
                  (invoice.showUpi && invoice.upiId.isNotBlank())
              ) {
                HorizontalDivider()
                Text(
                  text = "SHOP BANK & UPI SETTLEMENT DETAILS",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (invoice.showBankDetails && invoice.bankName.isNotBlank()) {
                  Text(
                    text =
                      "Bank: ${invoice.bankName} • A/C: ${invoice.bankAccountNumber} • IFSC: ${invoice.ifsc}",
                    fontSize = 13.sp,
                  )
                }
                if (invoice.showUpi && invoice.upiId.isNotBlank()) {
                  Text(
                    text = "UPI ID: ${invoice.upiId}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                  )
                }
              }

              // Terms & Conditions and Signatures
              if (invoice.termsAndConditions.isNotBlank()) {
                HorizontalDivider()
                Text(
                  text = "Terms & Conditions: ${invoice.termsAndConditions}",
                  fontSize = 11.sp,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
              }

              Spacer(modifier = Modifier.height(12.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
              ) {
                Column(horizontalAlignment = Alignment.Start) {
                  HorizontalDivider(modifier = Modifier.width(120.dp))
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Customer Signature",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
                Column(horizontalAlignment = Alignment.End) {
                  HorizontalDivider(modifier = Modifier.width(130.dp))
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = "Authorized Signatory",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                  )
                }
              }

              if (invoice.invoiceFooter.isNotBlank()) {
                Text(
                  text = invoice.invoiceFooter,
                  fontSize = 11.sp,
                  fontFamily = FontFamily.Serif,
                  fontWeight = FontWeight.Medium,
                  textAlign = TextAlign.Center,
                  color = MaterialTheme.colorScheme.onSurfaceVariant,
                  modifier = Modifier.fillMaxWidth().padding(top = 6.dp),
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun InvoiceDetailRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.Top,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      modifier = Modifier.weight(0.42f),
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyLarge,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.weight(0.58f),
    )
  }
}
