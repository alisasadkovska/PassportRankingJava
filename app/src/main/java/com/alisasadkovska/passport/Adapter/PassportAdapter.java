package com.alisasadkovska.passport.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.alisasadkovska.passport.common.Common;
import com.alisasadkovska.passport.common.TinyDB;
import com.alisasadkovska.passport.Model.Country;
import com.alisasadkovska.passport.R;
import com.alisasadkovska.passport.ui.CountryDetail;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import es.dmoral.toasty.Toasty;
import io.paperdb.Paper;

public class PassportAdapter extends RecyclerView.Adapter<PassportAdapter.ViewHolder>
implements Filterable {

     private Context context;
     private ArrayList<String>flagList;
     ArrayList<Country> mCountryList;
     private ArrayList<Country>mFilteredList;
     private CustomFilter filter;

    public PassportAdapter(Context context, ArrayList<Country> mCountryList, ArrayList<String>flagList) {
        this.context = context;
        this.mCountryList = mCountryList;
        this.mFilteredList = mCountryList;
        this.flagList = flagList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.country_item,parent,false);
        return new ViewHolder(view);
    }


    @SuppressLint({"ResourceAsColor", "CheckResult"})
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, final int pos) {
        viewHolder.countryName.setText(mCountryList.get(pos).getCountryName());

        if (mCountryList.get(pos).getVisaStatus()==0){
            viewHolder.countryStatus.setText(R.string.visa_required);
            viewHolder.countryStatus.setTextColor(context.getColor(R.color.visa_required));
        }
        else if (mCountryList.get(pos).getVisaStatus()==1){
            viewHolder.countryStatus.setText(R.string.eTA);
            viewHolder.countryStatus.setTextColor(context.getColor(R.color.eTa));
        }
        else if (mCountryList.get(pos).getVisaStatus()==2){
            viewHolder.countryStatus.setText(R.string.on_arrival);
            viewHolder.countryStatus.setTextColor(context.getColor(R.color.visa_on_arrival));
        }
        else if (mCountryList.get(pos).getVisaStatus()==3){
            viewHolder.countryStatus.setText(R.string.visa_free);
            viewHolder.countryStatus.setTextColor(context.getColor(R.color.visa_free));
        }
        else if (mCountryList.get(pos).getVisaStatus()==-1){
            viewHolder.countryStatus.setText(null);
        }

            if (mCountryList==mFilteredList && mCountryList.size() == Common.countryModel.size()){
                Picasso.get()
                        .load(flagList.get(pos))
                        .error(R.drawable.ic_terrain_black_24dp)
                        .placeholder(R.drawable.progress_animation)
                        .into(viewHolder.countryFlag);
            }else {
                DatabaseReference country_model = Common.getDatabase().getReference(Common.Country_Model);
                country_model.orderByChild(Common.Name).equalTo(mCountryList.get(pos).getCountryName())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot postSnap: dataSnapshot.getChildren()){
                                    String flag = (String) postSnap.child(Common.Flag).getValue();

                                    Picasso.get()
                                            .load(flag)
                                            .error(R.drawable.ic_terrain_black_24dp)
                                            .placeholder(R.drawable.progress_animation)
                                            .into(viewHolder.countryFlag);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Toasty.error(context, context.getString(R.string.error_toast) + databaseError.getMessage(),5).show();
                            }
                        });
            }

        viewHolder.card.setOnClickListener(v -> {
            if (Paper.book().read(Common.CountryName).equals(mCountryList.get(pos).getCountryName()))
                return;

            Common.COUNTRY = mCountryList.get(pos).getCountryName();
            v.getContext().startActivity(new Intent(context.getApplicationContext(), CountryDetail.class));
        });
    }

    @Override
    public int getItemCount() {
        return mCountryList.size();
    }

    @Override
    public Filter getFilter() {
        if (filter==null){
            filter = new CustomFilter(this, mCountryList);
        }
        return filter;
    }

    class ViewHolder extends RecyclerView.ViewHolder{
        private TextView countryName, countryStatus;
        private ImageView countryFlag;
        private LinearLayout card;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            countryName = itemView.findViewById(R.id.countryName);
            countryStatus = itemView.findViewById(R.id.visa_status);
            countryFlag = itemView.findViewById(R.id.flag);
            card = itemView.findViewById(R.id.header);
        }
    }
}
