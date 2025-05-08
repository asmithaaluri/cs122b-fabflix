function displayAddMovieResults(responseData) {
    let resultElement = jQuery("#form_results");
    if (responseData["status"] === "success") {
        if (responseData["added"] === true) {
            resultElement.text("Success! " +
                            "Movie ID: " + responseData['movieId'] +
                            " Star ID: " + responseData['starId'] +
                            " Genre ID: " + responseData['genreId']);
        } else {
            resultElement.text("Error: Duplicate movie.");
        }
    } else {
        resultElement.text("An error occurred. Please try again.");
    }
}

$(document).on("submit", "#add_movie_form", function (addMovieEvent) {
    addMovieEvent.preventDefault();

    let form = document.getElementById("add_movie_form");

    let title = form.elements["title"].value;
    let year = form.elements["year"].value;
    let director = form.elements["director"].value;
    let genre = form.elements["genre"].value;
    let starName = form.elements["star_name"].value;
    let birthYear = form.elements["birth_year"].value;

    const formData = {
        title: title,
        year: year,
        director: director,
        genre: genre,
        star_name: starName,
        birth_year: birthYear
    };

    $.ajax({
        type: "POST",
        url: "api/add-movie",
        data: formData,
        success: responseData => displayAddMovieResults(responseData)
    });
});