package com.lightspeed.browser.ui.bookmarks

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lightspeed.browser.BrowserApplication
import com.lightspeed.browser.R
import com.lightspeed.browser.data.db.entities.Bookmark
import com.lightspeed.browser.databinding.ActivityBookmarksBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class BookmarksActivity : AppCompatActivity() {
    private lateinit var binding: ActivityBookmarksBinding
    private val services get() = (application as BrowserApplication).serviceLocator

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_LightSpeed); super.onCreate(savedInstanceState)
        binding = ActivityBookmarksBinding.inflate(layoutInflater); setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            var items: List<Bookmark> = emptyList()
            override fun getItemCount() = items.size
            override fun onCreateViewHolder(p: ViewGroup, vt: Int) = object : RecyclerView.ViewHolder(
                layoutInflater.inflate(android.R.layout.simple_list_item_2, p, false)
            ) { init { itemView.setOnClickListener {
                items.getOrNull(bindingAdapterPosition)?.let { b ->
                    intent.putExtra("url", b.url); setResult(RESULT_OK, intent); finish()
                }
            }}}
            override fun onBindViewHolder(h: RecyclerView.ViewHolder, i: Int) {
                val b = items[i]
                (h.itemView as ViewGroup).getChildAt(0).let { (it as TextView).text = b.title.ifEmpty { b.url } }
                (h.itemView as ViewGroup).getChildAt(1).let { (it as TextView).text = b.url }
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        lifecycleScope.launch(Dispatchers.Main) {
            services.bookmarkDao.getAllBookmarks().collectLatest { list ->
                adapter.items = list; adapter.notifyDataSetChanged()
                binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
            }
        }
    }
}
