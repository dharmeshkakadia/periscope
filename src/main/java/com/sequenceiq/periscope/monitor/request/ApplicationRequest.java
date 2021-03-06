package com.sequenceiq.periscope.monitor.request;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.EnumSet;
import java.util.List;

import org.apache.hadoop.yarn.api.records.ApplicationReport;
import org.apache.hadoop.yarn.api.records.YarnApplicationState;
import org.apache.hadoop.yarn.exceptions.YarnException;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.SchedulerInfo;
import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.SchedulerTypeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestOperations;

import com.sequenceiq.periscope.domain.Cluster;
import com.sequenceiq.periscope.log.Logger;
import com.sequenceiq.periscope.log.PeriscopeLoggerFactory;
import com.sequenceiq.periscope.model.QueueAppUpdate;
import com.sequenceiq.periscope.monitor.event.ApplicationUpdateEvent;
import com.sequenceiq.periscope.monitor.event.UpdateFailedEvent;
import com.sequenceiq.periscope.service.configuration.ConfigParam;

@Component("ApplicationRequest")
@Scope("prototype")
public class ApplicationRequest extends AbstractEventPublisher implements EventPublisher {

    private static final Logger LOGGER = PeriscopeLoggerFactory.getLogger(ApplicationRequest.class);
    private static final String WS_URL = "/ws/v1/cluster/scheduler";
    private final Cluster cluster;

    @Autowired
    private RestOperations restOperations;

    public ApplicationRequest(Cluster cluster) {
        this.cluster = cluster;
    }

    @Override
    public void run() {
        try {
            List<ApplicationReport> applicationReport = getApplicationReport();
            SchedulerInfo schedulerInfo = getSchedulerInfo();
            QueueAppUpdate appUpdate = new QueueAppUpdate(applicationReport, schedulerInfo);
            publishEvent(new ApplicationUpdateEvent(cluster.getId(), appUpdate));
        } catch (Exception e) {
            LOGGER.error(cluster.getId(), "Error occurred during application update", e);
            publishEvent(new UpdateFailedEvent(cluster.getId()));
        }
    }

    private List<ApplicationReport> getApplicationReport() throws YarnException, IOException {
        // TODO should check for proper state to start with
        EnumSet<YarnApplicationState> states = EnumSet.of(YarnApplicationState.RUNNING);
        return cluster.getYarnClient().getApplications(states);
    }

    private SchedulerInfo getSchedulerInfo() throws NoSuchFieldException, IllegalAccessException {
        String rmAddress = cluster.getConfigValue(ConfigParam.YARN_RM_WEB_ADDRESS, "");
        String url = "http://" + rmAddress + WS_URL;
        SchedulerTypeInfo response = restOperations.getForObject(url, SchedulerTypeInfo.class);
        // TODO https://issues.apache.org/jira/browse/YARN-2280
        Field field = response.getClass().getDeclaredField("schedulerInfo");
        field.setAccessible(true);
        return (SchedulerInfo) field.get(response);
    }
}
