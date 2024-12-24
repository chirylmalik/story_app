package com.example.intermediate_submission_awal.customview

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.content.ContextCompat
import com.example.intermediate_submission_awal.R

class CVEmailEditText : AppCompatEditText {

    private lateinit var iconFormInput: Drawable

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        iconFormInput = ContextCompat.getDrawable(context, R.drawable.ic_baseline_email_24)
            ?: throw IllegalArgumentException("Drawable resource not found")
        setupTextWatcher()
    }

    private fun setupTextWatcher() {
        addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                showIconFormInput()
                error = if (s.isNotEmpty()) {
                    if (!isValidEmail(s.toString())) {
                        context.getString(R.string.label_invalid_email)
                    } else null
                } else null
            }

            override fun afterTextChanged(s: Editable) {
            }
        })
    }

    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(Regex(emailPattern))
    }

    private fun showIconFormInput() {
        setButtonDrawables(startOfTheText = iconFormInput)
    }

    private fun setButtonDrawables(
        startOfTheText: Drawable? = null,
        topOfTheText: Drawable? = null,
        endOfTheText: Drawable? = null,
        bottomOfTheText: Drawable? = null
    ) {
        setCompoundDrawablesWithIntrinsicBounds(
            startOfTheText,
            topOfTheText,
            endOfTheText,
            bottomOfTheText
        )
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        setupAppearance()
    }

    private fun setupAppearance() {
        context.apply {
            setTextColor(ContextCompat.getColor(this, R.color.black))
            setHintTextColor(ContextCompat.getColor(this, R.color.navy))
            background = ContextCompat.getDrawable(this, R.drawable.edit_text)
        }
        isSingleLine = true
        textAlignment = View.TEXT_ALIGNMENT_VIEW_START
    }
}