package net.senneco.economer.data;

/**
 * Created by senneco on 24.05.2014
 */
public class Item implements Comparable<Item> {
    private double mPrice;
    private double mSize;

    public double getPrice() {
        return mPrice;
    }

    public void setPrice(double price) {
        mPrice = price;
    }

    public double getSize() {
        return mSize;
    }

    public void setSize(double size) {
        mSize = size;
    }

    public double getPriceRate() {
        return mPrice / mSize;
    }

    @Override
    public int compareTo(Item another) {
        if (another == null) return -1;

        double result = mPrice / mSize - another.getPrice() / another.getSize();
        return result < 0 ? -1 : result > 0 ? 1 : 0;
    }
}
