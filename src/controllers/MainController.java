package controllers;

import algos.*;
import forms.MainForm;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainController {
    public DefaultListModel dlmSelectedFiles;
    public JLabel lbDestFolder;
    public IImageCompressor compressor;
    public JTextField tfSourceSize;
    public JTextField tfResultSize;
    public JTextField tfComprCoef;
    public JTextField tfComprTime;
    public JSpinner spinner;
    public JComboBox cbMethod;
    public boolean proceeded = false;
    private File[] selectedFiles;
    private File[] resultFiles;
    private long[] workTime;
    private int[] imageTypes;
    private float[] coefficient;
    private String destinationFolder;
    private JList listSelectedFiles;
    private JFileChooser fileChooser;

    public MainController(MainForm form) {
        destinationFolder = "C:\\";
        Connect(form);
        Initialize();
    }

    private void Connect(MainForm form) {
        listSelectedFiles = form._JlistSelectedFiles;
        dlmSelectedFiles = form._dlmSelectedFiles;
        lbDestFolder = form._lbDestFolder;
        tfSourceSize = form._tfSourceSize;
        tfResultSize = form._tfResultSize;
        tfComprCoef = form._tfComprCoef;
        tfComprTime = form._tfComprTime;
        spinner = form._spinner;
        cbMethod = form._cbCompressionType;
        form._spinner.addChangeListener(new ChangeListener() {

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
        form._btChooseDestFolder.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                BtChooseDestFolderClickEventHandler(arg0);
            }
        });

        form._btCompress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                BtCompressClick(arg0);
            }
        });
        form._btDecompress.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                BtDecompressClick(arg0);
            }
        });
        form._JlistSelectedFiles.addListSelectionListener(new ListSelectionListener() {
                                                              public void valueChanged(ListSelectionEvent listSelectionEvent) {
                                                                  UpdateStatisticsFields();
                                                              }
                                                          }
        );
        form._frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                Statistics.GetInstance().WriteData();
            }
        });

    }

    private void BtSelectFilesClickEventHandler(ActionEvent arg0) {
        fileChooser = new JFileChooser();
        fileChooser.setMultiSelectionEnabled(true);
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("bmp and bmpc", "bmp", "bmpc"));
        int returnValue = fileChooser.showDialog(null, "Choose Files");
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            proceeded = false;
            selectedFiles = fileChooser.getSelectedFiles();
            dlmSelectedFiles.clear();
            for (int i = 0; i < selectedFiles.length; ++i) {
                dlmSelectedFiles.addElement(selectedFiles[i]);
            }
        }
        UpdateStatisticsFields();
        imageTypes = new int[selectedFiles.length];

    }

    private void UpdateStatisticsFields() {
        if (proceeded) {
            int index = listSelectedFiles.getSelectedIndex();
            if (index >= 0) {
                tfSourceSize.setText(Long.toString(selectedFiles[index].length()));
                tfResultSize.setText(Long.toString(resultFiles[index].length()));
                tfComprCoef.setText(Float.toString(coefficient[index]));
                tfComprTime.setText(Long.toString(workTime[index]));
            }
        } else {
            tfSourceSize.setText("");
            tfResultSize.setText("");
            tfComprCoef.setText("");
            tfComprTime.setText("");
        }
    }

    private void BtChooseDestFolderClickEventHandler(ActionEvent arg0) {
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnValue = fileChooser.showDialog(null, "Choose destination directory");
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            destinationFolder = fileChooser.getSelectedFile().getPath();
            destinationFolder += "\\";
            lbDestFolder.setText(destinationFolder);

        }

    }

    private void BtCompressClick(ActionEvent arg0) {
        proceeded = true;
        resultFiles = new File[selectedFiles.length];
        workTime = new long[selectedFiles.length];
        coefficient = new float[selectedFiles.length];
        switch (cbMethod.getSelectedIndex()) {
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

        for (int i = 0; i < selectedFiles.length; ++i) {
            try {
                BufferedImage bufferedImage = ImageIO.read(selectedFiles[i]);

                long startTime = System.currentTimeMillis();
                byte[] result = compressor.Compress(bufferedImage);
                long endTime = System.currentTimeMillis();

                workTime[i] = endTime - startTime;

                String filename = destinationFolder + selectedFiles[i].getName() + "c";
                FileOutputStream fos = new FileOutputStream(filename);
                fos.write(result);
                fos.close();

                resultFiles[i] = new File(filename);
                coefficient[i] = 1.0f * selectedFiles[i].length() / resultFiles[i].length();

                ImageStatistics imageStatistics = new ImageStatistics();
                imageStatistics.SetName(selectedFiles[i].getName());
                imageStatistics.SetOriginalSize(selectedFiles[i].length());
                imageStatistics.SetCompressedSize(resultFiles[i].length());
                imageStatistics.SetImageType(imageTypes[i]);
                imageStatistics.SetHashCode(bufferedImage.hashCode());
                imageStatistics.SetWorkTime(endTime - startTime);
                imageStatistics.SetIsCompressionStatistics(true);
                Statistics.GetInstance().AddItem(imageStatistics);
            } catch (IOException e) {

            }

        }
        UpdateStatisticsFields();
    }

    private void BtDecompressClick(ActionEvent arg0) {
        proceeded = true;
        resultFiles = new File[selectedFiles.length];
        workTime = new long[selectedFiles.length];
        coefficient = new float[selectedFiles.length];
        switch (cbMethod.getSelectedIndex()) {
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
        for (int i = 0; i < selectedFiles.length; ++i) {
            try {
                FileInputStream fis = new FileInputStream(selectedFiles[i].getAbsoluteFile());
                byte[] bytes = new byte[(int) selectedFiles[i].length() + 1];
                fis.read(bytes);

                long startTime = System.currentTimeMillis();

                BufferedImage bufferedImage = compressor.Decompress(bytes);
                long endTime = System.currentTimeMillis();
                workTime[i] = endTime - startTime;

                String filename = destinationFolder + "_" + selectedFiles[i].getName().substring(0, selectedFiles[i].getName().length() - 1);
                File outputFile = new File(filename);
                ImageIO.write(bufferedImage, "bmp", outputFile);

                resultFiles[i] = outputFile;
                coefficient[i] = 1.0f * resultFiles[i].length() / selectedFiles[i].length();

                ImageStatistics imageStatistics = new ImageStatistics();
                imageStatistics.SetName(resultFiles[i].getName());
                imageStatistics.SetOriginalSize(resultFiles[i].length());
                imageStatistics.SetCompressedSize(selectedFiles[i].length());
                imageStatistics.SetImageType(imageTypes[i]);
                imageStatistics.SetHashCode(bufferedImage.hashCode());
                imageStatistics.SetWorkTime(endTime - startTime);
                imageStatistics.SetIsCompressionStatistics(false);
                Statistics.GetInstance().AddItem(imageStatistics);

            } catch (IOException e) {

            }

        }
        UpdateStatisticsFields();
    }

    private void SpinnerValueChanged(ChangeEvent arg0) {
        int index = listSelectedFiles.getSelectedIndex();
        if (index >= 0) {
            imageTypes[index] = new Integer(spinner.getValue().toString());
        }
    }

    private void Initialize() {

    }

}
