/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.whirr.service.hadoop;

import static org.apache.whirr.RolePredicates.role;

import java.io.IOException;
import java.net.InetAddress;

import org.apache.whirr.Cluster;
import org.apache.whirr.Cluster.Instance;
import org.apache.whirr.ClusterSpec;
import org.apache.whirr.service.ClusterActionEvent;
import org.apache.whirr.service.FirewallManager.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HadoopJobTrackerClusterActionHandler extends HadoopNameNodeClusterActionHandler {

  private static final Logger LOG =
      LoggerFactory.getLogger(HadoopJobTrackerClusterActionHandler.class);
    
  public static final String ROLE = "hadoop-jobtracker";
  
  @Override
  public String getRole() {
    return ROLE;
  }
  
  @Override
  protected void doBeforeConfigure(ClusterActionEvent event) throws IOException {
    Cluster cluster = event.getCluster();
    
    Instance jobtracker = cluster.getInstanceMatching(role(ROLE));
    event.getFirewallManager().addRules(
        Rule.create()
          .destination(jobtracker)
          .ports(HadoopCluster.JOBTRACKER_WEB_UI_PORT),
        Rule.create()
          .source(HadoopCluster.getNamenodePublicAddress(cluster).getHostAddress())
          .destination(jobtracker)
          .ports(HadoopCluster.JOBTRACKER_PORT)
    );
    
  }
  
  @Override
  protected void afterConfigure(ClusterActionEvent event) throws IOException {
    ClusterSpec clusterSpec = event.getClusterSpec();
    Cluster cluster = event.getCluster();
    
    LOG.info("Completed configuration of {} role {}", clusterSpec.getClusterName(), getRole());

    InetAddress jobtrackerPublicAddress = HadoopCluster.getJobTrackerPublicAddress(cluster);

    LOG.info("Jobtracker web UI available at http://{}:{}",
      jobtrackerPublicAddress.getHostName(), HadoopCluster.JOBTRACKER_WEB_UI_PORT);

  }
  
}
