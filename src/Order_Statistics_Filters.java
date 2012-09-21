import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
import ij.text.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import ij.plugin.frame.*;
import java.io.*;
import java.util.*;
import javax.swing.event.*;

/**     This plugin implements the more common order-statistics filters
        @author Leonardi Rosa-BS degree in Computer Science 
        Advisor: Prof. Battiato Sebastiano
        Organization: University of Catania - ITALY
*/
public class Order_Statistics_Filters extends PlugInFrame implements ActionListener, ItemListener
{
    private Font monoFont = new Font("Monospaced", Font.PLAIN, 12);
    private Font sans = new Font("SansSerif", Font.BOLD, 12);
    private Button preview;
    private Button apply;
    private long time;
    private ImageProcessor ip;  
    private ImagePlus imp;
    private int type;
    private Choice choice;
    private String selected;
    private int DimKernel=3;
    private int alpha = 3;
    private String[] filters={"Median Filter","Max Filter","Min Filter","Midpoint Filter", "Alpha-trimmed mean Filter"};
    public Order_Statistics_Filters() 
    {
        super("Order-Statistics Filters");
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
        Label title= new Label("Order-Statistics Filters", Label.CENTER);
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
        selected="Median Filter";
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
        
        // panel Info & Help
        Panel panelInfoHelp = new Panel();
        Button ih = new Button("Help & Info");
        ih.addActionListener(new InfoHelpViewerOSF());
        panelInfoHelp.add(ih);
        
        // panel container
        Panel container=new Panel();
        container.setLayout(new GridLayout(4,1));
        c.gridy = y++;
        c.insets = new Insets(10, 0, 0, 10);
        gridbag.setConstraints(container, c);
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
            OrderStatisticsFilter osf=null;
            ImagePlus processed = null;
            String chosen="";
            time=System.currentTimeMillis();
            if(selected.equals("Median Filter"))
                chosen="Median Filter";
            else if(selected.equals("Max Filter"))
                chosen="Max Filter";
            else if(selected.equals("Min Filter"))
                chosen="Min Filter";
            else if(selected.equals("Midpoint Filter"))
                chosen="Midpoint Filter";
            else if(selected.equals("Alpha-trimmed mean Filter"))
                chosen="Alpha-trimmed mean Filter";
            else chosen=selected;
            boolean dialog=showDialog();
            if (dialog==false)
            {
                IJ.error("Error","You must insert an entire number positive greater or equal odd number to 2 ");
                return;
            }
            IJ.showMessage("Filter: "," "+chosen+" "+DimKernel+"x"+DimKernel+" ");
            if(chosen.equals("Alpha-trimmed mean Filter"))
            {
                dialog=showDialogAlpha();
                if (dialog==false)
                {
                    IJ.error("Error","You must insert a greater number of zero and smaller one of "+DimKernel*DimKernel+" ");
                    return;
                }
            }
            osf= new OrderStatisticsFilter(imp, imp.getTitle(),chosen,DimKernel,alpha);
            if(type==ImagePlus.GRAY8)
                processed=osf.createProcessed8bitImage(); 
            else if(type==ImagePlus.GRAY16)
                processed=osf.createProcessed8bitImage();
            else if(type==ImagePlus.GRAY32)
                processed=osf.createProcessed8bitImage();
            else if(type==ImagePlus.COLOR_256)
                processed=osf.createProcessed8bitImage();
            else if(type==ImagePlus.COLOR_RGB)
                processed=osf.createProcessedRGBImage();
            time=System.currentTimeMillis()-time;
            double time_sec=time/1000.0;
            PreviewImageOSF prew=new PreviewImageOSF(processed,time_sec);
        }
        if(source==apply)
        {
            Undo.setup(Undo.COMPOUND_FILTER, imp);
            WindowManager.setTempCurrentImage(imp);
            OrderStatisticsFilter osf=null;
            String chosen="";
            if(selected.equals("Median Filter"))
                chosen="Median Filter";
            else 
                if(selected.equals("Max Filter"))
                    chosen="Max Filter";  
            else 
                if(selected.equals("Min Filter"))
                    chosen="Min Filter";  
            else 
                if(selected.equals("Midpoint Filter"))
                    chosen="Midpoint Filter";  
            else 
                if(selected.equals("Alpha-trimmed mean Filter"))
                    chosen="Alpha-trimmed mean Filter";  
            else chosen=selected;
            boolean dialog=showDialog();
            if (dialog==false)
            {
                IJ.error("Error","You must insert an entire number positive greater or equal odd number to 2 ");
                return;
            }
            IJ.showMessage("Filter: "," "+chosen+" "+DimKernel+"x"+DimKernel+" ");
            if(chosen.equals("Alpha-trimmed mean Filter"))
            {
                dialog=showDialogAlpha();
                if (dialog==false)
                {
                    IJ.error("Error","You must insert a greater number of zero and smaller one of "+DimKernel*DimKernel+" ");
                    return;
                }
            }
            osf = new OrderStatisticsFilter(imp, imp.getTitle(),chosen,DimKernel,alpha);
            if(type==ImagePlus.GRAY8)
                osf.createProcessed8bitImage(imp); 
            else if(type==ImagePlus.GRAY16)
                osf.createProcessed8bitImage(imp);
            else if(type==ImagePlus.GRAY32)
                osf.createProcessed8bitImage(imp);
            else if(type==ImagePlus.COLOR_256)
                osf.createProcessed8bitImage(imp);
            else if(type==ImagePlus.COLOR_RGB)
                osf.createProcessedRGBImage(imp);
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
    public boolean showDialogAlpha()
    {
        GenericDialog gd = new GenericDialog("Value of Alpha:");
        gd.addNumericField("alpha = ",alpha,0);
        gd.showDialog();
        if (gd.wasCanceled())
            return false;
        alpha=(int)gd.getNextNumber();
        if((alpha<0)||(alpha>DimKernel*DimKernel))
        {
            alpha=3;    
            return false;
        }
        else
            return true;
    }
    public void itemStateChanged(ItemEvent e)
    {
        selected=choice.getSelectedItem();  
    }
}

class InfoHelpViewerOSF implements ActionListener
{
    private String description="HELP ABOUT \"Order-Statistics Filters\"\n\n"+
            "This plugin implements the more common order-statistics filters\n"+
            "User can choose one of the order-statistics filters specified in the combo box and to insert the dimension of the kernel to apply.\n"+
            "Here is a brief description of the steps to do:\n"+
            "1) Choose a standard filter using the combo box;\n"+
            "2) Click on the button \"Preview\" to view as the image would be if it was processed according to the chosen filter;\n"+
            "3) To insert the dimension of the kernel to apply \n"+
            "4) Click on the button \"Apply\" to definitively apply the current filtering to the image;\n\n"+
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
        TextWindow tw = new TextWindow("About \" Order-Statistics Filters \"", sb.toString(), 700, 500);
        tw.show();
    }
}

