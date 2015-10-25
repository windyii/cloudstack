# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

%define _pythonparamiko Requires: python-paramiko
%define _javaversion java-1.7.0-openjdk
%define _tomcatversion tomcat => 7.0
%define _vlanconfigtool vconfig
%define _tomcatpathname tomcat
%define _managementstartscriptpath %{_initrddir}
%define _managementservice %{nil}
%define _managementserviceattribute %{nil}
%define _iptablesservice %{nil}
%define _serverxmlname server7
%define _cloudstackmanagementconf %{nil}
%define _cloudstackmanagementconfattr %{nil}
