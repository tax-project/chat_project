package com.dkm.manyChat.dao;

import com.dkm.IBaseMapper.IBaseMapper;
import com.dkm.manyChat.entity.ManyChatInfo;
import com.dkm.manyChat.entity.bo.ManyChatInfoBO;
import com.dkm.manyChat.entity.vo.ManyChatInfoVo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author qf
 * @date 2020/5/15
 * @vesion 1.0
 **/
@Component
public interface ManyChatInfoMapper extends IBaseMapper<ManyChatInfo> {

   /**
    *  建立群聊的具体群员
    * @param list 成员信息
    * @return 返回增加结果
    */
   Integer insertAllUser(List<ManyChatInfoVo> list);

   /**
    *  查询群聊具体的人员头像昵称等信息
    * @param manyChatId 群聊id
    * @return 返回具体信息
    */
   List<ManyChatInfoBO> getManyInfoAllList (Long manyChatId);


}
