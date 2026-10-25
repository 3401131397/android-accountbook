package com.example.accountbook.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

/**
 * 账单实体类 - Room数据库表
 */
@Entity(tableName = "bill_table")
public class Bill implements Serializable {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private double amount;          // 金额
    private String category;        // 分类 (餐饮/交通/购物/收入/...)
    private String type;            // 类型 (支出/收入)
    private String paymentMethod;   // 支付方式 (微信/支付宝/银行卡/现金/...)
    private String outputType;      // 输出类型 (自动识别/手动添加)
    private String description;     // 描述信息
    private Date date;              // 创建时间
    private String source;          // 来源 (通知栏应用名/SMS/手动)

    public Bill() {
        this.date = new Date();
    }

    public Bill(double amount, String category, String type, String paymentMethod,
                String outputType, String description, String source) {
        this.amount = amount;
        this.category = category;
        this.type = type;
        this.paymentMethod = paymentMethod;
        this.outputType = outputType;
        this.description = description;
        this.source = source;
        this.date = new Date();
    }

    // ===== Getters & Setters =====

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getOutputType() {
        return outputType;
    }

    public void setOutputType(String outputType) {
        this.outputType = outputType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "Bill{" +
                "id=" + id +
                ", amount=" + amount +
                ", category='" + category + '\'' +
                ", type='" + type + '\'' +
                ", paymentMethod='" + paymentMethod + '\'' +
                ", outputType='" + outputType + '\'' +
                ", description='" + description + '\'' +
                ", date=" + date +
                ", source='" + source + '\'' +
                '}';
    }
}
