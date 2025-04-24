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

function disableNextButtonOnLastPage(hasNextPage) {
    if (!hasNextPage) {
        document.getElementById('next_pg').disabled = true;
    }
}

function handleResult(resultData){
    populateTables(JSON.parse(resultData['movieData']));
    disableNextButtonOnLastPage(resultData['hasNextPage']);
}

function populateTables(resultData) {
    const MAX_GENRES = 3;
    const MAX_STARS = 3;

    let movieBodyElement = jQuery("#movie_table_body");

    let rowHTML = "";
    for (let i = 0; i < resultData.length; i++) {
        rowHTML += "<tr>"
            + "<td>" + '<a href="single-movie.html?id=' + resultData[i]["movie_id"] + '">' +
                        resultData[i]["title"] + '</a>' +
            "</td>"
            + "<td>" + resultData[i]["year"] + "</td>"
            + "<td>" + resultData[i]["director"] + "</td>"

        rowHTML += "<td>";
        let threeGenres = resultData[i]["genres"];
        // alphabetize genres
        const collator = new Intl.Collator("en");
        threeGenres.sort((a, b) => collator.compare(a.name, b.name));
        if (threeGenres && threeGenres.length > 0) {
            rowHTML += '<a href="list.html?genre=' + threeGenres[0]["id"] + '">' +
                threeGenres[0]["name"] +
                '</a>'
            for (let j = 1; j < Math.min(MAX_GENRES, threeGenres.length); j++) {
                rowHTML += ", " + '<a href="list.html?genre=' + threeGenres[j]["id"] + '">' +
                    threeGenres[j]["name"] +
                    '</a>'
            }
        } else {
            rowHTML += "N/A"; // No genres found.
        }
        rowHTML += "</td>";

        rowHTML += "<td>";
        let threeStars = resultData[i]["stars"];
        // sort by movie count, then by name
        threeStars.sort(
                (a, b) => {
                    if (a.count !== b.count) {
                        return b.count - a.count;
                    }
                    return collator.compare(a.name, b.name);
                }
            )
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
            "<form action='#' class='cart' method='post'>" +
            "<input type='hidden' name='movie_id' value='" + resultData[i]['movie_id'] + "' />" +
            "<input id='add-to-cart-button' type='submit' value='Add'/>" +
            "</form>" +
            "</td>"

        rowHTML += "</tr>";
    }
    movieBodyElement.append(rowHTML);
}


/**
 * Handle the items in item list
 * @param resultArray jsonObject, needs to be parsed to html
 */
function handleCartArray(resultArray) {
    console.log(resultArray);
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
    console.log("submit cart form");
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

function updateLocalStorage(sort, movies) {
    localStorage.setItem('sort', sort);
    localStorage.setItem('movies', movies);
    localStorage.setItem("page", "1");
}

let genre = getParameterByName('genre');
let prefix = getParameterByName('prefix');

let title = getParameterByName('title');
let year = getParameterByName('year');
let director = getParameterByName('director');
let star = getParameterByName('star');

let sort = getParameterByName('sort');
let moviesPerPage = getParameterByName('movies');
let page = getParameterByName('page');

let dataToSend = {};

if (title || year || director || star){
    if (title) {
        dataToSend.title = title;
    } else {
        dataToSend.title = null;
    }
    if (year) {
        dataToSend.year = year;
    } else {
        dataToSend.year = null;
    }
    if (director) {
        dataToSend.director = director;
    } else {
        dataToSend.director = null;
    }
    if (star) {
        dataToSend.star = star;
    } else {
        dataToSend.star = null;
    }
    localStorage.setItem("page", "1");
    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: "api/movies/search", // Setting request url mapped to MovieSearchServlet.java.
        data: dataToSend,
        success: (resultData) => handleResult(resultData)
    });
} else {
    if (sort) {
        dataToSend.sort = sort;
        dataToSend.moviesPerPage = moviesPerPage;
        updateLocalStorage(sort, moviesPerPage);
    } else if (page) {
        dataToSend.page = page;
        localStorage.setItem("page", page);
    } else if (genre) {
        dataToSend.genre = genre;
        localStorage.setItem("page", "1");
    } else if (prefix) {
        dataToSend.prefix = prefix;
        localStorage.setItem("page", "1");
    }
    jQuery.ajax({
        dataType: "json",  // Setting return data type
        method: "GET", // Setting request method
        url: "api/movies", // Setting request url mapped to MovieListServlet.java.
        data: dataToSend,
        success: (resultData) => handleResult(resultData)
    });
}

// display correct sorting method and movie per page limit
const moviesPerPageOption = document.getElementById('movies');
const sortingOption = document.getElementById('sortBy');
let lastSort = localStorage.getItem('sort');
let lastLimit = localStorage.getItem('movies');
if (lastSort) {
    sortingOption.value = lastSort;
}
if (lastLimit) {
    moviesPerPageOption.value = lastLimit;
}

// display correct page number
let pgNum = "1";
if (localStorage.getItem('page')){
    pgNum = localStorage.getItem('page');
} else {
    localStorage.setItem('page', "1");
}
document.getElementById('current_pg').textContent = pgNum;

const prevButton = document.getElementById('prev_pg');
const nextButton = document.getElementById('next_pg');

if (pgNum === "1"){
    prevButton.disabled = true;
}

function loadPage(pageNum) {
    document.getElementById('current_pg').textContent = pageNum;
    window.location.href = `list.html?page=${pageNum}`;
}

prevButton.addEventListener('click', () => {loadPage(parseInt(pgNum) - 1)});
nextButton.addEventListener('click', () => {loadPage(parseInt(pgNum) + 1)})