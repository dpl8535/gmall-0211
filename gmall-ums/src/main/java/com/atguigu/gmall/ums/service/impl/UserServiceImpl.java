package com.atguigu.gmall.ums.service.impl;

import com.atguigu.gmall.common.exception.UserException;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.atguigu.gmall.common.bean.PageResultVo;
import com.atguigu.gmall.common.bean.PageParamVo;

import com.atguigu.gmall.ums.mapper.UserMapper;
import com.atguigu.gmall.ums.entity.UserEntity;
import com.atguigu.gmall.ums.service.UserService;


@Service("userService")
public class UserServiceImpl extends ServiceImpl<UserMapper, UserEntity> implements UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Override
    public PageResultVo queryPage(PageParamVo paramVo) {
        IPage<UserEntity> page = this.page(
                paramVo.getPage(),
                new QueryWrapper<UserEntity>()
        );

        return new PageResultVo(page);
    }

    /**
     * @param data username phone email
     * @param type 1      2     3
     * @return
     */
    @Override
    public Boolean check(String data, Integer type) {
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<>();
        switch (type) {
            case 1:
                wrapper.eq("username", data);
                break;
            case 2:
                wrapper.eq("phone", data);
                break;
            case 3:
                wrapper.eq("email", data);
                break;
            default:
                return null;
        }
        return this.userMapper.selectCount(wrapper) == 0;
    }

    @Override
    public void register(UserEntity user, String code) {
        // 1.生成验证码，发送给用户
        String checkCode = this.redisTemplate.opsForValue().get(user.getPhone());
        if (!StringUtils.equals(code, checkCode)) {
            return;
        }
        // 2.生成盐
        String salt = UUID.randomUUID().toString().substring(0, 6);
        user.setSalt(salt);

        // 3.给密码加盐,并进行加密
        user.setPassword(DigestUtils.md5Hex(user.getPassword() + salt));

        // 4.给用户其它信息赋默认值,用户注册
        user.setCreateTime(new Date());
        user.setLevelId(1l);
        user.setSourceType(1);
        user.setGrowth(1000);
        user.setStatus(1);
        this.save(user);

        // 5.删除验证码
        redisTemplate.delete(user.getPhone());

    }

    @Override
    public UserEntity queryUser(String loginName, String password) {
        // 1.判断用户名、手机号、email是否和loginName相同
        UserEntity userEntity = this.getOne(new QueryWrapper<UserEntity>()
                .eq("username", loginName)
                .or().eq("phone", loginName)
                .or().eq("email", loginName));
        if (userEntity == null){
            throw new UserException("用户名或密码不正确loginName" + loginName);
        }
        // 2.根据获取到的用户获取到盐
        String salt = userEntity.getSalt();

        // 3.根据盐和password和加密时的算法查看是否和数据库中的密码相同
        String passwd = DigestUtils.md5Hex(password + salt);
        if (!StringUtils.equals(userEntity.getPassword(), passwd)) {
            throw new UserException("用户名或密码不正确passwd" + password);
        }

        // 4.返回用户
        return userEntity;
    }

}