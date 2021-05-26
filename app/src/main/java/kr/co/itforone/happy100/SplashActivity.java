package kr.co.itforone.happy100;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SplashActivity extends AppCompatActivity {
    Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        context = this.getBaseContext();
        
        Log.d("로그", "splash onCreate");

        checkPermissions();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23){ // 마시멜로(안드로이드 6.0) 이상 권한 체크
            TedPermission.with(context)
                    .setPermissionListener(permissionlistener)
                    //.setRationaleMessage("앱 사용을 위해 권한을 허용해 주세요.")
                    .setDeniedMessage("앱 사용을 위해 권한 설정이 필요합니다.\n [설정] > [권한] 에서 사용으로 변경해 주세요.")
                    .setGotoSettingButton(true)
                    .setPermissions(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE, // 기기, 사진, 미디어, 파일 엑세스 권한
                            Manifest.permission.READ_EXTERNAL_STORAGE,
                            Manifest.permission.CAMERA
                    )
                    .check();
        } else {
            initView();
        }
    }

    PermissionListener permissionlistener = new PermissionListener() {
        @Override
        public void onPermissionGranted() {
            initView(); // 권한이 승인되었을 때 실행할 함수
        }

        @Override
        public void onPermissionDenied(ArrayList<String> deniedPermissions) {
            Toast.makeText(context, "권한 요청에 동의 해주셔야 이용 가능합니다. [설정] > [권한] 에서 사용으로 변경해 주세요.", Toast.LENGTH_SHORT).show();
            // 앱종료
            moveTaskToBack(true);
            finish();
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    };

    private void initView() {
        // 핸들러로 이용해서 1초간 머물고 이동이 됨
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }


    /*
    private final int SPLASH_DISPLAY_LENGTH = 1000;

    private String[] permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE, // 기기, 사진, 미디어, 파일 엑세스 권한
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
    };
    private static final int MULTIPLE_PERMISSIONS = 101;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        //FirebaseApp.initializeApp(this);//firebase 등록함
        //FirebaseMessaging.getInstance().subscribeToTopic("namo");
        //토큰 생성
        //Common.TOKEN= FirebaseInstanceId.getInstance().getToken();

        try {
            if (Build.VERSION.SDK_INT >= 23) {
                checkPermissions();
            } else {
                goHandler();
            }
        } catch (Exception e) {
        }
    }

    public boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();

        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        Log.d("로그", permissionList.toString());

        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        } else {
            goHandler();
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    int chk_all = 0;
                    int chk_cnt = 0;
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[i])) {
                            chk_all++;
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showToast_PermissionDeny();
                            } else {
                                chk_cnt++;
                            }
                        }
                    }
                    if (chk_all == chk_cnt) goHandler();

                } else {
                    goHandler();
                }
                return;
            }
        }

    }

    private void showToast_PermissionDeny() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setData(Uri.parse("package:" + getPackageName()));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        startActivityForResult(intent, SPLASH_DISPLAY_LENGTH);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==SPLASH_DISPLAY_LENGTH){
            try {
                if (Build.VERSION.SDK_INT >= 23) {
                    checkPermissions();
                } else {
                    goHandler();
                }
            } catch (Exception e) {

            }
        }
    }

    // 핸들러로 이용해서 1초간 머물고 이동이 됨
    public void goHandler() {
        Handler mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }
     */

}