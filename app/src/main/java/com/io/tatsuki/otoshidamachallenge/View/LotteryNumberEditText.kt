package com.io.tatsuki.otoshidamachallenge.View

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import com.io.tatsuki.otoshidamachallenge.R

class LotteryNumberEditText @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.lottery_number_edittext_view, this, true)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()

    }
}