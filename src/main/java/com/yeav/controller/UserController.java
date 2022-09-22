package com.yeav.controller;

import com.yeav.controller.viewobject.UserVO;
import com.yeav.error.BusinessException;
import com.yeav.error.EmBusinessError;
import com.yeav.response.CommonReturnType;
import com.yeav.service.UserService;
import com.yeav.service.model.UserModel;
import org.apache.logging.log4j.message.Message;
import org.apache.tomcat.util.security.MD5Encoder;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sun.misc.BASE64Encoder;

import javax.servlet.http.HttpServletRequest;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author yeav
 */
@Controller("user")
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*") //解决Ajax跨域问题
public class UserController extends BaseController {

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private HttpServletRequest httpServletRequest;

    //用户注册接口
    @RequestMapping(value = "/register", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType register(@RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "name") String name,
                                     @RequestParam(name = "gender") Integer gender,
                                     @RequestParam(name = "password") String password,
                                     @RequestParam(name = "age") Integer age) throws BusinessException, UnsupportedEncodingException, NoSuchAlgorithmException {
        //验证手机号和对应的optCode相符合
        String inSeesionOtpCode = (String)this.httpServletRequest.getSession().getAttribute(telphone);
        //用该equals,内部已经做了判空处理
        if (!com.alibaba.druid.util.StringUtils.equals(otpCode, inSeesionOtpCode)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不正确");
        }
        //用户的注册流程
        UserModel userModel = new UserModel();
        userModel.setName(name);
        userModel.setAge(age);
        userModel.setTelphone(telphone);
        userModel.setGender(new Byte(String.valueOf(gender.intValue())));
        userModel.setRegisterMode("byphone");
        userModel.setEncrptPassword(this.EncodeByMd5(password));

        userService.register(userModel);
        return CommonReturnType.create(null);
    }

    public String EncodeByMd5(String str) throws UnsupportedEncodingException, NoSuchAlgorithmException {
            //确认计算方法
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        BASE64Encoder base64Encoder = new BASE64Encoder();
        //加密
        String newstr = base64Encoder.encode(md5.digest(str.getBytes("utf-8")));
        return newstr;
    }

    //用户获取otp短信接口
    @RequestMapping(value = "/getotp", method = {RequestMethod.POST}, consumes = {CONTENT_TYPE_FORMED})
    @ResponseBody
    public CommonReturnType getOtp(@RequestParam(name = "telphone") String telphone) {
        //需要按照一定规则生成opt验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);

        //将opt验证码同对应用户的手机号关联,使用httpseesion的方式绑定
        httpServletRequest.getSession().setAttribute(telphone, otpCode);

        //将otp验证码通过短信通道发送给用户，省略
        System.out.println("telphone = " + telphone + " & otpCode = " + otpCode);


        return CommonReturnType.create(null);
    }


    @RequestMapping("/get")
    @ResponseBody
    public CommonReturnType getUser(@RequestParam(name = "id") Integer id) throws BusinessException {
        //调用service服务获取对应id的用户对象并返回给前端
        UserModel userModel = userService.getUserById(id);

        //若获取的用户信息不存在
        if (userModel == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }

        //将核心领域模型用户对象转化为可供ui使用的viewobject
        UserVO userVO = convertFromModel(userModel);
        return CommonReturnType.create(userVO);
    }

    private UserVO convertFromModel(UserModel userModel) {
        if (userModel == null) {
            return null;
        } else {
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(userModel, userVO);
            return userVO;
        }
    }


}
