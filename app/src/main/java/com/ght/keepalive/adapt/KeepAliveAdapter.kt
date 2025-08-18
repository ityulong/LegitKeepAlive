package com.ght.keepalive.adapt

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.ght.keepalive.R
import com.ght.keepalive.enumid.SettingId
import com.ght.keepalive.model.SettingItem

class KeepAliveAdapter(
    private val items: List<SettingItem>,
    private val statusChecker: (SettingId) -> Boolean,
    private val onItemClick: (SettingItem) -> Unit
) : RecyclerView.Adapter<KeepAliveAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val container: ConstraintLayout = view.findViewById(R.id.item_container)
        val title: TextView = view.findViewById(R.id.tvTitle)
        val description: TextView = view.findViewById(R.id.tvDescription)
        val checkbox: CheckBox = view.findViewById(R.id.setting_checkbox)
        val arrow: ImageView = view.findViewById(R.id.setting_arrow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_keep_alive_setting, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.title.text = item.title
        holder.description.text = item.description

        if (item.is_checkable) {
            holder.checkbox.visibility = View.VISIBLE
            holder.arrow.visibility = View.GONE

            val isEnabled = statusChecker(item.id)
            holder.checkbox.isChecked = isEnabled

            holder.container.setOnClickListener { onItemClick(item) }
        } else {
            holder.checkbox.visibility = View.GONE
            holder.arrow.visibility = View.VISIBLE
            holder.container.setOnClickListener { onItemClick(item) }
        }
    }

    override fun getItemCount() = items.size
}