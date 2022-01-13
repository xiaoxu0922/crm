package com.yjxxt.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yjxxt.crm.base.BaseService;
import com.yjxxt.crm.bean.User;
import com.yjxxt.crm.bean.UserRole;
import com.yjxxt.crm.mapper.UserMapper;
import com.yjxxt.crm.mapper.UserRoleMapper;
import com.yjxxt.crm.model.UserModel;
import com.yjxxt.crm.query.UserQuery;
import com.yjxxt.crm.utils.AssertUtil;
import com.yjxxt.crm.utils.Md5Util;
import com.yjxxt.crm.utils.PhoneUtil;
import com.yjxxt.crm.utils.UserIDBase64;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class UserService extends BaseService<User, Integer> {
    @Resource
    private UserMapper userMapper;

    @Resource
    private UserRoleMapper userRoleMapper;

    public UserModel userLogin(String userName, String userPwd) {
        //检查用户登录
        checkUserLoginParam(userName, userPwd);
        //用户是否存在
        User temp = userMapper.selectUserByName(userName);
        AssertUtil.isTrue(temp == null, "用户不存在");
        //密码是否正确
        checkUserPwd(userPwd, temp.getUserPwd());
        //构建返回对象
        return builderUserInfo(temp);
    }

    //构建返回目标对象
    private UserModel builderUserInfo(User user) {
        //实例化目标对象
        UserModel userModel = new UserModel();
        //加密
        userModel.setUserIdStr(UserIDBase64.encoderUserID(user.getId()));
        userModel.setUserName(user.getUserName());
        userModel.setTrueName(user.getTrueName());
        //返回目标对象
        return userModel;
    }

    //校验用户密码
    private void checkUserLoginParam(String userName, String userPwd) {
        //用户名非空
        AssertUtil.isTrue(StringUtils.isBlank(userName), "用户名不能为空");
        //密码非空
        AssertUtil.isTrue(StringUtils.isBlank(userPwd), "密码不能为空");
    }

    //验证密码
    private void checkUserPwd(String userPwd, String userPwd1) {
        //对输入的密码加密
        userPwd = Md5Util.encode(userPwd);
        //加密的密码与数据库中的密码对比
        AssertUtil.isTrue(!userPwd.equals(userPwd1), "用户密码不正确");
    }

    public void changeUserPwd(Integer userId, String oldPassword, String newPassword, String confirmPassword) {
        //通过userId获取用户对象
        User user = userMapper.selectByPrimaryKey(userId);
        //密码校验
        checkPasswordParams(user, oldPassword, newPassword, confirmPassword);
        //修改密码
        user.setUserPwd(Md5Util.encode(newPassword));
        //是否修改成功
        AssertUtil.isTrue(userMapper.updateByPrimaryKeySelective(user) < 1, "修改失败");
    }
    //修改密码的验证

    private void checkPasswordParams(User user, String oldPassword, String newPassword, String confirmPassword) {
        // user对象 非空验证
        System.out.println(user);
        AssertUtil.isTrue(user == null, "用户未登录或不存在！");
        // 原始密码 非空验证
        AssertUtil.isTrue(StringUtils.isBlank(oldPassword), "请输入原密码！");
        //原始密码是否正确
        AssertUtil.isTrue(!(user.getUserPwd().equals(Md5Util.encode(oldPassword))), "原始密码不正确！");
        //新密码非空
        AssertUtil.isTrue(StringUtils.isBlank(newPassword), "新密码不能为空！");
        //新密码不能和原密码一样
        AssertUtil.isTrue(newPassword.equals(oldPassword), "新密码和原始密码不能相同！");
        //确认密码非空
        AssertUtil.isTrue(StringUtils.isBlank(confirmPassword), "请输入确认密码！");
        //确认密码与新密码相同
        AssertUtil.isTrue(!(confirmPassword.equals(newPassword)), "新密码与确认密码不一致！");
    }

    //查询所有销售人员的信息
    public List<Map<String, Object>> querySales() {
        return userMapper.queryAllSales();
    }

    /**
     * 用户模块的列表查询
     *
     * @param userQuery
     * @return
     */
    public Map<String, Object> findUserByParams(UserQuery userQuery) {
        //实例化对象
        Map<String, Object> map = new HashMap<String, Object>();
        //初始化分页单位
        PageHelper.startPage(userQuery.getPage(), userQuery.getLimit());

        PageInfo<User> plist = new PageInfo<>(selectByParams(userQuery));
        //准备数据
        map.put("code", 0);
        map.put("msg", "success");
        map.put("count", plist.getTotal());
        map.put("data", plist.getList());
        //返回目标对象
        return map;
    }

    /**
     * 验证
     * 1.用户名非空且唯一
     * 2.邮箱非空
     * 3.手机号非空，11位合法
     * 设定默认值
     * is_valid=1
     * 密码：123456---加密
     * createDate:系统时间
     * updateDate:系统时间
     * 判断添加是否成功
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void addUser(User user) {
        //验证
        checkParams(user);
        //设定默认值
        user.setUserPwd(Md5Util.encode("123456"));
        user.setCreateDate(new Date());
        user.setUpdateDate(new Date());
        //判断是否添加成功
        //AssertUtil.isTrue(insertSelective(user)<1,"添加失败！");
        AssertUtil.isTrue(insertHasKey(user) < 1, "添加失败！");
        System.out.println(user.getId() + "<<<<" + user.getRoleIds());
        relaionUserRole(user.getId(), user.getRoleIds());
    }

    /**
     * 操作中间表
     *
     * @param userId  用户Id
     * @param roleIds 角色Id
     *                <p>
     *                * 用户角色分配
     *                * 原始角色不存在 添加新的角色记录
     *                * 原始角色存在 添加新的角色记录
     *                * 原始角色存在 清空所有角色
     *                * 原始角色存在 移除部分角色
     *                * 如何进行角色分配???
     *                * 如果用户原始角色存在 首先清空原始所有角色 添加新的角色记录到用户角色表
     */
    private void relaionUserRole(Integer userId, String roleIds) {
        //准备集合存储对象
        List<UserRole> urlist = new ArrayList<UserRole>();
        //userId,roleId
        AssertUtil.isTrue(StringUtils.isBlank(roleIds), "请选择角色信息");
        //统计当前用户有多少个角色
        int count = userRoleMapper.countRoleById(userId);
        //删除当前用户的角色
        if (count > 0) {
            AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId) != count, "删除角色失败");
        }
        String[] RoleStrId = roleIds.split(",");
        //遍历数组
        for (String rid : RoleStrId) {
            //准备对象
            UserRole userRole = new UserRole();
            userRole.setUserId(userId);
            userRole.setRoleId(Integer.parseInt(rid));
            userRole.setCreateDate(new Date());
            userRole.setUpdateDate(new Date());
            //存放到集合
            urlist.add(userRole);
        }
        //批量添加
        AssertUtil.isTrue(userRoleMapper.insertBatch(urlist) != urlist.size(), "批量添加失败");
    }

    public void checkParams(User user) {
        AssertUtil.isTrue(StringUtils.isBlank(user.getUserName()), "用户名不能为空");
        // 验证用户名是否存在
        User temp = userMapper.selectUserByName(user.getUserName());
        if (user.getId() == null) {
            //添加操作
            AssertUtil.isTrue(temp != null, "该用户已存在");
        } else {
            //修改操作
            AssertUtil.isTrue(temp != null && !(temp.getId().equals(user.getId())), "用户名已存在");
        }
        AssertUtil.isTrue(!PhoneUtil.isMobile(user.getPhone()), "请输入正确的手机号码");
        AssertUtil.isTrue(StringUtils.isBlank(user.getEmail()), "邮箱不能为空");
    }

    /**
     * 验证
     * 当前用户ID存在 否则无法修改
     * 1.用户名非空且唯一
     * 2.邮箱非空
     * 3.手机号非空，11位合法
     * 设定默认值
     * is_valid=1
     * updateDate:系统时间
     * 判断修改是否成功
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void changeUser(User user) {
        //根据id查询用户信息
        User temp = userMapper.selectByPrimaryKey(user.getId());
        AssertUtil.isTrue(temp == null, "待修改用户不存在");
        //验证
        checkParams(user);
        //设定默认值
        user.setUpdateDate(new Date());
        //判断是否添加成功
        AssertUtil.isTrue(userMapper.updateByPrimaryKeySelective(user) < 1, "修改失败！");
        //
        relaionUserRole(user.getId(), user.getRoleIds());
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void removeUserIds(Integer[] ids) {
        //验证
        AssertUtil.isTrue(ids == null || ids.length == 0, "请选择要删除的数据");
        for (Integer userId:ids) {
            int count = userRoleMapper.countRoleById(userId);
            if(count>0){
                AssertUtil.isTrue(userRoleMapper.deleteUserRoleByUserId(userId)!=count,"删除角色失败");
            }
        }
        //判断是否删除成功
        AssertUtil.isTrue(deleteBatch(ids) < 1, "删除失败");
    }
}
