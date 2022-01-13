layui.use(['form', 'layer'], function () {
    var form = layui.form,
        layer = parent.layer === undefined ? layui.layer : top.layer,
        $ = layui.jquery;


    /**
     * 监听表单事件
     */
    form.on("submit(addOrUpdateSaleChance)",function(obj){
        /**
         * 加载特效
         */
        var index = layer.msg("数据正在提交中，请稍等",{icon : 16,time:false,shade:0.8});
        console.log(obj.field+"<<");

        //判断是添加还是修改  id==null添加  id!=null修改

        var url=ctx+"/sale_chance/save";
        //判断当前页面的隐藏域有没有ID
        if($("input[name=id]").val()){
            url=ctx+"/sale_chance/update";
        }

        $.ajax({
            type: "post",
            url:url,
            data:obj.field,
            dataType:"json",
            success:function (obj) {
                if(obj.code==200){
                    //提示信息
                    layer.msg("添加OK",{icon:5 });
                    //关闭加载层
                    layer.close(index);
                    //关闭ifream
                    layer.closeAll("ifream");
                    //刷新页面
                    window.parent.location.reload();
                }else {
                    layer.msg(obj.msg,{icon : 5 });
                }
            }
        });
        //取消跳转
        return false;
    });

    //取消按钮
    $("#closeBtn").click(function () {
        //关闭弹出层,获取当前弹出层的索引，根据索引关闭
        var idx = parent.layer.getFrameIndex(window.name);
        parent.layer.close(idx);
    });

    /*添加下拉框*/
    var assignMan = $("input[name='man']").val();

    $.ajax({
        type:"post",
        url:ctx+"/user/sales",
        dataType: "json",
        success:function (data) {
            //遍历
            for (var x in data) {
                if(data[x].id==assignMan){
                    $("#assignMan").append("<option selected value='"+data[x].id+"'>"+data[x].uname+"</option>");
                }else{
                    $("#assignMan").append("<option value='"+data[x].id+"'>"+data[x].uname+"</option>");
                }
            }
            // 重新渲染下拉框内容
            layui.form.render("select");
        }
        
    })
});