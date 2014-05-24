package net.senneco.economer.ui.activities;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.util.SparseArray;
import android.view.View;
import android.widget.TextView;
import net.senneco.economer.R;
import net.senneco.economer.data.Price;
import net.senneco.economer.ui.adapters.ItemsAdapter;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends ActionBarActivity {

    private static final Map<Price.Level, Integer> LEVEL_COLORS;

    private ItemsAdapter mItemsAdapter;
    private ViewPager mItemsPager;
    private SparseArray<Double> mEconomies;
    private SparseArray<Price.Level> mLevels;
    private View mEconomyBackgroundView;
    private TextView mEconomyText;

    static {
        LEVEL_COLORS = new HashMap<Price.Level, Integer>(5);
        LEVEL_COLORS.put(Price.Level.VERY_GOOD, R.color.very_good);
        LEVEL_COLORS.put(Price.Level.GOOD, R.color.good);
        LEVEL_COLORS.put(Price.Level.NORMAL, R.color.normal);
        LEVEL_COLORS.put(Price.Level.BAD, R.color.bad);
        LEVEL_COLORS.put(Price.Level.VERY_BAD, R.color.very_bad);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEconomies = new SparseArray<Double>();
        mLevels = new SparseArray<Price.Level>();

        mEconomyBackgroundView = findViewById(R.id.view_economy_background);
        mEconomyText = (TextView) findViewById(R.id.text_economy);

        mItemsAdapter = new ItemsAdapter(getSupportFragmentManager());

        mItemsPager = (ViewPager) findViewById(R.id.pager_items);
        mItemsPager.setAdapter(mItemsAdapter);
        mItemsPager.setPageMargin(10);
    }

    public void choosePrice(Price price) {
        mEconomies.put(mItemsPager.getCurrentItem(), price.getEconomy());
        mLevels.put(mItemsPager.getCurrentItem(), price.getLevel());

        double totalEconomy = 0;
        HashMap<Price.Level, Integer> levelsCounter = new HashMap<Price.Level, Integer>();
        for (Price.Level level : Price.Level.values()) {
            levelsCounter.put(level, 0);
        }

        for (int i = 0; i < mItemsAdapter.getCount(); i++) {
            totalEconomy += mEconomies.get(i, 0d);
            Price.Level level = mLevels.get(i);
            levelsCounter.put(level, levelsCounter.get(level) + 1);
        }

        Map.Entry<Price.Level, Integer> popularLevel = null;
        for (Map.Entry<Price.Level, Integer> levelCounter : levelsCounter.entrySet()) {
            if (popularLevel == null || levelCounter.getValue() >= popularLevel.getValue()) {
                popularLevel = levelCounter;
            }
        }

        mEconomyBackgroundView.setBackgroundColor(getResources().getColor(LEVEL_COLORS.get(popularLevel.getKey())));
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
