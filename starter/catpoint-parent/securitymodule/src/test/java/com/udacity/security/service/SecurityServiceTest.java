package com.udacity.security.service;

import com.udacity.image.service.FakeImageService;
import com.udacity.security.data.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.Charset;
import java.util.Random;
import java.util.stream.Stream;


@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private FakeImageService fakeImageService;
    @Mock
    private SecurityRepository securityRepository;
    private SecurityService securityService;
    private Sensor sensor;

    private String getRandomString() {
        byte[] array = new byte[7]; // length is bounded by 7
        new Random().nextBytes(array);
        String generatedString = new String(array, Charset.forName("UTF-8"));
        return generatedString;
    }

    private Sensor getNewSensor(String name, SensorType sensorType) {
        return new Sensor(name, sensorType);
    }

    @BeforeEach
    void init()
    {
        this.securityService = new SecurityService(securityRepository, fakeImageService);
    }


    /*
    Test Case: If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
     */
    @ParameterizedTest(name = "SensorType = {0} | ArmingStatus = {1}")
    @DisplayName("Alarm status changes to pending when sensor is activated")
    @MethodSource("armedAlarm_sensorActivated_putPendingAlarmStatus_argumentsProvider")
    void armedAlarm_sensorActivated_putPendingAlarmStatus(SensorType sensorType, ArmingStatus armingStatus)
    {
        this.sensor = getNewSensor(getRandomString(), sensorType);
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.addSensor(this.sensor);
        securityService.changeSensorActivationStatus(this.sensor, true);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    private static Stream<Arguments> armedAlarm_sensorActivated_putPendingAlarmStatus_argumentsProvider() {
        return Stream.of(
                Arguments.of(SensorType.DOOR, ArmingStatus.ARMED_HOME),
                Arguments.of(SensorType.DOOR, ArmingStatus.ARMED_AWAY),
                Arguments.of(SensorType.MOTION, ArmingStatus.ARMED_HOME),
                Arguments.of(SensorType.MOTION, ArmingStatus.ARMED_AWAY),
                Arguments.of(SensorType.WINDOW, ArmingStatus.ARMED_HOME),
                Arguments.of(SensorType.WINDOW, ArmingStatus.ARMED_AWAY)
        );
    }
}
