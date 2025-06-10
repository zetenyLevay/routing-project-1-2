# Project Name

Brief description of what this project does and its main purpose.

## Table of Contents
- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Running the Project](#running-the-application)
- [Usage Examples](#usage-examples)
- [Troubleshooting](#troubleshooting)
- [Dependencies](#dependencies)


## Features

### GUI
- Can generate route from given 2 input coordinates and a time.
- Start and end point can either by typed in or clicked on map.
- Heatmap of journey times from any stop to all other stops. 
- Visualization of the effect on the network when one stop is closed.
- Stop evaluation heatmap.

### Console
- Ability to load .zip files into .db files using the load command described under [CLI Usage Examples](#cli-usage)
- Ability to route from a coordinate to coordinate using the route command described under [CLI Usage Examples](#cli-usage)
## Prerequisites

Before you begin, ensure you have met the following requirements:

- [Java JDK 23 or higher](https://www.oracle.com/java/technologies/downloads/#jdk-23) installed  
- [Apache Maven 3.9.10 or higher](https://maven.apache.org/download.cgi) installed  
- [Git](https://git-scm.com/downloads) (optional, for cloning the repository)

### Checking Prerequisites

```bash
# Check Java version
java -version

# Check Maven version
mvn -version
```

## Installation

### Clone the Repository

```bash
git clone https://gitlab.maastrichtuniversity.nl/bcs1600-2025/group20.git
cd group20
```

### Install Dependencies

```bash
mvn clean install
```


## Running the Project

### Running the routing

```bash
# Run directly with Maven
mvn exec:java
```

### Running the UI 

Run main class of ```MapUI.java``` at filepath ```src\main\java\gui\MapUI.java```

## Usage Examples





### GUI usage



### CLI Usage

Input and output to the command line are JSON objects. They have to be formatted as below else you will encounter an ```{"error":"Bad JSON input"}``` error. 

#### Loading Database

##### IN
```JSON
{
  "load": "data/sample_gtfs.zip"
}
```

##### OUT
```JSON
{
  "ok": "loaded"
}
```

#### Routing
##### IN

```JSON
{
  "routeFrom": {
    "lat": 1,          // starting latitude
    "lon": 1           // starting longitude
  },
  "to": {
    "lat": 2,          // destination latitude
    "lon": 2           // destination longitude
  },
  "startingAt": "08:00"  // departure time (HH:MM)
}
```

##### OUT
```JSON
{
  "ok": [
    {
      "mode": "walk",               // walking leg
      "to": {
        "lat": 1.5,
        "lon": 1.2
      },
      "duration": 5,               // minutes walking
      "startTime": "08:00"         // when to start this leg
    },
    {
      "mode": "ride",               // transit leg
      "route": {
        "operator": "CityBus",      // agency name
        "shortName": "X1",          // route short name
        "longName": "Express Line", // route long name
        "headSign": "Downtown"      // trip headsign
      },
      "stop": "Central Station",    // alighting stop
      "to": {
        "lat": 2,
        "lon": 2
      },
      "duration": 15,               // minutes on bus
      "startTime": "08:05"
    }
  ]
}
```



### Example

```> {"load":"data/budapest_gtfs.zip"}```\
```< {"ok":"loaded"}```\
```> {"routeFrom":{"lat":47.51828032904577,"lon":18.97828487843043},"to":{"lat":47.4924417,"lon":19.0527917},"startingAt":"18:54"} ```\
```< {"ok":[{"duration":2.683333333333333,"mode":"walk","startTime":"18:54","to":{"lat":47.517099,"lon":18.980692}},``` ```{"duration":21,"mode":"ride","route":{"headSign":"Nyugati pályaudvar M","longName":"","operator":"BKK","shortName":"291"},"startTime":"19:01","stop":"Jászai Mari tér (F00930)","to":{"lat":47.512478,"lon":19.049558}},``` ```{"duration":8,"mode":"ride","route":{"headSign":"Kőbánya alsó vasútáll.","longName":"","operator":"BKK","shortName":"9"},"startTime":"19:25","stop":"Szent István Bazilika (048505)","to":{"lat":47.500141,"lon":19.054665}},``` ```{"duration":10.416666666666666,"mode":"walk","startTime":"19:33","to":{"lat":47.4924417,"lon":19.0527917}}]}```



## Troubleshooting

### Common Issues

#### Issue 1: Maven Build Failure



## Dependencies

This project uses the following major dependencies:


- **SQLite JDBC** (`org.xerial:sqlite-jdbc:3.49.1.0`)
- **JavaFX Base** (`org.openjfx:javafx-base:21.0.2`)
- **OpenCSV** (`com.opencsv:opencsv:5.7.1`)
- **JXMapViewer2** (`org.jxmapviewer:jxmapviewer2:2.6`)
- **leastfixedpoint-json** (`com.leastfixedpoint:leastfixedpoint-json:1.2`)
- **Apache Commons CSV** (`org.apache.commons:commons-csv:1.10.0`)
- **Jackson Core** (`com.fasterxml.jackson.core:jackson-core:2.18.1`)
- **Jackson Databind** (`com.fasterxml.jackson.core:jackson-databind:2.18.1`)
- **Jackson Annotations** (`com.fasterxml.jackson.core:jackson-annotations:2.18.1`)
- **JUnit Jupiter** (`org.junit.jupiter:junit-jupiter`, scope: test)


See `pom.xml` for a complete list of dependencies.

