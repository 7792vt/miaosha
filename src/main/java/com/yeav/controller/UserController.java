package com.yeav.controller;

import com.yeav.controller.viewobject.UserVO;
import com.yeav.error.BusinessException;
import com.yeav.error.EmBusinessError;
import com.yeav.response.CommonReturnType;
import com.yeav.service.UserService;
import com.yeav.service.model.UserModel;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * @author yeav
 */
@Controller("user")
@RequestMapping("/user")
public class UserController extends BaseController {

    @Autowired(required = false)
    private UserService userService;

    @Autowired(required = false)
    private HttpServletRequest httpServletRequest;

    //用户获取otp短信接口
    @RequestMapping("/getotp")
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
