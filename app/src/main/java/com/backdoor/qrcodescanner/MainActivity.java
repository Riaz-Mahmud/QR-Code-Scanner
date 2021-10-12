package com.backdoor.qrcodescanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.huawei.hianalytics.scankit.HiAnalyticsTools;
import com.huawei.hms.ads.AdParam;
import com.huawei.hms.ads.BannerAdSize;
import com.huawei.hms.ads.HwAds;
import com.huawei.hms.ads.banner.BannerView;
import com.huawei.hms.analytics.HiAnalytics;
import com.huawei.hms.analytics.HiAnalyticsInstance;
import com.huawei.hms.hmsscankit.OnResultCallback;
import com.huawei.hms.hmsscankit.RemoteView;
import com.huawei.hms.ml.scan.HmsScan;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    //declare RemoteView instance
    private RemoteView remoteView;
    //declare the key ,used to get the value returned from scankit
    public static final String SCAN_RESULT = "scanResult";

    int mScreenWidth;
    int mScreenHeight;
    //scan_view_finder width & height is  300dp
    final int SCAN_FRAME_SIZE = 300;

    private ImageView menu_img;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        init();

        // Enable SDK log recording.
        HiAnalyticsTools.enableLog();
        HiAnalyticsInstance instance = HiAnalytics.getInstance(this);
        // Or initialize Analytics Kit with the given context.
        Context context = this.getApplicationContext();
        HiAnalyticsInstance analyticsInstance = HiAnalytics.getInstance(context);
        analyticsInstance.setUserProfile("notify_user", "notify_user_value");

        scanning(savedInstanceState);
        setAd();

    }

    private void init() {
        menu_img = findViewById(R.id.menu_img);

        menu_img.setOnClickListener(v -> showBottomMenu());

    }

    private void showBottomMenu() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(MainActivity.this, R.style.bottomSheetDialogTheme);
        bottomSheetDialog.setContentView(R.layout.item_bottom_menu);
        bottomSheetDialog.setCanceledOnTouchOutside(true);

        Button makeQrCodeBtn = bottomSheetDialog.findViewById(R.id.makeQrCodeBtnMenu);
        Button scanFormImage = bottomSheetDialog.findViewById(R.id.scanFromImageBtnMenu);
        Button shareBtn = bottomSheetDialog.findViewById(R.id.shareBtnMenu);

        assert makeQrCodeBtn != null;
        makeQrCodeBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MakeQRCodeActivity.class)));

        assert scanFormImage != null;
        scanFormImage.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, BitmapActivity.class)));

        assert shareBtn != null;
        shareBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            String Body = "Download QR Code Scanner App";
            String Sub = "You can Scan any Bar code & QR code with this" + ". " + ". \nhttps://appgallery.huawei.com/#/app/C104816483";
            intent.putExtra(Intent.EXTRA_SUBJECT, Body);
            intent.putExtra(Intent.EXTRA_TEXT, Sub);
            startActivity(Intent.createChooser(intent, "Share using"));
        });

        bottomSheetDialog.show();
    }

    private void scanning(Bundle savedInstanceState) {
        //1.get screen density to caculate viewfinder's rect
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float density = dm.density;
        //2.get screen size
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        int scanFrameSize = (int) (SCAN_FRAME_SIZE * density);
        //3.caculate viewfinder's rect,it's in the middle of the layout
        //set scanning area(Optional, rect can be null,If not configure,default is in the center of layout)
        Rect rect = new Rect();
        rect.left = mScreenWidth / 2 - scanFrameSize / 2;
        rect.right = mScreenWidth / 2 + scanFrameSize / 2;
        rect.top = mScreenHeight / 2 - scanFrameSize / 2;
        rect.bottom = mScreenHeight / 2 + scanFrameSize / 2;

        //initialize RemoteView instance, and set calling back for scanning result
        remoteView = new RemoteView.Builder().setContext(this).setBoundingBox(rect).setFormat(HmsScan.ALL_SCAN_TYPE).build();
        remoteView.onCreate(savedInstanceState);
        remoteView.setOnResultCallback(new OnResultCallback() {
            @Override
            public void onResult(HmsScan[] result) {
                //judge the result is effective
                if (result != null && result.length > 0 && result[0] != null && !TextUtils.isEmpty(result[0].getOriginalValue())) {

                    Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                    intent.putExtra(SCAN_RESULT, result[0].getOriginalValue());
                    setResult(RESULT_OK, intent);
                    startActivity(intent);

                }
            }
        });

        //add remoteView to framelayout
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = findViewById(R.id.rim);
        frameLayout.addView(remoteView, params);

        //set back button listener
        ImageView backBtn = findViewById(R.id.back_img);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.this.finish();
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

    //manage remoteView lifecycle
    @Override
    protected void onStart() {
        super.onStart();
        remoteView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        remoteView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        remoteView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        remoteView.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        remoteView.onStop();
    }

}