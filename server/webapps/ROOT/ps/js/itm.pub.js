var service_name = "service_name";
var method = "method";
var param = "param";
var pscall = "/pscall";

function ajson(s, m, p) {

    var jsonStr = {
        service_name : s,
        method : m,
        param : p
    };
    
    return jsonStr;

}
