import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
import ij.text.*;
import ij.plugin.frame.*;
import ij.plugin.ContrastEnhancer;
import ij.measure.*;
import ij.measure.Calibration;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import javax.swing.event.*;
import java.util.*;


/**     
    This plugin implements the most common bandreject and bandpass filters in the frequency domain 
    It is based on Joachim Walter's FFT Filter plugin at "http://rsb.info.nih.gov/ij/plugins/fft.html".
    @author Leonardi Rosa-BS degree in Computer Science 
    Advisor: Prof. Battiato Sebastiano
    Organization: University of Catania - ITALY
*/
public class  Bandpass_Bandreject_Filter extends PlugInFrame implements ActionListener, ItemListener,TextListener
{
    private Font monoFont = new Font("Monospaced", Font.PLAIN, 12);
    private Font sans = new Font("SansSerif", Font.BOLD, 12);
    private Button preview;
    private Button apply;
    private Button show_fft;//show the FFT of the departure image 
    private Button show_filter;//show the filter like image 
    private TextField D0Field;
    private TextField WField;
    private TextField NField;
    private double d0=60;
    private double w=10;
    private double n=4;
    private long time;
    private ImageProcessor ip;  
    private ImagePlus imp;
    private int type;
    private Choice choice;
    private String selected;
    private String[] filters={"Ideal bandreject filter","Ideal bandpass filter","Butterworth bandreject filter","Butterworth bandpass filter","Gaussian bandreject filter","Gaussian bandpass filter"};
    public Bandpass_Bandreject_Filter() 
    {
        super("Bandreject and Bandpass Filters in the Frequency Domain");
        setResizable(true);
    }
    public void run(String arg) 
    {
        imp = WindowManager.getCurrentImage();
        if (imp==null) 
        {
            IJ.beep();
            IJ.showStatus("No image");
            IJ.noImage();
            return;
        }
        ip = imp.getProcessor();
        int imgWidth=ip.getWidth();
        int imgHeight=ip.getHeight();
        type = imp.getType();
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        setLayout(gridbag);
            
        // Panel title
        Panel paneltitle = new Panel();
        Label title= new Label("Bandreject and Bandpass Filters in the Frequency Domain", Label.CENTER);
        title.setFont(sans);
        paneltitle.add(title);
        c.gridx = 0;
        int y = 0;
        c.gridy = y++;
        c.fill = GridBagConstraints.BOTH;
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(5, 10, 0, 10);//top,left,bottom,right
        gridbag.setConstraints(paneltitle, c);
        add(paneltitle);
            
        Panel panelChoice = new Panel();
        c.gridy = y++;
        c.insets = new Insets(5, 10, 0, 10);
        gridbag.setConstraints(panelChoice, c);
        choice = new Choice();
        for (int i=0; i<filters.length; i++)
            choice.addItem(filters[i]);
        choice.addItemListener(this);
        selected="Ideal bandreject filter";
        choice.select(0);
        panelChoice.add(choice);
        add(panelChoice);
        
        // panel Preview
        Panel panelPreview = new Panel();
        preview = new Button("Preview");
        preview.addActionListener(this);
        panelPreview.add(preview);
        
        // panel Apply
        Panel panelApply = new Panel();
        apply = new Button("Apply");
        apply.addActionListener(this);
        panelApply.add(apply);
        
        // panel parameter
        Panel panelPar = new Panel();
        Label dLab = new Label("Cut frequency: ");
        dLab.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panelPar.add(dLab);
        int digits=0;
        D0Field = new TextField(IJ.d2s(d0, digits),4);
        D0Field.addTextListener(this);
        panelPar.add(D0Field);
        
        Label wLab = new Label("Width of the band: ");
        wLab.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panelPar.add(wLab);
        digits=2;
        WField = new TextField(IJ.d2s(w, digits),4);
        WField.addTextListener(this);
        panelPar.add(WField);
        
        Label nLab = new Label("Order of the Butterworth Filter: ");
        nLab.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panelPar.add(nLab);
        digits=0;
        NField = new TextField(IJ.d2s(n, digits),4);
        NField.addTextListener(this);
        panelPar.add(NField);
        
        // panel show DFT
        Panel panelDFT = new Panel();
        show_fft = new Button("Show FFT of the image");
        show_fft.addActionListener(this);
        show_filter = new Button("Show filter");
        show_filter.addActionListener(this);
        panelDFT.add(show_fft);
        panelDFT.add(show_filter);
        
        // panel Info & Help
        Panel panelInfoHelp = new Panel();
        Button ih = new Button("Help & Info");
        ih.addActionListener(new InfoHelpViewerBandFilter());
        panelInfoHelp.add(ih);
        
        // panel container
        Panel container=new Panel();
        container.setLayout(new GridLayout(5,1));
        c.gridy = y++;
        c.insets = new Insets(10, 0, 0, 10);
        gridbag.setConstraints(container, c);
        container.add(panelPar);
        container.add(panelDFT);
        container.add(panelPreview);
        container.add(panelApply);
        container.add(panelInfoHelp);
        add(container);
        
        // - - - - - - - - - - - - - - - - - -
        pack();
        GUI.center(this);
        show();
    }
    
