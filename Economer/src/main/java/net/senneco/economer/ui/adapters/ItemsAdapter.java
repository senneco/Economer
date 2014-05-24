package net.senneco.economer.ui.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import net.senneco.economer.ui.fragments.CalculatorFragment;

/**
 * Created by senneco on 24.05.2014
 */
public class ItemsAdapter extends FragmentStatePagerAdapter {

    private int mSize;
    private SparseArray<Fragment> mFragments;

    public ItemsAdapter(FragmentManager fm) {
        super(fm);

        mFragments = new SparseArray<Fragment>();
        mSize = 1;
    }

    public void addPage() {
        mSize++;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        if (mFragments.get(position) == null) {
            mFragments.put(position, new CalculatorFragment());
        }
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mSize;
    }
}
