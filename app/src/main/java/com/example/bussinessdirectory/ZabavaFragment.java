package com.example.bussinessdirectory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ZabavaFragment extends Fragment {

    ListView listView;
    ArrayList<Company> companies;
    CompanyAdapter adapter;
    ArrayList<Company> originalCompanies;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_zabava, container, false);

        listView = view.findViewById(R.id.listViewZabava);
        companies = new ArrayList<>();
        originalCompanies = new ArrayList<>();
        adapter = new CompanyAdapter(getContext(), companies);
        listView.setAdapter(adapter);

        loadCompanies();

        return view;
    }

    private void loadCompanies() {
        String url = "http://10.0.2.2:8888/bussiness_directory/get_companies.php?category=Zabava";

        RequestQueue queue = Volley.newRequestQueue(getContext());

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        companies.clear();
                        originalCompanies.clear();

                        for (int i = 0; i < response.length(); i++) {
                            try {
                                JSONObject obj = response.getJSONObject(i);
                                String name = obj.getString("name");
                                String address = obj.getString("address");
                                String phone = obj.getString("phone");
                                String website = obj.getString("website");

                                Company company = new Company(
                                        obj.getString("name"),
                                        obj.getString("address"),
                                        obj.getString("phone"),
                                        obj.getString("website"),
                                        obj.getDouble("latitude"),
                                        obj.getDouble("longitude"),
                                        "Zabava"
                                );                               companies.add(company);
                                originalCompanies.add(company);

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        adapter.notifyDataSetChanged();

                        if (response.length() == 0) {
                            Toast.makeText(getContext(), "No companies in Zabava", Toast.LENGTH_SHORT).show();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

        queue.add(request);
    }
    public void filter(String text) {
        if (listView == null) {
            return;
        }

        if (originalCompanies == null) {
            originalCompanies = new ArrayList<>();
        }

        if (companies == null) {
            companies = new ArrayList<>();
        }

        if (adapter == null) {
            adapter = new CompanyAdapter(getContext(), companies);
            listView.setAdapter(adapter);
        }

        ArrayList<Company> filteredList = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            if (companies != null && originalCompanies != null) {
                companies.clear();
                companies.addAll(originalCompanies);
                if (adapter != null) {
                    adapter.notifyDataSetChanged();
                }
            }
            return;
        }

        if (originalCompanies != null && !originalCompanies.isEmpty()) {
            for (Company company : originalCompanies) {
                if (company != null) {
                    try {
                        if (company.getName().toLowerCase().contains(text.toLowerCase()) ||
                                company.getAddress().toLowerCase().contains(text.toLowerCase()) ||
                                company.getPhone().toLowerCase().contains(text.toLowerCase()) ||
                                company.getWebsite().toLowerCase().contains(text.toLowerCase())) {

                            filteredList.add(company);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        if (companies != null && adapter != null) {
            companies.clear();
            companies.addAll(filteredList);
            adapter.notifyDataSetChanged();
        }
    }

    public void refreshDisplay() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            checkAndNotifyNearbyCompanies();
        }
    }

    public void checkAndNotifyNearbyCompanies() {
        if (MainActivity.currentLocationHelper == null || companies == null) return;

        boolean foundNearby = false;

        for (Company company : companies) {
            if (company != null) {
                boolean isNearby = MainActivity.currentLocationHelper.isNearby(
                        company.getLatitude(),
                        company.getLongitude()
                );

                if (isNearby) {
                    Toast.makeText(getContext(),
                            "🏢 Блиску сте до: " + company.getName(),
                            Toast.LENGTH_LONG).show();
                    foundNearby = true;
                }
            }
        }

        if (!foundNearby) {
            Toast.makeText(getContext(),
                    "Нема блиски фирми во радиус од 50 метри",
                    Toast.LENGTH_SHORT).show();
        }
    }
}