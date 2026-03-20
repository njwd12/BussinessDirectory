package com.example.bussinessdirectory;
import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class CompanyAdapter extends BaseAdapter {

    Context context;
    List<Company> companyList;
    List<Company> originalList;

    public CompanyAdapter(Context context, List<Company> companyList) {
        this.context = context;
        this.companyList = companyList;
        this.originalList = new ArrayList<>(companyList);
    }

    @Override
    public int getCount() {
        return companyList.size();
    }

    @Override
    public Object getItem(int position) {
        return companyList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.company_item, parent, false);

        TextView name = view.findViewById(R.id.companyName);
        TextView address = view.findViewById(R.id.companyAddress);
        TextView phone = view.findViewById(R.id.companyPhone);
        TextView website = view.findViewById(R.id.companyWebsite);
        ImageView companyLogo = view.findViewById(R.id.companyLogo);
        ImageView nearbyIndicator = view.findViewById(R.id.nearbyIndicator);

        Company company = companyList.get(position);

        name.setText(company.getName());
        address.setText(company.getAddress());
        phone.setText(company.getPhone());
        website.setText(company.getWebsite());

        // razlicni logoa spored kateogrija
        String category = company.getCategory();
        if (category == null) category = "";

        android.util.Log.d("CompanyAdapter", "Company: " + company.getName() + ", Category: " + category);

        int logoResource;
        switch(category) {
            case "Servisi":
                logoResource = R.drawable.ic_service;
                break;
            case "Zabava":
                logoResource = R.drawable.ic_entertainment;
                break;
            case "Industrija":
                logoResource = R.drawable.ic_industry;
                break;
            case "Edukacija":
                logoResource = R.drawable.ic_education;
                break;
            default:
                logoResource = R.drawable.ic_service;
                android.util.Log.d("CompanyAdapter", "Using default logo for category: " + category);
        }
        companyLogo.setImageResource(logoResource);

        // Proverka za blizina
        try {
            if (MainActivity.currentLocationHelper != null && company != null) {

                boolean isNearby = MainActivity.currentLocationHelper.isNearby(
                        company.getLatitude(),
                        company.getLongitude()
                );

                android.util.Log.d("CompanyAdapter", "isNearby: " + isNearby + " for " + company.getName());

                if (isNearby) {
                    view.setBackgroundColor(0xFF00FF00);
                    nearbyIndicator.setVisibility(View.VISIBLE); // ПРИКАЖИ ГО ИНДИКАТОРОТ

                    if (position == 0) {
                        Toast.makeText(context, "📍 Блиску сте до: " + company.getName(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    view.setBackgroundColor(0x00000000); // Транспарентно
                    nearbyIndicator.setVisibility(View.GONE); // СОКРИЈ ГО ИНДИКАТОРОТ
                }
            } else {
                nearbyIndicator.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            view.setBackgroundColor(0x00000000);
            nearbyIndicator.setVisibility(View.GONE);
        }

        return view;
    }

    public void filterList(List<Company> filteredList) {
        companyList = filteredList;
        notifyDataSetChanged();
    }

    public void resetList() {
        companyList = new ArrayList<>(originalList);
        notifyDataSetChanged();
    }

    public static float calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] results = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, results);
        return results[0];
    }
}