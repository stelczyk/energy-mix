package com.energymix;

import com.energymix.chargingwindow.NotEnoughForecastDataException;
import com.energymix.reservation.ChargerNotFoundException;
import com.energymix.reservation.InvalidReservationException;
import com.energymix.reservation.ReservationConflictException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(RestClientException.class)
    ProblemDetail handleExternalApiFailure(RestClientException exception) {
        log.warn("Carbon Intensity API call failed", exception);
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE,
                "Energy data provider is temporarily unavailable. Please try again later.");
    }

    @ExceptionHandler(NotEnoughForecastDataException.class)
    ProblemDetail handleNotEnoughData(NotEnoughForecastDataException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage());
    }

    @ExceptionHandler(InvalidReservationException.class)
    ProblemDetail handleInvalidReservation(InvalidReservationException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    @ExceptionHandler(ChargerNotFoundException.class)
    ProblemDetail handleChargerNotFound(ChargerNotFoundException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }

    @ExceptionHandler(ReservationConflictException.class)
    ProblemDetail handleReservationConflict(ReservationConflictException exception) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }
}
