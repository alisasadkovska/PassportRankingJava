package com.alisasadkovska.passport.ui.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alisasadkovska.passport.Adapter.CompareAdapter;
import com.alisasadkovska.passport.ExcelModel.Cell;
import com.alisasadkovska.passport.ExcelModel.ColTitle;
import com.alisasadkovska.passport.ExcelModel.RowTitle;
import com.alisasadkovska.passport.Model.Ranking;
import com.alisasadkovska.passport.R;
import com.alisasadkovska.passport.common.Common;
import com.alisasadkovska.passport.common.TinyDB;
import com.alisasadkovska.passport.ui.MainActivity;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cn.zhouchaoyuan.excelpanel.ExcelPanel;
import es.dmoral.toasty.Toasty;
import in.galaxyofandroid.spinerdialog.SpinnerDialog;
import io.paperdb.Paper;


public class CompareFragment extends Fragment{

    private Context context;


    public static CompareFragment newInstance(Context context) {
        return new CompareFragment(context);
    }

    private CompareFragment(Context context) {
        this.context = context;
    }

    private FloatingActionMenu floatingActionMenu;

    private final static int PAGE_SIZE = 1;
    private static int ROW_SIZE = Common.countryModel.size();

    private DatabaseReference countries;
    private ArrayList<Long>statusList = new ArrayList<>();
    private String countryName;
    private ArrayList<String> countryList = new ArrayList<>();
    private ArrayList<String> countryFlags = new ArrayList<>();
    private int mobilityScore = 0;
    private String cover;

    private CompareAdapter compareAdapter;

    private SpinnerDialog spinnerDialog;

    private List<RowTitle> rowTitles;
    private List<List<Cell>> cells;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        countryName = Paper.book().read(Common.CountryName);
        if (countryName==null)
            goToMainActivity();

        countryList = Paper.book().read(Common.CountryList);
        countryFlags = Paper.book().read(Common.FlagList);
        mobilityScore = Paper.book().read(Common.MobilityScore);
        cover = Paper.book().read(Common.Cover);

        countries = Common.getDatabase().getReference(Common.Countries);

