package com.example.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.data.model.Attendance
import com.example.databinding.ItemAttendanceBinding

class AttendanceAdapter : ListAdapter<Attendance, AttendanceAdapter.AttendanceViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttendanceViewHolder {
        val binding = ItemAttendanceBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AttendanceViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AttendanceViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class AttendanceViewHolder(private val binding: ItemAttendanceBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(attendance: Attendance) {
            binding.tvAttendanceMeeting.text = attendance.meetingTitle
            binding.tvAttendanceStatus.text = attendance.status
            binding.tvAttendanceUser.text = attendance.userName
            binding.tvAttendanceTime.text = attendance.timestamp
            binding.tvAttendanceRoleNim.text = "NIM: ${attendance.userNim} • ${attendance.role}"
            binding.tvAttendanceNotes.text = "Catatan: ${attendance.notes.ifBlank { "Hadir" }}"
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Attendance>() {
        override fun areItemsTheSame(oldItem: Attendance, newItem: Attendance) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Attendance, newItem: Attendance) = oldItem == newItem
    }
}
