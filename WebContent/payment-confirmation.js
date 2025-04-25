/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResultData(resultData) {
    // Populate the star table
    console.log("populating shopping cart with all previousCartItems")
    // Find the empty table body by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    movieTableBodyElement.empty();
    let totalPrice = 0;

    // Iterate through resultData, no more than 20 entries
    for (let i = 0; i < resultData.length; i++) {
        const quantity = parseFloat(resultData[i]["quantity"]);
        const price = parseFloat(resultData[i]["price"]);
        totalPrice += quantity * price;

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";

        rowHTML += "<td>" + resultData[i]["saleId"] + "</td>";

        // Title hyperlink to single movie page.
        rowHTML +=
            "<td>" +
            '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
            + resultData[i]["title"] +
            '</a>' +
            "</td>";

        rowHTML += "<td>" + resultData[i]["quantity"] + "</td>";

        rowHTML += "<td>$" + resultData[i]["price"] + "</td>";

        rowHTML += "</tr>";

        // Append the row created to the table body, which will refresh the page
        movieTableBodyElement.append(rowHTML);
    }
    $("#total_price").text(totalPrice);
}

// Makes the HTTP GET request and registers on success callback function handleStarResult
jQuery.ajax({
    dataType: "json", // Setting return data type
    method: "GET", // Setting request method
    url: "api/place-order",
    success: (resultData) => handleResultData(resultData)
});

