package com.android.talkback;

import com.google.android.accessibility.talkback.R;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

/**
 * Activity chính của ứng dụng ViTalk, hiển thị trên Launcher của thiết bị.
 * Cung cấp giao diện tối giản, dễ tiếp cận cho người khiếm thị để cấu hình ViTalk.
 */
public class ViTalkLauncherActivity extends AppCompatActivity {

    private TextView tvA11yStatus;
    private TextView tvVersion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vitalk_launcher);

        // Ánh xạ các thành phần giao diện
        tvA11yStatus = findViewById(R.id.tv_a11y_status);
        tvVersion = findViewById(R.id.tv_version);

        View btnA11yStatus = findViewById(R.id.btn_a11y_status);
        View btnAdvancedSettings = findViewById(R.id.btn_advanced_settings);
        View btnAbout = findViewById(R.id.btn_about);

        // Hiển thị phiên bản ứng dụng
        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            tvVersion.setText("Phiên bản " + pInfo.versionName);
        } catch (PackageManager.NameNotFoundException e) {
            tvVersion.setText("Phiên bản 1.0");
        }

        // Thiết lập sự kiện click cho các nút bấm
        btnA11yStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openAccessibilitySettings();
            }
        });



        btnAdvancedSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openViTalkSettings();
            }
        });

        btnAbout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAboutDialog();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
    }

    /**
     * Cập nhật trạng thái bật/tắt của dịch vụ trợ năng ViTalk.
     */
    private void updateServiceStatus() {
        if (isServiceRunning()) {
            tvA11yStatus.setText("Trạng thái: Đang hoạt động");
            tvA11yStatus.setTextColor(getResources().getColor(R.color.vitalk_primary));
        } else {
            tvA11yStatus.setText("Trạng thái: Đã tắt (Nhấn để bật)");
            tvA11yStatus.setTextColor(getResources().getColor(R.color.vitalk_text_secondary));
        }
    }

    /**
     * Kiểm tra xem dịch vụ ViTalkAccessibilityService có đang chạy hay không.
     */
    private boolean isServiceRunning() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) return false;
        
        List<AccessibilityServiceInfo> enabledServices = am.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_SPOKEN
        );
        for (AccessibilityServiceInfo enabledService : enabledServices) {
            ServiceInfo enabledServiceInfo = enabledService.getResolveInfo().serviceInfo;
            if (enabledServiceInfo.packageName.equals(getPackageName()) &&
                enabledServiceInfo.name.equals("com.google.android.marvin.talkback.TalkBackService")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Mở phần cài đặt Trợ năng của hệ thống.
     */
    private void openAccessibilitySettings() {
        try {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở cài đặt Trợ năng", Toast.LENGTH_SHORT).show();
        }
    }



    /**
     * Mở cài đặt nâng cao của ViTalk (TalkBackPreferencesActivity gốc).
     */
    private void openViTalkSettings() {
        try {
            Intent intent = new Intent(this, TalkBackPreferencesActivity.class);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Không thể mở cài đặt ViTalk", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Hiển thị hộp thoại giới thiệu thông tin ứng dụng ViTalk.
     */
    private void showAboutDialog() {
        String versionName = "1.0";
        try {
            versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException ignored) {}

        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.ViTalkMenuTheme);
        builder.setTitle("Giới thiệu ViTalk");
        
        String message = "ViTalk - Trình đọc màn hình thân thiện với người Việt.\n\n" +
                "Được phát triển bởi: Technology Entertainment, dưới sự dẫn dắt của Lc_Boy (Vũ Anh Lộc).\n\n" +
                "Phiên bản: " + versionName + "\n\n" +
                "Cảm ơn bạn đã tin dùng ViTalk!";
                
        builder.setMessage(message);
        builder.setPositiveButton("Đóng", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
