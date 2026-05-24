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

public class AddCompanyActivity extends AppCompatActivity {

    EditText name, address, latitude, longitude, email, phone, website;
    CheckBox cbServisi, cbZabava, cbIndustrija, cbEdukacija;
    Button saveBtn;
    DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_company);

        dbHelper = new DatabaseHelper(this);

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
        if (name.getText().toString().trim().isEmpty() ||
                address.getText().toString().trim().isEmpty() ||
                phone.getText().toString().trim().isEmpty()) {

            Toast.makeText(this, "Пополнете ги задолжителните полиња", Toast.LENGTH_SHORT).show();
            return;
        }

        // Категорија
        String category = "";
        if (cbServisi.isChecked()) category = "Servisi";
        else if (cbZabava.isChecked()) category = "Zabava";
        else if (cbIndustrija.isChecked()) category = "Industrija";
        else if (cbEdukacija.isChecked()) category = "Edukacija";
        else {
            Toast.makeText(this, "Изберете категорија", Toast.LENGTH_SHORT).show();
            return;
        }

        double lat = 0, lon = 0;
        try {
            lat = latitude.getText().toString().trim().isEmpty() ? 0 : Double.parseDouble(latitude.getText().toString().trim());
            lon = longitude.getText().toString().trim().isEmpty() ? 0 : Double.parseDouble(longitude.getText().toString().trim());
        } catch (NumberFormatException e) {
            // ignore
        }

        Company company = new Company(
                name.getText().toString().trim(),
                address.getText().toString().trim(),
                phone.getText().toString().trim(),
                website.getText().toString().trim(),
                lat, lon, category
        );
        company.setEmail(email.getText().toString().trim());

        long id = dbHelper.addCompany(company);

        if (id != -1) {
            Toast.makeText(this, "✅ Фирмата е зачувана!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            setResult(RESULT_OK, intent);
            finish();
        } else {
            Toast.makeText(this, "❌ Грешка при зачувување", Toast.LENGTH_SHORT).show();
        }
    }
}