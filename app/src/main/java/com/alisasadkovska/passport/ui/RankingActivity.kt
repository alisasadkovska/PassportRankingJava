package com.alisasadkovska.passport.ui

import android.annotation.SuppressLint
import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.AdapterDataObserver
import com.alisasadkovska.passport.R
import com.alisasadkovska.passport.ViewHolder.TopViewHolder
import com.alisasadkovska.passport.common.Common
import com.alisasadkovska.passport.common.Common.database
import com.alisasadkovska.passport.common.TinyDB
import com.alisasadkovska.passport.common.Utils
import com.alisasadkovska.passport.Model.Ranking
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump
import io.github.inflationx.viewpump.ViewPumpContextWrapper
import kotlinx.android.synthetic.main.activity_ranking.*

class RankingActivity : AppCompatActivity() {
    private lateinit var tinyDB: TinyDB
    private var themeId = 0
    private lateinit var ranking: DatabaseReference
    private lateinit var linearLayoutManager: LinearLayoutManager
    private lateinit var adapter: FirebaseRecyclerAdapter<Ranking, TopViewHolder>

    private var activeFilter = biggest

    companion object{
        const val biggest = "biggest"
        const val losers = "losers"
        const val biggestProgress = "biggestProgress"
        const val biggestLosers = "biggestLosers"
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase))
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
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
        setContentView(R.layout.activity_ranking)
        toolbar.title = getString(R.string.menu_ranking)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayShowTitleEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)


        ranking = database.getReference(Common.Top)
        ranking.keepSynced(true)


        linearLayoutManager = LinearLayoutManager(this)
        recycler.layoutManager = linearLayoutManager
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        populateRankingList(Common.TotalScore)
    }


    private fun populateRankingList(filter: String) {
        val query = ranking.orderByChild(filter)
        val options = FirebaseRecyclerOptions.Builder<Ranking>().setQuery(query, Ranking::class.java).build()
        adapter = object : FirebaseRecyclerAdapter<Ranking, TopViewHolder>(options) {
            @SuppressLint("SetTextI18n")
            override fun onBindViewHolder(topViewHolder: TopViewHolder, i: Int, ranking: Ranking) {
                Picasso.get()
                        .load(ranking.cover)
                        .into(topViewHolder.coverImg, object : Callback {
                            override fun onSuccess() {
                                topViewHolder.coverProgress.visibility = View.GONE
                            }

                            override fun onError(e: Exception) {
                                topViewHolder.coverProgress.visibility = View.GONE
                                Picasso.get().load(R.drawable.ic_terrain_black_24dp).into(topViewHolder.coverImg)
                            }
                        })
                if (ranking.progress != null) {
                    when {
                        ranking.progress > 0 -> {
                            topViewHolder.txtProgress.text = getString(R.string.monthlyProgress) + " +" + ranking.progress
                            topViewHolder.txtProgress.setTextColor(resources.getColor(R.color.visa_free))
                        }
                        ranking.progress < 0 -> {
                            topViewHolder.txtProgress.text = getString(R.string.monthlyProgress) + " " + ranking.progress
                            topViewHolder.txtProgress.setTextColor(resources.getColor(android.R.color.holo_red_dark))
                        }
                        else -> {
                            topViewHolder.txtProgress.text = getString(R.string.monthlyProgress) + " = " + ranking.progress
                            topViewHolder.txtProgress.setTextColor(resources.getColor(android.R.color.holo_blue_dark))
                        }
                    }
                }
                topViewHolder.txtCountryName.text = ranking.name
                topViewHolder.txtVisaRequired.text = ranking.visaRequired.toString()
                topViewHolder.txtTotalScore.text = ranking.totalScore.toString()
                topViewHolder.txtVisaOnArrival.text = ranking.visaOnArrival.toString()
                topViewHolder.progressBar.progress = ranking.visaOnArrival
                topViewHolder.progressBar.secondaryProgress = ranking.totalScore
                topViewHolder.setItemClickListener { view: View?, position: Int, isLongClick: Boolean ->
                    openCountryDetails(ranking.cover, ranking.totalScore, ranking.name
                            , ranking.visaOnArrival, ranking.visaFree, ranking.visaRequired, ranking.eTa, ranking.timestamp)
                }
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopViewHolder {
                val itemView = LayoutInflater.from(parent.context)
                        .inflate(R.layout.top_item, parent, false)
                return TopViewHolder(itemView)
            }
        }
        recycler.adapter = adapter
        recycler.adapter?.notifyDataSetChanged()
        adapter.startListening()
        adapter.registerAdapterDataObserver(object : AdapterDataObserver() {
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
        val inflater = layoutInflater
        val detailsDialogue = inflater.inflate(R.layout.top_item_dialogue, null)
        val coverImg = detailsDialogue.findViewById<ImageView>(R.id.coverImg)
        val progressBar = detailsDialogue.findViewById<ProgressBar>(R.id.progressBar)
        val countryProgress = detailsDialogue.findViewById<ProgressBar>(R.id.countryProgress)
        val textTimestamp = detailsDialogue.findViewById<TextView>(R.id.lastUpdate)
        val totalTxt = detailsDialogue.findViewById<TextView>(R.id.textTotal)
        val requiredTat = detailsDialogue.findViewById<TextView>(R.id.textVisaRequired)
        val visaFreeTxt = detailsDialogue.findViewById<TextView>(R.id.textVisaFree)
        val visaOnArrivalTxt = detailsDialogue.findViewById<TextView>(R.id.textVisaOnArrival)
        val visaEta = detailsDialogue.findViewById<TextView>(R.id.textEVisa)
        Picasso.get().load(cover).into(coverImg, object : Callback {
            override fun onSuccess() {
                progressBar.visibility = View.GONE
            }

            override fun onError(e: Exception) {
                Picasso.get().load(R.drawable.ic_terrain_black_24dp)
                        .fit()
                        .into(coverImg)
                progressBar.visibility = View.GONE
            }
        })
        countryProgress.progress = visaOnArrival
        countryProgress.secondaryProgress = totalScore
        textTimestamp.text = timestamp
        totalTxt.text = totalScore.toString()
        requiredTat.text = visaRequired.toString()
        visaFreeTxt.text = visaFree.toString()
        visaOnArrivalTxt.text = visaOnArrival.toString()
        visaEta.text = eVisa.toString()
        alertDialog.setNegativeButton(getString(R.string.close)) { dialogInterface: DialogInterface, _: Int -> dialogInterface.dismiss() }
        val alert = alertDialog.create()
        alert.setView(detailsDialogue)
        alert.show()
    }
    public override fun onStart() {
        super.onStart()
        adapter.startListening()
    }
    public override fun onStop() {
        adapter.stopListening()
        super.onStop()
    }
    public override fun onResume() {
        super.onResume()
        adapter.startListening()
    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        if (themeId==0)menuInflater.inflate(R.menu.ranking_light_menu, menu)
        else menuInflater.inflate(R.menu.ranking_dark_menu, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->{
                finish()
            }
            R.id.biggest->{
                if (activeFilter == biggest)
                    return false

                linearLayoutManager.reverseLayout = true
                linearLayoutManager.stackFromEnd = true
                populateRankingList(Common.TotalScore)
                activeFilter = biggest
            }
            R.id.losers->{
                if (activeFilter == losers)
                    return false

                linearLayoutManager.reverseLayout = false
                linearLayoutManager.stackFromEnd = false
                populateRankingList(Common.TotalScore)
                activeFilter = losers
            }
            R.id.biggestProgress->{
                if (activeFilter == biggestProgress)
                    return false

                linearLayoutManager.reverseLayout = true
                linearLayoutManager.stackFromEnd = true
                populateRankingList(Common.progress)
                activeFilter = biggestProgress
            }
            R.id.biggestLosers->{
                if (activeFilter == biggestLosers)
                    return false

                linearLayoutManager.reverseLayout = false
                linearLayoutManager.stackFromEnd = false
                populateRankingList(Common.progress)
                activeFilter = biggestLosers
            }
        }

        return super.onOptionsItemSelected(item)
    }
}