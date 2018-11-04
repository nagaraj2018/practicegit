package com.ubi.carpark.chargingapi.carpark;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ubi.carpark.chargingapi.chargingpoint.ChargingPoint;
import com.ubi.carpark.chargingapi.chargingpoint.ChargingPointService;

@Service
public class CarParkService {
	@Autowired
	private CarParkRepository carParkRepository;
	@Autowired
	private ChargingPointService chargingPointService;
	public List<CarPark> getAllCarParks()
	{
		List<CarPark> carparks =new ArrayList<>();
		 carParkRepository.findAll().forEach(carparks::add);
		 return carparks;
	}
	public CarPark getCarPark(String id)
	{
		return (CarPark) this.carParkRepository.findOne(id);		
	}
	
	public CarPark addCarPark(CarPark carPark)
	{
		CarPark saveCarPark = carParkRepository.save(carPark);
		ChargingPoint chargingpoint1=new ChargingPoint("CP1","ChargingPoint#1","ChargingPoint#1",0,0,carPark.getId());
		ChargingPoint chargingpoint2=new ChargingPoint("CP2","ChargingPoint#2","ChargingPoint#2",0,0,carPark.getId());
		ChargingPoint chargingpoint3=new ChargingPoint("CP3","ChargingPoint#3","ChargingPoint#3",0,0,carPark.getId());
		ChargingPoint chargingpoint4=new ChargingPoint("CP4","ChargingPoint#4","ChargingPoint#4",0,0,carPark.getId());
		ChargingPoint chargingpoint5=new ChargingPoint("CP5","ChargingPoint#5","ChargingPoint#5",0,0,carPark.getId());
		ChargingPoint chargingpoint6=new ChargingPoint("CP6","ChargingPoint#6","ChargingPoint#6",0,0,carPark.getId());
		ChargingPoint chargingpoint7=new ChargingPoint("CP7","ChargingPoint#7","ChargingPoint#7",0,0,carPark.getId());
		ChargingPoint chargingpoint8=new ChargingPoint("CP8","ChargingPoint#8","ChargingPoint#8",0,0,carPark.getId());
		ChargingPoint chargingpoint9=new ChargingPoint("CP9","ChargingPoint#9","ChargingPoint#9",0,0,carPark.getId());
		ChargingPoint chargingpoint10=new ChargingPoint("CP10","ChargingPoint#10","ChargingPoint#10",0,0,carPark.getId());
		
//		chargingpoint.setTopic(new Topic(topicId,"",""));
		chargingPointService.addChargingPoint(chargingpoint1);
		chargingPointService.addChargingPoint(chargingpoint2);
		chargingPointService.addChargingPoint(chargingpoint3);
		chargingPointService.addChargingPoint(chargingpoint4);
		chargingPointService.addChargingPoint(chargingpoint5);
		chargingPointService.addChargingPoint(chargingpoint6);
		chargingPointService.addChargingPoint(chargingpoint7);
		chargingPointService.addChargingPoint(chargingpoint8);
		chargingPointService.addChargingPoint(chargingpoint9);
		chargingPointService.addChargingPoint(chargingpoint10);
		return saveCarPark;
	}
	public void updateCarPark(CarPark topicParam, String id) {
		carParkRepository.save(topicParam);
	}
	public void deleteCarPark(String id) {
		this.carParkRepository.delete(id);;
		
	}
}
