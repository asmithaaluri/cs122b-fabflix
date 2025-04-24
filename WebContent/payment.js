/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */
function handleResultData(resultData) {
    console.log("on payment page to display total price and form")

    $("h3").html("Total Price: $" + resultData[0]["totalPrice"]);
}

$(document).on("submit", "#payment_form", function (placeOrderEvent) {
    console.log("place order form");
    placeOrderEvent.preventDefault();

    let form = document.getElementById("payment_form");

    let firstName = form.elements["first_name"].value;
    let lastName = form.elements["last_name"].value;
    let creditCardNumber = form.elements["credit_card_number"].value;
    let expirationDate = form.elements["expiration_date"].value;

    const formData = {
        first_name: firstName,
        last_name: lastName,
        credit_card_number: creditCardNumber,
        expiration_date: expirationDate
    };

    console.log(formData);

    $.ajax({
        type: "POST",
        url: "api/place-order",
        data: formData,
        success: function(responseData) {
            if (responseData.status === "success") {
                window.location.href = "payment-confirmation.html";
            } else if (responseData.status === "error") {
                alert("Error processing payment. Please reenter your payment information.");
            }
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
    url: "api/place-order",
    success: (resultData) => handleResultData(resultData)
});