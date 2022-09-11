package com.alexthedev.solutions.intercommer.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.core.widget.doOnTextChanged
import com.alexthedev.solutions.intercommer.R
import com.alexthedev.solutions.intercommer.core.*
import com.alexthedev.solutions.intercommer.databinding.AlertViewBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.alert_view.view.*
import kotlinx.android.synthetic.main.bottom_sheet_layout.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext


class MainActivity : AppCompatActivity(), CoroutineScope {

    companion object {
        private const val PREFS_FILE_NAME = "IntercommerPrefs"
        private const val PREF_RADIX = "radix"
        private const val PREF_MAX_LENGTH = "length"
        private const val PREF_ANTI_ALIAS = "alias"
        private const val PREF_ENCODE_LENGTH = "bitsCount"
        private const val DEFAULT_BACK_PRESSED_DIFF = 2000L
        private const val PERMS_REQ_CODE = 9
    }

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private lateinit var bsBehavior: BottomSheetBehavior<View>
    private var currentBitmap: Bitmap? = null
    private var lastTimeMillis: Long = 0
    private var mRadix = 16
    private var mLength = 64
    private var mEncodeLength = 8
    private var mAntiAlias = false
    private var mDialog: AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
        )
        initLengthAndRadix()
        initClicks()
        setupBottomSheet()
        initInput()
        toolbar_radix.alpha = 1f
        toolbar_logo.gone()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) requestPerms()
        else showInput()
    }

    override fun onBackPressed() {
        if (bsBehavior.state != BottomSheetBehavior.STATE_COLLAPSED)
            bsBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        else {
            val millis = System.currentTimeMillis()
            if (millis - lastTimeMillis >= DEFAULT_BACK_PRESSED_DIFF) {
                lastTimeMillis = millis
                Toast.makeText(this, R.string.one_more_time_to_exit, Toast.LENGTH_LONG).show()
            } else finish()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        grantResults.forEach {
            if (it != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.no_perms, Toast.LENGTH_LONG).show()
                lin_no_perms.visible()
                openAppSettings()
                return
            }
        }
        lin_no_perms.gone()
        showInput()
    }

    private fun initLengthAndRadix() {
        mRadix = getRadix()
        mLength = getMaxLength()
        mAntiAlias = getAntiAlias()
        mEncodeLength = getEncodeLength()
        toolbar_radix.apply {
            text = getString(R.string.toolbar_radix, mRadix)
            underline()
        }
        txt_max_length.apply {
            text = getString(R.string.length_vs_max_length, 0, mLength)
            underline()
        }
        num_input.inputType = if (mRadix <= 10) InputType.TYPE_CLASS_NUMBER
        else InputType.TYPE_CLASS_TEXT
        num_input.filters = arrayOf(InputFilter.LengthFilter(mLength))
        txt_encode_length.apply {
            if (mRadix == 2) gone()
            else {
                visible()
                text = getString(R.string.body_encode_length, mEncodeLength)
            }
            underline()
        }
        check_alias.isChecked = mAntiAlias
    }

    private fun initInput() {
        num_input.doAfterTextChanged {
            txt_max_length.apply {
                text =
                    getString(
                        R.string.length_vs_max_length,
                        it?.length ?: 0,
                        num_input.getMaxLength()
                    )
                underline()
            }
        }
    }

    private fun setupBottomSheet() {
        bsBehavior = BottomSheetBehavior.from(bs_root)
        bs_root.onClick { /* stub */ }
        bsBehavior.apply {
            peekHeight = getHeightOfBs()
            isHideable = false
            bs_buttons_lin.gone()
            bs_result.gone()
            bs_title.text = getString(R.string.bs_demo_title)
            addBottomSheetCallback(StandardBsCallback(view))
        }
        btn_save_to.onClick {
            if (currentBitmap != null) {
                stub.isVisible = true
                launch {
                    delay(500)
                    saveInPicturesDirectory(
                        this@MainActivity, currentBitmap!!, {
                            Toast.makeText(
                                this@MainActivity,
                                R.string.result_saved,
                                Toast.LENGTH_SHORT
                            )
                                .show()
                        }
                    ) {
                        showErrorDialog(R.string.err_saving)
                    }
                    stub.isVisible = false
                }
            }
        }
        btn_share.onClick {
            currentBitmap?.let {
                stub.isVisible = true
                launch {
                    delay(500)
                    saveInPicturesDirectory(
                        this@MainActivity,
                        it,
                        { uri -> startActivity(createIntentForSharingImage(uri)) },
                        {
                            showErrorDialog(R.string.err_saving)
                        }
                    )
                    stub.isVisible = false
                }
            }
        }
    }

    private fun initClicks() {
        stub.onClick { /* This is a stub. */ }
        btn_regain.onClick {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                requestPerms()
        }
        btn_encode.onClick {
            hideKeyboard()
            checkAndSubmit()
        }
        view.onClick {
            bsBehavior.state =
                if (bsBehavior.state == BottomSheetBehavior.STATE_COLLAPSED)
                    BottomSheetBehavior.STATE_EXPANDED
                else BottomSheetBehavior.STATE_COLLAPSED
        }
        txt_max_length.onClick { showLengthAlert() }
        txt_encode_length.onClick { showEncodeLengthAlert() }
        check_alias.setOnCheckedChangeListener { _, isChecked ->
            saveAntiAlias(isChecked)
            mAntiAlias = isChecked
        }
        toolbar_radix.onClick { showRadixAlert() }
    }

    private fun getHeightOfBs(): Int {
        val params = view.layoutParams as LinearLayout.LayoutParams
        return params.height + params.topMargin + params.bottomMargin
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestPerms() {
        requestPermissions(
            arrayOf(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            PERMS_REQ_CODE
        )
    }

    private fun showInput() {
        lin_input.visible()
        progressBar2.gone()
    }

    private fun checkAndSubmit() {
        showLoading()
        launch {
            try {
                val binString = with(num_input.getContent()) {
                    if (mRadix == 2) this
                    else {
                        val result = StringBuilder()
                        this.forEach {
                            result.append(Integer.toBinaryString(it.toString().toInt(mRadix)).padStart(mEncodeLength, '0'))
                        }
                        result.toString()
                    }
                }
                currentBitmap = createCircularBitsImage(
                    binString,
                    1500,
                    Color.WHITE,
                    Color.BLACK,
                    mAntiAlias
                )
                showInput()
                showResult()
            } catch (e: StringIsNotBinaryException) {
                showErrorDialog(R.string.err_str_not_binary)
                showInput()
            } catch (t: Throwable) {
                showErrorDialog(R.string.alert_bad_data)
                showInput()
                t.printStackTrace()
            }
        }
    }

    private fun showLoading() {
        progressBar2.visible()
        lin_input.gone()
    }

    private fun showResult() {
        bsBehavior.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
            bs_buttons_lin.visible()
            bs_result.visible()
            bs_title.text = getString(R.string.result_ready)
        }
        bs_result.setImageBitmap(currentBitmap)
    }

    private fun showErrorDialog(@StringRes errorMsg: Int) {
        createAlertDialogWithMessage(
            this@MainActivity,
            R.string.err_dial_title,
            R.string.err_dial_ok,
            errorMsg
        ).show()
    }

    private fun getRadix(): Int = workWithSharedPrefs {
        it.getInt(PREF_RADIX, 16)
    }

    private fun saveRadix(radix: Int) = workWithSharedPrefsEditor {
        it.putInt(PREF_RADIX, radix)
    }

    private fun getMaxLength(): Int = workWithSharedPrefs {
        it.getInt(PREF_MAX_LENGTH, 8)
    }

    private fun saveMaxLength(maxLength: Int) = workWithSharedPrefsEditor {
        it.putInt(PREF_MAX_LENGTH, maxLength)
    }

    private fun saveAntiAlias(antiAlias: Boolean) = workWithSharedPrefsEditor {
        it.putBoolean(PREF_ANTI_ALIAS, antiAlias)
    }

    private fun getAntiAlias(): Boolean = workWithSharedPrefs {
        it.getBoolean(PREF_ANTI_ALIAS, false)
    }

    private fun getEncodeLength(): Int = workWithSharedPrefs {
        it.getInt(PREF_ENCODE_LENGTH, 8)
    }

    private fun saveEncodeLength(length: Int) = workWithSharedPrefsEditor {
        it.putInt(PREF_ENCODE_LENGTH, length)
    }

    private inline fun workWithSharedPrefsEditor(action: (sharedPrefsEditor: SharedPreferences.Editor) -> Unit) {
        val editor = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE).edit()
        action(editor)
        editor.apply()
    }

    private inline fun <T> workWithSharedPrefs(crossinline action: (prefs: SharedPreferences) -> T): T {
        val prefs = getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)
        return action(prefs)
    }

    private fun showRadixAlert() {
        initAndShowAlertInternal(
            { (it.toIntOrNull() ?: 0) in 2..16 },
            R.string.alert_radix,
            getString(R.string.alert_hint_radix),
        ) {
            val num = it.toIntOrNull() ?: 0
            refreshRadix(num)
            saveRadix(num)
        }
    }

    private fun showLengthAlert() {
        initAndShowAlertInternal(
            { (it.toIntOrNull() ?: 0) in 1..256 },
            R.string.alert_length,
            getString(R.string.alert_hint_length)
        ) {
            val num = it.toIntOrNull() ?: 0
            refreshLength(num)
            saveMaxLength(num)
        }
    }

    private fun showEncodeLengthAlert() {
        initAndShowAlertInternal(
            {
                val num = it.toIntOrNull()?:0
                num in getMinEncodeLength(mRadix)..64
            },
            R.string.alert_encode_length,
            getString(R.string.alert_hint_encode_length, getMinEncodeLength(mRadix))
        ) {
            val num = it.toIntOrNull()?:0
            refreshEncodeLength(num)
            saveEncodeLength(num)
        }
    }

    private fun refreshRadix(newRadix: Int) {
        mRadix = newRadix
        toolbar_radix.apply {
            text = getString(R.string.toolbar_radix, mRadix)
            underline()
        }
        hideKeyboard()
        num_input.inputType = if (newRadix <= 10) {
            InputType.TYPE_CLASS_NUMBER
        } else InputType.TYPE_CLASS_TEXT
        if (mRadix == 2) txt_encode_length.gone()
        else txt_encode_length.visible()
    }

    private fun refreshLength(newLength: Int) {
        mLength = newLength
        val content = num_input.getContent()
        if (content.length > newLength)
            num_input.setText(content.substring(0 until newLength))
        txt_max_length.text =
            getString(R.string.length_vs_max_length, num_input.getContent().length, newLength)
        num_input.apply {
            filters = arrayOf(InputFilter.LengthFilter(mLength))
            setSelection(num_input.getContent().length)
            underline()
        }
    }

    private fun refreshEncodeLength(newLength: Int) {
        mEncodeLength = newLength
        txt_encode_length.apply {
            text = getString(R.string.body_encode_length, newLength)
            underline()
        }
    }

    @SuppressLint("InflateParams")
    private fun initAndShowAlertInternal(
        validator: (input: String) -> Boolean,
        @StringRes msg: Int,
        hint: String,
        onOkClicked: (input: String) -> Unit
    ) {
        AlertViewBinding.inflate(layoutInflater, null, false).apply {
            alertNotAcceptable.gone()
            alertTitle.text = getString(msg)
            alertInput.apply {
                doOnTextChanged { _, _, _, _ -> alertNotAcceptable.invisible() }
                setHint(hint)
            }
            btnOk.onClick {
                val input = alertInput.getContent()
                if (validator(input)) {
                    onOkClicked(input)
                    mDialog?.dismiss()
                    mDialog = null
                    hideKeyboard()
                } else alertNotAcceptable.visible()
            }
            btnCancel.onClick {
                mDialog?.dismiss()
                mDialog = null
                hideKeyboard()
            }
            mDialog = AlertDialog.Builder(this@MainActivity)
                .setView(this.root)
                .create()
            mDialog?.show()
        }
    }

    private fun getMinEncodeLength(radix: Int): Int {
        return when {
            radix <= 4 -> 2
            radix <= 8 -> 3
            radix <= 16 -> 4
            else -> Int.MAX_VALUE
        }
    }
}