package com.example.bussinessdirectory;

import androidx.appcompat.widget.Toolbar;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    ViewPagerAdapter adapter;
    ViewPager2 viewPager;
    EditText searchField;

    public static LocationHelper currentLocationHelper;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    // Променлива за да прикаже само еднаш Toast за локација
    private boolean firstLocationShown = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        TabLayout tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        searchField = findViewById(R.id.searchField);

        adapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0: tab.setText("Servisi"); break;
                        case 1: tab.setText("Zabava"); break;
                        case 2: tab.setText("Industrija"); break;
                        case 3: tab.setText("Edukacija"); break;
                    }
                }).attach();

        searchField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchCompanies(s.toString());
            }
            @Override
            public void afterTextChanged(Editable s) {}
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkLocationPermission();
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            initLocationHelper();
        }
    }

    private void initLocationHelper() {
        currentLocationHelper = new LocationHelper(this, new LocationHelper.LocationListener() {
            @Override
            public void onLocationReceived(double latitude, double longitude) {
                runOnUiThread(() -> {
                    if (!firstLocationShown) {
                        firstLocationShown = true;
                        Toast.makeText(MainActivity.this,
                                "📍 Твојата локација: " + String.format("%.4f", latitude) + ", " + String.format("%.4f", longitude),
                                Toast.LENGTH_LONG).show();
                    }
                    // Не треба да освежуваме тука, checkNearbyCompanies ќе повика onNearbyStatusChanged
                });
            }

            @Override
            public void onLocationError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "❌ Грешка со локација: " + error, Toast.LENGTH_LONG).show();
                });
            }

            @Override
            public void onNearbyStatusChanged() {
                runOnUiThread(() -> {
                    // Освежи ги сите фрагменти кога има промена на статусот за близина
                    refreshAllFragments();
                });
            }
        });
        currentLocationHelper.startLocationTracking();
    }

    // ✅ ОСВЕЖИ ГО ТЕКОВНИОТ ФРАГМЕНТ
    public void refreshCurrentFragment() {
        if (viewPager == null) return;
        Fragment currentFragment = getSupportFragmentManager()
                .findFragmentByTag("f" + viewPager.getCurrentItem());
        if (currentFragment instanceof BaseCategoryFragment) {
            ((BaseCategoryFragment) currentFragment).refreshDisplay();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initLocationHelper();
            } else {
                Toast.makeText(this, "❌ Локацијата е потребна за да се прикажат блиските фирми", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void searchCompanies(String query) {
        try {
            Fragment currentFragment = getSupportFragmentManager()
                    .findFragmentByTag("f" + viewPager.getCurrentItem());

            if (currentFragment != null && currentFragment.isAdded()) {
                if (currentFragment instanceof BaseCategoryFragment) {
                    ((BaseCategoryFragment) currentFragment).filter(query);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.addCompany) {
            Intent intent = new Intent(MainActivity.this, AddCompanyActivity.class);
            startActivityForResult(intent, 1);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            refreshAllFragments();
        }
    }

    private void refreshAllFragments() {
        if (viewPager != null && adapter != null) {
            for (int i = 0; i < adapter.getItemCount(); i++) {
                Fragment f = getSupportFragmentManager().findFragmentByTag("f" + i);
                if (f instanceof BaseCategoryFragment) {
                    ((BaseCategoryFragment) f).refreshDisplay();
                }
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (currentLocationHelper != null) {
            currentLocationHelper.startLocationTracking();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (currentLocationHelper != null) {
            currentLocationHelper.stopLocationTracking();
        }
    }



}