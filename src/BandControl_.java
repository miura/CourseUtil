import ij.*;
import ij.process.ImageProcessor.*;
import ij.process.*;
import ij.process.FHT.*;
import ij.plugin.filter.*;
import ij.plugin.*;
import ij.plugin.FFT.*;
import java.lang.Math.*;
import java.lang.String.*;
import javax.swing.*;
import ij.measure.Calibration;
import ij.gui.*;
import java.awt.*;

public class BandControl_ implements PlugInFilter
{
    private int threshold_min, threshold_max;
    private int M, N, size, w, h;
    private ImagePlus imp;
    private FHT fht;
    private ImageProcessor mask, ipFilter;
    private String filter;
    private boolean displayFilter;


    // method from PlugInFilter Interface
    public int setup(String arg, ImagePlus imp)
    {
        this.imp = imp;
        return DOES_ALL;
    }

    // method from PlugInFilter Interface
    public void run(ImageProcessor ip)
    {
        ip = imp.getProcessor();
        if (showDialog(ip)) filtering(ip,imp);
        IJ.showProgress(1.0);
    }

    // the following method opens a window for users
    boolean showDialog(ImageProcessor ip)
    {
        int dim = 0;
        M = ip.getWidth();
        N = ip.getHeight();
        if (M!=N) dim = (int)(Math.min(M,N)/2);
        else dim = M/2;
        threshold_min = 10;
        threshold_max = 20;
        String[] choices = {"BandPass","BandReject"};
        GenericDialog gd = new GenericDialog("Frequency Filters");
        gd.addChoice("filters: ",choices, "BandPass");
        gd.addNumericField("Minimum Threshold Factor:", threshold_min, 0);
        gd.addNumericField("Maximum Threshold Factor:", threshold_max, 0);
        gd.addCheckbox("Display Filter", displayFilter);
        gd.showDialog();
        if (gd.wasCanceled())
            return false;
        if(gd.invalidNumber())
        {
            IJ.error("Error", "Invalid input number");
            return false;
        }
        int choiceIndex = gd.getNextChoiceIndex();
        filter = choices[choiceIndex];
        threshold_min = (int) gd.getNextNumber();
        threshold_max = (int) gd.getNextNumber();
        displayFilter = gd.getNextBoolean();
        if (threshold_min>=0 && threshold_max<=dim && threshold_min<threshold_max)
            return true;
        else
        {
            GenericDialog gd2;
            boolean flag = true;
            while (flag)
            {
                threshold_min = 10;
                threshold_max = 20;
                JOptionPane.showMessageDialog(null,"error, threshold must belong to [" + 0 + "," + dim + "] and minimum threshold must be less than maximum threshold");
                gd2 = new GenericDialog(" Threshold ");
                gd2.addNumericField("Minimum Threshold Factor:", threshold_min, 0);
                gd2.addNumericField("Maximum Threshold Factor:", threshold_max, 0);
                gd2.showDialog();
                if (gd2.wasCanceled() || gd2.invalidNumber())
                    return false;
                else
                {
                    threshold_min = (int) gd2.getNextNumber();
                    threshold_max = (int) gd2.getNextNumber();
                    if (threshold_min>=0 && threshold_max<=dim && threshold_min<threshold_max)
                        flag = false;
                }
            }
        }
        return true;
    }


