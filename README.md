# Log4J 2 JSON Layout Plugin

[![Build Status](https://travis-ci.org/lukasniemeier-zalando/log4j2-json-layout.svg?branch=master)](https://travis-ci.org/lukasniemeier-zalando/log4j2-json-layout)[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://raw.githubusercontent.com/lukasniemeier-zalando/log4j2-json-layout/master/LICENSE)

A **Log4J 2** layout plugin rendering structured JSON log lines. 
The goal of this plugin is to offer a minimal, concise and ready-to-be-used JSON log layout.

```
{"time":"2018-03-21T17:50:20.868Z","severity":"INFO","logger":"MyLogger","message":"Hello World","thread":"main"}
{"time":"2018-03-21T18:00:46.175Z","severity":"WARN","logger":"MyLogger","message":"Huch","thrown":{"class":"java.lang.IllegalStateException","stack":"..."},"thread":"main"}
```

This plugin **only** supports Log4J 2.7 and requires Jackson 2.8. This is a perfect match in case you are using Spring Boot 1. 

## Usage

Use this layout by configuring the `SimpleJsonLayout` it on your appender of choice. 
Make sure to expose the plugin to your Log4J instance (see [here](https://logging.apache.org/log4j/2.x/manual/plugins.html)).

```
<Configuration packages="koeln.niemeier.log4j2.json">
    <Appenders>
        <Console name="stdout">
            <SimpleJsonLayout/>
        </Console>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="stdout"/>
        </Root>
    </Loggers>
</Configuration>

```

## Logging of Throwable

In case a `Throwable` is logged an additional object `exception` is rendered with the following fields.

- `thrown`: the exception's class.
- `message`: the exception's message (optional).
- `stack`: the full stack trace serialized as an escaped string.

```
{
    "time": "2018-03-22T08:00:46.175Z"
    "logger": "logger",
    "severity": "WARN",
    "message": "Huch",
    "thread": "main",
    "thrown": {
        "class": "java.lang.IllegalStateException",
        "message": "test",
        "stack":"java.lang.IllegalStateException: test\n\tat koeln.niemeier.log4j2.json.LayoutTest.testThrown(LayoutTest.kt:119) ~[classes/:?]\n\tat sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method) ~[?:1.8.0_121]\n\tat sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62) ~[?:1.8.0_121]\n\tat sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43) ~[?:1.8.0_121]\n\tat java.lang.reflect.Method.invoke(Method.java:498) ~[?:1.8.0_121]\n\t... suppressed 46 lines\n\tat com.intellij.junit5.JUnit5IdeaTestRunner.startRunnerWithArgs(JUnit5IdeaTestRunner.java:65) [junit5-rt.jar:?]\n\tat com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:47) [junit-rt.jar:?]\n\tat com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:242) [junit-rt.jar:?]\n\tat com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70) [junit-rt.jar:?]\n",
    }
}
```

## Customization

Currently the following options are available:

- `ignoredStackTracePackages`: a comma-separated list of packages to be ignored on rendering the stack trace.

Example XML configuration fragment showing all possible options:

```
<SimpleJsonLayout ignoredStackTracePackages="org.junit.,java.util.,org.gradle."/>
```

## Alternatives

There are many alternatives which may fit your use case better.

- "native" [Log4J 2 JSON Layout](https://logging.apache.org/log4j/2.x/manual/layouts.html)
- custom Log4J **1** [log4j-json-layout](https://github.com/szhem/log4j-json-layout)
- custom Log4J **1** [log4j-jsonevent-layout](https://github.com/logstash/log4j-jsonevent-layout)
- and many more...

As I did not get them to work with Log4J 2 easily, I took inspiration from all of them.

## Building

This project uses Kotlin with Gradle.

```
    $ ./gradlew clean build
```
