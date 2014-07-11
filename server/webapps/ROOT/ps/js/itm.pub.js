var service_name = "service_name";
var method_name = "method_name";
var param = "param";
var pscall = "/pscall";

function ajson(service, method, para) {

    var jsonStr = {
        service_name : service,
        method_name : method,
        param : para
    };
    
    return jsonStr;

}

function loadtable(table, service, method, para, callback){
//    $(table).dataTable().fnDestroy();
    // $(table).find("table").DataTables();
    $(table).dataTable(
        {
            "processing": true,
            "serverSide": true,
//            "bRetrieve": true,
            "ajax": pscall + "?" + service_name + "=" + service + "&" + method_name + "=" + method + "&" + param + "=" + para//,
//            "sAjaxSource": pscall + "?" + service_name + "=" + service + "&" + method_name + "=" + method + "&" + param + "=" + para,
//            "fnServerData": queryData(service, method, para, callback)
        }
    );
}


function queryData(service, method, para, callback){
    return $.ajax({
        "type": "get",
        "contentType": "application/json",
        "url": pscall,
        "dataType": "json",
        "data": {
            aoData:JSON.stringify(ajson(service, method, para)),
            // "type": 5,
            // "test": "liuzy"
        },
        "success": callback
    });
}
