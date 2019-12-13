package com.alisasadkovska.passport.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alisasadkovska.passport.Interface.ItemClickListener;
import com.alisasadkovska.passport.R;

public class TopViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    private ItemClickListener itemClickListener;

    public ImageView coverImg;
    public TextView txtCountryName, txtTotalScore, txtVisaOnArrival, txtVisaRequired, txtProgress;
    public ProgressBar progressBar, coverProgress;

    public TopViewHolder(@NonNull View itemView) {
        super(itemView);
        coverImg = itemView.findViewById(R.id.coverImg);
        txtCountryName = itemView.findViewById(R.id.countryNameTxt);
        txtTotalScore = itemView.findViewById(R.id.totalScoreTxt);
        txtVisaOnArrival = itemView.findViewById(R.id.visaOnArrivalTxt);
        txtVisaRequired = itemView.findViewById(R.id.visaRequiredTxt);
        progressBar = itemView.findViewById(R.id.countryProgress);
        coverProgress = itemView.findViewById(R.id.coverProgress);
        txtProgress = itemView.findViewById(R.id.textProgress);

        itemView.setOnClickListener(this);
    }

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }


    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }
}
