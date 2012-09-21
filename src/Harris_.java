//Harry detection per imageJ

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;
import ij.process.ColorProcessor;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JOptionPane;


/**
 * Harris Corner Detector
 * classe che effettua la detection di corners
 * multirisoluzione, in immagini a toni di grigio
 * @author Messina Mariagrazia
 *
 */
public class Harris_ implements PlugInFilter
{
       
	// lista che conterrà  i conrner ad agni iterazione
       List<int[]> corners;
	
	//dimensioni di mezza finestra
       private int halfwindow = 0;
       
	// varianza della gaussiana
       private float gaussiansigma = 0;
       
	// parametri dii soglia
       private int minDistance = 0;
       private int minMeasure = 0;
       private int piramidi=0;
	// oggetto utilizzato per il calcolo del gradiente
       GradientVector gradient = new GradientVector();
	//matrice dei corners
       int matriceCorner[][];
	// About...
       private void showAbout()
       {
              IJ.showMessage("Harris..."," Harris Corner Detector ");
       }
       
       public int setup(String arg, ImagePlus imp)
       {
              
	// about...
              if (arg.equals("about"))
              {
                     showAbout();
                     return DONE;
              }
              
	// else...
              if (imp==null) return DONE;
              
              
	// richiesta di parametri in input
              
              GenericDialog gd = new GenericDialog("PARAMETRI");
              gd.addNumericField("Varianza gaussiana",1.4,1);
              gd.addNumericField("Soglia minima",10,0);
              gd.addNumericField("Distanza minima",8,0);
              gd.addNumericField("Numero di iterazioni da effetture",1,0);
              
              int halfwindow = 1;
              float gaussiansigma = 0;
              int minMeasure = 0;
              int minDistance = 0;
              int piramidi=0;
              boolean controllo=true;
              while(controllo)
              {  
                     gd.showDialog();
                     if ( gd.wasCanceled() ) return DONE;
                     
                     
                     
                     gaussiansigma = (float) gd.getNextNumber();
                     minMeasure = (int) gd.getNextNumber();
                     minDistance = (int) gd.getNextNumber();
                     piramidi=(int) gd.getNextNumber();
                     if (gaussiansigma >0 && minMeasure >=0 && minDistance>=0)
                            controllo=false;
              }
              gd.dispose();
              
              this.halfwindow = halfwindow;
              this.gaussiansigma = gaussiansigma;
              this.minMeasure = minMeasure;
              this.minDistance = minDistance;
              this.piramidi=piramidi;
              return PlugInFilter.DOES_8G;
       }
       
       public void run(ImageProcessor ip)
       {
              

           ByteProcessor bp=Supporto.copyByteProcessor(ip);  
           ByteProcessor bp2=Supporto.copyByteProcessor(ip);  
           int width = bp.getWidth();
           int height =bp.getHeight(); 
           int potenza=(int)Math.pow(2,piramidi-1);
           if((width/potenza<8)||(height/potenza<8))
           {
                piramidi=1;
                JOptionPane.showMessageDialog(null,"n° di iteazioni da effettuare troppo alto,\n sarà effettuata una sola iterazione");
           }

            ByteProcessor newbp;
            List<int[]> tmp = new ArrayList<int[]>();
            int[] numero=new int[this.piramidi];
            
           for (int i=0;i<this.piramidi;i++) 
           {
                corners= new ArrayList<int[]>();
                filter( bp, this.minMeasure, this.minDistance,i );
		   for(int[] n:corners)
			   tmp.add(n);
		numero[i]=corners.size();
     
                bp=Supporto.smussaEsottocampiona(bp,3,this.gaussiansigma);
		
           }
           
            ColorProcessor image=Supporto.cambioColore(bp2);
            image=Supporto.disegna(tmp,image,numero);
            ImagePlus newImgLut = new ImagePlus("Risultato", image);
            newImgLut.show();
            
       }
       
// -------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
       
