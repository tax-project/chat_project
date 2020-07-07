package com.dkm.user.controller;

import com.dkm.config.RedisConfig;
import com.dkm.constanct.CodeType;
import com.dkm.entity.websocket.MsgInfo;
import com.dkm.exception.ApplicationException;
import com.dkm.jwt.contain.LocalUser;
import com.dkm.jwt.entity.UserLoginQuery;
import com.dkm.jwt.islogin.CheckToken;
import com.dkm.listener.vo.ChatVo;
import com.dkm.user.entity.User;
import com.dkm.user.entity.vo.*;
import com.dkm.user.service.IUserService;
import com.dkm.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author qf
 * @date 2020/5/11
 * @vesion 1.0
 **/
@Slf4j
@Api(tags = "用户API")
@RestController
@RequestMapping("/v1/user")
public class UserController {

   @Autowired
   private IUserService userService;


   @ApiOperation(value = "注册用户", notes = "注册用户")
   @ApiImplicitParams({
         @ApiImplicitParam(name = "userName", value = "账号", required = true, dataType = "String", paramType = "path"),
         @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String", paramType = "path"),
         @ApiImplicitParam(name = "nickName", value = "昵称", required = true, dataType = "String", paramType = "path"),
         @ApiImplicitParam(name = "sex", value = "性别(0-男 1-女)", required = true, dataType = "int", paramType = "path")
   })
   @PostMapping("/registerUser")
   @CrossOrigin
   public ResultVo registerUser (@RequestBody UserVo vo) {

      if (vo.getSex() == null || StringUtils.isBlank(vo.getNickName())
            || StringUtils.isBlank(vo.getUserName()) || StringUtils.isBlank(vo.getPassword())) {
         throw new ApplicationException(CodeType.PARAMETER_ERROR, "参数不能为空");
      }

      userService.registerUser(vo);

      ResultVo resultVo = new ResultVo();
      resultVo.setResult("ok");

      return resultVo;
   }


   @ApiOperation(value = "登录", notes = "登录")
   @ApiImplicitParams({
         @ApiImplicitParam(name = "userName", value = "账号", required = true, dataType = "String", paramType = "path"),
         @ApiImplicitParam(name = "password", value = "密码", required = true, dataType = "String", paramType = "path")
   })
   @PostMapping("/loginUser")
   @CrossOrigin
   public Map<String, Object> loginUser (@RequestBody UserLoginVo vo) {

      if (StringUtils.isBlank(vo.getUserName()) || StringUtils.isBlank(vo.getPassword())) {
         throw new ApplicationException(CodeType.PARAMETER_ERROR, "参数不能为空");
      }

      return userService.loginUser(vo.getUserName(), vo.getPassword());
   }


   @ApiOperation(value = "修改信息", notes = "修改信息")
   @ApiImplicitParams({
         @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "Long", paramType = "path"),
         @ApiImplicitParam(name = "nickName", value = "昵称", required = true, dataType = "String", paramType = "path"),
         @ApiImplicitParam(name = "address", value = "地区", required = true, dataType = "String", paramType = "path"),
         @ApiImplicitParam(name = "sign", value = "签名", required = true, dataType = "String", paramType = "path"),
         @ApiImplicitParam(name = "sex", value = "性别(0-男 1-女)", required = true, dataType = "int", paramType = "path")
   })
   @PostMapping("/updateUserInfo")
   @CrossOrigin
   @CheckToken
   public ResultVo updateUserInfo (@RequestBody UserUpdateInfoVo vo) {

      if (vo.getId() == null || vo.getSex() == null || StringUtils.isBlank(vo.getNickName())) {
         throw new ApplicationException(CodeType.PARAMETER_ERROR, "参数不能为空");
      }

      userService.updateUserInfo(vo);

      ResultVo resultVo = new ResultVo();
      resultVo.setResult("ok");

      return resultVo;
   }


   @ApiOperation(value = "修改密码", notes = "修改密码")
   @ApiImplicitParams({
         @ApiImplicitParam(name = "id", value = "id", required = true, dataType = "Long", paramType = "path"),
         @ApiImplicitParam(name = "oldPassword", value = "旧密码", required = true, dataType = "String", paramType = "path"),
         @ApiImplicitParam(name = "newPassword", value = "新密码", required = true, dataType = "String", paramType = "path")
   })
   @PostMapping("/updatePassword")
   @CrossOrigin
   @CheckToken
   public ResultVo updatePassword (@RequestBody UpdateVo vo) {

      if (vo.getId() == null || StringUtils.isBlank(vo.getOldPassword()) || StringUtils.isBlank(vo.getNewPassword())) {
         throw new ApplicationException(CodeType.PARAMETER_ERROR, "参数不能为空");
      }

      userService.updatePassword(vo);

      ResultVo resultVo = new ResultVo();
      resultVo.setResult("ok");

      return resultVo;
   }

   @ApiOperation(value = "根据id查询用户详细信息", notes = "根据id查询用户详细信息")
   @ApiImplicitParam(name = "id", value = "用户Id", required = true, dataType = "Long", paramType = "path")
   @GetMapping("/queryUserById")
   @CrossOrigin
   @CheckToken
   public User queryUserById (@RequestParam("id") Long id) {
      return userService.queryUserById(id);
   }


   @ApiOperation(value = "修改头像", notes = "修改头像")
   @ApiImplicitParam(name = "heardUrl", value = "头像地址", required = true, dataType = "String", paramType = "path")
   @GetMapping("/updateHeardUrl")
   @CrossOrigin
   @CheckToken
   public void updateHeardUrl (@RequestParam("heardUrl") String heardUrl) {
      if (StringUtils.isBlank(heardUrl)) {
         throw new ApplicationException(CodeType.PARAMETER_ERROR);
      }
      Long aLong = userService.updateHeardUrl(heardUrl);
   }



   @ApiOperation(value = "退出登录", notes = "退出登录")
   @GetMapping("/signOutLogin")
   @CrossOrigin
   @CheckToken
   public void signOutLogin () {
      userService.signOutLogin();
   }


   @ApiOperation(value = "根据账号查询用户详细信息", notes = "根据账号查询用户详细信息")
   @ApiImplicitParam(name = "userName", value = "账号", required = true, dataType = "String", paramType = "path")
   @GetMapping("/queryUserByName")
   @CrossOrigin
   @CheckToken
   public User queryUserByName (@RequestParam("userName") String userName) {
      return userService.queryUserByName(userName);
   }


   @ApiOperation(value = "触发离线消息", notes = "触发离线消息")
   @GetMapping("/getNotOnlineInfo")
   @CrossOrigin
   @CheckToken
   public List<MsgInfo> getNotOnlineInfo () {
      return userService.getNotOnlineInfo();
   }

}
