import ij.*;
import ij.io.*;
import ij.process.*;
import ij.gui.*;
import ij.text.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import java.awt.image.*;
import ij.plugin.frame.*;
import java.io.*;
import java.util.*;
import javax.swing.event.*;

/** This plugin implements the contrast stretching operator
@author Bosco Camillo-BS degree in Computer Science 
        Advisor: Prof. Battiato Sebastiano
        Organization: University of Catania - ITALY
*/
public class Contrast_Stretching extends PlugInFrame implements ActionListener
{

        private Font monoFont = new Font("Monospaced", Font.PLAIN, 12);
        private Font sans = new Font("SansSerif", Font.BOLD, 12);
        private ValueViewerCS viewer;
        private CSPlot plot;
        private Button preview;
        private Button apply;
        private long time;
        private ImageProcessor ip;  
        private ImagePlus imp;
        private int type;
        
        public Contrast_Stretching() 
        {
		super("Contrast Stretching Transform");
                setResizable(false);
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
                
                if((type==imp.GRAY16)||(type==imp.GRAY32))
                {
                  IJ.beep();
                  IJ.error("Error","Contrast Stretching requires an image of type\n \n8-bit grayscale\n8-bit indexed color\nRGB color\n");
                  return;
                }
                
                GridBagLayout gridbag = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		setLayout(gridbag);
		
                //PanelY
                Panel panelY = new Panel();
                c.gridx = 0;
                c.gridy = 1;
                c.insets = new Insets(0, 10, 0, 0);
                gridbag.setConstraints(panelY, c);
                panelY.setLayout(new GridLayout(8,1));
                Panel pan =new Panel();
                Label maxLabelY = new Label("255" , Label.RIGHT);
                maxLabelY.setFont(monoFont);
                pan.add(maxLabelY);
                panelY.add(pan);
                add(panelY);
                
                // Panel title
                Panel paneltitle = new Panel();
                Label title= new Label("Contrast Stretching Transform", Label.CENTER);
		title.setFont(sans);
                paneltitle.add(title);
                c.gridx = 1;
		int y = 0;
		c.gridy = y++;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.insets = new Insets(5, 10, 0, 10);//top,left,bottom,right
		gridbag.setConstraints(paneltitle, c);
                add(paneltitle);
                
                // Plot
                plot = new CSPlot();
                c.gridy = y++;
                c.insets = new Insets(10, 10, 0, 10);
                gridbag.setConstraints(plot, c);
                add(plot);
                
                // panellab
                Panel panellab = new Panel();
                c.gridy = y++;
                c.insets = new Insets(0, 2, 0, 6);
                gridbag.setConstraints(panellab, c);
                panellab.setLayout(new BorderLayout());
                Label minLabel = new Label("0", Label.LEFT);
                minLabel.setFont(monoFont);
                panellab.add("West", minLabel);
                Label maxLabel = new Label("255" , Label.RIGHT);
                maxLabel.setFont(monoFont);
                panellab.add("East", maxLabel);
                add(panellab);
                
                // panel List
                Panel panelList = new Panel();
                c.gridy = y++;
                c.insets = new Insets(0, 10, 0, 0);
                gridbag.setConstraints(panelList, c);
                Panel global=new Panel();
                Label mouseLabel = new Label("(--- , ---)", Label.LEFT);
                mouseLabel.setFont(monoFont);
                global.add(mouseLabel);
                Panel panelButtonList=new Panel();
                Button list = new Button("List");
                viewer=new ValueViewerCS(plot.getLUT());
                list.addActionListener(viewer);
                plot.setViewer(viewer);
                GridBagLayout gridbag_list = new GridBagLayout();
		GridBagConstraints c_list = new GridBagConstraints();
		panelButtonList.setLayout(gridbag_list);
                c_list.ipadx = 10;
                c_list.ipady = 10;
                gridbag_list.setConstraints(panelButtonList,c_list);
                panelButtonList.add(list);
                global.add(panelButtonList);
                panelList.add(global);
                add(panelList);
                plot.setLabelMouse(mouseLabel);
                
                // panel Save
                Panel panelSave = new Panel();
                c.gridy = y++;
                c.insets = new Insets(0, 10, 0, 0);
                gridbag.setConstraints(panelSave, c);
                Panel global2=new Panel();
                Label saveLabel = new Label("Save graph as GIF: ", Label.LEFT);
                saveLabel.setFont(monoFont);
                global2.add(saveLabel);
                Panel panelButtonSave=new Panel();
                Button save = new Button("Save");
                GridBagLayout gridbag_save = new GridBagLayout();
		GridBagConstraints c_save = new GridBagConstraints();
		panelButtonSave.setLayout(gridbag_save);
                c_save.ipadx = 10;
                c_save.ipady = 10;
                gridbag_save.setConstraints(panelButtonSave,c_save);
                panelButtonSave.add(save);
                global2.add(panelButtonSave);
                save.addActionListener(new SaveGIFCS((Canvas)plot));
                panelSave.add(global2);
                add(panelSave);
                
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
                ih.addActionListener(new InfoHelpViewerCS());
                panelInfoHelp.add(ih);
                
                // panel container
                Panel container=new Panel();
                container.setLayout(new GridLayout(5,1));
                c.gridx = 2;
                c.gridy = 1;
                c.insets = new Insets(10, 0, 0, 10);
                gridbag.setConstraints(container, c);
                container.add(new Panel());
                container.add(panelPreview);
                container.add(panelApply);
                container.add(panelInfoHelp);
                container.add(new Panel());
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
              //IJ.showMessage(imp.getTitle());
              CSTransform cst=new CSTransform(imp, plot.getLUT(), imp.getTitle());
              time=System.currentTimeMillis();
              ImagePlus processed=null;
              if(type==ImagePlus.GRAY8)
              {
                processed=cst.createProcessed8bitImage();                  
              }
              else if(type==ImagePlus.COLOR_256)
                   {
                    processed=cst.createProcessed8bitImage();                   
                   }
                   else if(type==ImagePlus.COLOR_RGB)
                        {
                          processed=cst.createProcessedRGBImage();                         
                        }
              time=System.currentTimeMillis()-time;
              double time_sec=time/1000.0;
              PreviewImageCS prew=new PreviewImageCS(processed,time_sec);
           }
           if(source==apply)
           {
              Undo.setup(Undo.COMPOUND_FILTER, imp);
              CSTransform cst=new CSTransform(imp, plot.getLUT(), imp.getTitle());
              if(type==ImagePlus.GRAY8)
              {
                cst.createProcessed8bitImage(imp);
              }
              else if(type==ImagePlus.COLOR_256)
                   {
                    cst.createProcessed8bitImage(imp);                   
                   }
                   else if(type==ImagePlus.COLOR_RGB)
                        {
                          cst.createProcessedRGBImage(imp);
                        }
              imp.updateAndRepaintWindow();
              imp.changes=true;
              Undo.setup(Undo.COMPOUND_FILTER_DONE, imp);
              close();
           }
        }
}

