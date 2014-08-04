package com.sequenceiq.periscope.policies.cloudbreak.rule;

import org.apache.hadoop.yarn.server.resourcemanager.webapp.dao.ClusterMetricsInfo;

import com.sequenceiq.periscope.policies.NamedRule;

public interface ClusterAdjustmentRule extends NamedRule {

    /**
     * Scale up or down to the required number of nodes.
     *
     * @param clusterInfo
     * @return
     */
    int scale(ClusterMetricsInfo clusterInfo);

    /**
     * Return the maximum/minimum number of nodes. Depends whether its scaling up or down.
     */
    int getLimit();
}