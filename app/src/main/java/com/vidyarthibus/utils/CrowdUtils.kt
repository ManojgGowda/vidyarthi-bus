package com.vidyarthibus.utils

import android.content.Context
import android.content.res.ColorStateList
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.vidyarthibus.R
import com.vidyarthibus.data.model.CrowdLevel
import com.vidyarthibus.data.model.CrowdState

object CrowdUtils {

    fun textColorRes(level: CrowdLevel) = when (level) {
        CrowdLevel.EMPTY  -> R.color.crowd_green_text
        CrowdLevel.SEATED -> R.color.crowd_amber_text
        CrowdLevel.FULL   -> R.color.crowd_red_text
    }

    fun bgColorRes(level: CrowdLevel) = when (level) {
        CrowdLevel.EMPTY  -> R.color.crowd_green_bg
        CrowdLevel.SEATED -> R.color.crowd_amber_bg
        CrowdLevel.FULL   -> R.color.crowd_red_bg
    }

    fun progressDrawable(level: CrowdLevel) = when (level) {
        CrowdLevel.EMPTY  -> R.drawable.bg_progress_green
        CrowdLevel.SEATED -> R.drawable.bg_progress_amber
        CrowdLevel.FULL   -> R.drawable.bg_progress_red
    }

    fun pillBg(level: CrowdLevel) = when (level) {
        CrowdLevel.EMPTY  -> R.drawable.bg_pill_green
        CrowdLevel.SEATED -> R.drawable.bg_pill_amber
        CrowdLevel.FULL   -> R.drawable.bg_pill_red
    }

    fun seatsText(state: CrowdState) = when (state.level) {
        CrowdLevel.EMPTY  -> "~${state.seatsAvailable} seats available"
        CrowdLevel.SEATED -> "${state.seatsAvailable} seats left"
        CrowdLevel.FULL   -> "0 seats — standing only"
    }

    fun applyToProgressBar(ctx: Context, bar: ProgressBar, state: CrowdState) {
        bar.progressDrawable = ContextCompat.getDrawable(ctx, progressDrawable(state.level))
        bar.progress = state.percentage
    }

    fun applyToPill(ctx: Context, tv: TextView, state: CrowdState) {
        tv.text = state.level.label
        tv.background = ContextCompat.getDrawable(ctx, pillBg(state.level))
        tv.setTextColor(ContextCompat.getColor(ctx, textColorRes(state.level)))
    }

    fun relativeTime(ts: Long): String {
        val diff = System.currentTimeMillis() - ts
        return when {
            diff < 60_000    -> "${diff / 1000}s ago"
            diff < 3_600_000 -> "${diff / 60_000}m ago"
            else             -> "a while ago"
        }
    }
}
