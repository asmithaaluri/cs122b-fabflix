DROP PROCEDURE IF EXISTS add_star;
DROP PROCEDURE IF EXISTS add_genre;
DROP PROCEDURE IF EXISTS add_movie;

DELIMITER //
CREATE PROCEDURE add_star(star VARCHAR(100), birthYear INTEGER, OUT starId VARCHAR(10))
    BEGIN
        SET @lastStoredId = (SELECT id FROM next_star_id LIMIT 1);
        SET @starMatchingId = (SELECT id FROM stars WHERE id = CAST(@lastStoredId AS CHAR(10)));
        SET @nextId = @lastStoredId;
        WHILE (@starMatchingId IS NOT NULL) DO
              SET @nextId = @nextId + 1;
              SET @starMatchingId = (SELECT id FROM stars WHERE id = CAST(@nextId AS CHAR(10)));
        END WHILE;

        INSERT INTO stars (id, name, birthYear)
        VALUES (CAST(@nextId AS CHAR(10)), star, birthYear);
        SELECT CAST(@nextId AS CHAR(10)) INTO starId;
        SET @nextId = @nextId + 1;
        UPDATE next_star_id SET id = @nextId WHERE id = @lastStoredId;
    END //

CREATE PROCEDURE add_genre(genreName VARCHAR(32), OUT genreId INTEGER)
    BEGIN
        INSERT INTO genres(name)
        VALUES (genreName);
        SELECT LAST_INSERT_ID() INTO genreId;
    END //

CREATE PROCEDURE add_movie(
    title VARCHAR(100),
    year INT,
    director VARCHAR(100),
    star VARCHAR(100),
    birthYear INTEGER,
    genre VARCHAR(32),
    OUT movieId VARCHAR(10),
    OUT starId VARCHAR(10),
    OUT genreId INTEGER,
    OUT added BIT(1)
)
    add_movie_label: BEGIN
        SET @movieMatch = (SELECT id
                           FROM movies m
                           WHERE m.title = title AND m.year = year AND m.director = director
                           LIMIT 1);
        IF (@movieMatch IS NOT NULL) THEN
            SELECT NULL INTO movieId;
            SELECT NULL INTO starId;
            SELECT NULL INTO genreId;
            SELECT FALSE INTO added;
            LEAVE add_movie_label;
        END IF;

        SET @lastStoredId = (SELECT id FROM next_movie_id LIMIT 1);
        SET @movieMatchingId = (SELECT id FROM movies WHERE id = CAST(@lastStoredId AS CHAR(10)));
        SET @nextId = @lastStoredId;
        WHILE (@movieMatchingId IS NOT NULL) DO
              SET @nextId = @nextId + 1;
              SET @movieMatchingId = (SELECT id FROM movies WHERE id = CAST(@nextId AS CHAR(10)));
        END WHILE;

        INSERT INTO movies (id, title, year, director)
        VALUES (CAST(@nextId AS CHAR(10)), title, year, director);
        SET @movieId = CAST(@nextId AS CHAR(10));
        SELECT @movieId INTO movieId;
        SET @nextId = @nextId + 1;
        UPDATE next_movie_id SET id = @nextId WHERE id = @lastStoredId;

        SET @starMatch = (SELECT id FROM stars WHERE stars.name = star LIMIT 1);
        SET @genreMatch = (SELECT id FROM genres WHERE genres.name = genre LIMIT 1);
        IF (@starMatch IS NULL) THEN
           CALL add_star(star, birthYear, @starMatch);
        END IF;
        IF (@genreMatch IS NULL) THEN
           CALL add_genre(genre, @genreMatch);
        END IF;

        INSERT INTO stars_in_movies (starId, movieId)
        VALUES (@starMatch, @movieId);

        INSERT INTO genres_in_movies(genreId, movieId)
        VALUES (@genreMatch, @movieId);

        SELECT @starMatch INTO starId;
        SELECT @genreMatch INTO genreId;
        SELECT TRUE INTO added;
    END //

DELIMITER ;