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

import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.banner.BannerView;

public class ResultActivity extends Activity {

    private TextView resultTxt;

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

        resultTxt.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ClipData clipData = ClipData.newPlainText("text", result);
                ClipboardManager clipboardManager = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboardManager.setPrimaryClip(clipData);

                Toast.makeText(ResultActivity.this, "Data Copied", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
    }

    private void setAd() {
        // Initialize the HUAWEI Ads SDK.
        HwAds.init(this);

        // Obtain BannerView based on the configuration in layout/ad_fragment.xml.
        BannerView bottomBannerView = findViewById(R.id.hw_banner_view);
        bottomBannerView.setAdId("testw6vs28auh3");
        bottomBannerView.setBannerAdSize(BannerAdSize.BANNER_SIZE_360_57);
        bottomBannerView.setBannerRefresh(30);

        AdParam adParam = new AdParam.Builder().build();
        bottomBannerView.loadAd(adParam);

    }
}