        /**
        *Harris Corner Detection
        *
        * @param c immagine
        * @param minMeasure saglio sul valore minimo che assume il corner
        * @param minDistance soglia sulla distanza minima tra 2 corners
        */
       public void filter(ByteProcessor c, int minMeasure, int minDistance,int factor)
       {
	     
              int width = c.getWidth();
              int height = c.getHeight();
              
		// scurire l'immagine
              ByteProcessor c2 = new ByteProcessor(width,height);
              for (int y=0; y<height; y++)
                     for (int x=0; x<width; x++)
                            c2.set(x,y,(int)(c.get(x,y)*0.80));
              
              for (int y=0; y<height; y++)
              {
                     for (int x=0; x<width; x++)
                     {
		// harris response(-1 se il pixel non è un massimo locale)
                            int h = (int)spatialMaximaofHarrisMeasure(c, x, y);
                            
		// aggiunge il corner alla lista se supera un valore di soglia
                            if (h>=minMeasure) 
                            {    
                               if(factor!=0)
                               {
                                    int XY[]= mappatura(x,y,factor);
                                    x=XY[0];
                                    y=XY[1];
                               }
                               
                                corners.add( new int[]{x,y,h} );
                                
                            }
                     }
              }
              
		// si tengono i valori di risposta più alti
              Iterator<int[]> iter = corners.iterator();
              while(iter.hasNext())
              {
                     int[] p = iter.next();
                     for(int[] n:corners)
                     {
                            if (n==p) continue;
                            int dist = (int)Math.sqrt( (p[0]-n[0])*(p[0]-n[0])+(p[1]-n[1])*(p[1]-n[1]) );
                            if( dist>minDistance) continue;
                            if (n[2]<p[2]) continue;
                            iter.remove();
                            break;
                     }
              }
              

       }
       
       /**
        * reatituisce il valore del pixel (x,y) se è un massimo, altrimenti restituisce -1
        *
        * @param c immagine
        * @param x coordinata x
        * @param y coordinata y
        * @return la harris response se il pixel è un massimo locale, -1 altrimenti
        */
       private double spatialMaximaofHarrisMeasure(ByteProcessor c, int x, int y)
       {
              int n=8;
              int[] dx = new int[] {-1,0,1,1,1,0,-1,-1};
              int[] dy = new int[] {-1,-1,-1,0,1,1,1,0};
		//si calcola il valore di harris response nel punto x,y
              double w = harrisMeasure(c,x,y);
		//per ogni punto dell'intorno di x,y si calcola il valore della harris response
              for(int i=0;i<n;i++)
              {
                     double wk = harrisMeasure(c,x+dx[i],y+dy[i]);
		//se almeno un valore calcolato in un punto dell'intorno è maggiore di quello del punto in questione, esso non
		// è un massimo locale e si restituisce -1
                     if (wk>=w) return -1;
              }
		//in caso contrario è un massimo locale
              return w;
       }
       
         /**
        * computa harris corner response
        *
        * @param c Image map
        * @param x coordinata x
        * @param y y coordinata y
        * @return harris corner response
        */
       private double harrisMeasure(ByteProcessor c, int x, int y)
       {
              double m00=0, m01=0, m10=0, m11=0;
              
		// k = det(A) - lambda * trace(A)^2
		// A matrice del secondo momento
		// lambda generalmente è tra 0.04 e 0.06. qui è stato fissato a 0.06
              
              for(int dy=-halfwindow;dy<=halfwindow;dy++)
              {
                     for(int dx=-halfwindow;dx<=halfwindow;dx++)
                     {
                            int xk = x + dx;
                            int yk = y + dy;
                            if (xk<0 || xk>=c.getWidth()) continue;
                            if (yk<0 || yk>=c.getHeight()) continue;
                            
		// calcolo del gradiente (derivate prime parziali ) di c nel punto xk,yk
                            double[] g = gradient.getVector(c,xk,yk);
                            double gx = g[0];
                            double gy = g[1];
                            
		// calcolo il peso della finestra gaussiana nel punto dx,dy
                            double gw = gaussian(dx,dy,gaussiansigma);
                            
		// creazione degli elementi della matrice
                            m00 += gx * gx * gw;
                            m01 += gx * gy * gw;
                            m10 = m01;
                            m11 += gy * gy * gw;
                     }
              }
              
		// harris = det(A) - 0.06*traccia(A)^2;
                //det(A)=m00*m11 - m01*m10
              double det= m00*m11 - m01*m10;
              //tr(A)=(m00+m11)*(m00+m11);
              double traccia=(m00+m11);
	      // harris response= det-k tr^2;
              double harris=det-0.06*(traccia*traccia);
              return harris/(256*256);
       }
       
       
       
