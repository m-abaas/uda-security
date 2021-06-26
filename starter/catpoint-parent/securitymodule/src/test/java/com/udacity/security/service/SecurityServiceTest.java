package com.udacity.security.service;

import com.udacity.image.service.FakeImageService;
import com.udacity.security.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;


@ExtendWith(MockitoExtension.class)
class SecurityServiceTest {

    @Mock
    private FakeImageService fakeImageService;
    @Mock
    private PretendDatabaseSecurityRepositoryImpl securityRepository;
    private SecurityService securityService;

    private String getRandomString() {
        byte[] array = new byte[7];
        new Random().nextBytes(array);
        return new String(array, StandardCharsets.UTF_8);
    }

    private Sensor getNewSensor(String name, SensorType sensorType) {
        return new Sensor(name, sensorType);
    }
    private BufferedImage getFakeBufferedImage() { return null; }

    @BeforeEach
    void init()
    {
        this.securityService = new SecurityService(securityRepository, fakeImageService);
    }

    /*
    Test Case 1.a: If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
    */
    @ParameterizedTest(name = "SensorType = {0} | ArmingStatus = {1}")
    @DisplayName("Alarm status changes to pending when: - Alarm status: NoALARM | - Sensor: ACTIVE | - Arming status: ARMED")
    @MethodSource("putAlarmStatusToPending_if_AlarmStatusNoAlarm_SensorActivated_ArmingActive_argumentsProvider")
    void putAlarmStatusToPending_if_AlarmStatusNoAlarm_SensorActivated_ArmingActive(SensorType sensorType, ArmingStatus armingStatus)
    {
        Sensor sensor = getNewSensor(getRandomString(), sensorType);
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.addSensor(sensor);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    /*
    Test Case 1.b: If system is inactive, and a sensor was activated, nothing should happen
    */
    @Test
    void nothingHappens_if_SensorActivated_ArmingInactive()
    {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        Sensor sensor = getNewSensor(getRandomString(), SensorType.DOOR);
        securityService.addSensor(sensor);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository, never()).setAlarmStatus(any());
    }


    private static Stream<Arguments> putAlarmStatusToPending_if_AlarmStatusNoAlarm_SensorActivated_ArmingActive_argumentsProvider() {
        return Stream.of(
                Arguments.of(SensorType.DOOR, ArmingStatus.ARMED_HOME),
                Arguments.of(SensorType.DOOR, ArmingStatus.ARMED_AWAY),
                Arguments.of(SensorType.MOTION, ArmingStatus.ARMED_HOME),
                Arguments.of(SensorType.MOTION, ArmingStatus.ARMED_AWAY),
                Arguments.of(SensorType.WINDOW, ArmingStatus.ARMED_HOME),
                Arguments.of(SensorType.WINDOW, ArmingStatus.ARMED_AWAY)
        );
    }


