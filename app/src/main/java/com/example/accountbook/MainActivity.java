package com.example.accountbook;

import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.accountbook.service.PaymentNotificationListenerService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
/**
 * 主Activity - 包含底部导航(账单列表/统计数据)、悬浮添加按钮
 */
public class MainActivity extends AppCompatActivity {

    private NavController navController;
    private AppBarConfiguration appBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化底部导航
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);

        // 设置Navigation
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
        }

        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_bills_list, R.id.nav_statistics)
                .build();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(bottomNav, navController);

        // 悬浮添加按钮
        ExtendedFloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddBillActivity.class);
            startActivity(intent);
        });

        // 检查是否已开启通知栏监听权限
        checkNotificationListenerPermission();
    }

    /**
     * 检查通知栏监听权限，如未开启则引导用户授权
     */
    private void checkNotificationListenerPermission() {
        String enabledListeners = Settings.Secure.getString(
                getContentResolver(), "enabled_notification_listeners");
        if (enabledListeners == null || !enabledListeners.contains(getPackageName())) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("需要通知栏监听权限")
                    .setMessage("为了自动识别和记录支付账单，需要开启通知栏监听权限。是否前往设置开启？")
                    .setPositiveButton("去开启", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
                        startActivity(intent);
                    })
                    .setNegativeButton("稍后再说", null)
                    .setCancelable(false)
                    .show();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}
