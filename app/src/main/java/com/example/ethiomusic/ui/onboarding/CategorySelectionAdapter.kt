package com.example.ethiomusic.ui.onboarding

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.ethiomusic.databinding.CategorySelectionListItemBinding
import com.example.ethiomusic.util.viewhelper.IRecyclerViewInteractionListener
import com.example.ethiomusic.util.viewhelper.RecyclerviewStateManager

class CategorySelectionAdapter(
    var list: List<String>, var stateManager: RecyclerviewStateManager<String>,
    var interaction: IRecyclerViewInteractionListener<String>, var owner: LifecycleOwner
) : RecyclerView.Adapter<CategorySelectionAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        var binding = CategorySelectionListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return CategoryViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        holder.bind(list[position])
    }

    inner class CategoryViewHolder(var binding: CategorySelectionListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {


        fun bind(data: String) {
            binding.lifecycleOwner = owner
            binding.stateManager = stateManager
            binding.category = data

            with(binding.root) {
                setOnClickListener {
                    interaction.onItemClick(data, adapterPosition,null)
                }
            }
        }
    }

}