class PreviewImageOSF implements ActionListener
{
    private ImageCanvas canvas;
    private Panel panelTime;
    private ImagePlus im;
        
    public PreviewImageOSF(ImagePlus img, double time)
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

class OrderStatisticsFilter
{
    private ImagePlus imp;
    private ImageProcessor ip;
    private int width;
    private int height;
    private String title, type_filter; 
    private int Dim_kernel;//dimension of the kernel
    private int alpha; 
    public OrderStatisticsFilter(ImagePlus image, String t, String type, int k, int a) 
    {
	    imp=image;
        ip=imp.getProcessor();
        width=ip.getWidth();
        height=ip.getHeight();
        title=t;
        type_filter=type;
        Dim_kernel=k;
        alpha=a;
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
    * Calculates the alpha-trimmed mean of a kxk pixel neighbourhood (including centre pixel).
    *
    * @param input The input image 2D array
    * @param k Dimension of the kernel
    * @param w The image width
    * @param h The image height
    * @param x The x coordinate of the centre pixel of the array
    * @param y The y coordinate of the centre pixel of the array
    * @return The the alpha-trimmed mean of the kxk pixels
    */ 
    public int AlphaTrimmed(int[][] input, int k,int w, int h, int x, int y) 
    {
        int[] supp=new int[k*k];
        int t=0;
        int sum=0;
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
        for(int u=0;u<(alpha/2);u++)
            supp[u]=-1;
        for(int v=number-1;v>number-1-(alpha/2);v--)
            supp[v]=-1;
        for(int l=0;l<number;l++)
        {
            if(supp[l]!=-1)
                sum=sum+supp[l];
        }
        if(sum==0)
            return 0;
        else
            return (sum/(number-alpha));
    }
    /**
    * Takes an image in 2D array form and it applies the median filter with the specified dimension of the kernel from the user 
    * @param input the input image
    * @param k Dimension of the kernel
    * @param width Width of the input image
    * @param height Height of the output image
    * @return the new filtered image 2D array
    */
    public int[][] ApplyMedian(int[][] input, int k,int width, int height)
    {
        int [][] outputArrays = new int [width][height];
        for(int j=0;j<height;++j)
            for(int i=0;i<width;++i)
                outputArrays[i][j] = median(input,k,width,height,i,j);
        return outputArrays;
    }
    /**
    * Takes an image in 2D array form and it applies the max filter with the specified dimension of the kernel from the user 
    * @param input the input image
    * @param k Dimension of the kernel
    * @param width Width of the input image
    * @param height Height of the output image
    * @return the new filtered image 2D array
    */
    public int[][] ApplyMax(int[][] input, int k,int width, int height)
    {
        int [][] outputArrays = new int [width][height];
        for(int j=0;j<height;++j)
            for(int i=0;i<width;++i)
                outputArrays[i][j] = max(input,k,width,height,i,j);
        return outputArrays;
    }
    /**
    * Takes an image in 2D array form and it applies the min filter with the specified dimension of the kernel from the user 
    * @param input the input image
    * @param k Dimension of the kernel
    * @param width Width of the input image
    * @param height Height of the output image
    * @return the new filtered image 2D array
    */
    public int[][] ApplyMin(int[][] input, int k,int width, int height)
    {
        int [][] outputArrays = new int [width][height];
        for(int j=0;j<height;++j)
            for(int i=0;i<width;++i)
                outputArrays[i][j] = min(input,k,width,height,i,j);
        return outputArrays;
    }
    /**
    * Takes an image in 2D array form and it applies the midpoint filter with the specified dimension of the kernel from the user 
    * @param input the input image
    * @param k Dimension of the kernel
    * @param width Width of the input image
    * @param height Height of the output image
    * @return the new filtered image 2D array
    */
    public int[][] ApplyMidpoint(int[][] input, int k,int width, int height)
    {
        int [][] outputArrays = new int [width][height];
        int t1=0;
        int t2=0;
        for(int j=0;j<height;++j)
            for(int i=0;i<width;++i)
            {
                t1=min(input,k,width,height,i,j);
                t2=max(input,k,width,height,i,j);
                outputArrays[i][j] =(t1+t2)/2;
            }
        return outputArrays;
    }
    /**
    * Takes an image in 2D array form and it applies the alpha-trimmed mean filter with the specified dimension of the kernel from the user 
    * @param input the input image
    * @param k Dimension of the kernel
    * @param width Width of the input image
    * @param height Height of the output image
    * @return the new filtered image 2D array
    */
    public int[][] ApplyAlpha(int[][] input, int k,int width, int height)
    {
        int [][] outputArrays = new int [width][height];
        int t1=0;
        int t2=0;
        for(int j=0;j<height;++j)
            for(int i=0;i<width;++i)
                outputArrays[i][j] = AlphaTrimmed(input,k,width,height,i,j);
        return outputArrays;
    }
    public byte[] applyTransform(byte[] pixels)
    {
        byte[] output=new byte[pixels.length];
        int[] tmp=new int[pixels.length];
        for (int i=0;i<pixels.length;i++)
            tmp[i]=pixels[i]&0xff;
        int[][] supp1;
        int[][] supp2;
        int[] supp3=new int[pixels.length];
        if(type_filter.equals("Median Filter"))
        {
            supp1=generateInputArrays(tmp,width,height);
            supp2=ApplyMedian(supp1,Dim_kernel,width,height);
            supp3=generateOutputArray(supp2,width,height);
        }
        else if(type_filter.equals("Max Filter"))
        {
            supp1=generateInputArrays(tmp,width,height);
            supp2=ApplyMax(supp1,Dim_kernel,width,height);
            supp3=generateOutputArray(supp2,width,height);
        }
        else if(type_filter.equals("Min Filter"))
        {
            supp1=generateInputArrays(tmp,width,height);
            supp2=ApplyMin(supp1,Dim_kernel,width,height);
            supp3=generateOutputArray(supp2,width,height);
        }
        else if(type_filter.equals("Midpoint Filter"))
        {
            supp1=generateInputArrays(tmp,width,height);
            supp2=ApplyMidpoint(supp1,Dim_kernel,width,height);
            supp3=generateOutputArray(supp2,width,height);
        }
        else if(type_filter.equals("Alpha-trimmed mean Filter"))
        {
            supp1=generateInputArrays(tmp,width,height);
            supp2=ApplyAlpha(supp1,Dim_kernel,width,height);
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
        if(type_filter.equals("Median Filter"))
        {
            supp1_red=generateInputArrays(tmp_red,width,height);
            supp1_green=generateInputArrays(tmp_green,width,height);
            supp1_blue=generateInputArrays(tmp_blue,width,height);
            supp2_red=ApplyMedian(supp1_red,Dim_kernel,width,height);
            supp2_green=ApplyMedian(supp1_green,Dim_kernel,width,height);
            supp2_blue=ApplyMedian(supp1_blue,Dim_kernel,width,height);
            output_red=generateOutputArray(supp2_red,width,height);
            output_green=generateOutputArray(supp2_green,width,height);
            output_blue=generateOutputArray(supp2_blue,width,height);
        }
        else if(type_filter.equals("Max Filter"))
        {
            supp1_red=generateInputArrays(tmp_red,width,height);
            supp1_green=generateInputArrays(tmp_green,width,height);
            supp1_blue=generateInputArrays(tmp_blue,width,height);
            supp2_red=ApplyMax(supp1_red,Dim_kernel,width,height);
            supp2_green=ApplyMax(supp1_green,Dim_kernel,width,height);
            supp2_blue=ApplyMax(supp1_blue,Dim_kernel,width,height);
            output_red=generateOutputArray(supp2_red,width,height);
            output_green=generateOutputArray(supp2_green,width,height);
            output_blue=generateOutputArray(supp2_blue,width,height);
        }
        else if(type_filter.equals("Min Filter"))
        {
            supp1_red=generateInputArrays(tmp_red,width,height);
            supp1_green=generateInputArrays(tmp_green,width,height);
            supp1_blue=generateInputArrays(tmp_blue,width,height);
            supp2_red=ApplyMin(supp1_red,Dim_kernel,width,height);
            supp2_green=ApplyMin(supp1_green,Dim_kernel,width,height);
            supp2_blue=ApplyMin(supp1_blue,Dim_kernel,width,height);
            output_red=generateOutputArray(supp2_red,width,height);
            output_green=generateOutputArray(supp2_green,width,height);
            output_blue=generateOutputArray(supp2_blue,width,height);
        }
        else if(type_filter.equals("Midpoint Filter"))
        {
            supp1_red=generateInputArrays(tmp_red,width,height);
            supp1_green=generateInputArrays(tmp_green,width,height);
            supp1_blue=generateInputArrays(tmp_blue,width,height);
            supp2_red=ApplyMidpoint(supp1_red,Dim_kernel,width,height);
            supp2_green=ApplyMidpoint(supp1_green,Dim_kernel,width,height);
            supp2_blue=ApplyMidpoint(supp1_blue,Dim_kernel,width,height);
            output_red=generateOutputArray(supp2_red,width,height);
            output_green=generateOutputArray(supp2_green,width,height);
            output_blue=generateOutputArray(supp2_blue,width,height);
        }
        else if(type_filter.equals("Alpha-trimmed mean Filter"))
        {
            supp1_red=generateInputArrays(tmp_red,width,height);
            supp1_green=generateInputArrays(tmp_green,width,height);
            supp1_blue=generateInputArrays(tmp_blue,width,height);
            supp2_red=ApplyAlpha(supp1_red,Dim_kernel,width,height);
            supp2_green=ApplyAlpha(supp1_green,Dim_kernel,width,height);
            supp2_blue=ApplyAlpha(supp1_blue,Dim_kernel,width,height);
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



