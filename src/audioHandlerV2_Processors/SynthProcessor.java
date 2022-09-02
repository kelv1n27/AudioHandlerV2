package audioHandlerV2_Processors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.sound.sampled.AudioFormat;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SynthProcessor extends AudioProcessor{
	
	private Object syncObject = new Object();
	private float frequency = 92.5f;
	private float panning = .5f;//0 is left, .5f is center, 1 is right
	private float volume = .75f;
	private int sampleCount = 0;
	
	JSlider volSlider;
	JSlider panSlider;
	JTextField freqBox;
	
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
		setSize(500, 250);
	}

	@Override
	public float[] process(float[] samples, AudioFormat sampleFormat) {//currently only sine waves, add more later maybe
		float output[] = new float[samples.length];
		float coeff = 2 * (float)Math.PI * (frequency/sampleFormat.getSampleRate());
		synchronized(syncObject) {
			for ( int i = 0; i < samples.length; i += sampleFormat.getChannels()) {
				output[i] = (1 - panning) * volume * (float) Math.sin(coeff*sampleCount) + samples[i];
				if (sampleFormat.getChannels() == 2)
					output[i+1] = panning * volume * (float) Math.sin(coeff*sampleCount) + samples[i+1];
				sampleCount++;//possibility of wierdness on overflow but whatever
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

	@Override
	public void dispose() {
		
	}

	@Override
	public String toString() {
		return "SynthProcessor";
	}

}
