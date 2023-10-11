package com.backdoor.qrcodescanner;

import static com.huawei.hms.mlkit.common.ha.BundleKeyConstants.AppInfo.packageName;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
    final int PERMISSION_REQUEST_CODE = 112;
    final int PERMISSION_REQUEST_CODE_CAMERA = 113;

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

        if (Build.VERSION.SDK_INT > 32) {
            if (!(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED)) {
                getNotificationPermission();
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)) {
                getCameraPermission();
            }
        }

    }

    private void getCameraPermission() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(
                        new String[]{Manifest.permission.CAMERA},
                        113);
            }

        } catch (Exception ignored) {
        }
    }

    public void getNotificationPermission() {
        try {
            if (Build.VERSION.SDK_INT > 32) {
                this.requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        112);
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case PERMISSION_REQUEST_CODE:
                // If request is cancelled, the result arrays are empty.
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // allow
                } else {
                    Toast.makeText(this, "Notification permission denied", Toast.LENGTH_LONG).show();
                }
            case PERMISSION_REQUEST_CODE_CAMERA:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                }

        }

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
            String Sub = "You can Scan any Bar code & QR code with this" + ". " + ". \nhttps://play.google.com/store/apps/details?id=com.backdoor.qrcodescanner";
            intent.putExtra(Intent.EXTRA_SUBJECT, Body);
            intent.putExtra(Intent.EXTRA_TEXT, Sub);
            startActivity(Intent.createChooser(intent, "Share using"));
        });

        bottomSheetDialog.show();
    }

    private void scanning(Bundle savedInstanceState) {
        getCameraPermission();
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