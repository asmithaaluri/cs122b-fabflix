# CS122B Fabflix
## By Asmitha Aluri and Rebecca Su

## Table of Contents
1. [Project 1](#Project-1)
2. [Project 2](#Project-2)
3. Project 3
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
### [Project 2 Demo Video](https://drive.google.com/file/d/1Kg39Cnh_RcR0HtGa23J1OwdRbqC_OA6-/view?usp=sharing)
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
