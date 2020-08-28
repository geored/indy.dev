FROM maven:3.6.2-jdk-8
WORKDIR /opt/indy
RUN java -version
COPY  . .
RUN mvn clean -DskipTests=true install
#RUN mvn clean -Prun-its install
# RUN tar -xvf ./deployments/launcher/target/indy-launcher-1.9.6-SNAPSHOT-complete.tar.gz 
# RUN ls -la
# RUN cd ./indy/bin && chmod +x indy.sh
# ENTRYPOINT ['/bin/bash','./indy.sh']


FROM openjdk:8
WORKDIR /opt/indy
ENV INDY_VER 2.5.0
ENV INDY_PORT 8080
EXPOSE ${INDY_PORT} 8081 8000	

# From indy's Dockerfile 
# USER root
# RUN chmod +x /usr/local/bin/*
# RUN	yum -y update
# RUN	yum -y install wget git tar which curl tree java-1.8.0-openjdk-devel
# RUN	yum clean all
# RUN	groupadd -g 1000 1001
# RUN	useradd -u 1000 -g 1001 --no-create-home -s /bin/false -d /opt/indy indy

COPY --from=0 /opt/indy/deployments/launcher/target/indy-launcher-${INDY_VER}-SNAPSHOT-complete.tar.gz .
RUN tar -xvf ./indy-launcher-${INDY_VER}-SNAPSHOT-complete.tar.gz

# From indy's Dockerfile
RUN mkdir -p /indy/storage /indy/data /indy/logs /indy/etc /indy/ssh /opt/indy-git-etc /opt/indy/indy/var/log/indy /opt/indy/indy/data/scheduler
# RUN chown -R indy:indy /indy/storage /indy/data /indy/logs /indy/etc /indy/ssh /opt/indy /opt/indy-git-etc /opt/indy/indy/var/log/indy /opt/indy/indy/data/scheduler
VOLUME /indy/storage /indy/data /indy/logs /indy/etc
# RUN chmod 755 /usr/local/bin/*

RUN chgrp -R 0 /indy/storage /indy/data /indy/logs /indy/etc /indy/ssh /opt/indy /opt/indy-git-etc /opt/indy/indy/var/log/indy /opt/indy/indy/data/scheduler && \
    chmod -R g+rwX /indy/storage /indy/data /indy/logs /indy/etc /indy/ssh /opt/indy /opt/indy-git-etc /opt/indy/indy/var/log/indy /opt/indy/indy/data/scheduler

#USER 1001

#RUN echo 'START' >> /opt/indy/indy/var/log/indy/indy.log \
#		echo 'START' >> /opt/indy/indy/var/log/indy/indy-content-delete.log \
#		echo 'START' >> /opt/indy/indy/var/log/indy/indy-inbound.log \
#		ls -la /opt/indy/indy/var/log/indy/ \
#		cat /opt/indy/indy/var/log/indy/indy.log \
#		cat /opt/indy/indy/var/log/indy/indy-content-delete.log \
#		cat /opt/indy/indy/var/log/indy/indy-inbound.log


ENTRYPOINT ["./indy/bin/indy.sh"]
