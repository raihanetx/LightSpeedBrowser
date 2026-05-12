package com.lightspeed.browser.ui.history

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.lightspeed.browser.BrowserApplication
import com.lightspeed.browser.R
import com.lightspeed.browser.data.db.entities.HistoryEntry
import com.lightspeed.browser.databinding.ActivityHistoryBinding

class HistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHistoryBinding
    private val services get() = (application as BrowserApplication).serviceLocator

    override fun onCreate(savedInstanceState: Bundle?) {
        setTheme(R.style.Theme_LightSpeed); super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater); setContentView(binding.root)
        binding.toolbar.setNavigationOnClickListener { finish() }

        val adapter = object : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
            var items: List<HistoryEntry> = emptyList()
            override fun getItemCount() = items.size
            override fun onCreateViewHolder(p: ViewGroup, vt: Int) = object : RecyclerView.ViewHolder(
                layoutInflater.inflate(android.R.layout.simple_list_item_2, p, false)
            ) { init { itemView.setOnClickListener {
                items.getOrNull(adapterPosition)?.let { e ->
                    intent.putExtra("url", e.url); setResult(RESULT_OK, intent); finish()
                }
            }}}
            override fun onBindViewHolder(h: RecyclerView.ViewHolder, i: Int) {
                val e = items[i]
                (h.itemView as ViewGroup).getChildAt(0).let { (it as TextView).text = e.title.ifEmpty { e.url } }
                (h.itemView as ViewGroup).getChildAt(1).let { (it as TextView).text = e.url }
            }
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        services.historyDao.getAllHistory().observe(this) { list ->
            adapter.items = list; adapter.notifyDataSetChanged()
            binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
        }
    }
}
