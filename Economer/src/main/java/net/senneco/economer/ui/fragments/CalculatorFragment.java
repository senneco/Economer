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
import net.senneco.economer.data.Price;
import net.senneco.economer.ui.activities.MainActivity;
import net.senneco.economer.ui.adapters.PricesAdapter;

/**
 * A simple {@link Fragment} subclass.
 *
 */
public class CalculatorFragment extends Fragment implements View.OnClickListener {

    public static final String PRICES = "prices";

    private PricesAdapter mPricesAdapter;
    private EditText mPriceEdit;
    private EditText mSizeEdit;

    public CalculatorFragment() {
        // pass
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_calculator, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPriceEdit = (EditText) view.findViewById(R.id.edit_price);
        mSizeEdit = (EditText) view.findViewById(R.id.edit_size);
        ImageButton addButton = (ImageButton) view.findViewById(R.id.butt_add);
        addButton.setOnClickListener(this);

        mPricesAdapter = new PricesAdapter(this);

        ListView itemsList = (ListView) view.findViewById(R.id.list_prices);
        itemsList.setAdapter(mPricesAdapter);

        if (savedInstanceState != null && savedInstanceState.containsKey(PRICES)) {
            //noinspection unchecked
            mPricesAdapter.addItems((java.util.List<Price>) savedInstanceState.getSerializable(PRICES));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(PRICES, mPricesAdapter.getPrices());
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.butt_add) {
            Price price = new Price();
            price.setPrice(Double.parseDouble(mPriceEdit.getText().toString()));
            price.setSize(Double.parseDouble(mSizeEdit.getText().toString()));

            mPricesAdapter.addItem(price);
        }
    }

    public void choosePrice(Price price) {
        ((MainActivity) getActivity()).choosePrice(price);
    }
}
