package com.havistudio.android.greekconstitution.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import com.havistudio.android.greekconstitution.data.local.FontSize
import com.havistudio.android.greekconstitution.data.local.PreferencesManager
import com.havistudio.android.greekconstitution.data.local.ThemePref
import com.havistudio.android.greekconstitution.data.local.UiLanguage
import com.havistudio.android.greekconstitution.ui.strings.AppStrings
import com.havistudio.android.greekconstitution.ui.strings.LocalAppStrings

private const val SAMPLE_TEXT =
    "Όλοι όσοι βρίσκονται στην Ελληνική Επικράτεια απολαμβάνουν την απόλυτη " +
        "προστασία της ζωής, της τιμής και της ελευθερίας τους, χωρίς διάκριση " +
        "εθνικότητας, φυλής, γλώσσας και θρησκευτικών ή πολιτικών πεποιθήσεων."

/** Hellenic Parliament reprint (1975, rev. 2008). */
private const val SOURCE_URL =
    "https://www.hellenicparliament.gr/userfiles/f3c70a23-7696-49db-9148-f24dce6a27c8/syntagma1_1.pdf"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    preferencesManager: PreferencesManager,
    onBack: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel(
        factory = SettingsViewModel.Factory(preferencesManager),
    ),
) {
    val prefs by viewModel.state.collectAsState()
    val labels = LocalAppStrings.current
    val context = LocalContext.current
    val customTabColor = MaterialTheme.colorScheme.surface.toArgb()

    var showDisclaimerResetDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(labels.settingsTitle) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = labels.back,
                        )
                    }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 24.dp),
        ) {
            item { SectionHeader(labels.appearance) }

            item {
                ThemeRow(
                    labels = labels,
                    selected = prefs.theme,
                    onSelect = viewModel::setTheme,
                )
            }

            item {
                SwitchRow(
                    icon = Icons.Default.Palette,
                    headline = labels.dynamicColor,
                    supporting = labels.dynamicColorSub,
                    checked = prefs.dynamicColor,
                    onCheckedChange = viewModel::setDynamicColor,
                )
            }

            item {
                FontSizeRow(
                    labels = labels,
                    selected = prefs.fontSize,
                    onSelect = viewModel::setFontSize,
                )
            }

            item { ThinDivider() }
            item { SectionHeader(labels.languageSection) }
            item {
                LanguageRow(
                    selected = prefs.language,
                    onSelect = viewModel::setLanguage,
                    note = labels.languageNote,
                )
            }

            item { ThinDivider() }
            item { SectionHeader(labels.about) }
            item {
                NavRow(
                    icon = Icons.AutoMirrored.Filled.MenuBook,
                    headline = labels.source,
                    supporting = labels.sourceSub,
                    onClick = { openCustomTab(context, SOURCE_URL, customTabColor) },
                )
            }
            item {
                NavRow(
                    icon = Icons.Default.PrivacyTip,
                    headline = labels.privacy,
                    supporting = labels.privacySub,
                    onClick = { showPrivacyDialog = true },
                )
            }
            item {
                NavRow(
                    icon = Icons.Default.WarningAmber,
                    headline = labels.showDisclaimer,
                    supporting = null,
                    onClick = { showDisclaimerResetDialog = true },
                )
            }

            item {
                Text(
                    text = labels.version,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 24.dp, top = 18.dp, end = 24.dp),
                )
            }
        }
    }

    if (showDisclaimerResetDialog) {
        AlertDialog(
            onDismissRequest = { showDisclaimerResetDialog = false },
            title = { Text(labels.showDisclaimer) },
            text = { Text(labels.showDisclaimerBody) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetDisclaimer()
                    showDisclaimerResetDialog = false
                }) { Text(labels.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showDisclaimerResetDialog = false }) {
                    Text(labels.cancel)
                }
            },
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            icon = { Icon(Icons.Default.PrivacyTip, contentDescription = null) },
            title = { Text(labels.privacy) },
            text = { Text(labels.privacyDialogBody) },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text(labels.confirm)
                }
            },
        )
    }
}

private fun openCustomTab(
    context: android.content.Context,
    url: String,
    toolbarColorArgb: Int,
) {
    val colorParams = CustomTabColorSchemeParams.Builder()
        .setToolbarColor(toolbarColorArgb)
        .build()
    val intent = CustomTabsIntent.Builder()
        .setDefaultColorSchemeParams(colorParams)
        .setShowTitle(true)
        .build()
    intent.launchUrl(context, url.toUri())
}

@Composable
internal fun SectionHeader(text: String) {
    Text(
        text = text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 18.dp, bottom = 6.dp),
    )
}

