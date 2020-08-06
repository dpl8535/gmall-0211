package com.atguigu.gmall.ums.api;

import com.atguigu.gmall.common.bean.ResponseVo;
import com.atguigu.gmall.ums.entity.UserEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author dplStart
 * @create 下午 10:18
 * @Description
 */
public interface GmallUmsApi {

    @GetMapping("ums/user/query")
    public ResponseVo<UserEntity> queryUser(@RequestParam String loginName,
                                            @RequestParam String password);
}
