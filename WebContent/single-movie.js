/**
 * Retrieve parameter from request URL, matching by parameter name
 * @param target String
 * @returns {*}
 */
function getParameterByName(target) {
    // Get request URL
    let url = window.location.href;
    // Encode target parameter name to url encoding
    target = target.replace(/[\[\]]/g, "\\$&");

    // Ues regular expression to find matched parameter value
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)"),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';

    // Return the decoded parameter value
    return decodeURIComponent(results[2].replace(/\+/g, " "));
}

/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    console.log("handleResult: populating movie info from movieData");
    let movieInfo = jQuery("#movie_info");
    movieInfo.append("<h2>Movie Title: " + resultData[0]["movie_title"] + "</h2>" +
        "<h5>Movie Year: " + resultData[0]["movie_year"] + "</h5>" +
        "<h5>Movie Director: " + resultData[0]["movie_director"] + "</h5>" +
        "<h5>Movie Rating: " + resultData[0]["movie_rating"] + "</h5>");

    let movieGenres = jQuery("#movie_genres");
    let genreHTML = "<p>Movie Genre(s): ";
    let genres = resultData[0]['genres'];
    let numGenres = genres.length;
    if (numGenres === 0){
        genreHTML += "N/A";
    } else {
        genreHTML += genres[0];
        for (let i = 1; i < numGenres; i++) {
            genreHTML += ", " + genres[i];
        }
    }
    genreHTML += "</p>";
    movieGenres.append(genreHTML);

    let movieStars = jQuery("#movie_stars");
    let starsHTML = "<p>Movie Star(s): ";
    let stars = resultData[0]['stars'];
    let starIds = resultData[0]['star_ids'];
    let numStars = stars.length;
    if (numStars === 0){
        starsHTML += "N/A";
    } else {
        starsHTML += '<a href="single-star.html?id=' + starIds[0] + '">' +
                    stars[0] +
                    '</a>';
        for (let i = 1; i < numStars; i++) {
            starsHTML += ", " + '<a href="single-star.html?id=' + starIds[i] + '">' +
            stars[i] +
            '</a>';
        }
    }
    starsHTML += "</p>";
    movieStars.append(starsHTML);
}

/**
 * Once this .js is loaded, following scripts will be executed by the browser\
 */

// Get id from URL
let movie_id = getParameterByName('id');

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/single-movie?id=" + movie_id, // Setting request url, which is mapped by StarsServlet in Stars.java
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});