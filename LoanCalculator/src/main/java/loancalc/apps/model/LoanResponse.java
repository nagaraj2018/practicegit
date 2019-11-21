package loancalc.apps.model;

import java.io.Serializable;
import java.math.BigDecimal;

import loancalc.apps.domain.LoanInstallmentPerMonth;

public class LoanResponse  implements Serializable {
    @Override
	public String toString() {
		return "LoanResponse [borrowerPaymentAmount=" + borrowerPaymentAmount + ", date=" + date
				+ ", initialOutstandingPrincipal=" + initialOutstandingPrincipal + ", interest=" + interest
				+ ", principal=" + principal + ", remainingOutstandingPrincipal=" + remainingOutstandingPrincipal + "]";
	}

	public LoanResponse() {
		super();
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;
    private BigDecimal borrowerPaymentAmount;
    private String date;
    private BigDecimal initialOutstandingPrincipal;
    private BigDecimal interest;
    private BigDecimal principal;
    private BigDecimal remainingOutstandingPrincipal;

    public LoanResponse(final LoanInstallmentPerMonth paymentPerMonth) {
        this.borrowerPaymentAmount = paymentPerMonth.getBorrowerPaymentAmount();
        this.date = paymentPerMonth.getDate();
        this.initialOutstandingPrincipal = paymentPerMonth.getInitialOutstandingPrincipal();
        this.interest = paymentPerMonth.getInterest();
        this.principal = paymentPerMonth.getPrincipal();
        this.remainingOutstandingPrincipal = paymentPerMonth.getRemainingOutstandingPrincipal();
    }

    public BigDecimal getBorrowerPaymentAmount() {
        return borrowerPaymentAmount;
    }

    public String getDate() {
        return date;
    }

    public BigDecimal getInitialOutstandingPrincipal() {
        return initialOutstandingPrincipal;
    }

    public BigDecimal getInterest() {
        return interest;
    }

    public BigDecimal getPrincipal() {
        return principal;
    }

    public BigDecimal getRemainingOutstandingPrincipal() {
        return remainingOutstandingPrincipal;
    }
}
