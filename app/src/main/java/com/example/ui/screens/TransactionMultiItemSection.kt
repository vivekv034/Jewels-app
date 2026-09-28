package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.domain.calculation.CalculationEngine
import com.example.domain.calculation.MultipleItemsCalculationResult
import com.example.domain.model.DeductionType
import com.example.domain.model.MetalType
import com.example.domain.model.RateUnit
import com.example.domain.model.TransactionItem
import java.math.BigDecimal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun QuickTunchChipsRow(
  metalType: MetalType,
  currentTunch: String,
  onSelectTunch: (String) -> Unit,
) {
  val presets =
    if (metalType == MetalType.GOLD) {
      listOf("99.50" to "99.5%", "91.60" to "22K (91.6%)", "83.50" to "20K (83.5%)", "75.00" to "18K (75%)", "58.50" to "14K (58.5%)")
    } else {
      listOf("99.00" to "99%", "92.50" to "Sterling (92.5%)", "80.00" to "80%", "70.00" to "70%", "65.00" to "65%")
    }

  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
      text = "Quick Tunch Presets (${metalType.displayName})",
      style = MaterialTheme.typography.labelSmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Spacer(modifier = Modifier.height(4.dp))
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
      presets.forEach { (value, label) ->
        AssistChip(
          onClick = { onSelectTunch(value) },
          label = { Text(label, style = MaterialTheme.typography.labelSmall) },
          modifier = Modifier.testTag("preset_tunch_${value.replace(".", "_")}"),
        )
      }
    }
  }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ScrapDeductionSection(
  enabledInSettings: Boolean,
  deductionType: DeductionType,
  onDeductionTypeChange: (DeductionType) -> Unit,
  deductionValueInput: String,
  onDeductionValueChange: (String) -> Unit,
  onEnableInSettingsToggle: (Boolean) -> Unit,
) {
  Card(
    modifier = Modifier.fillMaxWidth().testTag("scrap_deduction_card"),
    shape = RoundedCornerShape(14.dp),
    colors =
      CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
      ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Optional Scrap Deduction",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Configurable shop rule — no hidden deduction is ever applied automatically",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        Switch(
          checked = enabledInSettings || deductionType != DeductionType.NONE,
          onCheckedChange = { enabled ->
            onEnableInSettingsToggle(enabled)
            if (!enabled) {
              onDeductionTypeChange(DeductionType.NONE)
              onDeductionValueChange("")
            }
          },
          modifier = Modifier.testTag("toggle_scrap_deduction_switch"),
        )
      }

      if (enabledInSettings || deductionType != DeductionType.NONE) {
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          DeductionType.entries.forEach { type ->
            FilterChip(
              selected = deductionType == type,
              onClick = {
                onDeductionTypeChange(type)
                if (type == DeductionType.NONE) {
                  onDeductionValueChange("")
                }
              },
              label = { Text(type.displayName) },
              modifier = Modifier.testTag("deduction_type_${type.name.lowercase()}"),
            )
          }
        }

        if (deductionType != DeductionType.NONE) {
          val label =
            when (deductionType) {
              DeductionType.WEIGHT_DEDUCTION,
              DeductionType.WEIGHT -> "Weight Deduction (grams)"
              DeductionType.PERCENTAGE_DEDUCTION,
              DeductionType.PERCENTAGE -> "Percentage Deduction (%)"
              DeductionType.AMOUNT_DEDUCTION,
              DeductionType.FIXED_AMOUNT,
              DeductionType.CUSTOM -> "Amount Deduction (₹)"
              DeductionType.NONE -> "Deduction"
            }
          OutlinedTextField(
            value = deductionValueInput,
            onValueChange = onDeductionValueChange,
            label = { Text(label) },
            placeholder = { Text("0.00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth().testTag("input_deduction_value"),
          )
        }
      }
    }
  }
}

