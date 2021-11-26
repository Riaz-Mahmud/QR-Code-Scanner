package com.backdoor.qrcodescanner;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.huawei.hms.hmsscankit.RemoteView;
import com.huawei.hms.ml.scan.HmsScan;

public class MainActivity extends AppCompatActivity {

    //declare RemoteView instance
    private RemoteView remoteView;
    //declare the key ,used to get the value returned from scanKit
    public static final String SCAN_RESULT = "scanResult";

    int mScreenWidth;
    int mScreenHeight;
    //scan_view_finder width & height is  300dp
    final int SCAN_FRAME_SIZE = 300;

    private AdView mAdView;
    int click = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);

        init();

        scanning(savedInstanceState);
        setAd();

    }

    private void init() {
        ImageView menu_img = findViewById(R.id.menu_img);
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
        //1.get screen density to calculate viewfinder's rect
        DisplayMetrics dm = getResources().getDisplayMetrics();
        float density = dm.density;
        //2.get screen size
        mScreenWidth = getResources().getDisplayMetrics().widthPixels;
        mScreenHeight = getResources().getDisplayMetrics().heightPixels;
        int scanFrameSize = (int) (SCAN_FRAME_SIZE * density);
        //3.calculate viewfinder's rect,it's in the middle of the layout
        //set scanning area(Optional, rect can be null,If not configure,default is in the center of layout)
        Rect rect = new Rect();
        rect.left = mScreenWidth / 2 - scanFrameSize / 2;
        rect.right = mScreenWidth / 2 + scanFrameSize / 2;
        rect.top = mScreenHeight / 2 - scanFrameSize / 2;
        rect.bottom = mScreenHeight / 2 + scanFrameSize / 2;

        //initialize RemoteView instance, and set calling back for scanning result
        remoteView = new RemoteView.Builder().setContext(this).setBoundingBox(rect).setFormat(HmsScan.ALL_SCAN_TYPE).build();
        remoteView.onCreate(savedInstanceState);
        remoteView.setOnResultCallback(result -> {
            //judge the result is effective
            if (result != null && result.length > 0 && result[0] != null && !TextUtils.isEmpty(result[0].getOriginalValue())) {

                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra(SCAN_RESULT, result[0].getOriginalValue());
                setResult(RESULT_OK, intent);
                startActivity(intent);

            }
        });

        //add remoteView to frameLayout
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        FrameLayout frameLayout = findViewById(R.id.rim);
        frameLayout.addView(remoteView, params);

        //set back button listener
        ImageView backBtn = findViewById(R.id.back_img);
        backBtn.setOnClickListener(v -> MainActivity.this.finish());
    }

    private void setAd() {

        if (checkConnection()) {

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

    public boolean checkConnection() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        assert manager != null;
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        if (activeNetwork == null) {
            return false;
        } else {
            return true;
        }
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