package com.example.accountbook.ui.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.accountbook.R;
import com.example.accountbook.model.dao.CategorySum;
import com.example.accountbook.viewmodel.BillViewModel;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.material.chip.Chip;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * 统计图表页面 - 支出分类饼图 + 支付方式柱状图
 */
public class StatisticsFragment extends Fragment {

    private BillViewModel viewModel;
    private PieChart pieChartCategory;
    private BarChart barChartPayment;
    private MaterialTextView tvTotalExpense;
    private MaterialTextView tvTotalIncome;
    private MaterialTextView tvChipPeriod;
    private Chip chipThisMonth;
    private Chip chipThisWeek;
    private Chip chipCustom;

    private Date startDate;
    private Date endDate;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM", Locale.CHINA);

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(BillViewModel.class);

        pieChartCategory = view.findViewById(R.id.pie_chart_category);
        barChartPayment = view.findViewById(R.id.bar_chart_payment);
        tvTotalExpense = view.findViewById(R.id.tv_total_expense);
        tvTotalIncome = view.findViewById(R.id.tv_total_income);
        tvChipPeriod = view.findViewById(R.id.tv_chip_period);
        chipThisMonth = view.findViewById(R.id.chip_this_month);
        chipThisWeek = view.findViewById(R.id.chip_this_week);
        chipCustom = view.findViewById(R.id.chip_custom);

        // 默认本月
        setThisMonthRange();
        updateData();

        // 时间段选择
        chipThisMonth.setOnClickListener(v -> {
            setThisMonthRange();
            updateData();
        });

        chipThisWeek.setOnClickListener(v -> {
            setThisWeekRange();
            updateData();
        });

        chipCustom.setOnClickListener(v -> showDateRangePicker());
    }

    private void setThisMonthRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        startDate = cal.getTime();
        endDate = Calendar.getInstance().getTime();
        tvChipPeriod.setText(dateFormat.format(startDate) + " 至今");
    }

    private void setThisWeekRange() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        startDate = cal.getTime();
        endDate = Calendar.getInstance().getTime();
        tvChipPeriod.setText("本周至今");
    }

    private void showDateRangePicker() {
        MaterialDatePicker<Long> startPicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("选择起始日期")
                .setSelection(startDate.getTime())
                .build();

        startPicker.addOnPositiveButtonClickListener(selection -> {
            startDate = new Date(selection);
            MaterialDatePicker<Long> endPicker = MaterialDatePicker.Builder.datePicker()
                    .setTitleText("选择结束日期")
                    .setSelection(endDate.getTime())
                    .build();
            endPicker.addOnPositiveButtonClickListener(endSelection -> {
                endDate = new Date(endSelection);
                tvChipPeriod.setText("自定义区间");
                updateData();
            });
            endPicker.show(getParentFragmentManager(), "end_date_picker");
        });

        startPicker.show(getParentFragmentManager(), "start_date_picker");
    }

    private void updateData() {
        // 更新总额
        viewModel.getTotalExpense(startDate, endDate).observe(this, expense -> {
            tvTotalExpense.setText(String.format(Locale.CHINA, "¥%.2f", expense != null ? expense : 0));
        });

        viewModel.getTotalIncome(startDate, endDate).observe(this, income -> {
            tvTotalIncome.setText(String.format(Locale.CHINA, "¥%.2f", income != null ? income : 0));
        });

        // 更新分类饼图
        viewModel.getCategorySums(startDate, endDate, data -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> setupPieChart(data));
            }
        });

        // 更新支付方式柱状图
        viewModel.getPaymentMethodSums(startDate, endDate, data -> {
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> setupBarChart(data));
            }
        });
    }

    private void setupPieChart(List<CategorySum> categorySums) {
        List<PieEntry> entries = new ArrayList<>();
        for (CategorySum item : categorySums) {
            if (item.total > 0) {
                entries.add(new PieEntry((float) item.total, item.category));
            }
        }

        PieDataSet dataSet = new PieDataSet(entries, "支出分类");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(14f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueFormatter(new PercentFormatter(pieChartCategory));

        PieData data = new PieData(dataSet);
        pieChartCategory.setData(data);
        pieChartCategory.setDrawEntryLabels(true);
        pieChartCategory.setEntryLabelTextSize(12f);
        pieChartCategory.setCenterText("支出分布");
        pieChartCategory.setCenterTextSize(16f);
        pieChartCategory.setHoleRadius(40f);
        pieChartCategory.setTransparentCircleRadius(45f);
        pieChartCategory.getDescription().setEnabled(false);
        pieChartCategory.animateY(1000);
        pieChartCategory.invalidate();
    }

    private void setupBarChart(List<CategorySum> paymentMethods) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        for (int i = 0; i < paymentMethods.size(); i++) {
            CategorySum item = paymentMethods.get(i);
            entries.add(new BarEntry(i, (float) item.total));
            labels.add(item.category);
        }

        BarDataSet dataSet = new BarDataSet(entries, "支付方式");
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        dataSet.setValueTextSize(12f);

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.6f);

        barChartPayment.setData(data);
        barChartPayment.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChartPayment.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChartPayment.getXAxis().setGranularity(1f);
        barChartPayment.getXAxis().setLabelRotationAngle(-45);
        barChartPayment.getDescription().setEnabled(false);
        barChartPayment.getAxisRight().setEnabled(false);
        barChartPayment.animateY(1000);
        barChartPayment.invalidate();
    }
}
