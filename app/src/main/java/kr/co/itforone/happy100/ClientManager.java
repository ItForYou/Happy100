package kr.co.itforone.happy100;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.net.URISyntaxException;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

class ClientManager extends WebViewClient {
    Activity activity;
    MainActivity mainActivity;
    private GpsTracker gpsTracker;
    private int gpsCount = 0;
    private double curLat, curLng;
    public LocationHandler locationHandler = new LocationHandler();
    private boolean firstLoadChk = false;
    private boolean sendToken = false;

    ClientManager(MainActivity mainActivity){
        this.mainActivity = mainActivity;
    }
    ClientManager(Activity activity, MainActivity mainActivity){
        this.mainActivity = mainActivity;
        this.activity = activity;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        super.onPageStarted(view, url, favicon);
        //mainActivity.pBar.setVisibility(View.VISIBLE);  // 로딩바시작
        mainActivity.imgGif.setVisibility(View.VISIBLE);
    }

    // 로딩종료시
    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);
        //mainActivity.pBar.setVisibility(View.GONE);  // 로딩바끝
        mainActivity.imgGif.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            CookieSyncManager.getInstance().sync();
        } else {
            CookieManager.getInstance().flush();
        }

        // 현재위치 호출
        if (gpsCount == 0) locationHandler.sendEmptyMessage(0);

        // 첫시작시 장바구니 삭제
        if (!firstLoadChk) {
            mainActivity.webView.loadUrl("javascript:initDeleteCart()");
            firstLoadChk = true;
            //Log.d("로그:첫시작후 page fin", firstLoadChk + "");
        }

        // 당겨서 새로고침 제어
        String[] noRefreshPage = {"/bbs/store_menu.php", "/bbs/store_menu_list.php", "/bbs/add_delivery.php", "/bbs/add_detail", "/bbs/store_view.php"};
        Boolean chkPage = false;
        for (String page : noRefreshPage) {
            if (url.indexOf(page) > -1) chkPage = true;
        }
        if (chkPage) {
            mainActivity.Norefresh();
            mainActivity.flg_refresh=0;
        } else {
            mainActivity.Yesrefresh();
            mainActivity.flg_refresh=1;
        }

        // 이전 히스토리 삭제
        if (url.contains("/zeropay/cancel.php") || url.equals(mainActivity.getString(R.string.index)) || url.equals(mainActivity.getString(R.string.domain))) {
            view.clearHistory();
            Log.d("로그:히스토리삭제", url);
        }

        // fcm 토큰전달
        if (!sendToken && mainActivity.TOKEN != "") {
            mainActivity.webView.loadUrl("javascript:fcmKey('"+ mainActivity.TOKEN +"', 'AOS')");
            sendToken = true;
            Log.d("로그:fcmKey()", mainActivity.TOKEN);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Log.d("로그:url" , url);

        // 외부앱 실행
        if (url.startsWith("intent://")) {
            Intent intent = null;
            try {
                intent = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                if (intent != null) {
                    mainActivity.startActivity(intent);
                }
            } catch (URISyntaxException e) {
                //URI 문법 오류 시 처리 구간

            } catch (ActivityNotFoundException e) {
                String packageName = intent.getPackage();
                if (!packageName.equals("")) {
                    // 앱이 설치되어 있지 않을 경우 구글마켓 이동
                    mainActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
                }
            }
            return true;

        } else if (url.startsWith("https://play.google.com/store/apps/details?id=") || url.startsWith("market://details?id=")) {
            //표준창 내 앱설치하기 버튼 클릭 시 PlayStore 앱으로 연결하기 위한 로직
            Uri uri = Uri.parse(url);
            String packageName = uri.getQueryParameter("id");
            if (packageName != null && !packageName.equals("")) {
                // 구글마켓 이동
                mainActivity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packageName)));
            }
            return true;
        }

        //전화걸기
        if (url.startsWith("tel:")) {
            Intent i = new Intent(Intent.ACTION_DIAL, Uri.parse(url));
            mainActivity.startActivity(i);
            return true;
        }

        if (url.contains("/partner/request.php")) {
            // (배달올거제) 입점신청서 외부브라우저 실행
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mainActivity.startActivity(i);
            return true;

        } else if (url.contains("zeropaypoint") || url.contains("checkplus")) {
            // 제로페이 (거제사랑상품권)
            // 나이스 본인인증

            // 제로페이 상품권구매는 외부브라우저
            if (url.contains("main_0002_01.act")) {
                Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                mainActivity.startActivity(i);
                return true;
            }

        } else if (!url.contains(mainActivity.getString(R.string.domain))) {
            // 외부링크 연결
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mainActivity.startActivity(i);
            return true;
        }

        view.loadUrl(url);
        view.clearCache(true);
        return true;
    }

    // 현재위치 호출
    public class LocationHandler extends Handler {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            gpsTracker = new GpsTracker(mainActivity);
            curLat = gpsTracker.getLatitude();
            curLng = gpsTracker.getLongitude();

            if (curLng == 0.0) {
                sendEmptyMessageDelayed(0, 1000);
            } else {
                removeMessages(0);
                gpsCount++;

                //String androidId = Settings.Secure.getString(mainActivity.getContentResolver(), Settings.Secure.ANDROID_ID);
                mainActivity.webView.loadUrl("javascript:getMyLocation('"+ curLat +"', '"+ curLng +"', '')");
                Log.d("로그:주소", "curLat=" + curLat + "&curLng" + curLng);
            }

        }
    }

}