@Composable
fun PaymentAdjustmentSection(
  metalValueFormatted: String,
  cashPaidInput: String,
  onCashPaidChange: (String) -> Unit,
  cashReceivedInput: String,
  onCashReceivedChange: (String) -> Unit,
  remainingBalanceFormatted: String,
) {
  var expanded by remember {
    mutableStateOf(cashPaidInput.isNotBlank() || cashReceivedInput.isNotBlank())
  }
  Card(
    modifier = Modifier.fillMaxWidth().testTag("payment_adjustment_card"),
    shape = RoundedCornerShape(14.dp),
    colors =
      CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
      ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = "Payment Adjustment (Optional)",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
          )
          Text(
            text = "Record partial cash paid/received alongside metal payment",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
          )
        }
        OutlinedButton(
          onClick = { expanded = !expanded },
          modifier = Modifier.testTag("toggle_payment_adjustment_button"),
        ) {
          Text(if (expanded) "Hide" else "Adjust")
        }
      }

      if (expanded) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedTextField(
            value = cashReceivedInput,
            onValueChange = onCashReceivedChange,
            label = { Text("Cash Received (₹)") },
            placeholder = { Text("0.00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f).testTag("input_cash_received"),
          )
          OutlinedTextField(
            value = cashPaidInput,
            onValueChange = onCashPaidChange,
            label = { Text("Cash Paid (₹)") },
            placeholder = { Text("0.00") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.weight(1f).testTag("input_cash_paid"),
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text(
            text = "Metal Value: $metalValueFormatted",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
          )
          Text(
            text = "Remaining Balance: $remainingBalanceFormatted",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }
  }
}

@Composable
fun MultiItemsBuilderSection(
  metalType: MetalType,
  defaultRateInput: String,
  defaultRateUnit: RateUnit,
  items: List<TransactionItem>,
  multiCalcResult: MultipleItemsCalculationResult?,
  calcEngine: CalculationEngine,
  onAddItem: (TransactionItem) -> Unit,
  onRemoveItem: (Int) -> Unit,
  onClearAllItems: () -> Unit,
) {
  var newItemName by remember {
    mutableStateOf(if (metalType == MetalType.GOLD) "Gold Chain" else "Silver Anklet")
  }
  var newItemGross by remember { mutableStateOf("") }
  var newItemTunch by remember {
    mutableStateOf(if (metalType == MetalType.GOLD) "80.00" else "70.00")
  }
  var newItemRate by remember(defaultRateInput) { mutableStateOf(defaultRateInput) }
  var itemError by remember { mutableStateOf<String?>(null) }

  Card(
    modifier = Modifier.fillMaxWidth().testTag("multi_items_section_card"),
    shape = RoundedCornerShape(14.dp),
    colors =
      CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.25f)
      ),
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
  ) {
    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Layers,
            contentDescription = "Multiple Items",
            tint = MaterialTheme.colorScheme.primary,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Column {
            Text(
              text = "Multiple Items in One Transaction (${items.size})",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "Add multiple ornaments/scrap items to auto-sum weights & values",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          }
        }
        if (items.isNotEmpty()) {
          OutlinedButton(
            onClick = onClearAllItems,
            modifier = Modifier.testTag("clear_multi_items_button"),
          ) {
            Text("Single Mode")
          }
        }
      }

      // Existing items list
      items.forEachIndexed { idx, item ->
        Card(
          modifier = Modifier.fillMaxWidth().testTag("multi_item_row_$idx"),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        ) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "Item ${idx + 1}: ${item.itemName.ifBlank { item.description }}",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text =
                  "Gross: ${calcEngine.formatWeight(item.grossWeight)} g × Tunch: ${calcEngine.formatTunch(item.tunch)}% = Fine: ${calcEngine.formatWeight(item.fineWeight)} g",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
              )
              Text(
                text = "Value: ${calcEngine.formatCurrency(item.metalValue)}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
              )
            }
            IconButton(
              onClick = { onRemoveItem(idx) },
              modifier = Modifier.testTag("remove_multi_item_$idx"),
            ) {
              Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Remove Item",
                tint = MaterialTheme.colorScheme.error,
              )
            }
          }
        }
      }

      // Add new item inputs
      HorizontalDivider()
      Text(
        text = "Add Line Item #${items.size + 1}",
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.SemiBold,
      )
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
      ) {
        OutlinedTextField(
          value = newItemName,
          onValueChange = { newItemName = it },
          label = { Text("Item Name") },
          singleLine = true,
          modifier = Modifier.weight(1.2f).testTag("input_multi_item_name"),
        )
        OutlinedTextField(
          value = newItemGross,
          onValueChange = {
            newItemGross = it
            itemError = null
          },
          label = { Text("Gross Wt (g)") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.weight(1f).testTag("input_multi_item_gross"),
        )
        OutlinedTextField(
          value = newItemTunch,
          onValueChange = {
            newItemTunch = it
            itemError = null
          },
          label = { Text("Tunch (%)") },
          singleLine = true,
          keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
          modifier = Modifier.weight(1f).testTag("input_multi_item_tunch"),
        )
      }

      if (itemError != null) {
        Text(
          text = itemError!!,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.error,
        )
      }

      Button(
        onClick = {
          val gross = calcEngine.parseSafeBigDecimal(newItemGross)
          val tunch = calcEngine.parseSafeBigDecimal(newItemTunch)
          val rate =
            calcEngine.parseSafeBigDecimal(newItemRate)
              ?: calcEngine.parseSafeBigDecimal(defaultRateInput)
              ?: BigDecimal.ZERO
          if (gross == null || gross <= BigDecimal.ZERO) {
            itemError = "Enter valid Gross Weight > 0"
            return@Button
          }
          if (tunch == null || tunch <= BigDecimal.ZERO || tunch > BigDecimal("1000")) {
            itemError = "Enter valid Tunch > 0"
            return@Button
          }
          val fineRes = calcEngine.calculateFineWeight(gross, tunch)
          val valRes =
            if (rate > BigDecimal.ZERO) {
              calcEngine.calculateMetalValue(fineRes.fineWeight, rate, defaultRateUnit)
            } else {
              BigDecimal.ZERO
            }
          onAddItem(
            TransactionItem(
              itemName = newItemName.ifBlank { "Item ${items.size + 1}" },
              description = newItemName.ifBlank { "Item ${items.size + 1}" },
              metalType = metalType,
              grossWeight = fineRes.grossWeight,
              tunch = fineRes.tunch,
              purity = "${fineRes.tunch.toPlainString()}%",
              fineWeight = fineRes.fineWeight,
              rate = rate,
              rateUnit = defaultRateUnit,
              metalValue = valRes,
              amount = valRes,
            )
          )
          newItemGross = ""
          itemError = null
        },
        modifier = Modifier.fillMaxWidth().testTag("add_multi_item_button"),
      ) {
        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Text("Add Item to Transaction")
      }

      if (multiCalcResult != null && items.isNotEmpty()) {
        HorizontalDivider()
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text("Total Gross Weight:", style = MaterialTheme.typography.bodySmall)
          Text(
            "${calcEngine.formatWeight(multiCalcResult.totalGrossWeight)} g",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text("Total Fine Weight:", style = MaterialTheme.typography.bodySmall)
          Text(
            "${calcEngine.formatWeight(multiCalcResult.totalFineWeight)} g",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
          )
        }
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          Text("Total Net Value:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
          Text(
            calcEngine.formatCurrency(multiCalcResult.netValue),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }
  }
}
