package com.havistudio.android.greekconstitution.ui.disclaimer

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.havistudio.android.greekconstitution.data.local.PreferencesManager
import com.havistudio.android.greekconstitution.ui.strings.LocalAppStrings
import com.havistudio.android.greekconstitution.util.openCustomTab
import kotlinx.coroutines.launch

private const val SOURCE_LINK_TEXT = "hellenicparliament.gr"
private const val SOURCE_LINK_URL = "https://www.hellenicparliament.gr/Vouli-ton-Ellinon/To-Politevma/Syntagma/"

@Composable
fun DisclaimerScreen(
    preferencesManager: PreferencesManager,
    onAccepted: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val customTabColor = MaterialTheme.colorScheme.surface.toArgb()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = strings.appTitle,
                    style = MaterialTheme.typography.headlineSmall,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = strings.disclaimerBody,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = SOURCE_LINK_TEXT,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline,
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.clickable {
                        openCustomTab(context, SOURCE_LINK_URL, customTabColor)
                    },
                )
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        scope.launch {
                            preferencesManager.setDisclaimerAccepted()
                            onAccepted()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(strings.accept)
                }
            }
        }
    }
}
