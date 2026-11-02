package com.bookshelf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PriceCalculatorTest {

    private final PriceCalculator calculator = new PriceCalculator();
    private final Book book = new Book("978-0-00-000000-1", "Clean Code", "R. Martin", 10.0, 5);

    @Test
    void vipGetsTenPercentOff() {
        assertEquals(90.0, calculator.discountedPrice(book, "VIP", 10, false), 0.001);
    }

    @Test
    void memberGetsFivePercentOffAboveFiveItems() {
        assertEquals(57.0, calculator.discountedPrice(book, "MEMBER", 6, false), 0.001);
    }

    @Test
    void domesticShippingIsCheaper() {
        assertEquals(9.99, calculator.shippingCost(0.5, "PL"), 0.001);
    }
}
