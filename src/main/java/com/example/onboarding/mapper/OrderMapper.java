package com.example.onboarding.mapper;

import com.example.onboarding.domain.OrderRecord;
import com.example.onboarding.domain.OrderStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface OrderMapper {

    int insert(OrderRecord order);

    OrderRecord selectById(@Param("id") Long id);

    List<OrderRecord> selectRecentCreated(@Param("limit") int limit);

    int updateStatus(@Param("id") Long id, @Param("status") OrderStatus status);
}
