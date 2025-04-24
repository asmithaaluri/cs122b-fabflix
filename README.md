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
### Project 2 Demo Video (to be linked)
### LIKE/ILIKE Predicate
- We didn't use the `ILIKE` predicate
- For searching, we used the `LIKE` predicate for each of the following string parameters (listed in next line) when they were non-null and non-empty
   - String parameters: movie title, movie year, movie director, star name
   - We implemented substring matching by enclosing the string parameter given with '%'
      - For example, the keyword "term", when specified in the title would result in the predicate `LIKE '%term%'` 
   - Queries are located in `MovieSearchServlet.java`
- For browsing, we used the `LIKE` predicate for the prefix
   - To browse by a character prefix, we used `LIKE LOWER(?)`, where ? would be replaced with the concatenation of the parameter and '%'
### Project 2 Contributions
|Name|Contributions|
|---|------------|
|Asmitha|<ul><li>Task 1</li><li>Task 2: searching, browsing by genre and first character</li><li>Task 4</li></ul>|
|Rebecca|<ul><li>Task 2: browsing by '*'</li><li>Task 3</li></ul>|
