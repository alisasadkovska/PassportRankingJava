package com.alisasadkovska.passport.ui.Fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alisasadkovska.passport.Adapter.PassportAdapter;
import com.alisasadkovska.passport.common.Common;
import com.alisasadkovska.passport.Model.Country;
import com.alisasadkovska.passport.Model.SpinnerModel;
import com.alisasadkovska.passport.R;
import com.alisasadkovska.passport.ui.MainActivity;
import com.google.android.material.appbar.AppBarLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;
import java.util.stream.Collectors;

import es.dmoral.toasty.Toasty;
import io.paperdb.Paper;

public class HomeFragment extends Fragment{

    private Context context;


    private HomeFragment(Context context) {
        this.context = context;
    }

    public static HomeFragment newInstance(Context context)
    {
        return new HomeFragment(context);
    }

    private DatabaseReference countries;

    private ArrayList<SpinnerModel> spinnerModels;
    private ArrayAdapter<SpinnerModel> spinnerModelArrayAdapter;
    private AppCompatSpinner spinner;

    private ArrayList<String> countryFlags = new ArrayList<>();

    private ArrayList<Country> visaFreeList = new ArrayList<>();
    private ArrayList<Country> visaRequiredList = new ArrayList<>();
    private ArrayList<Country> visaEtaList = new ArrayList<>();
    private ArrayList<Country> visaOnArrivalList = new ArrayList<>();

    private RecyclerView recyclerView;
    private PassportAdapter passportAdapter;
    private LinearLayout headLayout;
    private ProgressBar progressBar,countryProgress;

    private TextView txtTotalScore;
    private TextView txtVisaFree;
    private TextView txtVisaOnArrival;
    private TextView txtEta;
    private TextView txtVisaRequired;

    private String countryName;
    private String passportCover;

    public HomeFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        countries = Common.getDatabase().getReference(Common.Countries);
        countries.keepSynced(true);
        setHasOptionsMenu(true);

        countryName = Paper.book().read(Common.CountryName);

        if (countryName==null)
            startMainActivity();

        passportCover = Paper.book().read(Common.Cover);
        countryFlags = Paper.book().read(Common.FlagList);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myFragment = inflater.inflate(R.layout.fragment_home, container, false);

        countryProgress = myFragment.findViewById(R.id.countryProgress);

        progressBar = myFragment.findViewById(R.id.progressBar);
        headLayout = myFragment.findViewById(R.id.headLayout);

        txtTotalScore = myFragment.findViewById(R.id.textTotal);
        txtVisaOnArrival = myFragment.findViewById(R.id.textVisaOnArrival);
        txtEta = myFragment.findViewById(R.id.textEVisa);
        txtVisaFree = myFragment.findViewById(R.id.textVisaFree);
        txtVisaRequired = myFragment.findViewById(R.id.textVisaRequired);

        TextView name = myFragment.findViewById(R.id.name);
        if (countryName!=null)
        name.setText(countryName);

        ProgressBar coverProgress = myFragment.findViewById(R.id.coverProgress);
        ImageView cover = myFragment.findViewById(R.id.passportCover);

