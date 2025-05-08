function displayAddedStarId(responseData) {
    let resultElement = jQuery("#form_results");
    if (responseData["status"] === "success") {
        resultElement.text(`Success! Star ID: ${responseData['starId']}`);
    } else {
        resultElement.text("An error occurred. Please try again.");
    }
}

$(document).on("submit", "#add_star_form", function (addStarEvent) {
    addStarEvent.preventDefault();

    let form = document.getElementById("add_star_form");

    let starName = form.elements["star_name"].value;
    let birthYear = form.elements["birth_year"].value;

    const formData = {
        star_name: starName,
        birth_year: birthYear
    };

    $.ajax({
        type: "POST",
        url: "api/add-star",
        data: formData,
        success: responseData => displayAddedStarId(responseData)
    });
});