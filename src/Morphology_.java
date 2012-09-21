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
import javax.swing.*;



//------------------------------------------------------------------------------------
//for binary
//structuralelement: 0=background ; <>0 foreground
//image : 0=backg. 255=foreground
//-----------------------------------------------------------------------------------------
public class Morphology_ extends PlugInFrame implements ActionListener, ChangeListener
{
	static int SET=1000;//costante per distinguere i pixel settati dal disegno o costruzione di elemento strutturale standard, dai pixel caricatida file...

	private JLabel structLabel;
	private JLabel operationLabel;
	private JLabel sizeLabel;
	private JLabel sliderValueLabel;
	private JButton applyButton,drawStructuralElementButton,binaryzeButton,infoButton,loadStructuralElementButton;
	JComboBox  selectStructuraElementCombo;
	JComboBox  selectOperationCombo;
	JComboBox sizeCombo;
	private ButtonGroup group;
	private JRadioButton radioBinaryMode,radioGrayMode;
	private JSlider slider;

	private ImageProcessor ip;
	private ImagePlus imp;
	private int type;

	private DrawPanel drawPanel;
	private int operatorToApply=0,sizeToApply=3,maskToApply=0;//sono valori che vengono settati dai vari combobox per stabilire l'operatore da applicare la dim e maschra.
	private boolean applyUserDefynedMask=false;//è vera quando applico un operatore disegnato dall'utente
	private boolean applyLoadedMask=false;//è vera quando applico un operatore caricato da file
	private boolean binaryOperation=true;
	private StructuringElement se;

	public Morphology_()
	{
		super("Morphology_");
		setResizable(false);
	}

