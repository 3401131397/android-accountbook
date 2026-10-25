package com.example.accountbook.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.accountbook.model.Bill;
import com.example.accountbook.model.dao.CategorySum;
import com.example.accountbook.repository.BillRepository;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BillViewModel extends AndroidViewModel {

    private final BillRepository repository;
    private final LiveData<List<Bill>> allBills;
    private final ExecutorService executorService;

    public BillViewModel(@NonNull Application application) {
        super(application);
        repository = new BillRepository(application);
        allBills = repository.getAllBills();
        executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Bill>> getAllBills() {
        return allBills;
    }

    public void insert(Bill bill) {
        repository.insert(bill, null);
    }

    public void insert(Bill bill, BillRepository.OnInsertCallback callback) {
        repository.insert(bill, callback);
    }

    public void update(Bill bill) {
        repository.update(bill);
    }

    public void delete(Bill bill) {
        repository.delete(bill);
    }

    public LiveData<List<Bill>> getBillsByType(String type) {
        return repository.getBillsByType(type);
    }

    public LiveData<List<Bill>> getBillsByDateRange(Date startDate, Date endDate) {
        return repository.getBillsByDateRange(startDate, endDate);
    }

    public LiveData<Double> getTotalExpense(Date startDate, Date endDate) {
        return repository.getTotalExpenseLive(startDate, endDate);
    }

    public LiveData<Double> getTotalIncome(Date startDate, Date endDate) {
        return repository.getTotalIncomeLive(startDate, endDate);
    }

    public void getCategorySums(Date startDate, Date endDate, CategorySumsCallback callback) {
        executorService.execute(() -> {
            List<CategorySum> result = repository.getCategorySums(startDate, endDate);
            callback.onResult(result);
        });
    }

    public void getPaymentMethodSums(Date startDate, Date endDate, CategorySumsCallback callback) {
        executorService.execute(() -> {
            List<CategorySum> result = repository.getPaymentMethodSums(startDate, endDate);
            callback.onResult(result);
        });
    }

    public interface CategorySumsCallback {
        void onResult(List<CategorySum> data);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}
