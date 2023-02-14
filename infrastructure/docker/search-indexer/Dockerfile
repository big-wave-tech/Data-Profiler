FROM eclipse-temurin:11

WORKDIR /usr/src/app
COPY data_profiler_core_jars/dataprofiler-tools-0.1.0.jar .

ENTRYPOINT [ "java", "-cp", "dataprofiler-tools-0.1.0.jar", "com.dataprofiler.searchindex.SearchIndexCli"]
