package com.alisasadkovska.passport.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.alisasadkovska.passport.Model.news.Article;
import com.alisasadkovska.passport.R;
import com.alisasadkovska.passport.ViewHolder.UnifiedNativeAdViewHolder;
import com.alisasadkovska.passport.common.Utils;
import com.google.android.gms.ads.formats.NativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.formats.UnifiedNativeAdView;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import es.dmoral.toasty.Toasty;

public class AdapterNews extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // A menu item view type.
    private static final int MENU_ITEM_VIEW_TYPE = 0;

    private static final int UNIFIED_NATIVE_AD_VIEW_TYPE = 1;

    private AdapterNews.OnItemClickListener onItemClickListener;

    private Context context;
    private List<Object> mRecyclerViewItems;


    public AdapterNews(Context context, List<Object> mRecyclerViewItems) {
        this.context = context;
        this.mRecyclerViewItems = mRecyclerViewItems;
    }

    public void setOnItemClickListener(AdapterNews.OnItemClickListener onItemClickListener){
        this.onItemClickListener = onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        TextView textAuthor, textTitle, textDescription, textPublished_at, textSource, textTime;
        ImageView newsCoverImage;
        ProgressBar progressBar;
        AdapterNews.OnItemClickListener onItemClickListener;

        MyViewHolder(@NonNull View itemView, AdapterNews.OnItemClickListener onItemClickListener) {
            super(itemView);

            itemView.setOnClickListener(this);
            textAuthor = itemView.findViewById(R.id.textAuthor);
            textTitle = itemView.findViewById(R.id.textTitle);
            textDescription = itemView.findViewById(R.id.textDescription);
            textPublished_at = itemView.findViewById(R.id.publishedAt);
            textSource = itemView.findViewById(R.id.textSource);
            textTime = itemView.findViewById(R.id.textTime);

            newsCoverImage = itemView.findViewById(R.id.newsCoverImage);
            progressBar = itemView.findViewById(R.id.progressBar);

            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public void onClick(View v) {
            onItemClickListener.onItemClick(v, getAdapterPosition());
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        switch (viewType){
            case UNIFIED_NATIVE_AD_VIEW_TYPE:
                View unifiedNativeLayoutView = LayoutInflater.from(
                        parent.getContext()).inflate(R.layout.ad_unified,
                        parent, false);
                return new UnifiedNativeAdViewHolder(unifiedNativeLayoutView);
            case MENU_ITEM_VIEW_TYPE:
                // Fall through.
            default:
                View view = LayoutInflater.from(context).inflate(R.layout.news_item, parent, false);
                return new MyViewHolder(view, onItemClickListener);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holders, int position) {
        int viewType = getItemViewType(position);
        switch (viewType){
            case UNIFIED_NATIVE_AD_VIEW_TYPE:
                UnifiedNativeAd nativeAd = (UnifiedNativeAd) mRecyclerViewItems.get(position);
                populateNativeAdView(nativeAd, ((UnifiedNativeAdViewHolder) holders).getAdView());
                break;
            case MENU_ITEM_VIEW_TYPE:
                // fall through
            default:
                MyViewHolder holder = (MyViewHolder) holders;
                Article article = (Article) mRecyclerViewItems.get(position);

                Picasso.get().load(article.getUrlToImage())
                        .into(holder.newsCoverImage, new Callback() {
                            @Override
                            public void onSuccess() {
                                holder.progressBar.setVisibility(View.GONE);
                            }

                            @Override
                            public void onError(Exception e) {
                                Toasty.error(context, Objects.requireNonNull(e.getMessage()), Toasty.LENGTH_SHORT).show();
                            }
                        });

                holder.textAuthor.setText(article.getAuthor());
                holder.textTitle.setText(article.getTitle());
                holder.textSource.setText(article.getSource().getName());
                holder.textTime.setText(" \u2022 " + Utils.DateToTimeFormat(article.getPublishedAt()));
                holder.textPublished_at.setText(Utils.DateFormat(article.getPublishedAt()));
        }
    }

    @Override
    public int getItemCount() {
        return mRecyclerViewItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        Object recyclerViewItem = mRecyclerViewItems.get(position);
        if (recyclerViewItem instanceof UnifiedNativeAd){
            return UNIFIED_NATIVE_AD_VIEW_TYPE;
        }
        return super.getItemViewType(position);
    }




    private void populateNativeAdView(UnifiedNativeAd nativeAd,
                                      UnifiedNativeAdView adView) {

        ((TextView) adView.getHeadlineView()).setText(nativeAd.getHeadline());
        ((TextView) adView.getBodyView()).setText(nativeAd.getBody());
        ((Button) adView.getCallToActionView()).setText(nativeAd.getCallToAction());

        NativeAd.Image icon = nativeAd.getIcon();

        if (icon == null) {
            adView.getIconView().setVisibility(View.INVISIBLE);
        } else {
            ((ImageView) adView.getIconView()).setImageDrawable(icon.getDrawable());
            adView.getIconView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getPrice() == null) {
            adView.getPriceView().setVisibility(View.INVISIBLE);
        } else {
            adView.getPriceView().setVisibility(View.VISIBLE);
            ((TextView) adView.getPriceView()).setText(nativeAd.getPrice());
        }

        if (nativeAd.getStore() == null) {
            adView.getStoreView().setVisibility(View.INVISIBLE);
        } else {
            adView.getStoreView().setVisibility(View.VISIBLE);
            ((TextView) adView.getStoreView()).setText(nativeAd.getStore());
        }

        if (nativeAd.getStarRating() == null) {
            adView.getStarRatingView().setVisibility(View.INVISIBLE);
        } else {
            ((RatingBar) adView.getStarRatingView())
                    .setRating(nativeAd.getStarRating().floatValue());
            adView.getStarRatingView().setVisibility(View.VISIBLE);
        }

        if (nativeAd.getAdvertiser() == null) {
            adView.getAdvertiserView().setVisibility(View.INVISIBLE);
        } else {
            ((TextView) adView.getAdvertiserView()).setText(nativeAd.getAdvertiser());
            adView.getAdvertiserView().setVisibility(View.VISIBLE);
        }

        // Assign native ad object to the native view.
        adView.setNativeAd(nativeAd);
    }
}
