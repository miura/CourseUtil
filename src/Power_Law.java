import ij.IJ;
import ij.ImagePlus;
import ij.Undo;
import ij.WindowManager;
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

/** This plugin implements the power law operator
@author Bosco Camillo-BS degree in Computer Science 
        Advisor: Prof. Battiato Sebastiano
        Organization: University of Catania - ITALY
*/
public class Power_Law extends PlugInFrame implements ActionListener, AdjustmentListener, TextListener
{

        private Font monoFont = new Font("Monospaced", Font.PLAIN, 12);
        private Font sans = new Font("SansSerif", Font.BOLD, 12);
        private float valueGamma=0.3f;
        private float valueScale=1.0f;
        private TextField gammaField;
        private TextField scaleField;  
        private ValueViewer viewer;
        private PowerPlot plot;
        private Button draw;
        private Button preview;
        private Button apply;
        private long time;
        private ImageProcessor ip;  
        private ImagePlus imp;
        private int type;
        private Label sliderLab;
        private Scrollbar slider;
        
        public Power_Law() 
        {
		super("Power Law Transform");
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
                  IJ.error("Error","Power Law requires an image of type\n \n8-bit grayscale\n8-bit indexed color\nRGB color\n");
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
                Label title= new Label("Power-law Transform", Label.CENTER);
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
                plot = new PowerPlot(valueGamma,valueScale);
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
                viewer=new ValueViewer(plot.getLUT());
                list.addActionListener(viewer);
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
                save.addActionListener(new SaveGIF((Canvas)plot));
                panelSave.add(global2);
                add(panelSave);
                
                // panel gamma
                Panel panelGamma = new Panel();
                Label gammaLab = new Label("Gamma Value: ");
                gammaLab.setFont(new Font("SansSerif", Font.PLAIN, 12));
                panelGamma.add(gammaLab);
                int digits=1;
                gammaField = new TextField(IJ.d2s(valueGamma, digits),3);
                gammaField.addTextListener(this);
                panelGamma.add(gammaField);
                
                
                // panel scale
                Panel panelScale = new Panel();
                Label scaleLab = new Label("Scale Value: ");
                scaleLab.setFont(new Font("SansSerif", Font.PLAIN, 12));
                panelScale.add(scaleLab);
                digits=0;
                scaleField = new TextField(IJ.d2s(valueScale, digits),3);
                scaleField.addTextListener(this);
                panelScale.add(scaleField);
                
                // panel Draw
                Panel panelDraw = new Panel();
                draw = new Button("Draw");
                draw.addActionListener(this);
                panelDraw.add(draw);
                
                // panel Preview & Apply
                Panel panelButtons = new Panel();
                preview = new Button("Preview");
                preview.addActionListener(this);
                panelButtons.add(preview);
                apply = new Button("Apply");
                apply.addActionListener(this);
                panelButtons.add(apply);
                
                // panel Info & Help
                Panel panelInfoHelp = new Panel();
                Button ih = new Button("Help & Info");
                ih.addActionListener(new InfoHelpViewer());
                panelInfoHelp.add(ih);
                
                // panel container
                Panel container=new Panel();
                container.setLayout(new GridLayout(5,1));
                c.gridx = 2;
                c.gridy = 1;
                c.insets = new Insets(10, 0, 0, 10);
                gridbag.setConstraints(container, c);
                container.add(panelGamma);
                container.add(panelScale);
                container.add(panelDraw);
                container.add(panelButtons);
                container.add(panelInfoHelp);
                add(container);
                
                // adding slider's label
                sliderLab = new Label("Gamma: 0.3",Label.CENTER);
                c.gridy = 2;
                c.insets = new Insets(0, 10, 0, 4);
                gridbag.setConstraints(sliderLab, c);
                add(sliderLab);
                
                // adding slider
                slider = new Scrollbar(Scrollbar.HORIZONTAL, 30, 1, 1, 341);
                slider.addAdjustmentListener(this);
                c.gridy = 3;
                c.insets = new Insets(3, 20, 20, 5);
                gridbag.setConstraints(slider, c);
                slider.setUnitIncrement(1);
                add(slider);
                
                // - - - - - - - - - - - - - - - - - -
                pack();
		GUI.center(this);
		show();
        }
        
