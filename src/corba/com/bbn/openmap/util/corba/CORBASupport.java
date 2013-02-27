// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/corba/com/bbn/openmap/util/corba/CORBASupport.java,v $
// $RCSfile: CORBASupport.java,v $
// $Revision: 1.4 $
// $Date: 2004/10/14 18:05:38 $
// $Author: dietrick $
// 
// **********************************************************************

package com.bbn.openmap.util.corba;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Vector;

import org.omg.CORBA.ORB;
import org.omg.CORBA.portable.ObjectImpl;
import org.omg.CosNaming.NameComponent;
import org.omg.CosNaming.NamingContext;
import org.omg.CosNaming.NamingContextHelper;
import org.omg.PortableServer.POA;
import org.omg.PortableServer.POAHelper;
import org.omg.PortableServer.Servant;

import com.bbn.openmap.Environment;
import com.bbn.openmap.util.Debug;

/**
 * CORBASupport provides common functions for OpenMap servers.
 */
public class CORBASupport {

   /**
    * Return a static reference to the ORB. Figures out which environment
    * (applet or application) the jre is in, and initialize the orb accordingly.
    */
   public ORB initORB(String[] args) {

      Debug.message("corba", "CORBAManager.getORB(): initializing ORB");
      if (Environment.isApplet()) {
         // initialize the Environment with the properties passed
         // in.
         if (Debug.debugging("corba")) {
            System.out.println("CORBAManager: initializing applet");
         }
         return ORB.init(Environment.getApplet(), Environment.getProperties());
      }

      if (Debug.debugging("corba")) {
         System.out.println("CORBAManager: initializing application");
      }
      return ORB.init((String[]) null, System.getProperties());
   }

   /**
    * Read a IOR file from an URL, return the org.omg.CORBA.Object for that IOR.
    */
   public org.omg.CORBA.Object readIOR(URL iorURL)
         throws IOException {
      org.omg.CORBA.Object object = null;

      if (iorURL != null) {
         URLConnection urlConnection = iorURL.openConnection();
         InputStream is = urlConnection.getInputStream();
         InputStreamReader isr = new InputStreamReader(is);
         BufferedReader reader = new BufferedReader(isr);
         String ior = reader.readLine();

         if (Debug.debugging("corba")) {
            Debug.output("CORBASupport.readIOR() using ior: " + ior);
         }
         reader.close();

         if (ior != null) {
            object = initORB(null).string_to_object(ior);
         }
      }

      return object;
   }

   /**
    * Write the IOR for a object to a file.
    * 
    * @param iorFile where to write the file
    * @param iorObj the object to represent in the IOR file.
    */
   public void writeIOR(String iorFile, org.omg.CORBA.Object iorObj)
         throws IOException {
      if (iorFile != null) {
         ORB orb = initORB(null);
         java.io.FileWriter outFile = new java.io.FileWriter(iorFile);
         java.io.PrintWriter writer = new java.io.PrintWriter(outFile);
         String ior = orb.object_to_string(iorObj);
         writer.println(ior);
         writer.close();

         if (Debug.debugging("corba")) {
            Debug.output(orb.object_to_string(iorObj));
         }
      }
   }

