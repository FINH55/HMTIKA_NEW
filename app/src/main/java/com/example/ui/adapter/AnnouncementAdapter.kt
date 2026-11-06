package com.example.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.data.model.Announcement
import com.example.data.model.UserRole
import com.example.databinding.ItemAnnouncementBinding

class AnnouncementAdapter(
    private val currentUserRole: UserRole,
    private val onDeleteClick: ((Announcement) -> Unit)? = null
) : ListAdapter<Announcement, AnnouncementAdapter.AnnouncementViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnnouncementViewHolder {
        val binding = ItemAnnouncementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AnnouncementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AnnouncementViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AnnouncementViewHolder(private val binding: ItemAnnouncementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(announcement: Announcement) {
            binding.tvAnnouncePriority.text = announcement.priority
            binding.tvAnnounceTag.text = announcement.tag
            binding.tvAnnounceTitle.text = announcement.title
            binding.tvAnnounceContent.text = announcement.content
            binding.tvAnnounceAuthor.text = announcement.author
            binding.tvAnnounceDate.text = announcement.date

            if (currentUserRole == UserRole.PENGURUS) {
                binding.btnDeleteAnnouncement.visibility = View.VISIBLE
                binding.btnDeleteAnnouncement.setOnClickListener { onDeleteClick?.invoke(announcement) }
            } else {
                binding.btnDeleteAnnouncement.visibility = View.GONE
            }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Announcement>() {
        override fun areItemsTheSame(oldItem: Announcement, newItem: Announcement) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Announcement, newItem: Announcement) = oldItem == newItem
    }
}
