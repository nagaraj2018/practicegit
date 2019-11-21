package loancalc.apps.controller;

import loancalc.apps.model.LoanRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import loancalc.apps.model.LoanResponse;
import loancalc.apps.service.PaymentPlanGeneratorService;

import java.util.List;

@RestController
@RequestMapping(value = "generate-plan")
public class PaymentPlanGeneratorController {

    @Autowired
    private PaymentPlanGeneratorService paymentPlanGeneratorService;

    @PostMapping
    @ResponseStatus(code = HttpStatus.OK)
    public List<LoanResponse> generatePaymentPlan(@RequestBody final LoanRequest request) {
        return paymentPlanGeneratorService.generatePaymentPlan(request);
    }
}