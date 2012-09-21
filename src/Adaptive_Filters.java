import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.text.*;
import java.awt.*;
import java.awt.event.*;
import ij.plugin.frame.*;
import java.util.*;

/**     
    This plugin implements the adaptive filters for the reduction of the local noise in one image 
    @author Leonardi Rosa-BS degree in Computer Science 
    Advisor: Prof. Battiato Sebastiano
    Organization: University of Catania - ITALY
*/
public class Adaptive_Filters extends PlugInFrame implements ActionListener, ItemListener, TextListener
{
    private Font monoFont = new Font("Monospaced", Font.PLAIN, 12);
    private Font sans = new Font("SansSerif", Font.BOLD, 12);
    private Button preview;
    private Button apply;
    private TextField varField;
    private double varianceNoise=1000;
    private long time;
    private ImageProcessor ip;  
    private ImagePlus imp;
    private int type;
    private Choice choice;
    private String selected;
    private int DimKernel=5;
    private int SMax=DimKernel+6; 
    private String[] filters={"Adaptive filter","Adaptive median filter"};
    
    public Adaptive_Filters() 
    {
        super("Adaptive Filters");
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
        Label title= new Label("Adaptive filters", Label.CENTER);
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
        selected="Adaptive filter";
        choice.select(0);
        panelChoice.add(choice);
        add(panelChoice);
            
        // panel variance of the noise
        Panel panelVar = new Panel();
        Label varLab = new Label("Estimate of the variance of the noise: ");
        varLab.setFont(new Font("SansSerif", Font.PLAIN, 12));
        panelVar.add(varLab);
        int digits=0;
        varField = new TextField(IJ.d2s(varianceNoise, digits),5);
        varField.addTextListener(this);
        panelVar.add(varField);

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
        
        // panel Info & Help
        Panel panelInfoHelp = new Panel();
        Button ih = new Button("Help & Info");
        ih.addActionListener(new InfoHelpViewerAF());
        panelInfoHelp.add(ih);
        
        // panel container
        Panel container=new Panel();
        container.setLayout(new GridLayout(4,1));
        c.gridy = y++;
        c.insets = new Insets(10, 0, 0, 10);
        gridbag.setConstraints(container, c);
        container.add(panelVar);
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
            AdaptiveF af=null;
            ImagePlus processed = null;
            String chosen="";
            time=System.currentTimeMillis();
            if(selected.equals("Adaptive filter"))
                chosen="Adaptive filter";
            else if(selected.equals("Adaptive median filter"))
                chosen="Adaptive median filter";
            else chosen=selected;
            boolean dialog=showDialog();
            if (dialog==false)
            {
                IJ.error("Error","You must insert an entire number positive greater or equal odd number to 2 ");
                return;
            }
            if(chosen.equals("Adaptive filter"))
                IJ.showMessage("Filter: "," "+chosen+" "+DimKernel+"x"+DimKernel+" with estimate of the variance of the noise equal to: "+varianceNoise+" ");
            else 
                if(chosen.equals("Adaptive median filter"))
                {
                    dialog=showDialog2();
                    if (dialog==false)
                    {
                        IJ.error("Error","You must insert an entire number positive greater or equal odd number to "+DimKernel);
                        return;
                    }
                    IJ.showMessage("Filter: "," "+chosen+" "+DimKernel+"x"+DimKernel+" with S_max= "+SMax);
                }
            af= new AdaptiveF(imp, imp.getTitle(),chosen,DimKernel,varianceNoise,SMax);
            if(type==ImagePlus.GRAY8)
                processed=af.createProcessed8bitImage(); 
            else if(type==ImagePlus.GRAY16)
                processed=af.createProcessed8bitImage();
            else if(type==ImagePlus.GRAY32)
                processed=af.createProcessed8bitImage();
            else if(type==ImagePlus.COLOR_256)
                processed=af.createProcessed8bitImage();
            else if(type==ImagePlus.COLOR_RGB)
                processed=af.createProcessedRGBImage();
            time=System.currentTimeMillis()-time;
            double time_sec=time/1000.0;
            PreviewImageAF prew=new PreviewImageAF(processed,time_sec);
        }
        if(source==apply)
        {
            Undo.setup(Undo.COMPOUND_FILTER, imp);
            WindowManager.setTempCurrentImage(imp);
            AdaptiveF af=null;
            String chosen="";
            if(selected.equals("Adaptive filter"))
                chosen="Adaptive filter";
            else 
                if(selected.equals("Adaptive median filter"))
                    chosen="Adaptive median filter";  
            else chosen=selected;
            boolean dialog=showDialog();
            if (dialog==false)
            {
                IJ.error("Error","You must insert an entire number positive greater or equal odd number to 2 ");
                return;
            }
            if(chosen.equals("Adaptive filter"))
                IJ.showMessage("Filter: "," "+chosen+" "+DimKernel+"x"+DimKernel+" with estimate of the variance of the noise equal to: "+varianceNoise+" ");
            else 
                if(chosen.equals("Adaptive median filter"))
                IJ.showMessage("Filter: "," "+chosen+" "+DimKernel+"x"+DimKernel+" ");
            af= new AdaptiveF(imp, imp.getTitle(),chosen,DimKernel,varianceNoise,SMax);
            if(type==ImagePlus.GRAY8)
                af.createProcessed8bitImage(imp); 
            else if(type==ImagePlus.GRAY16)
                af.createProcessed8bitImage(imp);
            else if(type==ImagePlus.GRAY32)
                af.createProcessed8bitImage(imp);
            else if(type==ImagePlus.COLOR_256)
                af.createProcessed8bitImage(imp);
            else if(type==ImagePlus.COLOR_RGB)
                af.createProcessedRGBImage(imp);
            imp.updateAndRepaintWindow();
            imp.changes=true;
            Undo.setup(Undo.COMPOUND_FILTER_DONE, imp);
            close();
        }
    }
    public boolean showDialog()
    {
        GenericDialog gd = new GenericDialog("Dimension of the Kernel kxk");
        gd.addNumericField("K = ",DimKernel,0);
        gd.showDialog();
        if (gd.wasCanceled())
            return false;
        DimKernel=(int)gd.getNextNumber();
        if((DimKernel<0)||(DimKernel<=2)||(DimKernel%2==0))
        {
            DimKernel=3;    
            return false;
        }
        else
            return true;
    }
    public boolean showDialog2()
    {
        GenericDialog gd = new GenericDialog("Dimension of the S max:");
        gd.addNumericField("S_max = ",SMax,0);
        gd.showDialog();
        if (gd.wasCanceled())
            return false;
        SMax=(int)gd.getNextNumber();
        if((SMax<0)||(SMax%2==0)||(SMax<DimKernel))
        {
            SMax=DimKernel;    
            return false;
        }
        else
            return true;
    }
    public void itemStateChanged(ItemEvent e)
    {
        selected=choice.getSelectedItem();  
    }
    public void textValueChanged(TextEvent e) 
    {
        TextComponent tc = (TextComponent)e.getSource();
        if(tc==varField)
        {
            try
            {
                varianceNoise=Double.parseDouble(varField.getText());  
            }
            catch(NumberFormatException exp)
            {
                IJ.error("Number Format Exception","You must enter a number");  
            }
        }
    }
}

