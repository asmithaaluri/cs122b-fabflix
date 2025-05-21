DROP DATABASE IF EXISTS moviedb;
CREATE DATABASE moviedb;
USE moviedb;

CREATE TABLE movies (
    id VARCHAR(10) PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    year INTEGER NOT NULL,
    director VARCHAR(100) NOT NULL,
    FULLTEXT idx (title)
);

CREATE TABLE stars (
    id VARCHAR(10) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    birthYear INTEGER
);

CREATE TABLE stars_in_movies (
    starId VARCHAR(10) NOT NULL,
    movieId VARCHAR(10) NOT NULL,
    PRIMARY KEY (starId, movieId),
    FOREIGN KEY (starId) REFERENCES stars(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE genres (
    id INTEGER AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(32) NOT NULL
);

CREATE TABLE genres_in_movies (
    genreId INTEGER NOT NULL,
    movieId VARCHAR(10) NOT NULL,
    PRIMARY KEY (genreId, movieId),
    FOREIGN KEY (genreId) REFERENCES genres(id),
    FOREIGN KEY (movieId) REFERENCES movies(id)
);

CREATE TABLE creditcards (
    id VARCHAR(20) PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    expiration DATE NOT NULL
);

CREATE TABLE customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    firstName VARCHAR(50) NOT NULL,
    lastName VARCHAR(50) NOT NULL,
    ccld VARCHAR(20) NOT NULL,
    FOREIGN KEY (ccld) REFERENCES creditcards(id),
    address VARCHAR(200) NOT NULL,
    email VARCHAR(50) NOT NULL,
    password VARCHAR(20) NOT NULL
);

CREATE TABLE sales (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customerId INT NOT NULL,
    FOREIGN KEY (customerId) REFERENCES customers(id),
    movieId VARCHAR(10) NOT NULL,
    FOREIGN KEY (movieId) REFERENCES movies (id),
    saleDate DATE NOT NULL
--     quantity INT NOT NULL DEFAULT 1
);

CREATE TABLE ratings (
    movieId VARCHAR(10) NOT NULL PRIMARY KEY,
    FOREIGN KEY (movieId) REFERENCES movies(id),
    rating FLOAT NOT NULL,
    numVotes INT NOT NULL
);

CREATE TABLE employees (
    email VARCHAR(50) PRIMARY KEY,
    password VARCHAR(20) NOT NULL,
    fullname VARCHAR(100)
);

CREATE TABLE next_movie_id (
    id INTEGER PRIMARY KEY
);

CREATE TABLE next_star_id (
    id INTEGER PRIMARY KEY
);