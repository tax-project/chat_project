package com.dkm.manyChat.vilidata;

import com.dkm.manyChat.entity.ManyChat;
import com.dkm.manyChat.entity.bo.ManyChatInfoBO;
import com.dkm.manyChat.entity.vo.ManyChatResultVo;
import com.dkm.manyChat.service.IManyChatInfoService;
import com.dkm.manyChat.service.IManyChatService;
import com.dkm.utils.DateUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author qf
 * @date 2020/7/17
 * @vesion 1.0
 **/
@Component
public class ControllerUtils {


   @Autowired
   private IManyChatService manyChatService;

   @Autowired
   private IManyChatInfoService manyChatInfoService;

   public ManyChatResultVo getResult (Long id) {

      ManyChatResultVo vo = new ManyChatResultVo();

      ManyChat manyChat = manyChatService.queryById(id);

      BeanUtils.copyProperties(manyChat, vo);
      vo.setCreateDate(DateUtil.formatDateTime(manyChat.getCreateDate()));

      List<ManyChatInfoBO> list = manyChatInfoService.getManyInfoAllList(id);

      vo.setList(list);

      return vo;
   }
}
