package com.ubi.carpark.chargingapi.chargingpoint;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.ubi.carpark.chargingapi.carpark.CarPark;
import com.ubi.carpark.chargingapi.carpark.CarParkService;

@Service
public class ChargingPointService {
	@Autowired
	private ChargingPointRepository chargingPointRepository;
	@Autowired
	private CarParkService carParkService;
	public List<ChargingPoint> getAllChargingPoints(String carparkId)
	{
		List<ChargingPoint> chargingpoints =new ArrayList<>();
		 chargingPointRepository.findByCarParkId(carparkId).forEach(chargingpoints::add);
		 return chargingpoints;
	}
	public ChargingPoint getChargingPoint(String id)
	{
		return (ChargingPoint) this.chargingPointRepository.findOne(id);
	}
	public void addChargingPoint(ChargingPoint chargingpoint)
	{
		chargingPointRepository.save(chargingpoint);
	}
	public void updateChargingPoint(ChargingPoint chargingpointParam) {
		/*calcuate the available capacity*/
		
		if(chargingpointParam.getInUse()==1)	//plugged in. increase in load
		{
			allocateCapacityForChargingPoint(chargingpointParam);
		}
		else if(chargingpointParam.getInUse()==0) //un-plugged. decrease in load
		{
			reAllocateCapacityForChargingPoint(chargingpointParam);
		}
	}

	public Integer calculatePowerCapactityAvl(String carParkId) {
		CarPark carPark = carParkService.getCarPark(carParkId);
		Integer chargingCapacity = carPark.getChargingCapacity();
		System.out.println("car park chargingCapacity=" + chargingCapacity);
		List<ChargingPoint> allChargingPoints = getAllChargingPoints(carParkId);

		Map<String, ChargingPoint> chargingPointMapInUse = allChargingPoints.stream()
				.filter(chargingPoint -> chargingPoint.getInUse() == 1)
				.collect(Collectors.toMap(ChargingPoint::getId, chargingPoint -> chargingPoint));
		System.out.println("car park current chargingPointMap=" + chargingPointMapInUse);

		Integer allocatedCapacity = allChargingPoints.stream().filter(chargingPoint -> chargingPoint.getInUse() == 1)
				.mapToInt(ChargingPoint::getChargingQty).sum();
		System.out.println("car park current allocatedCapacity=" + allocatedCapacity);
		Integer avlCapactiy = chargingCapacity - allocatedCapacity;
		return avlCapactiy;
	}
	private Integer allocateCapacityForChargingPoint(ChargingPoint chargingpointParam) {
		CarPark carPark = carParkService.getCarPark(chargingpointParam.getCarPark().getId());
		Integer avlCapactiy = calculatePowerCapactityAvl(carPark.getId());
		List<ChargingPoint> allChargingPoints = getAllChargingPoints(chargingpointParam.getCarPark().getId());
		Long numOfchargingPointInUse = allChargingPoints.stream().filter(chargingPoint -> chargingPoint.getInUse() == 1)
				.count();
		System.out.println("numOfchargingPointInUse=" + numOfchargingPointInUse);
		if(numOfchargingPointInUse<5)
		{
			 plugChargePoint(chargingpointParam,20);
		}
		else //if(numOfchargingPointInUse>=5)
		{
					//find the oldest cp and reduce the load
					 List<ChargingPoint> chargingpointsSorted = allChargingPoints.stream()
							 .filter(chargingPoint -> chargingPoint.getInUse() == 1)
							 .filter(chargingPoint -> chargingPoint.getChargingQty() == 20)
							 .sorted((o1, o2)->o1.getChargeStartTime().
			                 compareTo(o2.getChargeStartTime())).
			                 collect(Collectors.toList());
					 if(chargingpointsSorted.size()>0)
					 {
						 ChargingPoint chargingPoint= chargingpointsSorted.get(0);
						 if(chargingPoint.getChargingQty()==20)
						 {
							 chargingPoint.setChargingQty(10);
							 chargingPointRepository.save(chargingPoint);
							 plugChargePoint(chargingpointParam,10);
						 }
					 }
		}
		return avlCapactiy;
	}

	private Integer reAllocateCapacityForChargingPoint(ChargingPoint chargingpointParam) {
		List<ChargingPoint> allChargingPoints = getAllChargingPoints(chargingpointParam.getCarPark().getId());
		Long numOfchargingPointInUse = allChargingPoints.stream().filter(chargingPoint -> chargingPoint.getInUse() == 1)
				.count();
		System.out.println("numOfchargingPointInUse=" + numOfchargingPointInUse);
		unPlugChargePoint(chargingpointParam);
		Integer avlCapactiy = calculatePowerCapactityAvl(chargingpointParam.getCarPark().getId());
		if (numOfchargingPointInUse > 5)// less than equal to 5 no
										// re-distribution
		{
			// find the newest cp and increase the load
			List<ChargingPoint> chargingpointsSorted = allChargingPoints.stream()
					.filter(chargingPoint -> chargingPoint.getInUse() == 1)
					.filter(chargingPoint -> chargingPoint.getChargingQty() == 10)
					.sorted((o1, o2) -> o2.getChargeStartTime().compareTo(o1.getChargeStartTime()))
					.collect(Collectors.toList());
			if (chargingpointsSorted.size() > 0) {
				if (avlCapactiy == 10) {
					ChargingPoint chargingPoint = chargingpointsSorted.get(0);
					plugChargePoint(chargingPoint, 20);
				} else if (avlCapactiy == 20) {
					ChargingPoint chargingPoint = chargingpointsSorted.get(1);
					plugChargePoint(chargingPoint, 20);
				}
			} 
		}

		return avlCapactiy;
	}
	private void unPlugChargePoint(ChargingPoint chargingpointParam) {
		chargingpointParam.setInUse(0);
		chargingpointParam.setChargingQty(0);
		chargingpointParam.setChargeStartTime(null);
		chargingPointRepository.save(chargingpointParam);
	}
	private void plugChargePoint(ChargingPoint chargingpointParam, Integer ChargingQty) {
		chargingpointParam.setInUse(1);
		chargingpointParam.setChargingQty(ChargingQty);
		chargingpointParam.setChargeStartTime(LocalDateTime.now());
		chargingPointRepository.save(chargingpointParam);
	}
	public void deleteChargingPoint(String id) {
		this.chargingPointRepository.delete(id);
		
	}
	public void deleteChargingPointForCarPark(String carParkId) {
		CarPark carPark = carParkService.getCarPark(carParkId);
		List<ChargingPoint> allChargingPoints = getAllChargingPoints(carParkId);
		 for(ChargingPoint chargingPoint : allChargingPoints) {
             this.chargingPointRepository.delete(chargingPoint.getId());
       }
		
	}
}
