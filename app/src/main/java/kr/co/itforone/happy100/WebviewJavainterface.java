package kr.co.itforone.happy100;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.widget.Toast;

class WebviewJavainterface {
    Activity activity;
    kr.co.itforone.happy100.MainActivity mainActivity;

    WebviewJavainterface(kr.co.itforone.happy100.MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    WebviewJavainterface(Activity activity) {
        this.activity = activity;
    }

    /*
    @JavascriptInterface
    public void call_app() {
        String number ="";
        number = "tel:051-891-0088";
        mainActivity.startActivity(new Intent("android.intent.action.DIAL", Uri.parse(number)));

    }
     */

    // 기기에서 제공하는 공유하기 사용
    @JavascriptInterface
    public void doShare(final String arg1, final String arg2) {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, arg1);       // 제목
                shareIntent.putExtra(Intent.EXTRA_TEXT, arg2);          // 내용

                Intent chooser = Intent.createChooser(shareIntent, "공유하기");
                mainActivity.startActivity(chooser);
            }
        });
    }

    // 현재위치 호출..
    @JavascriptInterface
    public void callCurrentPosition() {
        mainActivity.webView.post(new Runnable() {
            @Override
            public void run() {
                ClientManager clientManager = new ClientManager(mainActivity);
                clientManager.locationHandler.sendEmptyMessage(0);
                Log.d("로그:현재위치 웹뷰에서 호출", "ㅇㅇ");
            }
        });
    }

    // 캐시삭제 webView.clearCache(true);
    @JavascriptInterface
    public void callClearCache() {
        mainActivity.webView.post(new Runnable() {
            @Override
            public void run() {
                mainActivity.webView.clearCache(true);
                Toast.makeText(mainActivity.getApplicationContext(), "캐시가 삭제되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