        if (Paper.book().contains(Common.StatusList)){
            statusList = Paper.book().read(Common.StatusList);
            if (statusList.size()!=Common.countryModel.size())
                getStatusList();
        }else
            getStatusList();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(context, MainActivity.class);
        startActivity(intent);
    }

    private void getStatusList() {
        statusList.clear();
        Query query = countries.orderByKey().equalTo(Paper.book().read(Common.CountryName));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnap: dataSnapshot.getChildren()){
                    if (dataSnapshot.exists()){
                        Map<String,Long> data = (Map)postSnap.getValue();
                        assert data != null;
                        Map<String, Long> treeMap = new TreeMap<>(data);

                        ArrayList<Long>status = new ArrayList<>();

                        for (Map.Entry<String,Long> entry : treeMap.entrySet()){
                            status.add(entry.getValue());
                            Paper.book().write(Common.StatusList, status);
                            statusList.addAll(status);
                        }
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toasty.error(context, databaseError.getMessage(), Toasty.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myFragment = inflater.inflate(R.layout.fragment_compare, container, false);
        ExcelPanel excelPanel = myFragment.findViewById(R.id.compare_container);

        compareAdapter = new CompareAdapter(context);
        excelPanel.setAdapter(compareAdapter);
        initData();
        excelPanel.addOnScrollListener(onScrollListener);

        floatingActionMenu = myFragment.findViewById(R.id.fabMenu);

        FloatingActionButton fabAdd = myFragment.findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(view -> {
            showSpinner();
            spinnerDialog.showSpinerDialog();
        });


        FloatingActionButton fabDelete = myFragment.findViewById(R.id.fabDelete);
        fabDelete.setOnClickListener(view -> clearList());

        return myFragment;
    }

    private void clearList() {
        if (rowTitles.size()>=1){
            rowTitles.clear();
            cells.clear();
            compareAdapter.setAllData(genColTitles(), null, null);
            Toasty.success(context, this.context.getString(R.string.data_removed), Toasty.LENGTH_SHORT).show();
        }else {
            Toasty.warning(context, this.context.getString(R.string.nothing_to_remove), Toasty.LENGTH_SHORT).show();
        }
    }

    private void initData(){
        rowTitles = generateRowData();
        cells = genCellData();
        compareAdapter.setAllData(genColTitles(), rowTitles,  cells);
    }

    private ExcelPanel.OnScrollListener onScrollListener = new ExcelPanel.OnScrollListener() {
        @Override
        public void onScrolled(ExcelPanel excelPanel, int dx, int dy) {
            super.onScrolled(excelPanel, dx, dy);
            if (dy > 0)
                floatingActionMenu.hideMenu(true);
            else if (dy < 0)
                floatingActionMenu.showMenu(true);


            if (dx > 0)
                floatingActionMenu.hideMenu(true);
            else if (dx < 0)
                floatingActionMenu.showMenu(true);


        }
    };

    private void showSpinner(){
        spinnerDialog = new SpinnerDialog(getActivity(), Paper.book().read(Common.CountryList), getString(R.string.select_to_compare), R.style.DialogAnimations_SmileWindow, "Close");

        spinnerDialog.setCancellable(true);
        spinnerDialog.setShowKeyboard(false);

        spinnerDialog.setTitleColor(getResources().getColor(R.color.colorAccent));
        spinnerDialog.setTitleColor(getResources().getColor(R.color.colorPrimaryText));
        spinnerDialog.setCloseColor(getResources().getColor(R.color.visa_required));

        spinnerDialog.bindOnSpinerListener((s, pos) -> {

            String countryNameToCompare = Common.countryModel.get(pos).getName();
            String coverToCompare = Common.countryModel.get(pos).getCover();

            for (int i = 0; i < rowTitles.size(); i++) {
                if (countryNameToCompare.matches(rowTitles.get(i).getCountryName())) {
                    Toasty.warning(context, countryNameToCompare + " " + getString(R.string.country_exists), Toasty.LENGTH_LONG).show();
                    spinnerDialog.closeSpinerDialog();
                    return;
                }
            }

            addNewColumn(countryNameToCompare,coverToCompare);
        });
    }

    private void addNewColumn(String countryNameToCompare, String coverToCompare) {
        DatabaseReference topRanking = Common.getDatabase().getReference(Common.Top);
        topRanking.orderByChild(Common.name).equalTo(countryNameToCompare).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnap : dataSnapshot.getChildren()) {
                    Ranking ranking = postSnap.getValue(Ranking.class);
                    assert ranking != null;
                    Integer totalScore = ranking.getTotalScore();
                    RowTitle rowTitle = new RowTitle();
                    rowTitle.setCountryName(countryNameToCompare);
                    rowTitle.setCover(coverToCompare);
                    rowTitle.setMobilityScore(totalScore);
                    rowTitles.add(rowTitle);

                    DatabaseReference countries = Common.getDatabase().getReference(Common.Countries);
                    Query query = countries.orderByKey().equalTo(countryNameToCompare);

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for (DataSnapshot postSnap : dataSnapshot.getChildren()) {
                                Map<String, Long> data = (Map) postSnap.getValue();
                                assert data != null;
                                Map<String, Long> treeMap = new TreeMap<>(data);

                                ArrayList<Long> status = new ArrayList<>();

                                for (Map.Entry<String, Long> entry : treeMap.entrySet()) {
                                    status.add(entry.getValue());
                                }

                                List<List<Cell>> newCell = updateData(status);

                                for (int i = 0; i < cells.size(); i++) {
                                    newCell.get(i).addAll(0, cells.get(i));
                                }

                                compareAdapter.setAllData(genColTitles(), rowTitles, newCell);
                                cells = newCell;

                                Toasty.success(context, countryNameToCompare + " " + getString(R.string.was_added), Toasty.LENGTH_SHORT).show();

                                floatingActionMenu.hideMenu(true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toasty.error(context, getString(R.string.error_toast) + databaseError.getMessage(), 5).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toasty.error(context, getString(R.string.error_toast) + databaseError.getMessage(), 5).show();
            }
        });
    }


    private List<ColTitle> genColTitles() {
        List<ColTitle> colTitles = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            ColTitle colTitle = new ColTitle();
            colTitle.setName(countryList.get(i));
            colTitle.setImage(countryFlags.get(i));
            colTitles.add(colTitle);
        }
        return colTitles;
    }

    private ArrayList<RowTitle> generateRowData(){
        ArrayList<RowTitle> rowTitles = new ArrayList<>();
        for (int i = 0; i < PAGE_SIZE; i++) {
            RowTitle rowTitle = new RowTitle();
            rowTitle.setCover(cover);
            rowTitle.setCountryName(countryName);
            rowTitle.setMobilityScore(mobilityScore);
            rowTitles.add(rowTitle);
        }
        return rowTitles;
    }



    private List<List<Cell>> genCellData() {
        List<List<Cell>> cells = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            List<Cell> cellList = new ArrayList<>();
            cells.add(cellList);
            Cell cell = new Cell();
            cell.setStatus(Math.toIntExact(statusList.get(i)));
            cellList.add(cell);
        }
        return cells;
    }


    private List<List<Cell>> updateData(ArrayList<Long> status) {
        List<List<Cell>> cells = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            List<Cell> cellList = new ArrayList<>();
            cells.add(cellList);
            Cell cell = new Cell();
            cell.setStatus(Math.toIntExact(status.get(i)));
            cellList.add(cell);
        }
        return cells;
    }


}
