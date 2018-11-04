package UbiTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.ubi.carpark.chargingapi.CarParkUbiApiDataApplication;
import com.ubi.carpark.chargingapi.carpark.CarPark;
import com.ubi.carpark.chargingapi.carpark.CarParkRepository;
import com.ubi.carpark.chargingapi.chargingpoint.ChargingPoint;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = CarParkUbiApiDataApplication.class)
@ActiveProfiles(profiles = "test")
public class CarParkApplicationTests {

    @Autowired
    CarParkRepository carParkRepository;
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Before
	public void setup() {
		MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
		ResponseEntity<String> response1 = createCarPark(body);
	}
    @After
	public void tearDown() {
		deleteCarPark();
	}
    @Test
   	public void getCarPark() throws Exception {
		List<CarPark> carParkList = Arrays
				.asList(this.restTemplate.getForEntity("/carpark", CarPark[].class).getBody());
		assertThat(carParkList).isNotEmpty();
    }
	private ResponseEntity<String> createCarPark(MultiValueMap<String, String> body) {
		CarPark carPark = new CarPark("UB1", "UB1 Car Parking", "UB1 Car Parking", 100);
		HttpEntity<CarPark> entity = new HttpEntity<CarPark>(carPark, body);
		ResponseEntity<String> response1 = this.restTemplate.exchange("/carpark", HttpMethod.POST, entity,
				String.class);
		return response1;
	}
	private void deleteCarPark() {
		String carParkIdForTesting="UB1";
		this.restTemplate.delete("/chargingpointsforall/"+carParkIdForTesting);
		this.restTemplate.delete("/carpark/"+carParkIdForTesting);
	}
    @Test
   	public void getChargingPoints() throws Exception {
		List<ChargingPoint> chargingPointList = Arrays
				.asList(this.restTemplate.getForEntity("/carpark/UB1/chargingpoints", ChargingPoint[].class).getBody());
		assertThat(chargingPointList).isNotEmpty();
    }
    @Test
	public void addChargingPointInSequence() throws Exception {
    	MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
    	String carParkIdForTesting="UB1";
		//increase load by plugging
		addOrRemoveChargingPoints_1_5(body, carParkIdForTesting,1);
		
		ResponseEntity<Integer> forEntity = this.restTemplate.getForEntity("/carpark/UB1/chargingpointsCapAvailable", Integer.class);
		Integer availCapacity = forEntity.getBody();
		assertTrue("addChargingPointInSequence chargingpointsCapAvailable return status code",forEntity.getStatusCode()==HttpStatus.OK);
		assertTrue("addChargingPointInSequence chargingpointsCapAvailable  step#1", availCapacity == 0);
		
		addOrRemoveChargingPoints_6_10(body, carParkIdForTesting,1);
		Integer availCapacity1 = this.restTemplate.getForEntity("/carpark/UB1/chargingpointsCapAvailable", Integer.class).getBody();
		assertTrue("addChargingPointInSequence chargingpointsCapAvailable step#2", availCapacity1 == 0);
	}
    @Test
   	public void removeChargingPointInSequence() throws Exception {
       	MultiValueMap<String, String> body = new LinkedMultiValueMap<String, String>();
       	String carParkIdForTesting="UB1";
       	//setup for intial test data
       	addOrRemoveChargingPoints_1_5(body, carParkIdForTesting,1);
       	addOrRemoveChargingPoints_6_10(body, carParkIdForTesting,1);
       	
   		//decrease load by unplugging
   		addOrRemoveChargingPoints_1_5(body, carParkIdForTesting,0);
   		
   		Integer availCapacity_1_5 = this.restTemplate.getForEntity("/carpark/UB1/chargingpointsCapAvailable", Integer.class).getBody();
   		assertTrue("removeChargingPointInSequence step#1", availCapacity_1_5 == 0);
   		
   		addOrRemoveChargingPoints_6_10(body, carParkIdForTesting,0);
   		Integer availCapacity_6_10 = this.restTemplate.getForEntity("/carpark/UB1/chargingpointsCapAvailable", Integer.class).getBody();
   		assertTrue("removeChargingPointInSequence step#2", availCapacity_6_10 == 100);
   	}

	private void addOrRemoveChargingPoints_1_5(MultiValueMap<String, String> body, String carParkId,Integer inUse) {
		ChargingPoint chargingPoint1 = new ChargingPoint("CP1", "ChargingPoint#1", "ChargingPoint#1", 20,inUse, carParkId);
		chargingPointUpdateService(body, chargingPoint1);
		ChargingPoint chargingPoint2 = new ChargingPoint("CP2", "ChargingPoint#2", "ChargingPoint#2", 20,inUse, carParkId);
		chargingPointUpdateService(body, chargingPoint2);
		ChargingPoint chargingPoint3 = new ChargingPoint("CP3", "ChargingPoint#3", "ChargingPoint#3", 20,inUse, carParkId);
		chargingPointUpdateService(body, chargingPoint3);
		ChargingPoint chargingPoint4 = new ChargingPoint("CP4", "ChargingPoint#4", "ChargingPoint#4", 20,inUse, carParkId);
		chargingPointUpdateService(body, chargingPoint4);
		ChargingPoint chargingPoint5 = new ChargingPoint("CP5", "ChargingPoint#5", "ChargingPoint#5", 20,inUse, carParkId);
		chargingPointUpdateService(body, chargingPoint5);
	}
	private void addOrRemoveChargingPoints_6_10(MultiValueMap<String, String> body, String carParkId,Integer inUse) {
		ChargingPoint chargingPoint6 = new ChargingPoint("CP6", "ChargingPoint#6", "ChargingPoint#1", 20,inUse, carParkId);
		chargingPointUpdateService(body, chargingPoint6);
		ChargingPoint chargingPoint7 = new ChargingPoint("CP7", "ChargingPoint#7", "ChargingPoint#2", 20,inUse, carParkId);
		chargingPointUpdateService(body, chargingPoint7);
		ChargingPoint chargingPoint8 = new ChargingPoint("CP8", "ChargingPoint#8", "ChargingPoint#3", 20,inUse, carParkId);
		chargingPointUpdateService(body, chargingPoint8);
		ChargingPoint chargingPoint9 = new ChargingPoint("CP9", "ChargingPoint#9", "ChargingPoint#4", 20,inUse, carParkId);
		chargingPointUpdateService(body, chargingPoint9);
		ChargingPoint chargingPoint10 = new ChargingPoint("CP10", "ChargingPoint#10", "ChargingPoint#5", 20,inUse, carParkId);
		chargingPointUpdateService(body, chargingPoint10);
	}
	private void chargingPointUpdateService(MultiValueMap<String, String> body, ChargingPoint chargingPoint) {
		HttpEntity<ChargingPoint> chargingPoint1 = new HttpEntity<ChargingPoint>(chargingPoint, body);
		ResponseEntity<String> response2 = this.restTemplate.exchange("/carpark/UB1/chargingpoints/"+chargingPoint.getId(), HttpMethod.PUT, chargingPoint1,
				String.class);
	}

}
