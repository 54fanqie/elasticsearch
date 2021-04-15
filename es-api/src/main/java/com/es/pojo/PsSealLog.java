package com.es.pojo;

import lombok.Data;

import java.util.Date;

/**
 * @author fanqie
 * @ClassName PsSealLog
 * @date 2021/4/15 下午5:34
 **/
@Data
public class PsSealLog {
    private String sealName;
    private String esId;
    private Long deptId;
    private String deptName;
    private Date signTime;
    private String businessType;
    private String businessName;
    private Long userId;
    private String userName;
    private String userAccount;
}
