package ssq.stock.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TextField;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import net.sf.json.JSONObject;
import ssq.stock.DateData;
import ssq.stock.Stock;
import ssq.stock.interpreter.Interpreter;
import ssq.utils.DirUtils;
import ssq.utils.FileUtils;

public class GUI extends FrameWithStatus
{
	
	private static final long	serialVersionUID	= 1L;
													
	private static final String	FORMULA				= "fomular";
	private static final String	MIN_GRADE			= "minGrade";
	private static final String	LIST_SIZE			= "listSize";
	private static final String	BACK_BY_DATE		= "backByDate";
	private static final String	BACK_TO_DATE		= "backToDate";
	private static final String	BACK_DAYS			= "backDays";
	private static final String	THREADS				= "downLoadThreads";
	private static final String	TDX_LOCATION		= "tdxLocation";
													
	private static final String	READY				= "查看参数是否正确, 并按开始";
													
	JSONObject					object;
								
	public static GUI			instance			= null;
	JComponent[]				labels;
	public TextField[]			textFields;
	JButton[]					buttons;
	public static final Font	SONGFONT_FONT		= new Font("宋体", Font.PLAIN, 16);
													
	public static void main(String[] args)
	{
		new GUI();
	}
	
	public static void statusText(String s)
	{
		if (GUI.instance != null)
		{
			GUI.instance.setStatusText(s);
		}
	}
	
	public GUI()
	{
		super(null);
		instance = this;
		fromJson();
		setVisible(true);
	}
	
	private void fillInDefaultVals()
	{
		JSONObject tmp = JSONObject.fromObject("{'fomular':'max(250->125).opening..norest * 2 < average(5->1).highest @2 && (3<4 || sum(250->1).quantity > 10000000000)','listSize':'300','minGrade':'50','backByDate':false,'backDays':'0','downLoadThreads':'50','backToDate':'20150522','tdxLocation':''}");
		
		for (Object key : tmp.keySet())
		{
			if (!object.containsKey(key))
			{
				object.put(key, tmp.get(key));
			}
		}
	}
	
	@Override
	protected void initData()
	{
		try
		{
			object = JSONObject.fromObject(FileUtils.openAssetsString("pref.txt"));
			fillInDefaultVals();
			
			fromJson();
		}
		catch (Exception e)
		{
			e.printStackTrace();
			
			restoreData();
		}
	}
	
	private String getWithDefault(String key, String defaultVal)
	{
		try
		{
			return object.getString(key);
		}
		catch (Exception e)
		{
			return defaultVal;
		}
	}
	
	private Boolean getWithDefault(String key, Boolean defaultVal)
	{
		try
		{
			return object.getBoolean(key);
		}
		catch (Exception e)
		{
			return defaultVal;
		}
	}
	
	private void fromJson()
	{
		textFields[0].setText(object.getString(FORMULA));
		textFields[1].setText(object.getString(LIST_SIZE));
		textFields[2].setText(object.getString(MIN_GRADE));
		Boolean byDate = object.getBoolean(BACK_BY_DATE);
		if (byDate)
		{
			((JComboBox<String>) labels[3]).setSelectedIndex(1);
		}
		textFields[3].setText(byDate ? object.getString(BACK_TO_DATE) : object.getString(BACK_DAYS));
		textFields[4].setText(object.getString(THREADS));
		textFields[5].setText(object.getString(TDX_LOCATION));
	}
	
	private void toJson()
	{
		object.element(FORMULA, textFields[0].getText());
		object.element(LIST_SIZE, textFields[1].getText());
		object.element(MIN_GRADE, textFields[2].getText());
		
		boolean backByDate = ((JComboBox<String>) labels[3]).getSelectedIndex() == 1;
		object.element(BACK_BY_DATE, backByDate);
		if (backByDate)
		{
			object.element(BACK_TO_DATE, textFields[3].getText());
		}
		else
		{
			object.element(BACK_DAYS, textFields[3].getText());
		}
		
		object.element(THREADS, textFields[4].getText());
		object.element(TDX_LOCATION, textFields[5].getText());
	}
	