class ValueViewerCS implements ActionListener
{
  private int[] lut;
    
  public ValueViewerCS(int[] l)
  {
    lut=l;
  }
  
  public void setLUT(int[] l)
  {
    for(int i=0;i<256;i++)
    {
            lut[i]=l[i];
    }
  }
  
  public void actionPerformed(ActionEvent e) 
  {
    StringBuffer sb = new StringBuffer();
    String headings = "X\tY";
    for (int i=0; i<lut.length; i++) 
    {
      sb.append(IJ.d2s(i,0)+"\t"+IJ.d2s(lut[i],0)+"\n");
    }
    TextWindow tw = new TextWindow("Plot Values", headings, sb.toString(), 200, 400);
    tw.show();
  }
}

class InfoHelpViewerCS implements ActionListener
{
  private String description="HELP ABOUT \"Contrast Stretching\"\n\n"+
                                    "This plugin implements the contrast stretching operator for image enhancement in the spatial domain.\n"+
                                    "It allows to enhance contrast of low-contrast images increasing their dynamic range.\n"+
                                    "User can choose his/her preferred transform function simply modifying the location of two control points (r1, s1) and (r2, s2) which allow to change the shape of the transformation.\n"+
                                    "There is only one important constraint: r1<=r2 and s1<=s2. It means that the transform function must be monotonically increasing: this condition preventes the creation of intensity artifacts in the processed image.\n"+
                                    "A particular case of contrast stretching operator is the thresholding operator.\n"+
                                    "Here is a brief description of the steps to do:\n"+
                                    "1) Change the control points in order to obtain your favourite LUT;\n"+
                                    "2) If you want, click on the button \"List\" to view the LUT values;\n"+
                                    "3) Also the possibility to save the graph in GIF format is provided by clicking on the button \"Save\";\n"+
                                    "4) Click on the button \"Preview\" to view as the image would be if it was processed according to the chosen transform curve;\n"+
                                    "5) Click on the button \"Apply\" to definitively apply the current transform to the image;\n\n"+
                                    "ABOUT THE AUTHOR\n\n"+
                                    "This plugin was implemented by Camillo Bosco under Professor Sebastiano Battiato's supervision.\n"+
                                    "It was a part of a project for the Multimedia Course (MSC Program in Computer Science) at University of Catania (ITALY)\n\n"+
                                    "CONTACT INFO\n\n"+
                                    "For further information contact us:\n"+
                                    "Prof. Battiato Sebastiano, Ph.d. in Computer Science, e-mail: battiato@dmi.unict.it, web: http://www.dmi.unict.it/~battiato\n"+
                                    "Bosco Camillo, BS degree in Computer Science, e-mail: camillo.bosco@studenti.unict.it\n\n"+
                                    "LINKS\n\n"+
                                    "IPLab - Image Processing Laboratory - University of Catania (Italy), web: http://www.dmi.unict.it/~iplab\n"+
                                    "Department of Mathematics and Computer Science - University of Catania (Italy), web: http://www.dmi.unict.it/";
  
