package net.senneco.economer.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import net.senneco.economer.R;
import net.senneco.economer.data.Price;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by senneco on 24.05.2014
 */
public class PricesAdapter extends BaseAdapter{

    private final List<Price> mPrices;

    public PricesAdapter() {
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
        ItemHolder holder;

        if (convertView == null) {
            convertView = View.inflate(parent.getContext(), R.layout.item_price, null);

            holder = new ItemHolder();
            holder.price = (TextView) convertView.findViewById(R.id.text_price);
            holder.size = (TextView) convertView.findViewById(R.id.text_size);
            holder.economy = (TextView) convertView.findViewById(R.id.text_economy);

            convertView.setTag(holder);
        } else {
            holder = (ItemHolder) convertView.getTag();
        }

        Price price = getItem(position);

        double economy = (1 - (price.getPriceRate() / mPrices.get(mPrices.size() - 1).getPriceRate())) * 100d;

        holder.price.setText(String.format("%.2f", price.getPrice()));
        holder.size.setText(String.format("%.0f", price.getSize()));
        holder.economy.setText(economy > 0 ? String.format("-%.0f%%", economy) : "");

        return convertView;
    }

    private class ItemHolder {
        TextView price;
        TextView size;
        TextView economy;
    }
}
