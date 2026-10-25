package com.example.accountbook.repository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.accountbook.database.AppDatabase;
import com.example.accountbook.model.Bill;
import com.example.accountbook.model.dao.BillDao;
import com.example.accountbook.model.dao.CategorySum;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BillRepository {

    private final BillDao billDao;
    private final ExecutorService executorService;

    public BillRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        this.billDao = database.billDao();
        this.executorService = Executors.newFixedThreadPool(2);
    }

    public LiveData<List<Bill>> getAllBills() {
        return billDao.getAllBills();
    }

    public List<Bill> getAllBillsSync() {
        return billDao.getAllBillsSync();
    }

    public void insert(Bill bill, OnInsertCallback callback) {
        executorService.execute(() -> {
            long id = billDao.insert(bill);
            if (callback != null) {
                callback.onInserted(id);
            }
        });
    }

    public void insertSync(Bill bill) {
        billDao.insert(bill);
    }

    public void update(Bill bill) {
        executorService.execute(() -> billDao.update(bill));
    }

    public void delete(Bill bill) {
        executorService.execute(() -> billDao.delete(bill));
    }

    public void deleteAll() {
        executorService.execute(() -> billDao.deleteAll());
    }

    public LiveData<List<Bill>> getBillsByType(String type) {
        return billDao.getBillsByType(type);
    }

    public LiveData<List<Bill>> getBillsByCategory(String category) {
        return billDao.getBillsByCategory(category);
    }

    public LiveData<List<Bill>> getBillsByDateRange(Date startDate, Date endDate) {
        return billDao.getBillsByDateRange(startDate, endDate);
    }

    public double getTotalExpense(Date startDate, Date endDate) {
        Double result = billDao.getTotalExpense(startDate, endDate);
        return result != null ? result : 0.0;
    }

    public double getTotalIncome(Date startDate, Date endDate) {
        Double result = billDao.getTotalIncome(startDate, endDate);
        return result != null ? result : 0.0;
    }

    public LiveData<Double> getTotalExpenseLive(Date startDate, Date endDate) {
        return billDao.getTotalExpenseLive(startDate, endDate);
    }

    public LiveData<Double> getTotalIncomeLive(Date startDate, Date endDate) {
        return billDao.getTotalIncomeLive(startDate, endDate);
    }

    public List<CategorySum> getCategorySums(Date startDate, Date endDate) {
        return billDao.getCategorySums(startDate, endDate);
    }

    public List<CategorySum> getPaymentMethodSums(Date startDate, Date endDate) {
        return billDao.getPaymentMethodSums(startDate, endDate);
    }

    public interface OnInsertCallback {
        void onInserted(long id);
    }
}
