var service_name = "service_name";
var method = "method";
var param = "param";

function remoteCall(s, m, p) {

    var jsonStr = [{
        service_name : s
    }, {
        method : m
    }, {
        param : p
    }];

    $.getJSON("/pscall", jsonStr, null);

}

function remoteCallback(data) {

    return data;

}
