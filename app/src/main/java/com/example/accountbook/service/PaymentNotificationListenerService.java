package com.example.accountbook.service;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.text.TextUtils;
import android.util.Log;

import com.example.accountbook.database.AppDatabase;
import com.example.accountbook.model.Bill;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 通知栏监听服务 - 实时监听支付通知并自动记录账单
 * 支持微信、支付宝、银行卡等支付渠道的通知识别
 */
public class PaymentNotificationListenerService extends NotificationListenerService {

    private static final String TAG = "PaymentListener";

    // 支付App的包名
    private static final String[] PAYMENT_PACKAGES = {
            "com.tencent.mm",        // 微信
            "com.eg.android.AlipayGphone", // 支付宝
            "com.android.bankabc",   // 中国农业银行
            "com.chinamworld.bocmbci", // 中国银行
            "com.cmbc.cn.iphoneclient", // 招商银行
            "com.icbc",              // 工商银行
            "com.ccb.ccbnetpay",     // 建设银行
            "cn.com.spdb.mobilebank.per", // 浦发银行
            "com.huawei.wallet",     // 华为钱包
            "com.unionpay",          // 银联
            "com.mipay.wallet"       // 小米支付
    };

    // 金额提取正则模式
    private static final Pattern[] AMOUNT_PATTERNS = {
            Pattern.compile("(?:消费|支出|支付|付款|转账|转款|扣款|花费)[^0-9]*([0-9]+(?:\\.[0-9]{1,2})?)"),
            Pattern.compile("(?:成功支付|已完成支付|交易成功)[^0-9]*([0-9]+(?:\\.[0-9]{1,2})?)"),
            Pattern.compile("(?:人民币|RMB|¥|￥)\\s*([0-9]+(?:\\.[0-9]{1,2})?)"),
            Pattern.compile("(-[0-9]+(?:\\.[0-9]{1,2})?)"),
            Pattern.compile("([0-9]+(?:\\.[0-9]{1,2})?)\\s*(?:元|人民币)"),
            Pattern.compile("(?:金额|款项)[^0-9]*([0-9]+(?:\\.[0-9]{1,2})?)"),
            Pattern.compile("(?:向|给|转给)\\s*[^0-9]*?\\s*([0-9]+(?:\\.[0-9]{1,2})?)"),
    };

    // 支付方式识别关键词
    private static final String[][] PAYMENT_METHOD_KEYWORDS = {
            {"微信", "微信支付"},
            {"支付宝", "支付宝"},
            {"银行卡", "银行卡"},
            {"余额宝", "余额宝"},
            {"零钱", "微信零钱"},
            {"信用卡", "信用卡"},
            {"花呗", "花呗"},
            {"白条", "京东白条"},
    };

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();

        // 检查是否是已知支付App
        boolean isPaymentApp = false;
        for (String pkg : PAYMENT_PACKAGES) {
            if (pkg.equals(packageName)) {
                isPaymentApp = true;
                break;
            }
        }

        if (!isPaymentApp) return;

        // 提取通知内容
        Notification notification = sbn.getNotification();
        if (notification == null) return;

        Bundle extras = notification.extras;
        if (extras == null) return;

        String title = extras.getString(Notification.EXTRA_TITLE, "");
        String text = extras.getString(Notification.EXTRA_TEXT, "");
        String bigText = extras.getString(Notification.EXTRA_BIG_TEXT, "");
        String subText = extras.getString(Notification.EXTRA_SUB_TEXT, "");

        String content = title + " " + text + " " + bigText + " " + subText;

        Log.d(TAG, "收到通知 - " + packageName + ": " + content);

