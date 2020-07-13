package com.dkm.friend.controller;

import com.dkm.constanct.CodeType;
import com.dkm.exception.ApplicationException;
import com.dkm.friend.entity.vo.FriendRequestInfoVo;
import com.dkm.friend.entity.vo.FriendRequestVo;
import com.dkm.friend.service.IFriendRequestService;
import com.dkm.jwt.islogin.CheckToken;
import com.dkm.user.entity.vo.ResultVo;
import com.dkm.utils.StringUtils;
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
@Api(tags = "好友申请Api")
@RestController
@RequestMapping("/v1/request")
public class FriendRequestController {

   @Autowired
   private IFriendRequestService friendRequestService;

   @ApiOperation(value = "添加好友", notes = "添加好友")
   @ApiImplicitParams({
         @ApiImplicitParam(name = "toId", value = "被加人ID", required = true, dataType = "Long", paramType = "path"),
         @ApiImplicitParam(name = "requestRemark", value = "请求加好友时候的备注", required = true, dataType = "String", paramType = "path")
   })
   @PostMapping("/friendRequest")
   @CrossOrigin
   @CheckToken
   public ResultVo friendRequest (@RequestBody FriendRequestVo vo) {

      if (vo.getToId() == null || StringUtils.isBlank(vo.getRequestRemark())) {
         throw new ApplicationException(CodeType.PARAMETER_ERROR, "参数不能为空");
      }

       friendRequestService.friendRequest(vo);

      ResultVo resultVo = new ResultVo();
      resultVo.setResult("ok");

      return resultVo;

   }



   @ApiOperation(value = "查询所有加我的信息", notes = "查询所有加我的信息")
   @GetMapping("/listAllRequestFriend")
   @CrossOrigin
   @CheckToken
   public List<FriendRequestInfoVo> listAllRequestFriend () {
      return friendRequestService.listAllRequestFriend();
   }


   @ApiOperation(value = "同意或者拒绝好友", notes = "同意或者拒绝好友")
   @ApiImplicitParams({
         @ApiImplicitParam(name = "id", value = "申请表id", required = true, dataType = "Long", paramType = "path"),
         @ApiImplicitParam(name = "fromId", value = "谁加的我的id", required = true, dataType = "Long", paramType = "path"),
         @ApiImplicitParam(name = "type", value = "0-同意  1-拒绝", required = true, dataType = "int", paramType = "path"),
         @ApiImplicitParam(name = "requestRemark", value = "申请好友时候的备注", required = true, dataType = "int", paramType = "path")
   })
   @GetMapping("/operationFriendRequest")
   @CrossOrigin
   @CheckToken
   public ResultVo operationFriendRequest (@RequestParam("id") Long id,
                                           @RequestParam("fromId") Long fromId,
                                           @RequestParam("type") Integer type,
                                           @RequestParam("requestRemark") String requestRemark) {

      if (id == null || fromId == null || type == null || StringUtils.isBlank(requestRemark)) {
         throw new ApplicationException(CodeType.PARAMETER_ERROR, "参数不能为空");
      }

      friendRequestService.operationFriendRequest(id,fromId,type,requestRemark);

      ResultVo resultVo = new ResultVo();
      resultVo.setResult("ok");

      return resultVo;

   }
}
