package com.example.bussinessdirectory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public abstract class BaseCategoryFragment extends Fragment {

    protected ListView listView;
    protected ProgressBar progressBar;
    protected TextView emptyView;
    protected ArrayList<Company> companies;
    protected ArrayList<Company> originalCompanies;
    protected CompanyAdapter adapter;
    protected RequestQueue requestQueue;
    protected String currentFilter = "";
    protected double currentLat = 0, currentLon = 0;
    protected boolean hasLocation = false;
    protected boolean isLoading = false;

    protected abstract String getCategoryName();
    protected abstract int getLayoutResource();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutResource(), container, false);

        listView = view.findViewById(R.id.listView); // сега сите фрагменти го користат истиот ID
        progressBar = view.findViewById(R.id.progressBar);
        emptyView = view.findViewById(R.id.emptyView);

        companies = new ArrayList<>();
        originalCompanies = new ArrayList<>();
        adapter = new CompanyAdapter(getContext(), companies);
        listView.setAdapter(adapter);

        requestQueue = Volley.newRequestQueue(getContext());

        loadCompanies();

        return view;
    }

    protected void loadCompanies() {
        if (isLoading) return;
        isLoading = true;
        if (progressBar != null) progressBar.setVisibility(View.VISIBLE);

        String url;
        if (hasLocation && currentLat != 0 && currentLon != 0) {
            url = "http://192.168.1.101:8888/bussiness_directory/get_companies.php?category=" + getCategoryName()
                    + "&lat=" + currentLat + "&lon=" + currentLon + "&radius=0.05";
        } else {
            url = "http://192.168.1.101:8888/bussiness_directory/get_companies.php?category=" + getCategoryName();
        }
        if (!currentFilter.isEmpty()) {
            url += "&search=" + currentFilter;
        }

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                response -> {
                    companies.clear();
                    originalCompanies.clear();
                    for (int i = 0; i < response.length(); i++) {
                        try {
                            JSONObject obj = response.getJSONObject(i);
                            String name = obj.getString("name");
                            String address = obj.optString("address");
                            String phone = obj.optString("phone");
                            String website = obj.optString("website");
                            String email = obj.optString("email");
                            double lat = obj.optDouble("latitude", 0);
                            double lon = obj.optDouble("longitude", 0);
                            int distance = obj.optInt("distance", -1);

                            Company company = new Company(name, address, phone, website, lat, lon, getCategoryName());
                            company.setEmail(email);
                            company.setDistance(distance);
                            companies.add(company);
                            originalCompanies.add(company);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    adapter.notifyDataSetChanged();
                    if (emptyView != null) {
                        emptyView.setVisibility(companies.isEmpty() ? View.VISIBLE : View.GONE);
                    }
                    isLoading = false;
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                },
                error -> {
                    Toast.makeText(getContext(), "Error loading: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    isLoading = false;
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                    if (emptyView != null) emptyView.setText("Грешка при вчитување");
                });
        requestQueue.add(request);
    }

    public void updateLocation(double lat, double lon) {
        this.currentLat = lat;
        this.currentLon = lon;
        this.hasLocation = true;
        loadCompanies();
    }

    public void filter(String query) {
        this.currentFilter = query;
        if (query == null || query.isEmpty()) {
            companies.clear();
            companies.addAll(originalCompanies);
            adapter.notifyDataSetChanged();
            return;
        }
        ArrayList<Company> filtered = new ArrayList<>();
        for (Company c : originalCompanies) {
            if (c.getName().toLowerCase().contains(query.toLowerCase()) ||
                    c.getAddress().toLowerCase().contains(query.toLowerCase()) ||
                    c.getPhone().toLowerCase().contains(query.toLowerCase()) ||
                    c.getWebsite().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(c);
            }
        }
        companies.clear();
        companies.addAll(filtered);
        adapter.notifyDataSetChanged();
    }

    public void refreshDisplay() {
        loadCompanies();
    }
}