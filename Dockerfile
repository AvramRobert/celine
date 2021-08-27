FROM openjdk:11

USER root

RUN apt-get update
RUN yes | apt-get install ffmpeg
RUN yes | apt-get install curl

RUN curl -L https://yt-dl.org/downloads/latest/youtube-dl -o /usr/local/bin/youtube-dl
RUN chmod a+rx /usr/local/bin/youtube-dl

USER ${uid}:${gid}

COPY ./target/scala-3.0.0/orchestra-assembly-0.1.0.jar /orchestra.jar

CMD ["java", "-jar", "/orchestra.jar"]

