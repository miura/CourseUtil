package emblcmci;

import ij.io.DirectoryChooser;
import ij.plugin.PlugIn;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Download a file from URL 
 * 
 * @author smdbanana (http://ameblo.jp/smd310/entry-10780602455.html)
 * @author Kota Miura (extended)
 * referred to
 * 	http://stackoverflow.com/questions/8253852/how-to-download-a-zip-file-from-a-url-and-store-them-as-zip-file-only
 * 
 */
public class URLSaver implements PlugIn{

	private static String srcURL;
	private static String destPath;

	public static void main(String[] args) {
        // args[0] = sourc URL 
        // args[1] = destination path, directory
//		srcURL = args[0];
//		destPath = args[1];
		//testing
		srcURL = "http://cmci.embl.de/sampleimages/spindle-frames.zip";
		destPath = "/Users/miura/Downloads";

		//run(srcURL, destPath);
		getDestinationDialog(srcURL);
    }
	
	public static void getDestinationDialog(String srcURL){
		DirectoryChooser opd = new DirectoryChooser("Set Destination Folder...");
		String destPath = opd.getDirectory();
		if (destPath != null)
			run(srcURL, destPath);
	}
	
	@Override
	public void run(String arg) {
		getDestinationDialog(arg);
	}
	
    public static void run(String src, String dest){
    	
    	String[] srcA = src.split(File.separator); 	
    	String filename = srcA[srcA.length-1];   	
    	dest = dest + filename;
		System.out.println(dest);  	
        try {
            download(src, dest);
        }
        catch (MalformedURLException e) {	//TODO
            e.printStackTrace(); 
        }
        catch (FileNotFoundException e) {	//TODO
            e.printStackTrace();
        }
        catch (IOException e) {	//TODO
            e.printStackTrace();
        }
        catch (Exception e) {	//TODO
            e.printStackTrace();
        }
    }

    /**
     * @param strUrl source URL 
     * @param outputPath destination path
     * @throws MalformedURLException
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void download(String strUrl, String outputPath) throws MalformedURLException, FileNotFoundException,
            IOException {  	
        URL url = new URL(strUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        InputStream in = connection.getInputStream();
        FileOutputStream out = new FileOutputStream(outputPath);        
        //FileOutputStream fos = new FileOutputStream(outputPath);
        //InputStream is = url.openStream();
//        while (true) {
//            int read = is.read();
//            if (read == -1)
//                break;
//            fos.write(read);
//        }
//        fos.close();
        copy(in, out, 1024);
        out.close();
        in.close();
    }
    public static void copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        byte[] buf = new byte[bufferSize];
        int n = input.read(buf);
        while (n >= 0) {
          output.write(buf, 0, n);
          n = input.read(buf);
        }
        output.flush();
      }

}