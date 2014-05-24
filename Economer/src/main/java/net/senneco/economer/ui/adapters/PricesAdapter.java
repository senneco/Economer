package net.senneco.economer.ui.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import net.senneco.economer.R;
import net.senneco.economer.data.Price;
import net.senneco.economer.ui.fragments.CalculatorFragment;

import java.util.*;

/**
 * Created by senneco on 24.05.2014
 */
public class PricesAdapter extends BaseAdapter implements View.OnClickListener {

    private static final Map<Price.Level, Integer> LEVEL_COLORS;

    private ArrayList<Price> mPrices;
    private CalculatorFragment mCalculatorFragment;

    static {
        LEVEL_COLORS = new HashMap<Price.Level, Integer>(5);
        LEVEL_COLORS.put(Price.Level.VERY_GOOD, R.color.very_good);
        LEVEL_COLORS.put(Price.Level.GOOD, R.color.good);
        LEVEL_COLORS.put(Price.Level.NORMAL, R.color.normal);
        LEVEL_COLORS.put(Price.Level.BAD, R.color.bad);
        LEVEL_COLORS.put(Price.Level.VERY_BAD, R.color.very_bad);
    }

    public PricesAdapter(CalculatorFragment calculatorFragment) {
        mCalculatorFragment = calculatorFragment;
        mPrices = new ArrayList<Price>();
    }

    public void addItems(List<Price> prices) {
        mPrices.addAll(prices);

        notifyDataSetChanged();
    }

    public void addItem(Price newPrice) {
        mPrices.add(newPrice);

        Collections.sort(mPrices);

        Price maxPrice = mPrices.get(mPrices.size() - 1);
        for (Price price : mPrices) {
            Price.EconomyCalculator.calc(price, maxPrice);
        }

        notifyDataSetChanged();
    }

    public ArrayList<Price> getPrices() {
        return mPrices;
    }

    @Override
    public int getCount() {
        return mPrices.size();
    }

    @Override
    public Price getItem(int position) {
        return mPrices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Context context = parent.getContext();

        ItemHolder holder;

        if (convertView == null) {
            convertView = View.inflate(context, R.layout.item_price, null);

            holder = new ItemHolder();
            holder.priceText = (TextView) convertView.findViewById(R.id.text_price);
            holder.sizeText = (TextView) convertView.findViewById(R.id.text_size);
            holder.economyText = (TextView) convertView.findViewById(R.id.text_economy);
            holder.acceptButton = (ImageButton) convertView.findViewById(R.id.butt_accept);

            holder.acceptButton.setOnClickListener(this);

            convertView.setTag(holder);
        } else {
            holder = (ItemHolder) convertView.getTag();
        }

        Price price = getItem(position);

        holder.priceText.setText(String.format("%.2f", price.getPrice()));
        holder.sizeText.setText(String.format("%.0f", price.getSize()));
        holder.economyText.setText(price.getEconomyPercents() > 0 ? String.format("-%.0f%%", price.getEconomyPercents()) : "");
        holder.acceptButton.setTag(price);

        Price.Level level = price.getLevel();
        int backgroundColorResId = LEVEL_COLORS.get(level);

        if (level == Price.Level.VERY_BAD) {
            if (mPrices.size() == 1) {
                backgroundColorResId = R.color.normal;
            } else if (mPrices.size() == 2) {
                backgroundColorResId = R.color.bad;
            }
        }

        //noinspection ConstantConditions
        convertView.setBackgroundColor(context.getResources().getColor(backgroundColorResId));

        return convertView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.butt_accept:
                mCalculatorFragment.choosePrice((Price) v.getTag());
                break;
        }
    }

    private class ItemHolder {
        TextView priceText;
        TextView sizeText;
        TextView economyText;
        ImageButton acceptButton;
    }
}
