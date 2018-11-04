package com.ubi.carpark.chargingapi.carpark;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class CarPark {
	@Id
	private String id;
	private String name;
	private String description;
	private Integer chargingCapacity;
	public CarPark(String id, String name, String description, Integer chargingCapacity) {
		super();
		this.id = id;
		this.name = name;
		this.description = description;
		this.chargingCapacity=chargingCapacity;
	}
	@Override
	public String toString() {
		return "CarPark [id=" + id + ", name=" + name + ", description=" + description + ", chargingCapacity="
				+ chargingCapacity + "]";
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public CarPark() {
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
	public Integer getChargingCapacity() {
		return chargingCapacity;
	}
	public void setChargingCapacity(Integer chargingCapacity) {
		this.chargingCapacity = chargingCapacity;
	}

}
