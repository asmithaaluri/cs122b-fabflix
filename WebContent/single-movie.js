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
    let movieInfo = jQuery("#movie_info");
    movieInfo.append(
        "<h2>" + resultData[0]["title"] + " (" + resultData[0]["year"] + ")</h2>" +
        "<h5><strong>Director: " + resultData[0]["director"] + "</h5>" +
        "<h5><strong>Rating: " + resultData[0]["rating"] + "</h5>"
    );

    let movieGenres = jQuery("#movie_genres");
    let genreHTML = "<h5><strong>Genre(s): ";
    let genres = resultData[0]['genres'];
    // alphabetize genres
    const collator = new Intl.Collator("en");
    genres.sort((a, b) => collator.compare(a.name, b.name));
    let numGenres = genres.length;
    if (numGenres === 0){
        genreHTML += "N/A";
    } else {
        genreHTML += '<a href="list.html?genre=' + genres[0]["id"] + '">' +
                            genres[0]["name"] +
                    '</a>'
        for (let i = 1; i < numGenres; i++) {
            genreHTML += ", " +
                        '<a href="list.html?genre=' + genres[i]["id"] + '">' +
                            genres[i]["name"] +
                        '</a>'
        }
    }
    genreHTML += "</h5>";
    movieGenres.append(genreHTML);

    let movieStars = jQuery("#movie_stars");
    let starsHTML = "<h5><strong>Star(s): ";
    // sort by movie count, then by name
    let stars = resultData[0]['stars'];
    stars.sort(
        (a, b) => {
            if (a.count !== b.count) {
                return b.count - a.count;
            }
            return collator.compare(a.name, b.name);
        }
    )
    let numStars = stars.length;
    if (numStars === 0){
        starsHTML += "N/A";
    } else {
        starsHTML += '<a href="single-star.html?id=' + stars[0]['id'] + '">' +
                        stars[0]['name'] +
                    '</a>';
        for (let i = 1; i < numStars; i++) {
            starsHTML += ", " +
                        '<a href="single-star.html?id=' + stars[i]['id'] + '">' +
                            stars[i]['name'] +
                        '</a>';
        }
    }
    starsHTML += "</h5>";
    movieStars.append(starsHTML);

    let addToCartButton = jQuery("#add_to_cart");
    let addToCartButtonHTML = "<form ACTION='#' class='cart' METHOD='post'>" +
        "<input type='hidden' name='movie_id' value='" + resultData[0]['movie_id'] + "' />" +
        "<input id='add-to-cart-button' type='submit' VALUE='Add to Cart'/>" +
        "</form>";
    addToCartButton.append(addToCartButtonHTML);
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