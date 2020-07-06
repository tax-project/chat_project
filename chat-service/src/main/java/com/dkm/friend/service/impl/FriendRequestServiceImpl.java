package com.dkm.friend.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dkm.config.RedisConfig;
import com.dkm.constanct.CodeType;
import com.dkm.entity.websocket.MsgInfo;
import com.dkm.exception.ApplicationException;
import com.dkm.friend.dao.FriendRequestMapper;
import com.dkm.friend.entity.Friend;
import com.dkm.friend.entity.FriendRequest;
import com.dkm.friend.entity.vo.FriendRequestInfoVo;
import com.dkm.friend.entity.vo.FriendRequestVo;
import com.dkm.friend.entity.vo.FriendVo;
import com.dkm.friend.entity.vo.IdVo;
import com.dkm.friend.service.IFriendRequestService;
import com.dkm.friend.service.IFriendService;
import com.dkm.jwt.contain.LocalUser;
import com.dkm.jwt.entity.UserLoginQuery;
import com.dkm.user.entity.User;
import com.dkm.utils.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author qf
 * @date 2020/5/11
 * @vesion 1.0
 **/
@Service
@Slf4j
@Transactional(rollbackFor = Exception.class)
public class FriendRequestServiceImpl extends ServiceImpl<FriendRequestMapper, FriendRequest> implements IFriendRequestService {

   @Autowired
   private IdGenerator idGenerator;

   @Autowired
   private RabbitTemplate rabbitTemplate;

   @Autowired
   private LocalUser localUser;

   @Autowired
   private IFriendService friendService;

   @Autowired
   private RedisConfig redisConfig;

   private String redisLock = "REDIS::REQUERST";

   private String lock = "REDIS::OPERATION";

   @Override
   public void friendRequest(FriendRequestVo vo) {

      UserLoginQuery user = localUser.getUser();

      if (vo.getToId().equals(user.getId())) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "你不能添加自己为好友");
      }

      try {
         Boolean aBoolean = redisConfig.redisLock(redisLock);

         if (!aBoolean) {
            throw new ApplicationException(CodeType.SERVICE_ERROR, "网络忙,请稍后再试");
         }

         LambdaQueryWrapper<FriendRequest> wrapper = new LambdaQueryWrapper<FriendRequest>()
               .eq(FriendRequest::getFromId, user.getId())
               .eq(FriendRequest::getToId,vo.getToId());

         FriendRequest request = baseMapper.selectOne(wrapper);

         if (request == null) {
            FriendRequest friendRequest = new FriendRequest();
            friendRequest.setId(idGenerator.getNumberId());

            friendRequest.setFromId(user.getId());
            friendRequest.setToId(vo.getToId());
            friendRequest.setRequestRemark(vo.getRequestRemark());

            friendRequest.setRequestTime(LocalDateTime.now());
            //申请中
            friendRequest.setStatus(0);

            int insert = baseMapper.insert(friendRequest);

            if (insert <= 0) {
               throw new ApplicationException(CodeType.SERVICE_ERROR, "申请失败");
            }
         } else {

            if (!request.getRequestRemark().equals(vo.getRequestRemark())) {

               FriendRequest friendRequest = new FriendRequest();
               friendRequest.setId(request.getId());

               friendRequest.setRequestRemark(vo.getRequestRemark());

               friendRequest.setRequestTime(LocalDateTime.now());

               int updateById = baseMapper.updateById(friendRequest);

               if (updateById <= 0) {
                  throw new ApplicationException(CodeType.SERVICE_ERROR, "申请失败");
               }
            }
         }

      } finally {
         redisConfig.deleteLock(redisLock);
      }


      //通知客户端收到好友申请的通知
      MsgInfo msgInfo = new MsgInfo();
      msgInfo.setType(101);
      msgInfo.setFromId(user.getId());
      msgInfo.setToId(vo.getToId());
      msgInfo.setMsg("收到一条好友申请");

      //将好友申请同步发送给好友
      rabbitTemplate.convertAndSend("chat_msg_fanoutExchange","", JSON.toJSONString(msgInfo));

   }


   /**
    * 查询所有加我的记录
    * @return
    */
   @Override
   public List<FriendRequestInfoVo> listAllRequestFriend() {

      UserLoginQuery user = localUser.getUser();

      LambdaQueryWrapper<FriendRequest> wrapper = new LambdaQueryWrapper<FriendRequest>()
            .eq(FriendRequest::getToId,user.getId());

      List<FriendRequest> list = baseMapper.selectList(wrapper);

      if (null == list || list.size() == 0) {
         return null;
      }

      List<IdVo> voList = new ArrayList<>();
      for (FriendRequest request : list) {
         IdVo vo = new IdVo();
         vo.setFromId(request.getFromId());
         voList.add(vo);
      }


      return baseMapper.listAllRequestFriend(voList, user.getId());
   }

   /**
    * 同意或者拒绝好友
    * @param id 申请表id
    * @param fromId 谁加的我的id
    * @param type 0-同意  1-拒绝
    */
   @Override
   public void operationFriendRequest(Long id, Long fromId, Integer type) {

      UserLoginQuery user = localUser.getUser();

      if (type != 0 && type != 1) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "type错误");
      }

      try {

         Boolean aBoolean = redisConfig.redisLock(lock);

         if (!aBoolean) {
            throw new ApplicationException(CodeType.SERVICE_ERROR, "网络繁忙");
         }

         Friend friend = friendService.queryOne(fromId, user.getId());

         if (friend != null) {
            throw new ApplicationException(CodeType.SERVICE_ERROR, "你们已经是好友了");
         }

         FriendRequest request = new FriendRequest();
         if (type == 0) {
            //同意
            request.setId(id);
            request.setStatus(1);

            int i = baseMapper.updateById(request);

            if (i <= 0) {
               throw new ApplicationException(CodeType.SERVICE_ERROR, "添加失败");
            }

            //将信息加进好友表
            FriendVo vo = new FriendVo();
            vo.setFromId(fromId);
            vo.setToId(user.getId());

            friendService.insertFriend(vo);
            return;
         }


         //不同意
         request.setId(id);
         request.setStatus(2);

         int i = baseMapper.updateById(request);

         if (i <= 0) {
            throw new ApplicationException(CodeType.SERVICE_ERROR, "添加失败");
         }

      } finally {
         redisConfig.deleteLock(lock);
      }


   }


   /**
    * 删除申请表的信息
    * @param fromId 删人的
    * @param toId 被删的
    */
   @Override
   public void deleteRequestInfo(Long fromId, Long toId) {

      LambdaQueryWrapper<FriendRequest> wrapper = new LambdaQueryWrapper<FriendRequest>()
            .eq(FriendRequest::getFromId,fromId)
            .eq(FriendRequest::getToId,toId);

      baseMapper.delete(wrapper);

      LambdaQueryWrapper<FriendRequest> lambdaQueryWrapper = new LambdaQueryWrapper<FriendRequest>()
            .eq(FriendRequest::getFromId,toId)
            .eq(FriendRequest::getToId,fromId);

      //将关于两个人的好友申请全删一遍
      baseMapper.delete(lambdaQueryWrapper);
   }
}
