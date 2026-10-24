// PlaybackSettingsScreen.kt

package com.example.audora.ui.screens.settings

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.audora.R
import com.example.audora.constants.PersistentQueueKey
import com.example.audora.constants.SkipSilenceKey
import com.example.audora.constants.StopPlaybackOnTaskRemovedKey
import com.example.audora.constants.SmartCacheEnabledKey
import com.example.audora.constants.SmartCacheWifiOnlyKey
import com.example.audora.ui.component.AppSwitch
import com.example.audora.ui.component.SettingsGroup
import com.example.audora.ui.component.SettingsGroupStyle
import com.example.audora.ui.component.SettingsItem
import com.example.audora.ui.component.SubSettingsScaffold
import com.example.audora.ui.component.SubSettingsSearchBar
import com.example.audora.utils.rememberPreference

@Composable
fun PlaybackSettingsScreen(navController: NavController) {
    var query by remember { mutableStateOf("") }

    val (stopPlaybackOnTaskRemoved, onStopPlaybackOnTaskRemovedChange) = rememberPreference(
        StopPlaybackOnTaskRemovedKey,
        defaultValue = false
    )
    val (persistentQueue, onPersistentQueueChange) = rememberPreference(
        PersistentQueueKey,
        defaultValue = true
    )
    val (skipSilence, onSkipSilenceChange) = rememberPreference(
        SkipSilenceKey,
        defaultValue = false
    )
    val (smartCache, onSmartCacheChange) = rememberPreference(SmartCacheEnabledKey, defaultValue = true)
    val (smartCacheWifiOnly, onSmartCacheWifiOnlyChange) = rememberPreference(
        SmartCacheWifiOnlyKey,
        defaultValue = true,
    )

    val keepPlaying = !stopPlaybackOnTaskRemoved

    SubSettingsScaffold(
        title = "Playback",
        onBack = { navController.navigateUp() },
        searchBar = { SubSettingsSearchBar(query = query, onQueryChange = { query = it }) },
    ) {
        val items = listOf(
            SettingsItem(
                title = { Text("Keep playing even when closed") },
                highlightKey = "Keep playing even when closed",
                descriptionText = "Keep playing after the app is cleared",
                detail = "Continue playback in background when app is cleared from recents",
                icon = painterResource(R.drawable.clear_all),
                iconShape = CircleShape,
                onClick = { onStopPlaybackOnTaskRemovedChange(!stopPlaybackOnTaskRemoved) },
                trailingContent = {
                    AppSwitch(
                        checked = keepPlaying,
                        onCheckedChange = { onStopPlaybackOnTaskRemovedChange(!it) },
                    )
                },
            ),
            SettingsItem(
                title = { Text("Save Player's last state") },
                highlightKey = "Save Player's last state",
                descriptionText = "Off clears played tracks on close",
                detail = "disabling this will removing previously played tracks when closing the app",
                icon = painterResource(R.drawable.cached),
                iconShape = CircleShape,
                onClick = { onPersistentQueueChange(!persistentQueue) },
                trailingContent = {
                    AppSwitch(
                        checked = persistentQueue,
                        onCheckedChange = onPersistentQueueChange,
                    )
                },
            ),
            SettingsItem(
                title = { Text("Skip Silence") },
                highlightKey = "Skip Silence",
                descriptionText = "Jump over the quiet gaps in songs",
                icon = painterResource(R.drawable.skip_next),
                iconShape = CircleShape,
                onClick = { onSkipSilenceChange(!skipSilence) },
                trailingContent = {
                    AppSwitch(
                        checked = skipSilence,
                        onCheckedChange = onSkipSilenceChange,
                    )
                },
            ),
            SettingsItem(
                title = { Text("Auto Download Smart Cache") },
                highlightKey = "Auto Download Smart Cache",
                descriptionText = "Keep Up Next and favourite playlists available offline",
                detail = "Completed automatic downloads appear in Playlist > Smart Cache",
                icon = painterResource(R.drawable.download),
                iconShape = CircleShape,
                onClick = { onSmartCacheChange(!smartCache) },
                trailingContent = {
                    AppSwitch(checked = smartCache, onCheckedChange = onSmartCacheChange)
                },
            ),
            SettingsItem(
                title = { Text("Smart Cache on Wi-Fi only") },
                highlightKey = "Smart Cache on Wi-Fi only",
                descriptionText = "Avoid automatic downloads on mobile data",
                icon = painterResource(R.drawable.wifi_proxy),
                iconShape = CircleShape,
                enabled = smartCache,
                onClick = { if (smartCache) onSmartCacheWifiOnlyChange(!smartCacheWifiOnly) },
                trailingContent = {
                    AppSwitch(
                        checked = smartCacheWifiOnly,
                        enabled = smartCache,
                        onCheckedChange = onSmartCacheWifiOnlyChange,
                    )
                },
            ),
        ).filter {
            query.isBlank() || it.highlightKey?.contains(query, ignoreCase = true) == true ||
                it.descriptionText?.contains(query, ignoreCase = true) == true
        }

        if (items.isNotEmpty()) {
            SettingsGroup(
                style = SettingsGroupStyle.Grouped,
                items = items,
            )
        }

        Spacer(Modifier.height(140.dp))
    }
}
