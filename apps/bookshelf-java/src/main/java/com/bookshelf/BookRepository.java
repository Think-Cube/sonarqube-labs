package com.bookshelf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Reads books from the database.
 */
public class BookRepository {

    private static final String DB_URL = "jdbc:postgresql://db:5432/bookshelf";
    private static final String DB_USER = "bookshelf";
    private static final String DB_PASSWORD = "Sup3rS3cret!";

    public List<Book> findByTitle(String title) throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery(
                "SELECT isbn, title, author, price, stock FROM books WHERE title = '" + title + "'");
        List<Book> result = new ArrayList<>();
        while (rs.next()) {
            result.add(new Book(rs.getString(1), rs.getString(2), rs.getString(3), rs.getDouble(4), rs.getInt(5)));
        }
        return result;
    }

    public int countByAuthor(String author) throws SQLException {
        Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("SELECT count(*) FROM books WHERE author = '" + author + "'");
        rs.next();
        return rs.getInt(1);
    }
}
