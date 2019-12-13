package com.alisasadkovska.passport.ui.Fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.alisasadkovska.passport.Adapter.AdapterNews;
import com.alisasadkovska.passport.Model.news.Article;
import com.alisasadkovska.passport.Model.news.News;
import com.alisasadkovska.passport.R;
import com.alisasadkovska.passport.common.Utils;
import com.alisasadkovska.passport.newsApi.ApiClient;
import com.alisasadkovska.passport.newsApi.ApiInterface;
import com.alisasadkovska.passport.ui.NewsDetailActivity;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NewsFragment extends Fragment{

    private Context context;
    private RecyclerView recyclerView;
    private List<Article> articles = new ArrayList<>();

    private AdapterNews adapterNews;

    private RelativeLayout errorLayout;
    private ImageView errorImage;
    private TextView errorTitle, errorMessage;
    private Button btnRetry;

    private static final int NUMBER_OF_ADS = 2;

    private AdLoader adLoader;

    private List<Object> mRecyclerViewItems = new ArrayList<>();

    private List<UnifiedNativeAd> mNativeAds = new ArrayList<>();

    private NewsFragment(Context context) {
        this.context = context;
    }

    public static NewsFragment newInstance(Context context)
    {
        return new NewsFragment(context);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View myFragment = inflater.inflate(R.layout.fragment_news, container, false);


        recyclerView = myFragment.findViewById(R.id.recyclerNews);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(context);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setNestedScrollingEnabled(false);

        errorLayout = myFragment.findViewById(R.id.errorLayout);
        errorImage = myFragment.findViewById(R.id.errorImage);
        errorTitle = myFragment.findViewById(R.id.errorTitle);
        errorMessage = myFragment.findViewById(R.id.errorMessage);
        btnRetry = myFragment.findViewById(R.id.btnRetry);

        LoadJson(getString(R.string.cheap_flights));

        return myFragment;
    }

    private void loadNativeAds() {

        AdLoader.Builder builder = new AdLoader.Builder(context, getString(R.string.NAL_admob_news_item));
        adLoader = builder.forUnifiedNativeAd(
                unifiedNativeAd -> {
                    mNativeAds.add(unifiedNativeAd);
                    if (!adLoader.isLoading()) {
                        insertAdsInNewsItems();
                    }
                }).withAdListener(
                new AdListener() {
                    @Override
                    public void onAdFailedToLoad(int errorCode) {
                        if (!adLoader.isLoading()) {
                            insertAdsInNewsItems();
                        }
                    }
                }).build();

        adLoader.loadAds(new AdRequest.Builder().build(), NUMBER_OF_ADS);
    }

    private void insertAdsInNewsItems() {
        if (mNativeAds.size() <= 0) {
            return;
        }

        int offset = (mRecyclerViewItems.size() / mNativeAds.size()) + 1;
        int index = 0;
        for (UnifiedNativeAd ad : mNativeAds) {
            mRecyclerViewItems.add(index, ad);
            index = index + offset;
        }
    }



    private void LoadJson(String keyword) {
        errorLayout.setVisibility(View.GONE);
        ApiInterface apiInterface = ApiClient.getApiClient().create(ApiInterface.class);

        String language = Utils.getLanguage();

        Call<News>call;
        call = apiInterface.getNewsSearch(keyword, language, "publishedAt", getString(R.string.NewsApiKey));

        call.enqueue(new Callback<News>() {
            @Override
            public void onResponse(@NonNull Call<News> call, @NonNull Response<News> response) {
                assert response.body() != null;
                if (response.isSuccessful() && response.body().getArticle()!=null){
                    if (!articles.isEmpty())
                        articles.clear();

                    articles = response.body().getArticle();
                    mRecyclerViewItems.addAll(articles);
                    loadNativeAds();
                    adapterNews = new AdapterNews(context, mRecyclerViewItems);
                    recyclerView.setAdapter(adapterNews);
                    adapterNews.notifyDataSetChanged();
                    initListener();
                }else {
                    String errorCode;
                    switch (response.code()){
                        case 404:
                            errorCode = context.getString(R.string.not_found);
                            break;
                        case 500:
                            errorCode = context.getString(R.string.broken_server);
                            break;
                        default:
                            errorCode = context.getString(R.string.unknown_error);
                            break;
                    }

                    showErrorMessage(
                            R.drawable.no_result,
                            context.getString(R.string.no_result),
                            context.getString(R.string.try_again)+
                                    errorCode);
                }
            }

            @Override
            public void onFailure(@NonNull Call<News> call, @NonNull Throwable t) {
                showErrorMessage(
                        R.drawable.oops,
                        context.getString(R.string.oops),
                        context.getString(R.string.network_failure)+
                                t.toString());
            }
        });
    }

    private void showErrorMessage(int imageView, String title, String message) {
        if (errorLayout.getVisibility() == View.GONE) {
            errorLayout.setVisibility(View.VISIBLE);
        }

        errorImage.setImageResource(imageView);
        errorTitle.setText(title);
        errorMessage.setText(message);

        btnRetry.setOnClickListener(v -> LoadJson(getString(R.string.cheap_flights)));
    }

    private void initListener() {
        adapterNews.setOnItemClickListener((view, position) -> {
            Intent intent = new Intent(context, NewsDetailActivity.class);

            Article article = articles.get(position);
            intent.putExtra("url", article.getUrl());
            intent.putExtra("title", article.getTitle());
            intent.putExtra("img",  article.getUrlToImage());
            intent.putExtra("date",  article.getPublishedAt());
            intent.putExtra("source",  article.getSource().getName());
            intent.putExtra("author",  article.getAuthor());

            startActivity(intent);
        });
    }
}
