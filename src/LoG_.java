import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import utility.*;


/**
 * This class make the edge extraction using LoG operator.
 * @author Carmelo Pulvirenti pulvirenti.carmelo@libero.it.
 */
public class LoG_ implements PlugInFilter
{
       /**
        * This is the LoG kernel.
        */
       private float [] log;
       
       
       /**
        * This is the threshold value.
        */
       private int threshold;
       
       
       
       
       
       /**
        * Show the information about this plugin.
        */
       private void showAbout()
       {
              IJ.showMessage("LoG operator","Carmelo Pulvirenti\npulvirenti.carmelo@libero.it");
       }
       
       
       
       public int setup(String arg, ImagePlus imp)
       {
              if (arg.equals("about"))
              {
                     showAbout();
                     return DONE;
              }
              
              //Cofiguration Input windows
              GenericDialog gd = new GenericDialog("Parameters for LoG");
              if (imp==null)
              {
                     gd = new GenericDialog("No Input!!");
                     gd.addMessage("No loaded image!");
                     gd.showDialog();
                     return PlugInFilter.DONE;
              }
              
              gd.addNumericField("Gaussian kernel size. Odd",3,0);
              gd.addNumericField("Gaussian sigma",0.5,1);
              gd.addNumericField("Threshold [0-255] ",60,0);
              gd.showDialog();
              
              if ( gd.wasCanceled() )	return DONE;
              
              
              short gaussianwindow = (short) gd.getNextNumber();
              float gaussiansigma  = (float) gd.getNextNumber();
              threshold=(int) gd.getNextNumber();
              gd.dispose();
              gd=new GenericDialog("Error");
              
              if (gaussianwindow<=0)
              {
                     gd.addMessage("The window of gaussian is negative or zero!");
                     gd.showDialog();
                     return PlugInFilter.DONE;
              }
              
              if (gaussiansigma<=0)
              {
                     gd.addMessage("Sigma is negative or zero!");
                     gd.showDialog();
                     return PlugInFilter.DONE;
              }
              
              if(gaussianwindow%2==0)
              {
                     gd.addMessage("Window isn't odd!");
                     gd.showDialog();
                     return PlugInFilter.DONE;
              }
              
              if(threshold<0||threshold>255)
              {
                     gd.addMessage("Threshold isn't between 0-255 ");
                     gd.showDialog();
                     return PlugInFilter.DONE;
              }
              gd.dispose();
              this.log=Service.initLaplacianOfGaussian(gaussianwindow,gaussiansigma);
              return PlugInFilter.DOES_ALL;
       }
       
       public void run(ImageProcessor image)
       {
              /***********************************************************************************
               * Initial phase
               ***********************************************************************************/
              int width=image.getWidth();
              int heigh=image.getHeight();
              ByteProcessor bp=Service.getByteProcessor(image);
              
              
              /***********************************************************************************
               * Convolve with LoG kernel
               ***********************************************************************************/
              Convolver convolver=new Convolver();
              convolver.setNormalize(false);
              convolver.convolve(bp,log,(int)Math.sqrt(log.length),(int)Math.sqrt(log.length));
              ImagePlus outImg = new ImagePlus("Later Log kernel", bp);
              outImg.show();
              
              
              
              /***********************************************************************************
               * threshold
               ***********************************************************************************/
              ByteProcessor bpThreshold=Service.getByteProcessor(bp);
              int lut[]=new int[256];
              int i=0;
              for (;i<threshold;i++)
                     lut[i]=0;
              for(int j=i;j<lut.length;j++)
                     lut[j]=255;
              bpThreshold.applyTable(lut);
              ImagePlus outImg1 = new ImagePlus("Later threshold phase",bpThreshold);
              outImg1.show();
              
              
              
              /***********************************************************************************
               * Find Zero crossing
               ***********************************************************************************/
              ByteProcessor out=new ByteProcessor(width,heigh);
              for (i=0;i<width;i++)
                     for(int j=0;j<heigh;j++)
                            out.set(i,j,255);
              for(int x=0;x<width-1;x++)
              {
                     for(int y=0;y<heigh-1;y++)
                     {
                            if (bpThreshold.get(x,y)!=bpThreshold.get(x,y+1))
                                   out.set(x,y,0);
                            if (bpThreshold.get(x,y)!=bpThreshold.get(x+1,y))
                                   out.set(x,y,0);
                            if (bpThreshold.get(x,y)!=bpThreshold.get(x+1,y+1))
                                   out.set(x,y,0);
                     }
              }
              ImagePlus outImg2 = new ImagePlus("Edge Find", out);
              outImg2.show();
       }
}
