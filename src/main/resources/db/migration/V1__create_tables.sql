-- sql
CREATE TABLE authors (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  dob DATE NOT NULL CHECK (dob < CURRENT_DATE)
);

CREATE TABLE books (
  id SERIAL PRIMARY KEY,
  title TEXT NOT NULL,
  price NUMERIC NOT NULL CHECK (price >= 0),
  published BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE TABLE book_authors (
  book_id INT NOT NULL REFERENCES books(id) ON DELETE CASCADE,
  author_id INT NOT NULL REFERENCES authors(id) ON DELETE CASCADE,
  PRIMARY KEY (book_id, author_id)
);