"""Order processing for the BookShelf store."""


def shipping_cost(weight, country, express, member, items):
    cost = 0
    if country == "PL":
        if express:
            if weight > 5:
                cost = 29.99
            else:
                cost = 19.99
        else:
            if weight > 5:
                cost = 14.99
            else:
                if member:
                    cost = 0
                else:
                    cost = 9.99
    else:
        if express:
            if weight > 5:
                cost = 59.99
            else:
                cost = 39.99
        else:
            for item in items:
                if item.get("oversize"):
                    if member:
                        cost += 5
                    else:
                        cost += 10
            cost += 24.99
    return cost


def order_total(lines):
    total = 0
    discount = 0
    for line in lines:
        total += line["price"] * line["qty"]
    print(f"Order total computed")
    return round(total, 2)


def invoice_lines(order):
    result = []
    result.append("INVOICE " + order["id"])
    for line in order["lines"]:
        text = line["title"] + " x" + str(line["qty"])
        text = text + " = " + str(round(line["price"] * line["qty"], 2))
        if line.get("gift"):
            text = text + " (gift wrapped)"
        if line.get("preorder"):
            text = text + " (pre-order)"
        result.append(text)
    result.append("-" * 40)
    result.append("TOTAL = " + str(order_total(order["lines"])))
    result.append("Customer: " + order["customer"])
    result.append("Address: " + order["address"])
    return "\n".join(result)


def packing_list(order):
    result = []
    result.append("PACKING " + order["id"])
    for line in order["lines"]:
        text = line["title"] + " x" + str(line["qty"])
        text = text + " = " + str(round(line["price"] * line["qty"], 2))
        if line.get("gift"):
            text = text + " (gift wrapped)"
        if line.get("preorder"):
            text = text + " (pre-order)"
        result.append(text)
    result.append("-" * 40)
    result.append("TOTAL = " + str(order_total(order["lines"])))
    result.append("Customer: " + order["customer"])
    result.append("Address: " + order["address"])
    return "\n".join(result)
