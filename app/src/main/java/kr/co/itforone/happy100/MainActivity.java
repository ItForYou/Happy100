package kr.co.itforone.happy100;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.ValueCallback;
import android.webkit.WebBackForwardList;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.common.internal.service.Common;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.File;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import kr.co.itforone.happy100.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding biding;

    public WebView webView;
    public SwipeRefreshLayout refreshLayout = null;
    public CookieManager cookieManager;

    private long backPrssedTime = 0;
    public int flg_refresh = 1;

    final int FILECHOOSER_NORMAL_REQ_CODE = 1200, FILECHOOSER_LOLLIPOP_REQ_CODE = 1300;
    public ValueCallback<Uri> filePathCallbackNormal;
    public ValueCallback<Uri[]> filePathCallbackLollipop;
    public Uri mCapturedImageURI;
    public String loadUrl = "";

    public static String TOKEN = ""; // 푸시토큰

    //public ProgressBar pBar;    // 로딩바
    public ImageView imgGif;

    public SharedPreferences preferences;  // 로그인데이터저장
    public SharedPreferences.Editor pEditor;

    private ActivityManager am = ActivityManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 데이터바인딩
        biding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        biding.setMainData(this);

        am.addActivity(this);

        webView = biding.webview;
        refreshLayout = biding.refreshlayout;
        //pBar = biding.pBar;
        //pBar.setVisibility(View.GONE);  // 로딩바 가림

        imgGif = biding.imgGif; // 로딩이미지
        Glide.with(this).asGif()    // GIF 로딩
                .load( R.raw.loading_spin )
                .override(200, 200)
                .diskCacheStrategy( DiskCacheStrategy.RESOURCE )    // Glide에서 캐싱한 리소스와 로드할 리소스가 같을때 캐싱된 리소스 사용
                .into( imgGif );

        setTOKEN(this);

        // 쿠키매니저
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.createInstance(this);
        }
        setCookieAllow(cookieManager, webView);

        // 로그인데이터저장
        preferences = getSharedPreferences("member", Activity.MODE_PRIVATE);
        if(preferences!=null) {
            pEditor = preferences.edit();
        }

        Intent splash = new Intent(kr.co.itforone.happy100.MainActivity.this, SplashActivity.class);
        startActivity(splash);

        webView.addJavascriptInterface(new WebviewJavainterface(this), "Android");
        webView.setWebViewClient(new kr.co.itforone.happy100.ClientManager(this));
        webView.setWebChromeClient(new kr.co.itforone.happy100.ChoromeManager(this, this));
        webView.setWebContentsDebuggingEnabled(true); // 크롬디버깅
        WebSettings settings = webView.getSettings();
        settings.setUserAgentString(settings.getUserAgentString() + "INAPP/APP_VER=12");
        settings.setTextZoom(100);
        settings.setJavaScriptEnabled(true);    // 자바스크립트
        // 휴대폰본인인증시 필수설정
        settings.setDomStorageEnabled(true);    //  로컬스토리지 사용
        settings.setJavaScriptCanOpenWindowsAutomatically(true); // window.open()허용

        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {    // 뒤로가기 net::ERR_CACHE_MISS
            settings.setCacheMode(WebSettings.LOAD_CACHE_ELSE_NETWORK);
        }
        settings.setAppCacheMaxSize(1024 * 1024 * 8); //8mb
        File dir = getCacheDir();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        settings.setAppCachePath(dir.getPath());
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);  // 앱내부캐시사용여부

        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                webView.clearCache(true);
                webView.reload();
                refreshLayout.setRefreshing(false);
            }
        });

        refreshLayout.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                if (webView.getScrollY() == 0 && flg_refresh == 1) {
                    refreshLayout.setEnabled(true);
                } else {
                    refreshLayout.setEnabled(false);
                }
            }
        });


        // push&공유하기 url체크
        loadUrl = getString(R.string.index);
        try {
            Intent intent = getIntent();
            /*
            Uri uriData = intent.getData();
            Log.d("로그:getExtras()", intent.getExtras().getString("goUrl"));

            if (uriData != null) {
                String idx = uriData.getQueryParameter("idx");
                if (!idx.equals("")) {
                    loadUrl = uriData.getQueryParameter("url").toString() + "?idx=" + uriData.getQueryParameter("idx").toString();
                }
            } else if (!intent.getExtras().getString("goUrl").equals("")) {
                loadUrl = intent.getExtras().getString("goUrl");
            }
            */
            if (intent.ACTION_VIEW.equals(intent.getAction())) {
                Uri uriData = intent.getData();
                Log.d("로그:uriData", uriData.toString());
                if (uriData != null) {
                    String idx = uriData.getQueryParameter("idx");
                    if (!idx.equals("")) {
                        loadUrl = uriData.getQueryParameter("url").toString() + "?idx=" + uriData.getQueryParameter("idx").toString();
                    }
                }
            } else if (!intent.getExtras().getString("goUrl").equals("")) {
                loadUrl = intent.getExtras().getString("goUrl");
            }

        } catch (Exception e) {
            Log.d("로그:uriData_exc", e.toString());
        }

        // 로그인데이터 전달..ㅎ
        loadUrl += (loadUrl.contains("?"))? "&" : "?";
        loadUrl += "app_mb_id=" + preferences.getString("appLoginId", "");
        Log.d("로그:onCreate", loadUrl);

        webView.loadUrl(loadUrl);
        webView.clearCache(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().stopSync();
        }
        Log.d("로그-onPause()", "");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().startSync();
        }
        Log.d("로그-onResume()", "");
    }

    //뒤로가기이벤트
    @Override
    public void onBackPressed() {
        WebBackForwardList historyList = webView.copyBackForwardList();
        String currentUrl = webView.getUrl();

        if (currentUrl.equals(getString(R.string.index))) {
            long tempTime = System.currentTimeMillis();
            long intervalTime = tempTime - backPrssedTime;

            if (0 <= intervalTime && 2000 >= intervalTime) {
                am.finishAllActivity();
            } else {
                backPrssedTime = tempTime;
                Toast.makeText(getApplicationContext(), "한번 더 뒤로가기 누를시 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (webView.canGoBack()) {
            String backTargetUrl = historyList.getItemAtIndex(historyList.getCurrentIndex() - 1).getUrl();
            Log.d("로그:currentUrl", currentUrl);
            Log.d("로그:backTargetUrl", backTargetUrl);

            if (currentUrl.contains("zeropaypoint.or.kr/join") || currentUrl.contains("zeropaypoint.or.kr/payment.act")) {
                // 제로페이 화면에서 뒤로가기시 주문서로 이동
                for (int i = (historyList.getSize() - 1); i < historyList.getSize(); i--) {
                    String tmpUrl = historyList.getItemAtIndex(i).getUrl();
                    Log.d("로그:tmpUrl:" + i, tmpUrl);
                    if (tmpUrl.contains("/zeropay/join")) {
                        webView.goBackOrForward(-i);
                        break;
                    }
                }
                return;

            }
            /*else if (currentUrl.contains("easypay.co.kr") && backTargetUrl.contains("easypay.co.kr")) {
                // 이지페이에서 뒤로가기시 주문서 이동
                for (int i = (historyList.getSize() - 1); i < historyList.getSize(); i--) {
                    String tmpUrl = historyList.getItemAtIndex(i).getUrl();
                    Log.d("로그:tmpUrl:"+i, tmpUrl);
                    if (tmpUrl.contains("/bbs/order_form.php")) {
                        webView.goBackOrForward((-i+1));
                        break;
                    }
                }
                return;
            } */
            else if ((currentUrl.contains("cart.php") && !backTargetUrl.contains("store_view")) || (currentUrl.contains("/bbs/order_result.php") && !backTargetUrl.contains("/bbs/myorder.php"))
            || currentUrl.contains("/bbs/order_done.php") || (currentUrl.contains("/bbs/order_list.php") && backTargetUrl.contains("/bbs/order_detail.php"))) {
                // 결제취소 후 장바구니로 돌아왔는데 뒤로가기시 상세화면이 아니면 인덱스로... or 결제완료->주문페이지 왔을떄 뒤로가기가 주문목록이 아닌경우 or 주문완료 페이지
                // or 푸시로 주문내역 목록인데 뒷페이지가 상세화면이면 인덱스로....
                webView.clearHistory();
                webView.loadUrl(getString(R.string.index));
            }

            webView.goBack();

        } else {
            long tempTime = System.currentTimeMillis();
            long intervalTime = tempTime - backPrssedTime;

            if (0 <= intervalTime && 2000 >= intervalTime) {
                am.finishAllActivity();
            } else {
                if (currentUrl.contains("store_view") || currentUrl.contains("/bbs/order_result.php") || currentUrl.contains("/zeropay/cancel.php")) {
                    // 공유하기 타고왔을경우 or 결제완료 후 주문내역 이동시 or 결제취소시
                    webView.clearHistory();
                    webView.loadUrl(getString(R.string.index));

                } else if (currentUrl.contains("order_detail.php")) {
                    // 푸시알림으로 왔을때
                    webView.loadUrl(getString(R.string.domain) + "bbs/order_list.php");

                } else {
                    backPrssedTime = tempTime;
                    Toast.makeText(getApplicationContext(), "한번 더 뒤로가기 누를시 앱이 종료됩니다.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    public void Norefresh() {
        refreshLayout.setEnabled(false);
    }

    public void Yesrefresh() {
        refreshLayout.setEnabled(true);
    }

    public void setCookieAllow(CookieManager cookieManager, WebView webView) {
        try {
            cookieManager = CookieManager.getInstance();
            cookieManager.setAcceptCookie(true);
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                webView.getSettings().setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
                cookieManager.setAcceptThirdPartyCookies(webView, true);
            }
        } catch (Exception e) {
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == FILECHOOSER_NORMAL_REQ_CODE) {
                if (filePathCallbackNormal == null) return;
                Uri result = (data == null || resultCode != RESULT_OK) ? null : data.getData();
                filePathCallbackNormal.onReceiveValue(result);
                filePathCallbackNormal = null;

            } else if (requestCode == FILECHOOSER_LOLLIPOP_REQ_CODE) {
                Uri[] result = new Uri[0];
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    // 카메라/갤러리 선택
                    if (resultCode == RESULT_OK) {
                        result = (data == null) ? new Uri[]{mCapturedImageURI} : WebChromeClient.FileChooserParams.parseResult(resultCode, data);
                    }
                    filePathCallbackLollipop.onReceiveValue(result);
                    filePathCallbackLollipop = null;
                }
            }
        } else {
            try {
                if (filePathCallbackLollipop != null) {
                    filePathCallbackLollipop.onReceiveValue(null);
                    filePathCallbackLollipop = null;
                    //webView.loadUrl("javascript:removeInputFile()");
                }
            } catch (Exception e) {
            }
        }

        Log.d("로그-onActivityResult()", "");


    }

    // 푸시..
    public static void setTOKEN(Activity mActivity){
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
                if (!task.isSuccessful()) {
                    return;
                }

                // Get new Instance ID token
                TOKEN = task.getResult().getToken();
                Log.d("로그:token", TOKEN);
            }
        });
    }



}
