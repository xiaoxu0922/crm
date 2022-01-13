package com.yjxxt.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yjxxt.crm.base.BaseService;
import com.yjxxt.crm.bean.Permission;
import com.yjxxt.crm.bean.Role;
import com.yjxxt.crm.mapper.ModuleMapper;
import com.yjxxt.crm.mapper.PermissionMapper;
import com.yjxxt.crm.mapper.RoleMapper;
import com.yjxxt.crm.query.RoleQuery;
import com.yjxxt.crm.utils.AssertUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

@Service
public class RoleService extends BaseService<Role,Integer> {
    @Autowired(required = false)
    private RoleMapper roleMapper;

    @Resource
    private PermissionMapper permissionMapper;

    @Resource
    private ModuleMapper moduleMapper;
    /**
     * 查询所有角色信息
     * @return
     */
    public List<Map<String,Object>> findRoles(Integer userId ){
        return roleMapper.selectRoles(userId);
    }

    /**
     * 角色的条件查询以及分页
     * @param roleQuery
     * @return
     */
    public Map<String,Object> findRoleByParams(RoleQuery roleQuery){
        Map<String,Object> map = new HashMap<String,Object>();
        //开始分页
        PageHelper.startPage(roleQuery.getPage(),roleQuery.getLimit());
        PageInfo rlist = new PageInfo(selectByParams(roleQuery));
        map.put("code",0);
        map.put("msg","success");
        map.put("count", rlist.getTotal());
        map.put("data", rlist.getList());
        return map;
    }

    /**
     * 验证：
     *  1.角色名非空
     *  2.角色名唯一
     * 设定默认值
     *  is_valid=1
     *  updateDate
     *  createDate
     * 判断是否添加成功
     */
    public void addRole(Role role){
        //1.角色名非空
        AssertUtil.isTrue(StringUtils.isBlank(role.getRoleName()),"角色名不能为空");
        //2.角色名唯一
        Role temp = roleMapper.selectRoleByName(role.getRoleName());
        AssertUtil.isTrue(temp!=null,"角色已经存在");
        //设定默认值
        role.setIsValid(1);
        role.setCreateDate(new Date());
        role.setUpdateDate(new Date());
        //判断是否添加成功
        AssertUtil.isTrue(insertHasKey(role)<1,"添加失败");
    }
    /**
     *授权
     *      统计当前角色有多少资源
     *      删除原有资源，重新添加
     * @param roleId
     * @param mids
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void addGrant(Integer roleId, Integer[] mids) {
        AssertUtil.isTrue(roleId == null || roleMapper.selectByPrimaryKey(roleId)==null, "请选择角色");
        AssertUtil.isTrue(mids == null || mids.length == 0, "至少选择一个角色");
        //统计当前角色的资源数量
        int count = permissionMapper.countPermissionByRoleId(roleId);
        //删除角色的资源信息
        if(count>0){
            AssertUtil.isTrue(permissionMapper.deletePermissionsByRoleId(roleId)!=count,"角色资源分配失败");
        }
        List<Permission> plist = new ArrayList<Permission>();
        if(mids!=null && mids.length>0){
            //遍历mids
            for (Integer mid : mids) {
                //实例化对象
                Permission permission = new Permission();
                permission.setRoleId(roleId);
                permission.setModuleId(mid);
                //权限码
                permission.setAclValue(moduleMapper.selectByPrimaryKey(mid).getOptValue());
                permission.setCreateDate(new Date());
                permission.setUpdateDate(new Date());
                plist.add(permission);
            }
        }
        //添加 t_permission
        AssertUtil.isTrue(permissionMapper.insertBatch(plist) != plist.size(), "授权失败");
    }/**
     * 验证：
     *      id验证
     *  1.角色名非空
     *  2.角色名唯一
     * 设定默认值
     *  updateDate
     * 判断是否添加成功
     */
    public void changeRole(Role role){
        // id验证
        Role temp = roleMapper.selectByPrimaryKey(role.getId());
        AssertUtil.isTrue(temp==null,"待修改记录不存在");
        //角色名非空
        AssertUtil.isTrue(StringUtils.isBlank(role.getRoleName()),"请输入角色名");
        //角色名唯一
        Role temp2 = roleMapper.selectRoleByName(role.getRoleName());
        AssertUtil.isTrue(temp2!=null && temp2.getId().equals(role.getId()),"角色已存在");
        //设定默认值
        temp2.setUpdateDate(new Date());
        //判断是否修改成功
        AssertUtil.isTrue(updateByPrimaryKeySelective(role)<1,"角色修改失败");
    }

    /**
     * 删除
     * @param role
     */
    public void removeRoleById(Role role) {
        //验证
        AssertUtil.isTrue(role.getId()==null || selectByPrimaryKey(role.getId())==null,"待删除角色不存在");
        //设定默认值
        role.setIsValid(0);
        //判断是否删除成功
        AssertUtil.isTrue(updateByPrimaryKeySelective(role)<1,"删除失败");
    }

}
