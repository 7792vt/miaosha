package com.yeav.service.impl;

import com.yeav.dao.UserDOMapper;
import com.yeav.dao.UserPasswordDOMapper;
import com.yeav.dataobject.UserDO;
import com.yeav.dataobject.UserPasswordDO;
import com.yeav.error.BusinessException;
import com.yeav.error.EmBusinessError;
import com.yeav.service.UserService;
import com.yeav.service.model.UserModel;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author yeav
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired(required = false)
    private UserDOMapper userDOMapper;

    @Autowired(required = false)
    private UserPasswordDOMapper userPasswordDOMapper;

    @Override
    public UserModel getUserById(Integer id) {
        //调用userdomapper获取对应用户的dataobject
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);

        if (userDO == null) {
            return null;
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());

        return convertFromDataObject(userDO,userPasswordDO);
    }

    @Override
    @Transactional
    public void register(UserModel userModel) throws BusinessException {
        if(userModel == null){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        if(StringUtils.isEmpty(userModel.getName())
                || userModel.getAge() == null
                || userModel.getName() == null
                || StringUtils.isEmpty(userModel.getTelphone())){
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        //实现model->dataobject方法
        UserDO userDO = new UserDO();
        userDOMapper.insertSelective(userDO);

        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDOMapper.insertSelective(userPasswordDO);

        return;
    }

    private UserPasswordDO convertPasswordFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setUserId(userModel.getId());
        userPasswordDO.setEncrptPassword(userModel.getEncrptPassword());
        return userPasswordDO;
    }

    private UserDO convertFromModel(UserModel userModel){
        if(userModel == null){
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userModel,userDO);
        return userDO;
    }
    private UserModel convertFromDataObject(UserDO userDO, UserPasswordDO userPasswordDO) {
        if (userDO == null) {
            return null;
        }

        UserModel userModel = new UserModel();
        BeanUtils.copyProperties(userDO, userModel);
        if (userPasswordDO != null) {
            userModel.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }

        return userModel;
    }
}
