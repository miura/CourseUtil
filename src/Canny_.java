import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import utility.Service;

/**
 * This class computes the Canny operator in an image.
 * @author Carmelo Pulvirenti pulvirenti.carmelo@libero.it
 */

public class Canny_  implements PlugInFilter
{
       
       /**
        * The gaussian kernel.
        */
       private float[] gaussianKernel = null;
       
       
       /**
        * The low threshold value.
        */
       private int lowThreshold;
       
       /**
        * The high threshold value.
        */
       private int highThreshold;
       
       /**
        * A matrix with the gradient value for each pixel in the image.
        */
       private float gradient[][];
       
       /**
        * A matrix with the angle value for each pixel in the image.
        */
       private float angle[][];
       
       
       /**
        * For making the convolution.
        */
       private Convolver convolver=new Convolver();
       
       
       /**
        * This method shows the information about this plugIn.
        */
       private void showAbout()
       {
              IJ.showMessage("Canny operator","Carmelo Pulvirenti\npulvirenti.carmelo@libero.it");
       }
       
       
       public int setup(String arg, ImagePlus imp)
       {
              if (arg.equals("about"))
              {
                     showAbout();
                     return DONE;
              }
              //Cofiguration Input windows
              GenericDialog gd = new GenericDialog("Parameters for Canny");
              if (imp==null)
              {
                     gd = new GenericDialog("No Input!!");
                     gd.addMessage("No loaded image!");
                     gd.showDialog();
                     return PlugInFilter.DONE;
              }
              gd.addNumericField("Gaussian kernel size",5,0);
              gd.addNumericField("Gaussian sigma",1.5,1);
              gd.addNumericField("Hysteresis low value",30,0);
              gd.addNumericField("Hysteresis high value",100,0);
              gd.showDialog();
              
              if ( gd.wasCanceled() )
                     return DONE;
              
              short window = (short) gd.getNextNumber();
              float sigma = (float) gd.getNextNumber();
              lowThreshold= (int) gd.getNextNumber();
              highThreshold= (int) gd.getNextNumber();
              gd.dispose();
              
              
              gd=new GenericDialog("Error");
              if (window<=0 ||window%2==0)
              {
                     gd.addMessage("The window of gaussian is negative or not odd!");
                     gd.showDialog();
                     return PlugInFilter.DONE;
              }
              
              if (sigma<=0)
              {
                     gd.addMessage("Sigma is negative or zero!");
                     gd.showDialog();
                     return PlugInFilter.DONE;
              }
              
              
              if (lowThreshold<0)
              {
                     gd.addMessage("Low threshold is negative");
                     gd.showDialog();
                     return PlugInFilter.DONE;
              }
              
              
              if (highThreshold>255)
              {
                     gd.addMessage("HighThreshold is greater than 255 ");
                     gd.showDialog();
                     return PlugInFilter.DONE;
              }
              
              
              if (highThreshold<lowThreshold)
              {
                     gd.addMessage("High threshold is less big than low threshold ");
                     gd.showDialog();
                     return PlugInFilter.DONE;
              }
              
              gd.dispose();
              gaussianKernel=Service.initGaussianKernel(window,sigma);
              return PlugInFilter.DOES_ALL;
       }
       
       
       public void run(ImageProcessor ip)
       {
              ByteProcessor bp = canny( ip );
              ImagePlus outImg = new ImagePlus("Canny Filter Result",bp);
              outImg.show();
       }
       
       
       /**
        * This method computes a canny edge in a image.
        * @param image The input image.
        * @return the output image.
        */
       
