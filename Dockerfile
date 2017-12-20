FROM openjdk:9

# install maven
ENV MAVEN_VERSION=3.3.9
ENV M2_HOME=/usr/local/maven
RUN wget --directory-prefix=/tmp \
    http://mirror.synyx.de/apache/maven/maven-3/${MAVEN_VERSION}/binaries/apache-maven-${MAVEN_VERSION}-bin.tar.gz && \
    tar xzf /tmp/apache-maven-${MAVEN_VERSION}-bin.tar.gz -C /usr/local && rm -rf /tmp/* && \
    cd /usr/local &&  ln -s apache-maven-${MAVEN_VERSION} maven

ENV PATH=$PATH:/usr/local/maven/bin
