package com.sequenceiq.periscope.rest.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.sequenceiq.periscope.domain.Alarm;
import com.sequenceiq.periscope.rest.converter.AlarmConverter;
import com.sequenceiq.periscope.rest.json.AlarmJson;
import com.sequenceiq.periscope.rest.json.AlarmsJson;
import com.sequenceiq.periscope.service.AlarmService;
import com.sequenceiq.periscope.service.ClusterNotFoundException;

@RestController
@RequestMapping("/clusters/{clusterId}/alarms")
public class AlarmController {

    @Autowired
    private AlarmService alarmService;
    @Autowired
    private AlarmConverter alarmConverter;

    @RequestMapping(method = RequestMethod.POST)
    public ResponseEntity<AlarmsJson> createAlarms(@PathVariable long clusterId, @RequestBody AlarmsJson json)
            throws ClusterNotFoundException {
        List<Alarm> alarms = alarmConverter.convertAllFromJson(json.getAlarms());
        return createAlarmsResponse(alarmService.setAlarms(clusterId, alarms), HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.PUT)
    public ResponseEntity<AlarmsJson> addAlarm(@PathVariable long clusterId, @RequestBody AlarmJson json)
            throws ClusterNotFoundException {
        Alarm alarm = alarmConverter.convert(json);
        return createAlarmsResponse(alarmService.addAlarm(clusterId, alarm), HttpStatus.CREATED);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ResponseEntity<AlarmsJson> getAlarms(@PathVariable long clusterId) throws ClusterNotFoundException {
        return createAlarmsResponse(alarmService.getAlarms(clusterId));
    }

    @RequestMapping(value = "/{alarmId}", method = RequestMethod.DELETE)
    public ResponseEntity<AlarmsJson> deleteAlarm(@PathVariable long clusterId, @PathVariable long alarmId)
            throws ClusterNotFoundException {
        return createAlarmsResponse(alarmService.deleteAlarm(clusterId, alarmId));
    }

    private ResponseEntity<AlarmsJson> createAlarmsResponse(List<Alarm> alarms) {
        return createAlarmsResponse(alarms, HttpStatus.OK);
    }

    private ResponseEntity<AlarmsJson> createAlarmsResponse(List<Alarm> alarms, HttpStatus status) {
        List<AlarmJson> alarmResponse = alarmConverter.convertAllToJson(alarms);
        return new ResponseEntity<>(new AlarmsJson(alarmResponse), status);
    }
}
