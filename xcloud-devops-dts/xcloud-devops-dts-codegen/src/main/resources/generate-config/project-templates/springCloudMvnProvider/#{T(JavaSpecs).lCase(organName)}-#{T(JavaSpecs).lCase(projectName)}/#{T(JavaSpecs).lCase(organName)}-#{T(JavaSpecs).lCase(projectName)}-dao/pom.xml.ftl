<!-- ${watermark} -->
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<artifactId>${organName?uncap_first}-${projectName?uncap_first}-dao</artifactId>
	<packaging>jar</packaging>
	<name>${organName?cap_first} ${projectName?cap_first} DAO</name>
	<url>http://${projectName?uncap_first}.${organName?uncap_first}.${organType}</url>
	<parent>
		<groupId>${organType?uncap_first}.${organName?uncap_first}</groupId>
		<artifactId>${organName?uncap_first}-${projectName?uncap_first}</artifactId>
		<version>${version?uncap_first}</version>
	</parent>
	<properties>
		<java.version>1.8</java.version>
	</properties>
	<dependencyManagement>
		<dependencies>
			<dependency>
				<groupId>com.wl4g</groupId>
				<artifactId>xcloud-bom</artifactId>
				<version>master</version>
				<type>pom</type>
				<scope>import</scope>
			</dependency>
		</dependencies>
	</dependencyManagement>
	<dependencies>
		<dependency>
			<groupId>com.wl4g</groupId>
			<artifactId>xcloud-components-data</artifactId>
		</dependency>
		<dependency>
			<groupId>${organType?uncap_first}.${organName?uncap_first}</groupId>
			<artifactId>${organName?uncap_first}-${projectName?uncap_first}-common</artifactId>
			<version>${version?uncap_first}</version>
		</dependency>
	</dependencies>
	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<source>${java.version}</source>
					<target>${java.version}</target>
				</configuration>
			</plugin>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-source-plugin</artifactId>
				<executions>
					<execution>
						<id>attach-sources</id>
						<phase>verify</phase>
						<goals>
							<goal>jar-no-fork</goal>
						</goals>
					</execution>
				</executions>
			</plugin>
		</plugins>
	</build>
</project>