package com.aimango.robot.server.mapper;

import com.aimango.robot.server.core.annotation.Repository;
import org.apache.ibatis.annotations.Mapper;

@Repository
@Mapper
public interface TestMapper {
    Integer select1();
}
