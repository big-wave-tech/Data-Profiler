#
# Copyright 2021 Merck & Co., Inc. Kenilworth, NJ, USA.
#
#	Licensed to the Apache Software Foundation (ASF) under one
#	or more contributor license agreements. See the NOTICE file
#	distributed with this work for additional information
#	regarding copyright ownership. The ASF licenses this file
#	to you under the Apache License, Version 2.0 (the
#	"License"); you may not use this file except in compliance
#	with the License. You may obtain a copy of the License at
#
#	http://www.apache.org/licenses/LICENSE-2.0
#
#
#	Unless required by applicable law or agreed to in writing,
#	software distributed under the License is distributed on an
#	"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#	KIND, either express or implied. See the License for the
#	specific language governing permissions and limitations
#	under the License.
#
FROM ghcr.io/big-wave-tech/data-profiler/playframework_base:latest AS builder

# Get Deps (don't bring in the app folder to help with Docker Cache)
WORKDIR /opt/api/app
WORKDIR /opt/api
ADD blank_file ./conf/application.conf
ADD blank_file ./conf/logback.xml
ADD blank_file ./conf/routes
ADD blank_file ./conf/swagger.yml
ADD project/build.properties ./project/build.properties
ADD project/plugins.sbt ./project/plugins.sbt

RUN sbt update

# Add data_profiler core jars
ADD data_profiler_core_jars /opt/api/data_profiler_core_jars
# install file plugin version 2.5.2+ required, will use jar metadata to generate groupId, etc
RUN for filename in /opt/api/data_profiler_core_jars/*.jar; do \
      mvn org.apache.maven.plugins:maven-install-plugin:2.5.2:install-file -Dfile="$filename" || continue; done

# Build everything
ADD build.sbt /opt/api/
ADD . /opt/api
RUN sbt clean stage

# Put the launchers in place
ADD dataprofiler-api-launcher-test.sh /opt/api/target/universal/stage/bin/dataprofiler-api-launcher-test.sh
ADD dataprofiler-api-launcher-minikube.sh /opt/api/target/universal/stage/bin/dataprofiler-api-launcher-minikube.sh
ADD dataprofiler-api-launcher.sh /opt/api/target/universal/stage/bin/dataprofiler-api-launcher.sh

ADD set-env.sh /opt/api/target/universal/stage/bin/set-env.sh

# Copy to runner
FROM ghcr.io/big-wave-tech/data-profiler/java
COPY --from=builder /opt/api/target/universal /opt/api/target/universal
EXPOSE 9000
WORKDIR /opt/api/target/universal/stage
CMD ["./bin/dataprofiler-api-launcher.sh"]
