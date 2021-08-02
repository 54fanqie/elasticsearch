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
    private Long id;

    private String esId;

    private Long sealId;

    private String sealName;

    private Long userId;

    private String userName;

    private Long sealDeptId;

    private String sealDeptName;

    private Long userDeptId;

    private String userDeptName;

    private int businessType;

    private int status;

    private Date actionTime;

    private int esKey;
}