  public void actionPerformed(ActionEvent e) 
  {
    StringBuffer sb = new StringBuffer();
    sb.append(description);
    TextWindow tw = new TextWindow("About \"Contrast Stretching Operator\"", sb.toString(), 700, 500);
    tw.show();
  }
}

class SaveGIFCS implements ActionListener
{
  private Canvas c;
  
  public SaveGIFCS(Canvas plot)
  {
    c=plot;
  }

  public void actionPerformed(ActionEvent e) 
  {
      Rectangle r = c.getBounds();
      Image image = c.createImage(r.width, r.height);
      Graphics g = image.getGraphics();
      c.paint(g);
      ImagePlus img=new ImagePlus("graph",image);
      ImageConverter conv=new ImageConverter(img);
      conv.convertRGBtoIndexedColor(256);
      new FileSaver(img).saveAsGif();
  }  
}

class CSPlot extends Canvas implements MouseMotionListener, MouseListener
{
	
	private static final int WIDTH=255, HEIGHT=255;
	private double min = 0;
	private double max = 255;
	private Label labMouse;
        private int[] y_values=new int[256];
        private int r1, r2, s1, s2;
        private Ellipse2D ellisse1;
        private Ellipse2D ellisse2;
        private boolean firstBool=false;
        private boolean secondBool=false;
        private ValueViewerCS view;
        
	public CSPlot() 
        {
                r1=96;
                s1=32;
                r2=160;
                s2=224;
                addMouseMotionListener(this);
                addMouseListener(this);
                setSize(WIDTH, HEIGHT);
                //IJ.showMessage("width: "+getSize().width+" \nheight: "+getSize().height);
                findLUT();
	}
        
        public void setViewer(ValueViewerCS v)
        {
          view=v;
        }
        
        public void setControlPoints(int x1,int y1,int x2,int y2)
        {
          r1=x1;
          s1=y1;
          r2=x2;
          s2=y2;          
        }
        
        public int[] getLUT()
        {
          return y_values;
        }
        
        public void setLabelMouse(Label l)
        {
          labMouse=l;  
        }

