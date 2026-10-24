// SimilarRecommendation.kt

package com.example.audora.models

import com.music.innertube.models.YTItem
import com.example.audora.db.entities.LocalItem

data class SimilarRecommendation(
    val title: LocalItem,
    val items: List<YTItem>,
)
