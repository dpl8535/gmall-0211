package com.atguigu.gmall.auth;

import com.atguigu.gmall.common.utils.JwtUtils;
import com.atguigu.gmall.common.utils.RsaUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;

/**
 * @author dplStart
 * @create 下午 11:28
 * @Description
 */
public class JwtTest {

    // 别忘了创建D:\\project\rsa目录
    private static final String pubKeyPath = "D:\\ideaIU-2019.2\\workspace-gulishop\\rsa\\rsa.pub";
    private static final String priKeyPath = "D:\\ideaIU-2019.2\\workspace-gulishop\\rsa\\rsa.pri";

    private PublicKey publicKey;

    private PrivateKey privateKey;

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(pubKeyPath, priKeyPath, "123/*-+!@#$%^&*()asdfjkl;ASDFJLK?.");
    }

    @BeforeEach
    public void testGetRsa() throws Exception {
        this.publicKey = RsaUtils.getPublicKey(pubKeyPath);
        this.privateKey = RsaUtils.getPrivateKey(priKeyPath);
    }

    @Test
    public void testGenerateToken() throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("id", "11");
        map.put("username", "liuyan");
        // 生成token
        String token = JwtUtils.generateToken(map, privateKey, 5);
        System.out.println("token = " + token);
    }

    @Test
    public void testParseToken() throws Exception {
        String token = "eyJhbGciOiJSUzI1NiJ9.eyJpZCI6IjExIiwidXNlcm5hbWUiOiJsaXV5YW4iLCJleHAiOjE1OTY1NTU2NDF9.VsOM69NNTCqzrhu8HgwEbC53AaE0rKqT8Ah1Dny7tCBsFxW1n0nMtPo8T-veYV3hFm-5XXIggHivdvyGib5A8AjdRqSwZyTiE44aYmr2IjladwIeTOQBwLTk2LjRM4nIaCAsv3cnQpkvtUxP55AuPrhbhdlVApFYBWCu5iqyra1mjJRZg4iASmdhbv6qeKcs3d8QDKC7fzY09J2sWpGtA_jmBSKTsoBQ_zfj4CLRNAYsbtPsRJWpxshSfFvYx4ygn-zUfJspYIXva7r9R36h7LZCgbdSN8lXSIlmpvr6nKdqLcbabnvRR_Ur-mtzpKo3UT74wE05c9QC2JNPziAPXg";
        // 解析token
        Map<String, Object> map = JwtUtils.getInfoFromToken(token, publicKey);
        System.out.println("id: " + map.get("id"));
        System.out.println("userName: " + map.get("username"));
    }
}
