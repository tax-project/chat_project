package com.dkm.friend.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * @author qf
 * @date 2020/5/11
 * @vesion 1.0
 **/
@Data
@TableName("tb_friend")
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = false)
public class Friend extends Model<Friend> {


   private Long id;

   /**
    * 谁的账号
    */
   private Long fromId;

   /**
    * 好友的用户id
    */
   private Long toId;

   /**
    * 创建时间
    */
   private LocalDateTime createDate;

   /**
    * 0--好友
    * 2--拉黑
    */
   private Integer status;

   /**
    * 备注
    */
   private String remark;

   /**
    *  0--正常
    *  1--置顶
    */
   private Integer isTop;

   /**
    * 0--我加他
    * 1--他加我
    */
   private Integer isAddStatus;
}
