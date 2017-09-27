function uploadXls() {
    var formData = new FormData($( "#upload" )[0]);
    console.log(formData);
    $.ajax({
        url:"/nix/upload.do",
        type : "POST",
        contentType: false,
        /**
         * 必须false才会避开jQuery对 formdata 的默认处理
         * XMLHttpRequest会对 formdata 进行正确的处理
         */
        processData: false,
        data : formData,
        success : function(data) {
            settingMsg(data,true);
        },
        error:function (data) {

        }
    });
}
function settingMsg(data,type) {
    if (data.status == 0) {
        alert(data.msg);
        return;
    }
    var msgDiv = $("#xls_msg").children()[0];
    msgDiv.innerHTML = "";
    var myArray=new Array();
    for (var j = 0;j < data.courese[0].length;j ++){
        data.courese[0][j] == "" ? null : myArray.push(j);
    }
    if (myArray.length == 2){
        var tr = $("<tr style='font-size: 30px;background-color: #eee'></tr>").appendTo(msgDiv);
        $("<th>学号</th>").appendTo(tr);
        $("<th>最终成绩</th>").appendTo(tr);
        for (var i = 0; i < data.courese.length; i++) {
            tr = $("<tr style='font-size: 25px;background-color: " + (i % 2 == 0 ? "#fff" : "#eee") + "' onmouseover=\"changeTrBackground(this)\" onmouseout=\"recovery(this)\"></tr>").appendTo(msgDiv);
            for (var j = 0; j < myArray.length; j++) {
                $("<td>" + data.courese[i][myArray[j]] + "</td>").appendTo(tr);
            }
        }
        $("#before").hide();
        $("#after").hide();
        $("#download").show();
    }else {
        var tr = $("<tr style='background-color: #eee' onmouseover=\"changeTrBackground(this)\" onmouseout=\"recovery(this)\"></tr>").appendTo(msgDiv);
        for (var i = 0; i < myArray.length; i++) {
            if (i == 0) $("<th>选择课程</th>").appendTo(tr);
            $("<th>" + getInput("line", myArray[i]) + "</th>").appendTo(tr);
        }
        tr = $("<tr  onmouseover=\"changeTrBackground(this)\" onmouseout=\"recovery(this)\"></tr>").appendTo(msgDiv);
        for (var i = 0; i < myArray.length; i++) {
            if (i == 0) $("<th></th>").appendTo(tr);
            $("<th>" + data.column[myArray[i]] + "</th>").appendTo(tr);
        }
        for (var i = 0; i < data.courese.length; i++) {
            tr = $("<tr style='color: red;background-color: " + (i % 2 == 1 ? "#fff" : "#eee") + "' onmouseover=\"changeTrBackground(this)\" onmouseout=\"recovery(this)\"></tr>").appendTo(msgDiv);
            $("<td>" + getInput("column", i) + "</td>").appendTo(tr);
            for (var j = 0; j < myArray.length; j++) {
                $("<td>" + data.courese[i][myArray[j]] + "</td>").appendTo(tr);
            }
        }
        $("#before").hide();
        $("#after").show();
    }
    file_id = data.file_id;
    if (type) alert("当前计算单元id值为:" + data.file_id);
    $("#after_id").html("当前计算单元id值为:" + data.file_id + "<br/>有效时间至:" + getNowFormatDate(new Date(data.create_time)));
    settingPrompting("事例同学成绩表");
}
function downloadExcel(){
    $.ajax({
        url:"/nix/download_url.do?id=" + file_id,
        type: "GET",
        success:function (data) {
            location.href = data.redirect;
        }
    });
}
function getMsgById() {
    var id = $('input:text[name=file_id]').val();
    $.ajax({
        url:"/nix/" + id + ".do",
        type: "POST",
        success:function (data) {
            settingMsg(data,false);
        }
    });
}
var file_id ;
var trBackground;
function changeTrBackground(tr) {
    trBackground = tr.style.backgroundColor;
    tr.style.backgroundColor = "#9e9a9f";
}
function recovery(tr) {
    tr.style.backgroundColor = trBackground;
}
var TEST_ELEMENT;
function coursesCHeck(input) {
    var tr = input.parentNode.parentNode;
    if (input.checked){
        tr.style.opacity = 1;
        tr.style.color = "red";
    }else {
        tr.style.opacity = 0.7;
        tr.style.color = "black";
    }
}

