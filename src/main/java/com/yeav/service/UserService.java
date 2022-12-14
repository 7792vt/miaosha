package com.yeav.service;

import com.yeav.error.BusinessException;
import com.yeav.service.model.UserModel;

/**
 * @author yeav
 */
public interface UserService {
    //通过用户ID获取用户对象的方法
    UserModel getUserById(Integer id);
    void register(UserModel userModel) throws BusinessException;
}
