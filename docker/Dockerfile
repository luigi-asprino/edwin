FROM maven:3.6.3-jdk-8

WORKDIR /opt

COPY install.sh /opt
COPY edwin.sh /opt

RUN sh /opt/install.sh

CMD ["/bin/bash","/opt/edwin.sh"]