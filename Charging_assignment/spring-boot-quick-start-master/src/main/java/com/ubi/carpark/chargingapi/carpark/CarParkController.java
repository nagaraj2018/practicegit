package com.ubi.carpark.chargingapi.carpark;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CarParkController {
	@Autowired
	private CarParkService carparkService;
	
	@RequestMapping("/carpark")
	public List<CarPark> getAllCarParks(){
		return carparkService.getAllCarParks();
	}
	@RequestMapping("/carpark/{id}")
	public CarPark getCarPark(@PathVariable String id){
		return carparkService.getCarPark(id);
	}
	@RequestMapping(method=RequestMethod.POST,value="/carpark")
	public CarPark addCarPark(@RequestBody CarPark carpark)
	{
		return carparkService.addCarPark(carpark);
	}
	
	@RequestMapping(method=RequestMethod.PUT,value="/carpark/{id}")
	public void updateCarPark(@RequestBody CarPark carpark, @PathVariable String id)
	{
		carparkService.updateCarPark(carpark,id);
	}
	@RequestMapping(method=RequestMethod.DELETE,value="/carpark/{id}")
	public void deleteCarPark(@PathVariable String id)
	{
		carparkService.deleteCarPark(id);
	}
 }
