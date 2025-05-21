/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleStarResult(resultData) {

    const MAX_GENRES = 3;
    const MAX_STARS = 3;

    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let starTableBodyElement = jQuery("#star_table_body");

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < Math.min(20, resultData.length); i++) {

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";
        // Title hyperlink to single movie page.
        rowHTML +=
            "<td>" +
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["title"] +
            '</a>' +
            "</td>";
        rowHTML += "<td>" + resultData[i]["year"] + "</td>";
        rowHTML += "<td>" + resultData[i]["director"] + "</td>";

        rowHTML += "<td>";
            let threeGenres = resultData[i]["genres"];
            if (threeGenres && threeGenres.length > 0) {
                rowHTML += threeGenres[0];
                for (let j = 1; j < Math.min(MAX_GENRES, threeGenres.length); j++) {
                    rowHTML += ", " + threeGenres[j];
                }
            } else {
                rowHTML += "N/A"; // No genres found.
            }
        rowHTML += "</td>";

        rowHTML += "<td>";
            let threeStars = resultData[i]["stars"];
            if (threeStars && threeStars.length > 0) {
                rowHTML += '<a href="single-star.html?id=' + threeStars[0]["id"] + '">' +
                    threeStars[0]["name"] +     // display star_name for the link text
                    '</a>'
                for (let j = 1; j < Math.min(MAX_STARS, threeStars.length); j++) {
                    rowHTML += ", " +
                        '<a href="single-star.html?id=' + threeStars[j]["id"] + '">' +
                        threeStars[j]["name"] +
                        '</a>'
                }
            } else {
                rowHTML += "N/A"; // No stars found.
            }
        rowHTML += "</td>";

        rowHTML += "<td id='rating-row'>" + resultData[i]["rating"] + "</td>";

        rowHTML += "<td>" +
                "<form ACTION='#' class='cart' METHOD='post'>" +
                    "<input type='hidden' name='movie_id' value='" + resultData[i]['movie_id'] + "' />" +
                    "<input id='add-to-cart-button' type='submit' VALUE='Add'/>" +
                "</form>" +
            "</td>"

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
}

/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleCartArray(resultArray) {
    let item_list = $("#item_list");
    // change it to html list
    let res = "<ul>";
    for (let i = 0; i < resultArray.length; i++) {
        // each item will be in a bullet point
        res += "<li>" + resultArray[i] + "</li>";
    }
    res += "</ul>";

    // clear the old array and show the new array in the frontend
    item_list.html("");
    item_list.append(res);
}

$(document).on("submit", ".cart", function (cartEvent) {
    cartEvent.preventDefault();
    const cartForm = $(this);

    $.ajax("api/index", {
        method: "POST",
        data: cartForm.serialize(),
        success: resultDataString => {
            let resultDataJson = JSON.parse(resultDataString);
            handleCartArray(resultDataJson["previousCartItems"]);
            alert("Successfully added to cart.");
        }
    });

    // clear input form
    cartForm[0].reset();

})

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/top20",
    success: (resultData) => handleStarResult(resultData) // Setting callback function to handle data returned successfully by the StarsServlet
});