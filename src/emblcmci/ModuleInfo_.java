package emblcmci;


import ij.IJ;
import ij.plugin.PlugIn;

public class ModuleInfo_ implements PlugIn {

	@Override
	public void run(String arg) {
		if (arg.equals("about")) {
			showAbout(); 
			return;
		}
	}

	public void showAbout() {
		IJ.showMessage("CMCI course modules",
			"<html>" +
			"CMCI course module plugin is a collection of <br>" +
			"plugins written by the following people:<br>" + 
			"<br>" + 
			"<b>Canny Edge Detector, LoG</b><br>" +
			"... by Carmelo Pulvirenti<br><br>" +
			"<b>Contrast Stretching, Logarithm, Power Law</b><br>" +
			"... by Bosco Camillo<br><br>" +
			"<b>Harris Courner Detector</b><br>" +
			"... by Mariagrazia Messina<br><br>" +
			"<b>BandControl, High Pass Filter, Low Pass Filter, <br>" +
			"Laplacian Filter</b><br>" + 
			"... by Annalisa Cappello<br><br>" +
			"<b>Adaptive Filters, BandPass BandReject filters, " + 
			"Mean Filter, Order Statistics</b><br>" +
			"... Leonardi Rosa<br><br>" +
			"<b>Morphological Operators</b><br>" +
			"... by Salvatore Aglieco<br><br>" +
			"All the plugins down to here were developed under the supervision of <br>" +
			"Prof. Sebastiano Battiato @ Univ. Catania, Italy<br><br>" +
			"each plugin is available at <br>" +
			"<a href='http://svg.dmi.unict.it/iplab/imagej/'>http://svg.dmi.unict.it/iplab/imagej/</a><br><br><br>" +
			"<b>CLAHE (Contrast Limited Adaptive Histogram Equalization)</b><br>" +
			"was written by<br>" + 
			"Stephan Saalfeld, MPICBG Dresden<br>" +
			"available in <a href='http://fiji.sc'>the Fiji site</a>."
			);
	}
	//unused
	public void showAboutSimple() {
		IJ.showMessage("CMCI course modules",
			"<html>" +
			"CMCI course module plugin is a collection of <br>" +
			"plugins written by the following people:\n" + 
			"\n" + 
			"Canny Edge Detector, LoG\n" +
			"... by Carmelo Pulvirenti\n\n" +
			"Contrast Stretching, Logarithm, Power Law\n" +
			"... by Bosco Camillo\n\n" +
			"Harris Courner Detector\n" +
			"... by Mariagrazia Messina\n\n" +
			"BandControl, High Pass Filter, Low Pass Filter, \n" +
			"Laplacian Filter\n" + 
			"... by Annalisa Cappello\n\n" +
			"Adaptive Filters, BandPass BandReject filters, " + 
			"Mean Filter, Order Statistics\n" +
			"... Leonardi Rosa\n\n" +
			"Morphological Operators\n" +
			"... by Salvatore Aglieco\n\n" +
			"All the plugins down to here were developed under the supervision of \n" +
			"Prof. Sebastiano Battiato @ Univ. Catania, Italy\n\n" +
			"each plugin is available at \n" +
			"http://svg.dmi.unict.it/iplab/imagej/\n\n\n" +
			"CLAHE (Contrast Limited Adaptive Histogram Equalization) " +
			"was written by\n" + 
			"Stephan Saalfeld, MPICBG Dresden\n" +
			"available in the Fiji site, http://fiji.sc"
			);
	}


}
