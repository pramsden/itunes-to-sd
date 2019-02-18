package tunestosd.model.app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.lang3.StringUtils;

import com.gps.itunes.lib.items.playlists.Playlist;
import com.gps.itunes.lib.items.playlists.PlaylistItem;
import com.gps.itunes.lib.items.tracks.Track;
import com.gps.itunes.lib.parser.ItunesLibraryParsedData;

import tunestosd.model.ITunesImporter;
import tunestosd.model.org.ExportOptions;
import tunestosd.model.org.JobExecutor;
import tunestosd.model.org.JobResult;

public class MainWindow extends JFrame {
	private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(MainWindow.class);

	private JobExecutor exporter;
	private JList<Playlist> listPlaylists;
	private JobTableModel tableModel;
	private JButton btnStart;
	private JButton btnCancel;
	private JPanel panel_1;

	private boolean wantCancel;

	public MainWindow() {
		setTitle("ITunes to SD Card");

		JSplitPane splitPane = new JSplitPane();
		splitPane.setResizeWeight(0.4);
		getContentPane().add(splitPane, BorderLayout.CENTER);

		JPanel panelLeft = new JPanel();
		splitPane.setLeftComponent(panelLeft);
		panelLeft.setLayout(new BorderLayout(0, 0));

		JPanel panelOptions = new JPanel();
		panelLeft.add(panelOptions, BorderLayout.CENTER);
		GridBagLayout gbl_panelOptions = new GridBagLayout();
		gbl_panelOptions.columnWidths = new int[] { 0, 0 };
		gbl_panelOptions.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panelOptions.columnWeights = new double[] { 0.0, 0.0 };
		gbl_panelOptions.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		panelOptions.setLayout(gbl_panelOptions);

		JLabel lblNewLabel = new JLabel("ITunes file");
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridy = 0;
		gbc_lblNewLabel.gridx = 0;
		panelOptions.add(lblNewLabel, gbc_lblNewLabel);

		txtItunes = new JTextField();
		GridBagConstraints gbc_txtItunes = new GridBagConstraints();
		gbc_txtItunes.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtItunes.anchor = GridBagConstraints.WEST;
		gbc_txtItunes.insets = new Insets(0, 0, 5, 5);
		gbc_txtItunes.gridy = 1;
		gbc_txtItunes.gridx = 0;
		panelOptions.add(txtItunes, gbc_txtItunes);
		txtItunes.setColumns(10);
		txtItunes.setText("C:\\Users\\paul\\Music\\iTunes\\iTunes Music Library.xml");

		JButton btnSelectItunes = new JButton("...");
		btnSelectItunes.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fc = new JFileChooser(txtItunes.getText());
				int returnVal = fc.showOpenDialog(MainWindow.this);

				if (returnVal == JFileChooser.APPROVE_OPTION) {
					File file = fc.getSelectedFile();
					txtItunes.setText(file.getAbsolutePath());
					openItunes(file);
				}
			}
		});
		GridBagConstraints gbc_btnSelectItunes = new GridBagConstraints();
		gbc_btnSelectItunes.insets = new Insets(0, 0, 5, 0);
		gbc_btnSelectItunes.gridy = 1;
		gbc_btnSelectItunes.gridx = 1;
		panelOptions.add(btnSelectItunes, gbc_btnSelectItunes);

		JLabel lblDestinationPath = new JLabel("Destination path");
		GridBagConstraints gbc_lblDestinationPath = new GridBagConstraints();
		gbc_lblDestinationPath.anchor = GridBagConstraints.WEST;
		gbc_lblDestinationPath.gridwidth = 2;
		gbc_lblDestinationPath.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblDestinationPath.insets = new Insets(0, 0, 5, 0);
		gbc_lblDestinationPath.gridx = 0;
		gbc_lblDestinationPath.gridy = 2;
		panelOptions.add(lblDestinationPath, gbc_lblDestinationPath);

		txtTarget = new JTextField();
		txtTarget.setText("f:\\");
		GridBagConstraints gbc_txtTarget = new GridBagConstraints();
		gbc_txtTarget.weightx = 1.0;
		gbc_txtTarget.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtTarget.insets = new Insets(0, 0, 5, 5);
		gbc_txtTarget.gridx = 0;
		gbc_txtTarget.gridy = 3;
		panelOptions.add(txtTarget, gbc_txtTarget);
		txtTarget.setColumns(10);

		JButton btnChooseTarget = new JButton("...");
		GridBagConstraints gbc_btnChooseTarget = new GridBagConstraints();
		gbc_btnChooseTarget.insets = new Insets(0, 0, 5, 0);
		gbc_btnChooseTarget.gridx = 1;
		gbc_btnChooseTarget.gridy = 3;
		panelOptions.add(btnChooseTarget, gbc_btnChooseTarget);

		JLabel lblStoreConvertedMp = new JLabel("Store converted MP3 files here (optional)");
		GridBagConstraints gbc_lblStoreConvertedMp = new GridBagConstraints();
		gbc_lblStoreConvertedMp.anchor = GridBagConstraints.WEST;
		gbc_lblStoreConvertedMp.gridwidth = 2;
		gbc_lblStoreConvertedMp.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblStoreConvertedMp.insets = new Insets(0, 0, 5, 0);
		gbc_lblStoreConvertedMp.gridx = 0;
		gbc_lblStoreConvertedMp.gridy = 4;
		panelOptions.add(lblStoreConvertedMp, gbc_lblStoreConvertedMp);

		txtLibrary = new JTextField();
		txtLibrary.setText("p:\\mp3library");
		GridBagConstraints gbc_txtLibrary = new GridBagConstraints();
		gbc_txtLibrary.fill = GridBagConstraints.HORIZONTAL;
		gbc_txtLibrary.insets = new Insets(0, 0, 5, 5);
		gbc_txtLibrary.gridx = 0;
		gbc_txtLibrary.gridy = 5;
		panelOptions.add(txtLibrary, gbc_txtLibrary);
		txtLibrary.setColumns(10);

		JButton button = new JButton("...");
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.insets = new Insets(0, 0, 5, 0);
		gbc_button.gridx = 1;
		gbc_button.gridy = 5;
		panelOptions.add(button, gbc_button);

		JLabel lblPlaylists = new JLabel("Playlists");
		GridBagConstraints gbc_lblPlaylists = new GridBagConstraints();
		gbc_lblPlaylists.anchor = GridBagConstraints.LINE_START;
		gbc_lblPlaylists.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblPlaylists.insets = new Insets(0, 0, 0, 5);
		gbc_lblPlaylists.gridx = 0;
		gbc_lblPlaylists.gridy = 6;
		panelOptions.add(lblPlaylists, gbc_lblPlaylists);

		listPlaylists = new JList<Playlist>();
		GridBagConstraints gbc_listPlaylists = new GridBagConstraints();
		gbc_listPlaylists.insets = new Insets(0, 0, 5, 0);
		gbc_listPlaylists.weighty = 1.0;
		gbc_listPlaylists.weightx = 1.0;
		gbc_listPlaylists.gridwidth = 2;
		gbc_listPlaylists.fill = GridBagConstraints.BOTH;
		gbc_listPlaylists.gridx = 0;
		gbc_listPlaylists.gridy = 7;
		panelOptions.add(new JScrollPane(listPlaylists), gbc_listPlaylists);
		listPlaylists.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent e) {
				onPlaylistSelected();
			}
		});
		listPlaylists.setCellRenderer(new DefaultListCellRenderer() {
			private Color background = new Color(0, 100, 255, 15);
			private Color defaultBackground = (Color) UIManager.get("List.background");

			@Override
			public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
					boolean cellHasFocus) {
				Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
				if (c instanceof JLabel) {
					JLabel label = (JLabel) c;
					Playlist item = (Playlist) value;
					label.setText(String.format("%s [%d]", item.getName(), item.getPlaylistItems().length));
					if (!isSelected) {
						label.setBackground(index % 2 == 0 ? background : defaultBackground);
					}
				}
				return c;
			}
		});

		panel_1 = new JPanel();
		panelLeft.add(panel_1, BorderLayout.SOUTH);
		panel_1.setLayout(new BorderLayout(0, 0));

		btnStart = new JButton("Start");
		panel_1.add(btnStart, BorderLayout.WEST);
		btnStart.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				startJob();
			}
		});

		// btnCancel = new JButton("Cancel");
		// btnCancel.setEnabled(false);
		// btnCancel.setVisible(false);
		// panel_1.add(btnCancel, BorderLayout.WEST);
		//
		// panel_1.doLayout();

		JPanel panelRight = new JPanel();
		splitPane.setRightComponent(panelRight);
		panelRight.setLayout(new BorderLayout(0, 0));

		tableJobs = new JTable();
		tableJobs.setFillsViewportHeight(true);
		final JScrollPane sp1 = new JScrollPane();
		sp1.setPreferredSize(new Dimension(600, 200));
		sp1.setViewportView(tableJobs);
		panelRight.add(sp1);
		tableModel = new JobTableModel();
		tableJobs.setModel(tableModel);

		if (StringUtils.isNotEmpty(txtItunes.getText())) {
			File file = new File(txtItunes.getText());
			if (file.exists() && file.isFile())
				openItunes(file);
		}
	}

	protected void startJob() {
		wantCancel = false;
		panel_1.doLayout();
		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				try {
					exporter.export(getExportOptions(), tableModel.getData());

					exporter.createPlaylists(getExportOptions(), listPlaylists.getSelectedValuesList(),
							tableModel.getData());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				return null;
			}
		};

		worker.execute();

		stopJob();
	}

	private void stopJob() {
		panel_1.doLayout();
	}

	private ExportOptions getExportOptions() {
		ExportOptions options = new ExportOptions();
		options.setPathMp3Library(new File(txtLibrary.getText()));
		options.setTargetFolder(new File(txtTarget.getText()));
		return options;
	}

	protected void onPlaylistSelected() {
		tableModel.clear();
		for (Playlist play : listPlaylists.getSelectedValuesList()) {
			for (PlaylistItem item : play.getPlaylistItems()) {
				long id = item.getTrackId();
				Track track = exporter.getTrack(id);
				if (track != null) {
					tableModel.add(new JobResult(track));
				}
			}
		}
		tableModel.fireTableDataChanged();
	}

	protected void openItunes(File file) {
		try {
			new ITunesImporter(file) {
				@Override
				protected void onImportFailed(File file, Exception e) {
					log.error(file.getAbsoluteFile(), e);
					super.onImportFailed(file, e);
				}

				@Override
				protected void onImportSuccess(ItunesLibraryParsedData library) {
					fillPlaylists(library);
				}
			};
//			exporter.addListener(new IExportListener() {
//
//				@Override
//				public boolean start() {
//					// TODO Auto-generated method stub
//					return wantCancel;
//				}
//
//				@Override
//				public boolean foundTracks(Track[] tracks) {
//					// TODO Auto-generated method stub
//					return wantCancel;
//				}
//
//				@Override
//				public boolean exportResult(JobResult result, int row) {
//					tableModel.fireTableRowsUpdated(row, row);
//					tableJobs.scrollRectToVisible(tableJobs.getCellRect(row, 0, true));
//					tableJobs.setRowSelectionInterval(row, row);
//					return wantCancel;
//				}
//
//				@Override
//				public void complete() {
//					// TODO Auto-generated method stub
//
//				}
//
//				@Override
//				public boolean addedToLibrary(Track track) {
//					// TODO Auto-generated method stub
//					return wantCancel;
//				}
//			});

			// fillPlaylists();
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Could not connect to ITunes file.\n\n" + e.getMessage());
		}
	}

	private void fillPlaylists(ItunesLibraryParsedData library) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				listPlaylists.removeAll();
				DefaultListModel<Playlist> model = new DefaultListModel<>();
				listPlaylists.setModel(model);

				for (Playlist play : library.getAllPlaylists()) {
					if (play.getPlaylistItems().length > 0)
						model.addElement(play);
				}
			}

		});
	}

	private static final long serialVersionUID = -7181091678596404810L;
	private JTextField txtItunes;
	private JTextField txtTarget;
	private JTextField txtLibrary;
	private JTable tableJobs;

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked
	 * from the event-dispatching thread.
	 */
	private static void createAndShowGUI() {
		MainWindow win = new MainWindow();
		win.setSize(900, 600);
		win.setVisible(true);
	}

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				createAndShowGUI();
			}
		});
	}

}
