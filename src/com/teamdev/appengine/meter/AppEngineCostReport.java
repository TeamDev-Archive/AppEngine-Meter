package com.teamdev.appengine.meter;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellRenderer;

import org.apache.jmeter.gui.util.FileDialoger;
import org.apache.jmeter.samplers.SampleResult;
import org.apache.jmeter.save.CSVSaveService;
import org.apache.jmeter.testelement.TestElement;
import org.apache.jmeter.util.JMeterUtils;
import org.apache.jmeter.visualizers.gui.AbstractVisualizer;
import org.apache.jorphan.gui.NumberRenderer;
import org.apache.jorphan.gui.ObjectTableModel;
import org.apache.jorphan.gui.RendererUtils;
import org.apache.jorphan.logging.LoggingManager;
import org.apache.jorphan.reflect.Functor;
import org.apache.jorphan.util.JOrphanUtils;
import org.apache.log.Logger;

public class AppEngineCostReport extends AbstractVisualizer implements ActionListener {

	private static final String USE_GROUP_NAME = "useGroupName"; //$NON-NLS-1$

    private static final String SAVE_HEADERS   = "saveHeaders"; //$NON-NLS-1$
	
	private static final long serialVersionUID = -4731016080238421438L;
	
	private static final Logger log = LoggingManager.getLoggerForClass();

	private JTable myJTable;
	
	private JScrollPane myScrollPane;
	
	private final JButton saveTable =
        new JButton(JMeterUtils.getResString("aggregate_graph_save_table"));

    private final JCheckBox saveHeaders = // should header be saved with the data?
        new JCheckBox(JMeterUtils.getResString("aggregate_graph_save_table_header"),true);

    private final JCheckBox useGroupName =
        new JCheckBox(JMeterUtils.getResString("aggregate_graph_use_group_name"));

	
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
        
        saveTable.addActionListener(this);
        JPanel opts = new JPanel();
        opts.add(useGroupName, BorderLayout.WEST);
        opts.add(saveTable, BorderLayout.CENTER);
        opts.add(saveHeaders, BorderLayout.EAST);
        this.add(opts,BorderLayout.SOUTH);
	}

	@Override
	public void add(final SampleResult sample) {
		if (!CostCalculator.hasCost(sample)) {
			return;
		}
		final String sampleLabel = sample.getSampleLabel(useGroupName.isSelected());
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

	@Override
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

	public void actionPerformed(ActionEvent ev) {
        if (ev.getSource() == saveTable) {
            JFileChooser chooser = FileDialoger.promptToSaveFile("expenses.csv");//$NON-NLS-1$
            if (chooser == null) {
                return;
            }
            FileWriter writer = null;
            try {
                writer = new FileWriter(chooser.getSelectedFile());
                CSVSaveService.saveCSVStats(model,writer, saveHeaders.isSelected());
            } catch (FileNotFoundException e) {
                log.warn(e.getMessage());
            } catch (IOException e) {
                log.warn(e.getMessage());
            } finally {
                JOrphanUtils.closeQuietly(writer);
            }
        }
    }
	
	@Override
    public void modifyTestElement(TestElement c) {
        super.modifyTestElement(c);
        c.setProperty(USE_GROUP_NAME, useGroupName.isSelected(), false);
        c.setProperty(SAVE_HEADERS, saveHeaders.isSelected(), true);
    }

    @Override
    public void configure(TestElement el) {
        super.configure(el);
        useGroupName.setSelected(el.getPropertyAsBoolean(USE_GROUP_NAME, false));
        saveHeaders.setSelected(el.getPropertyAsBoolean(SAVE_HEADERS, true));
    }

}
