package com.aus.prva_semestralka.objekty;

public class Threshold {

	public static final int BALANCE_FACTOR_THRESHOLD = 2;
	public static final int VELKOST_DATA_THRESHOLD = 10;

	public static boolean jePrekrocenyBalanceFactorThreshold(int value) {
		return  value > BALANCE_FACTOR_THRESHOLD;
	}

	public static boolean jePrekrocenyVelkostDataThreshold(int value) {
		return  value > VELKOST_DATA_THRESHOLD;
	}
}
