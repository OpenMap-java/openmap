package com.bbn.openmap.util;

import java.util.*;
import java.io.*;
import com.bbn.openmap.util.*;
/**
An object to read Comma Seperated Value file completely. Once read users can ask for tokens using line number and column number.
*/
public class CSVReader 
{
    Vector line = new Vector();// Every element of this vector will contain
    //a vector, which in turn will contain comma delimited values of a line
    String fileName = null;
    static final String delimiter = ",";
    public CSVReader(String in_fileName) {
        setFileName(in_fileName);
    }
    // With JDK 1.4, String.split should be used and following code should not used.
    /**
       Read the Comma seperated file fully and
       initialize internal data structures.       
     */    
    public void loadDataOld() {
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            String lineStr = null;

            while((lineStr = br.readLine())!=null) {
                StringTokenizer tokenizer = new StringTokenizer(lineStr,delimiter);// SUN discourages use of StringTokenizer, since it is buggy for this purpose.
                // init() below is hack but it seems to work.
                Vector tokens = new Vector();
                
                while(tokenizer.hasMoreTokens()) {
                    String tmp = tokenizer.nextToken();
                    // System.out.print(tmp +" ");
                    tokens.add(tmp);
                }
                if(tokens.size() > 0)
                    line.add(tokens);
            }
        }catch(Exception e) {
            System.err.println(e.getMessage());
            e.printStackTrace();            
        }        
    }
    /**
       Read the Comma seperated file fully and
       initialize internal data structures.       
     */    
    public void loadData() {
        
        try 
        {
            CSVTokenizer csvt = new CSVTokenizer(new FileReader(fileName),true);
            Object token = csvt.token();
            
                        
            while(!csvt.isEOF(token)) 
            {
                Vector tokens = new Vector();
                String val = null;
                
                while(!csvt.isNewline(token))
                {
                	Object lastToken = token;
                    if(token == null) 
                    {
                        val = "";
                    }
                    else 
                    {
                        val = (String)token;
                    }
                    //System.out.print(val + " ");
                    tokens.add(val);
                    //System.out.println(token);
                    token = csvt.token();
                    if((lastToken == null) && csvt.isNewline(token))
                    {
                    	tokens.add(new String(""));
                    }
                };
                
                if(tokens.size() > 0) 
                {
                    line.add(tokens);
                }
                token = csvt.token();
            }
        }
        catch(Exception e) 
        {
            System.err.println(e.getMessage());
            e.printStackTrace();            
        }
    }
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String in_fileName) {
        fileName = in_fileName;
    }
    public void showContents(){
        Enumeration line_it = line.elements();
        while(line_it.hasMoreElements()) {
            Vector tokens = (Vector)line_it.nextElement();
            Enumeration tokens_it = tokens.elements();
                while(tokens_it.hasMoreElements()) {
                    System.out.print((String)tokens_it.nextElement());
                    if(tokens_it.hasMoreElements()) {
                        System.out.print(",");
                    }
                }
                System.out.print(": Total Elements " + tokens.size());
                System.out.println();
        }
        System.out.println("total number of lines " + getTotalLines());
    }
    public void showContents2() {
        int nLines = getTotalLines();
        for(int i=0;i< nLines;i++)
            {
                int nTokens = getTotalTokensinLine(i);
                for(int j=0;j<nTokens;j++)
                    {
                        System.out.print(getToken(i,j)); 
                        if(j < (nTokens - 1) ) {
                            System.out.print(",");
                        }
                    }
                System.out.println();
            }
    }
    /**
       line_no and token_no are zero index based.
     */
    public String getToken(int line_no,int token_no) {
        Vector nLine = (Vector)line.elementAt(line_no);
        return (String)nLine.elementAt(token_no);
    }
    
    public String getLine(int line_no)
    {
	    Vector nLine = (Vector)line.elementAt(line_no);
	    String line = "";
	    int nTokens = getTotalTokensinLine(line_no);
                for(int j=0;j<nTokens;j++)
                    {
                        line = line + getToken(line_no,j); 
                        if(j < (nTokens - 1) ) {
                            line += "," ;
                        }
                    }
	    return line;
    }
    public int getTotalLines() {
        return line.size();
    }
    /**
       line_no has zero based index
     */
    public int getTotalTokensinLine(int line_no) {
        
        return ((Vector)line.elementAt(line_no)).size();
    }
    
}
