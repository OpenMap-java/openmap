// **********************************************************************
// 
// <copyright>
// 
//  BBN Technologies, a Verizon Company
//  10 Moulton Street
//  Cambridge, MA 02138
//  (617) 873-8000
// 
//  Copyright (C) BBNT Solutions LLC. All rights reserved.
// 
// </copyright>
// **********************************************************************
// 
// $Source: /cvs/distapps/openmap/src/openmap/com/bbn/openmap/layer/rpf/RpfAttributes.java,v $
// $RCSfile: RpfAttributes.java,v $
// $Revision: 1.2 $
// $Date: 2003/12/23 20:43:29 $
// $Author: wjeuerle $
// 
// **********************************************************************


package com.bbn.openmap.layer.rpf;

import java.io.File;
import java.io.IOException;
import java.io.FileNotFoundException;

import com.bbn.openmap.util.Debug;
import com.bbn.openmap.io.*;

/**
 * This class knows how to read the attribute section of an RPF file.
 * This section includes all the information about the image,
 * including source and production information.
 */
public class RpfAttributes {

    public String currencyDate;// [8];
    public String productionDate; // [8];
    public String significantDate; // [8];
    public String chartSeriesCode; // [10];
    public String mapDesignationCode; // [8];
    public String oldHorDatum; // [4];
    public String edition; // [7];
    public String projectionCode; // [2];
    public float projectionA;
    public float projectionB;
    public float projectionC;
    public float projectionD;
    public String vertDatumCode; // [4];
    public String horDatumCode; // [4];
    public long vertAbsAccuracy; //uint
    public int vertAbsUnits; //ushort
    public long horAbsAccuracy; //uint
    public int horAbsUnits; // ushort
    public long vertRelAccuracy; //uint
    public int vertRelUnits; // ushort
    public long horRelAccuracy; //uint
    public int horRelUnits; // ushort
    public String ellipsoidCode; // [3];
    public String soundingDatumCode; // [4];
    public int navSystemCode; // ushort
    public String gridCode; // [2];
    public float eMagChange; 
    public int eMagChangeUnits; // ushort
    public float wMagChange;
    public int wMagChangeUnits; // ushort
    public float magAngle; //uint
    public int magAngleUnits; // ushort
    public float gridConver; //uint
    public int gridConverUnits; // ushort
    public double highElevation;
    public int highElevationUnits; // ushort
    public double highLat;
    public double highLon;
    public String legendFileName; // [12];
    public String dataSource; // [12];
    public long gsd; // uint
    public int dataLevel; // ushort
    

    public RpfAttributes() {}