	public void run(String arg)
	{
		imp = WindowManager.getCurrentImage();//recupero l'ImagePlus chè la rappresentazionedell'immagine corrente
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

		if(type!=imp.GRAY8)
		{
			IJ.beep();
			IJ.error("Error","Morphology_ requires an image of type\n \n8-bit grayscale\n8-bit");
			return;
		}
		//-----------------------Creo l'elemento strutturale-----------------------
		se=new StructuringElement(maskToApply,sizeToApply);


		//-----------------------CREO L INTERFACCIA--------------------------------
		JPanel panel1=new JPanel();
		operationLabel=new JLabel("Select Operation");
		selectOperationCombo=new JComboBox();
		selectOperationCombo.addItem("Dilate");
		selectOperationCombo.addItem("Erode");
		selectOperationCombo.addItem("Closing");
		selectOperationCombo.addItem("Opening");
		selectOperationCombo.addItem("Boundary Extracion");
		selectOperationCombo.addItem("Smoothing");
		selectOperationCombo.addItem("Hit-Or-Miss Transform");
		selectOperationCombo.addItem("Gradient");
		selectOperationCombo.addActionListener(this);

		panel1.add(operationLabel);
		panel1.add(selectOperationCombo);
		group=new ButtonGroup();
		radioBinaryMode=new JRadioButton("binary");
		radioBinaryMode.addActionListener(this);
		radioGrayMode=new JRadioButton("gray");
		radioGrayMode.addActionListener(this);
		radioBinaryMode.setSelected(true);
		binaryOperation=true;
		group.add(radioBinaryMode);
		group.add(radioGrayMode);
		panel1.add(radioBinaryMode);
		panel1.add(radioGrayMode);
		panel1.setBorder(BorderFactory.createEtchedBorder());

		JPanel panel2=new JPanel();
		structLabel=new JLabel("Select Structuring Element");
		selectStructuraElementCombo=new JComboBox();

		selectStructuraElementCombo.addItem("Circle");
		selectStructuraElementCombo.addItem("Square");
		selectStructuraElementCombo.addItem("Cross");
		selectStructuraElementCombo.addItem("Vline");
		selectStructuraElementCombo.addItem("Hline");
		selectStructuraElementCombo.addActionListener(this);

		sizeLabel=new JLabel("Diameter");
		sizeCombo=new JComboBox();
		sizeCombo.addItem("3");
		sizeCombo.addItem("5");
		sizeCombo.addItem("7");
		sizeCombo.addItem("9");
		sizeCombo.addItem("11");
		sizeCombo.addItem("13");
		sizeCombo.addItem("15");
		sizeCombo.addItem("17");
		sizeCombo.setEditable(true);
		sizeCombo.addActionListener(this);


		panel2.add(structLabel);
		panel2.add(selectStructuraElementCombo);
		panel2.add(sizeLabel);
		panel2.add(sizeCombo);
		panel2.setBorder(BorderFactory.createEtchedBorder());

		JPanel panel3=new JPanel();
		applyButton=new JButton("Apply");
		applyButton.addActionListener(this);
		binaryzeButton=new JButton("Binaryze");
		binaryzeButton.addActionListener(this);
		infoButton=new JButton("Info");
		infoButton.addActionListener(this);

		panel3.add(binaryzeButton);
		panel3.add(infoButton);
		panel3.add(applyButton);
		panel3.setBorder(BorderFactory.createEtchedBorder());

		JPanel panel4=new JPanel();
		panel4.setLayout(new BorderLayout());
		drawStructuralElementButton=new JButton("Draw Structuring Element");
		loadStructuralElementButton=new JButton("Load Structuring Element");
		drawStructuralElementButton.addActionListener(this);
		loadStructuralElementButton.addActionListener(this);
		JPanel p=new JPanel();
		p.add(drawStructuralElementButton);
		p.add(loadStructuralElementButton);

		panel4.add(p,BorderLayout.NORTH);
		//panel4.add(drawStructuralElementButton,BorderLayout.NORTH);

		panel4.setBorder(BorderFactory.createEtchedBorder());
		panel4.setPreferredSize(new Dimension(250,250));
		drawPanel=new DrawPanel(3,se.getMask());
		panel4.add(drawPanel,BorderLayout.CENTER);


		JPanel panelSlider=new JPanel();
		slider=new JSlider(0,0,255,0);
		slider.setValue(255);
		slider.addChangeListener(this);
		slider.setEnabled(false);
		panelSlider.add(slider);
		sliderValueLabel=new JLabel("255  ");
		panelSlider.add(sliderValueLabel);

		panel4.add(panelSlider,BorderLayout.SOUTH);


		Panel container=new Panel();
		container.setLayout(new BorderLayout());
		JPanel panelA=new JPanel();
		panelA.setLayout(new GridLayout(2,1));
		panelA.add(panel1);
		panelA.add(panel2);
		container.add(panelA,BorderLayout.NORTH);
		container.add(panel4,BorderLayout.CENTER);
		container.add(panel3,BorderLayout.SOUTH);
		add(container);

		pack();
		GUI.center(this);
		show();
	}//run
	//-------------------ActionPerformed----------------------------
	public void actionPerformed(ActionEvent e)
	{
		Object source=e.getSource();


		if(source==radioBinaryMode)
		{
			slider.setEnabled(false);
			slider.setValue(255);
			sliderValueLabel=new JLabel("255  ");
		}

		if(source==radioGrayMode)
			slider.setEnabled(true);

		if(source==selectStructuraElementCombo)
		{
			maskToApply=selectStructuraElementCombo.getSelectedIndex();
			//se=new StructuringElement(maskToApply,sizeToApply);
			se.setShapeAndSize(maskToApply,sizeToApply);

			drawPanel.setGrid(se.getMask(),sizeToApply);
			drawPanel.setEnable(false);
			this.applyUserDefynedMask=false;
		}

		if(source==selectOperationCombo)
		{
			operatorToApply=selectOperationCombo.getSelectedIndex();
		}
		if(source==sizeCombo)
		{
			String item=(String)sizeCombo.getSelectedItem();
			try
			{
				if(Integer.parseInt(item)>100)
				{
					IJ.error("Error","Dimension not valid: max 100x100");
				}
				else
				{
					sizeToApply=Integer.parseInt(item);
					se.setShapeAndSize(maskToApply,sizeToApply);
					drawPanel.setGrid(se.getMask(),sizeToApply);
					drawPanel.setEnable(false);
					this.applyUserDefynedMask=false;
				}
			}
			catch(Exception ex)
			{
				IJ.error("Error","Dimensione not valid!");
			}
		}
		if(source==drawStructuralElementButton)
		{
			drawPanel.reset(slider.getValue());
			drawPanel.setEnable(true);
			this.applyUserDefynedMask=true;
		}

		if(source==loadStructuralElementButton)
		{
			slider.setEnabled(false);
			String directory = "";
			String fileName="";
			String name=null;
			OpenDialog od = new OpenDialog("Load File...",null);
			name = od.getFileName();
			if (name==null)
				return;
			directory = od.getDirectory();
			fileName = od.getFileName();

			//carico l'immagine dafile
			Opener opener=new Opener();
			ImagePlus imageStructElem=opener.openImage(directory,fileName);

			ImageProcessor imageStructElemIp=imageStructElem.getProcessor();
			byte[] mask=(byte[])imageStructElemIp.getPixels();
			int height=imageStructElem.getHeight();
			int width=imageStructElem.getWidth();

			if(height>100 || width>100)
				IJ.showMessage("Image too large; max 100x100");
			else
			{
				se.setMask(mask,height,width);
				drawPanel.setGrid(se.getMask(),se.getDiameter());
				drawPanel.setEnable(false);
				this.applyUserDefynedMask=false;
			}
		}

		if(source==binaryzeButton)
		{
			imp = WindowManager.getCurrentWindow().getImagePlus();//recupero imagePlus ed imageProcessor così che funzioni anche nel caso che la finestra cambi.
			ip = imp.getProcessor();
			this.processImage(-2);
			this.binaryOperation=true;
		}

		if(source==infoButton)
		{
			String description="ABOUT  \"Morphological Operators\"\n\n"+
			"This plugin implements the principal morphological operators, dilation erosion, opening, colosing, hit-or-miss transform\n"+
			"and morphological algorithm such as smoothing, boundary extraction, gradient.\n"+
			"It allows the selection of different type of structuring element. Otherwise it's possible to draw the structurin element or load it from a file.\n"+
			"The selected morphological operator is applyed with the selected mode, binary, for binary images, or gray scale, for gray scale images.\n";
			StringBuffer sb = new StringBuffer();
			sb.append(description);
			TextWindow tw = new TextWindow("About \"Contrast Stretching Operator\"", sb.toString(), 700, 500);
			tw.show();
		}

		if(source==applyButton)
		{
			if(radioBinaryMode.isSelected())
				binaryOperation=true;
			else
				binaryOperation=false;


			imp = WindowManager.getCurrentWindow().getImagePlus();//recupero imagePlus ed imageProcessor così che funzioni anche nel caso che la finestra cambi.
			ip = imp.getProcessor();

			//aggiorno l'elemento strutturale con il contenuto del pannello che lo visualizza

			type = imp.getType();
			if(type!=imp.GRAY8)
			{
				IJ.beep();
				IJ.error("Error","Morphology_ requires an image of type\n \n8-bit grayscale\n8-bit");
				return;
			}
			else
			{
				se.setMask(drawPanel.getMask(),drawPanel.getGrayValue());
				this.processImage(operatorToApply);
			}
		}
	}
	//-------------StateChanged-----------------------------------------------------------------
	public void stateChanged(ChangeEvent c)
	{
		int value=slider.getValue();
		sliderValueLabel.setText(""+value);
		drawPanel.setColor(value);
	}
	//------------------------------------------------------------------------------

