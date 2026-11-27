package com.energymix.chargingwindow;

public class NotEnoughForecastDataException extends RuntimeException {

    public NotEnoughForecastDataException(int hours) {
        super("Not enough forecast data to find a continuous " + hours + "-hour charging window. Please try again later.");
    }
}