       public ByteProcessor canny(ImageProcessor image)
       {
              /***********************************************************************************
               * Initial phase
               ***********************************************************************************/
              int width = image.getWidth();
              int height = image.getHeight();
              ByteProcessor bp = Service.getByteProcessor(image);
              
              
              
              
              
              /***********************************************************************************
               * First step: Convole the image in input with a gaussian.
               ***********************************************************************************/
              
              bp=convolveImage(bp,gaussianKernel,true);
              
              
              
              
              /***********************************************************************************
               * Second step: It calculates the direction and the magnitudo of the gradient.
               ***********************************************************************************/
              
              computeGradient(bp);
              
              
              
              /***********************************************************************************
               * Third step: Calculation of the strong edge and individualization of the weak edge
               ***********************************************************************************/
              ByteProcessor out = new ByteProcessor(width,height);
              List<int[]> pixels = new ArrayList<int[]>();
              
              for (int y=1; y<height-1; y++)
              {
                     for (int x=1; x<width-1; x++)
                     {
                            if (!isLocalMaxima(x,y)) continue;
                            float g = gradient[x][y];
                            
                            // if gradient in (x,y) is less great than  low-threshold  is not an edge.
                            if (g<lowThreshold) continue;
                            
                            // if gradient in (x,y) is bigger than  higt-threshold  is a strongedge.
                            if (g>highThreshold)
                            {
                                   out.set(x,y,255);
                                   continue;
                            }
                            
                            // between thresholds.It is a weak edge.
                            pixels.add(new int[]{x,y});
                     }
              }
              
              /***********************************************************************************
               * fourth step: linking.
               ***********************************************************************************/
              boolean change=true;
              while(change)
              {
                     
                     // for each weak edge.
                     change=false;
                     Iterator<int[]> iter = pixels.iterator();
                     
                     while(iter.hasNext())
                     {
                            int[] pixel = iter.next();
                            int x=pixel[0];
                            int y=pixel[1];
                            // guardo i vicini se sono edge forti
                            if(out.get(x-1,y+1)>0||out.get(x-1,y)>0||out.get(x-1,y-1)>0||out.get(x,y+1)>0||out.get(x,y-1)>0||out.get(x+1,y+1)>0||out.get(x+1,y)>0||out.get(x+1,y-1)>0)
                                   out.set(x,y,255);
                            
                            iter.remove();
                            change=true;
                     }
              }
              return out;
       }
       
       
       /**
        * This method computes the direction and the magnitudo of the gradient.
        * You can find the output value in two private matrix:
        * Angle and Gradient.
        * @param bp the image in input.
        */
       private void computeGradient(ByteProcessor bp)
       {
              ByteProcessor bpSobelX=convolveImage(bp,Service.sobelY,true);
              ByteProcessor bpSobelY=convolveImage(bp,Service.sobelX,true);
              int width=bp.getWidth();
              int height=bp.getHeight();
              gradient=new float[width][height];
              angle=new float[width][height];
              int valueImage1,valueImage2;
              for (int x=0;x<width;x++)
              {
                     for(int y=0;y<height;y++)
                     {
                            valueImage1=bpSobelX.get(x,y);
                            valueImage2=bpSobelY.get(x,y);
                            gradient[x][y]=(float)Math.sqrt((valueImage1*valueImage1)+(valueImage2*valueImage2));
                            angle[x][y]=(float)Math.atan2(valueImage2,valueImage1);
                     }
              }
       }
       
       
       
       /**
        * This method, convolve an image with a Kernel without change the input image.
        * @param b it is the image in input.
        * @param kernel[] the kernel for the convolution.
        * @return the output of the convolution.
        */
       private ByteProcessor convolveImage(ByteProcessor b,float kernel[],boolean flag)
       {
              convolver.setNormalize(true);
              ByteProcessor out=Service.getByteProcessor(b);
              convolver.convolve(out,kernel,(int)Math.sqrt(kernel.length),(int)Math.sqrt(kernel.length));
              return out;
       }
       
       
       /**
        * It verificate if the point (x,y) is a maxima local.
        */
       private boolean isLocalMaxima(int x, int y)
       {
              double grad=gradient[x][y];
              
              if (grad<1) return true;
              
              // gradient direction
              double gx = Math.cos(angle[x][y]);
              double gy = Math.sin(angle[x][y]);
              
              // scaling
              double gmax = Math.max( Math.abs(gx), Math.abs(gy) );
              double scale = 1.0/gmax;
              
              // gradient value at next position in the gradient direction
              int nextX =  (int)(x + gx * scale);
              int nextY = (int) (y + gy * scale);
              double gradn = gradient[nextX][nextY];
              
              // gradient value at previous position in the gradient direction
              int previousX = (int) (x - gx * scale);
              int previousY = (int) (y - gy * scale);
              double gradp = gradient[previousX][previousY];
              
              // is the current gradient value a local maxima ?
              if (grad>gradn && grad>gradp) return true;
              
              // otherwise
              return false;
       }
}
