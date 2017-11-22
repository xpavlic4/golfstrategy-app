FROM openjdk:latest
MAINTAINER John Smith <john.smith@example.com>
EXPOSE 9000
ADD files /
WORKDIR /opt/docker
RUN ["chown", "-R", "daemon", "."]
RUN ["chmod", "+x", "bin/test-act-docker"]
USER daemon
  ENTRYPOINT ["bin/test-act-docker", "-mem", "512", "-J-server"]
CMD []
