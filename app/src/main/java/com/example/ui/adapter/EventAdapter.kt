package com.example.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.data.model.Event
import com.example.data.model.UserRole
import com.example.databinding.ItemEventBinding

class EventAdapter(
    private val currentUserRole: UserRole,
    private val onItemClick: (Event) -> Unit,
    private val onEditClick: ((Event) -> Unit)? = null,
    private val onDeleteClick: ((Event) -> Unit)? = null
) : ListAdapter<Event, EventAdapter.EventViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val binding = ItemEventBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EventViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class EventViewHolder(private val binding: ItemEventBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(event: Event) {
            binding.tvEventCategory.text = event.category.uppercase()
            binding.tvEventTitle.text = event.title
            binding.tvEventDateTime.text = "${event.date} • ${event.time}"
            binding.tvEventLocation.text = event.location
            binding.tvEventSpeaker.text = event.speaker

            val percentage = if (event.quota > 0) {
                ((event.registeredCount.toFloat() / event.quota.toFloat()) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }
            binding.pbEventQuota.progress = percentage
            binding.tvEventQuotaText.text = "${event.registeredCount}/${event.quota} Terdaftar"

            // Show Edit / Delete only for Pengurus
            if (currentUserRole == UserRole.PENGURUS) {
                binding.btnEditEvent.visibility = View.VISIBLE
                binding.btnDeleteEvent.visibility = View.VISIBLE
                binding.btnEditEvent.setOnClickListener { onEditClick?.invoke(event) }
                binding.btnDeleteEvent.setOnClickListener { onDeleteClick?.invoke(event) }
            } else {
                binding.btnEditEvent.visibility = View.GONE
                binding.btnDeleteEvent.visibility = View.GONE
            }

            binding.btnEventDetail.setOnClickListener { onItemClick(event) }
            binding.root.setOnClickListener { onItemClick(event) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Event>() {
        override fun areItemsTheSame(oldItem: Event, newItem: Event) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Event, newItem: Event) = oldItem == newItem
    }
}
