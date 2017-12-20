# Crow - Cron Web Scheduler

Crow is a cron web scheduler for container environments.

Realized in Spring Boot.

This is still work in progress!

Still working on:

* REST interface.
* Mailing

Crow is a cron web scheduler for container environments.

Why Crow?

* Easy configurable and customizable.
* You can specify the executing user.
* You can specify environment variables.
* Dedicated logging to console.
* Low profile cron scheduler.

# Project Structure

Crow is separated into two modules:

1. application module - The Spring Boot application
1. console module - The chron engine implementation

# Compiling The Project

This project uses maven package and build management system.

Building The Project:

Maven:

~~~~
$ mvn clean install
~~~~

> Maven installation required.

Docker:

~~~~
$ docker run --rm \
    -v $(pwd):/crow \
    blacklabelops/swarm-jdk8 \
    mvn -f /crow/pom.xml clean install
~~~~

> Running build inside Docker container, container will be removed after build.

Running The Project:

Maven:

~~~~
$ mvn spring-boot:run
~~~~

Docker:

~~~~
$ docker run --rm \
    -v $(pwd):/crow \
    blacklabelops/swarm-jdk8 \
    mvn -f /crow/pom.xml spring-boot:run
~~~~

# Running The jar

You need at least Java 8 to run this project!

Java startup command:

~~~~
$ java -jar crow-application.jar
~~~~

Running the jar inside Docker:

~~~~
$ docker run --rm \
    -v $(pwd):/crow \
    blacklabelops/java \
    java -jar crow-application.jar
~~~~

# The Configuration File

The configuration file must be placed inside the same directory as the jar.

## Job Configuration

You can define an arbitrary number of jobs, each job consists of:

* `name`: Must be a global unique name. Two jobs may never have the same name.
* `cron`: A cron definition. Supports full cron language elements, see [Wikipedia-Cron](https://en.wikipedia.org/wiki/Cron)
* `command`: The command to be executed according to cron schedule.

Minimum example configuration file containing one job:

~~~~
---

crow:
  jobs:
    - name: HelloWorld
      cron: "* * * * *"
      command: echo 'Hello World!'
~~~~

> One job named `HelloWorld` printing `Hello World!` each minute.

## Environment variables

You can define an arbitrary amount of environment variables for each job.

Each environment variable consists of:

* `key`: The name of the environment variable.
* `value`: The value of the environment variable.

Example:

~~~~
--

crow:
  jobs:
    - name: "HelloUniverse"
      cron: "* * * * *"
      command: /bin/bash -c "echo $MY_KEY"
      environments:
        MY_KEY: Hello Universe!
        MY_SECOND_KEY: My Second Value
~~~~

> Will print `Hello Universe!` each minute on console.

## Parallel Job Execution

By default, all jobs with the same name run purely sequential, overlapping jobs will be dropped as long as the previous job is executed. 
Jobs can also run in parallel.

Each job can be configured with `execution` to run sequential or parallel:

*  `execution`: Possible values `sequential` or `parallel`. Default is `sequential`

Example:

~~~~
--

crow:
  jobs:
    - name: "HelloParallelJob"
      cron: "* * * * *"
      command: /bin/bash -c "echo Hello"
      execution: parallel
~~~~

> Multiple instances of job `HelloParallelJob` can run parallel.

## Job Error Mode

By default, all jobs will remain running even is several instances broke down with an error.

Each job can be configured with `errorMode` to run at all when an error occured.

* `errorMode`: Possible values `stop`, `continue`. Default is `continue`

Example:

~~~~
--

crow:
  jobs:
    - name: "HelloParallelJob"
      cron: "* * * * *"
      command: /bin/bash -c "echo Hello"
      errorMode: stop
~~~~

> Job will not be run if an error has occured.


