package net.brightroom.example.provider.database;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface GateManagementMapper {

  Boolean findEnabledByGateId(String gateId);
}
