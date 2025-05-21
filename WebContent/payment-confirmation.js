/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResultData(resultData) {
    // Populate the star table
    // Find the empty table body by id "star_table_body"
    let movieTableBodyElement = jQuery("#movie_table_body");
    movieTableBodyElement.empty();
    let totalPrice = 0;

    for (let i = 0; i < resultData.length - 1; i++) {
        const quantity = parseFloat(resultData[i]["quantity"]);
        const price = 1;
        totalPrice += quantity * price;

        // Concatenate the html tags with resultData jsonObject
        let rowHTML = "";
        rowHTML += "<tr>";

        rowHTML += "<td>" + resultData[i]["saleId"] + "</td>";

        // // Title hyperlink to single movie page.
        // rowHTML +=
        //     "<td>" +
        //     '<a href="single-movie.html?id=' + resultData[i]['movie_id'] + '">'
        //     + resultData[i]["title"] +
        //     '</a>' +
        //     "</td>";

        rowHTML += "<td>" + resultData[i]["movieTitle"] + "</td>";

        rowHTML += "<td>" + resultData[i]["quantity"] + "</td>";

        rowHTML += "<td> $1 </td>";

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