    public void actionPerformed(ActionEvent e)
    {
        Object source=e.getSource();
        if(source==preview)
        {
            BandFilter dft_f=null;
            ImagePlus processed = null;
            String chosen="";
            time=System.currentTimeMillis();
            if(selected.equals("Ideal bandreject filter"))
                chosen="Ideal bandreject filter";
            else if(selected.equals("Ideal bandpass filter"))
                chosen="Ideal bandpass filter";
            else if(selected.equals("Butterworth bandreject filter"))
                chosen="Butterworth bandreject filter";
            else if(selected.equals("Butterworth bandpass filter"))
                chosen="Butterworth bandpass filter";
            else if(selected.equals("Gaussian bandreject filter"))
                chosen="Gaussian bandreject filter";
            else if(selected.equals("Gaussian bandpass filter"))
                chosen="Gaussian bandpass filter";
            else chosen=selected;
            dft_f= new BandFilter(imp, imp.getTitle(),chosen,d0,w,n);
            if((type==ImagePlus.GRAY8)||(type==ImagePlus.GRAY16)||(type==ImagePlus.GRAY32)||(type==ImagePlus.COLOR_256))
                processed=dft_f.createProcessed8bitImage();
            else if(type==ImagePlus.COLOR_RGB)
                processed=dft_f.createProcessedRGBImage();
            time=System.currentTimeMillis()-time;
            double time_sec=time/1000.0;
            PreviewImageBandFilter prew=new PreviewImageBandFilter(processed,time_sec);
        }
        if(source==apply)
        {
            Undo.setup(Undo.COMPOUND_FILTER, imp);
            WindowManager.setTempCurrentImage(imp);
            BandFilter dft_f=null;
            String chosen="";
            if(selected.equals("Ideal bandreject filter"))
                chosen="Ideal bandreject filter";
            else if(selected.equals("Ideal bandpass filter"))
                chosen="Ideal bandpass filter";
            else if(selected.equals("Butterworth bandreject filter"))
                    chosen="Butterworth bandreject filter";  
            else if(selected.equals("Butterworth bandpass filter"))
                    chosen="Butterworth bandpass filter"; 
            else if(selected.equals("Gaussian bandreject filter"))
                    chosen="Gaussian bandreject filter";             
            else if(selected.equals("Gaussian bandpass filter"))
                    chosen="Gaussian bandpass filter";  
            else chosen=selected;
            dft_f = new BandFilter(imp, imp.getTitle(),chosen,d0,w,n);
            if((type==ImagePlus.GRAY8)||(type==ImagePlus.GRAY16)||(type==ImagePlus.GRAY32)||(type==ImagePlus.COLOR_256))
                dft_f.createProcessed8bitImage(imp);
            else if(type==ImagePlus.COLOR_RGB)
                dft_f.createProcessedRGBImage(imp);
            imp.updateAndRepaintWindow();
            imp.changes=true;
            Undo.setup(Undo.COMPOUND_FILTER_DONE, imp);
            close();
        }
        if(source==show_fft)
        {
            BandFilter dft_f=null;
            ImagePlus processed = null;
            String chosen=selected;
            dft_f= new BandFilter(imp, imp.getTitle(),chosen,d0,w,n);
            processed=dft_f.getFFT();
            processed.show();
        }
        if(source==show_filter)
        {
            BandFilter dft_f=null;
            ImagePlus processed = null;
            String chosen=selected;
            dft_f= new BandFilter(imp, imp.getTitle(),chosen,d0,w,n);
            processed=dft_f.getFilter();
            processed.show();
        }
    }
    public void itemStateChanged(ItemEvent e)
    {
        selected=choice.getSelectedItem();  
    }
    public void textValueChanged(TextEvent e) 
    {
        TextComponent tc = (TextComponent)e.getSource();
        if(tc==D0Field)
        {
            try
            {
                d0=Double.parseDouble(D0Field.getText());  
            }
            catch(NumberFormatException exp)
            {
               IJ.error("Number Format Exception","You must enter a number");  
            }
        }
        if(tc==WField)
        {
            try
            {
                w=Double.parseDouble(WField.getText());  
            }
            catch(NumberFormatException exp)
            {
               IJ.error("Number Format Exception","You must enter a number");  
            }
        }
        if(tc==NField)
        {
            try
            {
                n=Double.parseDouble(NField.getText());  
            }
            catch(NumberFormatException exp)
            {
               IJ.error("Number Format Exception","You must enter a number");  
            }
        }
    }
}

