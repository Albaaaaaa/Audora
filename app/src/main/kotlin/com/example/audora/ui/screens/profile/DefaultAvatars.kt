package com.example.audora.ui.screens.profile

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.audora.R

private const val AvatarPreferencePrefix = "audora-avatar:"

data class DefaultAvatar(
    val key: String,
    val label: String,
    @DrawableRes val drawableRes: Int,
) {
    val preferenceValue: String get() = "$AvatarPreferencePrefix$key"
}

val AudoraDefaultAvatars = listOf(
    DefaultAvatar("cat", "Cat", R.drawable.avatar_animal_cat),
    DefaultAvatar("corgi", "Corgi", R.drawable.avatar_animal_corgi),
    DefaultAvatar("rabbit", "Rabbit", R.drawable.avatar_animal_rabbit),
    DefaultAvatar("red-panda", "Red panda", R.drawable.avatar_animal_red_panda),
    DefaultAvatar("otter", "Otter", R.drawable.avatar_animal_otter),
    DefaultAvatar("panda", "Panda", R.drawable.avatar_animal_panda),
    DefaultAvatar("fox", "Fox", R.drawable.avatar_animal_fox),
    DefaultAvatar("capybara", "Capybara", R.drawable.avatar_animal_capybara),
    DefaultAvatar("frog", "Frog", R.drawable.avatar_animal_frog),
)

fun defaultAvatarFromPreference(value: String?): DefaultAvatar? =
    value?.takeIf { it.startsWith(AvatarPreferencePrefix) }
        ?.removePrefix(AvatarPreferencePrefix)
        ?.let { key -> AudoraDefaultAvatars.firstOrNull { it.key == key } }

/** Turns the saved profile value into something Coil can display. */
fun resolveProfilePhoto(value: String?): Any? {
    val trimmed = value?.trim().orEmpty()
    if (trimmed.isEmpty()) return null
    defaultAvatarFromPreference(trimmed)?.let { return it.drawableRes }
    return if (trimmed.contains("://")) trimmed else "file://$trimmed"
}

@Composable
fun DefaultAvatarPicker(
    selectedValue: String?,
    onSelect: (DefaultAvatar) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(horizontal = 2.dp, vertical = 3.dp),
    ) {
        items(AudoraDefaultAvatars, key = { it.key }) { avatar ->
            val selected = selectedValue == avatar.preferenceValue
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .then(
                        if (selected) Modifier.border(3.dp, Color.White, CircleShape)
                        else Modifier.border(1.dp, Color.White.copy(alpha = 0.16f), CircleShape)
                    )
                    .clickable { onSelect(avatar) },
                contentAlignment = Alignment.Center,
            ) {
                Image(
                    painter = painterResource(avatar.drawableRes),
                    contentDescription = "Use ${avatar.label} avatar",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