        public void actionPerformed(ActionEvent e)
        {
           Object source=e.getSource();
           if(source==draw)
           {
             if(checkGamma()&&checkScale())
             {
                plot.findLUT(valueGamma,valueScale);
                viewer.setLUT(plot.getLUT());
                if(valueGamma>1)
                {
                  Float f=new Float(IJ.d2s(valueGamma,1));
                  int i=(int)(10.0f*(valueGamma-1.0f)+100f);
                  slider.setValue(i);
                  sliderLab.setText("Gamma: "+IJ.d2s(valueGamma,1));  
                }
                else 
                {
                  Float f=new Float(IJ.d2s(valueGamma,2));
                  int i=(int)(f.floatValue()*100);
                  slider.setValue(i);
                  sliderLab.setText("Gamma: "+IJ.d2s(valueGamma,2));  
                }
             }
           }
           if(source==preview)
           {
              //IJ.showMessage(imp.getTitle());
              RaiseToPower rp=new RaiseToPower(imp, plot.getLUT(), imp.getTitle());
              time=System.currentTimeMillis();
              ImagePlus processed=null;
              if(type==ImagePlus.GRAY8)
              {
                processed=rp.createProcessed8bitImage();                  
              }
              else if(type==ImagePlus.COLOR_256)
                   {
                    processed=rp.createProcessed8bitImage();                   
                   }
                   else if(type==ImagePlus.COLOR_RGB)
                        {
                          processed=rp.createProcessedRGBImage();                         
                        }
              time=System.currentTimeMillis()-time;
              double time_sec=time/1000.0;
              PreviewImage prew=new PreviewImage(processed,time_sec);
           }
           if(source==apply)
           {
              Undo.setup(Undo.COMPOUND_FILTER, imp);
              RaiseToPower rp=new RaiseToPower(imp, plot.getLUT(), imp.getTitle());
              if(type==ImagePlus.GRAY8)
              {
                rp.createProcessed8bitImage(imp);
              }
              else if(type==ImagePlus.COLOR_256)
                   {
                    rp.createProcessed8bitImage(imp);                   
                   }
                   else if(type==ImagePlus.COLOR_RGB)
                        {
                          rp.createProcessedRGBImage(imp);
                        }
              imp.updateAndRepaintWindow();
              imp.changes=true;
              Undo.setup(Undo.COMPOUND_FILTER_DONE, imp);
              close();
           }
        }
        
        private boolean checkGamma()
        {
          if(valueGamma<=0)
          {  
            IJ.error("Error","You must enter a value greater\n than zero for gamma");    
            return false;
          }
          if(valueGamma>25)
          {  
            IJ.error("Error","You must enter a value less \nor equal than 25 for gamma");    
            return false;
          }
          String value=java.lang.Float.toString(valueGamma);
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
        
        private boolean checkScale()
        {
          if(valueScale<=0)
          {  
            IJ.error("Error","You must enter a value greater than zero for scale factor");    
            return false;
          }
          return true;           
        }
        
        public void adjustmentValueChanged(AdjustmentEvent e) 
        {
          int sliderValue = slider.getValue();
          
          if(sliderValue<=100)
          {
            valueGamma=sliderValue/100.0f;
          }
          else valueGamma=((sliderValue-100)/10.0f)+1.0f;
          
          if(checkScale())
          {
                plot.findLUT(valueGamma,valueScale);
                viewer.setLUT(plot.getLUT());
                if(valueGamma>=1)
                {
                  sliderLab.setText("Gamma: "+IJ.d2s(valueGamma,1));  
                }
                else sliderLab.setText("Gamma: "+IJ.d2s(valueGamma,2));  
                gammaField.setText(new Float(valueGamma).toString());
          }
        }
        
        public void textValueChanged(TextEvent e) 
        {
           TextComponent tc = (TextComponent)e.getSource();
           if(tc==gammaField)
           {
             try
             {
              valueGamma=Float.parseFloat(gammaField.getText());  
              //IJ.showMessage("valueGamma: "+valueGamma);                
             }
             catch(NumberFormatException exp)
             {
               IJ.error("Number Format Exception","You must enter a number");  
             }
           }
           if(tc==scaleField)
           {
             try
             {
              valueScale=Float.parseFloat(scaleField.getText());  
              //IJ.showMessage("valueScale: "+valueScale);                
             }
             catch(NumberFormatException exp)
             {
               IJ.error("Number Format Exception","You must enter a number");  
             }
           }
        }
}

class ValueViewer implements ActionListener
{
  private int[] lut;
    
