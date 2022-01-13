layui.use(['table','layer'],function(){
    var layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery,
        table = layui.table;
    /**
     * 用户列表展示
     */
    var  tableIns = table.render({
        elem: '#userList',
        url : ctx+'/user/list',
        cellMinWidth : 95,
        page : true,
        height : "full-125",
        limits : [10,15,20,25],
        limit : 10,
        toolbar: "#toolbarDemo",
        id : "userListTable",
        cols : [[
            {type: "checkbox", fixed:"left", width:50},
            {field: "id", title:'编号',fixed:"true", width:80},
            {field: 'userName', title: '用户名', minWidth:50, align:"center"},
            {field: 'email', title: '用户邮箱', minWidth:100, align:'center'},
            {field: 'phone', title: '用户电话', minWidth:100, align:'center'},
            {field: 'trueName', title: '真实姓名', align:'center'},
            {field: 'createDate', title: '创建时间', align:'center',minWidth:150},
            {field: 'updateDate', title: '更新时间', align:'center',minWidth:150},
            {title: '操作', minWidth:150, templet:'#userListBar',fixed:"right",align:"center"}
        ]]
    });

    /**
     * 表单的搜索，页面重载
     */
    $(".search_btn").click(function () {
        table.reload('userListTable', {
            where: { //设定异步数据接口的额外参数，任意设
                userName: $("input[name=userName]").val(),// 客户名
                phone: $("input[name=phone]").val(),// 创建人
                email: $("input[name=email]").val()// 状态
            }
            , page: {
                curr: 1 // 重新从第 1 页开始
            }
        }); // 只重载数据
    });


    /*头部工具栏的绑定*/
    //头工具栏事件
    table.on('toolbar(users)', function(obj){
        var checkStatus = table.checkStatus(obj.config.id);
        switch(obj.event){
            case 'add':
                openAddOrUpdateUserDialog();
                break;
            case 'del':
                deleteUser(checkStatus.data);
                break;
        };
    });

    /**
     * 用户模块批量删除操作
     * @param datas
     */
    function deleteUser(datas) {
        if(datas.length==0){
            layer.msg("请选择要删除的数据");
            return;
        }
        layer.confirm("你确定要删除这些数据吗？",{
            btn:['确认','取消']
        },function(index){
            layer.close(index);
            //收集数据
            var ids ="ids=";
            for (var i = 0; i < datas.length; i++) {
                if(i<datas.length-1){
                    ids = ids+datas[i].id+"&ids=";
                }else{
                    ids=ids+datas[i].id;
                }
            }
            //发送ajax删除数据
            $.post(ctx+"/user/delete",ids,function (result) {
                if(result.code==200){
                    //重新加载数据
                    tableIns.reload();
                }else{
                    layer.msg(result.msg,{icon:5});
                }
            },"json")
        });
    }
    //行内工具栏的绑定
//监听行工具事件
    table.on('tool(users)', function(obj){
        var data = obj.data;
        if(obj.event === 'del'){
            layer.confirm("你确定要删除这些数据吗？",{
                btn:['确认','取消']
            },function(index){
                layer.close(index);
                //发送ajax删除数据
                $.post(ctx+"/user/delete",{ids:data.id},function (result) {
                    if(result.code==200){
                        //重新加载数据
                        tableIns.reload();
                    }else{
                        layer.msg(result.msg,{icon:5});
                    }
                },"json")
            });
        } else if(obj.event === 'edit'){
            openAddOrUpdateUserDialog(data.id);
        }
    });

    /**
     * 用户模块添加--修改操作
     * @param userId
     */
    function openAddOrUpdateUserDialog(userId) {
        var url = ctx+"/user/addOrUpdatePage";
        var title = "<h2>用户模块--添加</h2>";
        //判断是添加还是修改
        if(userId){
            title="<h2>用户模块--修改</h2>";
            url = url+"?id="+userId;
        }
        layer.open({
            title:title,
            content:url,
            type:2,     //iframe
            area:["650px","400px"],
            maxmin:true
        })
    }
});