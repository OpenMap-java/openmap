
System Requirements:
- IIS Server
- .NET Framework
.NET Framework redistributable can be found at http://msdn.microsoft.com/downloads/default.asp?url=/downloads/sample.asp?url=/msdn-files/027/001/829/msdncompositedoc.xml
- Java Runtime Environment (JRE)
- OpenMap software


To run the OpenMap .NET Web Services Demo:

1. Copy ImageWebService and OpenMapWeb to a directory under IIS wwwroot.
You might have to configure IIS to create applications for ImageWebService and OpenMapWeb. 
To do this:
	- open Internet Information Services under the control panel.
	- access the properties for ImageWebService (and OpenMapWeb).
	- make sure the "Read" checkbox is checked.
	- under the 'Directory' tab press the 'Create' button.
	- choose "Scripts only" in the "Execute Permissions" section.

2. Edit <appSettings> in web.config in the ImageWebService directory to point to your OpenMap installation. 

3. Point your browser to http://<Web_home_directory>/OpenMapWeb/webmap.aspx

The web services are located at:
http://<Web_home_directory>/ImageWebService/GetMapService.asmx
http://<Web_home_directory>/ImageWebService/ProperService.asmx
http://<Web_home_directory>/ImageWebService/TimerService.asmx


****************************************************************************************

        New OpenMap .NET XML Web Services code has been written and included
        in the openmap/iis directory.  A demo of this code, which is an
        HTML/ASP.NET OpenMap client, will be set up at http://www.openmap.net (get
        it?) eventually, but a link from the main OpenMap web page will
        direct you to it as well.
        
        There are two directories in the OpenMap.net project:
        ImageWebService and OpenMapWeb.
        
        ImageWebService contains the .NET XML Web Services. There are
        three of them: ProperService.asmx, GetMapService.asmx, and
        TimerService.asmx.
        - ProperService is responsible for starting and killing Java Image
        servers, updating user sessions, keeping information about layers
        used.
        - GetMapService service has three methods: GetMapImage,
        RecenterMap, PanMap. It uses ProperService to start Java processes
        as needed.
        - TimerService is a utility that will call CleanServerProcs()
        method in ProperService every certain number of minutes to check
        whether it's time to kill some java servers.
        
        The <appSettings> section in web.config file in ImageWebService
        folder is used to configure web services to point to the Java
        OpenMap installation and openmap.properties file.


        The OpenMapWeb folder contains two ASP.NET files: WebMap.aspx and
        MapImage.aspx.
        - WebMap.aspx is the user interface to the project.
        - MapImage.aspx is used to contact a web service to get raw image
        data.


        To support this service, additional functionality was added to the
        ImageServer class:


        - the PAN, AZIMUTH, X and Y arguments were added to the parsable
        argument list, allowing the client to send the server panning and
        centering commands (based on the current image projection).
        - if the 'layers' property is not defined for the ImageServer
        (including the MapRequestHandler) to use to configure the default
        layers, then the 'openmap.layers' property will be used to create
        the layers available to the ImageServer, and the
        'openmap.startUpLayers' property will define the default layer list.
        
****************************************************************************************
