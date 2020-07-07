package com.dkm.friend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dkm.config.RedisConfig;
import com.dkm.constanct.CodeType;
import com.dkm.entity.websocket.MsgInfo;
import com.dkm.exception.ApplicationException;
import com.dkm.friend.dao.FriendMapper;
import com.dkm.friend.entity.Friend;
import com.dkm.friend.entity.bo.FriendAllListBo;
import com.dkm.friend.entity.bo.FriendBo;
import com.dkm.friend.entity.vo.FriendAllListVo;
import com.dkm.friend.entity.vo.FriendVo;
import com.dkm.friend.entity.vo.IdVo;
import com.dkm.friend.service.IFriendRequestService;
import com.dkm.friend.service.IFriendService;
import com.dkm.jwt.contain.LocalUser;
import com.dkm.jwt.entity.UserLoginQuery;
import com.dkm.user.entity.User;
import com.dkm.user.service.IUserService;
import com.dkm.utils.IdGenerator;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.cache.CacheKey;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author qf
 * @date 2020/5/11
 * @vesion 1.0
 **/
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class FriendServiceImpl extends ServiceImpl<FriendMapper, Friend> implements IFriendService {

   @Autowired
   private IdGenerator idGenerator;

   @Autowired
   private LocalUser localUser;

   @Autowired
   private IFriendRequestService friendRequestService;

   @Autowired
   private IUserService userService;

   @Autowired
   private RedisConfig redisConfig;

   private final String REDIS_LOCK = "REDIS::Lock:Chat::Friend";

   /**
    * 成为好友
    * @param vo
    */
   @Override
   @Caching(evict = {
         @CacheEvict(value = "friendAll", key = "'friendAll' + #vo.getFromId()"),
         @CacheEvict(value = "friendAll", key = "'friendAll' + #vo.getToId()")
   })
   public void insertFriend(FriendVo vo) {

      try {
         Boolean lock = redisConfig.redisLock(REDIS_LOCK);

         if (!lock) {
            throw new ApplicationException(CodeType.SERVICE_ERROR, "网络繁忙,请稍后再试");
         }

         //先查询是不是好友，如果是好友则给前端返回
         LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<Friend>()
               .eq(Friend::getFromId,vo.getFromId())
               .eq(Friend::getToId,vo.getToId())
               .or()
               .eq(Friend::getToId,vo.getFromId())
               .eq(Friend::getFromId,vo.getToId());

         Friend selectOne = baseMapper.selectOne(wrapper);

         if (selectOne != null) {
            throw new ApplicationException(CodeType.SERVICE_ERROR, "他已经是你的好友了.");
         }


         Friend friend = new Friend();

         friend.setId(idGenerator.getNumberId());
         friend.setStatus(0);
         friend.setCreateDate(LocalDateTime.now());
         friend.setFromId(vo.getFromId());
         friend.setToId(vo.getToId());

         int insert = baseMapper.insert(friend);

         if (insert <= 0) {
            throw new ApplicationException(CodeType.SERVICE_ERROR, "成为好友失败");
         }

         //两个人同时成为好友
         friend.setId(idGenerator.getNumberId());
         friend.setStatus(0);
         friend.setFromId(vo.getToId());
         friend.setToId(vo.getFromId());

         int i = baseMapper.insert(friend);

         if (i <= 0) {
            throw new ApplicationException(CodeType.SERVICE_ERROR, "成为好友失败");
         }
      } finally {
         redisConfig.deleteLock(REDIS_LOCK);
      }


   }

   /**
    *  删除好友
    * @param toId 要删除的人的id
    */
   @Override
   @Caching(evict = {
         @CacheEvict(value = "friendAll", key = "'friendAll' + #fromId"),
         @CacheEvict(value = "friendAll", key = "'friendAll' + #toId")
   })
   public void deleteFriend(Long fromId, Long toId) {

      //先删除好友表里面的好友
      LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<Friend>()
            .eq(Friend::getFromId,fromId)
            .eq(Friend::getToId,toId);

      int delete = baseMapper.delete(wrapper);

      if (delete <= 0) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "删除失败");
      }

      LambdaQueryWrapper<Friend> lambdaQueryWrapper = new LambdaQueryWrapper<Friend>()
            .eq(Friend::getFromId,toId)
            .eq(Friend::getToId,fromId);

      int delete1 = baseMapper.delete(lambdaQueryWrapper);

      if (delete1 <= 0) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "删除失败");
      }

      //删除申请表中的信息
      friendRequestService.deleteRequestInfo(fromId,toId);

   }


   /**
    * 展示全部好友
    * @return 所有好友信息
    */
   @Override
   @Cacheable(value = "friendAll", key = "'friendAll' + #userId")
   public List<FriendAllListVo> listAllFriend(Long userId) {

      LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<Friend>()
            .eq(Friend::getFromId,userId);

      List<Friend> friendList = baseMapper.selectList(wrapper);

      List<IdVo> list = new ArrayList<>();
      for (Friend friend : friendList) {
         IdVo vo = new IdVo();
         vo.setFromId(friend.getToId());
         list.add(vo);
      }

      //查询所有好友的信息
      List<FriendAllListVo> friendAllListVos = userService.queryAllFriend(list);

      Map<Long, Friend> friendMap = friendList.stream().
            collect(Collectors.toMap(Friend::getToId, friend
                  -> friend));

      return friendAllListVos.stream().map(friendAllListVo -> {
         friendAllListVo.setRemark(friendMap.get(friendAllListVo.getToId()).getRemark());
         return friendAllListVo;
      }).collect(Collectors.toList());
   }


   /**
    *  根据用户Id搜索用户信息以及好友情况
    * @param id 用户Id
    * @return 用户以及好友信息
    */
   @Override
   public FriendBo querySolrInfo(Long id) {

      //登录人信息
      UserLoginQuery loginQuery = localUser.getUser();

      //先查询用户信息
      User user = userService.queryUserById(id);

      LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<Friend>()
            .eq(Friend::getFromId,loginQuery.getId())
            .eq(Friend::getToId,user.getId());

      Friend friend = baseMapper.selectOne(wrapper);

      FriendBo friendBo = new FriendBo();
      BeanUtils.copyProperties(user,friendBo);
      if (friend == null) {
         //不是好友
         friendBo.setStatus(1);
         return friendBo;
      }
      friendBo.setRemark(friend.getRemark());
      friendBo.setStatus(friend.getStatus());
      return friendBo;
   }

   /**
    *  修改对好友的备注
    * @param remark 好友备注
    * @param id  好友的id
    */
   @Override
   public void updateFriendRemark(String remark, Long id) {
      //登录人信息
      UserLoginQuery loginQuery = localUser.getUser();

      Friend friend = new Friend();
      friend.setRemark(remark);

      LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<Friend>()
            .eq(Friend::getFromId,loginQuery.getId())
            .eq(Friend::getToId,id);

      int update = baseMapper.update(friend, wrapper);

      if (update <= 0) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "修改失败");
      }
   }

   @Override
   public Friend queryOne(Long fromId, Long toId) {

      LambdaQueryWrapper<Friend> wrapper = new LambdaQueryWrapper<Friend>()
            .eq(Friend::getFromId,fromId)
            .eq(Friend::getToId, toId);
      return baseMapper.selectOne(wrapper);
   }
}
