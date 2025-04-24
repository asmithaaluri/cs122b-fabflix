/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {

    // ---------- BROWSING BY MOVIE GENRES ----------
    console.log("handleResult: populating alphabetical list of genres from resultData");

    // From the html, grab the id where we will place this list of genres.
    let genreBodyElement = jQuery("#alphabetical_genre_list");

    // Concatenate the html tags with resultData jsonObject to create table rows
    let rowHTML = "";
    for (let i = 0; i < resultData.length; i++) {
        rowHTML +=
            '<a href="list.html?genre=' + resultData[i]['genre_id'] + '">'
            + resultData[i]["genre_name"] +
            ' </a>';
    }

    // Append the row created to the table body, which will refresh the page
    genreBodyElement.append(rowHTML);

    // ---------- BROWSING BY MOVIE TITLES ----------
    console.log("handleResult: populating alphanumerical list of characters including *");
    let titleBodyElement = jQuery("#alphanumeric_title_character_list");

    let alphanumeric_characters_without_star = [];

    for (let i = 65; i < 91; i++) { // Adding A through Z to array.
        alphanumeric_characters_without_star.push(String.fromCharCode(i));
    }

    for (let i = 0; i < 10; i++) { // Adding digits 0 to 9 to array.
        alphanumeric_characters_without_star.push(i);
    }

    rowHTML = "";
    for (let i = 0; i < alphanumeric_characters_without_star.length; i++) {
        rowHTML +=
            '<a href="list.html?prefix=' + alphanumeric_characters_without_star[i] + '">'
            + alphanumeric_characters_without_star[i] +
            ' </a>';
    }
    rowHTML += '<a href="list.html?prefix=*">*</a>';  // Add the star

    titleBodyElement.append(rowHTML);

}

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleResult
jQuery.ajax({
    dataType: "json",  // Setting return data type
    method: "GET",// Setting request method
    url: "api/genres", // Setting request url mapped to GenreServlet.java.
    success: (resultData) => handleResult(resultData) // Setting callback function to handle data returned successfully by the SingleStarServlet
});