    // shows the power spectrum and filters the image
    public void filtering(ImageProcessor ip, ImagePlus imp)
    {
        int maxN = Math.max(M, N);
        size = 2;
        while(size<maxN) size *= 2;
        IJ.runPlugIn("ij.plugin.FFT", "forward");
        h = Math.round((size-N)/2);
        w = Math.round((size-M)/2);
        ImageProcessor ip2 = ip.createProcessor(size, size);  // processor of the padded image
        ip2.fill();
        ip2.insert(ip, w, h);
        if (ip instanceof ColorProcessor)
        {
            ImageProcessor bright = ((ColorProcessor)ip2).getBrightness();
            fht = new FHT(bright);
            fht.rgb = (ColorProcessor)ip.duplicate(); // get a duplication of brightness in order to add it after filtering
        }
        else  fht = new FHT(ip2);
        fht.originalColorModel = ip.getColorModel();
        fht.originalBitDepth = imp.getBitDepth();
        fht.transform();	// calculates the Fourier transformation

        if (filter.equals("BandPass"))       ipFilter = BandPass();
        if (filter.equals("BandReject"))     ipFilter = BandReject();

        fht.swapQuadrants(ipFilter);
        byte[] pixels_id = (byte[])ipFilter.getPixels();
        float[] pixels_fht = (float[])fht.getPixels();

        for (int i=0; i<size*size; i++)
        {
            pixels_fht[i] = (float)(pixels_fht[i]*(pixels_id[i]&255)/255.0);
        }

        mask = fht.getPowerSpectrum();
        ImagePlus imp2 = new ImagePlus("inverse FFT of "+imp.getTitle(), mask);
        imp2.setProperty("FHT", fht);
        imp2.setCalibration(imp.getCalibration());
        doInverseTransform(fht);
    }


    // creates a band-pass filter
    public ImageProcessor BandPass()
    {
        ImageProcessor ip = new ColorProcessor(M,N);
        ip.fill();
        int xcenter = M/2;
        int ycenter = N/2;

        for (int radius=threshold_min; radius<threshold_max;radius++)
        {
            for (double counter = 0; counter < 10; counter = counter + 0.001)
            {
                double x = Math.sin(counter) * radius + xcenter;
                double y = Math.cos(counter) * radius + ycenter;
                ip.putPixel((int)x, (int)y, 255);
            }
        }

        ByteProcessor ip2 = new ByteProcessor(size,size);
        ip2.fill();
        ip2.insert(ip, w, h);
        if (displayFilter) new ImagePlus("Band-pass filter", ip2).show();
        return ip2;
    }


    // creates a band-reject filter
    public ImageProcessor BandReject()
    {
        ImageProcessor ip = new ColorProcessor(M,N);
        ip.setColor(Color.white);
        ip.fill();
        int xcenter = M/2;
        int ycenter = N/2;

        for (int radius=threshold_min; radius<threshold_max;radius++)
        {
            for (double counter = 0; counter < 10; counter = counter + 0.001)
            {
                double x = Math.sin(counter) * radius + xcenter;
                double y = Math.cos(counter) * radius + ycenter;
                ip.putPixel((int)x, (int)y, 0);
            }
        }

        ByteProcessor ip2 = new ByteProcessor(size,size);
        byte[] p = (byte[]) ip2.getPixels();
        for (int i=0; i<size*size; i++) p[i] = (byte)255;
        ip2.insert(ip, w, h);
        if (displayFilter) new ImagePlus("Band-Reject filter", ip2).show();
        return ip2;
    }


    // applies the inverse Fourier transform to the filtered image
    void doInverseTransform(FHT fht)
    {
        fht = fht.getCopy();
        fht.inverseTransform();
        fht.resetMinAndMax();
        ImageProcessor ip2 = fht;
        fht.setRoi(w, h, M, N);
        ip2 = fht.crop();
        
        int bitDepth = fht.originalBitDepth>0?fht.originalBitDepth:imp.getBitDepth();
        switch (bitDepth)
        {
            case 8 :  ip2 = ip2.convertToByte(true); break;
            case 16:  ip2 = ip2.convertToShort(true); break;
            case 24:
                      if (fht.rgb==null || ip2==null)
                      {
                            IJ.error("FFT", "Unable to set brightness");
                            return;
                      }
                      ColorProcessor rgb = (ColorProcessor)fht.rgb.duplicate();
                      rgb.setBrightness((FloatProcessor)ip2);
                      ip2 = rgb;
                      fht.rgb = null;
                      break;
            case 32: break;
        }
        if (bitDepth!=24 && fht.originalColorModel!=null)
            ip2.setColorModel(fht.originalColorModel);
        String title = imp.getTitle();
        if (title.startsWith("FFT of "))
            title = title.substring(7, title.length());
        ip2 = ip2.convertToByte(true);
        ImagePlus imp2 = new ImagePlus("Inverse FFT of "+title, ip2);
        if (imp2.getWidth()==imp.getWidth())
            imp2.setCalibration(imp.getCalibration());
        imp2.show();
    }
}
