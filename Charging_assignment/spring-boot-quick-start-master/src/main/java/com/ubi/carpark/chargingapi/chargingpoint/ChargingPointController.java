package com.ubi.carpark.chargingapi.chargingpoint;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ubi.carpark.chargingapi.carpark.CarPark;

@RestController
public class ChargingPointController {
	@Autowired
	private ChargingPointService courseService;
	@RequestMapping("/carpark/{id}/chargingpoints")
	public List<ChargingPoint> getAllChargingPoints(@PathVariable String id){
		return courseService.getAllChargingPoints(id);
	}
	@RequestMapping("/carpark/{id}/chargingpointsCapAvailable")
	public ResponseEntity<Integer> getAvailableCapacity(@PathVariable String id){
		return ResponseEntity.ok(courseService.calculatePowerCapactityAvl(id));
	}
	@RequestMapping("/carpark/{carparkId}/chargingpoints/{id}")
	public ChargingPoint getChargingPoint(@PathVariable String id){
		return courseService.getChargingPoint(id);
	}
	@RequestMapping(method=RequestMethod.POST,value="/carpark/{carparkId}/chargingpoints")
	public void addChargingPoint(@RequestBody ChargingPoint chargingpoint,@PathVariable String carparkId)
	{
		chargingpoint.setCarPark(new CarPark(carparkId,"","",0));
		courseService.addChargingPoint(chargingpoint);
	}
	
	@RequestMapping(method=RequestMethod.PUT,value="/carpark/{carparkId}/chargingpoints/{id}")
	public void updateChargingPoint(@RequestBody ChargingPoint chargingpoint, @PathVariable String carparkId,
			@PathVariable String id)
	{
		chargingpoint.setCarPark(new CarPark(carparkId,"","",0));
		courseService.updateChargingPoint(chargingpoint);
	}
	@RequestMapping(method=RequestMethod.DELETE,value="/chargingpoints/{id}")
	public void deleteChargingPoint(@PathVariable String id)
	{
		courseService.deleteChargingPoint(id);
	}
	@RequestMapping(method=RequestMethod.DELETE,value="/chargingpointsforall/{carparkId}")
	public void deleteChargingPointForCarPark(@PathVariable String carparkId)
	{
		courseService.deleteChargingPointForCarPark(carparkId);
	}
 }
