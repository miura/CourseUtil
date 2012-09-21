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

/**     This plugin implements the more common filters of average for the reduction of the noise in one image
        @author Leonardi Rosa-BS degree in Computer Science 
        Advisor: Prof. Battiato Sebastiano
        Organization: University of Catania - ITALY
*/
public class Mean_Filters extends PlugInFrame implements ActionListener, ItemListener
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
        private int DimKernel=5;
        private String[] filters={"Arithmetic mean","Geometric mean","Harmonic mean","Contraharmonic mean"};
        public Mean_Filters() 
        {
		    super("Mean Filters");
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
            Label title= new Label("Mean Filters", Label.CENTER);
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
            selected="Arithmetic mean";
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
            ih.addActionListener(new InfoHelpViewerMF());
            panelInfoHelp.add(ih);
            
            // panel container
            Panel container=new Panel();
            container.setLayout(new GridLayout(3,1));
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
                MeanF mf=null;
                ImagePlus processed = null;
                String chosen="";
                time=System.currentTimeMillis();
                if(selected.equals("Arithmetic mean"))
                    chosen="Arithmetic mean";
                else if(selected.equals("Geometric mean"))
                    chosen="Geometric mean";
                else if(selected.equals("Harmonic mean"))
                    chosen="Harmonic mean";
                else if(selected.equals("Contraharmonic mean"))
                    chosen="Contraharmonic mean";
                else chosen=selected;
                boolean dialog=showDialog();
                if (dialog==false)
                {
                    IJ.error("Error","You must insert an entire number positive greater or equal odd number to 2 ");
                    return;
                }
                IJ.showMessage("Filter: "," "+chosen+" "+DimKernel+"x"+DimKernel+" ");
                mf= new MeanF(imp, imp.getTitle(),chosen,DimKernel);
                if(type==ImagePlus.GRAY8)
                    processed=mf.createProcessed8bitImage(); 
                else if(type==ImagePlus.GRAY16)
                    processed=mf.createProcessed8bitImage();
                else if(type==ImagePlus.GRAY32)
                    processed=mf.createProcessed8bitImage();
                else if(type==ImagePlus.COLOR_256)
                    processed=mf.createProcessed8bitImage();
                else if(type==ImagePlus.COLOR_RGB)
                    processed=mf.createProcessedRGBImage();
                time=System.currentTimeMillis()-time;
                double time_sec=time/1000.0;
                PreviewImageMF prew=new PreviewImageMF(processed,time_sec);
            }
            if(source==apply)
            {
                Undo.setup(Undo.COMPOUND_FILTER, imp);
                WindowManager.setTempCurrentImage(imp);
                MeanF mf=null;
                String chosen="";
                if(selected.equals("Arithmetic mean"))
                    chosen="Arithmetic mean";
                else 
                    if(selected.equals("Geometric mean"))
                        chosen="Geometric mean";  
                else 
                    if(selected.equals("Harmonic mean"))
                        chosen="Harmonic mean";  
                else 
                    if(selected.equals("Contraharmonic mean"))
                        chosen="Contraharmonic mean";  
                else chosen=selected;
                boolean dialog=showDialog();
                if (dialog==false)
                {
                    IJ.error("Error","You must insert an entire number positive greater or equal odd number to 2 ");
                    return;
                }
                IJ.showMessage("Filter: "," "+chosen+" "+DimKernel+"x"+DimKernel+" ");
                mf= new MeanF(imp, imp.getTitle(),chosen,DimKernel);
                if(type==ImagePlus.GRAY8)
                    mf.createProcessed8bitImage(imp); 
                else if(type==ImagePlus.GRAY16)
                    mf.createProcessed8bitImage(imp);
                else if(type==ImagePlus.GRAY32)
                    mf.createProcessed8bitImage(imp);
                else if(type==ImagePlus.COLOR_256)
                    mf.createProcessed8bitImage(imp);
                else if(type==ImagePlus.COLOR_RGB)
                    mf.createProcessedRGBImage(imp);
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
        public void itemStateChanged(ItemEvent e)
        {
            selected=choice.getSelectedItem();  
        }
}

