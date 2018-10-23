package com.jzt.camera;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.tencent.smtt.export.external.interfaces.GeolocationPermissionsCallback;
import com.tencent.smtt.export.external.interfaces.SslError;
import com.tencent.smtt.export.external.interfaces.SslErrorHandler;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.os.Build.VERSION_CODES.M;

// 1111111111111
public class MainActivity1 extends AppCompatActivity {
    private WebView webview;
    private ValueCallback<Uri[]> mFilePathCallback;
    private ValueCallback<Uri> nFilePathCallback;
    private  String mCameraPhotoPath;
    public static final int INPUT_FILE_REQUEST_CODE = 1;
    public static final int INPUT_VIDEO_CODE = 2;
    private Uri photoURI;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        webview= (WebView) findViewById(R.id.wb);
        initView();
    }

    private void initView() {
        WebSettings settings = webview.getSettings();
        settings.setLoadWithOverviewMode(true);
        settings.setBuiltInZoomControls(true);
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setSupportZoom(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);

        settings.setGeolocationEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setDatabaseEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(true);
        //webview
        settings.setJavaScriptEnabled(true);
        //启用数据库
        settings.setDatabaseEnabled(true);
        //设置定位的数据库路径
        String dir = this.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setGeolocationDatabasePath(dir);
        //启用地理定位
        settings.setGeolocationEnabled(true);
        //开启DomStorage缓存
        settings.setDomStorageEnabled(true);

        webview.setWebChromeClient(new MyWebChromeClient());
        webview.setWebViewClient(new MyWebViewClient());
        webview.loadUrl("mUrl");
    }


    private class MyWebChromeClient extends WebChromeClient {
        @Override
        public void onReceivedIcon(WebView view, Bitmap icon) {
            super.onReceivedIcon(view, icon);
        }
        @Override
        public void onGeolocationPermissionsShowPrompt(String origin,GeolocationPermissionsCallback callback) {
//            UIUtils.showToast("origin==="+origin);
            callback.invoke(origin, true, false);
            super.onGeolocationPermissionsShowPrompt(origin, callback);
        }


        @SuppressLint("NewApi")
        @Override
        public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback,
                                         WebChromeClient.FileChooserParams fileChooserParams) {
            if (mFilePathCallback != null) { mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePathCallback;
            String[] acceptTypes = fileChooserParams.getAcceptTypes();

            if (acceptTypes[0].equals("image/*")) { Intent takePictureIntent = new
                    Intent(MediaStore.ACTION_IMAGE_CAPTURE); if
                    (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                File photoFile = null; try {
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath",mCameraPhotoPath);

                } catch (IOException ex) {
//                    Log.e("TAG", "Unable to create Image File", ex);

                }
                //¾ 7.0
                if(Build.VERSION.SDK_INT > M) {
                    if (photoFile != null) {
                        photoURI = FileProvider.getUriForFile(MainActivity1.this, BuildConfig.APPLICATION_ID+".fileprovider", photoFile);
                        takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                    }
                }else{
                    if (photoFile != null) { mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }
            }
                startActivityForResult(takePictureIntent, INPUT_FILE_REQUEST_CODE);
            } else if (acceptTypes[0].equals("video/*")) { Intent takeVideoIntent = new
                    Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if(takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takeVideoIntent, INPUT_VIDEO_CODE);
                }
            }
            return true;

        }
        /**
         * for android  4.0+
         * @param uploadMsg
         * @param acceptType
         * @param capture
         */
        @Override
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
//            mUploadMessage=uploadMsg;
//            take();

            if (nFilePathCallback != null) {
                nFilePathCallback.onReceiveValue(null);
            }
            nFilePathCallback = uploadMsg;
            if ("image/*".equals(acceptType)) {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    File photoFile = null;
                    try {
                        photoFile = createImageFile();
                        takePictureIntent.putExtra("PhotoPath",mCameraPhotoPath);

                    } catch (IOException ex) {
//                    Log.e("TAG", "Unable to create Image File", ex);
                        ex.fillInStackTrace();
                    }
                    if (photoFile != null) {
                        mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                    } else {
                        takePictureIntent = null;
                    }
                }
                startActivityForResult(takePictureIntent, INPUT_FILE_REQUEST_CODE);
            } else if ("video/*".equals(acceptType)) {
                Intent takeVideoIntent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
                if (takeVideoIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takeVideoIntent, INPUT_VIDEO_CODE);
                }
            }

        }

    }

    private File createImageFile()throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.CHINA).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );
        mCameraPhotoPath = image.getAbsolutePath();
        return image;
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != INPUT_FILE_REQUEST_CODE && requestCode != INPUT_VIDEO_CODE) {
            super.onActivityResult(requestCode, resultCode, data); return;
        }
        Uri[] results = null; Uri mUri = null;
        if (resultCode == Activity.RESULT_OK && requestCode == INPUT_FILE_REQUEST_CODE) {
            if (data == null) {
                if(Build.VERSION.SDK_INT > M) {
                    mUri=photoURI;
                    results=new Uri[]{mUri};
                }else{
                    if (mCameraPhotoPath != null) {
                        mUri = Uri.parse(mCameraPhotoPath);
                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                }
            } else {
                Uri nUri = data.getData(); if (nUri != null) {
                    mUri =nUri;
                    results = new Uri[]{nUri};
                }
            }
        } else if (resultCode == Activity.RESULT_OK && requestCode == INPUT_VIDEO_CODE) {
            mUri = data.getData();
            results = new Uri[]{mUri};
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            nFilePathCallback.onReceiveValue(mUri);
            nFilePathCallback = null;
            return;
        } else {
            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
            return;
        }
    }

    private class MyWebViewClient extends WebViewClient {
        @Override
        public void onReceivedSslError(WebView webView, SslErrorHandler sslErrorHandler, SslError sslError) {
            sslErrorHandler.proceed();
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (url.contains("javasctip:")){
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            view.getSettings().setJavaScriptEnabled(true);
            super.onPageStarted(view, url, favicon);

        }

        @Override
        public void onPageFinished(WebView view, String url) {
            view.getSettings().setJavaScriptEnabled(true);
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
        }
    }

}