	@Override
	protected void initListeners()
	{
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		for (int i = 0; i < buttons.length; i++)
		{
			buttons[i].addMouseListener(listeners[i]);
		}
		
		final JComboBox<String> jComboBox = (JComboBox<String>) labels[3];
		
		jComboBox.addItemListener(new ItemListener()
		{
			@Override
			public void itemStateChanged(ItemEvent e)
			{
				if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED)
				{
					textFields[3].setText((jComboBox.getSelectedIndex() == 0) ? getWithDefault(BACK_DAYS, "0") : getWithDefault(BACK_TO_DATE, DateData.format.format(new Date())));
				}
			}
		});
	}
	
	public static GUI getInstance()
	{
		return instance;
	}
	
	static class MyMouseAdapter extends MouseAdapter
	{
		String textString;
		
		@Override
		public void mouseEntered(MouseEvent e)
		{
			textString = instance.statusLabel.getText();
		}
		
		@Override
		public void mouseExited(MouseEvent e)
		{
			statusText(textString);
		}
	}
	
	static private MouseListener[] listeners = new MouseListener[] { new MyMouseAdapter()
	{
		@Override
		public void mouseEntered(MouseEvent e)
		{
			super.mouseEntered(e);
			statusText("保存设置到文件");
		}
		
		@Override
		public void mouseClicked(MouseEvent e)
		{
			try
			{
				getInstance().saveData();
			}
			catch (IOException e1)
			{
				e1.printStackTrace();
				statusText(e1.getLocalizedMessage());
			}
		}
	}, new MyMouseAdapter()
	{
		@Override
		public void mouseEntered(MouseEvent e)
		{
			super.mouseEntered(e);
			statusText("恢复成默认设置");
		}
		
		@Override
		public void mouseClicked(MouseEvent e)
		{
			GUI.getInstance().restoreData();
		}
	}, new MyMouseAdapter()
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			QueryHistoryFrame.showQueryHistory();
		}
		
		@Override
		public void mouseEntered(MouseEvent e)
		{
			super.mouseEntered(e);
			statusText("显示选股历史");
		}
	}, new MyMouseAdapter()
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			new DebugFrame();
		}
		
		@Override
		public void mouseEntered(MouseEvent e)
		{
			super.mouseEntered(e);
			statusText("打开一个调试窗口, 调试各原子公式");
		}
	}, new MyMouseAdapter()
	{
		@Override
		public void mouseClicked(MouseEvent e)
		{
			try
			{
				if (JOptionPane.showConfirmDialog(null, "更新数据可能会花费大量的时间, 请保持网络畅通并关注状态条上的进度提示. ", "更新股票数据? ", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION)
				{
					Stock.updateStocks();
				}
			}
			catch (Exception e1)
			{
				e1.printStackTrace();
				statusText("执行出现异常" + e1.getLocalizedMessage());
			}
		}
		
		@Override
		public void mouseEntered(MouseEvent e)
		{
			super.mouseEntered(e);
			statusText("更新股票数据, 可能需要较长时间");
		}
	}, new MyMouseAdapter()
	{
		@Override
		public void mouseEntered(MouseEvent e)
		{
			super.mouseEntered(e);
			statusText("开始分析");
		}
		
		@Override
		public void mouseClicked(MouseEvent e)
		{
			GUI.getInstance().disableButtons();
			
			new Thread(new Runnable()
			{
				@Override
				public void run()
				{
					try
					{
						new Interpreter(Integer.valueOf(GUI.getInstance().textFields[1].getText()), Float.valueOf(GUI.getInstance().textFields[2].getText()), Integer.valueOf(GUI.getInstance().textFields[3].getText()), GUI.getInstance().textFields[0].getText()).run();
					}
					catch (Exception e1)
					{
						e1.printStackTrace();
						statusText("执行出现异常" + e1.getLocalizedMessage());
					}
					
					GUI.getInstance().enableButtons();
					
					QueryHistoryFrame.showQueryHistory();
				}
			}).start();
		}
	} };
	
	private void disableButtons()
	{
		for (int i = 0; i < buttons.length; i++)
		{
			JButton jButton = buttons[i];
			jButton.setEnabled(false);
		}
		
		deleteListeners();
	}
	
	private void enableButtons()
	{
		for (int i = 0; i < buttons.length; i++)
		{
			JButton jButton = buttons[i];
			jButton.setEnabled(true);
		}
		
		initListeners();
	}
	
	private void restoreData()
	{
		object = JSONObject.fromObject("{}");
		fillInDefaultVals();
		fromJson();
	}
	
	public void saveData() throws IOException
	{
		toJson();
		
		File src = new File(DirUtils.getXxRoot("assets"), "pref.txt");
		
		PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(src),"UTF-8")));
		out.write(object.toString());
		out.close();
		
		File dst = new File(DirUtils.getXxRoot("assets"), "pref" + Calendar.getInstance().getTimeInMillis() + ".txt");
		FileUtils.copyFile(src, dst);
	}
	
	private void deleteListeners()
	{
		for (int i = 0; i < buttons.length; i++)
		{
			buttons[i].removeMouseListener(listeners[i]);
		}
	}
	
	@Override
	protected void initComponent()
	{
		super.initComponent();
		
		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.addItem("回溯交易日数");
		comboBox.addItem("回溯到日期");
		
		labels = new JComponent[] { new JLabel("选股公式"), new JLabel("最大列表长度"), new JLabel("最小接受的分值"), comboBox, new JLabel("下载线程数"), new JLabel("通达信路径") };
		textFields = new TextField[] { new TextField(90), new TextField(90), new TextField(90), new TextField(90), new TextField(90), new TextField(90) };
		buttons = new JButton[] { new JButton("保存设置"), new JButton("恢复默认设置"), new JButton("显示结果"), new JButton("调试选股公式"), new JButton("更新股票数据"), new JButton("开始选股") };
		
		setTitle("选股");
		setBackground(Color.WHITE);
		setLocation(200, 200);
		
		JPanel main = new JPanel(new GridBagLayout());
		
		GridBagConstraints gbc = new GridBagConstraints();
		
		gbc.gridheight = 1;
		gbc.weightx = 0;
		gbc.weighty = 0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.insets = new Insets(1, 1, 1, 1);
		gbc.ipadx = 1;
		gbc.ipady = 1;
		for (int i = 0; i < labels.length; i++)
		{
			gbc.gridy++;
			
			gbc.gridx = 0;
			gbc.gridwidth = 1;
			JComponent label = labels[i];
			label.setFont(SONGFONT_FONT);
			
			main.add(label, gbc);
			
			gbc.gridx = 1;
			gbc.gridwidth = GridBagConstraints.REMAINDER;
			main.add(textFields[i], gbc);
		}
		
		gbc.gridy++;
		for (int i = 0; i < buttons.length; i++)
		{
			JButton button = buttons[i];
			
			gbc.gridx = i;
			gbc.gridwidth = 1;
			button.setFont(SONGFONT_FONT);
			if (i == buttons.length - 1)
			{
				gbc.anchor = GridBagConstraints.EAST;
			}
			main.add(button, gbc);
		}
		
		add(main, BorderLayout.CENTER);
		
		setStatusText(READY);
		pack();
	}
}