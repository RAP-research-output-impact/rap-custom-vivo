/*
   FIX for IE and old versions of EDGE
*/
(function (w) {
    w.URLSearchParams = w.URLSearchParams || function (searchString) {
        var self = this;
        self.searchString = searchString;
        self.get = function (name) {
            var results = new RegExp('[\?&]' + name + '=([^&#]*)').exec(self.searchString);
            if (results == null) {
                return null;
            } else {
                return decodeURI(results[1]) || 0;
            }
        };
    }
})(window)

/*
    DTU Departments
*/
function bc_dept_dropdown(id) {
    var html = "<span id=\"bc-dept\">" + 
               "    <select id=\"dtu-dept\" name=\"dtu-dept\">" +
               "    </select>" +
               "    <select id=\"dtu-dept-tmp\" name=\"dtu-dept-tmp\" style=\"display: none;\">" +
               "        <option id=\"dtu-dept-tmp-opt\"></option>" +
               "    </select>" +
               "</span>" +
               "<span id=\"bc-dept-view\">" +
               "</span>";
    $("#" + id).html(html);
}

function bc_dept_width() {
    $("#dtu-dept-tmp-opt").text($("#dtu-dept option:selected").text());
    $("#dtu-dept").width($("#dtu-dept-tmp").width() * 1.02);
}

function bc_dept_setup(id, callback) {
    bc_dept_dropdown(id);
    dept_options("dtu-dept");
    var urlParams = new URLSearchParams(window.location.search);
    $("#dtu-dept").val(urlParams.get('dept'));
    bc_dept_width();
    $("#dtu-dept").change(function() {
        bc_dept_width();
        callback();
    });
}

function bc_dept_edit() {
    $("#bc-dept-view").hide();
    $("#bc-dept").show();
}

function bc_dept_view() {
    $("#bc-dept").hide();
    $("#bc-dept-view").html($("#dtu-dept option:selected").text());
    $("#bc-dept-view").show();
}

function dept_val() {
    var dept = $("#dtu-dept").val();

    if (dept == null) {
        return '';
    } else {
        return dept;
    }
}

function dept_options(id) {
    var dept = [
        {"uri":"",                                     "name":"Entire university"},
        {"uri":"dtusuborg-centre-for-oil-and-gas-dtu", "name":"Centre for Oil and Gas - DTU"},
        {"uri":"dtusuborg-dtu-aqua",                   "name":"DTU Aqua"},
        {"uri":"dtusuborg-dtu-bioengineering",         "name":"DTU Bioengineering"},
        {"uri":"dtusuborg-dtu-bioinformatics",         "name":"DTU Bioinformatics"},
        {"uri":"dtusuborg-dtu-biosustain",             "name":"DTU Biosustain"},
        {"uri":"dtusuborg-dtu-business",               "name":"DTU Business"},
        {"uri":"dtusuborg-dtu-chemical-engineering",   "name":"DTU Chemical Engineering"},
        {"uri":"dtusuborg-dtu-chemistry",              "name":"DTU Chemistry"},
        {"uri":"dtusuborg-dtu-civil-engineering",      "name":"DTU Civil Engineering"},
        {"uri":"dtusuborg-dtu-compute",                "name":"DTU Compute"},
        {"uri":"dtusuborg-dtu-diplom",                 "name":"DTU Diplom"},
        {"uri":"dtusuborg-dtu-electrical-engineering", "name":"DTU Electrical Engineering"},
        {"uri":"dtusuborg-dtu-energy",                 "name":"DTU Energy"},
        {"uri":"dtusuborg-dtu-entrepreneurship>",      "name":"DTU Entrepreneurship"},
        {"uri":"dtusuborg-dtu-environment",            "name":"DTU Environment"},
        {"uri":"dtusuborg-dtu-food",                   "name":"DTU Food"},
        {"uri":"dtusuborg-dtu-fotonik",                "name":"DTU Fotonik"},
        {"uri":"dtusuborg-dtu-health-tech",            "name":"DTU Health Tech"},
        {"uri":"dtusuborg-dtu-management",             "name":"DTU Management"},
        {"uri":"dtusuborg-dtu-mechanical-engineering", "name":"DTU Mechanical Engineering"},
        {"uri":"dtusuborg-dtu-nanolab",                "name":"DTU Nanolab"},
        {"uri":"dtusuborg-dtu-nanotech",               "name":"DTU Nanotech"},
        {"uri":"dtusuborg-dtu-nutech",                 "name":"DTU Nutech"},
        {"uri":"dtusuborg-dtu-physics",                "name":"DTU Physics"},
        {"uri":"dtusuborg-dtu-space",                  "name":"DTU Space"},
        {"uri":"dtusuborg-dtu-systems-biology",        "name":"DTU Systems Biology"},
        {"uri":"dtusuborg-dtu-vet",                    "name":"DTU Vet"},
        {"uri":"dtusuborg-dtu-wind-energy",            "name":"DTU Wind Energy"},
        {"uri":"dtusuborg-ris-dtu",                    "name":"Risø DTU"},
        {"uri":"dtusuborg-dtu-department-unknown",     "name":"DTU department unknown"}
    ];
    $("#" + id).val(null);
    dept.forEach(function(e) {
        $("#" + id).append(new Option(e.name, e.uri));
    });
}