	ImagePlus processImage(int operation)
	{
		//creo un nuovo Imageprocessor per utilizzarlo nella nuova finestra
		ImageProcessor processed_ip=this.ip.createProcessor(this.ip.getWidth(),this.ip.getHeight());
		copyPixels(ip,processed_ip);//copio i pixel dell'attuale imageProcessor su quello appena creato.
		processed_ip.setRoi(ip.getRoi());//copio nche la ROI.

		if (operation==-2) binaryzeImage((byte[])(processed_ip.getPixels()),processed_ip);
		else if(binaryOperation==false)
		{
			if (operation==0) dilateGray((byte[])(processed_ip.getPixels()),processed_ip,this.se);
			else if (operation==1) erodeGray((byte[])(processed_ip.getPixels()),processed_ip,this.se);
			else if (operation==2) closing((byte[])(processed_ip.getPixels()),processed_ip,this.se);
			else if (operation==3) opening((byte[])(processed_ip.getPixels()),processed_ip,this.se);
			else if (operation==4) boundaryExtraction((byte[])(processed_ip.getPixels()),processed_ip,this.se);
			else if (operation==5) smoothing((byte[])(processed_ip.getPixels()),processed_ip,this.se);
			else if (operation==7) gradient((byte[])(processed_ip.getPixels()),processed_ip,this.se);
		}
		else
		{
			if (operation==0) dilateBinary((byte[])(processed_ip.getPixels()),processed_ip,this.se);
			else if (operation==1) erodeBinary((byte[])(processed_ip.getPixels()),processed_ip,this.se);
			else if (operation==2) closing((byte[])(processed_ip.getPixels()),processed_ip,this.se);
			else if (operation==3) opening((byte[])(processed_ip.getPixels()),processed_ip,this.se);
			else if (operation==4) boundaryExtraction((byte[])(processed_ip.getPixels()),processed_ip,this.se);
			else if (operation==5) smoothing((byte[])(processed_ip.getPixels()),processed_ip,this.se);
			else if (operation==6) hitOrMiss((byte[])(processed_ip.getPixels()),processed_ip,this.se);
			else if (operation==7) gradient((byte[])(processed_ip.getPixels()),processed_ip,this.se);
		}

		//creo una nuova finestra che conterrà l'immagine elabirata
		ImagePlus im=new ImagePlus("",processed_ip);
		ImageWindow imgWin=new ImageWindow(im);
		imgWin.show();


		return null;
	}
	//-----------------------------------------------------------------------------------
	public void copyPixels(ImageProcessor sourceIp,ImageProcessor destIp)
	{
		byte[] pixelsSource=(byte[])sourceIp.getPixels();
		byte[] pixelsDest=(byte[])destIp.getPixels();
		for(int i=0;i<pixelsSource.length;i++)
			pixelsDest[i]=pixelsSource[i];
	}
	//--------DILATE GRAY----------------------------------------------------------------
	public void dilateGray(byte[] pixels,ImageProcessor ip ,StructuringElement se)
	{

		//---dimensioni dell'immagine

		int imgWidth=imp.getWidth();
		int imgHeight=imp.getHeight();

		Rectangle r=ip.getRoi();
		byte[][]img=new byte[imgHeight][imgWidth];
		byte[][]imgOut=new byte[r.height][r.width];

		for(int i=0;i<imgHeight;i++)
		{
			for(int j=0;j<imgWidth;j++)
			{
				img[i][j]=pixels[i*imgWidth+j];
			}
		}

		int val=(img[r.x][r.y]&0xff);
		int raggio=se.getDiameter()/2;

		for(int i=r.y;i<(r.y+r.height);i++)
		{
			for(int j=r.x;j<(r.x+r.width);j++)
			{
				int s=i;
				int t=j;
				int max=0;
				for(int x=-raggio;x<=raggio;x++)
					for(int y=-raggio;y<=raggio;y++)
						// if( se.get(x+raggio,y+raggio)!=0 )
					{
						if((s-x)>=r.y && (s-x)<(r.y+r.height) && (t-y)>=r.x && (t-y)<(r.x+r.width))
						{
							if( se.get(x+raggio,y+raggio)!=0 && max<((img[s-x][t-y]&0xff)+se.get(x+raggio,y+raggio)) ) max=(img[s-x][t-y]&0xff)+se.get(x+raggio,y+raggio);
						}
						if(max>255) max=255;
					}
				imgOut[s-r.y][t-r.x]=(byte)(max);
			}
		}

		//aggiorno i pixel dell'imageProcessor che verrà visualizzato sulla nuova finestra.
		for(int i=r.y;i<(r.y+r.height);i++)
		{
			for(int j=r.x;j<(r.x+r.width);j++)
			{
				pixels[i*imgWidth+j]=imgOut[i-r.y][j-r.x];
			}
		}
	}
	//----------------ERODE GRAY--------------------------------------------------------------------
	public void erodeGray(byte[] pixels,ImageProcessor ip ,StructuringElement se)
	{
		//IJ.showMessage("In Dilate");
		//byte[] pixels=(byte[])ip.getPixels();

		//---dimensioni dell'immagine
		int imgWidth=imp.getWidth();
		int imgHeight=imp.getHeight();
		byte[][]img=new byte[imgHeight][imgWidth];

		Rectangle r=ip.getRoi();
		byte[][]imgOut=new byte[r.height][r.width];

		for(int i=0;i<imgHeight;i++)
		{
			for(int j=0;j<imgWidth;j++)
			{
				img[i][j]=pixels[i*imgWidth+j];
			}
		}

		int raggio=se.getDiameter()/2;

		for(int i=r.y;i<(r.y+r.height);i++)
		{
			for(int j=r.x;j<(r.x+r.width);j++)
			{
				int s=i;
				int t=j;
				int min=300;
				for(int x=-raggio;x<=raggio;x++)
					for(int y=-raggio;y<=raggio;y++)
						if( se.get(x+raggio,y+raggio)!=0 )
						{
							if((s+x)>=r.y && (s+x)<(r.y+r.height) && (t+y)>=r.x && (t+y)<(r.x+r.width))
							{
								if( min>((img[s+x][t+y]&0xff)+se.get(x+raggio,y+raggio)) ) min=(img[s+x][t+y]&0xff)-se.get(x+raggio,y+raggio);
							}

							if(min<0) min=0;
						}
				imgOut[s-r.y][t-r.x]=(byte)(min);
			}
		}

		for(int i=r.y;i<(r.y+r.height);i++)
		{
			for(int j=r.x;j<(r.x+r.width);j++)
			{
				pixels[i*imgWidth+j]=imgOut[i-r.y][j-r.x];
			}
		}
	}
	//--------DILATE BINARY----------------------------------------------------------------
	public void dilateBinary(byte[] pixels,ImageProcessor ip ,StructuringElement se)
	{
		//IJ.showMessage("In Dilate");


		//---dimensioni dell'immagine
		int imgWidth=imp.getWidth();
		int imgHeight=imp.getHeight();
		byte[][]img=new byte[imgHeight][imgWidth];

		Rectangle r=ip.getRoi();
		byte[][]imgOut=new byte[r.height][r.width];

		for(int i=0;i<imgHeight;i++)
		{
			for(int j=0;j<imgWidth;j++)
			{
				img[i][j]=pixels[i*imgWidth+j];
			}
		}

		int raggio=se.getDiameter()/2;
		boolean intersection=false;
		for(int i=r.y;i<(r.y+r.height);i++)
		{
			for(int j=r.x;j<(r.x+r.width);j++)
			{
				int s=i;
				int t=j;
				int max=0;
				intersection=false;
				for(int x=-raggio;x<=raggio;x++)
					for(int y=-raggio;y<=raggio;y++)
					{
						//if((s-x)>=0 && (s-x)<r.height && (t-y)>=0 && (t-y)<r.width)
						if((s-x)>=r.y && (s-x)<(r.y+r.height) && (t-y)>=r.x && (t-y)<(r.x+r.width))
							if( (img[s-x][t-y]&0xff)>0 && se.get(x+raggio,y+raggio)!=0) intersection=true; //mg[s-x][t-y]&0xff)>0 è foreground ; se.get(x+raggio,y+raggio)!=0 è foreground
					}
				if(intersection)
					imgOut[s-r.y][t-r.x]=(byte)255;
				else imgOut[s-r.y][t-r.x]=(byte)0;
			}
		}

		for(int i=r.y;i<(r.y+r.height);i++)
		{
			for(int j=r.x;j<(r.x+r.width);j++)
			{
				pixels[i*imgWidth+j]=imgOut[i-r.y][j-r.x];
			}
		}
	}
	//--------ERODE BINARY----------------------------------------------------------------
	public void erodeBinary(byte[] pixels,ImageProcessor ip ,StructuringElement se)
	{

		//byte[] pixels=(byte[])ip.getPixels();
		//---dimensioni dell'immagine
		int imgWidth=imp.getWidth();
		int imgHeight=imp.getHeight();
		byte[][]img=new byte[imgHeight][imgWidth];

		Rectangle r=ip.getRoi();
		byte[][]imgOut=new byte[r.height][r.width];

		for(int i=0;i<imgHeight;i++)
		{
			for(int j=0;j<imgWidth;j++)
			{
				img[i][j]=pixels[i*imgWidth+j];
			}
		}

		int raggio=se.getDiameter()/2;
		//IJ.showMessage("In Erode binary \nraggio="+raggio+"  \ndiametro="+se.getDiameter());
		boolean contained=true;

		for(int i=r.y;i<(r.y+r.height);i++)
		{
			for(int j=r.x;j<(r.x+r.width);j++)
			{
				int s=i;
				int t=j;
				int max=0;
				contained=true;
				for(int x=-raggio;x<=raggio;x++)
					for(int y=-raggio;y<=raggio;y++)
					{
						if((s+x)>=r.y && (s+x)<(r.y+r.height) && (t+y)>=r.x && (t+y)<(r.x+r.width))
						{
							if(  se.get(x+raggio,y+raggio)!=0 && (img[s+x][t+y]&0xff)<250 ) contained=false; //metto <250 e non !=255 perche alcune immagini sebbene a 2 livelli possono avere ad es tutti 254 aql posto di 255...;se.get(x+raggio,y+raggio)!=0 è foreground; (img[s+x][t+y]&0xff)<250 è background
						}
						else contained=false;
					}
				if(contained)
					imgOut[s-r.y][t-r.x]=(byte)255;
				else imgOut[s-r.y][t-r.x]=(byte)0;
			}
		}

		for(int i=r.y;i<(r.y+r.height);i++)
		{
			for(int j=r.x;j<(r.x+r.width);j++)
			{
				pixels[i*imgWidth+j]=imgOut[i-r.y][j-r.x];
			}
		}
	}
	//---------------OPENING-------------------------------------------------------------------
	public void opening(byte[] pixels,ImageProcessor ip ,StructuringElement se)
	{
		if(binaryOperation==false)
		{
			erodeGray(pixels,ip,se);
			dilateGray(pixels,ip,se);
		}
		else
		{
			erodeBinary(pixels,ip,se);
			dilateBinary(pixels,ip,se);
		}
	}
	//----------------CLOSING------------------------------------------------------------------
	public void closing(byte[] pixels,ImageProcessor ip ,StructuringElement se)
	{
		if(binaryOperation==false)
		{
			dilateGray(pixels,ip,se);
			erodeGray(pixels,ip,se);
		}
		else
		{
			dilateBinary(pixels,ip,se);
			erodeBinary(pixels,ip,se);
		}
	}
	//--------------BOUNDARY EXTRACTION--------------------------------------------------------
	public void boundaryExtraction(byte[] pixels,ImageProcessor ip ,StructuringElement se)
	{
		byte[] pixelsIniziali=new byte[pixels.length];
		//salvo l'immagine di partenza che mi servirà poi per la differenza.
		for(int i=0;i<pixels.length;i++)
			pixelsIniziali[i]=pixels[i];
		int imgWidth=imp.getWidth();
		int imgHeight=imp.getHeight();

		if(binaryOperation==false)
		{
			erodeGray(pixels,ip,se);
		}
		else
		{
			erodeBinary(pixels,ip,se);
		}

		//faccio la differenza tra l'immagine di partenza e quella elaborata.
		for(int i=0;i<imgHeight;i++)
		{
			for(int j=0;j<imgWidth;j++)
			{
				if( ((pixelsIniziali[i*imgWidth+j]&0xff)-(pixels[i*imgWidth+j]&0xff))<0 ) pixels[i*imgWidth+j]=0;
				else pixels[i*imgWidth+j]=(byte)((pixelsIniziali[i*imgWidth+j]&0xff)-(pixels[i*imgWidth+j]&0xff));
			}
		}
	}
	//---------GRAY SMOOTHING------------------------------------------------------------------
	public void smoothing(byte[] pixels,ImageProcessor ip ,StructuringElement se)
	{
		opening(pixels,ip,se);
		closing(pixels,ip,se);
	}
	//---------HIT OR MISS TRANSFORM-----------------------------------------------------------
	public void hitOrMiss(byte[] pixels,ImageProcessor ip ,StructuringElement se)
	{
		int imgWidth=imp.getWidth();
		int imgHeight=imp.getHeight();
		byte[][]img=new byte[imgHeight][imgWidth];

		Rectangle r=ip.getRoi();
		byte[][]imgOut=new byte[r.height][r.width];

		for(int i=0;i<imgHeight;i++)
		{
			for(int j=0;j<imgWidth;j++)
			{
				img[i][j]=pixels[i*imgWidth+j];
			}
		}

		int raggio=se.getDiameter()/2;
		boolean contained=true;

		for(int i=r.y;i<(r.y+r.height);i++)
		{
			for(int j=r.x;j<(r.x+r.width);j++)
			{
				int s=i;
				int t=j;
				contained=true;
				for(int x=-raggio;x<=raggio;x++)
					for(int y=-raggio;y<=raggio;y++)
					{
						if((s+x)>=r.y && (s+x)<(r.y+r.height) && (t+y)>=r.x && (t+y)<(r.x+r.width))
						{
							if( ( se.get(x+raggio,y+raggio)>0 && (img[s+x][t+y]&0xff)<250 )|| (se.get(x+raggio,y+raggio)==0 && (img[s+x][t+y]&0xff)>0 ) )
								contained=false; //metto <250 e non !=255 perche alcune immagini sebbene a 2 livelli possono avere ad es tutti 254 aql posto di 255...
						}
						//else contained=false;
					}
				if(contained)
					imgOut[s-r.y][t-r.x]=(byte)255;
				else imgOut[s-r.y][t-r.x]=(byte)0;
			}
		}

		for(int i=r.y;i<(r.y+r.height);i++)
		{
			for(int j=r.x;j<(r.x+r.width);j++)
			{
				pixels[i*imgWidth+j]=imgOut[i-r.y][j-r.x];
			}
		}
	}
	//---------GRADIENT------------------------------------------------------------------------
	public void gradient(byte[] pixels,ImageProcessor ip ,StructuringElement se)
	{
		int imgWidth=imp.getWidth();
		int imgHeight=imp.getHeight();
		//salvo l'immagine di partenza che mi servirà poi per la differenza.
		byte[] pixels2=new byte[pixels.length];
		for(int i=0;i<pixels.length;i++)
			pixels2[i]=pixels[i];

		if(binaryOperation==false)
		{
			dilateGray(pixels,ip,se);
			erodeGray(pixels2,ip,se);
		}

		else
		{
			dilateBinary(pixels,ip,se);
			erodeBinary(pixels2,ip,se);
		}

		for(int i=0;i<imgHeight;i++)
		{
			for(int j=0;j<imgWidth;j++)
			{
				if( ((pixels[i*imgWidth+j]&0xff)-(pixels2[i*imgWidth+j]&0xff))<0 ) pixels[i*imgWidth+j]=0;
				else pixels[i*imgWidth+j]=(byte)((pixels[i*imgWidth+j]&0xff)-(pixels2[i*imgWidth+j]&0xff));
			}
		}
	}
	//---------BINARYZE------------------------------------------------------------------------
	public void binaryzeImage(byte[] pixels,ImageProcessor ip)
	{
		Rectangle r=ip.getRoi();
		for(int i=0;i<r.height;i++)
		{
			for(int j=0;j<r.width;j++)
			{
				if( (pixels[i*r.width+j]&0xff)>=127 ) pixels[i*r.width+j]=(byte)255;
				else pixels[i*r.width+j]=0;
			}
		}
	}
	//-----------------------------------------------------------------------------------------
}//class
//-----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------
class StructuringElement
{
	int shape;//determina la forma
	int diameter;//dmensione della maschera
	int[][]mask;
	public StructuringElement(int shape,int diameter)
	{
		this.shape=shape;
		this.diameter=diameter;
		mask=new int[diameter][diameter];
		if(shape==0) createCircularMask();
		else if(shape==1) createSquareMask();
		else if(shape==2) createCrossMask();
		else if(shape==3) createVLineMask();
		else if(shape==4) createHLineMask();
	}
	void createCircularMask()
	{
		int radius=diameter/2;
		int r2=radius*radius+1;//aggiungo 1 per ottenere una forma che sia più simile ad un cerchio e non ad un rombo.
		int x0=radius;//origine del cerchio
		int y0=radius;//origine del cerchio
		for(int i=0;i<diameter;i++)
			for(int j=0;j<diameter;j++)
			{
				if( ((i-x0)*(i-x0)+(j-y0)*(j-y0))<=r2 )
					this.mask[i][j]=Morphology_.SET;
				else
					this.mask[i][j]=0;
			}
	}

