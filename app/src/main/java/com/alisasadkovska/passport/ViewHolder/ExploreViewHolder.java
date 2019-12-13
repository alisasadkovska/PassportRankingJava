package com.alisasadkovska.passport.ViewHolder;

import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.alisasadkovska.passport.Interface.ItemClickListener;
import com.alisasadkovska.passport.R;

public class ExploreViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

    private ItemClickListener itemClickListener;

    public ImageView coverImg;
    public ProgressBar progressBar;


    public ExploreViewHolder(@NonNull View itemView) {
        super(itemView);
        coverImg = itemView.findViewById(R.id.coverImg);
        progressBar = itemView.findViewById(R.id.progressBar);

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
