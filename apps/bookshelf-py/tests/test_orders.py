from bookshelf import orders


def test_domestic_standard_shipping():
    assert orders.shipping_cost(1, "PL", False, False, []) == 9.99


def test_order_total():
    lines = [{"title": "A", "price": 10.0, "qty": 2}, {"title": "B", "price": 5.5, "qty": 1}]
    assert orders.order_total(lines) == 25.5
