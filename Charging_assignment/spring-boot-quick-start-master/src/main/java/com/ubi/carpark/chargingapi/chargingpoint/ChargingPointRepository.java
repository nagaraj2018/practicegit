package com.ubi.carpark.chargingapi.chargingpoint;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface ChargingPointRepository extends CrudRepository<ChargingPoint,String> {

	public List<ChargingPoint> findByCarParkId(String topicId);
}
