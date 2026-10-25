package com.example.accountbook.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.accountbook.R;
import com.example.accountbook.adapter.BillAdapter;
import com.example.accountbook.viewmodel.BillViewModel;
import com.google.android.material.tabs.TabLayout;

/**
 * 账单列表页面 - 展示所有账单，支持分类筛选
 */
public class BillsListFragment extends Fragment {

    private BillViewModel viewModel;
    private BillAdapter adapter;
    private RecyclerView recyclerView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bills_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(BillViewModel.class);

        recyclerView = view.findViewById(R.id.recycler_bills);
        TabLayout tabLayout = view.findViewById(R.id.tab_filter);

        adapter = new BillAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 默认显示全部
        viewModel.getAllBills().observe(getViewLifecycleOwner(), bills -> {
            adapter.submitList(bills);
        });

        // Tab筛选
        tabLayout.addTab(tabLayout.newTab().setText("全部"));
        tabLayout.addTab(tabLayout.newTab().setText("支出"));
        tabLayout.addTab(tabLayout.newTab().setText("收入"));
        tabLayout.addTab(tabLayout.newTab().setText("自动"));
        tabLayout.addTab(tabLayout.newTab().setText("手动"));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filterBills(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }

    private void filterBills(int position) {
        switch (position) {
            case 0: // 全部
                viewModel.getAllBills().observe(getViewLifecycleOwner(), bills -> {
                    adapter.submitList(bills);
                });
                break;
            case 1: // 支出
                viewModel.getBillsByType("支出").observe(this, bills -> {
                    adapter.submitList(bills);
                });
                break;
            case 2: // 收入
                viewModel.getBillsByType("收入").observe(this, bills -> {
                    adapter.submitList(bills);
                });
                break;
            case 3: // 自动识别
                viewModel.getAllBills().observe(this, bills -> {
                    adapter.submitList(bills);
                });
                break;
            case 4: // 手动添加
                viewModel.getAllBills().observe(this, bills -> {
                    adapter.submitList(bills);
                });
                break;
        }
    }
}
