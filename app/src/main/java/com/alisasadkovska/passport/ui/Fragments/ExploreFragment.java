package com.alisasadkovska.passport.ui.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alisasadkovska.passport.common.Common;
import com.alisasadkovska.passport.Model.Ranking;
import com.alisasadkovska.passport.ViewHolder.ExploreViewHolder;
import com.alisasadkovska.passport.Model.SpinnerModel;
import com.alisasadkovska.passport.R;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class ExploreFragment extends Fragment {

    private DatabaseReference ranking;
    private Context context;

    private ArrayList<SpinnerModel> spinnerModels;
    private ArrayAdapter<SpinnerModel> spinnerModelArrayAdapter;

    private ExploreFragment(Context context) {
        this.context = context;
        ranking = Common.getDatabase().getReference(Common.Top);
        ranking.keepSynced(true);
    }

    public static ExploreFragment newInstance(Context context)
    {
        return new ExploreFragment(context);
    }

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Ranking, ExploreViewHolder> filteredAdapter;
    private ProgressBar loadingInfoBar;

    public ExploreFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View myFragment = inflater.inflate(R.layout.fragment_explore, container, false);

        assignSpinnerModel();

        loadingInfoBar = myFragment.findViewById(R.id.loading_recycler);
        recyclerView = myFragment.findViewById(R.id.recycler);

        int screenSize = getResources().getConfiguration().screenLayout &
                Configuration.SCREENLAYOUT_SIZE_MASK;

        GridLayoutManager gridLayoutManager = null;

        switch (screenSize) {
            case Configuration.SCREENLAYOUT_SIZE_XLARGE:
                 gridLayoutManager = new GridLayoutManager(context,6);
                break;
            case Configuration.SCREENLAYOUT_SIZE_UNDEFINED:

                break;
            case Configuration.SCREENLAYOUT_SIZE_LARGE:
                gridLayoutManager = new GridLayoutManager(context,4);
                break;
            case Configuration.SCREENLAYOUT_SIZE_SMALL:
                gridLayoutManager = new GridLayoutManager(context,2);
                break;

            case Configuration.SCREENLAYOUT_SIZE_NORMAL:
            default:
                 gridLayoutManager = new GridLayoutManager(context,3);

        }
        recyclerView.setLayoutManager(gridLayoutManager);


        AppCompatSpinner spinner = myFragment.findViewById(R.id.action_bar_spinner);
        spinner.setAdapter(spinnerModelArrayAdapter);
        spinner.setSelection(1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                getCategoryName(spinnerModels.get(i).getName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return myFragment;
    }

    private void getCategoryName(String name) {
        String category;

        if (name.equals(getString(R.string.residence)) || name.equals(getString(R.string.citizenship))){
            category = Common.Industry;
        }
        else if (name.equals(getString(R.string.black)) || name.equals(getString(R.string.blue)) || name.equals(getString(R.string.green)) || name.equals(getString(R.string.red)))
            category = Common.Color;
        else
            category = Common.Continent;

            loadingInfoBar.setVisibility(View.VISIBLE);
            populateFilteredAdapter(category, name);

    }


    private void populateFilteredAdapter(String category, String name) {

        Query query = ranking.orderByChild(category).equalTo(name);

        FirebaseRecyclerOptions<Ranking> options = new FirebaseRecyclerOptions.Builder<Ranking>()
                .setQuery(query, Ranking.class)
                .build();

        filteredAdapter = new FirebaseRecyclerAdapter<Ranking, ExploreViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ExploreViewHolder exploreViewHolder, final int position, @NonNull Ranking ranking) {

                Picasso.get()
                        .load(ranking.getCover())
                        .into(exploreViewHolder.coverImg, new Callback() {
                            @Override
                            public void onSuccess() {
                                exploreViewHolder.progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {

                            }
                        });

                exploreViewHolder.setItemClickListener((view, position1, isLongClick) ->
                        openCountryDetails(ranking.getCover(), ranking.getTotalScore(), ranking.getName()
                                , ranking.getVisaOnArrival(),ranking.getVisaFree(), ranking.getVisaRequired(), ranking.getETa(), ranking.getTimestamp()));
            }

            @NonNull
            @Override
            public Ranking getItem(int pos) {
                return super.getItem(getItemCount() - 1 - pos);
            }

            @NonNull
            @Override
            public ExploreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.top_item_filtered, parent, false);
                return new ExploreViewHolder(itemView);
            }
        };


        recyclerView.setAdapter(filteredAdapter);
        Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
        filteredAdapter.startListening();

        filteredAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (itemCount!=0)
                    loadingInfoBar.setVisibility(View.GONE);
            }
        });
    }

    private void assignSpinnerModel() {
        spinnerModels = new ArrayList<>();

        spinnerModels.add(new SpinnerModel(true, R.drawable.blank_square, getString(R.string.continent)));
        spinnerModels.add(new SpinnerModel(false, R.drawable.asia, getString(R.string.asia)));
        spinnerModels.add(new SpinnerModel(false, R.drawable.africa, getString(R.string.africa)));
        spinnerModels.add(new SpinnerModel(false,  R.drawable.europe, getString(R.string.europe)));
        spinnerModels.add(new SpinnerModel(false,  R.drawable.north_america, getString(R.string.north_america)));
        spinnerModels.add(new SpinnerModel(false,  R.drawable.south_america, getString(R.string.south_america)));
        spinnerModels.add(new SpinnerModel(false,  R.drawable.oceania, getString(R.string.oceania)));

        spinnerModels.add(new SpinnerModel(true,  R.drawable.blank_square, getString(R.string.color)));
        spinnerModels.add(new SpinnerModel(false,R.drawable.black_square, getString(R.string.black)));
        spinnerModels.add(new SpinnerModel(false,R.drawable.blue_square, getString(R.string.blue)));
        spinnerModels.add(new SpinnerModel(false,R.drawable.green_square, getString(R.string.green)));
        spinnerModels.add(new SpinnerModel(false,R.drawable.red_square, getString(R.string.red)));

        spinnerModels.add(new SpinnerModel(true, R.drawable.blank_square, getString(R.string.industry)));
        spinnerModels.add(new SpinnerModel(false,R.drawable.citizen, getString(R.string.citizenship)));
        spinnerModels.add(new SpinnerModel(false,R.drawable.residency, getString(R.string.residence)));

        spinnerModelArrayAdapter = new ArrayAdapter<SpinnerModel>(context, android.R.layout.simple_spinner_dropdown_item, spinnerModels){

            @Override
            public boolean isEnabled(int position) {
                return !spinnerModels.get(position).isHeader();
            }

            @Override
            public boolean areAllItemsEnabled() {
                return false;
            }

            @SuppressLint("InflateParams")
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = convertView;
                if (view==null){
                    Context mContext = context;
                    LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    assert layoutInflater != null;
                    view = layoutInflater.inflate(R.layout.spinner_header, null);
                }

                TextView spinner_header_txt = view.findViewById(R.id.spinner_header_txt);
                SpinnerModel model = spinnerModels.get(position);
                spinner_header_txt.setText(model.getName());
                return view;
            }

            @SuppressLint("InflateParams")
            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = convertView;
                if (view == null){
                    Context mContext = this.getContext();
                    LayoutInflater layoutInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    assert layoutInflater != null;
                    view = layoutInflater.inflate(R.layout.spinner_item, null);
                }

                TextView spinner_item_txt = view.findViewById(R.id.spinner_name_txt);
                ImageView spinner_item_logo = view.findViewById(R.id.spinner_item_logo);

                SpinnerModel model = spinnerModels.get(position);
                spinner_item_txt.setText(model.getName());

                if (model.getDrawable()>0){
                    spinner_item_logo.setImageDrawable(ResourcesCompat.getDrawable(getResources(), model.getDrawable(), null));
                }

                return view;
            }


        };
    }

    private void openCountryDetails(String cover, Integer totalScore, String name, Integer visaOnArrival, Integer visaFree, Integer visaRequired, Integer eVisa, String timestamp) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setCancelable(true);
        alertDialog.setTitle(name);

        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams") View details_dialogue = inflater.inflate(R.layout.top_item_dialogue, null);
        ImageView coverImg = details_dialogue.findViewById(R.id.coverImg);
        ProgressBar progressBar = details_dialogue.findViewById(R.id.progressBar);

        ProgressBar countryProgress = details_dialogue.findViewById(R.id.countryProgress);
        TextView totalTxt = details_dialogue.findViewById(R.id.textTotal);
        TextView requiredTat = details_dialogue.findViewById(R.id.textVisaRequired);
        TextView visaFreeTxt = details_dialogue.findViewById(R.id.textVisaFree);
        TextView visaOnArrivalTxt = details_dialogue.findViewById(R.id.textVisaOnArrival);
        TextView visaEta = details_dialogue.findViewById(R.id.textEVisa);
        TextView timeStamp = details_dialogue.findViewById(R.id.lastUpdate);

        Picasso.get().load(cover).into(coverImg, new Callback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onError(Exception e) {
                Picasso.get().load(R.drawable.ic_terrain_black_24dp)
                        .fit()
                        .into(coverImg);
                progressBar.setVisibility(View.GONE);
            }
        });

        countryProgress.setProgress(visaOnArrival);
        countryProgress.setSecondaryProgress(totalScore);

        timeStamp.setText(timestamp);

        totalTxt.setText(String.valueOf(totalScore));
        requiredTat.setText(String.valueOf(visaRequired));
        visaFreeTxt.setText(String.valueOf(visaFree));
        visaOnArrivalTxt.setText(String.valueOf(visaOnArrival));
        visaEta.setText(String.valueOf(eVisa));

        alertDialog.setNegativeButton(getString(R.string.close), (dialogInterface, i
        ) -> dialogInterface.dismiss());

        final AlertDialog alert = alertDialog.create();
        alert.setView(details_dialogue);

        alert.show();
    }

    @Override
    public void onStart() {
        super.onStart();
       if(filteredAdapter!=null)
            filteredAdapter.startListening();
    }

    @Override
    public void onStop() {
       if(filteredAdapter!=null)
            filteredAdapter.stopListening();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        if(filteredAdapter!=null)
            filteredAdapter.startListening();
    }

}
