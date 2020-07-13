package com.dkm.manyChat.controller;

import com.dkm.constanct.CodeType;
import com.dkm.exception.ApplicationException;
import com.dkm.jwt.contain.LocalUser;
import com.dkm.jwt.entity.UserLoginQuery;
import com.dkm.jwt.islogin.CheckToken;
import com.dkm.manyChat.entity.ManyChat;
import com.dkm.manyChat.entity.bo.ManyChatBo;
import com.dkm.manyChat.entity.vo.ManyChatListVo;
import com.dkm.manyChat.entity.vo.ManyChatResultVo;
import com.dkm.manyChat.entity.vo.ManyChatVo;
import com.dkm.manyChat.service.IManyChatService;
import com.dkm.user.entity.vo.ResultVo;
import com.dkm.utils.DateUtil;
import com.dkm.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author qf
 * @date 2020/5/16
 * @vesion 1.0
 **/
@Api(tags = "群聊API")
@Slf4j
@RestController
@RequestMapping("/v1/manyChat")
public class ManyChatController {

   @Autowired
   private IManyChatService manyChatService;

   @Autowired
   private LocalUser localUser;

   @ApiOperation(value = "建立群聊", notes = "建立群聊")
   @ApiImplicitParams({
         @ApiImplicitParam(name = "manyName", value = "群聊名字", required = true, dataType = "String", paramType = "path"),
         @ApiImplicitParam(name = "manyHeadUrl", value = "群聊头像", required = true, dataType = "String", paramType = "path"),
         @ApiImplicitParam(name = "list", value = "id集合", required = true, dataType = "list", paramType = "path"),
   })
   @CrossOrigin
   @CheckToken
   @PostMapping("/insertManyChat")
   public void insertManyChat (@RequestBody ManyChatVo vo) {

      if (StringUtils.isBlank(vo.getManyName()) || StringUtils.isBlank(vo.getManyHeadUrl())) {
         throw new ApplicationException(CodeType.PARAMETER_ERROR, "群聊名字不能为空");
      }

      if (vo.getList() == null || vo.getList().size() == 0) {
         throw new ApplicationException(CodeType.PARAMETER_ERROR, "至少要两个人才能创建群聊");
      }

      manyChatService.insertManyChat(vo);
   }


   @ApiOperation(value = "根据群聊id查询群聊信息", notes = "根据群聊id查询群聊信息")
   @ApiImplicitParam(name = "id", value = "群聊id", required = true, dataType = "Long", paramType = "path")
   @CrossOrigin
   @CheckToken
   @GetMapping("/queryById")
   public ManyChatResultVo queryById (@RequestParam("id") Long id) {

      if (id == null) {
         throw new ApplicationException(CodeType.PARAMETER_ERROR);
      }

      ManyChatResultVo vo = new ManyChatResultVo();

      ManyChat manyChat = manyChatService.queryById(id);

      BeanUtils.copyProperties(manyChat, vo);
      vo.setCreateDate(DateUtil.formatDateTime(manyChat.getCreateDate()));

      return vo;
   }

   @ApiOperation(value = "查询我的群聊", notes = "查询我的群聊")
   @CrossOrigin
   @CheckToken
   @GetMapping("/queryManyChatList")
   public List<ManyChatListVo> queryManyChatList () {
      UserLoginQuery user = localUser.getUser();
      return manyChatService.queryManyChatList(user.getId());
   }

   @ApiOperation(value = "添加人进群", notes = "添加人进群")
   @CrossOrigin
   @CheckToken
   @PostMapping("/addManyChat")
   public void addManyChat (@RequestBody ManyChatBo bo) {
      manyChatService.addManyChat(bo);
   }

   @ApiOperation(value = "退出群聊", notes = "退出群聊")
   @ApiImplicitParam(name = "manyChatId", value = "群聊id", required = true, dataType = "Long", paramType = "path")
   @CrossOrigin
   @CheckToken
   @GetMapping("/exitManyChat")
   public void exitManyChat (@RequestParam("manyChatId") Long manyChatId) {
      manyChatService.exitManyChat(manyChatId);
   }
}