        public Dimension getPreferredSize() 
        {
          return new Dimension(WIDTH+1, HEIGHT+1);
        }

	public void update(Graphics g) 
        {
		paint(g);
	}

	public void paint(Graphics g) 
        {
          g.setColor(Color.white);
          g.fillRect(0, 0, WIDTH, HEIGHT);
          g.setColor(Color.black);
          g.drawRect(0, 0, WIDTH, HEIGHT);
          g.setColor(Color.red);
          Vector v=new Vector();
          for(int i=0;i<255;i++)
          {
           int[] first_couple=coordsConverter(i,y_values[i]);
           int[] second_couple=coordsConverter((i+1),y_values[i+1]);
           g.drawLine(first_couple[0], first_couple[1], first_couple[0], first_couple[1]);
           g.drawLine(second_couple[0], second_couple[1], second_couple[0], second_couple[1]);
           approximation(g, second_couple[0], second_couple[1], first_couple[0], first_couple[1],v);
          }
          int[] first_couple=coordsConverter(r1,s1);
          int[] second_couple=coordsConverter(r2,s2);
          Graphics2D g2=(Graphics2D)g;
          g2.setColor(Color.blue);
          ellisse1=new Ellipse2D.Double(first_couple[0]-3,first_couple[1]-3,6,6);
          ellisse2=new Ellipse2D.Double(second_couple[0]-3,second_couple[1]-3,6,6);
          g2.fill(ellisse1);
          g2.fill(ellisse2);
          g2.draw(ellisse1);
          g2.draw(ellisse2);
          g=(Graphics)g2;
          g.setColor(Color.red);
        }
        
        public void approximation(Graphics g, int succ_x, int succ_y, int prev_x, int prev_y, Vector v)
        {
          double hypo=Math.sqrt(Math.pow((prev_x-succ_x),2)+Math.pow((prev_y-succ_y),2));
          if(hypo > 1D)
          {
            int middle_x=(prev_x+succ_x)/2;
            int middle_y=(prev_y+succ_y)/2;
            approximation(g, succ_x, succ_y, middle_x, middle_y, v);
            approximation(g, prev_x, prev_y, middle_x, middle_y, v);
            g.drawLine(middle_x, middle_y, middle_x, middle_y);
            v.addElement(new Point(middle_x,middle_y));
          }
        }
        
        private int[] coordsConverter(int x, int y)
        {  
          int[] converted_coord=new int[2];
          converted_coord[0]=x;
          converted_coord[1]=255-y;
          return converted_coord;
        }
        
        public void findLUT()
        {
          int[] y_axis=new int[256];  
          
          if((r1==s1)&&(r2==s2))
          {
           for(int i=0;i<256;i++)
           {
             y_axis[i]=i;
           } 
          }
          else
          {            
            for(int i=0;i<=r1;i++)
            {
              y_axis[i]=findYCoordRect(i,0,0,r1,s1);
            }
            if(r1==r2)y_axis[r1]=s2;
            else
            {
              for(int i=r1+1;i<=r2;i++)
              {
                y_axis[i]=findYCoordRect(i,r1,s1,r2,s2);
              }              
            }
            for(int i=r2;i<=255;i++)
            {
                y_axis[i]=findYCoordRect(i,r2,s2,255,255);
            }
          }  
          setYValues(y_axis);
          if(view!=null)view.setLUT(y_axis);
          repaint();          
        }
        
        public int findYCoordRect(int x,int x0,int y0,int x1,int y1)
        {
          int y;
          if(y0==y1)y=y1;
          else if(x0==x1)y=y1;
               else
               {
                 y=(int)((((float)(x-x0)/(x1-x0))*(y1-y0))+y0);
               }
          return y;
        }
        
        public void setYValues(int[] values)
        {
          for(int i=0;i<256;i++)
          {
            y_values[i]=values[i];
          }
        }
        
        public void mouseMoved(MouseEvent e)
        {
          int x=e.getX();
          int y=e.getY();
          if(ellisse1.contains(x,y))
          {
            firstBool=true;  
          }
          else 
          {
            firstBool=false;
          }
          if(ellisse2.contains(x,y))
          {
                 secondBool=true;  
          }
          else 
          {
            secondBool=false;          
          }
          int[] cartes_coord=coordsConverter(x,y);
          labMouse.setText("("+cartes_coord[0]+" , "+cartes_coord[1]+")");
        }
        
