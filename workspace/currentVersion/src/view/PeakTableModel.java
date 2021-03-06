package view;
import java.awt.Color;
import java.util.Arrays;
import java.util.List;

import javax.swing.table.DefaultTableModel;


@SuppressWarnings("serial")
class PeakTableModel extends DefaultTableModel {

    List<Color> rowColours = Arrays.asList(
        Color.YELLOW
    );

    public PeakTableModel(Object[][] objects, String[] strings) {
		super(objects, strings);
	}

	public void setRowColour(int row, Color c) {
        rowColours.set(row, c);
        fireTableRowsUpdated(row, row);
    }

    public Color getRowColour(int row) {
        return rowColours.get(row);
    }

    @Override
    public Object getValueAt(int row, int column) {
        return String.format("%d %d", row, column);
    }
}