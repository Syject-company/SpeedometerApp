package com.trackingdeluxe.speedometer.ui

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.recyclerview.widget.LinearLayoutManager
import com.trackingdeluxe.speedometer.R
import com.trackingdeluxe.speedometer.adapters.SpeedIntervalAdapter
import com.trackingdeluxe.speedometer.data.DataManager
import com.trackingdeluxe.speedometer.data.enums.MetricType
import com.trackingdeluxe.speedometer.data.models.SpeedInterval
import com.trackingdeluxe.speedometer.ui.base.BaseActivity
import com.trackingdeluxe.speedometer.ui.dialogs.ColorPickerDialog
import com.trackingdeluxe.speedometer.utils.AppConstants
import kotlinx.android.synthetic.main.activity_settings.*
import java.util.*
import kotlin.collections.ArrayList


class ActivitySetting : BaseActivity(),
    View.OnClickListener, SpeedIntervalAdapter.OnSpeedLimitItemChanges {
    private lateinit var colorPickerDialog: ColorPickerDialog
    private lateinit var popupSelectMetricType: PopupWindow
    private lateinit var popupAddInterval: PopupWindow
    private lateinit var metricType: MetricType
    private var resultIntent = Intent()
    private var speedIntervalAdapter: SpeedIntervalAdapter? = null
    private var lastSelectedLimitColor = -1
    private var isLastSoundEnableValue = true
    private var isProVersion = false
    private var isMakeUpdate = false

    @SuppressLint("SourceLockedOrientationActivity")

    override fun getLayoutID() = R.layout.activity_settings

    override fun isPortraitOrientation() = true

    override fun init(savedInstanceState: Bundle?) {
        initValues()
        initButtonListener()
        if (isProVersion) {
            initSpeedIntervalsView()
            initColorPickerDialog()
            loadSpeedIntervals()
        }
        initViews()
    }

    //init activity views
    private fun initViews() {
        createMetricTypeView()
        createPopupWindowAddInterval()
        changeMetricUIElement()
        iniThemeSwitch()
        if (!isProVersion) {
            setSpeedInterval.setTextColor(getAppColor(R.color.disableSetIntervalTextColor))
            setDrawableOnTextView(setSpeedInterval, R.drawable.ic_arrow_down_gray)
        }
        changeEnableSoundButton()
        applyThemeToPopupViews(DataManager.instance.isLightTheme())
    }

    //init theme switcher
    private fun iniThemeSwitch() {
        themeSwitch.isChecked = DataManager.instance.isLightTheme()
        themeSwitch.setOnCheckedChangeListener { _, isLightTheme ->
            DataManager.instance.setIsLightThemeTheme(isLightTheme)
            resultIntent.putExtra(AppConstants.THEME_EXTRA, isLightTheme)
            setBackgroundByTheme(isLightTheme)
            applyThemeToPopupViews(isLightTheme)
            isMakeUpdate = true
        }
    }

    /**apply theme to popup windows
     * @param isLightTheme - flag is user select light theme
     */
    private fun applyThemeToPopupViews(isLightTheme: Boolean) {
        val popupDrawable =
            if (isLightTheme) R.drawable.background_popup_light
            else R.drawable.background_popup_dark
        val radioButtonDrawable =
            if (isLightTheme) R.drawable.selector_setting_radio_button_light
            else R.drawable.selector_setting_radio_button
        popupAddInterval.contentView.setBackgroundResource(popupDrawable)
        popupSelectMetricType.contentView.setBackgroundResource(popupDrawable)
        popupSelectMetricType.contentView.findViewById<AppCompatRadioButton>(R.id.inKilometersButton)
            .setButtonDrawable(radioButtonDrawable)
        popupSelectMetricType.contentView.findViewById<AppCompatRadioButton>(R.id.inMilesButton)
            .setButtonDrawable(radioButtonDrawable)
    }

    //change on enable - disable sound button text if user press
    private fun changeEnableSoundButton() {
        popupAddInterval.contentView.findViewById<TextView>(R.id.isSoundEnableButton)
            .setText(
                if (isLastSoundEnableValue) R.string.sound_enable
                else R.string.sound_disable
            )
    }

    //create metric type popup window
    private fun createMetricTypeView() {
        popupSelectMetricType = PopupWindow(
            initChangeMetricTypeView(),
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(ColorDrawable())
            isOutsideTouchable = true
            setOnDismissListener {
                setDrawableOnTextView(chaneMetricButton, R.drawable.ic_arrow_down)
            }
        }
    }

    //create add new speed interval popup window
    private fun createPopupWindowAddInterval() {
        popupAddInterval = PopupWindow(
            initAddIntervalsView(),
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            setBackgroundDrawable(ColorDrawable())
            isOutsideTouchable = true
            setOnDismissListener {
                setDrawableOnTextView(setSpeedInterval, R.drawable.ic_arrow_down)
            }
        }
    }

    //create metric type popup view
    @SuppressLint("InflateParams")
    private fun initChangeMetricTypeView(): View {
        val popupView = layoutInflater.inflate(R.layout.popup_setting_metrics, null)
        if (metricType == MetricType.MILES) {
            popupView.findViewById<AppCompatRadioButton>(R.id.inMilesButton).isChecked = true
        } else {
            popupView.findViewById<AppCompatRadioButton>(R.id.inKilometersButton).isChecked = true
        }
        popupView.findViewById<RadioGroup>(R.id.metricRadioGroup)
            .setOnCheckedChangeListener { _, checkedId ->
                metricType = MetricType.getMetricTypeByBool(checkedId == R.id.inMilesButton)
                changeMetricUIElement()
                speedIntervalAdapter?.changeShowResultInMiles(metricType)
                DataManager.instance.saveShowResultInMiles(metricType.boolValue)
                sendNewMetricTypeToAppActivity()
                isMakeUpdate = true
            }
        return popupView
    }

    //create add interval  popup view
    @SuppressLint("InflateParams")
    private fun initAddIntervalsView(): View {
        val popupView = layoutInflater.inflate(R.layout.popup_speed_interval, null)
        val isSoundEnableButton = popupView.findViewById<TextView>(R.id.isSoundEnableButton)
        val colorPicker = popupView.findViewById<View>(R.id.colorPicker)
        isSoundEnableButton.setOnClickListener {
            isLastSoundEnableValue = !isLastSoundEnableValue
            DataManager.instance.setLastIsSoundEnabled(isLastSoundEnableValue)
            changeEnableSoundButton()
        }
        colorPicker.setBackgroundColor(lastSelectedLimitColor)
        colorPicker.setOnClickListener(this)
        popupView.findViewById<View>(R.id.addIntervalButton).setOnClickListener {
            val speedIntervalValueText =
                popupView.findViewById<EditText>(R.id.speedLimitEditText).text.toString()
            if (speedIntervalValueText.isEmpty()) {
                showErrorIntervalToast()
            } else {
                val speedIntervalValue = speedIntervalValueText.toFloat()
                if (speedIntervalValue > 0 && speedIntervalValue < metricType.maxSpeed) {
                    addSpeedInterval(speedIntervalValue)
                } else {
                    showErrorIntervalToast()
                }
            }
            popupAddInterval.dismiss()
        }
        return popupView
    }

    /** set end drawable to text view
     * @param textView - textView to add drawable
     * @param drawableID - drawable resource id to draw of end in textView
     */
    private fun setDrawableOnTextView(textView: TextView, drawableID: Int) {
        textView.setCompoundDrawablesRelativeWithIntrinsicBounds(0, 0, drawableID, 0)
    }

    //add to result intent update new metric type views
    private fun sendNewMetricTypeToAppActivity() {
        resultIntent.putExtra(AppConstants.METRIC_TYPE_EXTRA, metricType)
        setResult(Activity.RESULT_OK, resultIntent)
    }

    // change metric text according selected metric type
    private fun changeMetricUIElement() {
        chaneMetricButton.text =
            getString(metricType.metricFullText).toLowerCase(Locale.getDefault())
    }

    /*load saved values
    * selected last metric type
    * is Pro version of app
    * is last user enable sound
    * last user selected color - if user not select before - set default green color
    */
    private fun initValues() {
        metricType = intent.extras!!.getSerializable(AppConstants.METRIC_TYPE_EXTRA) as MetricType
        isProVersion = intent.extras!!.getBoolean(AppConstants.IS_PRO_VERSION)
        lastSelectedLimitColor = DataManager.instance.getLastSelectedColor()
        if (lastSelectedLimitColor == -1)
            lastSelectedLimitColor = getAppColor(R.color.mainGreenColor)
        isLastSoundEnableValue = DataManager.instance.getLastIsSoundEnabled()
    }

    //init speed interval recycler view
    private fun initSpeedIntervalsView() {
        this.speedIntervalAdapter = SpeedIntervalAdapter(this)
        speedIntervalAdapter!!.metricType = metricType
        speedIntervalAdapter!!.onSpeedLimitItemChanges = this
        speedIntervalsView.layoutManager = object : LinearLayoutManager(this) {
            override fun isAutoMeasureEnabled() = true
        }
        speedIntervalsView.adapter = speedIntervalAdapter
    }

    //load and show saved intervals from database
    private fun loadSpeedIntervals() {
        Thread {
            val speedIntervals = DataManager.instance.getAllSpeedIntervals()
            if (speedIntervals.isNotEmpty())
                runOnUiThread { speedIntervalAdapter!!.addItems(speedIntervals) }
        }.start()
    }

    //init onclick buttons listener
    private fun initButtonListener() {
        setSpeedInterval.setOnClickListener(this)
        chaneMetricButton.setOnClickListener(this)
    }

    //init color picker dialog
    private fun initColorPickerDialog() {
        colorPickerDialog = ColorPickerDialog(this)
        colorPickerDialog.setOnCancelListener {
            if (colorPickerDialog.selectedColor != -1) {
                setSelectedColor(colorPickerDialog.selectedColor)
                saveSelectedColor(colorPickerDialog.selectedColor)
            }
        }
    }

    /**save last selected user color
     * @param selectedColor - user selected color
     */
    private fun saveSelectedColor(selectedColor: Int) {
        DataManager.instance.setLastSelectedColor(selectedColor)
    }

    /**change select color button background according selected color
     * @param selectedColor - user selected color
     */
    private fun setSelectedColor(selectedColor: Int) {
        lastSelectedLimitColor = selectedColor
        popupAddInterval.contentView.findViewById<View>(R.id.colorPicker)
            .setBackgroundColor(lastSelectedLimitColor)
    }


    /**trigger when user delete present interval
     * @param item - interval to delete
     */
    override fun onSpeedIntervalRemoved(item: SpeedInterval) {
        DataManager.instance.deleteSpeedInterval(item)
        isMakeUpdate = true
    }

    /**trigger when user update present interval
     * @param item - interval to update
     */
    override fun onSpeedIntervalUpdated(item: SpeedInterval) {
        DataManager.instance.updateSpeedInterval(item)
        isMakeUpdate = true
    }

    override fun onClick(v: View) {
        hideKeyboard()
        when (v.id) {
            R.id.setSpeedInterval -> {
                if (isProVersion) {
                    setDrawableOnTextView(v as TextView, R.drawable.ic_arrow_up)
                    showIntervalPopup()
                }
            }
            R.id.colorPicker -> {
                colorPickerDialog.show(DataManager.instance.isLightTheme())
            }
            R.id.chaneMetricButton -> {
                setDrawableOnTextView(v as TextView, R.drawable.ic_arrow_up)
                popupSelectMetricType.showAsDropDown(chaneMetricButton)
            }
        }
    }

    /* show create interval popup window
    * apply theme and speed type in popup view
    */
    private fun showIntervalPopup() {
        popupAddInterval.contentView.findViewById<View>(R.id.colorPicker)
            .setBackgroundColor(lastSelectedLimitColor)
        popupAddInterval.contentView.findViewById<TextView>(R.id.metricText).text =
            getString(metricType.perHourText)
        popupAddInterval.showAsDropDown(setSpeedInterval)
    }

    /** create speed interval
     * check if speed interval present - update him
     * @param speedIntervalValue - input by user speed interval
     */
    private fun addSpeedInterval(speedIntervalValue: Float) {
        isMakeUpdate = true
        val speedIntervalByMetersPerSeconds =
            calcSpeedToMeterPerSeconds(metricType, speedIntervalValue)
        var speedInterval = getIntervalModel(speedIntervalByMetersPerSeconds)
        speedInterval?.let {
            it.intervalColor = lastSelectedLimitColor
            it.isEnableSound = isLastSoundEnableValue
            DataManager.instance.updateSpeedInterval(it)
            speedIntervalAdapter!!.notifyDataSetChanged()
            return
        }
        speedInterval = SpeedInterval()
        speedInterval.intervalColor = lastSelectedLimitColor
        speedInterval.isEnableSound = isLastSoundEnableValue
        speedInterval.speedMetPerSeconds = speedIntervalByMetersPerSeconds
        speedInterval.id = DataManager.instance.insertSpeedInterval(speedInterval)
        speedIntervalAdapter!!.addItem(speedInterval)
    }

    /** get interval model if model with input by user speed  already present
     * @param speedMetPerSeconds - input user speed in meter per seconds
     */
    private fun getIntervalModel(speedMetPerSeconds: Float): SpeedInterval? {
        speedIntervalAdapter!!.items.forEach {
            if (speedMetPerSeconds == it.speedMetPerSeconds) {
                return it
            }
        }
        return null
    }

    //show error toast if user input incorrect speed interval
    private fun showErrorIntervalToast() {
        Toast.makeText(
            this,
            "${getString(R.string.error_message_speed_limit)} ${metricType.maxSpeed} " +
                    getString(metricType.perHourText),
            Toast.LENGTH_LONG
        ).show()
    }

    //if need update gauge add to result intent new data
    override fun onBackPressed() {
        if (isMakeUpdate) {
            speedIntervalAdapter?.let {
                resultIntent.putParcelableArrayListExtra(
                    AppConstants.SPEED_INTERVALS_EXTRA,
                    ArrayList(it.items)
                )
            }
            setResult(Activity.RESULT_OK, resultIntent)
        }
        super.onBackPressed()
    }
}