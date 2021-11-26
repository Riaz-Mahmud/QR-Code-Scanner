package com.backdoor.qrcodescanner;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;


public class ResultActivity extends Activity {

    private TextView resultTxt;

    private AdView mAdView;
    int click = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        setAd();

        resultTxt = findViewById(R.id.resultTxt);
        ImageView backBtnResult = findViewById(R.id.backBtnResult);


        if (getIntent().hasExtra(MainActivity.SCAN_RESULT)) {
            String result = getIntent().getStringExtra(MainActivity.SCAN_RESULT);
            resultTxt.setText(result);
            Linkify.addLinks(resultTxt, Linkify.ALL);

            textCopy(result);
        } else {
            ResultActivity.super.onBackPressed();
        }

        backBtnResult.setOnClickListener(v -> ResultActivity.super.onBackPressed());

    }

    private void textCopy(String result) {

        resultTxt.setOnLongClickListener(v -> {
            ClipData clipData = ClipData.newPlainText("text", result);
            ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            clipboardManager.setPrimaryClip(clipData);

            Toast.makeText(ResultActivity.this, "Data Copied", Toast.LENGTH_SHORT).show();
            return true;
        });
    }

    private void setAd() {
        MobileAds.initialize(this, initializationStatus -> {
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        mAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                super.onAdLoaded();
            }

            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError adError) {
                // Code to be executed when an ad request fails.
                super.onAdFailedToLoad(adError);
                mAdView.loadAd(adRequest);
            }

            @Override
            public void onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                super.onAdOpened();
            }

            @Override
            public void onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                super.onAdClicked();

                click++;
                if (click > 3) {
                    mAdView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onAdClosed() {
                // Code to be executed when the user is about to return
                // to the app after tapping on an ad.
            }
        });

    }
}
