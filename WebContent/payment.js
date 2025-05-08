// Populate HTML with price from backend
function handleResultData(resultData) {
    $("h1").html("Total Price: $" + resultData[0]["totalPrice"]);
}

$(document).on("submit", "#payment_form", function (placeOrderEvent) {
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

    $.ajax({
        type: "POST",
        url: "api/place-order",
        data: formData,
        success: function(responseData) {
            if (responseData.status === "success") {
                window.location.href = "payment-confirmation.html";
            } else {
                alert("Error processing payment. Please reenter your payment information.");
            }
        }
    });
});


jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/place-order",
    success: (resultData) => handleResultData(resultData)
});