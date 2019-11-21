package loancalc.apps.controller;

import java.time.Instant;

import org.springframework.web.client.RestTemplate;

import loancalc.apps.model.LoanRequest;
import loancalc.apps.model.LoanResponse;
import loancalc.apps.service.PaymentPlanGeneratorService;

public class StandaloneTestApp {

	public static void main(String[] args) {
		PaymentPlanGeneratorService paymentPlanGeneratorService = new PaymentPlanGeneratorService();
		calculateLoanSchedule();
		
	}
	private static void calculateLoanSchedule()
	{
	    final String uri = "http://localhost:8094/generate-plan";
	    Instant lt = Instant.now();
	    Instant inst = Instant.parse("2017-02-03T11:35:30.00Z");  
	    
	    
	    double loanAmount = 5000d;
		double nominalRate = 5d;
		int duration = 24;
		String startDate = Instant.parse("2019-11-19T00:00:00.00Z").toString();
		
		LoanRequest request = new LoanRequest(loanAmount, nominalRate, duration, startDate);
	    System.out.println("Loan input:"+request);
	    RestTemplate restTemplate = new RestTemplate();
	    LoanResponse[] result = restTemplate.postForObject( uri, request, LoanResponse[].class);
	    System.out.println("Loan output:");
	    for (LoanResponse loanResponse : result)
	       	    System.out.println(loanResponse);
	}

}
