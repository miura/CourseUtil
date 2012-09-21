
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.plugin.filter.Convolver;
import ij.process.ColorProcessor;
import java.awt.Image;
import java.util.List;
import javax.swing.JOptionPane;

public class Supporto
{
       
       
       public static ByteProcessor smussaEsottocampiona(ByteProcessor input,int window,float sigma) throws IllegalArgumentException
       {
              ByteProcessor prepocessing= copyByteProcessor(input);
              float  gauss[]=initGaussianKernel(window,sigma);
              Convolver convolver=new Convolver();
              convolver.convolve(prepocessing,gauss,(int)Math.sqrt(gauss.length),(int)Math.sqrt(gauss.length));
              
              int prepocessingWidth=prepocessing.getWidth();
              int prepocessingHeight=prepocessing.getHeight();
              ByteProcessor out=new ByteProcessor((int)(prepocessingWidth/2),(int)(prepocessingHeight/2));
              if(prepocessingWidth%2!=0)
                     prepocessingWidth--;
              if(prepocessingHeight%2!=0)
                     prepocessingHeight--;
              for (int i=0,x=0;i<prepocessingWidth;i=i+2)
              {
                     for(int j=0,y=0;j<prepocessingHeight;j=j+2)
                     {
                            out.set(x,y,prepocessing.get(i,j));
                            y++;
                     }
                     x++;
              }
              return out;
       }
       
       
       public static ColorProcessor cambioColore(ByteProcessor image)
       {
             Image im=image.createImage();
             return new ColorProcessor(im);
       }
       
       
       
       
       
       
       
       
       
       
       /*********************************************** METODI MIEI*****************************************************/
      
       /**
        *
        * Metodo che copia un ImageProcessor in un ByteProcessor.
        * @param ip  input ImageProcessor.
        * @return ByteProcessor.
        */
       public static ByteProcessor copyByteProcessor(ImageProcessor ip)
       {
              ByteProcessor bp = new ByteProcessor(ip.getWidth(),ip.getHeight());
              for (int y = 0; y < ip.getHeight(); y++)
              {
                     for (int x = 0; x < ip.getWidth(); x++)
                     {
                            bp.set(x,y,ip.getPixel(x,y));
                     }
              }
              return bp;
       }
       
       
       
       /**
        * Realizza la gaussiana e ne inserisce i valori in un array
        * @param window numero di righi e colonne della matrice gaussiana. Deve essere dispari
        * @param sigma 
        * @return array della gaussiana
        * @throws IllegalArgumentException se la finestra è negativa, zero o pari.
        *                                  se sigma è zero o negativa.
        */
       public static float[] initGaussianKernel(int window, float sigma) throws IllegalArgumentException
       {
              controlInput( window, sigma);
              short aperture = (short)(window/2);
              float [][]gaussianKernel = new float[2*aperture+1][2*aperture+1];
              float out[]=new float[(2*aperture+1)*(2*aperture+1)];
              int k=0;
              float sum=0;
              for(int dy=-aperture;dy<=aperture;dy++)
              {
                     for(int dx=-aperture;dx<=aperture;dx++)
                     {
                            gaussianKernel[dx+aperture][dy+aperture]=(float)Math.exp(-(dx*dx+dy*dy)/(2*sigma*sigma));
                            sum+= gaussianKernel[dx+aperture][dy+aperture];
                     }
              }
              for(int dy=-aperture;dy<=aperture;dy++)
                     for(int dx=-aperture;dx<=aperture;dx++)
                            out[k++]= gaussianKernel[dx+aperture][dy+aperture]/sum;
              return out;
       }
       
       /**
        * controllo dei valori della gaussiana
        * @param window la finestra della gaussiana.
        * @param sigma il valore di sigma della gaussiana
        * @throws IllegalArgumentException se la finestra è negativa, zero o non è dispari.
        *                                  se sigma è zero o negativa.
        */
       private static void controlInput(int window,float sigma)throws IllegalArgumentException
       {
              if(window%2==0)
                     throw new IllegalArgumentException("Window isn't an odd.");
              if(window<=0)
                     throw new IllegalArgumentException("Window is negative or zero");
              if(sigma<=0)
                     throw new IllegalArgumentException("Sigma of the gaussian is zero or negative.");
       }
        /**
        * metodo che disegna i corners nell'immagine
        * @param corn lista di tutti i corners trvati
        * @param i immagine su cui disegnare i corners
        * @param colori array che specifica il numero di corners trovati ad ogni iterazione
        * @return immagine a colori con i corners identificati da delle piccole croci colorate
       */
      public static ColorProcessor disegna(List<int[]> corn, ColorProcessor i, int []colori)
       {
           int width = i.getWidth();
              int height =i.getHeight();
           //crea le linee orizzontali
              int R=0;
              int G=0;
              int B=255;
               int colore = (R<<16)|(G<<8)|B;
               int conta=1;
               int j=0;
               boolean esiste=true;
               int tuttiColori[]=new int[colori.length];
               tuttiColori[0]=colore;
                
              for (int[] p:corn)
              {
                    
                         if(conta>colori[j])
                         {
				 conta=1;
			     esiste=true;
                             if(j<colori.length-1)
                             {   j++;
                              while(esiste)
                                {
                                    R=(int)(Math.random()*256);
                                    G=(int)(Math.random()*256);
                                    B=(int)(Math.random()*256);
			
                                    colore=(R<<16)|(G<<8)|B;
                                    esiste=false;
					
                                    for(int k=0;k<tuttiColori.length;k++)
                                         if (colore==tuttiColori[k])
                                             esiste=true;
                                }
				     
                                tuttiColori[j]=colore;
			
			        colore = (R<<16)|(G<<8)|B;
                             }
                         }
                         
                         for (int dx=-2; dx<=2; dx++)
                         {
                                if (p[0]+dx<0 || p[0]+dx>=width) continue;
                                i.set(p[0]+dx,p[1],colore);

                          }

		    //crea le linee verticali
                         for (int dy=-2; dy<=2; dy++)
                         {

                                if (p[1]+dy<0 || p[1]+dy>=height) continue;

                                 i.set(p[0],p[1]+dy,colore);
                         }
                             ++conta;
              }
            
            return i;
       }
        
        
        
        
}
