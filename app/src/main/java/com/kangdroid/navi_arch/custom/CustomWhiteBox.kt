package com.kangdroid.navi_arch.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.kangdroid.navi_arch.R

open class CustomWhiteBox (context: Context, attrs: AttributeSet) : LinearLayout(context, attrs){

    private var customTitle: TextView
    private var customImg: ImageView

    init {
        val v = View.inflate(context, R.layout.custom_white_box, this)
        customTitle = v.findViewById(R.id.custom_title)
        customImg = v.findViewById(R.id.custom_img)
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.CustomWhiteBox,
            0, 0).apply {
            try {
                setCustomTitle(getString(R.styleable.CustomWhiteBox_custom_title))
                setCustomImg(getResourceId(R.styleable.CustomWhiteBox_custom_img, R.drawable.icon_file))
            } finally {
                recycle()
            }
        }
    }


    fun setCustomTitle(text: String?){
        customTitle.text = text
        onRefresh()
    }

    fun setCustomImg(img: Int){
        customImg.setImageResource(img)
        onRefresh()
    }

    private fun onRefresh(){
        invalidate()
        requestLayout()
    }
}