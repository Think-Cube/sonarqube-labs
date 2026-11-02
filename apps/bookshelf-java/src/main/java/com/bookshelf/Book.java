package com.bookshelf;

/**
 * A book in the BookShelf catalog.
 */
public record Book(String isbn, String title, String author, double price, int stock) {
}
