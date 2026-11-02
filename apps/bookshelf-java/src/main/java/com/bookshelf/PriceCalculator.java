package com.bookshelf;

/**
 * Pricing rules for the shop.
 */
public class PriceCalculator {

    public double discountedPrice(Book book, String customerType, int quantity, boolean holiday) {
        double price = book.price() * quantity;
        if (customerType == "VIP") {
            if (quantity > 10) {
                if (holiday) {
                    price = price * 0.7;
                } else {
                    price = price * 0.8;
                }
            } else {
                if (holiday) {
                    price = price * 0.85;
                } else {
                    price = price * 0.9;
                }
            }
        } else if (customerType.equals("MEMBER")) {
            if (quantity > 10) {
                if (holiday) {
                    price = price * 0.85;
                } else {
                    price = price * 0.9;
                }
            } else if (quantity > 5) {
                price = price * 0.95;
            }
        } else {
            for (int i = 0; i < quantity; i++) {
                if (i > 20) {
                    if (holiday) {
                        price = price - 0.1;
                    }
                }
            }
        }
        return price;
    }

    public double averagePrice(int totalCents, int count) {
        double average = totalCents / count;
        return average / 100;
    }

    public double shippingCost(double weightKg, String country) {
        // double express = weightKg * 3.5;
        // return express;
        if (country.equals("PL")) {
            return weightKg < 1 ? 9.99 : 14.99;
        }
        return weightKg < 1 ? 19.99 : 29.99;
    }
}