	void createSquareMask()
	{
		for(int i=0;i<diameter;i++)
			for(int j=0;j<diameter;j++)
			{
				this.mask[i][j]=Morphology_.SET;
			}
	}

	void createCrossMask()
	{
		int radius=diameter/2;
		int x0=radius;
		int y0=radius;
		for(int i=0;i<diameter;i++)
			mask[i][y0]=Morphology_.SET;
		for(int j=0;j<diameter;j++)
			mask[x0][j]=Morphology_.SET;
	}

	public void createVLineMask()
	{
		int radius=diameter/2;
		int x0=radius;
		int y0=radius;
		for(int i=0;i<diameter;i++)
			mask[i][y0]=Morphology_.SET;
	}

	public void createHLineMask()
	{
		int radius=diameter/2;
		int x0=radius;
		int y0=radius;
		for(int j=0;j<diameter;j++)
			mask[x0][j]=Morphology_.SET;
	}

	public int[][] getMask()
	{
		return this.mask;
	}


	public void setShapeAndSize(int shape,int size)
	{
		this.mask=new int[size][size];
		this.diameter=size;
		this.shape=shape;
		if(shape==0) createCircularMask();
		else if(shape==1) createSquareMask();
		else if(shape==2) createCrossMask();
		else if(shape==3) createVLineMask();
		else if(shape==4) createHLineMask();
	}

