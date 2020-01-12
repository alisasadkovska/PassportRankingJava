package com.alisasadkovska.passport.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.alisasadkovska.passport.R
import com.alisasadkovska.passport.common.Common
import com.alisasadkovska.passport.common.Common.database
import com.alisasadkovska.passport.Model.Country
import com.alisasadkovska.passport.ui.CountryDetail
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import es.dmoral.toasty.Toasty
import io.paperdb.Paper
import kotlin.collections.ArrayList

class PassportAdapter(private val context: Context, var mCountryList: MutableList<Country>, flagList: ArrayList<String>) : RecyclerView.Adapter<PassportAdapter.ViewHolder>(), Filterable {
    private val flagList: ArrayList<String>
    private var mFilteredList: MutableList<Country> = ArrayList()


    init {
        mFilteredList = mCountryList
        this.flagList = flagList
    }

    override fun onCreateViewHolder(parent: ViewGroup, i: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.country_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, pos: Int) {
        viewHolder.countryName.text = mFilteredList[pos].countryName
        when (mFilteredList[pos].visaStatus) {
            0L -> {
                viewHolder.countryStatus.setText(R.string.visa_required)
                viewHolder.countryStatus.setTextColor(context.resources.getColor(R.color.visa_required))
            }
            1L -> {
                viewHolder.countryStatus.setText(R.string.eTA)
                viewHolder.countryStatus.setTextColor(context.resources.getColor(R.color.eTa))
            }
            2L -> {
                viewHolder.countryStatus.setText(R.string.on_arrival)
                viewHolder.countryStatus.setTextColor(context.resources.getColor(R.color.visa_on_arrival))
            }
            3L -> {
                viewHolder.countryStatus.setText(R.string.visa_free)
                viewHolder.countryStatus.setTextColor(context.resources.getColor(R.color.visa_free))
            }
            -1L -> {
                viewHolder.countryStatus.text = null
            }
        }
        if (mCountryList === mFilteredList && mCountryList.size == flagList.size) {
            Picasso.get()
                    .load(flagList[pos])
                    .error(R.drawable.ic_terrain_black_24dp)
                    .placeholder(R.drawable.progress_animation)
                    .into(viewHolder.countryFlag)
        } else {
                val countryModel = database.getReference(Common.Country_Model)
                countryModel.orderByChild(Common.Name).equalTo(mFilteredList[pos].countryName)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                for (postSnap in dataSnapshot.children) {
                                    val flag = postSnap.child(Common.Flag).value as String?
                                    Picasso.get()
                                            .load(flag)
                                            .error(R.drawable.ic_terrain_black_24dp)
                                            .placeholder(R.drawable.progress_animation)
                                            .into(viewHolder.countryFlag)
                                }
                            }

                            override fun onCancelled(databaseError: DatabaseError) {
                                Toasty.error(context, context.getString(R.string.error_toast) + databaseError.message, 5).show()
                            }
                        })
        }
        viewHolder.card.setOnClickListener {
            if (Paper.book().read<String>(Common.CountryName) == mFilteredList[pos].countryName) return@setOnClickListener

            val intent = Intent(context, CountryDetail::class.java)
            Common.COUNTRY = mFilteredList[pos].countryName.toString()
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            context.startActivity(intent)

        }
    }

    override fun getItemCount(): Int {
        return mFilteredList.size
    }

    override fun getFilter(): Filter {
        return object : Filter(){
            @SuppressLint("DefaultLocale")
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint.toString()
                var filteredList:MutableList<Country> = ArrayList()

                if (query.isEmpty())
                    filteredList = mCountryList
                else{
                    for (country in mCountryList)
                        if (country.countryName?.toLowerCase()?.contains(query.toLowerCase())!!)
                            filteredList.add(country)
                    }
                    val result = FilterResults()
                    result.count = filteredList.size
                    result.values = filteredList
                    return result
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                mFilteredList = results!!.values as MutableList<Country>
                notifyDataSetChanged()
            }
        }
    }



    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val countryName: TextView = itemView.findViewById(R.id.countryName)
        val countryStatus: TextView = itemView.findViewById(R.id.visa_status)
        val countryFlag: ImageView = itemView.findViewById(R.id.fabFlag)
        val card: LinearLayout = itemView.findViewById(R.id.header)

    }


}