/*
    Year ranges
*/
function bc_range_dropdown(id) {
    var html  = "<span id=\"bc-range\">";
    if (!window.location.pathname.match(/individual$/)) {
        html += "    From";
    }
    html +=     "    <select id=\"year-from\" name=\"year-from\">" +
                "    </select>" +
                "    -" +
                "    <select id=\"year-to\" name=\"year-to\">" +
                "    </select>" +
                "</span>" +
                "<span id=\"bc-range-view\">" +
                "</span>";
    $("#" + id).html(html);
}

function bc_range_options() {
    var last = new Date().getFullYear();
    var year;
    for(year = 2007; year <= last; year++) {
        $("#year-from").append(new Option(year, year));
        $("#year-to").append(new Option(year, year));
    }
}

function bc_range_setup(id, callback) {
    bc_range_dropdown(id);
    bc_range_options();
    var urlParams = new URLSearchParams(window.location.search);
    var last = new Date().getFullYear();
    var year;
    year = urlParams.get('year-from');
    if (year) {
        $("#year-from").val(year);
    } else {
        $("#year-from").val(last - 5);
    }
    year = urlParams.get('year-to');
    if (year) {
        $("#year-to").val(year);
    } else {
        $("#year-to").val(last);
    }
    $("#year-from").change(function() {
        if ($("#year-from").val() > $("#year-to").val()) {
            $("#year-to").val($("#year-from").val());
        } 
        callback();
    });
    $("#year-to").change(function() {
        if ($("#year-to").val() < $("#year-from").val()) {
            $("#year-from").val($("#year-to").val());
        } 
        callback();
    });
}

function bc_range_edit() {
    $("#bc-range-view").hide();
    $("#bc-range").show();
}

function bc_range_view() {
    $("#bc-range").hide();
    $("#bc-range-view").html("From " + $('#year-from').val() + " - " + $('#year-to').val());
    $("#bc-range-view").show();
}

function range_from_val() {
    return $("#year-from").val();
}

function range_to_val() {
    return $("#year-to").val();
}


function filter_setup(type) {
    $("#copub-" + type + "-filter").keyup(function() {
        $(".copub-" + type + "-row").each(function() {
            if ($(this).text().search(new RegExp($("#copub-" + type + "-filter").val(), "i")) != -1) {
                $(this).parent().show();
            } else {
                $(this).parent().hide();
            }
        });
    });
}

/*
    UI functions
*/
function sortArrow(up, used) {
    var svg = '<svg height="14" width="24">';
    if (up) {
        if (used) {
            svg += '<polygon points="12,2 22,12 2,12 12,2" style="fill:red;stroke:white;stroke-width:2" />';
        } else {
            svg += '<polygon points="12,2 22,12 2,12 12,2" style="fill:none;stroke:white;stroke-width:2" />';
        }
    } else {
        if (used) {
            svg += '<polygon points="12,12 2,2 22,2 12,12" style="fill:red;stroke:white;stroke-width:2" />';
        } else {
            svg += '<polygon points="12,12 2,2 22,2 12,12" style="fill:none;stroke:white;stroke-width:2" />';
        }
    }
    svg += '</svg>';
    return svg;
}

/*
    Data functions
*/
function loadData(url, callback) {
    var xhr = new XMLHttpRequest();

    xhr.open('GET', url);
    xhr.onload = function() {
        if (xhr.status === 200) {
            var response = JSON.parse(xhr.response)
            callback(response);
        } else {
            alert('Request failed.  Returned status of ' + xhr.status);
        }
    };
    xhr.send();
}

function fetchOrgList(service, field, code, name) {
    $("#copub-org-filter").val("");
    $("#copub-org-list tbody").html("<tr><td colspan=\"2\" style=\"font-size: 24px;\">Loading...</td></tr>");
    var dataURL = "";
    if (field) {
        dataURL = urls_base + "/vds/report/" + service + "?" + field + '=' + code + "&dept=" + dept_val() +
                  "&startYear=" + range_from_val() + "&endYear=" + range_to_val();
    } else {
        dataURL = urls_base + "/vds/report/" + service + "/" + code + "?dept=" + dept_val() +
                  "&startYear=" + range_from_val() + "&endYear=" + range_to_val();
    }
    console.log ("calling loadData: " + dataURL);
    loadData(dataURL, orgList);

    $("#bc-copub-type-link").show();
    $("#bc-copub-type").hide();
    bc_dept_view();
    bc_range_view();

    $("#bc-main").html(name + " &gt; ");
    $("#bc-main").show();
    $('#copub-main-list').hide();
    $('#copub-org-list').show();
}

