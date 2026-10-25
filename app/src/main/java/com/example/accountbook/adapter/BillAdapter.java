package com.example.accountbook.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accountbook.R;
import com.example.accountbook.model.Bill;
import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * 账单列表适配器 - Material3卡片风格
 */
public class BillAdapter extends RecyclerView.Adapter<BillAdapter.BillViewHolder> {

    private List<Bill> bills = new ArrayList<>();
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("MM-dd HH:mm", Locale.CHINA);

    public void submitList(List<Bill> newBills) {
        this.bills = newBills != null ? newBills : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BillViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_bill, parent, false);
        return new BillViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BillViewHolder holder, int position) {
        Bill bill = bills.get(position);

        holder.tvCategory.setText(bill.getCategory());
        holder.tvPaymentMethod.setText(bill.getPaymentMethod());
        holder.tvDate.setText(DATE_FORMAT.format(bill.getDate()));
        holder.tvDescription.setText(bill.getDescription());

        if ("支出".equals(bill.getType())) {
            holder.tvAmount.setText(String.format(Locale.CHINA, "-¥%.2f", bill.getAmount()));
            holder.tvAmount.setTextColor(holder.itemView.getContext().getColor(R.color.expense_red));
        } else {
            holder.tvAmount.setText(String.format(Locale.CHINA, "+¥%.2f", bill.getAmount()));
            holder.tvAmount.setTextColor(holder.itemView.getContext().getColor(R.color.income_green));
        }

        // 标记自动/手动
        if ("自动识别".equals(bill.getOutputType())) {
            holder.tvTag.setText("自动");
            holder.tvTag.setVisibility(View.VISIBLE);
        } else {
            holder.tvTag.setText("手动");
            holder.tvTag.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return bills.size();
    }

    static class BillViewHolder extends RecyclerView.ViewHolder {
        MaterialCardView card;
        TextView tvAmount;
        TextView tvCategory;
        TextView tvPaymentMethod;
        TextView tvDate;
        TextView tvDescription;
        TextView tvTag;

        BillViewHolder(@NonNull View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_bill);
            tvAmount = itemView.findViewById(R.id.tv_amount);
            tvCategory = itemView.findViewById(R.id.tv_category);
            tvPaymentMethod = itemView.findViewById(R.id.tv_payment_method);
            tvDate = itemView.findViewById(R.id.tv_date);
            tvDescription = itemView.findViewById(R.id.tv_description);
            tvTag = itemView.findViewById(R.id.tv_tag);
        }
    }
}