	public void setMask(int[][] m,int grayValue)
	{
		this.mask=new int[m.length][m.length];
		this.diameter=m.length;
		for(int i=0;i<mask.length;i++)
			for(int j=0;j<mask.length;j++)
				if(m[i][j]==Morphology_.SET) mask[i][j]=grayValue;//elem. strutturale standard o disegnato dall'utente
				else mask[i][j]=m[i][j]; //elemento strutturale caricato da file.

		padMask();
	}

	public void setMask(byte[] m,int rows,int cols)
	{
		this.mask=new int[rows][cols];
		this.diameter=m.length;
		for(int i=0;i<rows;i++)
		{
			for(int j=0;j<cols;j++)
			{
				mask[i][j]=m[i*cols+j];
			}
		}
		padMask();
	}


	public void padMask()
	{
		int rows=mask.length;
		int cols=mask[0].length;
		int max=rows;
		if (max<cols)
			max=cols;

		this.diameter=max;

		if(max%2==0) max++;

		int app[][]=new int[max][max];

		for(int i=0;i<max;i++)
		{
			for(int j=0;j<max;j++)
			{
				app[i][j]=0;
			}
		}

		for(int i=0;i<rows;i++)
		{
			for(int j=0;j<cols;j++)
			{
				app[i][j]=mask[i][j];
			}
		}
		mask=new int[max][max];
		for(int i=0;i<max;i++)
		{
			for(int j=0;j<max;j++)
			{
				mask[i][j]=app[i][j];
			}
		}


		/*
 String tmp="";
    for(int i=0;i<max;i++)
    {
     for(int j=0;j<max;j++)
     {
      tmp=tmp+" "+mask[i][j];
     }
     tmp=tmp+"\n";
    }
IJ.showMessage(tmp);
		 */
	}


