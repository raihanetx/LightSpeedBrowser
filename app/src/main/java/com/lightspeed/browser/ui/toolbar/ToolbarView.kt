package com.lightspeed.browser.ui.toolbar

import android.content.Context
import android.util.AttributeSet
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import com.lightspeed.browser.databinding.ViewToolbarBinding

/**
 * Simple toolbar: [<] [>] [URL bar] [↻]
 */
class ToolbarView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private val binding = ViewToolbarBinding.inflate(LayoutInflater.from(context), this, true)
    private var onUrlEnterListener: ((String) -> Unit)? = null

    private var title = ""; private var url = ""

    init {
        binding.urlBar.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_SEARCH || event?.keyCode == KeyEvent.KEYCODE_ENTER) {
                val text = binding.urlBar.text?.toString()?.trim() ?: return@setOnEditorActionListener true
                if (text.isNotEmpty()) { onUrlEnterListener?.invoke(text); binding.urlBar.clearFocus(); hideKeyboard() }
                return@setOnEditorActionListener true
            }
            false
        }
        binding.urlBar.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) binding.urlBar.setText(url.ifEmpty { title })
            else updateText()
        }
        binding.backButton.setOnClickListener { onBackClickListener?.invoke() }
        binding.forwardButton.setOnClickListener { onForwardClickListener?.invoke() }
        binding.refreshButton.setOnClickListener { onRefreshClickListener?.invoke() }
    }

    fun setUrl(u: String) { url = u; if (!binding.urlBar.isFocused) updateText() }
    fun setTitle(t: String) { title = t; if (!binding.urlBar.isFocused) updateText() }

    private fun updateText() {
        val d = when { title.isNotEmpty() -> title; url.isNotEmpty() -> url.removePrefix("https://").removePrefix("http://").trimEnd('/'); else -> "" }
        binding.urlBar.setText(d)
    }

    fun setOnUrlEnterListener(l: (String) -> Unit) { onUrlEnterListener = l }
    private var onBackClickListener: (() -> Unit)? = null; fun setOnBackClickListener(l: () -> Unit) { onBackClickListener = l }
    private var onForwardClickListener: (() -> Unit)? = null; fun setOnForwardClickListener(l: () -> Unit) { onForwardClickListener = l }
    private var onRefreshClickListener: (() -> Unit)? = null; fun setOnRefreshClickListener(l: () -> Unit) { onRefreshClickListener = l }

    fun setBackEnabled(e: Boolean) { binding.backButton.isEnabled = e; binding.backButton.alpha = if (e) 1f else 0.4f }
    fun setForwardEnabled(e: Boolean) { binding.forwardButton.isEnabled = e; binding.forwardButton.alpha = if (e) 1f else 0.4f }

    private fun hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.urlBar.windowToken, 0)
    }
}
