package com.alisasadkovska.passport.ui.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.alisasadkovska.passport.common.Common;
import com.alisasadkovska.passport.Model.Ranking;
import com.alisasadkovska.passport.Model.SpinnerModel;
import com.alisasadkovska.passport.R;
import com.alisasadkovska.passport.ViewHolder.TopViewHolder;
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
public class RankingFragment extends Fragment {

    private DatabaseReference ranking;
    private Context context;

    private ArrayList<SpinnerModel> spinnerModels;
    private ArrayAdapter<SpinnerModel> spinnerModelArrayAdapter;

    private RankingFragment(Context context) {
        this.context = context;
        ranking = Common.getDatabase().getReference(Common.Top);
        ranking.keepSynced(true);
    }

    public static RankingFragment newInstance(Context context)
    {
        return new RankingFragment(context);
    }

    private RecyclerView recyclerView;
    private FirebaseRecyclerAdapter<Ranking, TopViewHolder> adapter;
    private ProgressBar loadingInfoBar;

    public RankingFragment() { }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View myFragment = inflater.inflate(R.layout.fragment_ranking, container, false);

        assignSpinnerModel();

        loadingInfoBar = myFragment.findViewById(R.id.loading_recycler);
        recyclerView = myFragment.findViewById(R.id.recycler);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);

        recyclerView.setLayoutManager(linearLayoutManager);

        AppCompatSpinner spinner = myFragment.findViewById(R.id.action_bar_spinner);
        spinner.setAdapter(spinnerModelArrayAdapter);
        spinner.setSelection(1);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
              switch (position){
                  case 1:
                      linearLayoutManager.setReverseLayout(true);
                      linearLayoutManager.setStackFromEnd(true);
                      populateRankingList(Common.TotalScore);
                      break;
                  case 2:
                      linearLayoutManager.setReverseLayout(false);
                      linearLayoutManager.setStackFromEnd(false);
                      populateRankingList(Common.TotalScore);
                      break;
                  case 4:
                      linearLayoutManager.setReverseLayout(true);
                      linearLayoutManager.setStackFromEnd(true);
                      populateRankingList(Common.progress);
                      break;
                  case 5:
                      linearLayoutManager.setReverseLayout(false);
                      linearLayoutManager.setStackFromEnd(false);
                      populateRankingList(Common.progress);
                      break;
              }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        
        return myFragment;
    }

    private void assignSpinnerModel() {
        spinnerModels = new ArrayList<>();

        spinnerModels.add(new SpinnerModel(true, R.drawable.blank_square, getString(R.string.menu_ranking)));
        spinnerModels.add(new SpinnerModel(false, R.drawable.graph_up, getString(R.string.order_by_biggest)));
        spinnerModels.add(new SpinnerModel(false, R.drawable.graph_down, getString(R.string.order_by_losers)));
        spinnerModels.add(new SpinnerModel(true, R.drawable.blank_square, getString(R.string.progress)));
        spinnerModels.add(new SpinnerModel(false, R.drawable.trophy, getString(R.string.order_by_biggest_progress)));
        spinnerModels.add(new SpinnerModel(false, R.drawable.flame, getString(R.string.order_by_biggest_lose)));

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

    private void populateRankingList(String filter) {
        Query query = ranking.orderByChild(filter);

        FirebaseRecyclerOptions<Ranking> options = new FirebaseRecyclerOptions.Builder<Ranking>()
                .setQuery(query, Ranking.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Ranking, TopViewHolder>(options) {
            @SuppressLint("SetTextI18n")
            @Override
            protected void onBindViewHolder(@NonNull TopViewHolder topViewHolder, int i, @NonNull Ranking ranking) {
                Picasso.get()
                        .load(ranking.getCover())
                        .into(topViewHolder.coverImg, new Callback() {
                            @Override
                            public void onSuccess() {
                                topViewHolder.coverProgress.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {
                                topViewHolder.coverProgress.setVisibility(View.GONE);
                                Picasso.get().load(R.drawable.ic_terrain_black_24dp).into(topViewHolder.coverImg);
                            }
                        });

                if (ranking.getProgress()!=null){
                    if (ranking.getProgress()>0){
                        topViewHolder.txtProgress.setText(getString(R.string.monthlyProgress) + " +" + ranking.getProgress());
                        topViewHolder.txtProgress.setTextColor(getResources().getColor(R.color.visa_free));
                    }
                    else if (ranking.getProgress()<0){
                        topViewHolder.txtProgress.setText(getString(R.string.monthlyProgress) + " " + ranking.getProgress());
                        topViewHolder.txtProgress.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    }
                    else{
                        topViewHolder.txtProgress.setText(getString(R.string.monthlyProgress) + " = " + ranking.getProgress());
                        topViewHolder.txtProgress.setTextColor(getResources().getColor(android.R.color.holo_blue_dark));
                    }
                }

                topViewHolder.txtCountryName.setText(ranking.getName());
                topViewHolder.txtVisaRequired.setText(String.valueOf(ranking.getVisaRequired()));
                topViewHolder.txtTotalScore.setText(String.valueOf(ranking.getTotalScore()));
                topViewHolder.txtVisaOnArrival.setText(String.valueOf(ranking.getVisaOnArrival()));
                topViewHolder.progressBar.setProgress(ranking.getVisaOnArrival());
                topViewHolder.progressBar.setSecondaryProgress(ranking.getTotalScore());
                topViewHolder.setItemClickListener((view, position, isLongClick) -> openCountryDetails(ranking.getCover(), ranking.getTotalScore(), ranking.getName()
                        , ranking.getVisaOnArrival(),ranking.getVisaFree(), ranking.getVisaRequired(), ranking.getETa(), ranking.getTimestamp()));
            }

            @NonNull
            @Override
            public TopViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.top_item, parent, false);
                return new TopViewHolder(itemView);
            }
        };

        recyclerView.setAdapter(adapter);
        Objects.requireNonNull(recyclerView.getAdapter()).notifyDataSetChanged();
        adapter.startListening();

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                if (itemCount!=0)
                    loadingInfoBar.setVisibility(View.GONE);
            }
        });
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

        TextView textTimestamp = details_dialogue.findViewById(R.id.lastUpdate);

        TextView totalTxt = details_dialogue.findViewById(R.id.textTotal);
        TextView requiredTat = details_dialogue.findViewById(R.id.textVisaRequired);
        TextView visaFreeTxt = details_dialogue.findViewById(R.id.textVisaFree);
        TextView visaOnArrivalTxt = details_dialogue.findViewById(R.id.textVisaOnArrival);
        TextView visaEta = details_dialogue.findViewById(R.id.textEVisa);

        Picasso.get().load(cover).into(coverImg, new Callback(){
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

        textTimestamp.setText(timestamp);

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
        if(adapter!=null)
            adapter.startListening();
    }

    @Override
    public void onStop() {
        if(adapter!=null)
            adapter.stopListening();
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();  
        if(adapter!=null)
            adapter.startListening();
        
    }
}
