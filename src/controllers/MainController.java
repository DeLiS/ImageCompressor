package controllers;

import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import forms.*;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.*;
import algos.*;
public class MainController {		
	private File[] _selectedFiles;
	private File[] _resultFiles;
	private long[] _workTime;
	private int[] _imageTypes;
	private float[] _coefficient;
	private String _destinationFolder;
	private JList _listSelectedFiles;
	private JFileChooser _fileChooser;
	public DefaultListModel _dlmSelectedFiles;
	public JLabel _lbDestFolder;
	//public RleImageCompressor rle;
	public IImageCompressor compressor;
	public JTextField _tfSourceSize;
	public JTextField _tfResultSize;
	public JTextField _tfComprCoef;
	public JTextField _tfComprTime;
	public JSpinner _spinner;
	public JComboBox _cbMethod;
	public boolean proceeded = false;
	public MainController(MainForm form)
	{
		//rle = new RleImageCompressor();
		_destinationFolder = "C:\\";
		Connect(form);
		Initialize();
	}
	private void Connect(MainForm form)
	{
		_listSelectedFiles = form._JlistSelectedFiles;
		_dlmSelectedFiles = form._dlmSelectedFiles;
		_lbDestFolder = form._lbDestFolder;
		 _tfSourceSize = form._tfSourceSize;
		 _tfResultSize = form._tfResultSize;
		 _tfComprCoef = form._tfComprCoef;
		 _tfComprTime = form._tfComprTime;
		 _spinner = form._spinner;
		 _cbMethod = form._cbCompressionType;
		 form._spinner.addChangeListener(new ChangeListener(){			 

			@Override
			public void stateChanged(ChangeEvent arg0) {
				SpinnerValueChanged(arg0);
				
			}
		 });
		form._btSelectFiles.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				BtSelectFilesClickEventHandler(arg0);	
			}
		});
		form._btChooseDestFolder.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
			{
				BtChooseDestFolderClickEventHandler(arg0);
			}
		});
		
		form._btCompress.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
			{
				BtCompressClick(arg0);
			}
		});
		form._btDecompress.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent arg0)
			{
				BtDecompressClick(arg0);
			}
		});
		form._JlistSelectedFiles.addListSelectionListener(new ListSelectionListener()
			{
				public void valueChanged(ListSelectionEvent listSelectionEvent)
				{
					UpdateStatisticsFields();
				}
			}
		);
		form._frame.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e)
			{
				Statistics.GetInstance().WriteData();
			}
		});

	}
	private void Initialize()
	{
		
	}
	private void BtSelectFilesClickEventHandler(ActionEvent arg0)
	{
		_fileChooser = new JFileChooser();
		_fileChooser.setMultiSelectionEnabled(true);
		_fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("bmp and bmpc","bmp","bmpc"));
		int returnValue = _fileChooser.showDialog(null, "Choose Files");
		if(returnValue == JFileChooser.APPROVE_OPTION)
		{
			proceeded = false;
			_selectedFiles = _fileChooser.getSelectedFiles();
			_dlmSelectedFiles.clear();
			for(int i=0;i<_selectedFiles.length; ++ i)
			{
				_dlmSelectedFiles.addElement(_selectedFiles[i]);
			}
		}
		UpdateStatisticsFields();
		_imageTypes = new int[_selectedFiles.length];
		
	}
	
	private void BtChooseDestFolderClickEventHandler(ActionEvent arg0)
	{
		_fileChooser = new JFileChooser();
		_fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnValue = _fileChooser.showDialog(null, "Choose destination directory");
		if(returnValue == JFileChooser.APPROVE_OPTION)
		{
			_destinationFolder = _fileChooser.getSelectedFile().getPath();
			_destinationFolder+="\\";
			_lbDestFolder.setText(_destinationFolder);
			
		}
		
	}
	private void BtCompressClick(ActionEvent arg0)
	{
		proceeded = true;
		_resultFiles = new File[_selectedFiles.length];
		_workTime = new long[_selectedFiles.length];
		_coefficient = new float[_selectedFiles.length];
		switch(_cbMethod.getSelectedIndex())
		{
		case 0:
			compressor = new RleImageCompressor();
			break;
		case 1:
			compressor = new LzwImageCompressor();
			break;
		case 2:
			compressor = new HuffmanImageCompressor();
			break;
		case 3:
			compressor = new JpegImageCompressor();
			break;
		}
		 
		for(int i=0;i<_selectedFiles.length; ++i)
		{
			try
			{
				BufferedImage bufferedImage = ImageIO.read(_selectedFiles[i]);				
				
				long startTime = System.currentTimeMillis();				
				byte[] result = compressor.Compress(bufferedImage);				
				long endTime = System.currentTimeMillis();
				
				_workTime[i] = endTime - startTime;
				
				String filename = _destinationFolder + _selectedFiles[i].getName() + "c";
				FileOutputStream fos = new FileOutputStream(filename);
				fos.write(result);
				fos.close();
				
				_resultFiles[i] = new File(filename);				
				_coefficient[i] = 1.0f*_selectedFiles[i].length()/_resultFiles[i].length();
				
				ImageStatistics imageStatistics = new ImageStatistics();
				imageStatistics.SetName(_selectedFiles[i].getName());
				imageStatistics.SetOriginalSize(_selectedFiles[i].length());
				imageStatistics.SetCompressedSize(_resultFiles[i].length());
				imageStatistics.SetImageType(_imageTypes[i]);
				imageStatistics.SetHashCode(bufferedImage.hashCode());
				imageStatistics.SetWorkTime(endTime - startTime);
				imageStatistics.SetIsCompressionStatistics(true);
				Statistics.GetInstance().AddItem(imageStatistics);
			}
			catch(IOException e)
			{
				
			}
			
		}
		UpdateStatisticsFields();
	}
	
	private void BtDecompressClick(ActionEvent arg0)
	{
		proceeded  = true;
		_resultFiles = new File[_selectedFiles.length];
		_workTime = new long[_selectedFiles.length];
		_coefficient = new float[_selectedFiles.length];
		switch(_cbMethod.getSelectedIndex())
		{
		case 0:
			compressor = new RleImageCompressor();
			break;
		case 1:
			compressor = new LzwImageCompressor();
			break;
		case 2:
			compressor = new HuffmanImageCompressor();
			break;
		case 3:
			compressor = new JpegImageCompressor();
			break;
		}
		for(int i=0;i<_selectedFiles.length; ++i)
		{
			try
			{
				FileInputStream fis = new FileInputStream(_selectedFiles[i].getAbsoluteFile());
				byte[] bytes = new byte[(int) _selectedFiles[i].length()+1];
				fis.read(bytes);
				
				long startTime = System.currentTimeMillis();
				
				BufferedImage bufferedImage = compressor.Decompress(bytes);
				long endTime = System.currentTimeMillis();
				_workTime[i] = endTime - startTime;
				
				String filename =  _destinationFolder + "_"+ _selectedFiles[i].getName().substring(0,_selectedFiles[i].getName().length()-1);
				File outputFile = new File(filename);
				ImageIO.write(bufferedImage, "bmp", outputFile);
				
				_resultFiles[i] = outputFile;	
				_coefficient[i] = 1.0f*_resultFiles[i].length()/_selectedFiles[i].length();
				
				ImageStatistics imageStatistics = new ImageStatistics();
				imageStatistics.SetName(_resultFiles[i].getName());
				imageStatistics.SetOriginalSize(_resultFiles[i].length());
				imageStatistics.SetCompressedSize(_selectedFiles[i].length());
				imageStatistics.SetImageType(_imageTypes[i]);
				imageStatistics.SetHashCode(bufferedImage.hashCode());
				imageStatistics.SetWorkTime(endTime - startTime);
				imageStatistics.SetIsCompressionStatistics(false);
				Statistics.GetInstance().AddItem(imageStatistics);
				
			}
			catch(IOException e)
			{
				
			}
			
		}
		UpdateStatisticsFields();
	}
	
	private void UpdateStatisticsFields()
	{
		if(proceeded)
		{
			 int index = _listSelectedFiles.getSelectedIndex();
			 if(index >=0){
			 _tfSourceSize.setText(Long.toString(_selectedFiles[index].length()));
			 _tfResultSize.setText(Long.toString(_resultFiles[index].length()));
			 _tfComprCoef.setText(Float.toString(_coefficient[index]));
			 _tfComprTime.setText(Long.toString(_workTime[index]));
			 }
		}
		else
		{
			_tfSourceSize.setText("");
			 _tfResultSize.setText("");
			 _tfComprCoef.setText("");
			 _tfComprTime.setText("");
		}
	}
	
	private void SpinnerValueChanged(ChangeEvent arg0)
	{
		 int index = _listSelectedFiles.getSelectedIndex();
		 if(index >=0){
			 _imageTypes[index] = new Integer(_spinner.getValue().toString());
		 }
	}

}
