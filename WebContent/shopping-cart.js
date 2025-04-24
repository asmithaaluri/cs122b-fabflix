/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResultData(resultData) {
    // Populate the star table
    console.log("populating shopping cart with all previousCartItems")
    // Find the empty table body by id "star_table_body"
    let starTableBodyElement = jQuery("#star_table_body");
    starTableBodyElement.empty();
    let totalPrice = 0;

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < resultData.length; i++) {
        const quantity = parseFloat(resultData[i]["quantity"]);
        const price = parseFloat(resultData[i]["price"]);
        totalPrice += quantity * price;

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

        rowHTML += "<td>" +
            "<button class='update-movie-quantity-button' data-id='" + resultData[i]["movie_id"] + "' data-action='delete-movie-from-cart'>X</button> " +
            "   <button class='update-movie-quantity-button' data-id='" + resultData[i]["movie_id"] + "' data-action='decrease-movie-count'>-</button>   " +
            resultData[i]["quantity"] +
            "   <button class='update-movie-quantity-button' data-id='" + resultData[i]["movie_id"] + "' data-action='increase-movie-count'>+</button>" +
            "</td>";

        rowHTML += "<td>$" + resultData[i]["price"] + "</td>";

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        starTableBodyElement.append(rowHTML);
    }
    $("#total_price").text(totalPrice);
}

$(document).on("click", ".update-movie-quantity-button", function() {
    console.log("updating quantity of movie in cart");
    const movie_id = $(this).data("id");
    const action = $(this).data("action");

    $.ajax({
        url: "api/shopping-cart",
        method: "POST",
        data: {movie_id: movie_id,
               action: action},
        success: () => {
            $.ajax({
                dataType: "json",
                method: "GET",
                url: "api/shopping-cart",
                success: (resultData) => handleResultData(resultData)
            });
        }
    });
});

$(document).on("click", "#proceed-to-payment-button", function() {
    console.log("proceeding to payment page");

    let totalPrice = $("#total_price").text();

    $.ajax({
        url: "api/payment",
        method: "POST",
        data: {totalPrice: totalPrice},
        success: function() {
            window.location.href = "payment.html";
        }
    });
});

/**
 * Once this .js is loaded, following scripts will be executed by the browser
 */

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/shopping-cart",
    success: (resultData) => handleResultData(resultData)
});