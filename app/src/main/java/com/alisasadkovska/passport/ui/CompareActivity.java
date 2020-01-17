package com.alisasadkovska.passport.ui;

import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.alisasadkovska.passport.ExcelModel.Cell;
import com.alisasadkovska.passport.ExcelModel.ColTitle;
import com.alisasadkovska.passport.ExcelModel.RowTitle;
import com.alisasadkovska.passport.Model.CountryModel;
import com.alisasadkovska.passport.Model.Ranking;
import com.alisasadkovska.passport.R;
import com.alisasadkovska.passport.adapter.CompareAdapter;
import com.alisasadkovska.passport.common.BaseActivity;
import com.alisasadkovska.passport.common.Common;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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

public class CompareActivity extends BaseActivity {

    private FloatingActionMenu floatingActionMenu;

    private int ROW_SIZE;

    FirebaseDatabase database;
    DatabaseReference countries;

    private ArrayList<CountryModel> country = new ArrayList<>();
    private ArrayList<Long> statusList = new ArrayList<>();
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compare);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle(getString(R.string.menu_compare));
        setSupportActionBar(toolbar);

      if (getSupportActionBar()!=null){
          getSupportActionBar().setDisplayShowTitleEnabled(true);
          getSupportActionBar().setDisplayHomeAsUpEnabled(true);
          getSupportActionBar().setDisplayShowHomeEnabled(true);
      }


        country = Paper.book().read(Common.CountryModel);
        ROW_SIZE = country.size();


        countryName = Paper.book().read(Common.CountryName);
        countryList = Paper.book().read(Common.CountryList);
        statusList = Paper.book().read(Common.StatusList);
        countryFlags = Paper.book().read(Common.FlagList);
        mobilityScore = Paper.book().read(Common.MobilityScore);
        cover = Paper.book().read(Common.Cover);

        database = FirebaseDatabase.getInstance();
        countries = database.getReference(Common.Countries);

        ExcelPanel excelPanel = findViewById(R.id.compare_container);

        compareAdapter = new CompareAdapter(this);
        excelPanel.setAdapter(compareAdapter);
        initData();
        excelPanel.addOnScrollListener(onScrollListener);

        floatingActionMenu = findViewById(R.id.fabMenu);

        FloatingActionButton fabAdd = findViewById(R.id.fabAdd);
        fabAdd.setOnClickListener(view -> {
            showSpinner();
            spinnerDialog.showSpinerDialog();
        });


        FloatingActionButton fabDelete = findViewById(R.id.fabDelete);
        fabDelete.setOnClickListener(view -> clearList());
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
            if (dy > 0){
                floatingActionMenu.hideMenu(true);
            }
            else if (dy < 0){
                floatingActionMenu.showMenu(true);
            }

            if (dx > 0)
                floatingActionMenu.hideMenu(true);
            else if (dx < 0)
                floatingActionMenu.showMenu(true);
        }
    };

    private void showSpinner(){
        spinnerDialog = new SpinnerDialog(this, Paper.book().read(Common.CountryList), getString(R.string.select_to_compare), getString(R.string.close));

        spinnerDialog.setCancellable(false);
        spinnerDialog.setShowKeyboard(false);


        spinnerDialog.setTitleColor(getResources().getColor(R.color.darkColorPrimaryDark));
        spinnerDialog.setCloseColor(getResources().getColor(R.color.visa_required));
        spinnerDialog.setItemColor(getResources().getColor(R.color.visa_on_arrival_table));

        spinnerDialog.bindOnSpinerListener((s, pos) -> {

            String countryNameToCompare = country.get(pos).getName();
            String coverToCompare = country.get(pos).getCover();

            for (int i = 0; i < rowTitles.size(); i++) {
                if (countryNameToCompare.matches(rowTitles.get(i).getCountryName())) {
                    Toasty.warning(this, countryNameToCompare + " " + getString(R.string.country_exists), Toasty.LENGTH_LONG).show();
                    spinnerDialog.closeSpinerDialog();
                    return;
                }
            }

            addNewColumn(countryNameToCompare,coverToCompare);
        });
    }

    private void clearList() {
        if (rowTitles.size()>=1){
            rowTitles.clear();
            cells.clear();
            compareAdapter.setAllData(genColTitles(), null, null);
            Toasty.success(this, getString(R.string.data_removed), Toasty.LENGTH_SHORT).show();
        }else {
            Toasty.warning(this, getString(R.string.nothing_to_remove), Toasty.LENGTH_SHORT).show();
        }
    }
    private void addNewColumn(String countryNameToCompare, String coverToCompare) {
        DatabaseReference topRanking = database.getReference(Common.Top);
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

                    DatabaseReference countries = database.getReference(Common.Countries);
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

                                Toasty.success(getBaseContext(), countryNameToCompare + " " + getString(R.string.was_added), Toasty.LENGTH_SHORT).show();

                                floatingActionMenu.hideMenu(true);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            Toasty.error(getBaseContext(), getString(R.string.error_toast) + databaseError.getMessage(), 5).show();
                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toasty.error(getBaseContext(), getString(R.string.error_toast) + databaseError.getMessage(), 5).show();
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
        for (int i = 0; i < 1; i++) {
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
            Cell cell = new Cell();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cell.setStatus(Math.toIntExact(statusList.get(i)));
            }else {
                long _status = statusList.get(i);
                cell.setStatus((int)_status);
            }

            cellList.add(cell);
            cells.add(cellList);
        }
        return cells;
    }
    private List<List<Cell>> updateData(ArrayList<Long> status) {
        List<List<Cell>> cells = new ArrayList<>();
        for (int i = 0; i < ROW_SIZE; i++) {
            List<Cell> cellList = new ArrayList<>();
            Cell cell = new Cell();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                cell.setStatus(Math.toIntExact(status.get(i)));
            }else{
                long _status = status.get(i);
                cell.setStatus((int)_status);
            }
            cellList.add(cell);
            cells.add(cellList);
        }
        return cells;
    }


    @Override
    public boolean onOptionsItemSelected( MenuItem item) {
        if (item.getItemId()==android.R.id.home)
            finish();
        return super.onOptionsItemSelected(item);
    }
}