	public int get(int x,int y)
	{
		return this.mask[x][y];
	}

	public int getDiameter()
	{return this.diameter;}

	public int getWidth()
	{
		return this.mask[0].length;
	}

	public int getHeight()
	{
		return this.mask.length;
	}
	//-----------------------------------------------------------------------------------------
}
//-----------------------------------------------------------------------------------------
//-----------------------------------------------------------------------------------------
class DrawPanel extends JPanel implements MouseListener
{
	int gridSize;
	private int size,forma;
	private Color colore;
	private int width,height;
	private ArrayList listaVertici;
	private int baseRect;//base dei rettangolini interni
	private int base;//base del rettangolo contenitore
	private int[][] matrix;
	boolean enable;// è true quando è l'utente dopo aver cliccato su "drawStructuraElement" disegna lìelemento strutturale.
	private Color color=Color.white;
	private int grayValue=255;
	public DrawPanel(int size,int[][]shape)
	{
		super();
		gridSize=size;
		enable=false;
		matrix=new int[gridSize][gridSize];
		setGrid(shape,size);
		addMouseListener(this);
	}

	public void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		Graphics2D g2=(Graphics2D)g;
		base=getSize().width/2;//meta' della base del pannello
		Point P=new Point((getSize().width-base)/2,(getSize().height-base)/2);
		baseRect=base/this.gridSize;
		listaVertici=new ArrayList();