function orgList(data) {
    console.log ("orgList - building tbody");
    var tbody = "";
    var range = "&year-from=" + range_from_val() + "&year-to=" + range_to_val();
    var n = 0;
    if (data.orgs[0].impact) {
        for (var i = 0, j = data.orgs.length; i < j; i++){
            if (data.orgs[i].impact === undefined) {
                console.log ("undifined indicator for: " + data.orgs[i].org);
            } else {
                tbody += "<tr><td class=\"copub-org-row sort-org\"><a href=\"" + urls_base + "/individual?uri=" + data.orgs[i].org + range + "\">" +
                         data.orgs[i].name + "</a></td><td class=\"sort-org-imp\" style=\"text-align: right;\">" + data.orgs[i].impact + "</td>" + 
                         "<td class=\"sort-org-pub\" style=\"text-align: right;\">" + data.orgs[i].publications + "</td></tr>";
                n++;
            }
        }
    } else {
        for (var i = 0, j = data.orgs.length; i < j; i++){
            tbody += "<tr><td class=\"copub-org-row sort-org\"><a href=\"" + urls_base + "/individual?uri=" + data.orgs[i].org + range + "\">" + data.orgs[i].name +
                     "</a></td><td class=\"sort-org-pub\" style=\"text-align: right;\">" + data.orgs[i].publications + "</td></tr>";
            n++;
        }
    }
    console.log ("orgList - " + n + "rows");
    console.log ("orgList - writing tbody");
    $("#copub-org-list tbody").html(tbody);
    console.log ("orgList - adding sort");
    setSort("sort-org");
    $('#copub-container').removeClass('tab-spinner');
}

function setSort(name) {
    $("#" + name + "-pub .sort-dir").html (sortArrow (1, 1));
    $("#" + name + "-imp .sort-dir").html (sortArrow (0, 0));
    $("#" + name + " .sort-dir").html (sortArrow (0, 0));
    var inverse1 = false;
    var inverse2 = false;
    var inverse3 = false;
    $('#' + name).each(function() {
        $(this).click(function() {
            console.log("adding spinner");
            $('#copub-container').addClass('tab-spinner');
            $("td." + name).sortElements(function(a, b) {
                return $.text([a]) > $.text([b]) ?
                       inverse1 ? -1 : 1
                     : inverse1 ? 1 : -1;
            }, function() {
                return this.parentNode;
            });
            if (inverse1) {
                $("#" + name + " .sort-dir").html (sortArrow (1, 1));
            } else {
                $("#" + name + " .sort-dir").html (sortArrow (0, 1));
            }
            $("#" + name + "-pub .sort-dir").html (sortArrow (0, 0));
            $("#" + name + "-imp .sort-dir").html (sortArrow (0, 0));
            inverse1 = !inverse1;
            inverse2 = false;
            inverse3 = false;
            console.log("remove spinner");
            $('#copub-container').removeClass('tab-spinner');
        });
    });
    $('#' + name + '-pub').each(function() {
        $(this).click(function() {
            console.log("adding spinner");
            $('#copub-container').addClass('tab-spinner');
            $("td." + name + "-pub").sortElements(function(a, b) {
                return Number($.text([a])) > Number($.text([b])) ?
                       inverse2 ? -1 : 1
                     : inverse2 ? 1 : -1;
            }, function() {
                return this.parentNode;
            });
            if (inverse2) {
                $("#" + name + "-pub .sort-dir").html (sortArrow (1, 1));
            } else {
                $("#" + name + "-pub .sort-dir").html (sortArrow (0, 1));
            }
            $("#" + name + " .sort-dir").html (sortArrow (0, 0));
            $("#" + name + "-imp .sort-dir").html (sortArrow (0, 0));
            inverse2 = !inverse2;
            inverse1 = false;
            inverse3 = false;
            console.log("remove spinner");
            $('#copub-container').removeClass('tab-spinner');
        });
    });
    $('#' + name + '-imp').each(function() {
        $(this).click(function() {
            console.log("adding spinner");
            $('#copub-container').addClass('tab-spinner');
            $("td." + name + "-imp").sortElements(function(a, b) {
                return Number($.text([a])) > Number($.text([b])) ?
                       inverse3 ? -1 : 1
                     : inverse3 ? 1 : -1;
            }, function() {
                return this.parentNode;
            });
            if (inverse3) {
                $("#" + name + "-imp .sort-dir").html (sortArrow (1, 1));
            } else {
                $("#" + name + "-imp .sort-dir").html (sortArrow (0, 1));
            }
            $("#" + name + " .sort-dir").html (sortArrow (0, 0));
            $("#" + name + "-pub .sort-dir").html (sortArrow (0, 0));
            inverse3 = !inverse3;
            inverse1 = false;
            inverse2 = false;
            console.log("remove spinner");
            $('#copub-container').removeClass('tab-spinner');
        });
    });
}
