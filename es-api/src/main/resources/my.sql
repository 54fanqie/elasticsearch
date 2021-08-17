SELECT
    psl.id AS id,
    psl.seal_id AS sealId,
    ps.es_id AS esId,
    ps.name AS sealName,
    ps.owner_name AS ownerName,
    pu.id AS userId,
    pu.user_name AS userName,
    pd.id AS sealDeptId,
    pd.dept_name AS sealDeptName,
    (
        CASE psl.business_type
            WHEN '' THEN
                0
            WHEN '系统签章' THEN
                1
            WHEN '页面签章' THEN
                2
            WHEN 'SDK' THEN
                3
            ELSE
                0
            END
        ) AS businessType,
    date_format(
            psl.action_time,
            '%Y-%m-%d %H:%i:%S'
        ) AS actionTime
FROM
    (select id,seal_id,user_id,business_type,action_time from ps_seal_log  where id > :sql_last_value ) psl
        LEFT JOIN ps_seal ps ON psl.seal_id = ps.id
        LEFT JOIN ps_user pu ON psl.user_id = pu.id
        LEFT JOIN ps_seal_dept psd ON psl.seal_id = psd.seal_id
        LEFT JOIN ps_department pd ON psd.dept_id = pd.id;