class InfoHelpViewerBandFilter implements ActionListener 
{
    private String description="HELP ABOUT \"Bandreject and Bandpass Filters in the Frequency Domain\"\n\n"+
            "This plugin implements the more common Bandreject and Bandpass Filters in the Frequency Domain\n"+
            "It is based on Joachim Walter's FFT Filter plugin at \"http://rsb.info.nih.gov/ij/plugins/fft.html\".\n"+
            "User can choose one of the filters specified in the combo box and to insert the \"frequency of cut\" and the \"order\" of the filter .\n\n"+
            "Here is a brief description of the steps to do:\n"+
            "1) Choose a standard filter using the combo box;\n"+
            "2) Click on the button \"Show FFT of the image\" in order to visualize the FFT of the image to transform; \n"+
            "3) Click on the button \"Show filter\" in order to visualize the filter to apply like an image; \n"+
            "4) To insert the frequency of cut and the order of the filter to apply \n"+
            "5) Click on the button \"Preview\" to view as the image would be if it was processed according to the chosen filter;\n"+
            "6) Click on the button \"Apply\" to definitively apply the current filtering to the image;\n\n"+
            "ABOUT THE AUTHOR\n"+
            "This plugin was implemented by Rosa Leonardi under Professor Sebastiano Battiato's supervision.\n"+
            "It was a part of a project for the Multimedia Course (MSC Program in Computer Science) at University of Catania (ITALY)\n\n"+
            "CONTACT INFO\n"+
            "For further information contact us:\n"+
            "Prof. Battiato Sebastiano, Ph.d. in Computer Science, e-mail: battiato@dmi.unict.it, web: http://www.dmi.unict.it/~battiato\n"+
            "Rosa Leonardi, BS degree in Computer Science, e-mail: angirosa@tiscali.it\n\n"+
            "LINKS\n"+
            "IPLab - Image Processing Laboratory - University of Catania (Italy), web: http://www.dmi.unict.it/~iplab\n"+
            "Department of Mathematics and Computer Science - University of Catania (Italy), web: http://www.dmi.unict.it/";
  
    public void actionPerformed(ActionEvent e) 
    {
        StringBuffer sb = new StringBuffer();
        sb.append(description);
        TextWindow tw = new TextWindow("About Bandreject and Bandpass Filters in the Frequency Domain \"", sb.toString(), 700, 500);
        tw.show();
    }
}

