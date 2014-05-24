package net.senneco.economer.ui.fragments;



import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import net.senneco.economer.R;
import net.senneco.economer.data.Item;
import net.senneco.economer.ui.adapters.ItemsAdapter;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class CalculatorFragment extends Fragment implements View.OnClickListener {


    private ItemsAdapter mItemsAdapter;
    private EditText mPriceEdit;
    private EditText mSizeEdit;

    public CalculatorFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calculator, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPriceEdit = (EditText) view.findViewById(R.id.edit_price);
        mSizeEdit = (EditText) view.findViewById(R.id.edit_size);
        ImageButton addButton = (ImageButton) view.findViewById(R.id.butt_add);
        addButton.setOnClickListener(this);

        mItemsAdapter = new ItemsAdapter();

        ListView itemsList = (ListView) view.findViewById(R.id.list_prices);
        itemsList.setAdapter(mItemsAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.butt_add) {
            Item item = new Item();
            item.setPrice(Double.parseDouble(mPriceEdit.getText().toString()));
            item.setSize(Double.parseDouble(mSizeEdit.getText().toString()));

            mItemsAdapter.addItem(item);
        }
    }
}
