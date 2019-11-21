package loancalc.apps.service;

import org.springframework.stereotype.Service;

import loancalc.apps.domain.LoanInstallmentPerMonth;
import loancalc.apps.model.LoanRequest;
import loancalc.apps.model.LoanResponse;

import java.util.ArrayList;
import java.util.List;

@Service
public class PaymentPlanGeneratorService {

    public List<LoanResponse> generatePaymentPlan(final LoanRequest request) {
        request.validate();
        final List<LoanInstallmentPerMonth> paymentPerMonthList = generatePaymentPlanList(request);
        return generateResponseList(paymentPerMonthList);
    }

    private List<LoanInstallmentPerMonth> generatePaymentPlanList(final LoanRequest request) {

        final List<LoanInstallmentPerMonth> paymentPerMonthList = new ArrayList<>();
        paymentPerMonthList.add(firstMonth(request));
        paymentPerMonthList.addAll(remainingMonths(request, paymentPerMonthList.get(0)));
        return paymentPerMonthList;
    }

    private List<LoanInstallmentPerMonth> remainingMonths(final LoanRequest request, LoanInstallmentPerMonth previousPaymentPerMonth) {

        final List<LoanInstallmentPerMonth> paymentPerMonthList = new ArrayList<>();

        for (int counter = 1; counter < request.getDuration(); counter++) {
            previousPaymentPerMonth = new LoanInstallmentPerMonth(request, previousPaymentPerMonth, counter);
            paymentPerMonthList.add(previousPaymentPerMonth);
        }

        return paymentPerMonthList;
    }

    private LoanInstallmentPerMonth firstMonth(final LoanRequest request) {
        return new LoanInstallmentPerMonth(request);
    }

    private List<LoanResponse> generateResponseList(final List<LoanInstallmentPerMonth> paymentPerMonthList) {

        final List<LoanResponse> responseList = new ArrayList<>();

        for (final LoanInstallmentPerMonth paymentPerMonth : paymentPerMonthList) {
            final LoanResponse response = new LoanResponse(paymentPerMonth);
            responseList.add(response);
        }

        return responseList;
    }
}
