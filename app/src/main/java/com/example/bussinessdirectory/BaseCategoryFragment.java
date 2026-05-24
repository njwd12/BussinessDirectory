package com.example.bussinessdirectory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;

public abstract class BaseCategoryFragment extends Fragment {

    protected ListView listView;
    protected ProgressBar progressBar;
    protected TextView emptyView;
    protected ArrayList<Company> companies;
    protected ArrayList<Company> originalCompanies;
    protected CompanyAdapter adapter;
    protected DatabaseHelper dbHelper;
    protected String currentFilter = "";

    protected abstract String getCategoryName();
    protected abstract int getLayoutResource();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResource(), container, false);

        listView = view.findViewById(R.id.listView);
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);

        companies = new ArrayList<>();
        originalCompanies = new ArrayList<>();
        dbHelper = new DatabaseHelper(getContext());
        adapter = new CompanyAdapter(getContext(), companies);
        listView.setAdapter(adapter);

        loadCompanies();

        return view;
    }

    protected void loadCompanies() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }

        companies.clear();
        originalCompanies.clear();

        // Земете ги компаниите од локалната база
        ArrayList<Company> allCompanies = dbHelper.getCompaniesByCategory(getCategoryName());

        companies.addAll(allCompanies);
        originalCompanies.addAll(allCompanies);

        // Примени филтер ако има
        if (currentFilter != null && !currentFilter.isEmpty()) {
            ArrayList<Company> filtered = new ArrayList<>();
            for (Company c : companies) {
                if (c.getName().toLowerCase().contains(currentFilter.toLowerCase()) ||
                        c.getAddress().toLowerCase().contains(currentFilter.toLowerCase())) {
                    filtered.add(c);
                }
            }
            companies.clear();
            companies.addAll(filtered);
        }

        adapter.notifyDataSetChanged();

        if (emptyView != null) {
            emptyView.setVisibility(companies.isEmpty() ? View.VISIBLE : View.GONE);
        }

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    public void filter(String query) {
        this.currentFilter = query;
        loadCompanies();
    }

    public void refreshDisplay() {
        loadCompanies();
    }
}