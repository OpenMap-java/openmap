import com.bbn.openmap.util.CSVReader;

/**
 * A class to test CSVReader and show contents of a CSV file
 */
public class TestCSVReader {

    public static void Usage() {
	System.out.println("TestCSVReader Comma-Seperated-Value-File.extention");
    }

    public static void main(String args[]) {   
    
    	if (args.length == 0) {
	    Usage();
	    return;
	}
    	
        CSVReader csvr = new CSVReader(args[0]);
        csvr.loadData();
        csvr.showContents();
        System.out.println("ShowContents 2");
        csvr.showContents2();
    }
}
