package forms;

import java.awt.EventQueue;
import java.awt.Window;

import javax.swing.JFrame;
import javax.swing.JButton;
import java.awt.BorderLayout;

import javax.swing.DefaultListModel;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;
import javax.swing.LayoutStyle.ComponentPlacement;
import java.awt.List;
import javax.swing.JList;
import controllers.MainController;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JScrollBar;
import java.awt.ScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
public class MainForm {

	public JFrame _frame;
	public JList _JlistSelectedFiles;
	public DefaultListModel _dlmSelectedFiles;
	public JButton _btSelectFiles;
	public JButton _btChooseDestFolder;
	public JLabel _lbDestFolder;
	public JButton _btCompress;
	public JButton _btDecompress;
	public JTextField _tfSourceSize;
	public JTextField _tfResultSize;
	public JTextField _tfComprCoef;
	public JTextField _tfComprTime;
	private JLabel lblBytes;
	private JLabel lblBytes_1;
	private JLabel lblMs;
	public JSpinner _spinner;
	private JScrollPane scrollPane;
	public JComboBox _cbCompressionType;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainForm window = new MainForm();
					window._frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainForm() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		_frame = new JFrame();
		_frame.setResizable(false);
		_frame.setBounds(100, 100, 456, 523);
		_frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		_btSelectFiles = new JButton("\u0412\u044B\u0431\u0440\u0430\u0442\u044C \u0444\u0430\u0439\u043B\u044B");		
		_dlmSelectedFiles = new DefaultListModel();
		_btChooseDestFolder = new JButton("\u0412\u044B\u0431\u0440\u0430\u0442\u044C \u043F\u0430\u043F\u043A\u0443 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D\u0438\u044F");
		
		_lbDestFolder = new JLabel("\u041F\u0430\u043F\u043A\u0430 \u043D\u0430\u0437\u043D\u0430\u0447\u0435\u043D\u0438\u044F");
		
		_btCompress = new JButton("\u0421\u0436\u0430\u0442\u044C");
		
		_btDecompress = new JButton("\u0420\u0430\u0437\u0436\u0430\u0442\u044C");
		
		JLabel label = new JLabel("\u0418\u0441\u0445\u043E\u0434\u043D\u0430\u044F \u0434\u043B\u0438\u043D\u0430");
		
		JLabel label_1 = new JLabel("\u0414\u043B\u0438\u043D\u0430 \u0440\u0435\u0437\u0443\u043B\u044C\u0442\u0438\u0440\u0443\u044E\u0449\u0435\u0433\u043E \u0444\u0430\u0439\u043B\u0430");
		
		JLabel label_2 = new JLabel("\u041A\u043E\u044D\u0444\u0444\u0438\u0446\u0438\u0435\u043D\u0442 \u0441\u0436\u0430\u0442\u0438\u044F");
		
		JLabel label_3 = new JLabel("\u0412\u0440\u0435\u043C\u044F \u0441\u0436\u0430\u0442\u0438\u044F/\u0432\u043E\u0441\u0441\u0442\u0430\u043D\u043E\u0432\u043B\u0435\u043D\u0438\u044F");
		
		_tfSourceSize = new JTextField();
		_tfSourceSize.setColumns(10);
		
		_tfResultSize = new JTextField();
		_tfResultSize.setColumns(10);
		
		_tfComprCoef = new JTextField();
		_tfComprCoef.setColumns(10);
		
		_tfComprTime = new JTextField();
		_tfComprTime.setColumns(10);
		
		lblBytes = new JLabel("bytes");
		
		lblBytes_1 = new JLabel("bytes");
		
		lblMs = new JLabel("ms");
		
		_spinner = new JSpinner();
		_spinner.setModel(new SpinnerNumberModel(1, 1, 4, 1));
		
		scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		
		JLabel label_4 = new JLabel("\u0422\u0438\u043F");
		
		_cbCompressionType = new JComboBox();
		_cbCompressionType.setModel(new DefaultComboBoxModel(new String[] {"RLE", "LZW", "Huffman", "JPEG"}));
		_cbCompressionType.setSelectedIndex(0);
		GroupLayout groupLayout = new GroupLayout(_frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(groupLayout.createSequentialGroup()
									.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
										.addGroup(groupLayout.createSequentialGroup()
											.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(label_2)
												.addComponent(label)
												.addComponent(label_1))
											.addGap(41)
											.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
												.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
													.addComponent(_tfComprCoef, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
													.addComponent(_tfResultSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
												.addComponent(_tfSourceSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(label_3)
											.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
											.addComponent(_tfComprTime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
									.addGap(18)
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addComponent(lblBytes_1)
										.addComponent(lblBytes)
										.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
											.addComponent(label_4)
											.addComponent(lblMs))))
								.addGroup(groupLayout.createSequentialGroup()
									.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
										.addGroup(groupLayout.createSequentialGroup()
											.addComponent(_btSelectFiles)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(_btCompress)
											.addPreferredGap(ComponentPlacement.RELATED)
											.addComponent(_btDecompress))
										.addGroup(groupLayout.createSequentialGroup()
											.addGap(10)
											.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
												.addComponent(_btChooseDestFolder)
												.addComponent(_lbDestFolder))))
									.addPreferredGap(ComponentPlacement.RELATED, 71, Short.MAX_VALUE)))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(_spinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
							.addGap(137))
						.addGroup(groupLayout.createSequentialGroup()
							.addGap(10)
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(_cbCompressionType, GroupLayout.PREFERRED_SIZE, 139, GroupLayout.PREFERRED_SIZE)
								.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 414, GroupLayout.PREFERRED_SIZE))
							.addGap(80))))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(_tfSourceSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label)
						.addComponent(lblBytes))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(_tfResultSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label_1)
						.addComponent(lblBytes_1))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(label_2)
						.addComponent(_tfComprCoef, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addGap(11)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(_tfComprTime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label_3)
						.addComponent(lblMs))
					.addGap(40)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(_btSelectFiles)
						.addComponent(_btCompress)
						.addComponent(_btDecompress)
						.addComponent(_spinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(label_4))
					.addGap(3)
					.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 174, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.UNRELATED)
					.addComponent(_cbCompressionType, GroupLayout.PREFERRED_SIZE, 21, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED, 24, Short.MAX_VALUE)
					.addComponent(_btChooseDestFolder)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(_lbDestFolder)
					.addGap(42))
		);
		_JlistSelectedFiles = new JList(_dlmSelectedFiles);
		scrollPane.setViewportView(_JlistSelectedFiles);
		_JlistSelectedFiles.setValueIsAdjusting(true);
		_frame.getContentPane().setLayout(groupLayout);
		MainController mc = new MainController(this);
	}
}
