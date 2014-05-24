package net.senneco.economer.data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by senneco on 24.05.2014
 */
public class Price implements Comparable<Price>, Serializable {
    private double mPrice;
    private double mSize;
    private double mEconomyPercents;
    private double mEconomy;
    private Level mLevel;

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

    public double getEconomyPercents() {
        return mEconomyPercents;
    }

    public void setEconomyPercents(double economyPercents) {
        mEconomyPercents = economyPercents;
    }

    public double getEconomy() {
        return mEconomy;
    }

    public void setEconomy(double economy) {
        mEconomy = economy;
    }

    public Level getLevel() {
        return mLevel;
    }

    public void setLevel(Level level) {
        mLevel = level;
    }

    public double getPriceRate() {
        return mPrice / mSize;
    }

    @Override
    public int compareTo(Price another) {
        if (another == null) return -1;

        double result = mPrice / mSize - another.getPrice() / another.getSize();
        return result < 0 ? -1 : result > 0 ? 1 : 0;
    }

    public static enum Level {
        VERY_GOOD,
        GOOD,
        NORMAL,
        BAD,
        VERY_BAD
    }

    public static class EconomyCalculator {
        public static void calc(Price price, List<Price> prices) {

            Price maxPrice = prices.get(prices.size() - 1);

            double economyPercents = (1 - (price.getPriceRate() / maxPrice.getPriceRate())) * 100d;

            price.setEconomyPercents(economyPercents);
            price.setEconomy(price.getSize() * (maxPrice.getPriceRate() - price.getPriceRate()));

            Level level;
            if (economyPercents > 13) {
                level = Level.VERY_GOOD;
            } else if (economyPercents > 8) {
                level = Level.GOOD;
            } else if (economyPercents > 3 || prices.size() == 1) {
                level = Level.NORMAL;
            } else if (economyPercents > 1 || prices.size() < 4) {
                level = Level.BAD;
            } else {
                level = Level.VERY_BAD;
            }

            price.setLevel(level);
        }
    }
}
