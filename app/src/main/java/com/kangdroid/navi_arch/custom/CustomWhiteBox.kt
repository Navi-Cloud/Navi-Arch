package com.kangdroid.navi_arch.custom

import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import com.kangdroid.navi_arch.R
import com.kangdroid.navi_arch.databinding.CustomWhiteBoxBinding

class CustomWhiteBox (context: Context, attrs: AttributeSet) : LinearLayout(context, attrs){
    init {
        val v = View.inflate(context, R.layout.custom_white_box, this)
        val binding: CustomWhiteBoxBinding = CustomWhiteBoxBinding.bind(v)

        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomWhiteBox,
            0, 0).apply {
            kotlin.runCatching {
                binding.customTitle.text = getString(R.styleable.CustomWhiteBox_custom_title)
                binding.customImg.setImageResource(getResourceId(R.styleable.CustomWhiteBox_custom_img, R.drawable.icon_file))
                onRefresh()
            }.onFailure {
                Log.d(this::class.java.simpleName, "Failed to create customWhiteBox")
            }
        }
    }

    private fun onRefresh(){
        invalidate()
        requestLayout()
    }
}