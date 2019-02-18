package tunestosd.model.app;

import java.util.ArrayList;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import tunestosd.model.original.JobResult;

public class JobTableModel extends AbstractTableModel {
	private String[] columnNames = new String[] { "Status", "Artist", "Album", "Track", "Description" };
	private List<JobResult> data = new ArrayList<>();

	public void clear() {
		data.clear();
	}

	public void add(JobResult item) {
		if (data.contains(item) == false)
			data.add(item);
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return data == null ? 0 : data.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		if (data == null)
			return null;

		JobResult r = data.get(row);
		switch (column) {
		case 0:
			return r.getStatus();
		case 1:
			return r.getTrack().getAdditionalTrackInfo().getAdditionalInfo("Artist");
		case 2:
			return r.getTrack().getAdditionalTrackInfo().getAdditionalInfo("Album");
		case 3:
			return r.getTrack().getTrackName();
		case 4:
			return r.getDescription();
		default:
			return "";
		}
	}

	public List<JobResult> getData() {
		return data;
	}

}
