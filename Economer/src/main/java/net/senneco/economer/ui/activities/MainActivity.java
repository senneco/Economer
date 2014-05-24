package net.senneco.economer.ui.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import net.senneco.economer.R;
import net.senneco.economer.ui.adapters.ItemsAdapter;


public class MainActivity extends ActionBarActivity {

    private ItemsAdapter mItemsAdapter;
    private ViewPager mItemsPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mItemsAdapter = new ItemsAdapter(getSupportFragmentManager());

        mItemsPager = (ViewPager) findViewById(R.id.pager_items);
        mItemsPager.setAdapter(mItemsAdapter);
        mItemsPager.setPageMargin(10);
    }

    public void addItemIfAny() {
        if (mItemsPager.getCurrentItem() == mItemsAdapter.getCount() - 1) {
            mItemsAdapter.addPage();
        }
        mItemsPager.setCurrentItem(mItemsPager.getCurrentItem() + 1);
    }
}
