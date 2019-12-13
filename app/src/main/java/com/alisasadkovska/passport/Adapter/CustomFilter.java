package com.alisasadkovska.passport.Adapter;

import android.widget.Filter;
import com.alisasadkovska.passport.Model.Country;
import java.util.AbstractList;
import java.util.ArrayList;

public class CustomFilter extends Filter {

    private PassportAdapter passportAdapter;
    private AbstractList<Country> filteredList;

     CustomFilter(PassportAdapter passportAdapter, AbstractList<Country> filteredList) {
        this.passportAdapter = passportAdapter;
        this.filteredList = filteredList;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint){
        FilterResults results = new FilterResults();
        if (constraint!=null&&constraint.length()>0){

            constraint=constraint.toString().toUpperCase();
            ArrayList<Country> filteredCountries = new ArrayList<>();

            for (int i=0;i<filteredList.size();i++){
                if (filteredList.get(i).getCountryName().toUpperCase().contains(constraint)){
                    filteredCountries.add(filteredList.get(i));
                }
            }
            results.count = filteredCountries.size();
            results.values = filteredCountries;
        }else {
            results.count = filteredList.size();
            results.values=filteredList;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence charSequence, FilterResults filterResults){
        passportAdapter.mCountryList = (ArrayList<Country>)filterResults.values;
        passportAdapter.notifyDataSetChanged();
    }
}
