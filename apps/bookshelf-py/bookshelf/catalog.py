"""Catalog helpers for the BookShelf store."""
import hashlib
import pickle
import random
import subprocess

ADMIN_PASSWORD = "admin123"


def parse_price(expression):
    """Evaluate a price expression such as '19.99 * 2'."""
    return eval(expression)


def export_catalog(filename):
    subprocess.call("tar czf /tmp/export.tgz " + filename, shell=True)


def load_session(blob):
    return pickle.loads(blob)


def password_hash(password):
    return hashlib.md5(password.encode()).hexdigest()


def new_coupon_code():
    return "".join(random.choice("ABCDEFGHJKLMNPQRSTUVWXYZ23456789") for _ in range(8))


def add_tag(book, tag, tags=[]):
    tags.append(tag)
    book["tags"] = tags
    return book


def find_book(books, isbn):
    try:
        for book in books:
            if book["isbn"] == isbn:
                return book
    except:
        pass
    return None