class InfoHelpViewerMF implements ActionListener
{
    private String description="HELP ABOUT \"Mean Filters\"\n\n"+
            "This plugin implements the more common filters of average for the reduction of the noise in one image\n"+
            "User can choose one of the filters of mean standard specified in the combo box and to insert the dimension of the kernel to apply.\n"+
            "For the contraharmonic mean filter, the user can also choose the \"order\" of the filter.\n"+
            "Here is a brief description of the steps to do:\n"+
            "1) Choose a standard filter using the combo box;\n"+
            "2) Click on the button \"Preview\" to view as the image would be if it was processed according to the chosen filter;\n"+
            "3) To insert the dimension of the kernel to apply \n"+
            "4) If the contraharmonic mean filter comes chosen, to insert the \"order\" of the filter \n"+ 
            "5) Click on the button \"Apply\" to definitively apply the current filtering to the image inserting the dimension and the order of the filter like before;\n\n"+
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
        TextWindow tw = new TextWindow("About \" Mean Filters \"", sb.toString(), 700, 500);
        tw.show();
    }
}

class PreviewImageMF implements ActionListener
{
    private ImageCanvas canvas;
    private Panel panelTime;
    private ImagePlus im;
        
    public PreviewImageMF(ImagePlus img, double time)
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

class MeanF
{
    private ImagePlus imp;
    private ImageProcessor ip;
    private int width;
    private int height;
    private String title, type_mean; 
    private int Dim_kernel;//dimension of the kernel
    private double Q=1.5;//order of the filter contraharmonic
    public MeanF(ImagePlus image, String t, String mean, int k) 
    {
	    imp=image;
        ip=imp.getProcessor();
        width=ip.getWidth();
        height=ip.getHeight();
        title=t;
        type_mean=mean;
        Dim_kernel=k;
    }
    public boolean showDialog()
    {
        GenericDialog gd = new GenericDialog("Order of the contraharmonic mean filter:");
        gd.addNumericField("Q = ",Q,2);
        gd.showDialog();
        Q=gd.getNextNumber();
        String value=java.lang.Double.toString(Q);
        if(value.indexOf('.')!=-1)
        {
            if(value.substring(value.indexOf('.')).length()>3)
            {
                IJ.error("Error","You must enter a value with not\n more than two decimal places");
                return false;              
            }
        }
        return true;
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
    * Calculates the arithmetic mean of a kxk pixel neighbourhood (including centre pixel).
    *
    * @param input The input image 2D array
    * @param k Dimension of the kernel
    * @param w The image width
    * @param h The image height
    * @param x The x coordinate of the centre pixel of the array
    * @param y The y coordinate of the centre pixel of the array
    * @return The arithmetic mean of the kxk pixels
    */ 
    public int meanArithmetic(int[][] input, int k,int w, int h, int x, int y) 
    {
        int sum = 0;
        int number = 0;
        for(int j=0;j<k;++j)
        {
            for(int i=0;i<k;++i)
            {
	            if(((x-1+i)>=0) && ((y-1+j)>=0) && ((x-1+i)<w) && ((y-1+j)<h))
                {
	                sum = sum + input[x-1+i][y-1+j];
	                ++number;
	            }
            }
        }
        if(number==0) 
            return 0;
        return (sum/number);
    }
    /**
    * Calculates the geometric mean of a kxk pixel neighbourhood (including centre pixel).
    *
    * @param input The input image 2D array
    * @param k Dimension of the kernel
    * @param w The image width
    * @param h The image height
    * @param x The x coordinate of the centre pixel of the array
    * @param y The y coordinate of the centre pixel of the array
    * @return The geometric mean of the kxk pixels
    */ 
    public int meanGeometric(int[][] input, int k, int w, int h, int x, int y)
    {
        double product = 1;
        double number = 0;
        for(int j=0;j<k;++j)
        {
            for(int i=0;i<k;++i)
            {
	            if(((x-1+i)>=0) && ((y-1+j)>=0) && ((x-1+i)<w) && ((y-1+j)<h))
                {
	                product = product * input[x-1+i][y-1+j];
	                ++number;
	            }
            }
        }
        if(number==0) 
            return 0;
        double esp=1/number;
        double result=Math.pow(product,esp);
        if(result<0)
            result=0;
        if(result>255)
            result=255;
        return (int)result;
    }
    /**
    * Calculates the harmonic mean of a kxk pixel neighbourhood (including centre pixel).
    *
    * @param input The input image 2D array
    * @param k Dimension of the kernel
    * @param w The image width
    * @param h The image height
    * @param x The x coordinate of the centre pixel of the array
    * @param y The y coordinate of the centre pixel of the array
    * @return The harmonic mean of the kxk pixels
    */ 
    public int meanHarmonic(int[][] input, int k,int w, int h, int x, int y) 
    {
        double sum = 0;
        double number = 0;
        double supp;
        for(int j=0;j<k;++j)
        {
            for(int i=0;i<k;++i)
            {
	            if(((x-1+i)>=0) && ((y-1+j)>=0) && ((x-1+i)<w) && ((y-1+j)<h))
                {
                    supp=(double)input[x-1+i][y-1+j];
                    sum = sum + 1/supp;
                    ++number; 
	            }
            }
        }
        if(number==0) 
            return 0;
        double result=number/sum;
        if(result<0)
            result=0;
        if(result>255)
            result=255;
        return (int)result;
    }
    /**
    * Calculates the contraharmonic mean of a kxk pixel neighbourhood (including centre pixel).
    *
    * @param input The input image 2D array
    * @param k Dimension of the kernel
    * @param w The image width
    * @param h The image height
    * @param x The x coordinate of the centre pixel of the array
    * @param y The y coordinate of the centre pixel of the array
    * @return The contraharmonic mean of the kxk pixels
    */ 
    public int meanContraHarmonic(int[][] input, int k,int w, int h, int x, int y) 
    {
        double sum1 = 0;
        double sum2 = 0;
        int number = 0;
        double supp;
        for(int j=0;j<k;++j)
        {
            for(int i=0;i<k;++i)
            {
	            if(((x-1+i)>=0) && ((y-1+j)>=0) && ((x-1+i)<w) && ((y-1+j)<h))
                {
                    supp=(double)input[x-1+i][y-1+j];
	                sum1 = sum1 + Math.pow(supp,Q+1);
                    sum2 = sum2 + Math.pow(supp,Q);
	                ++number;
	            }
            }
        }
        if(number==0) 
            return 0;
        double result=sum1/sum2;
        if(result<0)
            result=0;
        if(result>255)
            result=255;
        return (int)result;
    }
    /**
    * Takes an image in 2D array form and it applies the filter of arithmetic mean with the specified dimension of the kernel from the user 
    * @param input the input image
    * @param k Dimension of the kernel
    * @param width Width of the input image
    * @param height Height of the output image
    * @return the new smoothed image 2D array
    */
    public int[][] ApplyMeanArithmetic(int[][] input, int k,int width, int height)
    {
        int [][] outputArrays = new int [width][height];
        for(int j=0;j<height;++j)
            for(int i=0;i<width;++i)
                outputArrays[i][j] = meanArithmetic(input,k,width,height,i,j);
        return outputArrays;
    }
    /**
    * Takes an image in 2D array form and it applies the filter of geometric mean with the specified dimension of the kernel from the user 
    * @param input the input image
    * @param k Dimension of the kernel
    * @param width Width of the input image
    * @param height Height of the output image
    * @return the new smoothed image 2D array
    */
    public int[][] ApplyMeanGeometric(int[][] input, int k,int width, int height)
    {
        int [][] outputArrays = new int [width][height];
        for(int j=0;j<height;++j)
            for(int i=0;i<width;++i)
                outputArrays[i][j] = meanGeometric(input,k,width,height,i,j);
        return outputArrays;
    }
    /**
    * Takes an image in 2D array form and it applies the filter of harmonic mean with the specified dimension of the kernel from the user 
    * @param input the input image
    * @param k Dimension of the kernel
    * @param width Width of the input image
    * @param height Height of the output image
    * @return the new smoothed image 2D array
    */
    public int[][] ApplyMeanHarmonic(int[][] input, int k,int width, int height)
    {
        int [][] outputArrays = new int [width][height];
        for(int j=0;j<height;++j)
            for(int i=0;i<width;++i)
                outputArrays[i][j] = meanHarmonic(input,k,width,height,i,j);
        return outputArrays;
    }
    /**
    * Takes an image in 2D array form and it applies the filter of contraharmonic mean with the specified dimension of the kernel from the user 
    * @param input the input image
    * @param k Dimension of the kernel
    * @param width Width of the input image
    * @param height Height of the output image
    * @return the new smoothed image 2D array
    */
    public int[][] ApplyMeanContraHarmonic(int[][] input, int k,int width, int height)
    {
        int [][] outputArrays = new int [width][height];
        for(int j=0;j<height;++j)
            for(int i=0;i<width;++i)
                outputArrays[i][j] = meanContraHarmonic(input,k,width,height,i,j);
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
        if(type_mean.equals("Arithmetic mean"))
        {
            supp1=generateInputArrays(tmp,width,height);
            supp2=ApplyMeanArithmetic(supp1,Dim_kernel,width,height);
            supp3=generateOutputArray(supp2,width,height);
        }
        else if(type_mean.equals("Geometric mean"))
        {
            supp1=generateInputArrays(tmp,width,height);
            supp2=ApplyMeanGeometric(supp1,Dim_kernel,width,height);
            supp3=generateOutputArray(supp2,width,height);
        }
        else if(type_mean.equals("Harmonic mean"))
        {
            supp1=generateInputArrays(tmp,width,height);
            supp2=ApplyMeanHarmonic(supp1,Dim_kernel,width,height);
            supp3=generateOutputArray(supp2,width,height);
        }
        else if(type_mean.equals("Contraharmonic mean"))
        {
            showDialog();
            supp1=generateInputArrays(tmp,width,height);
            supp2=ApplyMeanContraHarmonic(supp1,Dim_kernel,width,height);
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
        if(type_mean.equals("Arithmetic mean"))
        {
            supp1_red=generateInputArrays(tmp_red,width,height);
            supp1_green=generateInputArrays(tmp_green,width,height);
            supp1_blue=generateInputArrays(tmp_blue,width,height);
            supp2_red=ApplyMeanArithmetic(supp1_red,Dim_kernel,width,height);
            supp2_green=ApplyMeanArithmetic(supp1_green,Dim_kernel,width,height);
            supp2_blue=ApplyMeanArithmetic(supp1_blue,Dim_kernel,width,height);
            output_red=generateOutputArray(supp2_red,width,height);
            output_green=generateOutputArray(supp2_green,width,height);
            output_blue=generateOutputArray(supp2_blue,width,height);
        }
        else if(type_mean.equals("Geometric mean"))
        {
            supp1_red=generateInputArrays(tmp_red,width,height);
            supp1_green=generateInputArrays(tmp_green,width,height);
            supp1_blue=generateInputArrays(tmp_blue,width,height);
            supp2_red=ApplyMeanGeometric(supp1_red,Dim_kernel,width,height);
            supp2_green=ApplyMeanGeometric(supp1_green,Dim_kernel,width,height);
            supp2_blue=ApplyMeanGeometric(supp1_blue,Dim_kernel,width,height);
            output_red=generateOutputArray(supp2_red,width,height);
            output_green=generateOutputArray(supp2_green,width,height);
            output_blue=generateOutputArray(supp2_blue,width,height);
        }
        else if(type_mean.equals("Harmonic mean"))
        {
            supp1_red=generateInputArrays(tmp_red,width,height);
            supp1_green=generateInputArrays(tmp_green,width,height);
            supp1_blue=generateInputArrays(tmp_blue,width,height);
            supp2_red=ApplyMeanHarmonic(supp1_red,Dim_kernel,width,height);
            supp2_green=ApplyMeanHarmonic(supp1_green,Dim_kernel,width,height);
            supp2_blue=ApplyMeanHarmonic(supp1_blue,Dim_kernel,width,height);
            output_red=generateOutputArray(supp2_red,width,height);
            output_green=generateOutputArray(supp2_green,width,height);
            output_blue=generateOutputArray(supp2_blue,width,height);
        }
        else if(type_mean.equals("Contraharmonic mean"))
        {
            showDialog();
            supp1_red=generateInputArrays(tmp_red,width,height);
            supp1_green=generateInputArrays(tmp_green,width,height);
            supp1_blue=generateInputArrays(tmp_blue,width,height);
            supp2_red=ApplyMeanContraHarmonic(supp1_red,Dim_kernel,width,height);
            supp2_green=ApplyMeanContraHarmonic(supp1_green,Dim_kernel,width,height);
            supp2_blue=ApplyMeanContraHarmonic(supp1_blue,Dim_kernel,width,height);
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



