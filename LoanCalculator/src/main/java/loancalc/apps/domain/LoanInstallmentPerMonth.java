package loancalc.apps.domain;

import loancalc.apps.constants.Constants;

import java.math.BigDecimal;
import java.time.*;
import java.time.format.DateTimeFormatter;

import loancalc.apps.model.LoanRequest;

public class LoanInstallmentPerMonth {
    private BigDecimal borrowerPaymentAmount;
    private String date;
    private BigDecimal initialOutstandingPrincipal;
    private BigDecimal interest;
    private BigDecimal principal;
    private BigDecimal remainingOutstandingPrincipal;

    public LoanInstallmentPerMonth(final LoanRequest request) {
        this.borrowerPaymentAmount = roundOff(calculateBorrowerPaymentAmount(request));
        this.date = generatePaymentDate(request.getStartDate());
        this.initialOutstandingPrincipal = roundOff(request.getLoanAmount());
        this.interest = roundOff(calculateInterest(BigDecimal.valueOf(request.getNominalRate())));
        this.principal = roundOff(calculatePrincipal(this.borrowerPaymentAmount));
        this.remainingOutstandingPrincipal = roundOff(this.initialOutstandingPrincipal.subtract(this.principal));
    }

    public LoanInstallmentPerMonth(final LoanRequest request, final LoanInstallmentPerMonth previousPaymentPerMonth, final int counter) {
        this.initialOutstandingPrincipal = roundOff(previousPaymentPerMonth.getRemainingOutstandingPrincipal());
        this.date = generatePaymentDate(request.getStartDate(), counter);
        this.interest = roundOff(calculateInterest(BigDecimal.valueOf(request.getNominalRate())));
        this.principal = roundOff(calculatePrincipal(previousPaymentPerMonth.getBorrowerPaymentAmount()));
        this.borrowerPaymentAmount = roundOff(this.principal.add(this.interest));
        this.remainingOutstandingPrincipal = roundOff(this.initialOutstandingPrincipal.subtract(this.principal));
    }

    private BigDecimal roundOff(final double doubleValue) {
        final BigDecimal bigDecimalValue = BigDecimal.valueOf(doubleValue);
        return bigDecimalValue.setScale(Constants.DECIMALS, BigDecimal.ROUND_HALF_UP);
    }

    private BigDecimal roundOff(final BigDecimal value) {
        return value.setScale(Constants.DECIMALS, BigDecimal.ROUND_HALF_UP);
    }

    private double calculateBorrowerPaymentAmount(final LoanRequest request) {
        return borrowerPaymentAmountNumerator(request) / borrowerPaymentAmountDenominator(request);
    }

    private double borrowerPaymentAmountNumerator(final LoanRequest request) {
        return request.getLoanAmount() * interestRateOverPaymentsPerYear(request.getNominalRate());
    }

    private double borrowerPaymentAmountDenominator(final LoanRequest request) {
        final double base = 1 + interestRateOverPaymentsPerYear(request.getNominalRate());
        final double subtrahend = Math.pow(base, -request.getDuration());
        return (1 - subtrahend);
    }

    private double interestRateOverPaymentsPerYear(final double nominalRate) {
        return ((nominalRate / 100) / Constants.PAYMENTS_PER_YEAR);
    }

    private String generatePaymentDate(final String startDateTime) {
        final LocalDate startDate = convertStringToLocalDate(startDateTime);
        return convertLocalDateToString(startDate);
    }

    private String generatePaymentDate(final String startDateTime, final int counter) {
        final LocalDate startDate = convertStringToLocalDate(startDateTime);
        final LocalDate paymentDate = startDate.plusMonths(counter);
        return convertLocalDateToString(paymentDate);
    }

    private LocalDate convertStringToLocalDate(final String dateTimeAsString) {
        final Instant instant = Instant.parse(dateTimeAsString);
        final LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, ZoneId.of(ZoneOffset.UTC.getId()));
        return localDateTime.toLocalDate();
    }

    private String convertLocalDateToString(final LocalDate localDate) {
        final ZonedDateTime zonedDateTime = ZonedDateTime
                .of(LocalDateTime.of(localDate, LocalTime.MIDNIGHT), ZoneId.of("UTC"));
        return zonedDateTime.format(DateTimeFormatter.ISO_INSTANT);
    }

    private BigDecimal calculateInterest(final BigDecimal nominalRate) {
        final BigDecimal interestInCents = (nominalRate.multiply(Constants.DAYS_PER_MONTH)
                .multiply(this.initialOutstandingPrincipal))
                .divide(Constants.DAYS_PER_YEAR, BigDecimal.ROUND_HALF_UP);
        return interestInCents.movePointLeft(Constants.DECIMALS);
    }

    private BigDecimal calculatePrincipal(final BigDecimal annuity) {
        final BigDecimal principal = annuity.subtract(this.interest);

        if (principal.compareTo(this.initialOutstandingPrincipal) > 0) {
            return this.initialOutstandingPrincipal;
        } else {
            return principal;
        }
    }

    public BigDecimal getBorrowerPaymentAmount() {
        return this.borrowerPaymentAmount;
    }

    public String getDate() {
        return this.date;
    }

    public BigDecimal getInitialOutstandingPrincipal() {
        return this.initialOutstandingPrincipal;
    }

    public BigDecimal getInterest() {
        return this.interest;
    }

    public BigDecimal getPrincipal() {
        return this.principal;
    }

    public BigDecimal getRemainingOutstandingPrincipal() {
        return this.remainingOutstandingPrincipal;
    }
}
