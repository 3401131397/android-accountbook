package com.example.accountbook.model.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.accountbook.model.Bill;

import java.util.Date;
import java.util.List;

@Dao
public interface BillDao {

    @Insert
    long insert(Bill bill);

    @Update
    void update(Bill bill);

    @Delete
    void delete(Bill bill);

    @Query("DELETE FROM bill_table")
    void deleteAll();

    @Query("SELECT * FROM bill_table ORDER BY date DESC")
    LiveData<List<Bill>> getAllBills();

    @Query("SELECT * FROM bill_table ORDER BY date DESC")
    List<Bill> getAllBillsSync();

    @Query("SELECT * FROM bill_table WHERE id = :id LIMIT 1")
    Bill getBillById(long id);

    @Query("SELECT * FROM bill_table WHERE type = :type ORDER BY date DESC")
    LiveData<List<Bill>> getBillsByType(String type);

    @Query("SELECT * FROM bill_table WHERE category = :category ORDER BY date DESC")
    LiveData<List<Bill>> getBillsByCategory(String category);

    @Query("SELECT * FROM bill_table WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    LiveData<List<Bill>> getBillsByDateRange(Date startDate, Date endDate);

    @Query("SELECT SUM(amount) FROM bill_table WHERE type = '支出' AND date BETWEEN :startDate AND :endDate")
    double getTotalExpense(Date startDate, Date endDate);

    @Query("SELECT SUM(amount) FROM bill_table WHERE type = '收入' AND date BETWEEN :startDate AND :endDate")
    double getTotalIncome(Date startDate, Date endDate);

    @Query("SELECT SUM(amount) FROM bill_table WHERE type = '支出' AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalExpenseLive(Date startDate, Date endDate);

    @Query("SELECT SUM(amount) FROM bill_table WHERE type = '收入' AND date BETWEEN :startDate AND :endDate")
    LiveData<Double> getTotalIncomeLive(Date startDate, Date endDate);

    @Query("SELECT category, SUM(amount) as total FROM bill_table " +
            "WHERE type = '支出' AND date BETWEEN :startDate AND :endDate " +
            "GROUP BY category ORDER BY total DESC")
    List<CategorySum> getCategorySums(Date startDate, Date endDate);

    @Query("SELECT paymentMethod, SUM(amount) as total FROM bill_table " +
            "WHERE date BETWEEN :startDate AND :endDate " +
            "GROUP BY paymentMethod ORDER BY total DESC")
    List<CategorySum> getPaymentMethodSums(Date startDate, Date endDate);

    @Query("SELECT COUNT(*) FROM bill_table WHERE outputType = '自动识别' AND date BETWEEN :startDate AND :endDate")
    int getAutoDetectedCount(Date startDate, Date endDate);

    @Query("SELECT COUNT(*) FROM bill_table WHERE outputType = '手动添加' AND date BETWEEN :startDate AND :endDate")
    int getManualAddedCount(Date startDate, Date endDate);
}
