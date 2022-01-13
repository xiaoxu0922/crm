package com.yjxxt.crm.service;

import com.yjxxt.crm.base.BaseService;
import com.yjxxt.crm.bean.Module;
import com.yjxxt.crm.dto.TreeDto;
import com.yjxxt.crm.mapper.ModuleMapper;
import com.yjxxt.crm.mapper.PermissionMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ModuleService extends BaseService<Module,Integer> {

    @Autowired(required = false)
    private ModuleMapper moduleMapper;

    @Autowired(required = false)
    private PermissionMapper permissionMapper;

    /**
     * 查询所有的资源信息
     * @return
     */
    public List<TreeDto> findModules() {
        return moduleMapper.selectModules();
    }

    public List<TreeDto> findModuleByRoleId(Integer roleId){
        //获取所有的资源信息
        List<TreeDto> tlist = moduleMapper.selectModules();
        //获取当前角色拥有的资源信息
        List<Integer> roleHasModules=permissionMapper.selectModuleByRoleId(roleId);
        //遍历
        for (TreeDto treeDto:tlist) {
            if(roleHasModules.contains(treeDto.getId())){
                //判断比对：checked=TRUE
                treeDto.setChecked(true);
            }
        }
        return tlist;
    }

    public Map<String, Object> queryModules() {
        //准备数据
        Map<String,Object> map = new HashMap();
        //查询所有资源
        List<Module> mlist = moduleMapper.selectAllModules();
        //准备数据项
        map.put("code",0);
        map.put("msg","success");
        map.put("count",mlist.size());
        map.put("data",mlist);
        //返回目标map
        return map;
    }
}
