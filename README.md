# telnetclient

### What is this repository for? ###
Repository to backup a device information into application

### How to import project for editing ###

* Import as maven project in your IDE

### Build, install and run application ###

To get started build the build the latest sources with Maven 3 and Java 8 
(or higher). 

	$ cd telnetclient
	$ mvn clean install 

You can run this application as spring-boot app by following command:

	$ mvn spring-boot:run

Once done you can run the application by executing 

	$ java -jar target/telnetclient-x.x.x-SNAPSHOT-exec.jar

## Application api's documentation ##

### /telnetclient/backup

api to collect backup object for specified ip.

	Method: GET
	Params:
		cls * Adapter class name either as "ZipTie::Adapters::Cisco::IOS" or as "ZipTieAdaptersCiscoIOS"
		ip * device ip i.e. 0.0.0.0
		port device port
		user device login username
		password device login password
		prompt device active prompt after login
	Response:
		Backup json as string

### Sample api request

	http://localhost:8099/telnetclient/backup?cls=ZipTieAdaptersCiscoIOS&ip=127.0.0.1
