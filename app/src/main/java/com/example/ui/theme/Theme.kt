package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme =
  darkColorScheme(
    primary = GoldPrimaryDark,
    onPrimary = OnGoldPrimaryDark,
    primaryContainer = GoldContainerDark,
    onPrimaryContainer = OnGoldContainerDark,
    secondary = SilverSecondaryDark,
    onSecondary = OnSilverSecondaryDark,
    secondaryContainer = SilverContainerDark,
    onSecondaryContainer = OnSilverContainerDark,
    tertiary = EmeraldTertiaryDark,
    onTertiary = OnEmeraldTertiaryDark,
    tertiaryContainer = EmeraldContainerDark,
    onTertiaryContainer = OnEmeraldContainerDark,
    background = ObsidianBackground,
    onBackground = Color(0xFFF5EFE6),
    surface = ObsidianSurface,
    onSurface = Color(0xFFF5EFE6),
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = Color(0xFFD6C9B8),
  )

private val LightColorScheme =
  lightColorScheme(
    primary = GoldPrimaryLight,
    onPrimary = OnGoldPrimaryLight,
    primaryContainer = GoldContainerLight,
    onPrimaryContainer = OnGoldContainerLight,
    secondary = SilverSecondaryLight,
    onSecondary = OnSilverSecondaryLight,
    secondaryContainer = SilverContainerLight,
    onSecondaryContainer = OnSilverContainerLight,
    tertiary = EmeraldTertiaryLight,
    onTertiary = OnEmeraldTertiaryLight,
    tertiaryContainer = EmeraldContainerLight,
    onTertiaryContainer = OnEmeraldContainerLight,
    background = WarmIvoryBackground,
    onBackground = DeepEspressoText,
    surface = WarmIvorySurface,
    onSurface = DeepEspressoText,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = MutedBronzeText,
  )

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Use our intentional jewellery & bullion palette rather than generic dynamic wallpaper colors
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
