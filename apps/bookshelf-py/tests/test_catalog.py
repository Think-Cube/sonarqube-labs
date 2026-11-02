from bookshelf import catalog


def test_parse_price():
    assert catalog.parse_price("10 * 2") == 20


def test_password_hash_is_hex():
    assert catalog.password_hash("abc") == "900150983cd24fb0d6963f7d28e17f72"


def test_find_book():
    books = [{"isbn": "1", "title": "A"}, {"isbn": "2", "title": "B"}]
    assert catalog.find_book(books, "2")["title"] == "B"
    assert catalog.find_book(books, "3") is None
