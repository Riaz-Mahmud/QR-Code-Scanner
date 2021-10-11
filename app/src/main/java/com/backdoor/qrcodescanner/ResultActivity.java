package com.backdoor.qrcodescanner;

import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.banner.BannerView;

public class ResultActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        setAd();

        TextView resultTxt = findViewById(R.id.resultTxt);
        ImageView backBtnResult = findViewById(R.id.backBtnResult);


        if (getIntent().hasExtra(MainActivity.SCAN_RESULT)) {
            String result = getIntent().getStringExtra(MainActivity.SCAN_RESULT);
            resultTxt.setText(result);
        }else{
            ResultActivity.super.onBackPressed();
        }

        backBtnResult.setOnClickListener(v -> ResultActivity.super.onBackPressed());

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
