/**
 * Handles the data returned by the API, read the jsonObject and populate data into html elements
 * @param resultData jsonObject
 */

function handleResult(resultData) {
    let metadataElement = jQuery("#metadata");
    for (let i = 0; i < resultData.length; i++){
        let tableInfo = resultData[i];

        let header = "<h2 class='mt-5'>" + tableInfo["table_name"] + "</h2>";
        metadataElement.append(header);

        let table = "";
        table +=
            "<div class=\"table-container table-responsive\">" +
            "        <table class=\"table table-striped\">" +
            "            <thead>" +
            "            <tr>" +
            "                <th>Attribute</th>" +
            "                <th>Type</th>" +
            "            </tr>" +
            "            </thead>";
        table += "<tbody>";

        let columns = resultData[i]["columns"];

        for (let row = 0; row < columns.length; row++) {
            table +=
                "<tr>" +
                    "<td>" + columns[row]["column_name"] + "</td>" +
                    "<td>" + columns[row]["column_type"] + "</td>" +
                "</tr>"
        }

        table += "</tbody>";
        table +=
            "</table>" +
            "    </div>"

        metadataElement.append(table);
    }
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/metadata",
    success: (resultData) => handleResult(resultData)
})