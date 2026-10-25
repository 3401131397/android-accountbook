package com.example.accountbook;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.accountbook.model.Bill;
import com.example.accountbook.viewmodel.BillViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 手动添加账单页面
 */
public class AddBillActivity extends AppCompatActivity {

    private BillViewModel viewModel;
    private TextInputEditText etAmount;
    private TextInputEditText etDescription;
    private MaterialAutoCompleteTextView actvCategory;
    private MaterialAutoCompleteTextView actvPaymentMethod;
    private MaterialButton btnSelectDate;
    private SwitchMaterial switchType;

    private Date selectedDate = new Date();

    private static final String[] CATEGORIES = {
            "餐饮美食", "购物消费", "交通出行", "生活缴费",
            "医疗健康", "娱乐休闲", "转账红包", "工资收入", "其他"
    };

    private static final String[] PAYMENT_METHODS = {
            "微信支付", "支付宝", "银行卡", "信用卡",
            "现金", "余额宝", "微信零钱", "其他"
    };

    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_bill);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("添加账单");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        viewModel = new ViewModelProvider(this).get(BillViewModel.class);
        initViews();
        setupDropdowns();
    }

    private void initViews() {
        etAmount = findViewById(R.id.et_amount);
        etDescription = findViewById(R.id.et_description);
        actvCategory = findViewById(R.id.actv_category);
        actvPaymentMethod = findViewById(R.id.actv_payment_method);
        btnSelectDate = findViewById(R.id.btn_select_date);
        switchType = findViewById(R.id.switch_type);

        btnSelectDate.setText(DATE_FORMAT.format(selectedDate));
        btnSelectDate.setOnClickListener(v -> showDatePicker());

        switchType.setOnCheckedChangeListener((buttonView, isChecked) -> {
            switchType.setText(isChecked ? "收入" : "支出");
        });
    }

    private void setupDropdowns() {
        ArrayAdapter<String> categoryAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, CATEGORIES);
        actvCategory.setAdapter(categoryAdapter);
        actvCategory.setText(CATEGORIES[0], false);

        ArrayAdapter<String> paymentAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_list_item_1, PAYMENT_METHODS);
        actvPaymentMethod.setAdapter(paymentAdapter);
        actvPaymentMethod.setText(PAYMENT_METHODS[0], false);
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("选择日期")
                .setSelection(selectedDate.getTime())
                .build();

        datePicker.addOnPositiveButtonClickListener(selection -> {
            selectedDate = new Date(selection);
            btnSelectDate.setText(DATE_FORMAT.format(selectedDate));
        });

        datePicker.show(getSupportFragmentManager(), "date_picker");
    }

    private void saveBill() {
        String amountStr = etAmount.getText().toString().trim();
        if (amountStr.isEmpty()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("提示")
                    .setMessage("请输入金额")
                    .setPositiveButton("确定", null)
                    .show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
            if (amount <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("提示")
                    .setMessage("请输入有效的正数金额")
                    .setPositiveButton("确定", null)
                    .show();
            return;
        }

        String type = switchType.isChecked() ? "收入" : "支出";
        String category = actvCategory.getText().toString();
        String paymentMethod = actvPaymentMethod.getText().toString();
        String description = etDescription.getText().toString().trim();

        Bill bill = new Bill(amount, category, type, paymentMethod, "手动添加", description, "手动输入");
        bill.setDate(selectedDate);

        viewModel.insert(bill, id -> runOnUiThread(() -> {
            if (id > 0) {
                finish();
            }
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_bill, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (itemId == R.id.action_save) {
            saveBill();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
