package com.teamdev.appengine.meter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.NumberRenderer;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.RendererUtils;
import org.apache.jorphan.reflect.Functor;

public class AppEngineCostReport extends AbstractVisualizer {

	private static final long serialVersionUID = -4731016080238421438L;

	private JTable myJTable;
	
	private JScrollPane myScrollPane;
	
	private transient ObjectTableModel model;
	
	private final transient Object lock = new Object();
	
	private final Map<String, CostCalculator> tableRows =
	        new ConcurrentHashMap<String, CostCalculator>();
	
	private static final String[] COLUMNS = {
        "Label",              
        "# Samples",      
        "Average",
        "Sum"
        };
	
    private static final TableCellRenderer[] RENDERERS =
        new TableCellRenderer[]{
            null, // Label
            null, // count
            new NumberRenderer("$#0.00000"), // Average
            new NumberRenderer("$#0.000") // Sum
        };

	public AppEngineCostReport() {
		super();
		model = new ObjectTableModel(COLUMNS,
                CostCalculator.class,// All rows have this class
                new Functor[] {
                    new Functor("getLabel"),             
                    new Functor("getCount"),             
                    new Functor("getAverage"),
                    new Functor("getSum")    
                },
                new Functor[] { null, null, null},
                new Class[] { String.class, Long.class, String.class, String.class});
        clearData();
        init();
	}

	private void init() {
		this.setLayout(new BorderLayout());

        // MAIN PANEL
        JPanel mainPanel = new JPanel();
        Border margin = new EmptyBorder(10, 10, 5, 10);

        mainPanel.setBorder(margin);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        mainPanel.add(makeTitlePanel());

        myJTable = new JTable(model);
        myJTable.setPreferredScrollableViewportSize(new Dimension(500, 70));
        RendererUtils.applyRenderers(myJTable, RENDERERS);
        myScrollPane = new JScrollPane(myJTable);
        this.add(mainPanel, BorderLayout.NORTH);
        this.add(myScrollPane, BorderLayout.CENTER);
	}

	@Override
	public void add(final SampleResult sample) {
		if (!CostCalculator.hasCost(sample)) {
			return;
		}
		final String sampleLabel = sample.getSampleLabel();
        JMeterUtils.runSafe(new Runnable() {
            public void run() {
                CostCalculator row = null;
                synchronized (lock) {
                    row = tableRows.get(sampleLabel);
                    if (row == null) {
                        row = new CostCalculator(sampleLabel);
                        tableRows.put(row.getLabel(), row);
                        model.addRow(row);
                    }
                }
                /*
                 * Synch is needed because multiple threads can update the counts.
                 */
                synchronized(row) {
                    row.addSample(sample);
                }
                model.fireTableDataChanged();                
            }
        });
	}

	public void clearData() {
        //Synch is needed because a clear can occur while add occurs
        synchronized (lock) {
            model.clearData();
            tableRows.clear();
        }
    }

	@Override
	public String getLabelResource() {
		return null;
	}

	@Override
	public String getStaticLabel() {
		return "App Engine Cost Report";
	}
	
	

}
