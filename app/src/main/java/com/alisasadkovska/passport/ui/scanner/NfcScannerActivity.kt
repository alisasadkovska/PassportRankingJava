package com.alisasadkovska.passport.ui.scanner

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.nfc.NfcAdapter
import android.nfc.NfcManager
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.alisasadkovska.passport.R
import com.alisasadkovska.passport.common.Common.*
import com.alisasadkovska.passport.common.TinyDB
import com.alisasadkovska.passport.common.Utils
import es.dmoral.toasty.Toasty
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_nfc_scanner.*
import java.util.*


class NfcScannerActivity : AppCompatActivity() {

    private lateinit var tinyDB: TinyDB

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath("fonts/Manjari-Regular.ttf")
                                .setFontAttrId(R.attr.fontPath)
                                .build())).build())

        tinyDB = TinyDB(this)


        val pm: PackageManager = this.packageManager
        if (!pm.hasSystemFeature(PackageManager.FEATURE_NFC)){
            onBackPressed()
            Toasty.info(this, getString(R.string.no_nfc), Toasty.LENGTH_SHORT).show()
        }


        Utils.onActivityCreateSetTheme(this, tinyDB.getInt(THEME_ID))
        setContentView(R.layout.activity_nfc_scanner)

        toolbar.title = getString(R.string.nfc_passport_scanner)
        setSupportActionBar(toolbar)


        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        textAboutScanner.movementMethod = LinkMovementMethod.getInstance()

        if (tinyDB.getString(PASSPORT_NUMBER)!=null)
            edtPassportNumber.setText(tinyDB.getString(PASSPORT_NUMBER))

        edtPassportNumber.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tinyDB.putString(PASSPORT_NUMBER, edtPassportNumber.text.toString())
            }
        })
        edtExpirationDate.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tinyDB.putString(EXPIRATION_DATE, edtExpirationDate.text.toString())
            }
        })
        edtBirthDate.addTextChangedListener(object :TextWatcher{
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tinyDB.putString(BIRTH_DATE, edtBirthDate.text.toString())
            }
        })

        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        if (tinyDB.getString(EXPIRATION_DATE)!=null)
            edtExpirationDate.setText(tinyDB.getString(EXPIRATION_DATE))

        if (tinyDB.getString(BIRTH_DATE)!=null)
            edtBirthDate.setText(tinyDB.getString(BIRTH_DATE))

        btnCalendarExpiration.setOnClickListener{
            val datePickerDialogue = DatePickerDialog(this, getStyle(), DatePickerDialog.OnDateSetListener { _, mYear, mMonth, mDay ->
                val selectedMonth = mMonth+1

                val mSelectedMonth = if (selectedMonth<=9) "0$selectedMonth" else selectedMonth.toString()
                val mSelectedDay = if(mDay<=9) "0$mDay" else mDay.toString()

                edtExpirationDate.setText("$mYear/$mSelectedMonth/$mSelectedDay")
                tinyDB.putString(EXPIRATION_DATE, edtExpirationDate.text.toString())
            }, year, month, day)
            datePickerDialogue.show()
        }

        btnCalendarBirth.setOnClickListener {
            val datePickerDialogue = DatePickerDialog(this, getStyle(), DatePickerDialog.OnDateSetListener { _, mYear, mMonth, mDay ->
                val selectedMonth = mMonth+1

                val mSelectedMonth = if (selectedMonth<=9) "0$selectedMonth" else selectedMonth.toString()
                val mSelectedDay = if(mDay<=9) "0$mDay" else mDay.toString()

                edtBirthDate.setText("$mYear/$mSelectedDay/$mSelectedMonth")
                tinyDB.putString(BIRTH_DATE, edtBirthDate.text.toString())
            }, year, month, day)
            datePickerDialogue.show()
        }


        buttonStartScanner.setOnClickListener {
            val manager = this.getSystemService(Context.NFC_SERVICE) as NfcManager
            val adapter: NfcAdapter = manager.defaultAdapter
            when {
                !adapter.isEnabled -> Toasty.warning(this, getString(R.string.enable_nfc), Toasty.LENGTH_SHORT).show()
                TextUtils.isEmpty(edtPassportNumber.text) -> textInputPassportNumber.error = getString(R.string.numberRequired)
                TextUtils.isEmpty(edtExpirationDate.text) -> textInputExpirationDate.error = getString(R.string.expiration_required)
                TextUtils.isEmpty(edtBirthDate.text) -> textInputBirthRate.error = getString(R.string.birth_date_required)
                edtExpirationDate.text!!.length<10 ->{
                    Toasty.info(this, getString(R.string.format_expiration), Toasty.LENGTH_SHORT).show()
                    textInputExpirationDate.error = getString(R.string.format_expiration)
                }
                edtBirthDate.text!!.length<10 ->{
                    Toasty.info(this, getString(R.string.format_birthDate), Toasty.LENGTH_SHORT).show()
                    textInputBirthRate.error = getString(R.string.format_birthDate)
                }
                else -> startScanner()
            }
        }
    }


    private fun startScanner() {
        val intent = Intent(this, ScannerActivity::class.java)
        startActivity(intent)
    }

    private fun getStyle(): Int {
        return if (tinyDB.getInt(THEME_ID)==1)
            R.style.my_dialog_theme_dark
        else
            R.style.my_dialog_theme
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home-> {
                onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }



}