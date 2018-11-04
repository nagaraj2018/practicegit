package com.ubi.carpark.chargingapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

import com.ubi.carpark.chargingapi.carpark.CarParkController;
import com.ubi.carpark.chargingapi.chargingpoint.ChargingPointController;

@SpringBootApplication
public class CarParkUbiApiDataApplication {

	public static void main(String[] args) {
		SpringApplication.run(CarParkUbiApiDataApplication.class, args);
	}

}
