FROM maven:3.8.2-jdk-11

# Add Jenkins as a user
RUN addgroup --gid 501 --system jenkins && adduser --system --group jenkins --uid 501 --shell /bin/bash --home /home/jenkins\
    && chown -R jenkins:jenkins /usr/local/ && chmod -R a+w /usr/local/

USER jenkins

RUN mkdir -p /home/jenkins/.m2
RUN chown -R jenkins:jenkins /home/jenkins/.m2
COPY ./settings.xml /home/jenkins/.m2/settings.xml
