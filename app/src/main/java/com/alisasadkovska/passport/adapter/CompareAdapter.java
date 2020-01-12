package com.alisasadkovska.passport.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.alisasadkovska.passport.ExcelModel.Cell;
import com.alisasadkovska.passport.ExcelModel.ColTitle;
import com.alisasadkovska.passport.ExcelModel.RowTitle;
import com.alisasadkovska.passport.R;
import com.squareup.picasso.Picasso;

import cn.zhouchaoyuan.excelpanel.BaseExcelPanelAdapter;

public class CompareAdapter extends BaseExcelPanelAdapter<RowTitle, ColTitle, Cell>{

    private Context context;

    public CompareAdapter(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public RecyclerView.ViewHolder onCreateCellViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.country_status_normal_cell, parent, false);
        return new CellHolder(layout);
    }

    @Override
    public void onBindCellViewHolder(RecyclerView.ViewHolder holder, int verticalPosition, int horizontalPosition){
        Cell cell = getMajorItem(verticalPosition, horizontalPosition);
        CellHolder viewHolder = (CellHolder)holder;

        switch (cell.getStatus()){
            case -1:
                viewHolder.countryStatusTxt.setText("-");
                viewHolder.backgroundColor.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray));
                break;
            case 0:
               viewHolder.countryStatusTxt.setText(R.string.visa_required);
               viewHolder.backgroundColor.setBackgroundColor(ContextCompat.getColor(context, R.color.visa_required_table));
            break;
            case 1:
                viewHolder.countryStatusTxt.setText(R.string.eTA);
                viewHolder.backgroundColor.setBackgroundColor(ContextCompat.getColor(context, R.color.eTa_table));
                break;
            case 2:
                viewHolder.countryStatusTxt.setText(R.string.on_arrival);
                viewHolder.backgroundColor.setBackgroundColor(ContextCompat.getColor(context, R.color.visa_on_arrival_table));
                break;
            case 3:
                viewHolder.countryStatusTxt.setText(R.string.visa_free);
                viewHolder.backgroundColor.setBackgroundColor(ContextCompat.getColor(context, R.color.visa_free_table));
                break;
        }
    }

    static class CellHolder extends RecyclerView.ViewHolder{

        final TextView countryStatusTxt;
        final RelativeLayout backgroundColor;

        CellHolder(@NonNull View itemView) {
            super(itemView);
            countryStatusTxt = itemView.findViewById(R.id.visaStatusTxt);
            backgroundColor = itemView.findViewById(R.id.background);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateTopViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.country_top_cell, parent, false);
        return new TopHolder(layout);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindTopViewHolder(RecyclerView.ViewHolder holder, int position) {
        RowTitle rowTitle = getTopItem(position);

        TopHolder viewHolder = (TopHolder)holder;

        Picasso.get()
                .load(rowTitle.getCover())
                .error(R.drawable.ic_terrain_black_24dp)
                .placeholder(R.drawable.progress_animation)
                .into(viewHolder.passportCover);

        viewHolder.countryName.setText(rowTitle.getCountryName());

        if (viewHolder.countryName.getText().equals("Saint Vincent and the Grenadines"))
            viewHolder.countryName.setTextSize(12);

        viewHolder.mobilityScore.setText(this.context.getString(R.string.score) + rowTitle.getMobilityScore());
    }

    static class TopHolder extends RecyclerView.ViewHolder{

        final TextView countryName;
        final TextView mobilityScore;
        final ImageView passportCover;

        TopHolder(@NonNull View itemView) {
            super(itemView);
            countryName = itemView.findViewById(R.id.countryNameTxt);
            mobilityScore = itemView.findViewById(R.id.mobilityScoreTxt);
            passportCover = itemView.findViewById(R.id.passportCoverImg);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateLeftViewHolder(ViewGroup parent, int viewType) {
        View layout = LayoutInflater.from(parent.getContext()).inflate(R.layout.country_left_cell, parent, false);
        return new LeftHolder(layout);
    }

    @Override
    public void onBindLeftViewHolder(RecyclerView.ViewHolder holder, int position) {
        ColTitle colTitle = getLeftItem(position);

        LeftHolder viewHolder = (LeftHolder)holder;
        viewHolder.countryName.setText(colTitle.getName());

        Picasso.get()
                .load(colTitle.getImage())
                .error(R.drawable.ic_terrain_black_24dp)
                .placeholder(R.drawable.progress_animation)
                .into(viewHolder.countryFlag);
    }

    static class LeftHolder extends RecyclerView.ViewHolder{

        final TextView countryName;
        final ImageView countryFlag;

        LeftHolder(@NonNull View itemView) {
            super(itemView);
            countryName = itemView.findViewById(R.id.countryNameTxt);
            countryFlag = itemView.findViewById(R.id.flagImg);
        }
    }


    @Override
    public View onCreateTopLeftView() {
        @SuppressLint("InflateParams") View progressView = LayoutInflater.from(context).inflate(R.layout.country_normal_cell,null);
        ProgressBar progressBar = progressView.findViewById(R.id.progress_circular);
        return progressView;
    }
}

