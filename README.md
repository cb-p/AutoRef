# AutoRef
This repository contains the source code for the automatic referee for RoboCup SSL tournaments built for the RoboTeam Twente.

## Usage
Before one can actually run the autoRef, they will have to set-up [RoboTeam World](https://github.com/RoboTeamTwente/roboteam/tree/main/roboteam_world). 
This software package is responsible for processing the raw data provided by [SSL Vision](https://github.com/RoboCup-SSL/ssl-vision). 

As the project is managed and built using Gradle, any way of using Gradle should work with this project, such as the Gradle integration
of your IDE. Explained below is the basic usage guidelines that will require no extra installation steps apart from Java itself.

### Windows
Make sure you have some distribution of at least Java 17 installed on your machine.

Next, you can run the referee using Gradle from the command prompt:
```
gradlew run
```

### Linux
Make sure you have some distribution of at least Java 17 installed on your machine. On Ubuntu, you can install OpenJDK JRE 17 using the following command:
```bash
sudo apt install openjdk-17-jre -y
```

Next, make sure the `gradlew` file is executable:
```
chmod +x ./gradlew
```

Finally, you can use Gradle to run the application:
```
./gradlew run
```

### Program Arguments
The program accepts a few program arguments:
```
--world-ip=...         the IP on which the application tries to connect to the RoboTeam World Observer to [default = 127.0.0.1]
--world-port=...       the port on which the application tries to connect to the RoboTeam World Observer to [default = 5558]
--gc-ip=...            the IP on which the application tries to connect to the Game Controller [default = 127.0.0.1]
--gc-port=...          the port on which the application tries to connect to the Game Controller [default = 10007]
--division=[A|B]       the division of the match that should be set when the application starts [default = B]
--active               if the application should start in active mode instead of passive [default = passive]
```

When running using gradle, these arguments can be specified in the following way:
```bash
./gradlew run --args="--active --division=A"
```