@Composable
private fun ThinDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
internal fun ThemeRow(
    labels: AppStrings,
    selected: ThemePref,
    onSelect: (ThemePref) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            Icons.Default.Brightness6,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(labels.theme, style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(8.dp))
            ThemeSegmented(selected = selected, labels = labels, onSelect = onSelect)
        }
    }
}

@Composable
internal fun ThemeSegmented(
    selected: ThemePref,
    labels: AppStrings,
    onSelect: (ThemePref) -> Unit,
) {
    val options = listOf(
        Triple(ThemePref.System, labels.themeSystem, Icons.Default.Settings),
        Triple(ThemePref.Light,  labels.themeLight,  Icons.Default.LightMode),
        Triple(ThemePref.Dark,   labels.themeDark,   Icons.Default.DarkMode),
    )
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(percent = 50),
            ),
    ) {
        options.forEachIndexed { idx, (value, label, icon) ->
            val on = value == selected
            val shape = when (idx) {
                0 -> RoundedCornerShape(topStart = 100.dp, bottomStart = 100.dp)
                options.lastIndex -> RoundedCornerShape(topEnd = 100.dp, bottomEnd = 100.dp)
                else -> RoundedCornerShape(0.dp)
            }
            Row(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 40.dp)
                    .background(
                        color = if (on) MaterialTheme.colorScheme.secondaryContainer
                                else androidx.compose.ui.graphics.Color.Transparent,
                        shape = shape,
                    )
                    .clickable {
                        if (value != selected) {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                        onSelect(value)
                    }
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = if (on) Icons.Default.Check else icon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = if (on) MaterialTheme.colorScheme.onSecondaryContainer
                           else MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (on) MaterialTheme.colorScheme.onSecondaryContainer
                            else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            if (idx < options.lastIndex) {
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .heightIn(min = 40.dp)
                        .background(MaterialTheme.colorScheme.outline),
                )
            }
        }
    }
}

@Composable
internal fun SwitchRow(
    icon: ImageVector,
    headline: String,
    supporting: String?,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(headline, style = MaterialTheme.typography.bodyLarge)
            if (supporting != null) {
                Text(
                    supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
internal fun FontSizeRow(
    labels: AppStrings,
    selected: FontSize,
    onSelect: (FontSize) -> Unit,
) {
    val sizeLabels = listOf(
        labels.fontSmall, labels.fontMedium, labels.fontLarge, labels.fontXLarge,
    )
    val previewSp = listOf(14, 16, 18, 21)[selected.index]
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.TextFields,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.width(16.dp))
            Text(
                text = labels.fontSize,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = sizeLabels[selected.index],
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(8.dp))

        Slider(
            value = selected.index.toFloat(),
            onValueChange = { v ->
                val newIndex = v.toInt()
                if (newIndex != selected.index) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onSelect(FontSize.fromIndex(newIndex))
                }
            },
            valueRange = 0f..3f,
            steps = 2,
            colors = SliderDefaults.colors(),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            listOf(12, 14, 17, 21).forEach { sp ->
                Text(
                    text = "A",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = TextUnit(sp.toFloat(), TextUnitType.Sp),
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    RoundedCornerShape(12.dp),
                )
                .padding(14.dp),
        ) {
            Column {
                Text(
                    text = labels.previewLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = SAMPLE_TEXT,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = TextUnit(previewSp.toFloat(), TextUnitType.Sp),
                    ),
                )
            }
        }
    }
}

@Composable
internal fun LanguageRow(
    selected: UiLanguage,
    onSelect: (UiLanguage) -> Unit,
    note: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 4.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            Icons.Default.Translate,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 2.dp),
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(top = 4.dp),
            ) {
                LanguageChip(
                    label = "Ελληνικά",
                    selected = selected == UiLanguage.Greek,
                    onClick = { onSelect(UiLanguage.Greek) },
                )
                LanguageChip(
                    label = "English",
                    selected = selected == UiLanguage.English,
                    onClick = { onSelect(UiLanguage.English) },
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
internal fun LanguageChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Row(
        modifier = Modifier
            .heightIn(min = 40.dp)
            .background(
                color = if (selected) MaterialTheme.colorScheme.secondaryContainer
                        else androidx.compose.ui.graphics.Color.Transparent,
                shape = RoundedCornerShape(percent = 50),
            )
            .border(
                width = 1.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(percent = 50),
            )
            .clickable {
                if (!selected) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                }
                onClick()
            }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (selected) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
            )
            Spacer(Modifier.width(6.dp))
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun NavRow(
    icon: ImageVector,
    headline: String,
    supporting: String?,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(headline, style = MaterialTheme.typography.bodyLarge)
            if (supporting != null) {
                Text(
                    supporting,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