    /**
     * Read the section in a file.  The method will start reading
     * from the offset provided.
     *
     * @param binFile the opened RPF file.
     * @param attributeLocation the offset of the attribute section in
     * the file.
     */
    public boolean read(BinaryFile binFile, long attributeLocation) {

	try {
	    
	    long k;
	    long j=0;

	    AttributeSubheader attributeSubheader = new AttributeSubheader();
//  	    AttributeOffsetRecord attributeOffsetRecord = new AttributeOffsetRecord();
  	    AttributeOffsetRecord attributeOffsetRecord;

	    binFile.seek(attributeLocation);
	    attributeSubheader.read(binFile);

	    if (Debug.debugging("rpfdetail")) {
		System.out.println(attributeSubheader);
	    }


	    AttributeOffsetRecord[] attributeOffsetRecords = 
		new AttributeOffsetRecord[attributeSubheader.numAttributes];

	    int i = attributeSubheader.numAttributes - 1;

	    // OK, I know what you are thinking - two sequential
	    // loops?  It's an optimization for the binFile,
	    // especially if we're reading from a URL - turns this
	    // into a couple of sequential reads.

	    j = 0;
  	    for (int attIndex = attributeSubheader.numAttributes; attIndex > 0; attIndex--) {
		i = attIndex - 1;
		k = attributeLocation+10/*sizeof(attribute_subheader)*/ + 
		    attributeSubheader.tableOffset +
		    ((attributeSubheader.numAttributes-attIndex)*8/*sizeof(attributeOffsetRecord*/);

		binFile.seek(k);

		attributeOffsetRecords[i] = new AttributeOffsetRecord();
		attributeOffsetRecords[i].read(binFile);
		
		if (Debug.debugging("rpfdetail")) {
		    Debug.output(" ##" + (++j) + " at " + k + 
				 " => attrib ID '" + 
				 attributeOffsetRecords[i].attributeId + "'|Param ID '" +
				 attributeOffsetRecords[i].parameterId + "'|offset '" +
				 attributeOffsetRecords[i].offset + "'");
		}
	    }

  	    for (i = attributeOffsetRecords.length - 1; i >= 0; i--) {

		attributeOffsetRecord = attributeOffsetRecords[i];

		binFile.seek(attributeLocation+10+attributeOffsetRecord.offset);
		switch (attributeOffsetRecord.attributeId) {
		case 1:
		    currencyDate = binFile.readFixedLengthString(8);// [8];
		    break;
		case 2:
		    productionDate = binFile.readFixedLengthString(8); // [8];
		    break;
		case 3:
		    significantDate = binFile.readFixedLengthString(8); // [8];
		    break;
		case 4:
		    if ((int)attributeOffsetRecord.parameterId == 1)
			chartSeriesCode = binFile.readFixedLengthString(10); // [10];
		    else if ((int)attributeOffsetRecord.parameterId == 2)
			mapDesignationCode = binFile.readFixedLengthString(8); // [8];
		    else if ((int)attributeOffsetRecord.parameterId == 3)
			oldHorDatum = binFile.readFixedLengthString(4); // [4];
		    else if ((int)attributeOffsetRecord.parameterId == 4)
			edition = binFile.readFixedLengthString(7); // [7];
		    break;
		case 5:
		    if ((int)attributeOffsetRecord.parameterId == 1)
			projectionCode = binFile.readFixedLengthString(2); // [2];
		    else if ((int)attributeOffsetRecord.parameterId == 2)
			projectionA = binFile.readFloat();
		    else if ((int)attributeOffsetRecord.parameterId == 3)
			projectionB = binFile.readFloat();
		    else if ((int)attributeOffsetRecord.parameterId == 4)
			projectionC = binFile.readFloat();
		    else if ((int)attributeOffsetRecord.parameterId == 5)
			projectionD = binFile.readFloat();
		    break;
		case 6:
		    vertDatumCode = binFile.readFixedLengthString(4); // [4];
		    break;
		case 7:
		    horDatumCode = binFile.readFixedLengthString(4); // [4];
		    break;
		case 8:
		    if ((int)attributeOffsetRecord.parameterId == 1)
			vertAbsAccuracy = (long)binFile.readInteger();
		    else if ((int)attributeOffsetRecord.parameterId == 2)
			vertAbsUnits = (int) binFile.readShort(); //ushort
		    break;
		case 9:
		    if ((int)attributeOffsetRecord.parameterId == 1)
			horAbsAccuracy = (long)binFile.readInteger();
		    else if ((int)attributeOffsetRecord.parameterId == 2)
			horAbsUnits = (int) binFile.readShort(); // ushort
		    break;
		case 10:
		    if ((int)attributeOffsetRecord.parameterId == 1)
			vertRelAccuracy = (long)binFile.readInteger();
		    else if ((int)attributeOffsetRecord.parameterId == 2)
			vertRelUnits = (int) binFile.readShort(); // ushort
		    break;
		case 11:
		    if ((int)attributeOffsetRecord.parameterId == 1)
			horRelAccuracy = (long) binFile.readInteger();
		    else if ((int)attributeOffsetRecord.parameterId == 2)
			horRelUnits = (int) binFile.readShort(); // ushort
		    break;
		case 12:
		    ellipsoidCode = binFile.readFixedLengthString(3); // [3];
		    break;
		case 13:
		    soundingDatumCode = binFile.readFixedLengthString(4); // [4];
		    break;
		case 14:
		    navSystemCode = (int) binFile.readShort(); // ushort
		    break;
		case 15:
		    gridCode = binFile.readFixedLengthString(2); // [2];
		    break;
		case 16:
		    if ((int)attributeOffsetRecord.parameterId == 1)
			eMagChange = binFile.readFloat();
		    else if ((int)attributeOffsetRecord.parameterId == 2)
			eMagChangeUnits = (int) binFile.readShort(); // ushort
		    break;
		case 17:
		    if ((int)attributeOffsetRecord.parameterId == 1)
			wMagChange = binFile.readFloat();
		    else if ((int)attributeOffsetRecord.parameterId == 2)
			wMagChangeUnits = (int) binFile.readShort(); // ushort
		    break;
		case 18:
		    if ((int)attributeOffsetRecord.parameterId == 1)
			magAngle = binFile.readFloat();
		    else if ((int)attributeOffsetRecord.parameterId == 2)
			magAngleUnits = (int) binFile.readShort(); // ushort
		    break;
		case 19:
		    if ((int)attributeOffsetRecord.parameterId == 1)
			gridConver = binFile.readFloat();
		    else if ((int)attributeOffsetRecord.parameterId == 2)
			gridConverUnits = (int) binFile.readShort(); // ushort
		    break;
		case 20:
		    if ((int)attributeOffsetRecord.parameterId == 1)
			highElevation = binFile.readDouble();
		    else if ((int)attributeOffsetRecord.parameterId == 2)
			highElevationUnits = (int) binFile.readShort(); // ushort
		    else if ((int)attributeOffsetRecord.parameterId == 3)
			highLat = binFile.readDouble();
		    else if ((int)attributeOffsetRecord.parameterId == 4)
			highLon = binFile.readDouble();
		    break;
		case 21:
		    legendFileName = binFile.readFixedLengthString(12); // [12];
		    break;
		case 22:
		    if((int)attributeOffsetRecord.parameterId == 1)
			dataSource = binFile.readFixedLengthString(12); // [12];
		    else if ((int)attributeOffsetRecord.parameterId == 2)
			gsd = (long)binFile.readInteger(); // uint
		    break;
		case 23: 
		    dataLevel = (int) binFile.readShort(); // ushort
		    break;
		}
		i--;
	    }

	} catch (IOException e) {
	    Debug.error("RpfAttributes: read(): File IO Error!\n" + e);
	    return false;
	} catch (FormatException f) {
	    Debug.error("RpfAttributes: read(): File IO Format error!\n" + f);
	    return false;
	}

	return true;
    }
    
