package audioHandlerV2_Processors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.sampled.AudioFormat;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SynthProcessor extends AudioProcessor{
	
	private Object syncObject = new Object();
	private float frequency = 92.5f;
	private float panning = .5f;//0 is left, .5f is center, 1 is right
	private float volume = .5f;
	private int sampleCount = 0;
	
	private JSlider volSlider;
	private JSlider panSlider;
	private JTextField freqBox;
	private JCheckBox sineBox;
	private JCheckBox squareBox;
	
	private waveForm form = waveForm.SINE;
	
	public enum waveForm {
		SINE,
		SQUARE
	}
	
	public SynthProcessor() {
		setTitle(toString());
		setResizable(false);
		volSlider = new JSlider(JSlider.VERTICAL, 0, 100, 100);
		volSlider.setMajorTickSpacing(10);
		volSlider.setPaintTicks(true);
		volSlider.setPaintLabels(true);
		volSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				changeVol((float)volSlider.getValue()/100);
			}
		});
		panel.add(volSlider);
		panSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 100);
		panSlider.setMajorTickSpacing(10);
		panSlider.setPaintTicks(true);
		panSlider.setPaintLabels(true);
		panSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				changePan((float)panSlider.getValue()/100);
			}
		});
		panel.add(panSlider);
		freqBox = new JTextField("92.5", 6);
		freqBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					frequency = Float.parseFloat(freqBox.getText());
				} catch (Exception ex) {
					freqBox.setText(frequency + "");
				}
			}
		});
		panel.add(freqBox);
		JPanel formPanel = new JPanel();
		ButtonGroup group = new ButtonGroup();
		sineBox = new JCheckBox("Sine");
		sineBox.setSelected(true);
		sineBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeForm(waveForm.SINE);
			}
		});
		group.add(sineBox);
		formPanel.add(sineBox);
		squareBox = new JCheckBox("Square");
		squareBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				changeForm(waveForm.SQUARE);
			}
		});
		group.add(squareBox);
		formPanel.add(squareBox);
		panel.add(formPanel);
		setSize(500, 250);
	}

	@Override
	public float[] process(float[] samples, AudioFormat sampleFormat) {//currently only sine waves, add more later maybe
		float output[] = new float[samples.length];
		synchronized(syncObject) {
			switch(form) {
			case SINE:
				float coeff = 2 * (float)Math.PI * (frequency/sampleFormat.getSampleRate());
				if (sampleFormat.getChannels() == 1) {
					for (int i = 0; i < samples.length; i++) {
						output[i] = volume * (float) Math.sin(coeff*sampleCount) + samples[i];
						sampleCount++;
					}
				} if (sampleFormat.getChannels() == 2) {
					for ( int i = 0; i < samples.length; i += 2) {
						output[i] = (1 - panning) * volume * (float) Math.sin(coeff*sampleCount) + samples[i];
						output[i+1] = panning * volume * (float) Math.sin(coeff*sampleCount) + samples[i+1];
						sampleCount++;//possibility of wierdness on overflow but whatever
					}
				}
				break;
			case SQUARE:
				int samplesPerPeriod = (int) (sampleFormat.getSampleRate()/frequency);
				if (sampleFormat.getChannels() == 1) {
					for (int i = 0; i < samples.length; i++) {
						output[i] = (sampleCount % samplesPerPeriod > samplesPerPeriod>>1 ? volume : -1 * volume ) + samples[i];
						sampleCount++;
					}
				} if (sampleFormat.getChannels() == 2) {
					for ( int i = 0; i < samples.length; i += 2) {
						output[i] = (sampleCount % samplesPerPeriod > samplesPerPeriod>>1 ? volume : -1 * volume ) + samples[i];
						output[i + 1] = (sampleCount % samplesPerPeriod > samplesPerPeriod>>1 ? volume : -1 * volume ) + samples[i + 1];
						sampleCount++;
					}
				}
				break;
			}
			
			
		}
		
		return output;
	}
	
	public void changeVol(float newVol) {
		newVol = Math.min(Math.max(0, newVol), 1);
		synchronized(syncObject) {
			volume = newVol;
			volSlider.setValue((int)(newVol * 100));
		}
	}
	
	public void changePan(float newPan) {
		newPan = Math.min(Math.max(0, newPan), 1);
		synchronized(syncObject) {
			panning = newPan;
			panSlider.setValue((int)(newPan * 100));
		}
	}
	
	public void changeFreq(float newFreq) {
		newFreq = Math.max(newFreq, 0);
		synchronized (syncObject) {
			frequency = newFreq;
			freqBox.setText(newFreq + "");
		}
	}
	
	public void changeForm(waveForm form) {
		this.form = form;
		switch (form) {
		case SINE:
			sineBox.setSelected(true);
			break;
		case SQUARE:
			squareBox.setSelected(true);
			break;
		}
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public String toString() {
		return "SynthProcessor";
	}

}
