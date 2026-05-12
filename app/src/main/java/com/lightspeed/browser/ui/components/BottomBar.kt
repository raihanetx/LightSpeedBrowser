package com.lightspeed.browser.ui.components

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.lightspeed.browser.databinding.ViewBottomBarBinding

/**
 * Simple bottom bar: [←] [→] [zoom-] [zoom+] [☰]
 */
class BottomBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewBottomBarBinding.inflate(LayoutInflater.from(context), this, true)

    val menuButton: android.view.View get() = binding.menuButton

    fun setOnBackClickListener(l: () -> Unit) { binding.backButton.setOnClickListener { l() } }
    fun setOnForwardClickListener(l: () -> Unit) { binding.forwardButton.setOnClickListener { l() } }
    fun setOnMenuClickListener(l: () -> Unit) { binding.menuButton.setOnClickListener { l() } }
    fun setOnZoomInClickListener(l: () -> Unit) { binding.zoomInButton.setOnClickListener { l() } }
    fun setOnZoomOutClickListener(l: () -> Unit) { binding.zoomOutButton.setOnClickListener { l() } }

    fun setBackEnabled(e: Boolean) { binding.backButton.isEnabled = e; binding.backButton.alpha = if (e) 1f else 0.4f }
    fun setForwardEnabled(e: Boolean) { binding.forwardButton.isEnabled = e; binding.forwardButton.alpha = if (e) 1f else 0.4f }
}
