$(document).ready(
    loadtable('#histab', "itmcentral","loadhistory","all",callbackNPR)
);

function buildNPR(){
    $.getJSON(pscall,ajson("itmcentral","buildNPR","all"),callbackNPR);
}

function callbackNPR(data){
    // var obj = JSON.parse(data);
    // var str = data.result_value;
    var firstTR = $("#histab tr").eq(0);
    
    // var trHtml = "<tr><td class='center'>" + "NPR201406200001" + "</td><td class='center'>" + "2014��6��20�� 13:40 �� 2014��6��20�� 16:40"+
    // "</td><td class='center'>" + "2014��6��21�� 15:33" + "</td><td class='center'>" + "test" +
    // "</td><td class='center'><span class='label label-success'>" + "Success" + "</span></td><td class='center'>" +
    // "<a class='btn btn-success' href='#'><i class='icon-zoom-in icon-white'></i></a><a class='btn btn-info' href='#'><i class='icon-edit icon-white'></i>����</a><a class='btn btn-danger' href='#'><i class='icon-trash icon-white'></i> ɾ��</a></td></tr>  
    
    var trHtml = "<tr><td>" + "test" + "</td><td>1</td><td>1</td><td>1</td><td>1</td><td>1</td></tr>";
    
    firstTR.after(trHtml);
}