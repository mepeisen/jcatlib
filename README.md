# jcatlib
This is a port of some features from c++ library libcat.

The original can be found [at google code](https://code.google.com/archive/p/libcatid/source)

# current version
release: NONE

snapshot: 0.0.1-SNAPSHOT

# maven identification
group-id: eu.xworlds.jcatlib

artifact-id: jcatlib

# use/download
configure your maven with following repository (f.e. inside settings.xml):

    <profile>
      <id>xworlds</id>
      <activation>
        <activeByDefault>true</activeByDefault>
      </activation>
      <repositories>
        <repository>
          <id>xworlds-releases</id>
          <name>Repository for xworlds releases</name>
          <url>http://nexus.xworlds.eu/nexus/content/repositories/releases</url>
          <layout>default</layout>
          <releases>
            <enabled>true</enabled>
            <updatePolicy>never</updatePolicy>
            <checksumPolicy>warn</checksumPolicy>
          </releases>
          <snapshots>
            <enabled>false</enabled>
          </snapshots>
        </repository>
        <repository>
          <id>xworlds-snapshots</id>
          <name>Repository for xworlds snapshots</name>
          <url>http://nexus.xworlds.eu/nexus/content/repositories/snapshots</url>
          <layout>default</layout>
          <releases>
            <enabled>false</enabled>
          </releases>
          <snapshots>
            <enabled>true</enabled>
            <updatePolicy>always</updatePolicy>
            <checksumPolicy>warn</checksumPolicy>
          </snapshots>
        </repository>
      </repositories>
    </profile>
