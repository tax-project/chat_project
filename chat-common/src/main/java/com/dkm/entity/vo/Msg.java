package com.dkm.entity.vo;

import com.dkm.entity.websocket.MsgInfo;
import lombok.Data;

import java.util.List;

/**
 * @author qf
 * @date 2020/7/7
 * @vesion 1.0
 **/
@Data
public class Msg {

   /**
    *  群聊未在线消息列表
    */
   private List<MsgInfo> list;
}