        Picasso.get()
                .load(passportCover)
                .into(cover, new Callback() {
                    @Override
                    public void onSuccess() {
                        coverProgress.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(Exception e) {
                        coverProgress.setVisibility(View.GONE);
                        Picasso.get().load(R.drawable.ic_terrain_black_24dp)
                                .into(cover);
                    }
                });

        cover.setOnClickListener(view -> scaleCoverImage(passportCover, countryName));

        AppBarLayout appBarLayout = myFragment.findViewById(R.id.appbar);
        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.BaseOnOffsetChangedListener() {

            boolean isShow = false;
            int scrollRange = -1;
            Toolbar toolbar = Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {


                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true;
                    toolbar.setTitle(countryName);
                } else if (isShow) {
                    isShow = false;
                    toolbar.setTitle(getString(R.string.menu_home));
                }
            }
        });

        recyclerView = myFragment.findViewById(R.id.recycler);
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        assignSpinnerModel();

        spinner = myFragment.findViewById(R.id.action_bar_spinner);
        spinner.setAdapter(spinnerModelArrayAdapter);
        spinner.setSelection(0);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        loadRecyclerViewData(countryName);
                        break;
                    case 1:
                        passportAdapter = new PassportAdapter(context, visaFreeList, countryFlags);
                        recyclerView.setAdapter(passportAdapter);
                        break;
                    case 2:
                        passportAdapter = new PassportAdapter(context, visaOnArrivalList, countryFlags);
                        recyclerView.setAdapter(passportAdapter);
                        break;
                    case 3:
                        passportAdapter = new PassportAdapter(context, visaEtaList, countryFlags);
                        recyclerView.setAdapter(passportAdapter);
                        break;
                    case 4:
                        passportAdapter = new PassportAdapter(context, visaRequiredList, countryFlags);
                        recyclerView.setAdapter(passportAdapter);
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        loadRecyclerViewData(countryName);

        return myFragment;
    }

    private void startMainActivity() {
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
    }

    private void assignSpinnerModel() {
        spinnerModels = new ArrayList<>();

        spinnerModels.add(new SpinnerModel(false, R.mipmap.ic_launcher_round, getString(R.string.all_countries)));
        spinnerModels.add(new SpinnerModel(false, R.drawable.green_square, getString(R.string.visa_free)));
        spinnerModels.add(new SpinnerModel(false, R.drawable.blue_square, getString(R.string.on_arrival)));
        spinnerModels.add(new SpinnerModel(false, R.drawable.yellow_square, getString(R.string.eTA)));
        spinnerModels.add(new SpinnerModel(false, R.drawable.red_square, getString(R.string.visa_required)));

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

    private void scaleCoverImage(String coverPath, String countryName){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setCancelable(true);
        alertDialog.setTitle(countryName);

        LayoutInflater inflater = this.getLayoutInflater();
        @SuppressLint("InflateParams") View scale_dialogue = inflater.inflate(R.layout.passport_item, null);

        ImageView coverScaled = scale_dialogue.findViewById(R.id.coverScale);

        Picasso.get()
                .load(coverPath)
                .error(R.drawable.ic_terrain_black_24dp)
                .placeholder(R.drawable.progress_animation)
                .into(coverScaled);

        alertDialog.setNegativeButton(getString(R.string.close), (dialogInterface, i) -> dialogInterface.dismiss());

        final AlertDialog alert = alertDialog.create();
        alert.setView(scale_dialogue);

        alert.show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        MenuItem mSearch = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView)mSearch.getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                passportAdapter.getFilter().filter(s);
                return false;
            }
        });
    }

    private ArrayList<Country> getCountries(String homeCountry){
        final ArrayList<Country>countryList = new ArrayList<>();
        Query query = countries.orderByKey().equalTo(homeCountry);

        query.addListenerForSingleValueEvent(new ValueEventListener(){
            @SuppressLint("NewApi")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnap: dataSnapshot.getChildren()){
                    if (dataSnapshot.exists()){
                        Map<String,Long> data = (Map)postSnap.getValue();
                        assert data != null;
                        Map<String, Long> treeMap = new TreeMap<>(data);

                        ArrayList<Long>status = new ArrayList<>();

                        for (Map.Entry<String,Long> entry : treeMap.entrySet()){
                            countryList.addAll(Collections.singleton(new Country(entry.getKey(), entry.getValue())));

                            status.add(entry.getValue());

                            visaFreeList = (ArrayList<Country>) countryList.stream().filter(country -> country.getVisaStatus()==3).collect(Collectors.toList());
                            visaOnArrivalList = (ArrayList<Country>) countryList.stream().filter(country -> country.getVisaStatus()==2).collect(Collectors.toList());
                            visaEtaList = (ArrayList<Country>) countryList.stream().filter(country -> country.getVisaStatus()==1).collect(Collectors.toList());
                            visaRequiredList = (ArrayList<Country>) countryList.stream().filter(country -> country.getVisaStatus()==0).collect(Collectors.toList());


                            int visa_free = Collections.frequency(status, (long) 3);
                            int visa_onArrival = Collections.frequency(status, (long) 2);
                            int visa_eta = Collections.frequency(status, (long) 1);
                            int visa_required = Collections.frequency(status, (long) 0);
                            int total = visa_free+visa_onArrival+visa_eta;


                            txtTotalScore.setText(String.valueOf(total));
                            txtVisaFree.setText(String.valueOf(visa_free));
                            txtEta.setText(String.valueOf(visa_eta));
                            txtVisaOnArrival.setText(String.valueOf(visa_onArrival));
                            txtVisaRequired.setText(String.valueOf(visa_required));

                            countryProgress.setProgress(visa_onArrival);
                            countryProgress.setSecondaryProgress(total);

                            Paper.book().write(Common.MobilityScore, total);
                            Paper.book().write(Common.StatusList, status);


                            passportAdapter.notifyDataSetChanged();
                            progressBar.setVisibility(View.GONE);
                            headLayout.setVisibility(View.VISIBLE);
                            spinner.setVisibility(View.VISIBLE);
                        }
                    }
                    }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toasty.error(context, getString(R.string.error_toast) + databaseError.getMessage(),5).show();
                progressBar.setVisibility(View.GONE);
            }
        });
        return countryList;
    }

    private void loadRecyclerViewData(String countryName) {
        passportAdapter = new PassportAdapter(context, getCountries(countryName), countryFlags);
        recyclerView.setAdapter(passportAdapter);
    }


}