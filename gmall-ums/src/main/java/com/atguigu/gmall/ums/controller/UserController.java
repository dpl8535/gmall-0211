package com.atguigu.gmall.ums.controller;

import java.util.List;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.common.bean.PageParamVo;

/**
 * 用户表
 *
 * @author Mr.Ding
 * @email MrDing@atguigu.com
 * @date 2020-07-21 08:32:08
 */
@Api(tags = "用户表 管理")
@RestController
@RequestMapping("ums/user")
public class UserController {

    @Autowired
    private UserService userService;


    /**
     * 根据用户名和密码查询用户是否存在
     * @param loginName
     * @param password
     * @return
     */
    @GetMapping("query")
    public ResponseVo<UserEntity> queryUser(@RequestParam String loginName,
                                            @RequestParam String password) {
        UserEntity userEntity = this.userService.queryUser(loginName, password);
        return ResponseVo.ok(userEntity);
    }

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @param code 验证码
     * @return
     */
    @PostMapping("register")
    public ResponseVo<UserEntity> register(UserEntity user, @RequestParam("code") String code) {
        this.userService.register(user, code);
        return ResponseVo.ok();
    }

    /**
     * 如果数据库中不存在该信息则可以注册
     *
     * @param data
     * @param type
     * @return
     */
    @GetMapping("check/{data}/{type}")
    public ResponseVo<Boolean> check(@PathVariable("data") String data, @PathVariable("type") Integer type) {
        Boolean b = this.userService.check(data, type);
        return ResponseVo.ok(b);

    }

    /**
     * 列表
     */
    @GetMapping
    @ApiOperation("分页查询")
    public ResponseVo<PageResultVo> queryUserByPage(PageParamVo paramVo) {
        PageResultVo pageResultVo = userService.queryPage(paramVo);

        return ResponseVo.ok(pageResultVo);
    }


    /**
     * 信息
     */
    @GetMapping("{id}")
    @ApiOperation("详情查询")
    public ResponseVo<UserEntity> queryUserById(@PathVariable("id") Long id) {
        UserEntity user = userService.getById(id);

        return ResponseVo.ok(user);
    }

    /**
     * 保存
     */
    @PostMapping
    @ApiOperation("保存")
    public ResponseVo<Object> save(@RequestBody UserEntity user) {
        userService.save(user);

        return ResponseVo.ok();
    }

    /**
     * 修改
     */
    @PostMapping("/update")
    @ApiOperation("修改")
    public ResponseVo update(@RequestBody UserEntity user) {
        userService.updateById(user);

        return ResponseVo.ok();
    }

    /**
     * 删除
     */
    @PostMapping("/delete")
    @ApiOperation("删除")
    public ResponseVo delete(@RequestBody List<Long> ids) {
        userService.removeByIds(ids);

        return ResponseVo.ok();
    }

}
