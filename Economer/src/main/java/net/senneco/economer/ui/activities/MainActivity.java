package net.senneco.economer.ui.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseArray;
import android.widget.TextView;
import net.senneco.economer.R;
import net.senneco.economer.data.Price;
import net.senneco.economer.ui.adapters.ItemsAdapter;


public class MainActivity extends ActionBarActivity {

    private ItemsAdapter mItemsAdapter;
    private ViewPager mItemsPager;
    private SparseArray<Double> mEconomies;
    private TextView mEconomyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEconomies = new SparseArray<Double>();

        mEconomyText = (TextView) findViewById(R.id.text_economy);

        mItemsAdapter = new ItemsAdapter(getSupportFragmentManager());

        mItemsPager = (ViewPager) findViewById(R.id.pager_items);
        mItemsPager.setAdapter(mItemsAdapter);
        mItemsPager.setPageMargin(10);
    }

    public void choosePrice(Price choosePrice, Price maxPrice) {
        double economy = choosePrice.getSize() * (maxPrice.getPriceRate() - choosePrice.getPriceRate());

        mEconomies.put(mItemsPager.getCurrentItem(), economy);

        double totalEconomy = 0;
        for (int i = 0; i < mItemsAdapter.getCount(); i++) {
            totalEconomy += mEconomies.get(i, 0d);
        }

        mEconomyText.setText(String.format("%.2f", totalEconomy));

        showNextItem();
    }

    @Override
    public void onBackPressed() {
        int currentItem = mItemsPager.getCurrentItem();
        if (currentItem > 0) {
            mItemsPager.setCurrentItem(currentItem - 1);
        } else {
            super.onBackPressed();
        }
    }

    public void showNextItem() {
        if (mItemsPager.getCurrentItem() == mItemsAdapter.getCount() - 1) {
            mItemsAdapter.addPage();
        }
        mItemsPager.setCurrentItem(mItemsPager.getCurrentItem() + 1);
    }
}
