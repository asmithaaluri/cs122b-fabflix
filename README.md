# CS122B Fabflix
## By Asmitha Aluri and Rebecca Su

## Table of Contents
1. [Project 1](#Project-1)
2. [Project 2](#Project-2)
3. [Project 3](#Project-3)
4. Project 4
5. Project 5

## Project 1
### [Project 1 Demo Video](https://drive.google.com/file/d/1rMphVmC_LnNFGCZipHyd4ApsRIuF9lG_/view?usp=sharing)
### Project 1 Contributions
|Name|Contributions|
|---|------------|
|Asmitha|<ul><li>Create table queries</li><li>Top 20 movie page: linking movies, fetching movie info</li><li>Individual movie page: fetching movie info</li><li>Individual star page: fetching star info</li><li>Styling/CSS</li><li>AWS instance setup</li><li>Deployment on AWS</li></ul>|
|Rebecca|<ul><li>Create table queries</li><li>Top 20 movie page: linking stars, getting rating</li><li>Individual movie page: fetching genres, fetching/linking stars</li><li>Individual star page: linking movies</li><li>Navbar</li><li>Troubleshooting Tomcat/AWS deployment</li></ul>|

## Project 2
### [Project 2 Demo Video](https://drive.google.com/file/d/1RRXVhAHh35Z9tmKUvW6T2JZIGjdG_iQs/view?usp=sharing)
#### Extra Features
- Logout

### LIKE/ILIKE Predicate
- We didn't use the `ILIKE` predicate
- For searching, we used the `LIKE` predicate for each of the following string parameters (listed in next line) when they were non-null and non-empty
   - String parameters: movie title, movie director, star name
   - We implemented substring matching by enclosing the string parameter given (inputted by the user) with '%'
      - For example, the keyword "term", when specified in the title would result in the predicate `LIKE '%term%'` 
   - Substring matching portion of queries are located in [SearchUtility.java](https://github.com/uci-jherold2-2025spring-cs122b/2025-spring-cs-122b-aa-rs/blob/main/src/SearchUtility.java)
- For browsing, we used the `LIKE` predicate for the prefix
   - To browse by a character prefix, in [MovieList.java](https://github.com/uci-jherold2-2025spring-cs122b/2025-spring-cs-122b-aa-rs/blob/09da8cb8b2425b74198b817f802ecddd13cdfb61/src/MovieListServlet.java#L107), we used `LIKE ?`, where ? would be replaced with the character parameter (a single letter or digit)
      - Full clause used: `LEFT(m.title, 1) LIKE ?` to check only the first character of the title
     
### Project 2 Contributions
|Name|Contributions|
|---|------------|
|Asmitha|<ul><li>Task 1</li><li>Task 2: searching, browsing by genre and first character</li><li>Task 4</li></ul>|
|Rebecca|<ul><li>Task 2: browsing by '*'</li><li>Task 3</li><li>Logout</li></ul>|

## Project 3
### [Project 3 Demo Video](https://drive.google.com/file/d/13nZTAuTUUNf6nHwHPdONy6IuQvAe6uqH/view?usp=sharing)

### Filenames with Prepared Statements
- DashboardLoginServlet.java
- LoginServlet.java
- MetadataServlet.java
- MovieListServlet.java
- MovieSearchServlet.java
   - SearchUtility.java (helper function with PreparedStatement parameter) 
- PlaceOrderServlet.java
- ShoppingCartServlet.java
- SingleMovieServlet.java
- SingleStarServlet.java
- DatabaseModificationsFromXML.java

### Inconsistency Report Summary
<img width="303" alt="image" src="https://github.com/user-attachments/assets/b39aeb95-904d-4509-9843-6a807c03cb88" />


### Parsing Procedures
- "Skipped" means that we did not consider it valid data and did not insert in our database
1. Parse `actors.xml` -> [Inconsistency Report](https://github.com/uci-jherold2-2025spring-cs122b/2025-spring-cs-122b-aa-rs/blob/main/inconsistent_star_info.txt)

- We uniquely identify actors by their name (two actors with the same name and different birth year are considered inconsistencies rather than two different actors)

|Inconsistency|How We Handled|
|---------------|-----------------|
| Empty star names | Skipped and counted as inconsistency |
| Duplicate stars with different birth years | Duplicates are skipped and counted as inconsistency |
| Duplicate stars with same birth year | Duplicates are skipped |
| Duplicate stars with null birth year, appearing before non-null birth year | Birth year is overwritten with non-null |
| Duplicate stars with null birth year, appearing after non-null birth year | Skipped and counted as inconsistency |

- Produces hash map of actors (deemed "valid" so far)

2. Parse `casts.xml` -> [Inconsistency Report](https://github.com/uci-jherold2-2025spring-cs122b/2025-spring-cs-122b-aa-rs/blob/main/inconsistent_cast_info.txt)

| Inconsistency | How We Handled |
|---------------|-----------------|
| Actors appearing in the hash map of actors, but not in `casts.xml` | Counted as inconsistency |
| Actors appearing in `casts.xml` but does not exist in the hash map of actors | Skipped and counted as inconsistency |

- Produces hash map of actors appearing in both `actors.xml` and `casts.xml` (subset of actors from parsing `actors.xml`)
- Produces set of movies which have associated actor information

4. Parse `mains.xml` -> [Inconsistency Report](https://github.com/uci-jherold2-2025spring-cs122b/2025-spring-cs-122b-aa-rs/blob/main/inconsistent_movie_info.txt)

| Inconsistency | How We Handled |
|---------------|-----------------|
| Duplicate movie IDs | Skipped and counted as inconsistency |
| Empty movie title | Skipped and counted as inconsistency |
| Invalid movie year | Skipped and counted as inconsistency |
| Unknown movie director | Skipped and counted as inconsistency |
| Genres with invalid formatting | Still add the movie, but set the genre to NULL|

#### Additional Inconsistencies
- Parsed data without ending tags are overwritten

### Parsing Time Optimization Strategies
1. Parsing order
   - We chose to parse the XML files in the following order: actors, then casts, and lastly movies.
   - Parsing smaller files first allowed us to keep a smaller set of potentially valid information to compare with newly parsed info
   - By parsing actors first, which was smaller than the casts file, we started off with a smaller number of potentially valid stars than parsing casts before actors
      - Our approach: Based on the number of `<stagename>` tags, `actors.xml` indicated that the possible # of valid actors was capped at around 6.8k. When we parsed casts, we checked if each actor listed in `casts.xml` was in the hashmap of valid actors from `actors.xml` to build a hashmap of valid actors appearing in both `actors.xml` and `casts.xml`
      - Our approach is more efficient than parsing `casts.xml` before `actors.xml` because if we had parsed `casts.xml` first, when parsing `actors.xml`, we woould have to check if each actor is listed in the hashmap of valid actors from `casts.xml`, which could have been over 10k.
   - Similar to how we parsed `actors` before `casts`, parsing `movies` after `actors` also allowed us to optimize parsing time
      - `movies.xml` was the largest file and had over 10k films
          - By building a set of valid movies based on the valid actors found in `actors` and `casts`, we were able to filter out the films parsed from `movies.xml`. This was more efficient than if we were to parse `movies.xml` first, which would result in a very large amount of data we would then need to compare with `actors.xml` and `casts.xml` to identify inconsistencies
- Time reduction: 5.3 minutes -> 1.53 minutes
   - This time reduction included treating actors in `casts.xml` as inconsistencies if they didn't appear in `actors.xml`  
2. Used sets and hashmaps to prevent duplicates when storing preliminary results after parsing a file and identify inconsistencies (ie movies that showed up in one file but not another)
3. MySQL batch processing when inserting XML data into `moviedb`
4. Multithreaded insertion of movies parsed from the XML files
- Time reduction: 1.53 minutes -> 1.49 minutes

### Project 3 Contributions
|Name|Contributions|
|---|------------|
|Asmitha|<ul><li>Task 2</li><li>Task 3</li><li>Task 6</li></ul>|
|Rebecca|<ul><li>Task 1</li><li>Task 3</li><li>Task 4</li><li>Task 5</li><li>Full-text indexes, multithreading for task 6</li></ul>|