class PreviewImageBandFilter implements ActionListener
{
    private ImageCanvas canvas;
    private Panel panelTime;
    private ImagePlus im;
    
    public PreviewImageBandFilter(ImagePlus img, double time)
    {
        im=img;
        canvas=new ImageCanvas(im);
        ImageWindow imgWin=new ImageWindow(im);
        imgWin.setResizable(false);
        imgWin.setSize(canvas.getSize().width+150,canvas.getSize().height+150);
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        imgWin.setLayout(gridbag);
        
        //Panel Canvas
        c.gridx = 0;
        int y=0;
        c.gridy = y++;
        c.insets = new Insets(10, 10, 0, 10);
        gridbag.setConstraints(canvas, c);
        imgWin.add(canvas);
        
        //Panel Time
        Panel panelTime = new Panel();
        c.gridy = y++;
        c.insets = new Insets(2, 10, 0, 10);
        gridbag.setConstraints(panelTime, c);
        Label timeLab = new Label("Elapsed Time: ");
        timeLab.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panelTime.add(timeLab);
        Label timeLab2 = new Label(new Double(time).toString()+" seconds");
        timeLab2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panelTime.add(timeLab2);
        imgWin.add(panelTime);
        
        Panel panelHisto = new Panel();
        c.gridy = y++;
        c.insets = new Insets(2, 10, 0, 10);
        gridbag.setConstraints(panelHisto, c);
        Button histo = new Button("View Histogram");
        histo.addActionListener(this);
        panelHisto.add(histo);
        imgWin.add(panelHisto);
        imgWin.show();
    }
    public void actionPerformed(ActionEvent e) 
    {
        HistogramWindow isto=new HistogramWindow(im.getTitle(),im,256);
        isto.show();
    }
}


class BandFilter implements Measurements
{
    private ImagePlus imp;
    private ImageProcessor ip;
    private int width;
    private int height;
    private int originalWidth;
    private int originalHeight;

    private String title, type; 
    private float[][] H;//filter function
    private double D0;//frequency of cut
    private double N;//order of the filter 
    private double W;//width of the band
    private FHT fht;
    private FHT fht_filter;
    private boolean padded;
    private int stackSize = 1;
    private int slice = 1;
    
    public BandFilter(ImagePlus image, String t, String filter, double d, double w, double n) 
    {
	    imp=image;
        ip=imp.getProcessor();
        width=ip.getWidth();
        height=ip.getHeight();
        originalWidth=width;
        originalHeight=height;
        title=t;
        type=filter;
        D0=d;
        N=n;
        W=w;
        stackSize = imp.getStackSize();
        fht = newFHT(ip);
    }
   
