package com.example.simplewebview

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ActivityFeedAdapter(private val items: List<ActivityItem>) : RecyclerView.Adapter<ActivityFeedAdapter.ViewHolder>() {

    data class ActivityItem(
        val username: String,
        val time: String,
        val actionText: String,
        val showButton: Boolean = false,
        val showPreviewImage: Boolean = false
    )

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvUsername: TextView = view.findViewById(R.id.tv_username)
        val tvTime: TextView = view.findViewById(R.id.tv_time)
        val tvAction: TextView = view.findViewById(R.id.tv_action)
        val btnAction: TextView = view.findViewById(R.id.btn_action)
        val cvPreview: View = view.findViewById(R.id.cv_preview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_activity_feed, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvUsername.text = item.username
        holder.tvTime.text = item.time
        holder.tvAction.text = item.actionText

        holder.btnAction.visibility = if (item.showButton) View.VISIBLE else View.GONE
        holder.cvPreview.visibility = if (item.showPreviewImage) View.VISIBLE else View.GONE
    }

    override fun getItemCount() = items.size
}
