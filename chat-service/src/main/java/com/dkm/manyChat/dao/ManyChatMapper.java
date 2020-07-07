package com.dkm.manyChat.dao;

import com.dkm.IBaseMapper.IBaseMapper;
import com.dkm.manyChat.entity.ManyChat;
import com.dkm.manyChat.entity.vo.ManyChatListVo;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author qf
 * @date 2020/5/15
 * @vesion 1.0
 **/
@Component
public interface ManyChatMapper extends IBaseMapper<ManyChat> {

   /**
    *  查询我加入的群聊
    * @param userId 用户id
    * @return 返回所有我加入的群聊信息
    */
   List<ManyChatListVo> queryManyChatList(Long userId);
}
