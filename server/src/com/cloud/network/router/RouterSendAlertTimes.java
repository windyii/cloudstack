// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
package com.cloud.network.router;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class RouterSendAlertTimes {

    private String instanceName;

    private Map<String, Integer> mapAlertTimes;

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

    public String getInstanceName() {
        return instanceName;
    }

    public void setInstanceName(String instanceName) {
        this.instanceName = instanceName;
    }

    public Map<String, Integer> getMapAlertTimes() {
        return mapAlertTimes;
    }

    public void setMapAlertTimes(Map<String, Integer> mapAlertTimes) {
        this.mapAlertTimes = mapAlertTimes;
    }

    public RouterSendAlertTimes(String instanceName) {
        this.instanceName = instanceName;
        this.mapAlertTimes = new HashMap<String, Integer>();
    }

    public int getAlertTimes() {
        synchronized (this) {
            int time = 0;
            Date date = new Date();
            Integer count = mapAlertTimes.get(sdf.format(date));
            if (count != null) time = count;
            return time;
        }
    }

    public void setAlertTimes() {
        synchronized (this) {
            int times = getAlertTimes();
            Date date = new Date();
            mapAlertTimes.put(sdf.format(date), ++times);
        }
    }
}
