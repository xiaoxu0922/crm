package com.yjxxt.crm.controller;

import com.yjxxt.crm.base.BaseController;
import com.yjxxt.crm.base.ResultInfo;
import com.yjxxt.crm.bean.User;
import com.yjxxt.crm.exceptions.ParamsException;
import com.yjxxt.crm.model.UserModel;
import com.yjxxt.crm.query.UserQuery;
import com.yjxxt.crm.service.UserService;
import com.yjxxt.crm.utils.LoginUserUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("user")
public class UserController extends BaseController {

    @Autowired
    private UserService userService;

    @RequestMapping("toPasswordPage")
    public String updatePwd() {
        return "user/password";
    }

    @RequestMapping("toSettingPage")
    public String setting(HttpServletRequest req){
        //获取用户ID
        int userId = LoginUserUtil.releaseUserIdFromCookie(req);
        //调用方法
        User user = userService.selectByPrimaryKey(userId);
        //存储
        req.setAttribute("user",user);
        //转发
        return "user/setting";
    }

    @RequestMapping("index")
    public String index() {
        return "user/user";
    }

    @RequestMapping("addOrUpdatePage")
    public String addOrUpdatePage(Integer id, Model model) {
        if(id!=null){
            model.addAttribute("user",userService.selectByPrimaryKey(id));
        }
        return "user/add_update";
    }
    //登录
    @RequestMapping("login")
    @ResponseBody
    public ResultInfo say(User user) {

        ResultInfo resultInfo = new ResultInfo();

        UserModel userModel = userService.userLogin(user.getUserName(), user.getUserPwd());
        resultInfo.setResult(userModel);

        return resultInfo;
    }
    //登录
    @RequestMapping("setting")
    @ResponseBody
    public ResultInfo sayUpdate(User user) {

        ResultInfo resultInfo = new ResultInfo();
        //修改信息
        userService.updateByPrimaryKeySelective(user);
        //返回目标数据对象
        return resultInfo;
    }

    //修改密码
    @PostMapping("updatePwd")
    @ResponseBody
    public ResultInfo updatePwd(HttpServletRequest request, String oldPassword, String newPassword, String confirmPassword) {

        ResultInfo resultInfo = new ResultInfo();

        // 获取Cookie中的userId
        int userId = LoginUserUtil.releaseUserIdFromCookie(request);
        userService.changeUserPwd(userId, oldPassword, newPassword, confirmPassword);
        //修改密码操作
        return resultInfo;
    }

    //查询所有销售人员的信息
    @RequestMapping("sales")
    @ResponseBody
    public List<Map<String,Object>> findSales(){
        List<Map<String, Object>> list = userService.querySales();
        return list;
    }
    //根据参数查询用户信息
    @RequestMapping("list")
    @ResponseBody
    public Map<String,Object> findUserByParams(UserQuery userQuery){
        return userService.findUserByParams(userQuery);
    }

    @RequestMapping("save")
    @ResponseBody
    public ResultInfo saveUser(User user){
        userService.addUser(user);
        return success("用户添加成功");
    }
    @RequestMapping("update")
    @ResponseBody
    public ResultInfo updateUser(User user){
        userService.changeUser(user);
        return success("用户修改成功");
    }
    @RequestMapping("delete")
    @ResponseBody
    public ResultInfo deleteUser(Integer[] ids){
        userService.removeUserIds(ids);
        return success("批量删除成功");
    }
}
