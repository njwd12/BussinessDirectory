package com.example.bussinessdirectory;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {

            case 0:
                return new ServisiFragment();

            case 1:
                return new ZabavaFragment();

            case 2:
                return new IndustrijaFragment();

            case 3:
                return new EdukacijaFragment();

            default:
                return new ServisiFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }

    // Додај го овој метод во ViewPagerAdapter класата
    public Fragment getFragment(int position) {
        return createFragment(position);
    }
}