   /**
    * Set up the CORBA NamingService with the name for an object.
    */
   public void setUpNamingService(String naming, org.omg.CORBA.Object namingObj) {

      ORB orb = initORB(null);

      if (naming != null) {
         // *** Naming server stuff
         org.omg.CORBA.Object obj = null;
         try {
            obj = orb.resolve_initial_references("NameService");
            if (Debug.debugging("corba")) {
               Debug.output("CORBASupport.setUpNamingService(): NameService Object OK");
            }
         } catch (org.omg.CORBA.ORBPackage.InvalidName e) {
            Debug.error("CORBASupport.setUpNamingService(): Invalid Name exception \n" + e.getMessage());
         }

         NamingContext rootContext = NamingContextHelper.narrow(obj);

         if (rootContext == null) {
            if (Debug.debugging("corba")) {
               Debug.output("CORBASupport.setUpNamingService(): Root context null!!");
            }
         }

         String temp = naming;
         Vector components = new Vector();
         int numcomponents = 0;
         String temporaryTemp = null;

         int tindex = temp.indexOf("/");
         while (tindex != -1) {
            numcomponents++;
            temporaryTemp = temp.substring(0, tindex);
            if (Debug.debugging("corba")) {
               Debug.output("CORBASupport.setUpNamingService(): Adding Name component: " + temporaryTemp);
            }
            components.addElement(temporaryTemp);
            temp = temp.substring(tindex + 1);
            tindex = temp.indexOf("/");
         }

         if (Debug.debugging("corba")) {
            Debug.output("CORBASupport.setUpNamingService(): Adding final Name component: " + temp);
         }
         components.addElement(temp);

         NamingContext newContext = null;
         NamingContext oldContext = rootContext;
         for (int i = 0; i < components.size() - 1; i++) {
            NameComponent[] newName = new NameComponent[1];
            newName[0] = new NameComponent((String) (components.elementAt(i)), "");
            String debugName = (String) (components.elementAt(i));
            if (Debug.debugging("corba")) {
               Debug.output("CORBASupport.setUpNamingService(): Working on: " + debugName);
            }
            try {
               if (oldContext != null) {
                  newContext = NamingContextHelper.narrow(oldContext.resolve(newName));
               }
            } catch (org.omg.CosNaming.NamingContextPackage.NotFound nfe) {
               try {
                  if (Debug.debugging("corba")) {
                     Debug.output("CORBASupport.setUpNamingService(): Doing a bind new context");
                  }
                  newContext = oldContext.bind_new_context(newName);
               } catch (org.omg.CosNaming.NamingContextPackage.AlreadyBound abe) {
                  Debug.output("CORBASupport.setUpNamingService(): Already bound for new context");
               } catch (org.omg.CosNaming.NamingContextPackage.CannotProceed cpe0) {
                  Debug.output("CORBASupport.setUpNamingService(): Cannot proceed for new context");
               } catch (org.omg.CosNaming.NamingContextPackage.InvalidName ine0) {
                  Debug.output("CORBASupport.setUpNamingService(): Invalid Name for new context");
               } catch (org.omg.CosNaming.NamingContextPackage.NotFound nfe0) {
                  Debug.output("CORBASupport.setUpNamingService(): Not found for new context");
               }
            } catch (org.omg.CosNaming.NamingContextPackage.InvalidName ine) {
               Debug.output("CORBASupport.setUpNamingService(): Invalid name");
            } catch (org.omg.CosNaming.NamingContextPackage.CannotProceed cpe) {
               Debug.output("CORBASupport.setUpNamingService(): Cannot proceed");
            }
            oldContext = newContext;
         }

         NameComponent[] finalName = new NameComponent[1];
         finalName[0] = new NameComponent((String) (components.elementAt(components.size() - 1)), "");
         String debugName = (String) (components.elementAt(components.size() - 1));
         if (Debug.debugging("corba")) {
            Debug.output("CORBASupport.setUpNamingService: Finally working on: " + debugName);
         }

         if (oldContext != null) {
            try {
               if (Debug.debugging("corba")) {
                  Debug.output("CORBASupport.setUpNamingService(): Doing a rebind :" + orb.object_to_string(oldContext));
               }
               oldContext.rebind(finalName, namingObj);
               if (Debug.debugging("corba")) {
                  Debug.output("CORBASupport.setUpNamingService(): Completed rebind for " + finalName[0]);
               }
            } catch (org.omg.CosNaming.NamingContextPackage.CannotProceed cpe1) {
               Debug.output("CORBASupport.setUpNamingService(): Cannot proceed in rebind");
            } catch (org.omg.CosNaming.NamingContextPackage.InvalidName ine1) {
               Debug.output("CORBASupport.setUpNamingService(): Invalid Name in rebind");
            } catch (org.omg.CosNaming.NamingContextPackage.NotFound nfe1) {
               Debug.output("CORBASupport.setUpNamingService(): Not found in rebind");
            }
         }
      }
   }

   public org.omg.CORBA.Object resolveName(String naming) {

      if (naming != null) {
         try {
            ORB orb = initORB(null);

            if (Debug.debugging("corbadetail")) {
               listServices(orb);
            }

            // Get the root context of the name service.
            org.omg.CORBA.Object obj = null;
            try {
               obj = orb.resolve_initial_references("NameService");
            } catch (Exception e) {
               Debug.error("CORBASupport.resolveName(): Error getting root naming context.");
               e.printStackTrace();
            }

            NamingContext rootContext = NamingContextHelper.narrow(obj);

            if (Debug.debugging("corba")) {
               if (rootContext == null) {
                  Debug.error("CORBASupport.resolveName(): No root context!");
               }
            }

            // Resolve the specialist
            String temp = naming;
            Vector components = new Vector();
            int numcomponents = 0;
            String temporaryTemp = null;

            int tindex = temp.indexOf("/");
            while (tindex != -1) {
               numcomponents++;
               temporaryTemp = temp.substring(0, tindex);
               if (Debug.debugging("corba")) {
                  Debug.output("CORBASupport.resolveName(): Adding Name component: " + temporaryTemp);
               }
               components.addElement(temporaryTemp);
               temp = temp.substring(tindex + 1);
               tindex = temp.indexOf("/");
            }
            if (Debug.debugging("corba")) {
               Debug.output("CORBASupport.resolveName(): Adding final Name component: " + temp);
            }
            components.addElement(temp);

            NameComponent[] objectName = new NameComponent[components.size()];
            for (int i = 0; i < components.size(); i++) {
               objectName[i] = new NameComponent((String) (components.elementAt(i)), "");
            }

            obj = null; // reset
            try {
               if (rootContext != null) {
                  obj = rootContext.resolve(objectName);
               } else {
                  Debug.output("CORBASupport.resolveName(): No Root Context for naming.");
               }
            } catch (Exception e) {
               Debug.output("CORBASupport.resolveName(): Error resolving for the object.");
               e.printStackTrace();
            }

            if (obj == null) {
               if (Debug.debugging("corba")) {
                  Debug.output("CORBASupport.resolveName(): no object after resolve");
               }
            } else {
               if (Debug.debugging("corba")) {
                  Debug.output("CORBASupport.resolveName(): " + orb.object_to_string(obj));
               }
            }
            return obj;

         } catch (org.omg.CORBA.SystemException e) {
            Debug.error("CORBASupport.resolveName(): " + e);
         } catch (Throwable t) {
            Debug.output("CORBASupport.resolveName(): " + t);
         }
      }

      return null;
   }

