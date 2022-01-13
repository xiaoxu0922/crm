package com.yjxxt.crm.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.yjxxt.crm.base.BaseService;
import com.yjxxt.crm.bean.SaleChance;
import com.yjxxt.crm.query.SaleChanceQuery;
import com.yjxxt.crm.utils.AssertUtil;
import com.yjxxt.crm.utils.PhoneUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class SaleChanceService extends BaseService<SaleChance,Integer> {
    /*条件查询列表
    * code
    * msg
    * count
    * data:
    * */
    public Map<String,Object> querySaleChanceByParams(SaleChanceQuery saleChanceQuery){
        //实例化MAP
        Map<String,Object> map = new HashMap();
        //实例化分页单位
        PageHelper.startPage(saleChanceQuery.getPage(),saleChanceQuery.getLimit());
        //开始分页
        PageInfo<SaleChance> plist = new PageInfo<>(selectByParams(saleChanceQuery));
        //准备数据
        map.put("code",0);
        map.put("msg","succes");
        map.put("count",plist.getTotal());
        map.put("data",plist.getList());
        //返回map
        return map;
    }

    /*
    * 添加操作
    * */
    @Transactional(propagation = Propagation.REQUIRED)
    public void addSaleChance(SaleChance saleChance){
        //参数校验
        checkSaleChanceParam(saleChance.getCustomerName(),saleChance.getLinkMan(),saleChance.getLinkPhone());
        //设置相关参数默认值 state (0--未分配  1--已分配)
        // devResult(0--未开发 1--开发中 2--开发成功 3--开发失败)
        //未选择分配人
        if(StringUtils.isBlank(saleChance.getAssignMan())){
            saleChance.setState(0);
            saleChance.setDevResult(0);
        }
        //选择分配人
        if(StringUtils.isNotBlank(saleChance.getAssignMan())){
            saleChance.setState(1);
            saleChance.setDevResult(1);
            saleChance.setAssignTime(new Date());
        }
        //分配时间：createData，updateData
        saleChance.setCreateDate(new Date());
        saleChance.setUpdateDate(new Date());
        //是否有效
        saleChance.setIsValid(1);
        //判断是否添加成功
        AssertUtil.isTrue(insertSelective(saleChance)<1,"添加失败");
    }
    /**
     * 验证
     * 客户名称非空
     * 联系人非空
     * 联系电话非空，11位手机号
     * @param customerName 顾客姓名
     * @param linkMan 联系人
     * @param linkPhone 联系号码
     */
    private void checkSaleChanceParam(String customerName, String linkMan, String linkPhone) {
        AssertUtil.isTrue(StringUtils.isBlank(customerName),"请输入客户名称");
        AssertUtil.isTrue(StringUtils.isBlank(linkMan),"联系人不能为空");
        AssertUtil.isTrue(StringUtils.isBlank(linkPhone),"联系号码不能为空");
        AssertUtil.isTrue(!PhoneUtil.isMobile(linkPhone),"请输入合法的手机号");
    }

    /** 修改操作
     * 1.验证
     *  验证当前对象ID
     *  用户名非空
     *  联系人非空
     *  电话非空，11位合法
     * 2.设定默认值
     * 3.判断是否修改成功
     * @param saleChance
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public void changeSalChance(SaleChance saleChance){
        //验证
        SaleChance temp = selectByPrimaryKey(saleChance.getId());
        System.out.println(temp);
        AssertUtil.isTrue(temp==null,"待修改记录不存在");
        checkSaleChanceParam(saleChance.getCustomerName(), saleChance.getLinkMan(),saleChance.getLinkPhone());
        //原来  未分配state，DevResult
        if(StringUtils.isBlank(temp.getAssignMan()) && StringUtils.isNotBlank(saleChance.getAssignMan())){
            saleChance.setState(1);
            saleChance.setDevResult(1);
            saleChance.setAssignTime(new Date());
        }
        //原来 已分配
        if(StringUtils.isNotBlank(temp.getAssignMan()) && StringUtils.isBlank(saleChance.getAssignMan())){
            saleChance.setState(0);
            saleChance.setDevResult(0);
            saleChance.setAssignTime(null);
            saleChance.setAssignMan("");
        }
        //设定默认值
        saleChance.setUpdateDate(new Date());
        //是否修改成功
        AssertUtil.isTrue(updateByPrimaryKeySelective(saleChance)<1,"修改失败");
    }

    /**
     * 批量删除操作
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public  void removeSaleChanceIds(Integer[] ids){
        //验证
        AssertUtil.isTrue(ids==null && ids.length==0,"请选择要删除的数据");
        //判断是否成功
        AssertUtil.isTrue(deleteBatch(ids)<0 ,"批量删除失败啦");

    }
}
