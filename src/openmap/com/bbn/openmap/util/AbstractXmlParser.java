/* 
 * <copyright>
 *  Copyright 2010 BBN Technologies
 * </copyright>
 */
package com.bbn.openmap.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Base class for xml parsing
 * 
 * @author rshapiro
 */
abstract public class AbstractXmlParser
      extends DefaultHandler {
   private final Logger logger = Logger.getLogger("com.bbn.openmap.util.AbstractXmlParser");

   private final StringBuilder charactersCollector = new StringBuilder();
   private String collectCharactersForElement;

   /**
    * Parse an XML resource by url. Exceptions are logged rather than thrown. If
    * an exception occurs the return value will be false to indicate that fact.
    * 
    * @param file The file to parse.
    * @return Whether or not the parse succeeded.
    */
   public boolean parseXmlResource(File file) {
      Reader reader = null;
      try {
         reader = new BufferedReader(new FileReader(file));
         return parseXmlResource(file.getPath(), new InputSource(reader));
      } catch (FileNotFoundException e) {
         logger.warning("Failed to open " + file + ":" + e.getMessage());
         return false;
      } finally {
         if (reader != null) {
            try {
               reader.close();
            } catch (IOException e) {
               // best-effort
            }
         }
      }
   }

   /**
    * Parse from the given source. The source will not be closed after the
    * parse: the caller should do this.
    * 
    * Exceptions are logged rather than thrown. If an exception occurs the
    * return value will be false to indicate that fact.
    * 
    * @param resourceName The resource name to use in warning messages.
    * @param source The source of the xml input.
    * @return Whether or not the parse succeeded.
    */
   public boolean parseXmlResource(Object resourceName, InputSource source) {
      XMLReader reader;
      source.setEncoding("UTF8");
      try {
         reader = XMLReaderFactory.createXMLReader();
      } catch (SAXException e) {
         if (logger.isLoggable(Level.SEVERE)) {
            logger.warning("Failed to create reader for " + resourceName + ": " + e.getMessage());
         }
         return false;
      }
      boolean status = false;
      reader.setContentHandler(this);
      reader.setErrorHandler(this);
      try {
         reader.parse(source);
         status = true;
      } catch (SAXParseException e) {
         if (logger.isLoggable(Level.WARNING)) {
            logger.warning("Failed to parse " + resourceName + " Line: " + e.getLineNumber() + " Col: " + e.getColumnNumber()
                  + ": " + e);
         }
      } catch (SAXException e) {
         if (logger.isLoggable(Level.WARNING)) {
            logger.warning("Failed to parse " + resourceName + e.getMessage());
         }
      } catch (IOException e) {
         if (logger.isLoggable(Level.WARNING)) {
            logger.warning("Failed to parse " + resourceName + ":" + e.getMessage());
         }
      }
      return status;
   }

   /**
    * Inform the parser that we want to start gathering up the character data
    * for the current element. This would typically be invoked in
    * {@link #startElement} with the localName as the parameter.
    * 
    * Use {@link #getCollectedCharacters}, typically in the corresponding
    * {@link #endElement}, to retrieve the result.
    * 
    * @param expectedElement The element whose character data we're gathering.
    *        Caller must pass the same element to
    *        {@link #getCollectedCharacters} to ensure consistency.
    */
   public void collectCharacters(String expectedElement) {
      collectCharactersForElement = expectedElement;
   }

   /**
    * Get the final string accumulated from all invocations of
    * {@link #characters} since the last call to {@link #collectCharacters}.
    * 
    * @param expectedElement The element whose character data we're gathering.
    *        Caller must pass the same element to {@link #collectCharacters} to
    *        ensure consistency.
    * @return The String accumulated from all intervening calls to
    *         {@link #characters}.
    * @throws RuntimeException if the expectedElement doesn't match.
    */
   public String getCollectedCharacters(String expectedElement) {
      if (expectedElement.equals(collectCharactersForElement)) {
         String result = charactersCollector.toString();
         charactersCollector.delete(0, result.length());
         collectCharactersForElement = null;
         return result;
      } else {
         throw new RuntimeException("Expected \"" + expectedElement + "\", found \"" + collectCharactersForElement + "\"");
      }
   }

   /**
    * Handle multiple callbacks per element, which will happen when the data
    * includes xml escape sequences. Each escape sequence comes in its own
    * callback.
    */
   @Override
   public void characters(char[] ch, int start, int length)
         throws SAXException {
      if (collectCharactersForElement != null) {
         charactersCollector.append(ch, start, length);
      }
   }
}