		g2.setPaint(this.color);

		for(int i=0;i<gridSize;i++)
			for(int j=0;j<gridSize;j++)
			{
				Point corner=new Point(P.x+j*baseRect,P.y+i*baseRect);
				listaVertici.add(corner);
				Rectangle2D.Double r=new Rectangle2D.Double(corner.x,corner.y,baseRect,baseRect);
				if(matrix[i][j]==0)//disegno il background
				{
					g2.setColor(Color.black);
					g2.fill(r);//g2.draw(r);
				}
				else
				{
					if(matrix[i][j]==-1) g2.setPaint(Color.red);
					else//disegno il foreground
					{
						if(matrix[i][j]==Morphology_.SET)
							g2.setPaint(this.color);
						else
						{
							int val=matrix[i][j]&0xff;
							this.color=new Color(val,val,val);
							g2.setPaint(this.color);
						}
						g2.fill(r);
					}
				}
			}

		int xSize=gridSize*baseRect;
		int ySize=gridSize*baseRect;


		g2.setPaint(Color.gray);
		for(int i=0;i<gridSize;i++)
		{
			Line2D.Double line=new Line2D.Double(P.x+i*baseRect,P.y,P.x+i*baseRect,P.y+ySize);
			g2.draw(line);
		}
		for(int i=0;i<gridSize;i++)
		{
			Line2D.Double line=new Line2D.Double(P.x,P.y+i*baseRect,P.x+xSize,P.y+i*baseRect);
			g2.draw(line);
		}
	}

	public void mouseClicked(MouseEvent m)
	{
		int button=0;
		int modifiers=m.getModifiers();
		if((modifiers & InputEvent.BUTTON1_MASK)!=0) button=1;
		else if((modifiers & InputEvent.BUTTON2_MASK)!=0) button=2;
		else if((modifiers & InputEvent.BUTTON3_MASK)!=0) button=3;

		if(this.enable)
		{
			Point p=m.getPoint();
			int index=this.find(p);
			if (index!=-1)
			{
				int col=index%gridSize;
				int row=index/gridSize;

				if(button==1)
				{
					if(matrix[row][col]==0) matrix[row][col]=Morphology_.SET;
					else matrix[row][col]=0;
				}
				/*else
   {
    if(matrix[row][col]==0) matrix[row][col]=-1;
     else matrix[row][col]=0;
   }
				 */
				//IJ.showMessage("index="+index+" row="+row+" col="+col+" val="+matrix[row][col]+" ");
				repaint();
			}
		}
	}
	public void mouseMoved(MouseEvent e){}
	public void mousePressed(MouseEvent e) {}
	public void mouseReleased(MouseEvent e) {}
	public void mouseExited(MouseEvent e) {}
	public void mouseEntered(MouseEvent e) {}

	public int find(Point p)
	{
		for(int i=0;i<listaVertici.size();i++)
		{
			RectangularShape shape;
			Point leftC=(Point)listaVertici.get(i);
			shape=new Rectangle2D.Double(leftC.x,leftC.y,baseRect,baseRect);
			if (shape.contains(p))
			{
				return i;
			}
		}
		return -1;
	}

	public void setGrid(int[][] mask,int size)
	{
		this.gridSize=size;
		matrix=new int[gridSize][gridSize];
		for(int i=0;i<gridSize;i++)
			for(int j=0;j<gridSize;j++)
				//if (mask[i][j]!=0)matrix[i][j]=1;
				//else matrix[i][j]=0;
				matrix[i][j]=mask[i][j];
		this.color=Color.white;
		repaint();

		/*
 String tmp="";
 for(int i=0;i<gridSize;i++)
 {
  for(int j=0;j<gridSize;j++)
  {
   tmp=tmp+" "+matrix[i][j];
  }
  tmp=tmp+"\n";
 }
 IJ.showMessage(tmp);
		 */
	}

	public void setEnable(boolean b)
	{
		this.enable=b;
	}

	public void reset(int foregroundColor)
	{
		for(int i=0;i<matrix.length;i++)
			for(int j=0;j<matrix.length;j++)
				matrix[i][j]=0;
		this.color=Color.black;
		repaint();
		this.color=new Color(foregroundColor,foregroundColor,foregroundColor);
		this.grayValue=foregroundColor;
	}

	public int[][] getMask()
	{return this.matrix;}

	public void setColor(int val)
	{
		this.color=new Color(val,val,val);
		this.grayValue=val;
		repaint();
	}
	public int getGrayValue()
	{return grayValue;}

}


//----------------------------------------------------------------------------