class InfoHelpViewerAF implements ActionListener
{
    private String description="HELP ABOUT \"Adapive Filters\"\n\n"+
            "This plugin implements the adaptive filters for the reduction of the local noise in one image \n"+
            "User can choose one of the adaptive filters specified in the combo box and to insert the dimension of the kernel to apply.\n"+
            "Here is a brief description of the steps to do:\n"+
            "1) Choose a standard filter using the combo box;\n"+
            "2) Click on the button \"Preview\" to view as the image would be if it was processed according to the chosen filter;\n"+
            "3) To insert the dimension of the kernel to apply \n"+
            "4) Click on the button \"Apply\" to definitively apply the current filtering to the image inserting the dimension of the filter like before;\n\n"+
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
        TextWindow tw = new TextWindow("About \" Adaptive Filters \"", sb.toString(), 700, 500);
        tw.show();
    }
}

class PreviewImageAF implements ActionListener
{
    private ImageCanvas canvas;
    private Panel panelTime;
    private ImagePlus im;
        
    public PreviewImageAF(ImagePlus img, double time)
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

class AdaptiveF
{
    private ImagePlus imp;
    private ImageProcessor ip;
    private int width;
    private int height;
    private String title, type; 
    private int Dim_kernel;//dimension of the kernel
    private double var_noise;//estimate of the variance of the noise
    private int S_max;
    public AdaptiveF(ImagePlus image, String t, String adaptive, int k, double vn, int smax) 
    {
	    imp=image;
        ip=imp.getProcessor();
        width=ip.getWidth();
        height=ip.getHeight();
        title=t;
        type=adaptive;
        Dim_kernel=k;
        var_noise=vn;
        S_max=smax;
    }
    /**
    * Converts a 1D array into a 2D array.
    *
    * @param input The input image 1D array
    * @param width Width of the input image
    * @param height Height of the input image
    * @return the newly created 2D array [width][height]
    */
    public int[][] generateInputArrays(int[] input, int width, int height)
    {
        int[][] arrays = new int [width][height];
        for(int i=0;i<width;++i)
            for(int j=0;j<height;++j)
	            arrays[i][j] = input[i+(j*width)];
        return arrays;
    }
    /**
    * Converts a 2D array into a 1D array.
    *
    * @param outputArrays the image 2D array
    * @param width Width of the image
    * @param height Height of the image
    * @return the new 1D array [width*height]
    */
    public int[] generateOutputArray(int[][] outputArrays, int width, int height)
    {
        int [] output = new int [width*height];
        for(int i=0;i<width;++i)
            for(int j=0;j<height;++j)
	            output[i+(j*width)] = outputArrays[i][j];
        return output;
    }
   /**
    * Calculates the mean of a kxk pixel neighbourhood (including centre pixel).
    *
    * @param input The input image 2D array
    * @param k Dimension of the kernel
    * @param w The image width
    * @param h The image height
    * @param x The x coordinate of the centre pixel of the array
    * @param y The y coordinate of the centre pixel of the array
    * @return The mean of the kxk pixels
    */ 
    public double MeanLocal(int[][] input, int k,int w, int h, int x, int y) 
    {
        double sum=0;
        double number = 0;
        for(int j=0;j<k;++j)
        {
            for(int i=0;i<k;++i)
            {
	            if(((x-1+i)>=0) && ((y-1+j)>=0) && ((x-1+i)<w) && ((y-1+j)<h))
                {
                    sum = sum+input[x-1+i][y-1+j];
                    ++number;
                }
            }
        }
        if(number==0) 
            return 0;
        return (sum/number);
    }
    /**
    * Calculates the variance of a kxk pixel neighbourhood (including centre pixel).
    *
    * @param input The input image 2D array
    * @param k Dimension of the kernel
    * @param w The image width
    * @param h The image height
    * @param x The x coordinate of the centre pixel of the array
    * @param y The y coordinate of the centre pixel of the array
    * @param m The mean of a kxk pixel neighbourhood 
    * @return The variance of the kxk pixels
    */ 
    public double VarianceLocal(int[][] input, int k,int w, int h, int x, int y, double m) 
    {
        double sum=0;
        double number = 0;
        for(int j=0;j<k;++j)
        {
            for(int i=0;i<k;++i)
            {
	            if(((x-1+i)>=0) && ((y-1+j)>=0) && ((x-1+i)<w) && ((y-1+j)<h))
                {
                    sum = sum+Math.pow((input[x-1+i][y-1+j]-m),2);
                    ++number;
                }
            }
        }
        if(number==0) 
            return 0;
        return (sum/number);
    }
    /**
    * Calculates the median of a kxk pixel neighbourhood (including centre pixel).
    *
    * @param input The input image 2D array
    * @param k Dimension of the kernel
    * @param w The image width
    * @param h The image height
    * @param x The x coordinate of the centre pixel of the array
    * @param y The y coordinate of the centre pixel of the array
    * @return The median of the kxk pixels
    */ 
    public int median(int[][] input, int k,int w, int h, int x, int y) 
    {
        int[] supp=new int[k*k];
        int t=0;
        int number = 0;
        for(int j=0;j<k;++j)
        {
            for(int i=0;i<k;++i)
            {
	            if(((x-1+i)>=0) && ((y-1+j)>=0) && ((x-1+i)<w) && ((y-1+j)<h))
                {
                    supp[t]=input[x-1+i][y-1+j];
                    t++;
	                ++number;
	            }
            }
        }
        if(number==0) 
            return 0;
        Arrays.sort(supp);
        return supp[((k*k-1)/2)];
    }
    /**
    * Calculates the maximum of a kxk pixel neighbourhood (including centre pixel).
    *
    * @param input The input image 2D array
    * @param k Dimension of the kernel
    * @param w The image width
    * @param h The image height
    * @param x The x coordinate of the centre pixel of the array
    * @param y The y coordinate of the centre pixel of the array
    * @return The maximum of the kxk pixels
    */ 
    public int max(int[][] input, int k, int w, int h, int x, int y)
    {
        int[] supp=new int[k*k];
        int t=0;
        int number = 0;
        for(int j=0;j<k;++j)
        {
            for(int i=0;i<k;++i)
            {
	            if(((x-1+i)>=0) && ((y-1+j)>=0) && ((x-1+i)<w) && ((y-1+j)<h))
                {
                    supp[t]=input[x-1+i][y-1+j];
                    t++;
	                ++number;
	            }
            }
        }
        if(number==0) 
            return 0;
        Arrays.sort(supp);
        return supp[k*k-1];
    }
    /**
    * Calculates the minimum of a kxk pixel neighbourhood (including centre pixel).
    *
    * @param input The input image 2D array
    * @param k Dimension of the kernel
    * @param w The image width
    * @param h The image height
    * @param x The x coordinate of the centre pixel of the array
    * @param y The y coordinate of the centre pixel of the array
    * @return The minimum of the kxk pixels
    */ 
    public int min(int[][] input, int k,int w, int h, int x, int y) 
    {
        int[] supp=new int[k*k];
        int t=0;
        int number = 0;
        for(int j=0;j<k;++j)
        {
            for(int i=0;i<k;++i)
            {
	            if(((x-1+i)>=0) && ((y-1+j)>=0) && ((x-1+i)<w) && ((y-1+j)<h))
                {
                    supp[t]=input[x-1+i][y-1+j];
                    t++;
	                ++number;
	            }
            }
        }
        if(number==0) 
            return 0;
        Arrays.sort(supp);
        return supp[0];
    }
    /**
    * Takes an image in 2D array form and it applies the adaptive filter with the specified dimension of the kernel from the user 
    * @param input the input image
    * @param k Dimension of the kernel
    * @param width Width of the input image
    * @param height Height of the output image
    * @return the new filtered image 2D array
    */
    public int[][] ApplyAdaptiveFilter(int[][] input, int k,int width, int height)
    {
        double mean_l=0;
        double var_l=0;
        int [][] outputArrays = new int [width][height];
        int value=0;
        for(int j=0;j<height;++j)
            for(int i=0;i<width;++i)
            {
                mean_l=MeanLocal(input,k,width,height,i,j);
                var_l=VarianceLocal(input,k,width,height,i,j,mean_l);
                double ratio;
                if(var_noise>var_l)
                    ratio=1;
                else
                    ratio=var_noise/var_l;
                value=input[i][j]-(int)(ratio*(input[i][j]-mean_l));
                if(value<0)
                    value=0;
                if(value>255)
                    value=255;
                outputArrays[i][j] = value;
            }
        return outputArrays;
    }
    /**
    * Takes an image in 2D array form and it applies the adaptive median filter with the specified dimension of the kernel from the user 
    * @param input the input image
    * @param k Dimension of the kernel
    * @param width Width of the input image
    * @param height Height of the output image
    * @return the new filtered image 2D array
    */
    public int[][] ApplyAdaptiveMedianFilter(int[][] input, int k,int width, int height)
    {
        int z_min;
        int z_max;
        int z_med;
        int z;
        boolean supp=false;
        int [][] outputArrays = new int [width][height];
        for(int j=0;j<height;++j)
            for(int i=0;i<width;++i)
            {
                int dim=k;
                int value=0;
                while(dim<S_max+1)
                {  
                    z_min=min(input,dim,width,height,i,j);
                    z_max=max(input,dim,width,height,i,j);
                    z_med=median(input,dim,width,height,i,j);
                    z=input[i][j];
                    int A1=z_med-z_min;
                    int A2=z_med-z_max;
                    if((A1>0)&&(A2<0))
                    {  
                        int B1=z-z_min;
                        int B2=z-z_max;
                        if((B1>0)&&(B2<0))
                            value=z;
                        else 
                            value=z_med;
                        break;
                    }
                    else
                    {
                        dim=dim+2;
                        if(dim<=S_max)
                            continue;
                        else 
                        {
                            value=z;
                            break;
                        }
                    }
                }
                if(value<0)
                    value=0;
                if(value>255)
                    value=255;
                outputArrays[i][j] = value;
            }
        return outputArrays;
    }
    public byte[] applyTransform(byte[] pixels)
    {
        byte[] output=new byte[pixels.length];
        int[] tmp=new int[pixels.length];
        for (int i=0;i<pixels.length;i++)
            tmp[i]= pixels[i]&0xff;
        int[][] supp1;
        int[][] supp2;
        int[] supp3=new int[pixels.length];
        if(type.equals("Adaptive filter"))
        {
            supp1=generateInputArrays(tmp,width,height);
            supp2=ApplyAdaptiveFilter(supp1,Dim_kernel,width,height);
            supp3=generateOutputArray(supp2,width,height);
        }
        else if(type.equals("Adaptive median filter"))
        {
            supp1=generateInputArrays(tmp,width,height);
            supp2=ApplyAdaptiveMedianFilter(supp1,Dim_kernel,width,height);
            supp3=generateOutputArray(supp2,width,height);
        }
        for(int j=0;j<supp3.length;j++)
            output[j]=(byte)supp3[j];
        return output;
    }
    public int[] applyTransform(int[] pixels)
    {
        int[] output=new int[pixels.length];
        int[] output_red=new int[pixels.length];;
        int[] output_green=new int[pixels.length];;
        int[] output_blue=new int[pixels.length];;
        int[] tmp_red=new int[pixels.length];
        int[] tmp_green=new int[pixels.length];
        int[] tmp_blue=new int[pixels.length];
        for (int i=0;i<pixels.length;i++)
        {
            int c=pixels[i];
            tmp_red[i]=(c&0xff0000)>>16;
            tmp_green[i]=(c&0x00ff00)>>8;
            tmp_blue[i]=c&0x0000ff;
        }
        int[][] supp1_red;
        int[][] supp1_green;
        int[][] supp1_blue;
        int[][] supp2_red;
        int[][] supp2_green;
        int[][] supp2_blue;
        if(type.equals("Adaptive filter"))
        {
            supp1_red=generateInputArrays(tmp_red,width,height);
            supp1_green=generateInputArrays(tmp_green,width,height);
            supp1_blue=generateInputArrays(tmp_blue,width,height);
            supp2_red=ApplyAdaptiveFilter(supp1_red,Dim_kernel,width,height);
            supp2_green=ApplyAdaptiveFilter(supp1_green,Dim_kernel,width,height);
            supp2_blue=ApplyAdaptiveFilter(supp1_blue,Dim_kernel,width,height);
            output_red=generateOutputArray(supp2_red,width,height);
            output_green=generateOutputArray(supp2_green,width,height);
            output_blue=generateOutputArray(supp2_blue,width,height);
        }
        else if(type.equals("Adaptive median filter"))
        {
            supp1_red=generateInputArrays(tmp_red,width,height);
            supp1_green=generateInputArrays(tmp_green,width,height);
            supp1_blue=generateInputArrays(tmp_blue,width,height);
            supp2_red=ApplyAdaptiveMedianFilter(supp1_red,Dim_kernel,width,height);
            supp2_green=ApplyAdaptiveMedianFilter(supp1_green,Dim_kernel,width,height);
            supp2_blue=ApplyAdaptiveMedianFilter(supp1_blue,Dim_kernel,width,height);
            output_red=generateOutputArray(supp2_red,width,height);
            output_green=generateOutputArray(supp2_green,width,height);
            output_blue=generateOutputArray(supp2_blue,width,height);
        }
        for(int j=0;j<output.length;j++)
            output[j]=((output_red[j]&0xff)<<16)+((output_green[j]&0xff)<<8)+(output_blue[j]&0xff);
        return output;
    }
    public ImagePlus createProcessed8bitImage()
    {
        ImageProcessor processed_ip = ip.crop();
        ImagePlus processed= imp.createImagePlus();
        processed.setProcessor(title, processed_ip);
        byte[] processed_pixels = (byte[])processed_ip.getPixels();
        byte[] output=applyTransform(processed_pixels);
        for (int i=0; i<output.length; i++) 
          processed_pixels[i]=output[i];
        return processed;
    }
    public void createProcessed8bitImage(ImagePlus img)
    {
        byte[] processed_pixels = (byte[])(img.getProcessor()).getPixels();
        byte[] output=applyTransform(processed_pixels);
        for (int i=0; i<output.length; i++) 
          processed_pixels[i]=output[i];
    }
    public ImagePlus createProcessedRGBImage()
    {
        ImageProcessor processed_ip = ip.crop();
        ImagePlus processed= imp.createImagePlus();
        processed.setProcessor(title, processed_ip);
        int[] processed_pixels = (int[])processed_ip.getPixels();
        int[] output= applyTransform(processed_pixels);
        for (int i=0; i<output.length; i++) 
          processed_pixels[i]=output[i];
        return processed;
    }
    public void createProcessedRGBImage(ImagePlus img)
    {
        int[] processed_pixels = (int[])(img.getProcessor()).getPixels();
        int[] output= applyTransform(processed_pixels);
        for (int i=0; i<output.length; i++) 
          processed_pixels[i]=output[i];
    }
}



