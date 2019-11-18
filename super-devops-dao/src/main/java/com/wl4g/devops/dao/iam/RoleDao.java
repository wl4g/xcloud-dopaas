package com.wl4g.devops.dao.iam;

import com.wl4g.devops.common.bean.iam.Role;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RoleDao {
    int deleteByPrimaryKey(Integer id);

    int insert(Role record);

    int insertSelective(Role record);

    Role selectByPrimaryKey(Integer id);

    List<Role> selectByRoot();

    int updateByPrimaryKeySelective(Role record);

    int updateByPrimaryKey(Role record);

    List<Role> selectByUserId(Integer userId);

    List<Role> selectByGroupId(Integer groupId);

    List<Role> selectByGroupIds(@Param("groupIds") List<Integer> groupIds);

    List<Role> list(@Param("groupIds") List<Integer> groupIds,@Param("name") String name, @Param("displayName") String displayName);
}