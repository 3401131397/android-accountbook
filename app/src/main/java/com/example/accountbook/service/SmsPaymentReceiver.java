package com.example.accountbook.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.example.accountbook.database.AppDatabase;
import com.example.accountbook.model.Bill;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 短信支付监听接收器 - 监听银行短信自动识别账单
 */
public class SmsPaymentReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsPaymentReceiver";

    // 银行短信发送号码特征
    private static final String[] BANK_SENDER_PATTERNS = {
            "95588",  // 工商银行
            "95599",  // 农业银行
            "95566",  // 中国银行
            "95533",  // 建设银行
            "95555",  // 招商银行
            "95528",  // 浦发银行
            "95559",  // 交通银行
            "95577",  // 华夏银行
            "95568",  // 民生银行
            "95561",  // 中信银行
    };

    // 支付平台短信关键词
    private static final String[] PAYMENT_KEYWORDS = {
            "支付成功", "消费", "支出", "扣款", "转账成功",
            "交易成功", "付款成功", "支出完成", "已付款",
            "您的账户", "完成交易", "人民币支出"
    };

    // 金额提取模式
    private static final Pattern[] SMS_AMOUNT_PATTERNS = {
            Pattern.compile("(?:消费|支出|支出人民币)[^0-9]*([0-9]+(?:\\.[0-9]{1,2})?)"),
            Pattern.compile("(?:RMB|CNY)[^0-9]*([0-9]+(?:\\.[0-9]{1,2})?)"),
            Pattern.compile("(?:-[0-9]+(?:\\.[0-9]{1,2})?)"),
            Pattern.compile("(?:金额|款项|款)[^0-9]*([0-9]+(?:\\.[0-9]{1,2})?)"),
            Pattern.compile("([0-9]+(?:\\.[0-9]{1,2})?)\\s*(?:元|RMB)"),
    };

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!intent.getAction().equals("android.provider.Telephony.SMS_RECEIVED")) {
            return;
        }

        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        StringBuilder fullMessage = new StringBuilder();
        String sender = "";

        for (Object pdu : pdus) {
            SmsMessage smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
            sender = smsMessage.getOriginatingAddress();
            fullMessage.append(smsMessage.getMessageBody());
        }

        String messageBody = fullMessage.toString();
        Log.d(TAG, "收到短信 - 发送方: " + sender + ", 内容: " + messageBody);

        // 检查是否是支付相关短信
        if (!isPaymentSms(sender, messageBody)) {
            return;
        }

        // 解析金额
        double amount = extractAmount(messageBody);
        if (amount <= 0) return;

        // 创建账单
        saveSmsBill(context, sender, messageBody, amount);
    }

    /**
     * 判断是否是支付/交易短信
     */
    private boolean isPaymentSms(String sender, String content) {
        // 检查发送号码
        if (sender != null) {
            for (String pattern : BANK_SENDER_PATTERNS) {
                if (sender.contains(pattern)) {
                    return true;
                }
            }
        }

        // 检查短信内容关键词
        for (String keyword : PAYMENT_KEYWORDS) {
            if (content.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 从短信内容中提取金额
     */
    private double extractAmount(String content) {
        for (Pattern pattern : SMS_AMOUNT_PATTERNS) {
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                try {
                    String amountStr = matcher.group(1);
                    double amount = Double.parseDouble(amountStr);
                    if (amount > 0 && amount < 1000000) {
                        return Math.round(amount * 100.0) / 100.0;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
        }
        return 0;
    }

    /**
     * 保存短信账单到数据库
     */
    private void saveSmsBill(Context context, String sender, String message, double amount) {
        Bill bill = new Bill();
        bill.setAmount(amount);
        bill.setType(amount > 0 ? "支出" : "收入");
        bill.setPaymentMethod("银行卡");
        bill.setOutputType("自动识别");
        bill.setSource("短信: " + sender);
        bill.setDescription(message.length() > 120 ? message.substring(0, 120) : message);
        bill.setDate(new java.util.Date());
        bill.setCategory("银行交易");

        new Thread(() -> {
            try {
                AppDatabase.getInstance(context).billDao().insert(bill);
                Log.d(TAG, "短信账单自动记录成功: " + amount + "元");
            } catch (Exception e) {
                Log.e(TAG, "保存短信账单失败: " + e.getMessage());
            }
        }).start();
    }
}
