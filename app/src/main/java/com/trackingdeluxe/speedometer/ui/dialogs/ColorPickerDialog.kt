package com.trackingdeluxe.speedometer.ui.dialogs

import android.app.Dialog
import android.content.Context
import android.view.View
import android.view.Window
import com.flask.colorpicker.ColorPickerView
import com.trackingdeluxe.speedometer.R

class ColorPickerDialog constructor(context: Context) : Dialog(context) {

    var selectedColor = -1
    private var colorPicView: ColorPickerView

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_color_picker)
        colorPicView = findViewById(R.id.colorPickerView)
        findViewById<View>(R.id.closeDialogButton).setOnClickListener {
            setSelectedColor()
            cancel()
        }
    }
    //get user selected color
    private fun setSelectedColor() {
        selectedColor = colorPicView.selectedColor
    }

    /**show color picker dialog
     * @param isLightTheme - flag of selected user theme to apply of dialog background
     */
    fun show(isLightTheme: Boolean) {
        window!!.setBackgroundDrawableResource(
            if (isLightTheme) R.drawable.background_popup_light
            else R.drawable.background_popup_dark
        )
        super.show()
    }
}