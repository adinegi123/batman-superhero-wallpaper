package com.adi121.bat_superhero_man_wallpaper.adapters

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.adi121.bat_superhero_man_wallpaper.R
import com.adi121.bat_superhero_man_wallpaper.databinding.RecyclerviewSingleItemBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.adi121.bat_superhero_man_wallpaper.models.SingleImage



class ImageAdapter(val onHomeImageCLicked: OnHomeImageCLicked) : PagingDataAdapter<SingleImage, ImageAdapter.DataHolder>(
    PHOTO_COMPARATOR
) {


    override fun onBindViewHolder(holder: DataHolder, position: Int) {
        val currentItem = getItem(position)
        currentItem?.let {
            holder.bind(it)
        }
        holder.itemView.setOnClickListener {
            currentItem?.let { it1 -> onHomeImageCLicked.onImageCLicked(it1.url) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DataHolder {
        val binding = RecyclerviewSingleItemBinding.inflate(
            LayoutInflater.from(parent.context), parent, false

        )
        return DataHolder(binding)
    }

    inner class DataHolder(private val binding: RecyclerviewSingleItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(singleImage: SingleImage) {
            binding.apply {
                Glide.with(ivSingleImage)
                    .load(singleImage.url)
                    .timeout(60000)
                    .centerCrop()
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .error(R.drawable.placeholder)
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.progressRv.isVisible=false
                            return false
                        }

                        override fun onResourceReady(
                            resource: Drawable?,
                            model: Any?,
                            target: Target<Drawable>?,
                            dataSource: DataSource?,
                            isFirstResource: Boolean
                        ): Boolean {
                            binding.progressRv.isVisible=false
                            return false
                        }
                    })
                    .into(ivSingleImage)

            }
        }

    }

    companion object {
        private val PHOTO_COMPARATOR = object : DiffUtil.ItemCallback<SingleImage>() {
            override fun areItemsTheSame(oldItem: SingleImage, newItem: SingleImage): Boolean {
                return oldItem.url == newItem.url
            }

            override fun areContentsTheSame(oldItem: SingleImage, newItem: SingleImage): Boolean {
                return oldItem == newItem
            }

        }
    }

    interface OnHomeImageCLicked{
        fun onImageCLicked(url:String)
    }


}