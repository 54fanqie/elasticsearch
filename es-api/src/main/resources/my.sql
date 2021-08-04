select psl.id as id,psl.seal_id as sealId, ps.es_id as esId, ps.name as sealName, pu.id as userId, pu.user_name as userName,pd.id as sealDeptId,pd.dept_name as sealDeptName,(case psl.business_type when '' then 0  when '系统签章' then 1  when '页面签章' then 2 when 'SDK' then 3 else 0 end ) as businessType, date_format(psl.action_time,'%Y-%m-%d %H:%i:%S')  as actionTime,psl.action_time  as actionTimeStamp
from ps_seal_log psl
         left join ps_seal ps on psl.seal_id = ps.id
         left join ps_user pu on psl.user_id = pu.id
         left join ps_seal_dept psd on psl.seal_id = psd.seal_id
         left join ps_department pd on psd.dept_id = pd.id where  psl.id > :sql_last_value