FROM amazoncorretto:17-alpine3.20

ENV POWER_LOG=/logs/

RUN mkdir /powerms
VOLUME /powerms

ADD powerms/prod.yml /powerms/prod.yml
ADD target/power-ms.jar /opt/power-ms.jar

CMD java -jar /opt/power-ms.jar

EXPOSE 9090 9091
