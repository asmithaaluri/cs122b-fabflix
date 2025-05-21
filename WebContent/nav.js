/*
 * This function is called by the library when it needs to lookup a query.
 *
 * The parameter query is the query string.
 * The doneCallback is a callback function provided by the library, after you get the
 *   suggestion list from AJAX, you need to call this function to let the library know.
 */
function handleLookup(query, doneCallback) {
    console.log("----- Autocomplete Initiated -----")

    // Checking past query results in cache.
    let cache = sessionStorage.getItem(query);
    if (cache != null) {
        console.log("Using cache for query: " + query);
        let jsonData = JSON.parse(cache);
        let suggestionsFromCache = reformatJSONServletResponseForAutocompleteLibrary(jsonData)
        console.log("Suggestion list: ");
        console.log(suggestionsFromCache);
        doneCallback( { suggestions: suggestionsFromCache } );
        return;
    }

    console.log("Sending AJAX request to backend for query: " + query);
    jQuery.ajax({
        "method": "GET",
        url: "api/movies/autocomplete-search",
        data: {title: query},
        "success": function(data) {
            handleLookupAjaxSuccess(data, query, doneCallback)
        },
        "error": function(errorData) {
            console.log("lookup ajax error")
            console.log(errorData)
        }
    })
}

function reformatJSONServletResponseForAutocompleteLibrary(data) {
    return data.map((item) =>  {
        return { value: item.title, data: item.movie_id };
    });
}

/*
 * This function is used to handle the ajax success callback function.
 * It is called by our own code upon the success of the AJAX request
 *
 * data is the JSON data string you get from your Java Servlet
 *
 */
function handleLookupAjaxSuccess(data, query, doneCallback) {
    // this is already in JSON because I set application type to JSON
    let suggestionsFromSendingToBackend = {};
    if (data && 'movieData' in data) {
        let jsonData = data['movieData'];

        // Caching results.
        sessionStorage.setItem(query, JSON.stringify(jsonData));

        suggestionsFromSendingToBackend = reformatJSONServletResponseForAutocompleteLibrary(jsonData);
    }
    console.log("Suggestion list:");
    console.log(suggestionsFromSendingToBackend);
    doneCallback( { suggestions: suggestionsFromSendingToBackend } );
}


/*
 * This function is the select suggestion handler function.
 * When a suggestion is selected, this function is called by the library.
 *
 * You can redirect to the page you want using the suggestion data.
 */
function handleSelectSuggestion(suggestion) {
    console.log("you select " + suggestion["value"] + " with ID " + suggestion["data"])
    window.location.href = "single-movie.html?id=" + suggestion["data"];
}

/*
 * This statement binds the autocomplete library with the input box element and
 *   sets necessary parameters of the library.
 *
 * The library documentation can be find here:
 *   https://github.com/devbridge/jQuery-Autocomplete
 *   https://www.devbridge.com/sourcery/components/jquery-autocomplete/
 *
 */
// $('#autocomplete') is to find element by the ID "autocomplete"
$(document).ready( () => {
        $('#autocomplete').autocomplete({
            // documentation of the lookup function can be found under the "Custom lookup function" section
            lookup: function (query, doneCallback) {
                handleLookup(query, doneCallback)
            },
            onSelect: function (suggestion) {
                handleSelectSuggestion(suggestion)
            },
            deferRequestBy: 300,
            minChars: 3
        });
    }
)