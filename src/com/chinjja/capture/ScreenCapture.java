package com.chinjja.capture;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

public class ScreenCapture {
	private JFileChooser chooser = new JFileChooser();
	private Robot robot;
	private String ext;
	private Rectangle bound;
	
	public ScreenCapture(File dir) {
		chooser.setCurrentDirectory(dir);
	}
	
	public ScreenCapture(File dir, String ext) {
		if(!dir.exists()) {
			dir.mkdirs();
		}
		chooser.setCurrentDirectory(dir);
		this.ext = ext;
	}
	
	public void setExtension(String ext) {
		this.ext = ext;
	}
	
	public String getExtension() {
		return ext;
	}
	
	public void setBound(Rectangle bound) {
		this.bound = bound;
	}
	
	public Rectangle getBound() {
		return bound;
	}
	
	private File makeFile(String name) {
		if(ext == null) throw new NullPointerException("extension is null");
		return new File(chooser.getCurrentDirectory(), name + "." + ext);
	}
	
	private JDialog window;
	private JTextField directory;
	private JTextField filename;
	
	public boolean isStart() {
		return window != null;
	}
	
	private Timer timer = new Timer(250, new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent e) {
			timer.stop();
			if(window == null) return;
			capture(component);
		}
	});
	
	private Component component;
	
	public void setComponent(Component component) {
		this.component = component;
	}
	
	public Component getComponent() {
		return component;
	}
	
	public void start() {
		if(window != null) return;
		window = new JDialog();
		window.setTitle("Screen Capture");
		try {
			window.setIconImage(ImageIO.read(getClass().getResource("capture.png")));
		} catch(IOException e1) {
			e1.printStackTrace();
		}
		window.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentHidden(ComponentEvent e) {
				timer.restart();
			}
			
		});
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});
		window.setAlwaysOnTop(true);
		window.setResizable(false);
		window.setFocusableWindowState(false);
		
		JButton capture = new JButton("Capture");
		capture.setIcon(new ImageIcon(getClass().getResource("capture.png")));
		capture.setFocusable(false);
		capture.setFont(capture.getFont().deriveFont(Font.BOLD, 14));
		capture.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				prepare();
			}
		});
		JButton close = new JButton("Close");
		close.setIcon(new ImageIcon(getClass().getResource("close.png")));
		close.setFocusable(false);
		close.setFont(close.getFont().deriveFont(Font.BOLD, 14));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		JPanel buttons = new JPanel(new GridLayout(1, 2));
		buttons.add(capture);
		buttons.add(close);
		
		directory = new JTextField(20);
		directory.setFont(directory.getFont().deriveFont(14f));
		directory.setEditable(false);
		JButton changeDir = new JButton("...");
		changeDir.setFocusable(false);
		changeDir.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if(chooser.showSaveDialog(window) != JFileChooser.APPROVE_OPTION) return;
				
				File dir = chooser.getSelectedFile();
				chooser.setCurrentDirectory(dir);
				updateDirectory();
			}
		});
		JPanel dir = new JPanel(new BorderLayout());
		dir.add(BorderLayout.CENTER, directory);
		dir.add(BorderLayout.EAST, changeDir);
		
		filename = new JTextField();
		filename.setFont(filename.getFont().deriveFont(14f));
		filename.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				prepare();
			}
		});
		filename.addMouseListener(new MouseAdapter() {

			@Override
			public void mousePressed(MouseEvent e) {
				window.setFocusableWindowState(true);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						filename.requestFocus();
					}
				});
			}
		});
		filename.addFocusListener(new FocusListener() {
			
			@Override
			public void focusLost(FocusEvent e) {
				window.setFocusableWindowState(false);
			}
			
			@Override
			public void focusGained(FocusEvent e) {
				filename.selectAll();
			}
		});
		
		window.add(BorderLayout.NORTH, dir);
		window.add(BorderLayout.CENTER, filename);
		window.add(BorderLayout.SOUTH, buttons);
		window.pack();
		updateLocation(component);
		updateFilename();
		updateDirectory();
		window.setVisible(true);
	}
	
	private void prepare() {
		window.setVisible(false);
	}
	
	private void updateLocation(Component component) {
		if(bound != null) {
			window.setLocation(
					bound.x + (bound.width - window.getWidth())/2,
					bound.y + (bound.height - window.getHeight())/2);
		} else {
			window.setLocationRelativeTo(component);
		}
	}
	
	public void stop() {
		if(window == null) return;
		window.dispose();
		window = null;
	}
	
	public File generateFile() {
		SimpleDateFormat format = new SimpleDateFormat("yyyyMMdd-HHmmssSSS");
		File file = makeFile(format.format(new Date()));
		return file;
	}
	
	private void updateFilename() {
		if(window == null) return;
		this.filename.setText(generateFile().getName());
	}
	
	private void updateDirectory() {
		if(window == null) return;
		directory.setText(chooser.getCurrentDirectory().getAbsolutePath());
	}
	
	public void capture(Component component) {
		try {
			BufferedImage image = grabImage(component);
			window.setVisible(true);
			String filename = this.filename.getText();
			if(filename.trim().isEmpty()) {
				JOptionPane.showMessageDialog(window, "filename is empty");
				return;
			}
			if(!filename.endsWith("." + ext)) {
				filename += ("." + ext);
			}
			File file = new File(chooser.getCurrentDirectory(), filename);
			if(file.exists()) {
				int rs = JOptionPane.showConfirmDialog(window, "Overrite?", filename, JOptionPane.YES_NO_OPTION);
				if(rs != JOptionPane.YES_OPTION) return;
			}
			updateFilename();
			updateDirectory();
			ImageIO.write(image, ext, file);
		} catch(Exception e) {
			JOptionPane.showMessageDialog(window, e.getLocalizedMessage());
		}
	}
	
	public BufferedImage grabImage(Component component) throws AWTException {
		if(robot == null) {
			robot = new Robot();
		}
		Rectangle bound;
		if(component == null) {
			if(this.bound != null) {
				bound = this.bound;
			} else {
				bound = new Rectangle(new Point(), Toolkit.getDefaultToolkit().getScreenSize());
			}
		} else {
			bound = new Rectangle(component.getLocationOnScreen(), component.getSize());
		}
		return robot.createScreenCapture(bound);
	}
	
	// java -jar xx.jar path extension x y width height
	public static void main(final String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					UIManager.setLookAndFeel(new NimbusLookAndFeel());
					
					File path;
					if(args.length > 0) {
						path = new File(args[0]);
					} else {
						JFileChooser chooser = new JFileChooser();
						chooser.setDialogTitle("Select Save Directory");
						chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
						if(chooser.showSaveDialog(null) != JFileChooser.APPROVE_OPTION) return;
						path = chooser.getSelectedFile().getAbsoluteFile();
					}
					
					String ext = "png";
					if(args.length > 1) {
						ext = args[1];
					}
					
					Rectangle bound = null;
					if(args.length > 5) {
						bound = new Rectangle(
								Integer.parseInt(args[2]),
								Integer.parseInt(args[3]),
								Integer.parseInt(args[4]),
								Integer.parseInt(args[5])
								);
					}
					
					ScreenCapture capture = new ScreenCapture(path, ext);
					if(bound != null) {
						capture.setBound(bound);
					}
					capture.start();
				} catch(Exception e) {
					e.printStackTrace();
				} finally {
					System.out.println("capture");
				}
			}
		});
	}
}
