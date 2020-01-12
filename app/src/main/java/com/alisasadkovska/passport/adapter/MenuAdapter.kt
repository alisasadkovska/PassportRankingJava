package com.alisasadkovska.passport.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.alisasadkovska.passport.R
import com.alisasadkovska.passport.menu.Menu
import com.alisasadkovska.passport.ui.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.menu_item.view.*

class MenuAdapter(val context: Context, private val menuList: List<Menu>):RecyclerView.Adapter<MenuAdapter.MenuViewHolder>() {

    companion object{
        const val COMPARE = 0
        const val MAPS = 1
        const val RANKING = 2
        const val EXPLORE = 3
        const val INFO = 4
    }

    class MenuViewHolder(val view: View):RecyclerView.ViewHolder(view)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuViewHolder {
        return MenuViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.menu_item,parent,false))
    }

    override fun getItemCount(): Int {
        return menuList.size
    }

    override fun onBindViewHolder(holder: MenuViewHolder, position: Int) {
      holder.view.menuTitle.text = menuList[position].title
      Picasso.get().load(menuList[position].logo).into(holder.view.menuLogo)
        holder.view.setOnClickListener{
           when(position){
               COMPARE->{
                   val intent = Intent(context, CompareActivity::class.java)
                   context.startActivity(intent)
               }
               MAPS->{
                   val intent = Intent(context, MapsActivity::class.java)
                   context.startActivity(intent)
               }
               RANKING->{
                   val intent = Intent(context, RankingActivity::class.java)
                   context.startActivity(intent)
               }
               EXPLORE->{
                   val intent = Intent(context, ExploreActivity::class.java)
                   context.startActivity(intent)
               }
               INFO->{
                   val intent = Intent(context, InfoActivity::class.java)
                   context.startActivity(intent)
               }
           }
        }
    }
}