   protected void listServices(ORB orb) {
      String[] services = orb.list_initial_services();
      if (services != null) {
         Debug.output("CORBASupport: Listing services:");

         for (int k = 0; k < services.length; k++) {
            Debug.output("  service " + k + ": " + services[k]);
         }
      } else {
         Debug.output("CORBASupport: no services available");
      }
   }

   /**
    * This is a default start method that initializes the server represented by
    * a Servant. This is a POA method. The args should be command line
    * arguments, and the ior file is written and naming service is started from
    * here.
    * 
    * @param servant the Servant of the POA object to hook up to the ORB.
    * @param args a String[] of args to pass to orb on initialization.
    * @param iorFile the path of the ior file to write, can be null to not write
    *        a file.
    * @param naming the name of the Servant to pass to the NamingService, can be
    *        null to not register a name.
    */
   public void start(Servant servant, String[] args, String iorFile, String naming) {
      start(servant, args, iorFile, naming, true);
   }

   /**
    * This is a default start method that initializes the server represented by
    * a Servant. This is a POA method. The args should be command line
    * arguments, and the ior file is written and naming service is started from
    * here.
    * 
    * @param servant the Servant of the POA object to hook up to the ORB.
    * @param args a String[] of args to pass to orb on initialization.
    * @param iorFile the path of the ior file to write, can be null to not write
    *        a file.
    * @param naming the name of the Servant to pass to the NamingService, can be
    *        null to not register a name.
    * @param runORB flag to call orb.run() at the end of the method. This will
    *        block the current thread! If the servant is a server, then this
    *        should be true. If you have a callback object that you just want to
    *        register with the orb, this should be false.
    */
   public void start(Servant servant, String[] args, String iorFile, String naming, boolean runORB) {
      // Initialize the ORB
      ORB orb = initORB(args);
      POA poa = null;

      if (Debug.debugging("corbadetail")) {
         listServices(orb);
      }

      // find root poa
      try {
         org.omg.CORBA.Object raw = orb.resolve_initial_references("RootPOA");
         poa = POAHelper.narrow(raw);
         poa.the_POAManager().activate();
      } catch (Exception error) {
         Debug.error("Error getting root POA: " + error);
         error.printStackTrace();
         return;
      }

      try {
         poa.activate_object(servant);
      } catch (Exception e) {
         Debug.error("Caught exception activating POA object: \n" + e.getMessage());
      }

      // write the IOR out
      try {
         writeIOR(iorFile, servant._this_object());
      } catch (IOException ioe) {
         Debug.error("CORBASupport caught IOException writing IOR file to " + iorFile);
      }

      // Set up naming service
      setUpNamingService(naming, servant._this_object());

      // Announce ourselves to the world
      Debug.output(servant.toString() + " is ready.");

      if (runORB) {
         orb.run();
      }
   }

   /**
    * This is a default start method that initializes a CORBA server This is a
    * BOA method, and the main BOA method calls are commented out because the
    * BOA isn't available in the jdk by default. If you are using a CORBA
    * installation with a BOA implementation, uncomment the lines below and
    * recompile. The args should be command line arguments, and the ior file is
    * written and naming service is started from here.
    */
   public void start(ObjectImpl obj, String[] args, String iorFile, String naming) {
      ORB orb = initORB(args);

      // Initialize the BOA
      // org.omg.CORBA.BOA boa = orb.BOA_init();

      // // Export the newly created object
      // boa.obj_is_ready(obj);

      if (Debug.debugging("corbadetail")) {
         listServices(orb);
      }

      // write the IOR
      try {
         writeIOR(iorFile, obj);
      } catch (IOException ioe) {
         Debug.error("CORBASupport caught IOException writing IOR file to " + iorFile);
      }

      // Set up naming service
      setUpNamingService(naming, obj);

      // Announce ourselves to the world
      Debug.output(obj.toString() + " is ready.");

      // // Wait for incoming requests
      // boa.impl_is_ready();

   }

}