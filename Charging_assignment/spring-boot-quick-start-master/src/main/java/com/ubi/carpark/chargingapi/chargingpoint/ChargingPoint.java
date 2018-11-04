package com.ubi.carpark.chargingapi.chargingpoint;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.ubi.carpark.chargingapi.carpark.CarPark;

@Entity
public class ChargingPoint {
	@Id
	private String id;
	private String name;
	private String description;
	private Integer chargingQty=0;
	private Integer inUse=0;
	@JsonDeserialize(using=LocalDateDeserializer.class)
	private LocalDateTime  chargeStartTime;


	@ManyToOne
	private CarPark carPark;
	public String getId() {
		return id;
	}

	public ChargingPoint(String id, String name, String description, Integer chargingQty,Integer inUse, String carParkId) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.setChargingQty(chargingQty);
		this.setInUse(inUse);
		this.carPark = new CarPark(carParkId, "", "",0);
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public ChargingPoint() {
		super();
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public CarPark getCarPark() {
		return carPark;
	}
	public void setCarPark(CarPark topic) {
		this.carPark = topic;
	}

	public Integer getChargingQty() {
		return chargingQty;
	}

	public void setChargingQty(Integer chargingQty) {
		this.chargingQty = chargingQty;
	}

	public Integer getInUse() {
		return inUse;
	}

	public void setInUse(Integer inUse) {
		this.inUse = inUse;
	}

	public LocalDateTime getChargeStartTime() {
		return chargeStartTime;
	}

	public void setChargeStartTime(LocalDateTime chargeStartTime) {
		this.chargeStartTime = chargeStartTime;
	}


}
class LocalDateDeserializer extends JsonDeserializer<LocalDate>{

	  @Override
	  public LocalDate deserialize(JsonParser p, DeserializationContext ctxt)
	        throws IOException, JsonProcessingException {

	      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("required format");

	      LocalDate localDate = null;
	      localDate = LocalDate.parse(p.getText(), formatter);

	      return localDate;
	  }
	}