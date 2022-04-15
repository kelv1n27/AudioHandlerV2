package audioHandlerV2_Core;

import java.awt.Color;
import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;
import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import audioHandlerV2_Processors.AudioProcessor;

public class AudioWorker extends VisibleComponent{

	private AudioFormat format;
	private int floatBufferSize;
	private ArrayList<AudioProcessor> processors = new ArrayList<AudioProcessor>();
	private ArrayList<AudioWorker> workers = new ArrayList<AudioWorker>();
	private Object syncObject = new Object();
	
	//////////////////////////////////
	private String name;
	private DefaultListModel inModel = new DefaultListModel();
	private JList inputs = new JList(inModel);
	private DefaultListModel procModel = new DefaultListModel();
	private JList processorList = new JList(procModel);
	//////////////////////////////////
	
	public AudioWorker(int floatBufferSize, AudioFormat format, String name) {
		this.floatBufferSize = floatBufferSize;
		this.format = format;
		
		////////////////////////////
		this.name = name;
		setTitle(toString());
		setResizable(false);
		getContentPane().setForeground(Color.BLACK);
		
		JLabel inputLabel = new JLabel("inputs");
		JPanel inputsPanel = new JPanel();
		inputsPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		inputsPanel.add(inputLabel);
		inputsPanel.add(inputs);
		inputs.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
            	if (!arg0.getValueIsAdjusting() && inputs.getSelectedValue() != null) {
            		((VisibleComponent) inputs.getSelectedValue()).setVisible(true);
            		inputs.clearSelection();
            	}
            }
        });
		panel.add(inputsPanel, "East");
		
		JLabel processorsLabel = new JLabel("processors");
		JPanel processorsPanel = new JPanel();
		processorsPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		processorsPanel.add(processorsLabel);
		processorsPanel.add(processorList);
		processorList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent arg0) {
            	if (!arg0.getValueIsAdjusting() && processorList.getSelectedValue() != null) {
            		((VisibleComponent) processorList.getSelectedValue()).setVisible(true);
            		processorList.clearSelection();
            	}
            }
        });
		panel.add(processorsPanel, "West");
		//panel.setSize(300, 100);
		setSize(600, 200);
		////////////////////////////
	}
	
	public float[] process() {
		float[] samples = new float[floatBufferSize];
		synchronized(syncObject) {
			for (int i = 0; i < workers.size(); i++) {
				float temp[] = workers.get(i).process();
				for (int j = 0; j < temp.length; j++)
					samples[j] += temp[j];
			}
			for (int i = 0; i < processors.size(); i++)
				samples = processors.get(i).process(samples, format);
		}
		return samples;	
	}
	
	public AudioWorker addWorker(String name) {
		synchronized(syncObject) {
			AudioWorker worker = new AudioWorker(floatBufferSize, format, name);
			workers.add(worker);
			//
			inModel.addElement(worker);
			//
			return worker;
		}
	}
	
	public void addProcessor(AudioProcessor p) {
		synchronized(syncObject) {
			processors.add(p);
			//
			procModel.addElement(p);
			//
		}
	}
	
	public void removeWorker(AudioWorker worker) {
		synchronized(syncObject) {
			workers.remove(worker);
			worker.dispose();
			//
			inModel.removeElement(worker);
			//
		}
	}
	
	public void removeProcessor(AudioProcessor p) {
		synchronized(syncObject) {
			processors.remove(p);
			p.dispose();
			//
			procModel.removeElement(p);
			//
		}
	}

	//this is synchronized at AudioHandler level
	public void setBufferSize(int newBufferSize) {
		floatBufferSize = newBufferSize;
		for (AudioWorker a : workers)
			a.setBufferSize(newBufferSize);
	}
	
	public void dispose() {
		super.dispose();
		for (AudioWorker w : workers)
			w.dispose();
		for (AudioProcessor p : processors) {
			p.dispose();
		}
	}

	@Override///////////////////////////////
	public String toString() {
		return "AudioWorker: " + name;
	}

}
