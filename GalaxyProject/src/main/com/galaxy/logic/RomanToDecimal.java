package main.com.galaxy.logic;

public class RomanToDecimal extends BusinessRules{


	public float romanToDecimal(java.lang.String romanNumber) {

		float decimal = 0;
		float lastNumber = 0;
		char[] romanNumeral = romanNumber.toUpperCase().toCharArray();

		for (int x = romanNumeral.length- 1; x >= 0 ; x--) {
			Character convertToDecimal = romanNumeral[x];

			switch (convertToDecimal) {
			case 'M':
				super.checkLiteralCountValidity(convertToDecimal);
				decimal = processDecimal(1000, lastNumber, decimal);
				lastNumber = 1000;
				break;

			case 'D':
				super.checkLiteralCountValidity(convertToDecimal);
				decimal = processDecimal(500, lastNumber, decimal);
				lastNumber = 500;
				break;

			case 'C':
				super.checkLiteralCountValidity(convertToDecimal);
				decimal = processDecimal(100, lastNumber, decimal);
				lastNumber = 100;
				break;

			case 'L':
				super.checkLiteralCountValidity(convertToDecimal);
				decimal = processDecimal(50, lastNumber, decimal);
				lastNumber = 50;
				break;

			case 'X':
				super.checkLiteralCountValidity(convertToDecimal);
				decimal = processDecimal(10, lastNumber, decimal);
				lastNumber = 10;
				break;

			case 'V':
				super.checkLiteralCountValidity(convertToDecimal);
				decimal = processDecimal(5, lastNumber, decimal);
				lastNumber = 5;
				break;

			case 'I':
				super.checkLiteralCountValidity(convertToDecimal);
				decimal = processDecimal(1, lastNumber, decimal);
				lastNumber = 1;
				break;
			}
		}
		super.resetLiteralsCounter();
		return decimal;
	}

	public float processDecimal(float decimal, float lastNumber, float lastDecimal) {
		if (lastNumber > decimal) {
			return super.subtractionLogic(lastDecimal, decimal, lastNumber);
		}
		else {
			return lastDecimal + decimal;
		}
	}
}
