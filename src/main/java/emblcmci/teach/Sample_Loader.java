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
        "<html>" +
				"<b>EMBL Course Sample Images (2014-03-27) </b><br>" +
				"Kota Miura<br>" +
        "<a href=\"http://cmci.embl.de\">CMCI, EMBL</a><br>" +
				"<br>" +
				"A collection of Sample Images for use in practical courses.<br>" +
				"<br>" +
				"<b>FISH2D.tif</b><br>" +
        "... kindly provided by Edouard Bertrand & Florian Mueller<br>" +
        "... The original 3D stack could be downloaded from the following site:<br>" +
        "... <a href=\"https://code.google.com/p/fish-quant/\">FISH-quant</a><br>" +
        "<br>" +
        "<b>NPC (Nuclear Pore Complex) images</b><br>" + 
        "... kindly provided by Andrea Boni<br>" +
        "<br>" +
        "<b>centrosomes.lsm</b><br>" + 
        "... kindly provided by Mayumi Isokane<br>" +
				"<br>" +
				"For more information, visit course section in<br>" +
				"<br>" +
				"<a href'http://cmci.embl.de'>cmci.embl.de</a>"	
			);
	}
}
