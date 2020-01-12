package com.alisasadkovska.passport.ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.alisasadkovska.passport.R
import com.alisasadkovska.passport.ViewHolder.ExploreViewHolder
import com.alisasadkovska.passport.common.Common
import com.alisasadkovska.passport.common.Common.database
import com.alisasadkovska.passport.common.TinyDB
import com.alisasadkovska.passport.common.Utils
import com.alisasadkovska.passport.Model.Ranking
import com.alisasadkovska.passport.Model.SpinnerModel
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_explore.*
import kotlinx.android.synthetic.main.activity_ranking.progressBar
import kotlinx.android.synthetic.main.activity_ranking.recycler
import kotlinx.android.synthetic.main.activity_ranking.toolbar

class ExploreActivity : AppCompatActivity() {
    private var spinnerModels:ArrayList<SpinnerModel> = ArrayList()
    private lateinit var spinnerAdapter: ArrayAdapter<SpinnerModel>

    var themeId = 0
    private lateinit var tinyDB: TinyDB


    private lateinit var ranking: DatabaseReference
    private var filteredAdapter: FirebaseRecyclerAdapter<Ranking, ExploreViewHolder> ?=null

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ViewPump.init(ViewPump.builder()
                .addInterceptor(CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                                .setDefaultFontPath(Common.fontPath)
                                .setFontAttrId(R.attr.fontPath)
                                .build())).build())

        tinyDB = TinyDB(this)
        themeId = tinyDB.getInt(Common.THEME_ID)
        Utils.onActivityCreateSetTheme(this, themeId)
        setContentView(R.layout.activity_explore)

        toolbar.title = getString(R.string.menu_explore)
        setSupportActionBar(toolbar)
        if (supportActionBar != null) {
            supportActionBar!!.setDisplayShowTitleEnabled(true)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        assignSpinnerModel()

        ranking = database.getReference(Common.Top)
        ranking.keepSynced(true)

        val screenSize = resources.configuration.screenLayout and
                Configuration.SCREENLAYOUT_SIZE_MASK
        var gridLayoutManager = GridLayoutManager(this, 3)
        when (screenSize) {
            Configuration.SCREENLAYOUT_SIZE_XLARGE -> gridLayoutManager = GridLayoutManager(this, 6)
            Configuration.SCREENLAYOUT_SIZE_UNDEFINED -> {
            }
            Configuration.SCREENLAYOUT_SIZE_LARGE -> gridLayoutManager = GridLayoutManager(this, 4)
            Configuration.SCREENLAYOUT_SIZE_SMALL -> gridLayoutManager = GridLayoutManager(this, 2)
            Configuration.SCREENLAYOUT_SIZE_NORMAL -> gridLayoutManager = GridLayoutManager(this, 3)
            else -> gridLayoutManager = GridLayoutManager(this, 3)
        }
        recycler.layoutManager = gridLayoutManager

        spinner.adapter = spinnerAdapter
        spinner.setSelection(1)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                getCategoryName(spinnerModels[position].name)
            }
        }
    }

    private fun getCategoryName(name: String) {
        val category:String = if (name == getString(R.string.residence) || name == getString(R.string.citizenship))
            Common.Industry
        else if (name == getString(R.string.black) || name == getString(R.string.blue) || name == getString(R.string.red) || name == getString(R.string.green))
            Common.Color
        else Common.Continent

        progressBar.visibility = View.GONE
        populateFilteredAdapter(category, name)
    }

    private fun assignSpinnerModel() {
        spinnerModels.add(SpinnerModel(true, R.drawable.blank_square, getString(R.string.continent)))
        spinnerModels.add(SpinnerModel(false, R.drawable.asia, getString(R.string.asia)))
        spinnerModels.add(SpinnerModel(false, R.drawable.africa, getString(R.string.africa)))
        spinnerModels.add(SpinnerModel(false,  R.drawable.europe, getString(R.string.europe)))
        spinnerModels.add(SpinnerModel(false,  R.drawable.north_america, getString(R.string.north_america)))
        spinnerModels.add(SpinnerModel(false,  R.drawable.south_america, getString(R.string.south_america)))
        spinnerModels.add(SpinnerModel(false,  R.drawable.oceania, getString(R.string.oceania)))

        spinnerModels.add(SpinnerModel(true,  R.drawable.blank_square, getString(R.string.color)))
        spinnerModels.add( SpinnerModel(false,R.drawable.black_square, getString(R.string.black)))
        spinnerModels.add( SpinnerModel(false,R.drawable.blue_square, getString(R.string.blue)))
        spinnerModels.add( SpinnerModel(false,R.drawable.green_square, getString(R.string.green)))
        spinnerModels.add( SpinnerModel(false,R.drawable.red_square, getString(R.string.red)))

        spinnerModels.add(SpinnerModel(true, R.drawable.blank_square, getString(R.string.industry)))
        spinnerModels.add(SpinnerModel(false,R.drawable.citizen, getString(R.string.citizenship)))
        spinnerModels.add(SpinnerModel(false,R.drawable.residency, getString(R.string.residence)))

        spinnerAdapter = object : ArrayAdapter<SpinnerModel>(this,android.R.layout.simple_spinner_dropdown_item, spinnerModels){
            override fun isEnabled(position: Int): Boolean {
                return !spinnerModels[position].isHeader
            }

            override fun areAllItemsEnabled(): Boolean {
                return false
            }

            @SuppressLint("InflateParams", "ViewHolder")
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view: View = layoutInflater.inflate(R.layout.spinner_header, null)

                val spinnerHeaderTxt = view.findViewById<TextView>(R.id.spinner_header_txt)
                val model : SpinnerModel = spinnerModels[position]
                if (spinnerHeaderTxt != null) {
                    spinnerHeaderTxt.text = model.name
                }
                return view
            }

            @SuppressLint("InflateParams")
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = layoutInflater.inflate(R.layout.spinner_item, null)

                val spinnerText = view.findViewById<TextView>(R.id.spinner_name_txt)
                val spinnerLogo = view.findViewById<ImageView>(R.id.spinner_item_logo)
                val model : SpinnerModel = spinnerModels[position]

                spinnerText.text = model.name

                if (model.drawable>0)
                    spinnerLogo.setImageDrawable(ResourcesCompat.getDrawable(resources, model.drawable, null))

                return view
            }
        }

    }

    private fun populateFilteredAdapter(category: String, name: String) {
        val query = ranking.orderByChild(category).equalTo(name)
        val options = FirebaseRecyclerOptions.Builder<Ranking>()
                .setQuery(query, Ranking::class.java)
                .build()
        filteredAdapter = object : FirebaseRecyclerAdapter<Ranking, ExploreViewHolder>(options) {
            override fun onBindViewHolder(exploreViewHolder: ExploreViewHolder, position: Int, ranking: Ranking) {
                Picasso.get()
                        .load(ranking.cover)
                        .into(exploreViewHolder.coverImg, object : Callback {
                            override fun onSuccess() {
                                exploreViewHolder.progressBar.visibility = View.GONE
                            }

                            override fun onError(e: Exception) {}
                        })
                exploreViewHolder.setItemClickListener { _: View?, _: Int, _: Boolean ->
                    openCountryDetails(ranking.cover, ranking.totalScore, ranking.name
                            , ranking.visaOnArrival, ranking.visaFree, ranking.visaRequired, ranking.eTa, ranking.timestamp)
                }
            }

            override fun getItem(pos: Int): Ranking {
                return super.getItem(itemCount - 1 - pos)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExploreViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.top_item_filtered, parent, false)
                return ExploreViewHolder(itemView)
            }
        }
        recycler.adapter = filteredAdapter
        recycler.adapter?.notifyDataSetChanged()
        filteredAdapter?.startListening()
        filteredAdapter?.registerAdapterDataObserver(object : AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)
                if (itemCount != 0) progressBar.visibility = View.GONE
            }
        })
    }


    @SuppressLint("InflateParams")
    private fun openCountryDetails(cover: String, totalScore: Int, name: String, visaOnArrival: Int, visaFree: Int, visaRequired: Int, eVisa: Int, timestamp: String) {

        val alertDialog = AlertDialog.Builder(this)
        alertDialog.setCancelable(true)
        alertDialog.setTitle(name)

        val inflater = this.layoutInflater
        val detailsDialogue = inflater.inflate(R.layout.top_item_dialogue, null)

        val coverImg = detailsDialogue.findViewById<ImageView>(R.id.coverImg)
        val progressBar = detailsDialogue.findViewById<ProgressBar>(R.id.progressBar)
        val countryProgress = detailsDialogue.findViewById<ProgressBar>(R.id.countryProgress)
        val totalTxt = detailsDialogue.findViewById<TextView>(R.id.textTotal)
        val requiredTat = detailsDialogue.findViewById<TextView>(R.id.textVisaRequired)
        val visaFreeTxt = detailsDialogue.findViewById<TextView>(R.id.textVisaFree)
        val visaOnArrivalTxt = detailsDialogue.findViewById<TextView>(R.id.textVisaOnArrival)
        val visaEta = detailsDialogue.findViewById<TextView>(R.id.textEVisa)
        val timeStamp = detailsDialogue.findViewById<TextView>(R.id.lastUpdate)

        Picasso.get().load(cover).into(coverImg, object : Callback {
            override fun onSuccess() {
                progressBar.visibility = View.GONE
            }

            override fun onError(e: Exception) {
                Picasso.get().load(R.drawable.ic_terrain_black_24dp)
                        .into(coverImg)
                progressBar.visibility = View.GONE
            }
        })

        countryProgress.progress = visaOnArrival
        countryProgress.secondaryProgress = totalScore
        timeStamp.text = timestamp
        totalTxt.text = totalScore.toString()
        requiredTat.text = visaRequired.toString()
        visaFreeTxt.text = visaFree.toString()
        visaOnArrivalTxt.text = visaOnArrival.toString()
        visaEta.text = eVisa.toString()

        alertDialog.setNegativeButton(getString(R.string.close)) {
            dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
        val alert = alertDialog.create()
        alert.setView(detailsDialogue)
        alert.show()
    }

    public override fun onStart() {
        super.onStart()
        filteredAdapter?.startListening()
    }

    public override fun onStop() {
        filteredAdapter?.stopListening()
        super.onStop()
    }

    public override fun onResume() {
        super.onResume()
        filteredAdapter?.startListening()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }
}