        public void mouseDragged(MouseEvent e)
        {
          int x=e.getX();
          int y=e.getY();
          int[] cartes_coord=coordsConverter(x,y);
      
          if(firstBool)
          {
            if(((cartes_coord[0]<=r2)&&(cartes_coord[1]<=s2))&&((cartes_coord[0]>=0)&&(cartes_coord[1]>=0))&&((cartes_coord[0]<=255)&&(cartes_coord[1]<=255)))
            {
              setControlPoints(cartes_coord[0],cartes_coord[1],r2,s2);
              findLUT();  
            }
          }
          if(secondBool)
          {
            if(((r1<=cartes_coord[0])&&(s1<=cartes_coord[1]))&&((cartes_coord[0]>=0)&&(cartes_coord[1]>=0))&&((cartes_coord[0]<=255)&&(cartes_coord[1]<=255)))
            {
              setControlPoints(r1,s1,cartes_coord[0],cartes_coord[1]);
              findLUT();  
            }
          }
        }

        public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}
        public void mouseClicked(MouseEvent e) {}          
}

class CSTransform
{
      private ImagePlus imp;
      private ImageProcessor ip;
      private int width;
      private int height;
      private int[] lut;
      private String title;        
      
      public CSTransform(ImagePlus image, int[] l, String t) 
      {
	imp=image;
        ip=imp.getProcessor();
        width=ip.getWidth();
        height=ip.getHeight();
        lut=l;
        title=t;
      }
      
      public void setLUT(int[] l)
      {
          for(int i=0;i<256;i++)
          {
            lut[i]=l[i];
          }
      }
      
      public byte[] applyTransform(byte[] pixels)
      {
          byte[] output=new byte[pixels.length];  
        
          for(int i=0;i<pixels.length;i++)
          {
            output[i]=(byte)lut[pixels[i]&0xff];
          }
          return output;          
      }
      
      public int[] applyTransform(int[] pixels)
      {
          int[] output=new int[pixels.length];  
        
          for(int i=0;i<pixels.length;i++)
          {
            int c=pixels[i];
            int red=(c&0xff0000)>>16;
            int green=(c&0x00ff00)>>8;
            int blue=(c&0x0000ff);
            red=lut[red&0xff];
            green=lut[green&0xff];
            blue=lut[blue&0xff];
            output[i]=((red&0xff)<<16)+((green&0xff)<<8)+(blue&0xff);
          }
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
        {
          processed_pixels[i]=output[i];
        }
        //processed.show();
        return processed;
      }
      
      public void createProcessed8bitImage(ImagePlus img)
      {
        byte[] processed_pixels = (byte[])(img.getProcessor()).getPixels();
        byte[] output=applyTransform(processed_pixels);
        for (int i=0; i<output.length; i++) 
        {
          processed_pixels[i]=output[i];
        }
      }
      
      public ImagePlus createProcessedRGBImage()
      {
        ImageProcessor processed_ip = ip.crop();
        ImagePlus processed= imp.createImagePlus();
        processed.setProcessor(title, processed_ip);
        int[] processed_pixels = (int[])processed_ip.getPixels();
        int[] output= applyTransform(processed_pixels);
        for (int i=0; i<output.length; i++) 
        {
          processed_pixels[i]=output[i];
        }
        return processed;
      }
      
      public void createProcessedRGBImage(ImagePlus img)
      {
        int[] processed_pixels = (int[])(img.getProcessor()).getPixels();
        int[] output= applyTransform(processed_pixels);
        for (int i=0; i<output.length; i++) 
        {
          processed_pixels[i]=output[i];
        }
      }
}

class PreviewImageCS implements ActionListener
{
      private ImageCanvas canvas;
      private Panel panelTime;
      private ImagePlus im;
        
      public PreviewImageCS(ImagePlus img, double time)
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