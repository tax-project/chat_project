package com.dkm.user.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dkm.constanct.CodeType;
import com.dkm.entity.websocket.MsgInfo;
import com.dkm.exception.ApplicationException;
import com.dkm.file.service.IFileService;
import com.dkm.file.utils.FileVo;
import com.dkm.friend.entity.vo.FriendAllListVo;
import com.dkm.friend.entity.vo.FriendNotOnlineVo;
import com.dkm.friend.entity.vo.IdVo;
import com.dkm.friend.service.IFriendNotOnlineService;
import com.dkm.jwt.contain.LocalUser;
import com.dkm.jwt.entity.UserLoginQuery;
import com.dkm.user.dao.UserMapper;
import com.dkm.user.entity.User;
import com.dkm.user.entity.vo.UpdateVo;
import com.dkm.user.entity.vo.UserUpdateInfoVo;
import com.dkm.user.entity.vo.UserVo;
import com.dkm.user.service.IUserService;
import com.dkm.user.vilidata.CreateToken;
import com.dkm.user.vilidata.MyMessagePostProcessor;
import com.dkm.utils.IdGenerator;
import com.dkm.utils.ShaUtils;
import com.dkm.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author qf
 * @date 2020/5/11
 * @vesion 1.0
 **/
@Slf4j
@Service
@Transactional(rollbackFor = Exception.class)
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

   @Autowired
   private IdGenerator idGenerator;

   @Autowired
   private CreateToken createToken;

   @Autowired
   private RabbitTemplate rabbitTemplate;

   @Autowired
   private RedisTemplate redisTemplate;

   @Autowired
   private LocalUser localUser;

   @Autowired
   private IFileService fileService;

   @Value("${file.qrCodeUrl}")
   private String qrCodeUrl;

   @Value("${file.heardUrl}")
   private String heardUrl;

   @Value("${file.womanHeardUrl}")
   private String womanHeardUrl;

   @Autowired
   private IFriendNotOnlineService friendNotOnlineService;

   /**
    * 注册用户信息
    * @param userVo
    */
   @Override
   public synchronized void registerUser(UserVo userVo) {

      //查询用户名是否重复
      LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
            .eq(User::getUserName,userVo.getUserName());

      User one = baseMapper.selectOne(wrapper);

      if (one != null) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "该账号已被使用");
      }

      User user = new User();
      Long userId = idGenerator.getNumberId();
      user.setId(userId);

      user.setNickName(userVo.getNickName());
      user.setUserName(userVo.getUserName());
      user.setPassword(ShaUtils.getSha1(userVo.getPassword()));
      //生成二维码
      FileVo qrCode = fileService.getQrCode(qrCodeUrl + "?userId=" + userId);
      user.setQrCode(qrCode.getFileUrl());
      user.setSex(userVo.getSex());

      if (userVo.getSex() == 1) {
         //男
         //默认头像
         user.setHeadUrl(heardUrl);
      } else {
         //女
         user.setHeadUrl(womanHeardUrl);
      }

      int insert = baseMapper.insert(user);

      if (insert <= 0) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "注册失败");
      }

   }


   /**
    * 登录
    * @param userName
    * @param password
    * @return
    */
   @Override
   public Map<String, Object> loginUser(String userName, String password) {

      LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
            .eq(User::getUserName,userName);

      User user = baseMapper.selectOne(wrapper);

      if (user == null) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "该账号不存在");
      }

      if (!ShaUtils.getSha1(password).equals(user.getPassword())) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "您输入的密码有误");
      }


      //判断该账号是否已经登录，如果已经登录，则强行挤下线
      //先去未在线消息表中查询是否有未在线消息，若有，则不在线，并且将未在线消息发送给客户端
      //没有离线消息,
      //去redis中查询是否有cid,若是有，则先通知服务器强制下线，同时修改redis中的cid
      String cid = (String) redisTemplate.opsForValue().get(user.getId());

      if (StringUtils.isNotBlank(cid)) {
         //找到设备编号发给客户端强制下线
         MsgInfo msgInfo = new MsgInfo();
         msgInfo.setType(100);
         msgInfo.setCid(cid);
         rabbitTemplate.convertAndSend("chat_msg_fanoutExchange","",JSON.toJSONString(msgInfo));
      }
      //将用户Id和cid绑定存入redis中返回token以及一些必要的信息给前端
      String cId = idGenerator.getUuid();
      redisTemplate.opsForValue().set(user.getId(),cId);

      //生成token
      String token = createToken.getToken(user);

      Map<String,Object> map = new HashMap<>(3);
      map.put("token",token);
      map.put("userInfo",user);
      map.put("cid",cid);

      return map;
   }


   /**
    * 修改信息
    * @param vo
    */
   @Override
   public void updateUserInfo(UserUpdateInfoVo vo) {
      User user = new User();
      user.setId(vo.getId());
      user.setSex(vo.getSex());
      user.setNickName(vo.getNickName());
      user.setAddress(vo.getAddress());
      user.setSign(vo.getSign());

      int i = baseMapper.updateById(user);

      if (i <= 0) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "修改失败");
      }
   }


   /**
    * 修改密码
    * @param vo
    */
   @Override
   public void updatePassword(UpdateVo vo) {

      User user = baseMapper.selectById(vo.getId());

      if (user == null) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "该id不存在");
      }

      if (!ShaUtils.getSha1(vo.getOldPassword()).equals(user.getPassword())) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "您输入的原密码错误");
      }

      User user1 = new User();
      user1.setId(vo.getId());
      user1.setPassword(ShaUtils.getSha1(vo.getNewPassword()));

      int i = baseMapper.updateById(user1);

      if (i <= 0) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "修改密码失败");
      }
   }

   @Override
   public User queryUserById(Long id) {
      return baseMapper.selectById(id);
   }


   /**
    *  查询当前登陆人所有好友
    * @param list id的集合
    * @return 好友信息的集合
    */
   @Override
   public List<FriendAllListVo> queryAllFriend(List<IdVo> list) {

      if (null == list || list.size() == 0) {
         return null;
      }

      return baseMapper.queryAllFriend(list);
   }


   /**
    *  修改头像
    * @param heardUrl 头像地址
    */
   @Override
   public Long updateHeardUrl(String heardUrl) {

      UserLoginQuery query = localUser.getUser();

      User user = new User();
      user.setId(query.getId());
      user.setHeadUrl(heardUrl);

      int update = baseMapper.updateById(user);

      if (update <= 0) {
         throw new ApplicationException(CodeType.SERVICE_ERROR, "修改头像失败");
      }

      return query.getId();
   }


   /**
    * 退出登录
    */
   @Override
   public void signOutLogin() {

      UserLoginQuery user = localUser.getUser();

      String cid = (String) redisTemplate.opsForValue().get(user.getId());

      if (StringUtils.isNotBlank(cid)) {
         //通知服务器移除连接
         MsgInfo msgInfo = new MsgInfo();
         msgInfo.setType(102);
         msgInfo.setCid(cid);

         rabbitTemplate.convertAndSend("chat_msg_fanoutExchange","",JSON.toJSONString(msgInfo));

         redisTemplate.delete(user.getId());
      }
   }

   @Override
   public User queryUserByName(String userName) {
      LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
            .eq(User::getUserName,userName);

      return baseMapper.selectOne(wrapper);
   }

   @Override
   public List<MsgInfo> getNotOnlineInfo() {

      UserLoginQuery user = localUser.getUser();

      List<FriendNotOnlineVo> list = friendNotOnlineService.queryOne(user.getId());

      List<MsgInfo> msgInfoList = new ArrayList<>();

      if (null != list && list.size() != 0) {
         //有离线消息,当前该账号未在线，将未在线消息发送给客户端
         List<Long> longList = new ArrayList<>();
         for (FriendNotOnlineVo onlineVo : list) {
            MsgInfo msgInfo = new MsgInfo();
            BeanUtils.copyProperties(onlineVo, msgInfo);

            //离线消息
            msgInfo.setMsg(onlineVo.getContent());
            msgInfo.setSendDate(onlineVo.getCreateDate());

            msgInfoList.add(msgInfo);
            //将消息更改成已读
            longList.add(onlineVo.getToId());
         }

         //删除离线表中的未读状态
         friendNotOnlineService.deleteLook(longList);

      }

      return msgInfoList;
   }
}
