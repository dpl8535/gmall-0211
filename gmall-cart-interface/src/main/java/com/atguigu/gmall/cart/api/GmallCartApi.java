package com.atguigu.gmall.cart.api;

import com.atguigu.gmall.cart.pojo.Cart;
import com.atguigu.gmall.common.bean.ResponseVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author dplStart
 * @create 上午 12:52
 * @Description
 */
public interface GmallCartApi {
    @GetMapping("user/{userId}")
    @ResponseBody
    public ResponseVo<List<Cart>> queryCheckedCartByUserId(@PathVariable("userId") Long userId);
}