    public String toString() {
	StringBuffer s = new StringBuffer();
	s.append("Attribute Section:\n");
	s.append(" currency date = " + currencyDate + "\n");
	s.append(" production date = " + productionDate + "\n");
	s.append(" significant date = " + significantDate + "\n");
	s.append(" chart series code = " + chartSeriesCode + "\n"); 
	s.append(" map designation code = " + mapDesignationCode + "\n");
	s.append(" old horizontal datum = " + oldHorDatum + "\n");
	s.append(" edition = " + edition + "\n");
	s.append(" projection code = " + projectionCode + "\n");
	s.append(" projection A = " + projectionA + "\n");
	s.append(" projection B = " + projectionB + "\n");
	s.append(" projection C = " + projectionC + "\n");
	s.append(" projection D = " + projectionD + "\n");
	s.append(" vertical datum code = " + vertDatumCode + "\n"); 
	s.append(" horizontal datum code = " + horDatumCode + "\n");
	s.append(" vertical absolute accuracy = " + vertAbsAccuracy + "\n");
	s.append(" vertical absolute units = " + vertAbsUnits + "\n");
	s.append(" horizontal absolute accuracy = " + horAbsAccuracy + "\n");
	s.append(" horizontal absolute units = " + horAbsUnits + "\n"); 
	s.append(" vertical relative accuracy = " + vertRelAccuracy + "\n");
	s.append(" vertical relative units = " + vertRelUnits + "\n");
	s.append(" horizontal relative accuracy = " + horRelAccuracy + "\n");
	s.append(" horizontal relative units = " + horRelUnits + "\n"); 
	s.append(" ellipoid code = " + ellipsoidCode + "\n");
	s.append(" sounding datum code = " + soundingDatumCode + "\n");
	s.append(" nav system code = " + navSystemCode + "\n"); 
	s.append(" grid code = " + gridCode + "\n"); 
	s.append(" east mag change = " + eMagChange + "\n");
	s.append(" east mag change units = " + eMagChangeUnits + "\n");
	s.append(" west mag change = " + wMagChange + "\n");
	s.append(" west mag units = " + wMagChangeUnits + "\n");
	s.append(" magnetic angle = " + magAngle + "\n");
	s.append(" magnetic angle units = " + magAngleUnits + "\n"); 
	s.append(" grid conversion = " + gridConver + "\n");
	s.append(" grid conversion units = " + gridConverUnits + "\n");
	s.append(" high elevation = " + highElevation + "\n");
	s.append(" high elevation units = " + highElevationUnits + "\n");
	s.append(" high latitude = " + highLat + "\n");
	s.append(" high longitude = " + highLon + "\n");
	s.append(" legend file name = " + legendFileName + "\n");
	s.append(" data source = " + dataSource + "\n"); 
	s.append(" gsd = " + gsd + "\n");
	s.append(" data level = " + dataLevel + "\n");
	return s.toString();
    }