function paramLine(input) {
/*    TEST_ELEMENT = input;
    var td = input.parentNode;
    if (input.checked){
        switch (input.name){
            case "credits" : td.style.backgroundColor = "red";break;
            case "results" : td.style.backgroundColor = "green";break;
            case "courses" : td.style.backgroundColor = "yellow";break;
        }
    }else{
        td.style.backgroundColor = "#fff";
    }*/
}

function getNowFormatDate(date) {
    // var date = new Date();
    date =+date + 1000*60*60*24;
    date = new Date(date);
    var seperator1 = "-";
    var seperator2 = ":";
    var month = date.getMonth() + 1;
    var strDate = date.getDate();
    if (month >= 1 && month <= 9) {
        month = "0" + month;
    }
    if (strDate >= 0 && strDate <= 9) {
        strDate = "0" + strDate;
    }
    var currentdate = date.getFullYear() + seperator1 + month + seperator1 + strDate
        + " " + date.getHours() + seperator2 + date.getMinutes()
        + seperator2 + date.getSeconds();
    return currentdate;
}

function getResult() {
    var courses = $('input:radio[name=courses]:checked').val();
    var results = $('input:radio[name=results]:checked').val();
    var credits = $('input:radio[name=credits]:checked').val();
    if (courses == null){
        alert("请选择课程所在列");
        return;
    }
    if (results == null){
        alert("请选择单科成绩所在列");
        return;
    }
    if (credits == null){
        alert("请选择单科学分所在列");
        return;
    }
    if (courses == results || courses == credits || results == credits) {
        alert("不能存在一类属于两种类别");
        return;
    }
    var indexArray = new Array();
    $('input:checkbox[name=column]:checked').each(function(i){
        indexArray.push(parseInt($(this).val()));
    });
    var url = "/nix/result.do?id=" + file_id + "&credits=" + credits + "&results=" + results + "&courses=" + courses + "&formula=" + $('input:text[name=formula]').val();
    var trs = $("#xls_msg").children().children();
    courses++;
    var str = "";
    for (var index = 0 ;index < indexArray.length;index ++){
        // if (index == 0) str += "\"" + (trs[indexArray[index] + 2].children)[courses].innerText + "\"";
        str += "&columns=" + (trs[indexArray[index] + 2].children)[courses].innerText;
    }
    console.log(str);
    str = encodeURI(str).replace(/\+/g,'%2B')
    $.ajax({
        url: url,
        type: "POST",
        data:str,
        success: function (data) {
            if (data.status == 0) {
                alert(data.msg);
                return;
            }
            var msgDiv = $("#xls_msg").children()[0];
            msgDiv.innerHTML = "";
            var tr = $("<tr style='font-size: 30px;background-color: #eee'></tr>").appendTo(msgDiv);
            $("<th>学号</th>").appendTo(tr);
            $("<th>最终成绩</th>").appendTo(tr);
            var i = 0;
            for (var stu in data.content){
                tr = $("<tr style='font-size: 25px;background-color: " + (i%2 == 1 ? "#eee" : "#fff") + "'></tr>").appendTo(msgDiv);
                $("<th>" + stu + "</th>").appendTo(tr);
                $("<th>" + data.content[stu] + "</th>").appendTo(tr);
                i ++;
            }
            settingPrompting("统计成绩");
            setInstruction("成绩统计完成。点击下载可以下载如下统计的excel表 excel表按学号排序");
            $("#before").hide();
            $("#after").hide();
            $("#download").show();
        }
    });
}
function settingPrompting(msg) {
    $("#prompting").html(msg);
}
function setInstruction(msg) {
    $("#instruction").html(msg);
}


function getInput(name,value) {
    if (name == "line")
        return "<input type='radio' name='courses' value='" + value +"'  onclick=\"paramLine(this)\">A" +
                "<input type='radio' name='results' value='" + value +"'  onclick=\"paramLine(this)\">B" +
                "<input type='radio' name='credits' value='" + value +"'  onclick=\"paramLine(this)\">C";
    else
        return "<input type='checkbox' name='" + name + "' value='" + value +"'  onclick=\"coursesCHeck(this)\"  checked = 'true'>";
        // return "<input type='checkbox' name='" + name + "' value='" + value +"'  onclick=\"coursesCHeck(this)\" >";
}
$(document).ready(function () {
});

