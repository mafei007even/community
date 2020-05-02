function like(btn, entityType, entityId, entityUserId) {

    $.ajax({
        url: CONTEXT_PATH + "/like",
        type: "post",
        dataType: 'json',
        data: {"entityType": entityType, "entityId": entityId, "entityUserId": entityUserId}
    }).done(function (data) {
        // 改变dom： 赞还是已赞，点赞数
        $(btn).children("i").text(data.data.likeCount);
        $(btn).children("b").text(data.data.likeStatus == 1 ? "已赞" : "赞");
    }).fail(function (data) {
        // 是否登陆
        if (data.responseJSON.status === 403) {
            let goLogin = confirm("您还没有登陆，不能点赞！是否现在登陆？");
            if (goLogin === true){
                location.href = data.responseJSON.data;
            }
            return;
        }
        alert(data.responseJSON.message);
    })

}

function goProfile(userId){
    location.href = CONTEXT_PATH + "/profile/" + userId;
}