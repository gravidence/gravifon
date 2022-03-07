# Gravifon

An audio player with focus on advanced music management.

## Build distribution

OS specific distribution is supported by [compose-jb framework](https://github.com/JetBrains/compose-jb/tree/master/tutorials/Native_distributions_and_local_execution) out of the box.
However, there is no cross-compilation support available at the moment, so the formats can only be built using the specific OS. But with [Vagrant](https://www.vagrantup.com/) that's not a big problem.

### Java

First option is to run Gravifon as regular Java application. Let's create a single jar file (aka _uber jar_ or _fat jar_), containing all dependencies for particular OS.

Linux:

```
vagrant up linuxJava
vagrant destroy linuxJava
```

Windows:

```
vagrant up windowsJava
vagrant destroy windowsJava
```

Target artefact will be stored in _`./gravifon/build/compose/jars/gravifon-<platform>-x64-<version>.jar`_ (and copied to _`./distrib`_ for convenience). It could be run by doule-click (if file type registered in target OS), or using command line:

```
java -jar gravifon-<platform>-x64-<version>.jar
```

## Runtime dependencies

### Gstreamer

Gravifon uses [Gstreamer](https://gstreamer.freedesktop.org/) as it's audio backend. There's a Java-to-native bridge under the hood, which expects Gstreamer binaries are [installed and registered](https://gstreamer.freedesktop.org/documentation/installing/index.html) on target platform.

## Licenses

* Ice Cream icon set by https://icons8.com
