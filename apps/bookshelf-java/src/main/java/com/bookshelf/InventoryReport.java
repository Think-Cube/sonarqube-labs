package com.bookshelf;

import java.util.List;

/**
 * Text reports for the warehouse team.
 */
public class InventoryReport {

    private int generatedReports;

    public String lowStock(List<Book> books, int threshold) {
        StringBuilder report = new StringBuilder();
        report.append("=== LOW STOCK ===\n");
        int count = 0;
        for (Book book : books) {
            if (book.stock() > 0 && book.stock() < threshold) {
                report.append(book.isbn()).append(" | ")
                      .append(book.title()).append(" | ")
                      .append(book.author()).append(" | ")
                      .append(book.stock()).append(" left\n");
                count++;
            }
        }
        report.append("Total: ").append(count).append(" titles\n");
        System.out.println(report);
        return report.toString();
    }

    public String outOfStock(List<Book> books, int threshold) {
        StringBuilder report = new StringBuilder();
        report.append("=== OUT OF STOCK ===\n");
        int count = 0;
        for (Book book : books) {
            if (book.stock() > 0 && book.stock() < threshold) {
                report.append(book.isbn()).append(" | ")
                      .append(book.title()).append(" | ")
                      .append(book.author()).append(" | ")
                      .append(book.stock()).append(" left\n");
                count++;
            }
        }
        report.append("Total: ").append(count).append(" titles\n");
        System.out.println(report);
        return report.toString();
    }
}
