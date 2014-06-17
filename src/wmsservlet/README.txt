WMSServlet Package README

This package, built separately, creates a war file that can be loaded into tomcat or 
glassfish to provide a OGC WMS server.  The servlet code in the war file contains 
everything you need, but it must be configured for your data and system configuration.

- The build.xml file controls the creation of the war file.  You may want to modify 
what gets added to the war, data files and such, and where the servlet-api.jar file 
can be found to compile the servlet classes.  It can be found in the tomcat/glassfish 
installations.

- Before you create the war file (using ant), you need to modify the WEB-INF/web.xml file,
setting the port your server is running on (8080 is the default port of tomcat/glassfish),
your URL request path, and the path to the properties file you are using to configure the
servlet map layers.  If you want to upload the properties file and have it found in the war
file by simply naming the file with no path, it should work if you put it in the 
WEB-INF/classes directory.  Otherwise, you can store the properties file anywhere on your system
and specify the path in the web.xml file.

A sample properties file is in this directory, wms.properties.  It works like the layer 
section of a standard openmap.properties file with a couple of extra properties.