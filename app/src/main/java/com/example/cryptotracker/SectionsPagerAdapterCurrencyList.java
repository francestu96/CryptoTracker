package com.example.cryptotracker;

import android.util.SparseArray;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class SectionsPagerAdapterCurrencyList extends FragmentPagerAdapter {

    private SparseArray<String> fragmentTags;
    private FragmentManager fragmentManager;

    protected SectionsPagerAdapterCurrencyList(FragmentManager fm) {
        super(fm, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        fragmentManager = fm;
        fragmentTags = new SparseArray<>();
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Object object = super.instantiateItem(container, position);
        if (object instanceof Fragment) {
            Fragment fragment = (Fragment) object;
            String tag = fragment.getTag();
            fragmentTags.put(position, tag);
        }
        return object;
    }

    public Fragment getFragment(int position) {
        Fragment fragment = null;
        String tag = fragmentTags.get(position);
        if (tag != null) {
            fragment = fragmentManager.findFragmentByTag(tag);
        }
        return fragment;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return CurrencyListFragment.newInstance();
            case 1:
                return FavoriteCurrencyListFragment.newInstance();
        }
        return null;
    }

    @Override
    public int getCount() {
        // Total pages to show
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        switch (position) {
            case 0:
                return "All Coins";
            case 1:
                return "Favorites";
            default:
                return null;
        }
    }
}