    /**
    * Calculates the fft of an image
    * @return The ImagePlus representative the fft    
    */
    public ImagePlus getFFT()
    {
        ImagePlus result = doForewardTransform(fht, ip);
        return result;
    }
    public ImagePlus doForewardTransform(FHT fht, ImageProcessor ip) 
    {
        showStatus("Foreward transform");
        fht.transform();
        showStatus("Calculating power spectrum");
        ImageProcessor ps = fht.getPowerSpectrum();
        ImagePlus imp2 = new ImagePlus("FFT of "+imp.getTitle(), ps);
        imp2.setProperty("FHT", fht);
        imp2.setCalibration(imp.getCalibration());
        return imp2;
    }
    /**
    * Calculates the inverse fft of an image
    * @return The ImagePlus representative the inverse fft    
    */
    public ImagePlus doInverseTransform(FHT fht, ImageProcessor ip) 
    {
        fht = fht.getCopy();
        doMasking(fht);
        showStatus("Inverse transform");
        fht.inverseTransform();
        if (fht.quadrantSwapNeeded)
            fht.swapQuadrants();
        fht.resetMinAndMax();
        ImageProcessor ip2 = fht;
        if (fht.originalWidth>0) 
        {
            fht.setRoi(0, 0, fht.originalWidth, fht.originalHeight);
            ip2 = fht.crop();
        }
        int bitDepth = fht.originalBitDepth>0?fht.originalBitDepth:imp.getBitDepth();
        switch (bitDepth) 
        {
            case 8: ip2 = ip2.convertToByte(false); break;
            case 16: ip2 = ip2.convertToShort(false); break;
            case 24:
                showStatus("Setting brightness");
                if (fht.rgb==null || ip2==null) 
                {
                    IJ.error("FFT", "Unable to set brightness");
                    return null;
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
        ImagePlus imp2 = new ImagePlus("Inverse FFT of "+title, ip2);
        if (imp2.getWidth()==imp.getWidth())
            imp2.setCalibration(imp.getCalibration());
        return imp2;
    }
    /**
    * Calculates the ImagePlus of the selected filter
    * @return The ImagePlus of the filter
    */
    public ImagePlus getFilter()
    { 
        int maxN = Math.max(width, height);
        int i = 2;
        while(i<maxN) 
            i *= 2;
        maxN = i;
        H = new float[maxN][maxN];
        filter(maxN);
        ImagePlus supp=NewImage.createFloatImage("Filter Function",maxN,maxN,1,NewImage.FILL_WHITE);
        ImageProcessor filter=supp.getProcessor();
        supp.setProcessor(""+type+" ",filter);
        float[] tmp=generateOutputArray(H, maxN, maxN);
        float[] processed_pixels = (float[])filter.getPixels();  
        for (i=0; i<tmp.length; i++)
            processed_pixels[i]=tmp[i];
        return supp;
    }
    /**
    * Calculates the values of the filter function
    */
    public void filter(int maxN)
    {
        double dist;
        if(type.equals("Ideal bandreject filter"))
        {
            for(int v=0;v<maxN;v++)
                for(int u=0;u<maxN;u++)
                {
                    dist=distance(u,v,maxN);
                    if((dist<(D0-W/2))||(dist>(D0+W/2)))
                        H[u][v]=1;
                    else 
                        H[u][v]=0;
                }
        }
        else if(type.equals("Ideal bandpass filter"))
        {
            for(int v=0;v<maxN;v++)
                for(int u=0;u<maxN;u++)
                {
                    dist=distance(u,v,maxN);
                    if((dist<(D0-W/2))||(dist>(D0+W/2)))
                        H[u][v]=0;
                    else 
                        H[u][v]=1;
                }
        }
        else if(type.equals("Butterworth bandreject filter"))
        {
           for(int v=0;v<maxN;v++)
                for(int u=0;u<maxN;u++)
                {
                    dist=distance(u,v,maxN);
                    double supp1=dist*W;
                    double supp2=Math.pow(dist,2)-Math.pow(D0,2);
                    double supp3=Math.pow(supp1/supp2,2*N);
                    H[u][v]=(float)(1/(1+supp3));
                }
            
        }
        else if(type.equals("Butterworth bandpass filter"))
        {
           for(int v=0;v<maxN;v++)
                for(int u=0;u<maxN;u++)
                {
                    dist=distance(u,v,maxN);
                    double supp1=dist*W;
                    double supp2=Math.pow(dist,2)-Math.pow(D0,2);
                    double supp3=Math.pow(supp1/supp2,2*N);
                    H[u][v]=(float)(1-(1/(1+supp3)));
                }
           
        }
        else if(type.equals("Gaussian bandreject filter"))
        {
            for(int v=0;v<maxN;v++)
                for(int u=0;u<maxN;u++)
                {
                    dist=distance(u,v,maxN);
                    double supp1=Math.pow(dist,2)-Math.pow(D0,2);
                    double supp2=dist*W;
                    double supp3=-Math.pow(supp1,2);
                    double supp4=2*Math.pow(supp2,2);
                    H[u][v]=(float)(1-Math.exp(supp3/supp4));
                }
        }
        else if(type.equals("Gaussian bandpass filter"))
        {
            for(int v=0;v<maxN;v++)
                for(int u=0;u<maxN;u++)
                {
                    dist=distance(u,v,maxN);
                    double supp1=Math.pow(dist,2)-Math.pow(D0,2);
                    double supp2=dist*W;
                    double supp3=-Math.pow(supp1,2);
                    double supp4=2*Math.pow(supp2,2);
                    H[u][v]=(float)Math.exp(supp3/supp4);
                }
        }
    }
    /**
    * Calculates the distance of the point (u,v) from the origin of the centered frequency rectangle
    * @param u 
    * @param v 
    * @param maxN the width of the function filter
    * @return The distance from the origin    
    */
    public double distance(int u,int v,int maxN)
    {
        double x=Math.pow(u-(maxN/2),2);
        double y=Math.pow(v-(maxN/2),2);
        return Math.sqrt(x+y);
    }
    public byte[] applyTransform(byte[] pixels)
    {
        ImagePlus frequency_image = doForewardTransform(fht, ip); 
      
        ImagePlus supp=getFilter();
        ImageProcessor filter = supp.getProcessor();
        filter =  filter.convertToByte(true);
        fht_filter = newFHT(filter);
        ImagePlus frequency_filter = doForewardTransform(fht_filter, filter); 
        
        ImageProcessor filt=fht_filter.getPowerSpectrum();
        
        float[] fhtPixels = (float[])fht.getPixels();
        fht_filter.swapQuadrants(filt);
        byte[] filterPixels = (byte[])filt.getPixels();
        for (int i=0; i<fhtPixels.length; i++)
            fhtPixels[i] = (float)(fhtPixels[i]*(filterPixels[i]&255)/255.0);
        fht_filter.swapQuadrants(filt);
        
        ip=fht.getPowerSpectrum();
        imp=new ImagePlus(title,ip);
        imp.setProperty("FHT", fht);

        ImagePlus result = doInverseTransform(fht,ip);
        ImageProcessor output=result.getProcessor();
        return (byte[])output.getPixels();
    }
    
    public int[] applyTransform(int[] pixels)
    {
        ImagePlus frequency_image = doForewardTransform(fht, ip); 
      
        ImagePlus supp=getFilter();
        ImageProcessor filter = supp.getProcessor();
        filter =  filter.convertToByte(true);
        fht_filter = newFHT(filter);
        ImagePlus frequency_filter = doForewardTransform(fht_filter, filter); 
        
        ImageProcessor filt=fht_filter.getPowerSpectrum();
        
        float[] fhtPixels = (float[])fht.getPixels();
        fht_filter.swapQuadrants(filt);
        byte[] filterPixels = (byte[])filt.getPixels();
        for (int i=0; i<fhtPixels.length; i++)
        {
            fhtPixels[i] = (float)(fhtPixels[i]*(filterPixels[i]&255)/255.0);
        }
        fht_filter.swapQuadrants(filt);
        
        ip=fht.getPowerSpectrum();
        imp=new ImagePlus(title,ip);
        imp.setProperty("FHT", fht);
        
        ImagePlus result = doInverseTransform(fht,ip);
        ImageProcessor output=result.getProcessor();
            return (int[])output.getPixels();
    }
    public ImagePlus createProcessed8bitImage()
    {
        ImageProcessor processed_ip = ip.crop();
        ImagePlus processed= imp.createImagePlus();
        processed.setProcessor(title, processed_ip);
        byte[] processed_pixels = (byte[])processed_ip.getPixels();
        byte[] output=applyTransform(processed_pixels);
        for (int i=0; i<processed_pixels.length; i++) 
          processed_pixels[i]=output[i];
        return processed;
    }
    public void createProcessed8bitImage(ImagePlus img)
    {
        byte[] processed_pixels = (byte[])(img.getProcessor()).getPixels();
        byte[] output=applyTransform(processed_pixels);
        for (int i=0; i<processed_pixels.length; i++) 
          processed_pixels[i]=output[i];
    }
    public ImagePlus createProcessedRGBImage()
    {
        ImageProcessor processed_ip = ip.crop();
        ImagePlus processed= imp.createImagePlus();
        processed.setProcessor(title, processed_ip);
        int[] processed_pixels = (int[])processed_ip.getPixels();
        int[] output= applyTransform(processed_pixels);
        for (int i=0; i<processed_pixels.length; i++) 
          processed_pixels[i]=output[i];
        return processed;
    }
    public void createProcessedRGBImage(ImagePlus img)
    {
        int[] processed_pixels = (int[])(img.getProcessor()).getPixels();
        int[] output= applyTransform(processed_pixels);
        for (int i=0; i<processed_pixels.length; i++) 
          processed_pixels[i]=output[i];
    }
    public FHT newFHT(ImageProcessor ip) 
    {
        FHT fht;
        if (ip instanceof ColorProcessor) 
        {
            showStatus("Extracting brightness");
            ImageProcessor ip2 = ((ColorProcessor)ip).getBrightness();
            fht = new FHT(pad(ip2));
            fht.rgb = (ColorProcessor)ip.duplicate(); // save so we can later update the brightness
        } 
        else
            fht = new FHT(pad(ip));
        if (padded) 
        {
            fht.originalWidth = originalWidth;
            fht.originalHeight = originalHeight;
        }
        fht.originalBitDepth = imp.getBitDepth();
        fht.originalColorModel = ip.getColorModel();
        return fht;
    }
    public ImageProcessor pad(ImageProcessor ip) 
    {
        originalWidth = ip.getWidth();
        originalHeight = ip.getHeight();
        int maxN = Math.max(originalWidth, originalHeight);
        int i = 2;
        while(i<maxN) i *= 2;
        if (i==maxN && originalWidth==originalHeight) 
        {
            padded = false;
            return ip;
        }
        maxN = i;
        showStatus("Padding to "+ maxN + "x" + maxN);
        ImageStatistics stats = ImageStatistics.getStatistics(ip, MEAN, null);
        ImageProcessor ip2 = ip.createProcessor(maxN, maxN);
        ip2.setValue(stats.mean);
        ip2.fill();
        ip2.insert(ip, 0, 0);
        padded = true;
        Undo.reset();
        return ip2;
    }
    public void showStatus(String msg) 
    {
        if (stackSize>1)
            IJ.showStatus("FFT: " + slice+"/"+stackSize);
        else
            IJ.showStatus(msg);
    }
    public void doMasking(FHT ip) 
    {
        if (stackSize>1)
            return;
        float[] fht = (float[])ip.getPixels();
        ImageProcessor mask = imp.getProcessor();
        mask = mask.convertToByte(false);
        ImageStatistics stats = ImageStatistics.getStatistics(mask, MIN_MAX, null);
        if (stats.histogram[0]==0 && stats.histogram[255]==0)
            return;
        boolean passMode = stats.histogram[255]!=0;
        IJ.showStatus("Masking: "+(passMode?"pass":"filter"));
        mask = mask.duplicate();
        if (passMode)
            changeValues(mask, 0, 254, 0);
        else
            changeValues(mask, 1, 255, 255);
        for (int i=0; i<3; i++)
            mask.smooth();
        ip.swapQuadrants(mask);
        byte[] maskPixels = (byte[])mask.getPixels();
        for (int i=0; i<fht.length; i++) 
        {
            fht[i] = (float)(fht[i]*(maskPixels[i]&255)/255.0);
        }
    }
    public void changeValues(ImageProcessor ip, int v1, int v2, int v3) 
    {
        byte[] pixels = (byte[])ip.getPixels();
        int v;
        for (int i=0; i<pixels.length; i++) 
        {
            v = pixels[i]&255;
            if (v>=v1 && v<=v2)
                pixels[i] = (byte)v3;
        }
    }
    /**
    * Converts a 2D array into a 1D array.
    *
    * @param outputArrays the image 2D array
    * @param width Width of the image
    * @param height Height of the image
    * @return the new 1D array [width*height]
    */
    public float[] generateOutputArray(float[][] outputArrays, int width, int height)
    {
        float[] output = new float[width*height];
        for(int i=0;i<width;++i)
            for(int j=0;j<height;++j)
	            output[i+(j*width)] = outputArrays[i][j];
        return output;
    }
}