package com.dkm.friend.dao;

import com.dkm.IBaseMapper.IBaseMapper;
import com.dkm.friend.entity.FriendNotOnline;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author qf
 * @date 2020/5/11
 * @vesion 1.0
 **/
@Component
public interface FriendNotOnlineMapper extends IBaseMapper<FriendNotOnline> {


   /**
    *  更改未读状态
    * @param list id集合
    * @return 返回结果
    */
   Integer deleteLook(List<Long> list);

   /**
    *  批量增加未在线消息
    * @param list 未在线消息参数列表
    * @return 增加结果
    */
   Integer allInsertNotOnlineInfo(List<FriendNotOnline> list);
}
