$('document').ready(function() {
    function search(val) {
        let friends = $(".friends  .list-group-item").toArray();
        if (val != '') {
            friends.forEach(function (friend) {
                if (friend.innerText.toLowerCase().search(val.toLowerCase()) == -1) {
                    $(friend).hide();
                } else {
                    $(friend).show();
                }
            })
        } else {
            friends.forEach(function (friend) {
                $(friend).show();
            })
        }
    }
    $('#search_friends').on('input' , function () {
        let val = this.value.trim();
        search(val);
    });
    $("#search_friends").bind("keypress", function (e) {
        if (e.keyCode == 13) {
            $("#btnSearch").attr('value');
            return false;
        }
    });
});