  public ValueViewer(int[] l)
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

class InfoHelpViewer implements ActionListener
{
  private String description="HELP ABOUT \"POWER LAW\"\n\n"+
                                    "This plugin implements the exponential transform operator for image enhancement in the spatial domain.\n"+
                                    "It is defined as follows:\n"+
                                    "s=c*(r^gamma)\n"+
                                    "where c and gamma are positive constants, r is the value of the input pixel and s is the corresponding value of the output pixel.\n"+
                                    "Here is a brief description of the steps to do:\n"+
                                    "1) Choose your preferred float values for gamma (with 0 < gamma <=25) and c (scale factor) with c!=0 and c > 0;\n"+
                                    "2) Click on the button \"Draw\" to draw the graph for the chosen parameters values;\n"+
                                    "3) If you want, click on the button \"List\" to view the LUT values;\n"+
                                    "4) Also the possibility to save the graph in GIF format is provided by clicking on the button \"Save\";\n"+
                                    "5) Click on the button \"Preview\" to view as the image would be if it was processed according to the chosen transform curve;\n"+
                                    "6) Click on the button \"Apply\" to definitively apply the current transform to the image;\n"+
                                    "You can also draw the chosen curve using the slider which allows to select the gamma value.\n\n"+
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
    TextWindow tw = new TextWindow("About \"Power Law\"", sb.toString(), 700, 500);
    tw.show();
  }
}

class SaveGIF implements ActionListener
{
  private Canvas c;
  
  public SaveGIF(Canvas plot)
  {
    c=plot;
  }

  public void actionPerformed(ActionEvent e) 
  {
      Rectangle r = c.getBounds();
      Image image = c.createImage(r.width, r.height);
      Graphics g = image.getGraphics();
      c.paint(g);
      ImagePlus img = new ImagePlus("graph",image);
      ImageConverter conv=new ImageConverter(img);
      conv.convertRGBtoIndexedColor(256);
      new FileSaver(img).saveAsGif();
  }  
}

class PowerPlot extends Canvas implements MouseMotionListener
{
	
	private static final int WIDTH=255, HEIGHT=255;
	private double min = 0;
	private double max = 255;
	private Label labMouse;
        private int[] y_values=new int[256];
          
	public PowerPlot(float g, float s) 
        {
                addMouseMotionListener(this);
                setSize(WIDTH, HEIGHT);
                //IJ.showMessage("width: "+getSize().width+" \nheight: "+getSize().height);
                findLUT(g,s);
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
          for(int i=0;i<255;i++)
          {
            int[] first_couple=coordsConverter(i,y_values[i]);
            int[] second_couple=coordsConverter((i+1),y_values[i+1]);
            g.drawLine(first_couple[0], first_couple[1], first_couple[0], first_couple[1]);
            g.drawLine(second_couple[0], second_couple[1], second_couple[0], second_couple[1]);
            approximationCurve(g, second_couple[0], second_couple[1], first_couple[0], first_couple[1]);             
          }
        }
        
        public void approximationCurve(Graphics g, int succ_x, int succ_y, int prev_x, int prev_y)
        {
          double hypo=Math.sqrt(Math.pow((prev_x-succ_x),2)+Math.pow((prev_y-succ_y),2));
          if(hypo > 1D)
          {
            int middle_x=(prev_x+succ_x)/2;
            int middle_y=(prev_y+succ_y)/2;
            approximationCurve(g, succ_x, succ_y, middle_x, middle_y);
            approximationCurve(g, prev_x, prev_y, middle_x, middle_y);
            g.drawLine(middle_x, middle_y, middle_x, middle_y);
          }
        }
        
        private int[] coordsConverter(int x, int y)
        {  
          int[] converted_coord=new int[2];
          converted_coord[0]=x;
          converted_coord[1]=255-y;
          return converted_coord;
        }
        
        public void findLUT(float gamma, float scale)
        {
          int[] y_axis=new int[256];  
          
          for(int i=0;i<256;i++)
          {
            int processed = (int)(Math.pow((float)(i / 255F), gamma) * 255D);
            processed=(int)((processed*scale));
            if (processed > 255) processed = 255;
            if (processed < 0) processed = 0;
            y_axis[i]=processed;
          }
          setYValues(y_axis);
          repaint();          
        }
        
        public void setYValues(int[] values)
        {
          //String str="";
          for(int i=0;i<256;i++)
          {
            y_values[i]=values[i];
            //str=str+"y_values["+i+"]= "+y_values[i]+"\n";            
          }
          //IJ.showMessage(str);
        }
        
        public void mouseMoved(MouseEvent e)
        {
          //IJ.showStatus("(x= "+e.getX()+"y= "+e.getY()+")");
          int x=e.getX();
          int y=e.getY();
          int[] cartes_coord=coordsConverter(x,y);
          labMouse.setText("("+cartes_coord[0]+" , "+cartes_coord[1]+")");
        }
        
        public void mouseDragged(MouseEvent e){}  

}

class RaiseToPower
{
      private ImagePlus imp;
      private ImageProcessor ip;
      private int width;
      private int height;
      private int[] lut;
      private String title;        
      
      public RaiseToPower(ImagePlus image, int[] l, String t) 
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

class PreviewImage implements ActionListener
{
      private ImageCanvas canvas;
      private Panel panelTime;
      private ImagePlus im;
        
      public PreviewImage(ImagePlus img, double time)
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