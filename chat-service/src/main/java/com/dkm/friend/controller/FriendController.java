package com.dkm.friend.controller;

import com.dkm.constanct.CodeType;
import com.dkm.exception.ApplicationException;
import com.dkm.friend.entity.bo.FriendAllListBo;
import com.dkm.friend.entity.bo.FriendBo;
import com.dkm.friend.entity.vo.FriendAllListVo;
import com.dkm.friend.entity.vo.FriendRequestInfoVo;
import com.dkm.friend.service.IFriendService;
import com.dkm.jwt.contain.LocalUser;
import com.dkm.jwt.entity.UserLoginQuery;
import com.dkm.jwt.islogin.CheckToken;
import com.dkm.user.entity.vo.ResultVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author qf
 * @date 2020/5/12
 * @vesion 1.0
 **/
@Slf4j
@Api(tags = "好友Api")
@RestController
@RequestMapping("/v1/friend")
public class FriendController {


   @Autowired
   private IFriendService friendService;

   @Autowired
   private LocalUser localUser;


   @ApiOperation(value = "展示全部好友", notes = "展示全部好友")
   @GetMapping("/listAllFriend")
   @CrossOrigin
   @CheckToken
   public List<FriendAllListVo> listAllFriend () {
      UserLoginQuery user = localUser.getUser();
      return friendService.listAllFriend(user.getId());
   }


   @ApiOperation(value = "删除好友", notes = "删除好友")
   @ApiImplicitParam(name = "toId", value = "要删除的人的id", required = true, dataType = "Long", paramType = "path")
   @GetMapping("/deleteFriend")
   @CrossOrigin
   @CheckToken
   public ResultVo deleteFriend (@RequestParam("toId") Long toId) {

      if (toId == null) {
         throw new ApplicationException(CodeType.PARAMETER_ERROR, "参数不能为空");
      }
      UserLoginQuery user = localUser.getUser();
      friendService.deleteFriend(user.getId(),toId);

      ResultVo resultVo = new ResultVo();
      resultVo.setResult("ok");

      return resultVo;

   }


   @ApiOperation(value = "根据用户Id搜索用户信息以及好友情况", notes = "根据用户Id搜索用户信息以及好友情况")
   @ApiImplicitParam(name = "id", value = "用户id", required = true, dataType = "Long", paramType = "path")
   @GetMapping("/querySolrInfo")
   @CrossOrigin
   @CheckToken
   public FriendBo querySolrInfo (@RequestParam("id") Long id) {

      if (id == null) {
         throw new ApplicationException(CodeType.PARAMETER_ERROR, "参数不能为空");
      }

      return friendService.querySolrInfo(id);

   }


   @ApiOperation(value = "修改对好友的备注", notes = "修改对好友的备注")
   @ApiImplicitParam(name = "id", value = "用户id", required = true, dataType = "Long", paramType = "path")
   @GetMapping("/updateFriendRemark")
   @CrossOrigin
   @CheckToken
   public ResultVo updateFriendRemark (@RequestParam("id") Long id, @RequestParam("remark") String remark) {

      if (id == null) {
         throw new ApplicationException(CodeType.PARAMETER_ERROR, "参数不能为空");
      }

      UserLoginQuery user = localUser.getUser();
      friendService.updateFriendRemark(remark, user.getId(), id);

      ResultVo vo = new ResultVo();
      vo.setResult("ok");

      return vo;

   }
}
