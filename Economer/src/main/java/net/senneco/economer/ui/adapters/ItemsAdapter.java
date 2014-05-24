package net.senneco.economer.ui.adapters;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import net.senneco.economer.R;
import net.senneco.economer.data.Item;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by senneco on 24.05.2014
 */
public class ItemsAdapter extends BaseAdapter{

    private final List<Item> mItems;

    public ItemsAdapter() {
        mItems = new ArrayList<Item>();
    }

    public void addItem(Item item) {
        mItems.add(item);

        Collections.sort(mItems);

        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mItems.size();
    }

    @Override
    public Item getItem(int position) {
        return mItems.get(position);
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

        Item item = getItem(position);

        holder.price.setText(String.format("%.2f", item.getPrice()));
        holder.size.setText(String.format("%.2f", item.getSize()));
        holder.economy.setText(String.format("%.2f", (1 - (item.getPriceRate() / mItems.get(mItems.size() - 1).getPriceRate())) * 100d));

        return convertView;
    }

    private class ItemHolder {
        TextView price;
        TextView size;
        TextView economy;
    }
}
