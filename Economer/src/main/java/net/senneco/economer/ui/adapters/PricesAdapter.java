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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by senneco on 24.05.2014
 */
public class PricesAdapter extends BaseAdapter implements View.OnClickListener {

    private final List<Price> mPrices;
    private CalculatorFragment mCalculatorFragment;

    public PricesAdapter(CalculatorFragment calculatorFragment) {
        mCalculatorFragment = calculatorFragment;
        mPrices = new ArrayList<Price>();
    }

    public void addItem(Price price) {
        mPrices.add(price);

        Collections.sort(mPrices);

        notifyDataSetChanged();
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

        double economy = (1 - (price.getPriceRate() / mPrices.get(mPrices.size() - 1).getPriceRate())) * 100d;

        holder.priceText.setText(String.format("%.2f", price.getPrice()));
        holder.sizeText.setText(String.format("%.0f", price.getSize()));
        holder.economyText.setText(economy > 0 ? String.format("-%.0f%%", economy) : "");
        holder.acceptButton.setTag(price);

        int backgroundColorResId;
        if (economy > 13) {
            backgroundColorResId = R.color.very_good;
        } else if (economy > 8) {
            backgroundColorResId = R.color.good;
        } else if (economy > 3) {
            backgroundColorResId = R.color.normal;
        } else if (economy > 1) {
            backgroundColorResId = R.color.bad;
        } else if (mPrices.size() > 2) {
            backgroundColorResId = R.color.very_bad;
        } else if (mPrices.size() > 1) {
            backgroundColorResId = R.color.bad;
        } else {
            backgroundColorResId = R.color.normal;
        }



        //noinspection ConstantConditions
        convertView.setBackgroundColor(context.getResources().getColor(backgroundColorResId));

        return convertView;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.butt_accept:
                mCalculatorFragment.choosePrice((Price) v.getTag(), mPrices.get(mPrices.size() - 1));
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
