var State = {"def": "", "cur": "", "all": ["researchers", "researcher", "records", "record", "units", "unit"], "lst": []};
var Doctype = {
    "":                  "All",
    "article":           "Article",
    "review":            "Review",
    "proceedings paper": "Proceedings paper",
    "abstract":          "Abstracts",
    "correction":        "Corrections",
    "other":             "Other"
};
var Organisation = {
    "":   "All",
    "1":  "DTU",
    "0":  "Non-DTU"
};
var Impact = {
    "":      "All",
    "top1":  "Top 1%",
    "top10": "Top 10%",
    "avg":   ">= Average",
    "blw":   "< Average",
};
var Access = {
    "":      "All",
    "oa":    "OA",
    "notoa": "Not OA",
};
var Crumb = {"dep": null, mapping: {}, over: {}};
var url_base = "";
var url_theme = "";
function fetch_department_options(callback, arg) {
    console.log("GET /rap-adh/ws/department_options");
    var xhr = new XMLHttpRequest();
    xhr.open('GET', "/rap-adh/ws/department_options");
    xhr.onload = function() {
        if (xhr.status === 200) {
            var response = JSON.parse(xhr.response);
            Crumb.dep = response.rapas.response.body;
            if (arg) {
                callback();
            } else {
                callback(response.rapas.response.body);
            }
        } else {
            alert('Request failed.  Returned status of ' + xhr.status);
        }
    };
    xhr.send();
}
function update_crumb() {
    var dep = $("#department").val();
    var sec = $("#section").val();
    if (dep == null) {
        dep = Crumb.over.dep;
    }
    if (sec == null) {
        sec = Crumb.over.sec;
    }
    var html = '';
    $.each(State.lst, function(index, state) {
        switch(state) {
            case "researchers":
                if (html != "") {
                    html += '<span class="crumb-sep">&gt;</span>';
                }
                if (State.def == state) {
                    html += '<span class="crumb-current">DTU Researchers</span><span class="crumb-sep">&gt;</span>';
                    html += '<select id="crumb-width" style="display: none;"><option id="crumb-width-opt"></option></select>';
                    if (State.cur == state) {
                        if (Crumb.dep != null) {
                            html += '<select id="department">';
                            $.each(Crumb.dep.options, function(index, op) {
                                html += '<option value="' + op[0] + '">' + op[1] + '</option>';
                                if (op[0]) {
                                    Crumb.mapping[op[0]] = op[1];
                                }
                            });
                            html += '</select>';
                            if (dep) {
                                html += '<span class="crumb-sep">&gt;</span><select id="section">';
                                $.each(Crumb.dep[dep], function(index, op) {
                                    html += '<option value="' + op[0] + '">' + op[1] + '</option>';
                                    if (op[0]) {
                                        Crumb.mapping[op[0]] = op[1];
                                    }
                                });
                                html += '</select>';
                            }
                        }
                    } else {
                        var anchor = '';
                        if (dep) {
                            anchor = Crumb.mapping[dep];
                            if (sec) {
                                anchor += ' / ' + Crumb.mapping[sec];
                            }
                        } else {
                            anchor = 'All';
                        }
                        html += '<span class="crumb"><a onClick="state_set(' + "'researchers'" + ');">' + anchor + '</a></span>';
                        html += '<input id="department" type="hidden" value="' + dep + '">';
                        html += '<input id="section" type="hidden" value="' + sec + '">';
                    }
                } else {
                    if (State.cur == state) {
                        html += '<span class="crumb-current researcher-name">Researchers</span>';
                    } else {
                        html += '<span class="crumb"><a onClick="state_set(' + "'researchers'" + ');">Researchers</a></span>';
                    }
                }
                break;
            case "researcher":
                if (html != "") {
                    html += '<span class="crumb-sep">&gt;</span>';
                }
                if (State.cur == state) {
                    html += '<span class="crumb-current researcher-name">' + Crumb.name + '</span>';
                } else {
                    html += '<span class="crumb"><a class="researcher-name" onClick="state_set(' + "'researcher'" + ');">' + Crumb.name + '</a></span>';
                }
                break;
            case "records":
                if (html != "") {
                    html += '<span class="crumb-sep">&gt;</span>';
                }
                if (State.cur == state) {
                    html += '<span class="crumb-current">Publication list</span>';
                } else {
                    html += '<span class="crumb"><a onClick="state_set(' + "'records'" + ');">Publication list</a></span>';
                }
                break;
            case "record":
                if (html != "") {
                    html += '<span class="crumb-sep">&gt;</span>';
                }
                if (State.cur == state) {
                    html += '<span class="crumb-current ut-display">' + Crumb.ut + '</span>';
                } else {
                    html += '<span class="crumb"><a class="ut-display" onClick="state_set(' + "'record'" + ');">' + Crumb.ut + '</a></span>';
                }
                break;
            case "units":
                if (html != "") {
                    html += '<span class="crumb-sep">&gt;</span>';
                }
                if (State.cur == state) {
                    html += '<span class="crumb-current">DTU Units</span>';
                } else {
                    html += '<span class="crumb"><a onClick="state_set(' + "'units'" + ');">DTU Units</a></span>';
                }
                break;
            case "unit":
                if (html != "") {
                    html += '<span class="crumb-sep">&gt;</span>';
                }
                if (State.cur == state) {
                    html += '<span class="crumb-current unit-name">' + Crumb.unit + '</span>';
                } else {
                    html += '<span class="crumb"><a class="unit-name" onClick="state_set(' + "'unit'" + ');">' + Crumb.unit + '</a></span>';
                }
                break;
            default:
                console.log("error: unknown state: " + state);
        }
    });
    $("#crumb").html(html);
    if (State.cur == "researchers") {
        if (sec) {
            $("#section").val(sec);
        }
        if (dep) {
            $("#department").val(dep);
            $("#crumb-width-opt").text($("#section option:selected").text());
            $("#section").width($("#crumb-width").width() * 1.02);
        }
        $("#crumb-width-opt").text($("#department option:selected").text());
        $("#department").width($("#crumb-width").width() * 1.02);
        $("#department").change(function() {
            console.log('Section: ' + $("#section").val());
            $("#section").val("");
            console.log('Section: ' + $("#section").val());
            researchers_setup(1);
        });
        $("#section").change(function() {
            researchers_setup(1);
        });
    }
}
function state_set(val) {
    console.log("state_set(" + val + ")");
    var push = 1;
    if (val == null) {
        var val = window.location.href.split('#')[1];
        console.log("val 1: " + val);
        if (val == null || val == "") {
            val = State.def;
            console.log("val 2: " + val);
        } else {
            if (val != State.def) {
                if (!$("#researcher-orcid").val().match(/[0-9]/)) {
                    if (!$("#unit-id").val().match(/[a-z]/)) {
                        val = State.def;
                        console.log("val 3: " + val);
                    }
                }
            }
        }
        push = 0;
    }
    if (push) {
        window.scrollTo(0, 0);
    }
    var match = 0;
    $.each(State.all, function(index, state) {
        if (state == val) {
            $("#" + state + "-section").show();
            match = 1;
            console.log("info: moving state from '" + State.cur + "' to '" + state + "'");
            State.cur = state;
            if (State.def == "") {
                State.def = state;
            }
            State.lst.push(state);
            var crb = [];
            $.each(State.lst, function(index, ele) {
                if (crb != null) {
                    crb.push(ele);
                    if (ele == state) {
                        console.log("info: path changing from " + State.lst.join(", ") + " to " + crb.join(", "));
                        State.lst = crb;
                        crb = null;
                    }
                }
            });
            if (push) {
                console.log("pushing " + state);
                window.history.pushState('forward', null, '#' + state);
            }
            update_crumb();
        } else {
            $("#" + state + "-section").hide();
        }
    });
    if (match == 0) {
        console.log("error: cannot find match for state: '" + val + "'");
    }
    return (false);
}
function html_encode(text) {
    return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt");
}
function paging_html (page, pages, comm) {
    var html = "";
    if (page > 1) {
        html += '<span class="paging-prev"><a onClick="' + comm.replace(/PAGE/, (page - 1)) + ';">Previous</a></span>';
    } else {
        html += '<span class="paging-prev">Previous</span>';
    }
    if (pages < 8) {
        for(var i = 1; i <= pages; i++) {
            if (i == page) {
                html += '<span class="paging-page">' + i + '</span>';
            } else {
                html += '<span class="paging-page"><a onClick="' + comm.replace(/PAGE/, i) + ';">' + i + '</a></span>';
            }
        }
    } else {
        if (page < 5) {
            for(var i = 1; i <= 5; i++) {
                if (i == page) {
                    html += '<span class="paging-current">' + i + '</span>';
                } else {
                    html += '<span class="paging-page"><a onClick="' + comm.replace(/PAGE/, i) + ';">' + i + '</a></span>';
                }
            }
            html += '<span class="paging-elipse">...</span>';
            html += '<span class="paging-page"><a onClick="' + comm.replace(/PAGE/, pages) + ';">' + pages + '</a></span>';
        } else {
            html += '<span class="paging-page"><a onClick="' + comm.replace(/PAGE/, 1) + ';">1</a></span>';
            html += '<span class="paging-elipse">...</span>';
            if ((pages - page) < 4) {
                for(var i = (pages - 4); i <= pages; i++) {
                    if (i == page) {
                        html += '<span class="paging-current">' + i + '</span>';
                    } else {
                        html += '<span class="paging-page"><a onClick="' + comm.replace(/PAGE/, i) + ';">' + i + '</a></span>';
                    }
                }
            } else {
                for(var i = (page - 1); i <= (page + 1); i++) {
                    if (i == page) {
                        html += '<span class="paging-current">' + i + '</span>';
                    } else {
                        html += '<span class="paging-page"><a onClick="' + comm.replace(/PAGE/, i) + ';">' + i + '</a></span>';
                    }
                }
                html += '<span class="paging-elipse">...</span>';
                html += '<span class="paging-page"><a onClick="' + comm.replace(/PAGE/, pages) + ';">' + pages + '</a></span>';
            }
        }
    }
    if (page < pages) {
        html += '<span class="paging-next"><a onClick="' + comm.replace(/PAGE/, (page + 1)) + ';">Next</a></span>';
    } else {
        html += '<span class="paging-next">Next</span>';
    }
    return html;
}
function flip_doctype(id, callback) {
    var tf = true;
    $.each($("input." + id + ":checked"), function() {
        tf = false;
    });
    $.each($("input." + id), function() {
        $(this).prop( "checked", tf );
    });
    callback();
}
function researchers_process(retable, mod, res) {
    start = performance.now();
    for (var i = 0, l = res.rapas.response.body.length; i < l; i++) {
        res.rapas.response.body[i].shift();
        if (res.rapas.response.body[i][1]) {
            res.rapas.response.body[i][0] = '<a onClick="researcher_fetch(' + "'" + res.rapas.response.body[i][1] + "'" + '); state_set(' + "'researcher'" + ');">' + res.rapas.response.body[i][0] + '</a>';
        }
//      retable.row.add(res.rapas.response.body[i]).draw(false);
    }
    retable.rows.add(res.rapas.response.body).draw();
    console.log("loaded " + res.rapas.response.body.length + " records in " + (performance.now() - start) + " milliseconds");
    if (mod == "/head") {
        researchers_fetch(retable, "rest");
    } else {
        update_crumb();
    }
}
function researchers_fetch(retable, mod) {
    if (mod == null) {
        mod = "";
    } else {
        mod = "/" + mod;
    }
    var dep = $("#department").val();
    var sec = $("#section").val();
    console.log("dep: '" + dep + "'");
    if (dep == null) {
        dep = Crumb.over.dep;
        sec = Crumb.over.sec;
    }
    console.log("dep: '" + dep + "'");
    if (sec) {
        mod += '?sec=' + sec;
    } else {
        if (dep) {
            mod += '?dep=' + dep;
        }
    }
    console.log("GET /rap-adh/ws/researchers" + mod);
    var xhr = new XMLHttpRequest();
    xhr.open('GET', "/rap-adh/ws/researchers" + mod);
    xhr.onload = function() {
        if (xhr.status === 200) {
            var response = JSON.parse(xhr.response)
            researchers_process(retable, mod, response);
        } else {
            alert('Request failed.  Returned status of ' + xhr.status);
        }
    };
    xhr.send();
}
function researchers_setup(partial) {
    var retable;
    if ($.fn.dataTable.isDataTable('#researchers-content')) {
        retable = $('#researchers-content').DataTable();
        retable.rows().remove();
    } else {
        retable = $('#researchers-content').DataTable({
            "lengthMenu": [20],
            "lengthChange": false,
            "order": [[0, "asc"]],
            "deferRender": true,
            "orderClasses": false
        });
    }
    if (partial) {
        researchers_fetch(retable);
    } else {
        researchers_fetch(retable, "head");
    }
}
function researcher_process(res) {
    if (res.rapas.response.body.ind.absFirst) {
        var ys = $("#researcher-year-start").children("option:selected").val();
        var ye = $("#researcher-year-end").children("option:selected").val();
        console.log("years: " + ys + '-' + ye);
        if (ys < res.rapas.response.body.ind.absFirst) {
            ys = res.rapas.response.body.ind.absFirst;
        }
        if (ye > res.rapas.response.body.ind.absLast) {
            if (ye > year_end()) {
                ye = res.rapas.response.body.ind.absLast;
            } else {
                ye = year_end();
            }
        }
        console.log("corrected years: " + ys + '-' + ye);
        $("#researcher-year-start").children('option').remove();
        $("#researcher-year-end").children('option').remove();
        for (var year = res.rapas.response.body.ind.absFirst; year <= res.rapas.response.body.ind.absLast; year++) {
            $("#researcher-year-start").append(new Option(year, year));
            $("#researcher-year-end").append(new Option(year, year));
        }
        for (var year = res.rapas.response.body.ind.absLast + 1; year <= ye; year++) {
            $("#researcher-year-end").append(new Option(year, year));
        }
        $("#researcher-year-start").val(ys);
        $("#researcher-year-end").val(ye);
    }
    Crumb.name = res.rapas.response.body.person.name;
    $.each($(".researcher-name"), function() {
            $(this).html(res.rapas.response.body.person.name);
    });
    $("#researcher-info-name").html(res.rapas.response.body.person.displayName);
    if (res.rapas.response.body.person.orcid) {
        $("#researcher-info-orcid").html('<a href="https://orcid.org/' + res.rapas.response.body.person.orcid + '" target="_blank">' + res.rapas.response.body.person.orcid + '</a>');
    } else {
        $("#researcher-info-orcid").html("");
    }
    if (res.rapas.response.body.person.rid) {
        $("#researcher-info-rid").html('<a href="http://researcherid.com/rid/' + res.rapas.response.body.person.rid + '" target="_blank">' + res.rapas.response.body.person.rid + '</a>');
    } else {
        $("#researcher-info-rid").html("");
    }
    $("#researcher-info-email").html(res.rapas.response.body.person.email);
    $("#researcher-info-start").html(res.rapas.response.body.person.start + ' -');
    $("#researcher-info-phd").html(res.rapas.response.body.person.phd);
    $("#researcher-info-first-pub").html(res.rapas.response.body.ind.absFirst);
    $("#researcher-info-last-pub").html(res.rapas.response.body.ind.absLast);
    $("#researcher-affiliation tbody").children('tr').remove();
    $.each(res.rapas.response.body.affiliation, function(index, row) {
        var html = '<tr>';
        $.each(row, function(ind, td) {
            html += '<td>' + td + '</td>';
        });
        html += '</tr>';
        $("#researcher-affiliation tbody").append(html);
    });
    if (res.rapas.response.body.ind.absFirst == 'NA') {
        $('#researcher-pubs').hide();
        $('#researcher-no-pubs').show();
    } else {
        $('#researcher-pubs').show();
        $('#researcher-no-pubs').hide();
        var row1 = '<tr class="label">';
        var row2 = '<tr>';
        $.each(["all", "article", "review", "proceedings paper", "abstract", "correction", "other"], function(index, fld) {
            if (fld == 'other' && res.rapas.response.body.summary[fld] > 0) {
                row1 += '<th width="13%" title="Includes types: ' + res.rapas.response.body.summary.doctype_other + '">' + fld.replace(/^\w/, c => c.toUpperCase()) + "</th>";
            } else {
                if (fld == "proceedings paper") {
                    row1 += '<th width="22%">' + fld.replace(/^\w/, c => c.toUpperCase()) + "</th>";
                } else {
                    row1 += '<th width="13%">' + fld.replace(/^\w/, c => c.toUpperCase()) + "</th>";
                }
            }
            row2 += "<td>" + res.rapas.response.body.summary[fld] + "</td>";
        });
        row1 += '</tr>';
        row2 += '</tr>';
        $("#researcher-summary").children('tr').remove();
        $("#researcher-summary").append(row1);
        $("#researcher-summary").append(row2);
        var row1 = '<tr>';
        $.each(["pubs","cites","citesPerPub","citesPerYear","hindex","pInt","pOA"], function(index, fld) {
            if (fld.match(/p(Int|OA)/)) {
                if (res.rapas.response.body.ind[fld] == 'NA') {
                    row1 += "<td>" + res.rapas.response.body.ind[fld] + "</td>";
                } else {
                    row1 += "<td>" + res.rapas.response.body.ind[fld] + " % </td>";
                }
            } else {
                row1 += "<td>" + res.rapas.response.body.ind[fld] + "</td>";
            }
        });
        row1 += '</tr>';
        $("#researcher-indicator > tbody").children('tr').remove();
        $("#researcher-indicator > tbody").append(row1);
        if (res.rapas.response.body.pubCite) {
            $('#pubCite-researcher').html('');
            graph_pubs_vs_cites(res.rapas.response.body.pubCite, 'pubCite-researcher', 1000, 600, "Annual publications and their citations", 1);
        } else {
            $('#pubCite-researcher').html('<div class="pubCite-none">Insufficient data for graph.</div>');
        }
    }
}
function researcher_process_na() {
    var row1 = '<tr>';
    $.each(["pubs","cites","citesPerPub","citesPerYear","hindex","pInt","pOA"], function(index, fld) {
        row1 += "<td>NA</td>";
    });
    row1 += '</tr>';
    $("#researcher-indicator > tbody").children('tr').remove();
    $("#researcher-indicator > tbody").append(row1);
}
function researcher_fetch(orcid) {
    var newORCID = 0;
    console.log("researcher_fetch(" + orcid + ")");
    if (orcid == null) {
        orcid = $("#researcher-orcid").val();
        console.log("researcher_fetch(" + orcid + ")");
    } else {
        if ($("#researcher-orcid").val() != orcid) {
            newORCID = 1;
            console.log("researcher_fetch: new ORCID");
        }
        $("#researcher-orcid").val(orcid);
    }
    if (newORCID) {
        ys = 1900;
        ye = year_end();
        $("#researcher-year-start").children('option').remove();
        $("#researcher-year-end").children('option').remove();
        $("#researcher-year-start").append(new Option(ys, ys));
        $("#researcher-year-end").append(new Option(ye, ye));
        $("#researcher-year-start").val(ys);
        $("#researcher-year-end").val(ye);
        console.log("researcher_fetch: years: " + ys + '-' + ye);
        $.each($("input.researcher-doctype"), function() {
            $(this).prop( "checked", true );
        });
    } else {
        var ys = $("#researcher-year-start").children("option:selected").val();
        console.log("researcher_fetch: year start: " + ys);
        if (ys == null) {
            ys = 1900;
            console.log("researcher_fetch: year start: " + ys);
        }
        var ye = $("#researcher-year-end").children("option:selected").val();
        console.log("researcher_fetch: year end: " + ye);
        if (ye == null) {
            ye = 2030;
            console.log("researcher_fetch: year end: " + ye);
        }
    }
    console.log("researcher_fetch: years: " + ys + '-' + ye);
    var checked = [];
    $.each($("input.researcher-doctype:checked"), function() {
        checked.push($(this).val());
    });
    var doctype = checked.join(";")
    if (doctype.length > 0) {
        var xhr = new XMLHttpRequest();
        console.log('GET /rap-adh/ws/researcher/' + orcid + "/" + ys + "/" + ye + "/" + doctype);
        xhr.open('GET', '/rap-adh/ws/researcher/' + orcid + "/" + ys + "/" + ye + "/" + doctype);
        xhr.onload = function() {
            if (xhr.status === 200) {
                var response = JSON.parse(xhr.response)
                researcher_process(response);
            } else {
                alert('Request failed.  Returned status of ' + xhr.status);
            }
        };
        xhr.send();
    } else {
        researcher_process_na();
    }
}
function researcher_setup() {
    $("#researcher-year-start").change(function() {
        if ($("#researcher-year-start").val() > $("#researcher-year-end").val()) {
            $("#researcher-year-end").val($("#researcher-year-start").val());
        }
        researcher_fetch();
    });
    $("#researcher-year-end").change(function() {
        if ($("#researcher-year-end").val() < $("#researcher-year-start").val()) {
            $("#researcher-year-start").val($("#researcher-year-end").val());
        }
        researcher_fetch();
    });
    $("input.researcher-doctype").change(function() {
        researcher_fetch();
    });
    $(".back-to-researchers").click(function() {
        state_set("researchers");
    });
    $("#researcher-publications").click(function() {
        records_fetch('orcid:' + $("#researcher-orcid").val(), 1);
        state_set("records");
    });
}
function records_process(res) {
    $("#records-content").children('li').remove();
    if (res.rapas.response.years) {
        var yearStart = $("#records-year-start").children("option:selected").val();
        var yearEnd = $("#records-year-end").children("option:selected").val();
        $("#records-year-start").children('option').remove();
        $("#records-year-end").children('option').remove();
        var yearMin = 9999;
        var yearMax = 0;
        $.each(res.rapas.response.years, function(index, val) {
            if (val < yearMin) {
                yearMin = val;
            }
            if (val > yearMax) {
                yearMax = val;
            }
            $("#records-year-start").append(new Option(val, val));
            $("#records-year-end").append(new Option(val, val));
        });
        if ((yearStart != null) && (yearStart != "") && (yearStart >= yearMin)) {
            $("#records-year-start").val(yearStart);
        } else {
            $("#records-year-start").val(yearMin);
        }
        if ((yearEnd != null) && (yearEnd != "") && (yearEnd <= yearMax)) {
            $("#records-year-end").val(yearEnd);
        } else {
            $("#records-year-end").val(yearMax);
        }
    }
    if (res.rapas.response.doctype) {
        var type = $("#records-doctype").children("option:selected").val();
        $("#records-doctype").children('option').remove();
        $("#records-doctype").append(new Option("All", ""));
        $.each(Doctype, function(key, val) {
            if (res.rapas.response.doctype[key]) {
                $("#records-doctype").append(new Option(val, key));
            }
        });
        $("#records-doctype").val(type);
    }
    if (res.rapas.response.dtu) {
        var org = $("#records-dtu").children("option:selected").val();
        $("#records-dtu").children('option').remove();
        $("#records-dtu").append(new Option("All", ""));
        $.each(Organisation, function(key, val) {
            if (res.rapas.response.dtu[key]) {
                $("#records-dtu").append(new Option(val, key));
            }
        });
        $("#records-dtu").val(org);
    }
    if (res.rapas.response.impact) {
        var imp = $("#records-impact").children("option:selected").val();
        $("#records-impact").children('option').remove();
        $("#records-impact").append(new Option("All", ""));
        $.each(Impact, function(key, val) {
            if (res.rapas.response.impact[key]) {
                $("#records-impact").append(new Option(val, key));
            }
        });
        $("#records-impact").val(imp);
    }
    if (res.rapas.response.access) {
        var acc = $("#records-access").children("option:selected").val();
        $("#records-access").children('option').remove();
        $("#records-access").append(new Option("All", ""));
        $.each(Access, function(key, val) {
            if (res.rapas.response.access[key]) {
                $("#records-access").append(new Option(val, key));
            }
        });
        $("#records-access").val(acc);
    }
    $.each($(".researcher-name"), function() {
            $(this).html(res.rapas.response.name);
    });
    var page = res.rapas.request.page;
    var from = (page - 1) * 10 + 1;
    $(".records-from").html(from);
    if ((from + 9) < res.rapas.response.hits) {
        $(".records-to").html(from + 9);
    } else {
        $(".records-to").html(res.rapas.response.hits);
    }
    $(".records-total").html(res.rapas.response.hits);
    $(".records-paging").html(paging_html (page, res.rapas.response.pages, "records_fetch(null, PAGE)"));
    var year = 0;
    $.each(res.rapas.response.body, function(index, rec) {
        if (year != rec.year) {
            year = rec.year;
            $("#records-content").append("<li><h2>" + year + "</h2></li>");
        }
        var html = '<li>';
        html += '<h5><a class="records-title" onClick="record_fetch(' + "'" + rec.ut + "'" + '); state_set(' + "'record'" + ');">' + html_encode(rec.title) + '</a></h5>';
        html += '<div>' + rec.authors + '</div>';
        html += '<div><span class="records-source">' + rec.source + '</span>(' + rec.pubdate + ')</div>';
        html += '<div><span class="records-ut">' + rec.ut + '</span><span class="records-refs">References: ' + rec.refs + '</span><span class="records-cited">Citations: ' + rec.cited + '</span>';
        if (rec.doi) {
            html += '<span class="records-doi">DOI: ' + rec.doi + '</span>';
        }
        html += '</div>';
        html += '</li>';
        $("#records-content").append(html);
    });
}
function records_fetch_url(id, page, excel) {
    var args = [];
    if (id == null) {
        id = $("#records-id").val();
    } else {
        $("#records-id").val(id);
        $.each(["year-start", "year-end", "doctype", "dtu", 'impact', 'access', 'search'], function(index, fld) {
            $("#records-" + fld).val("");
        });
    }
    if (id != null) {
        args.push("id=" + id);
    }
    if (page == null) {
        page = $("#records-page").val();
        if (page == null) {
            page = 1;
        }
    } else {
        $("#records-page").val(page);
    }
    args.push("page=" + page);
    $.each(["year-start", "year-end", "doctype", "dtu", 'impact', 'access'], function(index, fld) {
        var arg = $("#records-" + fld).children("option:selected").val();
        if (arg != null) {
            if (arg != "") {
                args.push(fld + '=' + arg);
            }
        }
    });
    var sea = $("#records-search").val();
    if (sea != null) {
        if (!sea.match(/^ *$/)) {
           args.push('sea=' + escape(sea));
        }
    }
    var xhr = new XMLHttpRequest();
    if (excel) {
        return "/rap-adh/ws/records_excel/publications.xlsx" + "?" + args.join('&');
    } else {
        return "/rap-adh/ws/records" + "?" + args.join('&');
    }
}
function records_fetch(id, page) {
    var url = records_fetch_url(id, page, 0);
    var xhr = new XMLHttpRequest();
    console.log("GET " + url);
    xhr.open('GET', url);
    xhr.onload = function() {
        if (xhr.status === 200) {
            var response = JSON.parse(xhr.response)
            records_process(response);
        } else {
            alert('Request failed.  Returned status of ' + xhr.status);
        }
    };
    xhr.send();
}
function records_fetch_excel() {
    var url = records_fetch_url(null, 0, 1);

    window.location.assign(url);
}
function records_setup() {
    $(".back-to-researcher").click(function() {
        state_set("researcher");
    });
    $("#records-year-start").change(function() {
        var yearStart = $("#records-year-start").children("option:selected").val();
        var yearEnd = $("#records-year-end").children("option:selected").val();
        if (yearStart > yearEnd) {
            $("#records-year-end").val(yearStart);
        }
        records_fetch(null, 1);
    });
    $("#records-year-end").change(function() {
        var yearStart = $("#records-year-start").children("option:selected").val();
        var yearEnd = $("#records-year-end").children("option:selected").val();
        if (yearEnd < yearStart) {
            $("#records-year-start").val(yearEnd);
        }
        records_fetch(null, 1);
    });
    $("#records-doctype").change(function() {
        records_fetch(null, 1);
    });
    $("#records-dtu").change(function() {
        records_fetch(null, 1);
    });
    $("#records-impact").change(function() {
        records_fetch(null, 1);
    });
    $("#records-access").change(function() {
        records_fetch(null, 1);
    });
}
function record_process(res) {
    var rec = res.rapas.response.body;
    if (rec.title) {
        $("#record-title").html(rec.title);
    } else {
        $("#record-title").html("[Missing title]");
    }
    var html = "";
    $.each(rec.names, function(index, author) {
        html += '<span class="author-name">' + author.name + '</span>';
        if (author.id) {
            html += '<span class="author-address">[' + author.id + ']</span>';
        } else {
            html += '<span class="author-address"></span>';
        }
    });
    if (html) {
        $("#record-authors").html(html);
    } else {
        $("#record-authors").html("");
    }
    if (rec.source) {
        $("#record-source").html(rec.source);
    } else {
        $("#record-source").html("");
    }
    var pub = {
        "volume":  "Volume",
        "issue":   "Issue",
        "pages":   "Pages",
        "issn":    "ISSN",
        "eissn":   "E-ISSN",
        "isbn":    "ISBN",
        "doi":     "DOI",
        "pubdate": "Published",
        "ut":      "Web of Science",
        "refs":    "References",
        "cited":   "Citations"
    };
    var link = [];
    if (rec['doi']) {
        link['doi'] = 'http://doi.org/' + rec['doi'];
    }
    if (rec['refs']) {
        link['refs'] = 'http://apps.webofknowledge.com/InterService.do?product=WOS&toPID=WOS&action=AllCitationService&isLinks=yes&fromPID=WOS&search_mode=CitedRefList&parentProduct=WOS&UT=' + rec['ut'];
    }
    if (rec['cited']) {
        link['cited'] = 'http://gateway.webofknowledge.com/gateway/Gateway.cgi?GWVersion=2&SrcApp=VIVO&SrcAuth=TRINTCEL&DestLinkType=CitingArticles&DestApp=WOS_CPL&KeyUT=' + rec['ut'];
    }
    link['ut'] = 'http://gateway.webofknowledge.com/gateway/Gateway.cgi?GWVersion=2&SrcApp=VIVO&SrcAuth=TRINTCEL&DestLinkType=FullRecord&DestApp=WOS_CPL&KeyUT=' + rec['ut'];
    html = "";
    $.each(pub, function(fld, label) {
        if (rec[fld]) {
            if (link[fld]) {
                html += '<span class="pub-label">' + label + ': </span><span class="pub-data"><a target="_blank" href="' + link[fld] + '">' + rec[fld] + '</a></span>';
            } else {
                html += '<span class="pub-label">' + label + ': </span><span class="pub-data">' + rec[fld] + '</span>';
            }
        }
    });
    $("#record-pub").html(html);
    html = "";
    $.each(rec.abstract, function(index, val) {
        html += "<p>" + val + "</p>";
    });
    if (html) {
        $("#record-abstract").html('<div class="pub-label">Abstract</div>' + html);
    } else {
        $("#record-abstract").html("");
    }
    html = "";
    $.each(rec.keywords, function(index, val) {
        html += '<span class="record-keyword">' + val + ";</span>";
    });
    if (html) {
        $("#record-keywords").html('<div class="pub-label">Keywords</div>' + html);
    } else {
        $("#record-keywords").html("");
    }
    html = "";
    $.each(rec.category, function(index, val) {
        html += '<span class="record-category">' + val + ";</span>";
    });
    if (html) {
        $("#record-class").html('<div class="pub-label">Categories/Classification</div><span class="pub-label">Web of Science Categories:</span>' + html);
    } else {
        $("#record-class").html("");
    }
    var html = "";
    $.each(rec.address, function(index, val) {
        html += '<tr><td class="address-id">[' + val.id + ']</td><td class="author-text">' + val.text + '</td></tr>';
    });
    if (html) {
        $("#record-address").html('<div class="pub-label">Author Addresses</div><table>' + html + '</table>');
    } else {
        $("#record-address").html("");
    }
    html = "";
    $.each(rec.grants, function(index, val) {
        html += '<li class="record-funder">' + val.name + '</li>';
        if (val.id) {
            var ids = [];
            var done = {};
            $.each(val.id.split(','), function(i, v) {
                v = v.trim();
                if (!done[v]) {
                    done[v] = 1;
                    ids.push (v);
                }
            });
            html += '<li class="record-grant">' + ids.join(', ') + '</li>';
        }
    });
    if (html) {
        $("#record-funding").html('<div class="pub-label">Funding</div><ul>' + html + '</ul>');
    } else {
        $("#record-funding").html("");
    }
    if (rec.doctype) {
        $("#record-doctype").html('<span class="pub-label">Document Type:</span>' + rec.doctype);
    } else {
        $("#record-doctype").html("");
    }
    html  = '<div id="ind-cp" class="pub-label"><a onClick="' + "$('#ind-cp').hide(); $('#ind-ex').show();" + '">' +
            '<svg height="16" width="16" style="vertical-align: middle;"><polygon points="0,0 0,12 12,6 0,0" style="fill:#4472c4;stroke:white;stroke-width:1"></polygon></svg>' +
            'InCites Indicators</a></div>';
    html += '<div id="ind-ex" style="display: none;"><div class="pub-label"><a onClick="' + "$('#ind-cp').show(); $('#ind-ex').hide();"  + '">' +
            '<svg height="16" width="16" style="vertical-align: middle;"><polygon points="0,0 12,0 6,12 0,0" style="fill:#4472c4;stroke:white;stroke-width:1"></polygon></svg>' +
            'InCites Indicators</a></div>';
    if (rec.ind.tot_cites == null) {
        html += '<div>This record is not yet covered by InCites</div>';
    } else {
        var ind = {
            "tot_cites":        "Times Cited",
            "nci":              "Category Normalized Citation Impact (CNCI)",
            "percentile10":     "In top 10%",
            "percentile1":      "In top 1%",
            "percentile":       "Percentile in Subject Area",
            "is_industry":      "Industry Collaboration",
            "is_institution":   "Institution Collaboration",
            "is_international": "International Collaboration",
            "oa_flag":          "Open Access",
            "oa_types":         "Open Access Type",
            "esi_most_cited":   "Highly Cited Paper",
            "hot_paper":        "Hot Paper",
            "ae_rate":          "Category Expected Citations",
            "impact_factor":    "Journal Impact Factor",
            "jou_act_exp_cit":  "Journal Normalized Citation Impact (JNCI)",
            "jou_exp_cit":      "Journal Expected Citations"
        };
        html += '<table>';
        $.each(ind, function(fld, label) {
            if (fld != 'oa_types' || rec.ind['oa_flag'] != 'No') {
                if (typeof(rec.ind[fld]) == 'number') {
                    html += '<tr><td class="pub-label">' + label + ': </td><td class="pub-data-num">' + rec.ind[fld] + '</td></tr>';
                } else if (rec.ind[fld].match(/^[\.0-9]+$/)) {
                    html += '<tr><td class="pub-label">' + label + ': </td><td class="pub-data-num">' + rec.ind[fld] + '</td></tr>';
                } else {
                    html += '<tr><td class="pub-label">' + label + ': </td><td class="pub-data-center">' + rec.ind[fld] + '</td></tr>';
                }
            }
        });
        html += '</table>';
    }
    html += '</div>';
    $("#record-indicators").html(html);
    $(".ut-display").html(rec.ut);
    Crumb.ut = rec.ut;
}
function record_fetch(ut) {
    var xhr = new XMLHttpRequest();
    var url = "/rap-adh/ws/record/" + ut;
    console.log("GET " + url);
    xhr.open('GET', url);
    xhr.onload = function() {
        if (xhr.status === 200) {
            var response = JSON.parse(xhr.response)
            record_process(response);
        } else {
            alert('Request failed.  Returned status of ' + xhr.status);
        }
    };
    xhr.send();
}
function record_setup() {
    $(".back-to-researcher").click(function() {
        state_set("researcher");
    });
}
function departments_process(res) {
    var ys = $("#departments-year-start").children("option:selected").val();
    var ye = $("#departments-year-end").children("option:selected").val();
    if (ys < res.rapas.response.body.yearmin) {
        ys = res.rapas.response.body.yearmin;
    }
    if (ye > res.rapas.response.body.yearmax) {
        ye = res.rapas.response.body.yearmax;
    }
    $("#departments-year-start").children('option').remove();
    $("#departments-year-end").children('option').remove();
    for (var year = res.rapas.response.body.yearmin; year <= res.rapas.response.body.yearmax; year++) {
        $("#departments-year-start").append(new Option(year, year));
        $("#departments-year-end").append(new Option(year, year));
    }
    $("#departments-year-start").val(ys);
    $("#departments-year-end").val(ye);
    var colors1 = [0, 0, "#2a3feb", "#1ed082", "#ffb428", "#d90077"];
    var colors2 = [0, 0, "#929ded", "#8eecc2", "#ffe899", "#fb98cd"];
    var html = '';
    var dtu = [0, 0, 0, 0, 0, 0];
    for (var i = 0, l = res.rapas.response.body.rows.length; i < l; i++) {
        var id = res.rapas.response.body.rows[i].shift();
        html += '<tr>';
        $.each(res.rapas.response.body.rows[i], function(index, col) {
            if (index > 1) {
                html += '<td class="col-' + index + '" style="position: relative;">';
                if (i == 0) {
                    html += '<div class="head" style="background: ' + colors1[index] + '; width: ' + col[1] + '%; position: absolute; top: 0; left: 0; height: 98%;">' + col[0] + '</div>';
                    dtu[index] = col[1];
                    html += '<div style="width: ' + dtu[index] + '%; height: 100%; border-right: 1px solid red; z-index: 10; position: absolute; top: 0; left: 0;">&nbsp;</div>';

                } else {
                    html += '<div style="background: ' + colors2[index] + '; width: ' + col[1] + '%; position: absolute; top: 0; left: 0; height: 98%;">' + col[0] + '</div>';
                    html += '<div style="width: ' + dtu[index] + '%; height: 100%; border-right: 1px solid red; z-index: 10; position: absolute; top: 0; left: 0;">&nbsp;</div>';
                }
                html += '</td>';
            } else {
                if (i == 0) {
                    html += '<td class="col-' + index + ' head">' + col + '</td>';
                } else {
                    html += '<td class="col-' + index + '">' + col + '</td>';
                }
            }
        });
        html += '</tr>';
    }
    $("#departments > tbody").html(html);
}
function departments_fetch() {
    var ys = $("#departments-year-start").children("option:selected").val();
    if (ys == null) {
        ys = 1900;
        console.log("researcher_fetch: year start: " + ys);
    }
    var ye = $("#departments-year-end").children("option:selected").val();
    console.log("researcher_fetch: year end: " + ye);
    if (ye == null) {
        ye = year_end();
        console.log("researcher_fetch: year end: " + ye);
    }
    console.log("departments_fetch: years: " + ys + '-' + ye);
    var checked = [];
    $.each($("input.departments-doctype:checked"), function() {
        checked.push($(this).val());
    });
    var doctype = checked.join(";")
    if (doctype.length > 0) {
        var xhr = new XMLHttpRequest();
        console.log('GET /rap-adh/ws/departments/' + ys + "/" + ye + "/" + doctype);
        xhr.open('GET', '/rap-adh/ws/departments/' + ys + "/" + ye + "/" + doctype);
        xhr.onload = function() {
            if (xhr.status === 200) {
                var response = JSON.parse(xhr.response)
                departments_process(response);
            } else {
                alert('Request failed.  Returned status of ' + xhr.status);
            }
        };
        xhr.send();
    }
}
function departments_setup() {
    $("#departments-year-start").change(function() {
        if ($("#departments-year-start").val() > $("#departments-year-end").val()) {
            $("#departments-year-end").val($("#departments-year-start").val());
        }
        departments_fetch();
    });
    $("#departments-year-end").change(function() {
        if ($("#departments-year-end").val() < $("#departments-year-start").val()) {
            $("#departments-year-start").val($("#departments-year-end").val());
        }
        departments_fetch();
    });
    $("input.departments-doctype").change(function() {
        departments_fetch();
    });
    departments_fetch();
}
function units_process(res) {
    var html = '';
    $.each(res.options, function(index, dep) {
        if (dep[0] != "") {
            var dslink = dep[0] + '-sec-link';
            var dslist = dep[0] + '-sec-list';
            html += '<tr><td class="units-dep"><a onClick="unit_fetch(' + "'dep:" + dep[0] + "'); state_set('unit');" + '">' + dep[1] + '</a>';
            html += '<div id="' + dslink + '"><a class="section-link" onClick="' + "$('#" + dslink + "').hide(); $('#" + dslist + "').show();" + '">' +
                    '<svg height="16" width="16" style="vertical-align: middle;"><polygon points="0,0 0,12 12,6 0,0" style="fill:#4472c4;stroke:white;stroke-width:1"></polygon></svg>' +
                    'Sections</a></div>';
            var link = '<a class="section-link" onClick="' + "$('#" + dslist + "').hide(); $('#" + dslink + "').show();" + '">' +
                       '<svg height="16" width="16" style="vertical-align: middle;"><polygon points="0,0 12,0 6,12 0,0" style="fill:#4472c4;stroke:white;stroke-width:1"></polygon></svg>' +
                       'Sections</a>';
            html += '<table id="' + dslist + '" class="section-list" style="display: none;">';
            $.each(res[dep[0]], function(index, sec) {
                if (sec[0] != "") {
                    html += '<tr><td>' + link + '</td><td class="units-sec"><a onClick="unit_fetch(' + "'sec:" + sec[0] + "'); state_set('unit');" + '">' + sec[1] + '</a></td></tr>';
                    link = '';
                }
            });
            html += '</table></</td>';
            if (res.sheet[dep[0]]) {
                html += '<td class="unit-sheet"><a href="/sheets/' + res.sheet[dep[0]].sheet + '">' + 'Based on <img src="' + url_theme + '/images/excel.png" width="42" height="42"/></a><br/>Loaded: ' + res.sheet[dep[0]].upd + '</td>';
            }
            html += '</tr>';
        }
    });
    $("#units-content tbody").html(html);
}
function units_fetch() {
}
function units_setup(base, theme) {
    if (base != null) {
        url_base = base;
    }
    if (theme != null) {
        url_theme = theme;
    }
}
function unit_process(res) {
    if (res.rapas.response.body.yearmin) {
        var ys = $("#unit-year-start").children("option:selected").val();
        var ye = $("#unit-year-end").children("option:selected").val();
        if (ys < res.rapas.response.body.yearmin) {
            ys = res.rapas.response.body.yearmin;
        }
        if (ye > res.rapas.response.body.yearmax) {
            if (ye > year_end()) {
                ye = res.rapas.response.body.yearmax;
            } else {
                ye = year_end();
            }
        }
        $("#unit-year-start").children('option').remove();
        $("#unit-year-end").children('option').remove();
        for (var year = res.rapas.response.body.yearmin; year <= res.rapas.response.body.yearmax; year++) {
            $("#unit-year-start").append(new Option(year, year));
            $("#unit-year-end").append(new Option(year, year));
        }
        for (var year = res.rapas.response.body.yearmax + 1; year <= year_end(); year++) {
            $("#unit-year-end").append(new Option(year, year));
        }
        $("#unit-year-start").val(ys);
        $("#unit-year-end").val(ye);
    }
    Crumb.unit = res.rapas.response.body.name;
    $(".unit-name").html(Crumb.unit);
    if (res.rapas.response.body.leader) {
        var html = '';
        if (res.rapas.response.body.leader.orcid && res.rapas.response.body.leader.orcid != "NA") {
            html += '<span class="label">Head:</span><span class="value"><a onClick="' + "researcher_fetch('" + res.rapas.response.body.leader.orcid + "'); state_set('researcher');" + '">';
            html += res.rapas.response.body.leader.name;
            html += '</a></span>';
        } else {
            html += '<span class="label">Head:</span><span class="value">';
            html += res.rapas.response.body.leader.name;
            html += '</span>';
        }
        html += '</span>';
        $("#unit-leader").html(html);
    } else {
        $("#unit-leader").html('');
    }
    if (res.rapas.response.body.dep) {
        Crumb.over.dep = res.rapas.response.body.dep;
        if (res.rapas.response.body.sec) {
            Crumb.over.sec = res.rapas.response.body.sec;
        } else {
            Crumb.over.sec = "";
        }
        var html = '<button onClick="researchers_setup(1); state_set(' + "'researchers'" + ');">View list</button>';
        $("#unit-researchers").html(html);
    } else {
        $("#unit-researchers").html('');
    }



    if (res.rapas.response.body.summary.all == 0) {
        $('#unit-pubs').hide();
        $('#unit-no-pubs').show();
    } else {
        $('#unit-pubs').show();
        $('#unit-no-pubs').hide();
        var row1 = '<tr class="label">';
        var row2 = '<tr>';
        $.each(["all", "article", "review", "proceedings paper", "abstract", "correction", "other"], function(index, fld) {
            if (fld == 'other' && res.rapas.response.body.summary[fld] > 0) {
                row1 += '<th width="13%" title="Includes types: ' + res.rapas.response.body.summary.doctype_other + '">' + fld.replace(/^\w/, c => c.toUpperCase()) + "</th>";
            } else {
                if (fld == "proceedings paper") {
                    row1 += '<th width="22%">' + fld.replace(/^\w/, c => c.toUpperCase()) + "</th>";
                } else {
                    row1 += '<th width="13%">' + fld.replace(/^\w/, c => c.toUpperCase()) + "</th>";
                }
            }
            row2 += '<td>' + res.rapas.response.body.summary[fld] + "</td>";
        });
        row1 += '</tr>';
        row2 += '</tr>';
        $("#unit-summary").children('tr').remove();
        $("#unit-summary").append(row1);
        $("#unit-summary").append(row2);
        var row1 = '<tr>';
        $.each(["pubs","cites","citesPerPub","citesPerYear","hindex","pCited","cnci","top10","top1","pInt","pOA"], function(index, fld) {
            if (fld.match(/(p(Cited|Int|OA)|top10|top1)/)) {
                if (res.rapas.response.body.ind[fld] == 'NA') {
                    row1 += "<td>" + res.rapas.response.body.ind[fld] + "</td>";
                } else {
                    row1 += "<td>" + res.rapas.response.body.ind[fld] + " % </td>";
                }
            } else {
                row1 += "<td>" + res.rapas.response.body.ind[fld] + "</td>";
            }
        });
        row1 += '</tr>';
        $("#unit-indicator > tbody").children('tr').remove();
        $("#unit-indicator > tbody").append(row1);
        if (res.rapas.response.body.pubCite) {
            $('#pubCite-unit').html('');
            graph_pubs_vs_cites(res.rapas.response.body.pubCite, 'pubCite-unit', 1000, 600, "Annual publications and their citations", 1);
        } else {
            $('#pubCite-unit').html('<div class="pubCite-none">Insufficient data for graph.</div>');
        }
    }
}
function unit_process_na() {
    var row1 = '<tr>';
    $.each(["pubs","cites","citesPerPub","citesPerYear","hindex","pCited","cnci","top10","top1","pInt","pOA"], function(index, fld) {
        row1 += "<td>NA</td>";
    });
    row1 += '</tr>';
    $("#unit-indicator > tbody").children('tr').remove();
    $("#unit-indicator > tbody").append(row1);
}
function unit_fetch(id) {
    var newid = 0;
    if (id == null) {
        id = $("#unit-id").val();
    } else {
        if ($("#unit-id").val() != id) {
            newid = 1;
        }
        $("#unit-id").val(id);
    }
    if (newid) {
        ys = 1900;
        ye = year_end();
        $("#unit-year-start").children('option').remove();
        $("#unit-year-end").children('option').remove();
        $("#unit-year-start").append(new Option(ys, ys));
        $("#unit-year-end").append(new Option(ye, ye));
        $("#unit-year-start").val(ys);
        $("#unit-year-end").val(ye);
        $.each($("input.unit-doctype"), function() {
            $(this).prop( "checked", true );
        });
    } else {
        var ys = $("#unit-year-start").children("option:selected").val();
        if (ys == null) {
            ys = 1900;
        }
        var ye = $("#unit-year-end").children("option:selected").val();
        if (ye == null) {
            ye = 2030;
        }
    }
    var checked = [];
    $.each($("input.unit-doctype:checked"), function() {
        checked.push($(this).val());
    });
    var doctype = checked.join(";")
    if (doctype.length > 0) {
        var xhr = new XMLHttpRequest();
        var url = '/rap-adh/ws/unit?id=' + id + '&syear=' + ys + '&eyear=' + ye + '&doctype=' + doctype;
        console.log('GET ' + url);
        xhr.open('GET', url);
        xhr.onload = function() {
            if (xhr.status === 200) {
                var response = JSON.parse(xhr.response)
                unit_process(response);
            } else {
                alert('Request failed.  Returned status of ' + xhr.status);
            }
        };
        xhr.send();
    } else {
        unit_process_na();
    }
}
function unit_setup() {
    $("#unit-year-start").change(function() {
        if ($("#unit-year-start").val() > $("#unit-year-end").val()) {
            $("#unit-year-end").val($("#unit-year-start").val());
        }
        unit_fetch();
    });
    $("#unit-year-end").change(function() {
        if ($("#unit-year-end").val() < $("#unit-year-start").val()) {
            $("#unit-year-start").val($("#unit-year-end").val());
        }
        unit_fetch();
    });
    $("input.unit-doctype").change(function() {
        unit_fetch();
    });
    $(".back-to-units").click(function() {
        state_set("units");
    });
    $("#unit-publications").click(function() {
        records_fetch($("#unit-id").val(), 1);
        state_set("records");
    });
}
function overlay_add(svgContainer, bar, actual, xScale, yScale, width, margin, count) {
    d3.select(bar)
        .transition()
        .duration(300)
        .attr('opacity', 0.8)
        .attr('x', (a) => xScale(a.year) - 3)
        .attr('width', xScale.bandwidth() + 6);
    var overlay = svgContainer.select('#overlay');
    var left = xScale(actual.year) - 3;
    if ((left + 148) > (width + margin - 20)) {
        left -= ((left + 148) - (width + margin) + 30);
    }
    var top = yScale(count) - 75;
    overlay
        .append('rect')
        .attr('class', 'text-box')
        .attr('x', left)
        .attr('y', top)
        .attr('rx', 6)
        .attr('ry', 6)
        .attr('height', 60)
        .attr('width', 148);
    left += 3;
    top += 15;
    overlay
        .append('text')
        .attr('class', 'current-pubs-title')
        .attr('x', left)
        .attr('y', top)
        .attr('text-anchor', 'start')
        .text((a) => actual.year);
    top += 14;
    overlay
        .append('text')
        .attr('class', 'current-pubs')
        .attr('x', left + 32)
        .attr('y', top)
        .attr('text-anchor', 'end')
        .text((a) => actual.pubs);
    overlay
        .append('text')
        .attr('class', 'current-pubs')
        .attr('x', left + 36)
        .attr('y', top)
        .attr('text-anchor', 'start')
        .text((a) => "Total publications");
    top += 12;
    overlay
        .append('text')
        .attr('class', 'current-pubs')
        .attr('x', left + 32)
        .attr('y', top)
        .attr('text-anchor', 'end')
        .text((a) => actual.oa);
    overlay
        .append('text')
        .attr('class', 'current-pubs')
        .attr('x', left + 36)
        .attr('y', top)
        .attr('text-anchor', 'left')
        .text((a) => "OA publications");
    top += 12;
    overlay
        .append('text')
        .attr('class', 'current-pubs')
        .attr('x', left + 32)
        .attr('y', top)
        .attr('text-anchor', 'end')
        .text((a) => actual.cites);
    overlay
        .append('text')
        .attr('class', 'current-pubs')
        .attr('x', left + 36)
        .attr('y', top)
        .attr('text-anchor', 'left')
        .text((a) => "Citations");
}
function overlay_del(bar, xScale) {
    d3.selectAll('.pubs')
        .attr('opacity', 1);
    d3.select(bar)
        .transition()
        .duration(300)
        .attr('opacity', 1)
        .attr('x', (a) => xScale(a.year))
        .attr('width', xScale.bandwidth());
    d3.selectAll('.current-pubs-title').remove();
    d3.selectAll('.current-pubs').remove();
    d3.selectAll('.text-box').remove();
}
function graph_pubs_vs_cites(data, id, width, height, title, separateScale) {
    const svgContainer = d3.select('#' + id);
    svgContainer.selectAll('svg').remove();
    const svg = svgContainer.append('svg');

    $('#' + id).css ('width', width + 'px');
    $('#' + id).css ('height', height + 'px');

    const margin  = 80;
    width  -= (2 * margin);
    height -= (2 * margin);
    var maxY1 = 0;
    var maxY2 = 0;
    data.forEach(function(e) {
        if (e.pubs > maxY1) {
            maxY1 = e.pubs;
        }
        if (e.cites > maxY2) {
            maxY2 = e.cites;
        }
    });
    if (!separateScale) {
        if (maxY1 > maxY2) {
            maxY2 = maxY1;
        } else {
            maxY1 = maxY2;
        }
    }
    svg.append('text')
        .attr('class', 'title')
        .attr('x', width / 2 + margin)
        .attr('y', 30)
        .attr('text-anchor', 'middle')
        .text(title);
    svg.append('text')
        .attr('class', 'note')
        .attr('x', width / 2 + margin)
        .attr('y', height + margin + 60)
        .attr('text-anchor', 'middle')
        .text('(*) Times cited = total number of citations received thus far for the publications published in a specific year.');
    var x = width / 2 + margin - 260;
    svg.append('rect')
        .attr('class', 'lcit')
        .attr('x', x)
        .attr('y', 60)
        .attr('height', 1)
        .attr('width', 60)
    svg.append('circle')
        .attr('class', 'point')
        .attr("r", 3)
        .attr('cx', x)
        .attr('cy', 60)
    svg.append('circle')
        .attr('class', 'point')
        .attr("r", 3)
        .attr('cx', x + 60)
        .attr('cy', 60)
    svg.append('text')
        .attr('class', 'leg')
        .attr('x', x + 66)
        .attr('y', 65)
        .attr('text-anchor', 'left')
        .text('Times Cited (*)');
    x += 160;
    svg.append('rect')
        .attr('class', 'lpub')
        .attr('x', x)
        .attr('y', 50)
        .attr('height', 20)
        .attr('width', 60)
    svg.append('text')
        .attr('class', 'leg')
        .attr('x', x + 66)
        .attr('y', 65)
        .attr('text-anchor', 'left')
        .text('Publications');
    x += 160;
    svg.append('rect')
        .attr('class', 'loa')
        .attr('x', x)
        .attr('y', 50)
        .attr('height', 20)
        .attr('width', 60)
    svg.append('text')
        .attr('class', 'leg')
        .attr('x', x + 66)
        .attr('y', 65)
        .attr('text-anchor', 'left')
        .text('OA Publications');
    const chart = svg.append('g')
        .attr('transform', `translate(${margin}, ${margin})`);

    const line = svg.append('g')
        .attr('transform', `translate(${margin}, ${margin})`);

    const points = svg.append('g')
        .attr('transform', `translate(${margin}, ${margin})`);

    const pointGroups = points.selectAll()
        .data(data)
        .enter()
        .append('g');

    const xScale = d3.scaleBand()
        .range([0, width])
        .domain(data.map((s) => s.year))
        .padding(0.2);

    const yScale = d3.scaleLinear()
        .range([height, 0])
        .domain([0, maxY1]);

    const y2Scale = d3.scaleLinear()
        .range([height, 0])
        .domain([0, maxY2]);

    const makeYLines = () => d3.axisLeft()
        .scale(yScale);

    chart.append('g')
        .attr('transform', `translate(0, ${height})`)
        .call(d3.axisBottom(xScale))
        .selectAll("text")
            .attr("y",6)
            .attr("x", -12)
            .attr("dy", ".35em")
            .attr("transform", "rotate(-60)")
            .style("text-anchor", "end");

    chart.append('g')
        .call(d3.axisLeft(yScale));

    chart.append('g')
        .attr("transform", `translate(${width},0)`)
        .call(d3.axisRight(y2Scale));

    if (!separateScale) {
        chart.append('g')
            .attr('class', 'grid')
            .call(makeYLines()
                .tickSize(-width, 0, 0)
                .tickFormat('')
            );
    }

    const barGroups = chart.selectAll()
        .data(data)
        .enter()
        .append('g');

    barGroups
        .append('rect')
        .attr('class', 'bar')
        .attr('x', (g) => xScale(g.year))
        .attr('y', (g) => yScale(g.pubs))
        .attr('height', (g) => height - yScale(g.pubs))
        .attr('width', xScale.bandwidth())
        .on('mouseenter', function (actual, i) {
            overlay_add(svgContainer, this, actual, xScale, yScale, width, margin, actual.pubs);
        })
        .on('mouseleave', function () {
            overlay_del(this, xScale);
        });
    barGroups
        .append('rect')
        .attr('class', 'bar-oa')
        .attr('x', (g) => xScale(g.year))
        .attr('y', (g) => yScale(g.oa))
        .attr('height', (g) => height - yScale(g.oa))
        .attr('width', xScale.bandwidth())
        .on('mouseenter', function (actual, i) {
            overlay_add(svgContainer, this, actual, xScale, yScale, width, margin, actual.oa);
        })
        .on('mouseleave', function () {
            overlay_del(this, xScale);
        });

    line.append('path')
        .datum(data)
        .attr("fill", "none")
        .attr("stroke-width", 1.5)
        .attr("d", d3.line()
            .x(function(d) { return xScale(d.year) + (xScale.bandwidth() / 2) })
            .y(function(d) { return y2Scale(d.cites) })
        );
    pointGroups
        .append('circle')
        .attr('class', 'point')
        .attr("r", 3)
        .attr('cx', (g) => xScale(g.year) + (xScale.bandwidth() / 2))
        .attr('cy', (g) => y2Scale(g.cites))
        .on('mouseenter', function (actual, i) {
            overlay_add(svgContainer, this, actual, xScale, y2Scale, width, margin, actual.cites);
        })
        .on('mouseleave', function () {
            overlay_del(this, xScale);
        });

    svg.append('text')
        .attr('class', 'label')
        .attr('x', -(height / 2) - margin)
        .attr('y', margin / 2.4)
        .attr('transform', 'rotate(-90)')
        .attr('text-anchor', 'middle')
        .text('Publications');

    svg.append('text')
        .attr('class', 'label')
        .attr('x', (height / 2) + margin)
        .attr('y', -(width + margin * 1.7))
        .attr('transform', 'rotate(90)')
        .attr('text-anchor', 'middle')
        .text('Times cited');

    svg.append('g')
        .attr('id', 'overlay')
        .attr('transform', `translate(${margin}, ${margin})`);
}
function rapas_update(res) {
    if (res == null) {
        var xhr = new XMLHttpRequest();
        var url = '/rap-adh/ws/last_update';
        console.log('GET ' + url);
        xhr.open('GET', url);
        xhr.onload = function() {
            if (xhr.status === 200) {
                var res = JSON.parse(xhr.response)
                $(".page-home-rapas-update").html("updated: " + res.rapas.response.body.updlong);
            } else {
                alert('Request failed.  Returned status of ' + xhr.status);
            }
        };
        xhr.send();
    }
}
function year_end() {
    var dt = new Date();
    var ye = dt.getFullYear();
    if (dt.getMonth() < 1) {
        ye--;
    }
    return ye;
}
