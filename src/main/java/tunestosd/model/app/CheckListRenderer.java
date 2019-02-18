package tunestosd.model.app;

import java.awt.Component;

import javax.swing.JCheckBox;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

class CheckListRenderer extends JCheckBox implements ListCellRenderer {
	public Component getListCellRendererComponent(JList list, final Object value, int index, boolean isSelected,
			boolean hasFocus) {
		final CheckListItem cli = (CheckListItem) value;
		setEnabled(list.isEnabled());
		setSelected(cli.isSelected());
		setFont(list.getFont());
		setBackground(list.getBackground());
		setForeground(list.getForeground());
		setText(value.toString());
		this.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				cli.setSelected(isSelected);
			}
		});
		return this;
	}
}