        // 尝试提取支付信息
        PaymentInfo paymentInfo = parsePaymentInfo(content, packageName);
        if (paymentInfo != null && paymentInfo.amount > 0) {
            // 保存到账单数据库
            saveBill(paymentInfo, packageName, content);
        }
    }

    /**
     * 解析通知内容，提取支付信息
     */
    private PaymentInfo parsePaymentInfo(String content, String packageName) {
        if (TextUtils.isEmpty(content)) return null;

        PaymentInfo info = new PaymentInfo();
        info.source = packageName;

        // 提取金额
        double amount = extractAmount(content);
        if (amount <= 0) return null;
        info.amount = amount;

        // 判断支付/收入类型
        if (content.contains("收款") || content.contains("到账")
                || content.contains("收入") || content.contains("入账")
                || content.contains("退款") || content.contains("余额")) {
            info.type = "收入";
        } else {
            info.type = "支出";
        }

        // 识别支付方式
        info.paymentMethod = detectPaymentMethod(content, packageName);

        // 识别分类
        info.category = detectCategory(content);

        // 设置输出类型
        info.outputType = "自动识别";
        info.description = content.length() > 100 ? content.substring(0, 100) : content;

        return info;
    }

    /**
     * 提取金额 - 多种正则模式匹配
     */
    private double extractAmount(String content) {
        for (Pattern pattern : AMOUNT_PATTERNS) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                try {
                    String amountStr = matcher.group(1);
                    double amount = Double.parseDouble(amountStr);
                    if (amount > 0 && amount < 1000000) { // 合理金额范围
                        return Math.round(amount * 100.0) / 100.0;
                    }
                } catch (NumberFormatException | IllegalStateException e) {
                    continue;
                }
            }
        }
        return 0;
    }

    /**
     * 检测支付方式
     */
    private String detectPaymentMethod(String content, String packageName) {
        // 先检查包名
        if (packageName.contains("tencent")) return "微信支付";
        if (packageName.contains("alipay")) return "支付宝";
        if (packageName.contains("icbc") || packageName.contains("bankabc")
                || packageName.contains("bocmbci") || packageName.contains("cmbc")
                || packageName.contains("ccb") || packageName.contains("spdb")) return "银行卡";

        // 检查关键词
        for (String[] keyword : PAYMENT_METHOD_KEYWORDS) {
            if (content.contains(keyword[0])) {
                return keyword[1];
            }
        }

        return "其他";
    }

    /**
     * 检测消费分类
     */
    private String detectCategory(String content) {
        if (content.contains("餐饮") || content.contains("吃饭") || content.contains("外卖")
                || content.contains("餐厅") || content.contains("美食") || content.contains("火锅")
                || content.contains("咖啡") || content.contains("奶茶")) {
            return "餐饮美食";
        }
        if (content.contains("超市") || content.contains("便利店") || content.contains("商城")
                || content.contains("淘宝") || content.contains("京东") || content.contains("拼多多")) {
            return "购物消费";
        }
        if (content.contains("打车") || content.contains("滴滴") || content.contains("地铁")
                || content.contains("公交") || content.contains("加油") || content.contains("高速")) {
            return "交通出行";
        }
        if (content.contains("水电") || content.contains("物业") || content.contains("房租")
                || content.contains("话费") || content.contains("充值")) {
            return "生活缴费";
        }
        if (content.contains("转账") || content.contains("红包") || content.contains("转给")) {
            return "转账红包";
        }
        if (content.contains("医疗") || content.contains("药店") || content.contains("挂号")) {
            return "医疗健康";
        }
        return "其他";
    }

    /**
     * 保存账单到数据库
     */
    private void saveBill(PaymentInfo info, String packageName, String rawContent) {
        Bill bill = new Bill(
                info.amount,
                info.category,
                info.type,
                info.paymentMethod,
                info.outputType,
                info.description,
                info.source
        );

        new Thread(() -> {
            try {
                AppDatabase.getInstance(this).billDao().insert(bill);
                Log.d(TAG, "自动记录账单成功: " + info.amount + "元, " + info.paymentMethod);
            } catch (Exception e) {
                Log.e(TAG, "保存账单失败: " + e.getMessage());
            }
        }).start();
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        // 通知移除时的处理（暂不需要）
    }

    /**
     * 支付信息内部类
     */
    private static class PaymentInfo {
        double amount;
        String type;
        String paymentMethod;
        String category;
        String outputType;
        String description;
        String source;
    }
}
