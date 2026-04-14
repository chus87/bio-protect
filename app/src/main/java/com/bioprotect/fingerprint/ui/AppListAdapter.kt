package com.bioprotect.fingerprint.ui

import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bioprotect.fingerprint.R
import com.bioprotect.fingerprint.databinding.RowAppBinding
import com.bioprotect.fingerprint.model.AppInfo

class AppListAdapter(
    private val onToggle: (AppInfo, Boolean) -> Unit
) : RecyclerView.Adapter<AppListAdapter.AppViewHolder>() {

    private val items = mutableListOf<AppRowModel>()

    fun submitList(newItems: List<AppRowModel>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val binding = RowAppBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AppViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size

    inner class AppViewHolder(private val binding: RowAppBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AppRowModel) {
            binding.appIconImage.setImageDrawable(item.appInfo.icon)
            binding.appNameText.text = item.appInfo.appName
            binding.packageNameText.text = item.appInfo.packageName
            binding.protectedCheckBox.setOnCheckedChangeListener(null)
            binding.protectedCheckBox.isChecked = item.protected
            binding.protectedBadge.visibility = if (item.protected) View.VISIBLE else View.GONE

            val strokeColor = ContextCompat.getColor(binding.root.context, if (item.protected) R.color.bp_primary else R.color.bp_border)
            binding.cardView.strokeColor = strokeColor
            binding.cardView.strokeWidth = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                if (item.protected) 2f else 1f,
                binding.root.resources.displayMetrics
            ).toInt()

            binding.protectedCheckBox.setOnCheckedChangeListener { _, isChecked ->
                onToggle(item.appInfo, isChecked)
            }
            binding.cardView.setOnClickListener {
                binding.protectedCheckBox.isChecked = !binding.protectedCheckBox.isChecked
            }
        }
    }
}

data class AppRowModel(
    val appInfo: AppInfo,
    val protected: Boolean
)
