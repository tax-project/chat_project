package com.dkm.manyChat.controller;

import com.dkm.constanct.CodeType;
import com.dkm.exception.ApplicationException;
import com.dkm.jwt.islogin.CheckToken;
import com.dkm.manyChat.entity.vo.ManyChatResultVo;
import com.dkm.manyChat.service.IManyChatInfoService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author qf
 * @date 2020/7/17
 * @vesion 1.0
 **/
@Api(tags = "群聊人员api")
@RestController
@Slf4j
@RequestMapping("/v1/manyChatInfo")
public class ManyChatInfoController {

   @Autowired
   private IManyChatInfoService manyChatInfoService;

   @ApiOperation(value = "设定管理员", notes = "设定管理员")
   @ApiImplicitParams({
         @ApiImplicitParam(name = "userId", value = "用户id", required = true, dataType = "Long", paramType = "path"),
         @ApiImplicitParam(name = "manyChatId", value = "群聊id", required = true, dataType = "Long", paramType = "path"),
         })
   @CrossOrigin
   @CheckToken
   @GetMapping("/updateAdmin")
   public void updateAdmin (@RequestParam("userId") Long userId, @RequestParam("manyChatId") Long manyChatId) {
      if (userId == null || manyChatId == null) {
         throw new ApplicationException(CodeType.PARAMETER_ERROR, "参数不能为空");
      }
      manyChatInfoService.updateAdmin(userId, manyChatId);
   }
}