       /**
        * Funzione per il computo della Gaussian window
        *
        * @param x coordinata x
        * @param y coordinata y
        * @param sigma2 variannza
        * @return valore della funzione
        */
       
       private double gaussian(double x, double y, float sigma2)
       {
              double t = (x*x+y*y)/(2*sigma2);
              double u = 1.0/(2*Math.PI*sigma2);
              double e = u*Math.exp( -t );
              return e;
       }
              
      /**
        * Funzione che realizza la mappatura dei pixel dell'immagine sottocampionata, nell'immagine originale
        *
        * @param x coordinata x
        * @param y coordinata y
        * @param fact parametro di scala
        * @return coordinate x e y nell'immagine originale
        */
       
       public int[] mappatura(int x, int y,int fact)
       {
           int nuoviXY[]=new int[2];
          nuoviXY[0]=x*(2*fact);
            nuoviXY[1]=y*(2*fact);    
            return nuoviXY;
       }
       
}


/**
 * Gradient vector
 * classe che effettua il calcolo del gradiente smussato,
*  effetturando le derivate x e y di una gaussiana
 * @author Messina Mariagrazia
 *
 */
class GradientVector
{
       
       int halfwindow = 1; 
       double sigma2 = 1.2;
       
       double[][] kernelGx = new double[2*halfwindow+1][2*halfwindow+1];
       double[][] kernelGy = new double[2*halfwindow+1][2*halfwindow+1];
       
      /**
        * Metodo costruttore
        *
        */
       public GradientVector()
       {
              for(int y=-halfwindow;y<=halfwindow;y++)
              {
                     for(int x=-halfwindow;x<=halfwindow;x++)
                     {
                            kernelGx[halfwindow+y][halfwindow+x] = Gx(x, y);
                            kernelGy[halfwindow+y][halfwindow+x] = Gy(x, y);
                     }
              }
       }
       
      /**
        * Funzione che realizza lo smussamento dell'immagine mediante una gaussiana per poi calcolarne la derivata x (operatore Drog)
        *
        * @param x coordinata x
        * @param y coordinata y
        * @return volere della gaussiana nel punto x,y
        */
       private double Gx(int x, int y)
       {
              double t = (x*x+y*y)/(2*sigma2);
              double d2t = -x / sigma2;
              double e = d2t * Math.exp( -t );
              return e;
       }
       
       /**
        * Funzione che realizza lo smussamento dell'immagine mediante una gaussiana per poi calcolarne la derivata y (operatore Drog)
        *
        * @param x coordinata x
        * @param y coordinata y
        * @return volere della gaussiana nel punto x,y
        */
       private double Gy(int x, int y)
       {
              double t = (x*x+y*y)/(2*sigma2);
              double d2t = -y /sigma2;
              double e = d2t * Math.exp( -t );
              return e;
       }
       
	// restituisce  il vettore del Gradient per il pixel(x,y)
       /**
        * Funzione che inserisce in un vettore il valore del gradiente dei punti appartenenti ad una finestre
        *
        * @param x coordinata x
        * @param y coordinata y
        * @param c immagine
        * @return volere del gradiente x e y in tutti i punti della finestra
        */
       public double[] getVector(ByteProcessor c, int x, int y)
       {
              double gx=0, gy=0;
              for(int dy=-halfwindow;dy<=halfwindow;dy++)
              {
                     for(int dx=-halfwindow;dx<=halfwindow;dx++)
                     {
                            int xk = x + dx;
                            int yk = y + dy;
                            double vk = c.getPixel(xk,yk); // <-- value of the pixel
                            gx += kernelGx[halfwindow-dy][halfwindow-dx] * vk;
                            gy += kernelGy[halfwindow-dy][halfwindow-dx] * vk;
                     }
              }
              
              double[] gradientVector = new double[] { gx, gy };
              
              return gradientVector;
       }
}