    /**
     * Get the attributes from within a RPF Frame file.  Returns null
     * if something goes wrong.  You do need to make sure that the
     * Debug class is initialized before calling this class.  For
     * OpenMap, it usually is. 
     *
     * @param filename the file path for the RPF frame file.
     * @return a RpfAttributes object.
     */
    public static RpfAttributes getAttributes(String filename) 
	throws IOException, FileNotFoundException {

	BinaryFile binFile = new BinaryBufferedFile(filename);
// 	BinaryFile binFile = new BinaryFile(filename);
	
	RpfHeader head = new RpfHeader();
	head.read(binFile);
	
	RpfAttributes att = getAttributes(binFile);
	binFile.close();
	head = null;
	return att;
    }

    /**
     * Get the attributes from within a RPF Frame file, after the
     * header has been read.  Returns null if something goes wrong.
     * You do need to make sure that the Debug class is initialized
     * before calling this class.  For OpenMap, it usually is.
     *
     * @param binFile BinaryFile.
     * @return a RpfAttributes object.
     */
    public static RpfAttributes getAttributes(BinaryFile binFile) 
	throws IOException, FileNotFoundException {
	RpfFileSections rfs = new RpfFileSections(binFile);
	RpfAttributes att = rfs.parseAttributes(binFile);
	return att;
    }

    public static void main(String[] args) {
	if (args.length != 1) {
	    System.out.println("Usage: java RpfAttributes <path to RPF frame>");
	    return;
	}

	Debug.init(System.getProperties());
	try {
	    RpfAttributes att = RpfAttributes.getAttributes(args[0]);
	    Debug.output(att.toString());
	} catch (FileNotFoundException e) {
	    Debug.error("RpfAttributes: file "+ args[0] + " not found");
	} catch (IOException ioe) {
	    Debug.error("RpfAttributes: File IO Error while handling attributes: \n" + ioe);
	}
    }

    static public class AttributeSubheader {
	int numAttributes; //ushort
	int numArealRecords; // ushort
	long tableOffset; // ulong
	int offsetRecordLength; // ushort

	public void read(BinaryFile binFile) {
	    try{
		numAttributes = (int) binFile.readShort();
		numArealRecords = (int) binFile.readShort();
		tableOffset = (long) binFile.readInteger();
		offsetRecordLength = (int) binFile.readShort();
	    } catch (IOException e) {
		Debug.error("AttributeSubheader: read(): File IO Error!\n" + e);
	    } catch (FormatException f) {
		Debug.error("AttributeSubheader: read(): File IO Format error!\n" + f);
	    }

	}

	public String toString() {
	    StringBuffer s = new StringBuffer();
	    s.append("## RPF ATTRIBUTE INFORMATION\n");
	    s.append("Number of Attributes - " + numAttributes + "\n");
	    s.append("Number of Areal Records - " + numArealRecords + "\n");
	    s.append("Size of offset - " + tableOffset + "\n");
	    s.append("Record Length - " + offsetRecordLength + "\n");
	    return s.toString();
	}
    }

    static public class AttributeOffsetRecord {
	int attributeId; // ushort
	int parameterId; //char
	int sequenceNum; //char
	long offset; //ulong

	public void read(BinaryFile binFile) {
	    try{
		attributeId = (int) binFile.readShort();
		parameterId = binFile.read();
		sequenceNum = binFile.read();
		offset = (long) binFile.readInteger();
	    } catch (IOException e) {
		Debug.error("AttributeOffsetRecord: read(): File IO Error!\n" + e);
	    } catch (FormatException f) {
		Debug.error("AttributeOffsetRecord: read(): File IO Format error!\n" + f);
	    }	
	}
    }
}
