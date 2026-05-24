package com.example.bussinessdirectory;

import android.content.Context;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

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
        // Користи convertView за подобри перформанси
        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.company_item, parent, false);
        }

        View view = convertView;

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

        // Различни логоа според категорија
        String category = company.getCategory();
        if (category == null) category = "";

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
        }
        companyLogo.setImageResource(logoResource);

        // Проверка за близина во РЕАЛНО ВРЕМЕ
        try {
            if (MainActivity.currentLocationHelper != null && company != null) {
                boolean isNearby = MainActivity.currentLocationHelper.isNearby(
                        company.getLatitude(),
                        company.getLongitude()
                );

                if (isNearby) {
                    view.setBackgroundColor(0xFF00FF00); // Зелена боја
                    nearbyIndicator.setVisibility(View.VISIBLE);
                } else {
                    view.setBackgroundColor(0x00000000); // Транспарентно
                    nearbyIndicator.setVisibility(View.GONE);
                }
            } else {
                view.setBackgroundColor(0x00000000);
                nearbyIndicator.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            view.setBackgroundColor(0x00000000);
            nearbyIndicator.setVisibility(View.GONE);
        }

        return view;
    }

    public void updateList(List<Company> newList) {
        this.companyList = newList;
        this.originalList = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filterList(List<Company> filteredList) {
        this.companyList = filteredList;
        notifyDataSetChanged();
    }

    public void resetList() {
        this.companyList = new ArrayList<>(originalList);
        notifyDataSetChanged();
    }
}