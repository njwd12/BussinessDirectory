package com.example.bussinessdirectory;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class AddCompanyActivity extends AppCompatActivity {

    // Promenlivi za polinja
    EditText name, address, latitude, longitude, email, phone, website;
    CheckBox cbServisi, cbZabava, cbIndustrija, cbEdukacija;
    Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_company);

        // Povrzuvame promenlivite
        name = findViewById(R.id.name);
        address = findViewById(R.id.address);
        latitude = findViewById(R.id.latitude);
        longitude = findViewById(R.id.longitude);
        email = findViewById(R.id.email);
        phone = findViewById(R.id.phone);
        website = findViewById(R.id.website);

        cbServisi = findViewById(R.id.cbServisi);
        cbZabava = findViewById(R.id.cbZabava);
        cbIndustrija = findViewById(R.id.cbIndustrija);
        cbEdukacija = findViewById(R.id.cbEdukacija);

        saveBtn = findViewById(R.id.saveBtn);

        // Klik na save kopceto
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveCompany();
            }
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void saveCompany() {
//proverka dali se vneseni
                if (name.getText().toString().isEmpty() ||
                address.getText().toString().isEmpty() ||
                email.getText().toString().isEmpty() ||
                phone.getText().toString().isEmpty()) {

            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        StringBuilder categoryBuilder = new StringBuilder();
        if (cbServisi.isChecked()) categoryBuilder.append("Servisi,");
        if (cbZabava.isChecked()) categoryBuilder.append("Zabava,");
        if (cbIndustrija.isChecked()) categoryBuilder.append("Industrija,");
        if (cbEdukacija.isChecked()) categoryBuilder.append("Edukacija,");

        if (categoryBuilder.length() == 0) {
            Toast.makeText(this, "Please select at least one category", Toast.LENGTH_SHORT).show();
            return;
        }

        String category = categoryBuilder.toString();
        if (category.endsWith(",")) {
            category = category.substring(0, category.length() - 1);
        }

        final String finalCategory = category;

        // URL do PHP fajl
        String url = "http://192.168.1.101:8888/bussiness_directory/add_company.php";

        // Volley
        RequestQueue queue = Volley.newRequestQueue(this);

        StringRequest request = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Успешно испратено
                        // После Toast кажува дека е зачувано
                        Toast.makeText(AddCompanyActivity.this, "Company saved!", Toast.LENGTH_SHORT).show();

// Наместо finish(), направи:
                        Intent intent = new Intent();
                        setResult(RESULT_OK, intent);
                        finish();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Грешка
                        Toast.makeText(AddCompanyActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                // Isprakame podatocite preku POST
                Map<String, String> params = new HashMap<>();
                params.put("name", name.getText().toString());
                params.put("address", address.getText().toString());
                params.put("latitude", latitude.getText().toString().isEmpty() ? "0" : latitude.getText().toString());
                params.put("longitude", longitude.getText().toString().isEmpty() ? "0" : longitude.getText().toString());
                params.put("email", email.getText().toString());
                params.put("phone", phone.getText().toString());
                params.put("website", website.getText().toString());
                params.put("category", finalCategory);  // Користи ја final променливата
                return params;
            }
        };

        queue.add(request);
    }
}