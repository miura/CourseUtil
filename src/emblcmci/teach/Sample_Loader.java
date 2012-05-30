package emblcmci.teach;
/** For opening sample images for teaching via internet
 * accessing CMCI server
 * 
 */

import java.io.IOException;

import ij.IJ;
import ij.plugin.BrowserLauncher;
import ij.plugin.PlugIn;
import ij.plugin.URLOpener;

public class Sample_Loader implements PlugIn{

	public String loadURL(String filename){
		
		String fullpath = "http://cmci.embl.de/sampleimages/"+filename;
		
		return fullpath;
	}
	
	public void load(String filename){
		URLOpener uo = new URLOpener();
		uo.run(loadURL(filename));		
	}

	@Override
	public void run(String arg) {
//		Sample_Loader sl = new Sample_Loader();
//		sl.load(arg);
		if (arg.equals("about")){
			showAbout(); return;
		}
		if ((arg.endsWith("/")) || (arg.endsWith(".html")) || (arg.endsWith(".htm"))){
			try {
				BrowserLauncher.openURL(arg);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		load(arg);
	}
	public void showAbout() {
		IJ.showMessage("Sample Images Plugin",
				"EMBL Course Sample Images (2012-05-30) \n" +
				"Kota Miura\n" +
				"\n" +
				"Collection of Sample Images for use in practical courses\n" +
				"For more information, visit course section in\n" +
				"\n" +
				"http://cmci.embl.de"	
			);
	}
}