    /*
    Test Case 2: If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.
    */
    @ParameterizedTest(name = "SensorType = {0} | ArmingStatus = {1}")
    @DisplayName("Alarm status changes to ALARM when: - Alarm status: PendingAlarm | - Sensor: ACTIVE | - Arming status: ARMED")
    @MethodSource("putAlarmStatusToAlarm_if_AlarmStatusPending_SensorActivated_ArmingActive_argumentsProvider")
    void putAlarmStatusToAlarm_if_AlarmStatusPending_SensorActivated_ArmingActive(SensorType sensorType, ArmingStatus armingStatus)
    {
        Sensor sensor = getNewSensor(getRandomString(), sensorType);
        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.addSensor(sensor);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    private static Stream<Arguments> putAlarmStatusToAlarm_if_AlarmStatusPending_SensorActivated_ArmingActive_argumentsProvider() {
        return Stream.of(
                Arguments.of(SensorType.DOOR, ArmingStatus.ARMED_HOME),
                Arguments.of(SensorType.DOOR, ArmingStatus.ARMED_AWAY),
                Arguments.of(SensorType.MOTION, ArmingStatus.ARMED_HOME),
                Arguments.of(SensorType.MOTION, ArmingStatus.ARMED_AWAY),
                Arguments.of(SensorType.WINDOW, ArmingStatus.ARMED_HOME),
                Arguments.of(SensorType.WINDOW, ArmingStatus.ARMED_AWAY)
        );
    }

    /*
    Test Case 3: If pending alarm and all sensors are inactive, return to no alarm state.
    */
    @Test
    void putAlarmStatusToNoAlarm_if_AlarmStatusPending_AllSensorsInactive()
    {
        // Mocks behavior
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        // Generating a group of sensors
        Sensor doorSensor = getNewSensor(getRandomString(), SensorType.DOOR);
        Sensor motionSensor = getNewSensor(getRandomString(), SensorType.MOTION);
        Sensor windowSensor = getNewSensor(getRandomString(), SensorType.WINDOW);

        // Returning sensors
        when(securityRepository.getSensors()).thenReturn(Set.of(doorSensor, motionSensor, windowSensor));

        securityService.addSensor(doorSensor);
        securityService.addSensor(motionSensor);
        securityService.addSensor(windowSensor);

        // Activating
        securityService.changeSensorActivationStatus(doorSensor, true);
        securityService.changeSensorActivationStatus(motionSensor, true);
        securityService.changeSensorActivationStatus(windowSensor, true);

        // Then deactivating
        securityService.changeSensorActivationStatus(doorSensor, false);
        securityService.changeSensorActivationStatus(motionSensor, false);
        securityService.changeSensorActivationStatus(windowSensor, false);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    /*
    Test Case 4: If alarm is active, change in sensor state should not affect the alarm state.
    */
    @Test
    void alarmStatusUnchanged_if_alarmActive_SensorStateChanged()
    {
        // Mocks behavior
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        Sensor sensor = getNewSensor(getRandomString(), SensorType.DOOR);
        when(securityRepository.getSensors()).thenReturn(Set.of(sensor));
        securityService.addSensor(sensor);
        // First time should trigger the method
        securityService.changeSensorActivationStatus(sensor, true);
        // Second one shouldn't trigger method
        securityService.changeSensorActivationStatus(sensor, true);
        // Changing it to false shouldn't trigger method as well
        securityService.changeSensorActivationStatus(sensor, false);

        // This method should only be called once
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    /*
    Test Case 5: If a sensor is activated while already active and the system is in pending state, change it to alarm state.
    */
    @Test
    void putAlarmStatusToAlarm_if_sensorActivated_whileAlreadyActive()
    {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);

        Sensor sensor = getNewSensor(getRandomString(), SensorType.MOTION);
        securityService.addSensor(sensor);

        // Alarm status changes to ALARM
        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }


    /*
    Test Case 6: If a sensor is deactivated while already inactive, make no changes to the alarm state.
    */
    @Test
    void alarmStatusUnchanged_if_sensorDeactivateWhileBeingAlreadyDeactivated()
    {
        Sensor sensor = getNewSensor(getRandomString(), SensorType.DOOR);
        securityService.addSensor(sensor);

        // Alarm status isn't affected
        securityService.changeSensorActivationStatus(sensor, false);
        securityService.changeSensorActivationStatus(sensor, false);
        verify(securityRepository, never()).setAlarmStatus(any());
    }

    /*
    Test Case 7: If the image service identifies an image containing a cat while the system is armed-home, put the system into alarm status.
    */
    @Test
    void putAlarmStatusToAlarm_if_detectedCat()
    {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(fakeImageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        securityService.processImage(getFakeBufferedImage());

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    /*
    Test Case 8.a: If the image service identifies an image that does not contain a cat, change the status to no alarm as long as the sensors are not active.
    In this TC, we leave a sensor inactive.
    */
    @Test
    void putAlarmStatusToNoAlarm_if_noCatDetected_allSensorsNotActive()
    {
        Sensor sensor = getNewSensor(getRandomString(), SensorType.DOOR);
        sensor.setActive(false);
        securityService.addSensor(sensor);

        when(securityRepository.getSensors()).thenReturn(Set.of(sensor));
        when(fakeImageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        securityService.processImage(getFakeBufferedImage());

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    /*
    Test Case 8.b: If the image service identifies an image that does not contain a cat, change the status to no alarm as long as the sensors are not active.
    In this TC, we leave a sensor active.
    */
    @Test
    void noChangeForAlarmStatus_if_noCatDetected_sensorActive()
    {
        Sensor sensor = getNewSensor(getRandomString(), SensorType.DOOR);
        sensor.setActive(true);
        securityService.addSensor(sensor);

        when(securityRepository.getSensors()).thenReturn(Set.of(sensor));
        when(fakeImageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        securityService.processImage(getFakeBufferedImage());

        verify(securityRepository, never()).setAlarmStatus(any());
    }

    /*
    Test Case 9: If the image service identifies an image that does not contain a cat, change the status to no alarm as long as the sensors are not active.
    */
    @Test
    void putAlarmStatusToNoAlarm_if_systemIsDisarmed()
    {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    /*
    Test Case 10: If the system is armed, reset all sensors to inactive.
    */
    @Test
    void putAllSensorsToInactive_if_systemIsArmed()
    {
        Sensor sensor = getNewSensor(getRandomString(), SensorType.WINDOW);
        securityService.addSensor(sensor);
        when(securityRepository.getSensors()).thenReturn(Set.of(sensor));
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository, times(1)).updateSensor(sensor);
    }

    /*
    Test Case 11: If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
    */
    @Test
    void putAlarmStatusToAlarm_if_systemArmed_whileCatIsDetected()
    {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(fakeImageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        securityService.processImage(getFakeBufferedImage());

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);

    }
}
