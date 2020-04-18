$(function () {
    $("#sendBtn").click(send_letter);
    $(".close").click(delete_msg);
});

function send_letter() {
    $("#sendModal").modal("hide");

    // 取值
    var toName = $("#recipient-name").val();
    var content = $("#message-text").val();

    $.ajax({
        url: CONTEXT_PATH + "/letter/send",
        type: 'post',
        data: {"toName": toName, "content": content},
        dataType: 'json',
    }).done(function (data) {
        // 在提示框中显示返回的消息
        $("#hintBody").text(data.message);

        // 显示提示框
        $("#hintModal").modal("show");
        // 2秒后，自动隐藏提示框
        setTimeout(function () {
            $("#hintModal").modal("hide");
            // 刷新页面
            if (data.status === 200) {
                window.location.reload();
            }
        }, 2000);
    }).fail(function (data) {
        // 在提示框中显示返回的消息
        let errors;
        if (data.responseJSON.data != null) {
            errors = Object.values(data.responseJSON.data);
        } else{
            errors = data.responseJSON.message;
        }
        $("#hintBody").text(errors);

        // 显示提示框
        $("#hintModal").modal("show");
        // 2秒后，自动隐藏提示框
        setTimeout(function () {
            $("#hintModal").modal("hide");
        }, 2000);
    });

}

function delete_msg() {
    // TODO 删除数据
    $(this).parents(".media").remove();
}