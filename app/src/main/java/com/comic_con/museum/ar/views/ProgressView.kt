package com.comic_con.museum.ar.views

import android.content.Context
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.widget.TextView
import com.comic_con.museum.ar.R
import com.comic_con.museum.ar.experience.progress.Progress
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.comic_con.museum.ar.experience.progress.ProgressModel


class ProgressView(c: Context, a: AttributeSet): LinearLayout(c, a) {

    private val titleText by lazy {
        this.findViewById<TextView>(R.id.progressTitle)
    }
    private val achievedProgressText by lazy {
        this.findViewById<TextView>(R.id.achievedProgressNum)
    }
    private val maxProgressNum by lazy {
        this.findViewById<TextView>(R.id.maxProgressNum)
    }
    private val progressBarContainer by lazy {
        this.findViewById<LinearLayout>(R.id.progressBarContainer)
    }

    fun setProgress(progressModel: ProgressModel, progress: Progress?) {
        progress ?: return
        // Get the achieved items for this progress item
        val relevantAchievedItems = progressModel.achievedContentItems.filter { achievedContentId ->
            achievedContentId in (progress.contentItems)
        }

        // Set text values
        titleText?.text = progress.progressName

        // If progress complete
        if (relevantAchievedItems.size >= progress.contentItems.size) {
            // Update background
            this.background = ContextCompat.getDrawable(this.context, R.drawable.progress_background_complete)

            // Update progress bar
            progressBarContainer.background = ContextCompat.getDrawable(this.context, R.drawable.progress_bar_complete)

            // Update text color
            titleText.setTextColor(ContextCompat.getColor(this.context, R.color.black))

            // Hide/update progress numbers
            maxProgressNum?.visibility = View.INVISIBLE
            achievedProgressText?.text = this.context.getString(R.string.progress_completed)
            achievedProgressText?.setTextColor(ContextCompat.getColor(this.context, R.color.black))
        }
        // Else progress is not complete
        else {
            achievedProgressText?.text = relevantAchievedItems.size.toString()
            maxProgressNum?.text = progress.contentItems.size.toString()

            // Set progress bar
            progressBarContainer?.weightSum = progress.contentItems.size.toFloat()
            (this.findViewById<View>(R.id.progressBar)?.layoutParams as? LinearLayout.LayoutParams)?.weight =
                    relevantAchievedItems.size.toFloat()

        }
    }
}