package audioHandlerV2_Processors;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.BufferedInputStream;
import java.io.IOException;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class SampleProcessor extends AudioProcessor{
	
	private AudioInputStream inStream;
	private AudioFormat format;
	private boolean looping = false;
	private int reader = 0;
	private Object syncObject = new Object();
	private String source;
	JCheckBox loopBox;
	
	public SampleProcessor(String source) {
		setTitle("Sampler: no source");
		JLabel sourceLabel = new JLabel("No Source");
		panel.add(sourceLabel);
		loopBox = new JCheckBox("looping");
		loopBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				setLooping(e.getStateChange() == 1);
			}
		});
		panel.add(loopBox);
		JButton resetButton = new JButton("Reset Sample");
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				resetSample();
			}
		});
		panel.add(resetButton);
		setSize(450, 150);
		try {
			inStream = AudioSystem.getAudioInputStream(new BufferedInputStream(getClass().getResourceAsStream(source)));
			format = inStream.getFormat();
			inStream.mark(Integer.MAX_VALUE);
			inStream.reset();
			this.source = source;
			sourceLabel.setText(source);
			setTitle("Sampler: "+ source);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//out[n] = 1 >= sample[n] >= -1 --if looping out[n] = 1 >= sample[n%sample.length] >= -1
	@Override
	public float[] process(float[] samples, AudioFormat sampleFormat) {
		byte[] inBytes = new byte[samples.length * (sampleFormat.getSampleSizeInBits()/8)];
		float[] output = new float[samples.length];
		synchronized(syncObject) {
			if (reader != -1) {
				try {
					reader = inStream.read(inBytes, 0, inBytes.length);
					float[] temp = decodeBytes(inBytes, format);
					for(int i = 0; i < samples.length; i++) {
						output[i] = Math.max(-1.0f, Math.min(1.0f, samples[i]+temp[i]));
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (reader == -1 && looping) {				//if looping
					reader = 0;								//if looping
					try {									//if looping
						inStream.reset();					//if looping
					} catch (IOException e) {				//if looping
						e.printStackTrace();				//if looping
					}										//if looping
				}	
			}
			return output;
		}	
	}
	
	public void dispose() {
		try {
			inStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void setLooping(boolean looping) {
		synchronized(syncObject) {
			this.looping = looping;
			loopBox.setSelected(looping);
		}
	}
	
	public boolean getLooping() {
		synchronized(syncObject) {
			return looping;
		}
	}
	
	public void resetSample() {
		synchronized(syncObject) {
			try {
				reader = 0;
				inStream.reset();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private float[] decodeBytes(byte[] bytes, AudioFormat format) {
		int bytesPerSample = format.getSampleSizeInBits()/8;
		float[] samples = new float[bytes.length/bytesPerSample];
		for (int i = 0; i < bytes.length; i+=bytesPerSample) {
			long temp = (bytes[i] & 0xff) | (bytesPerSample==1?0:((bytes[i + 1] & 0xff) << 8));
			if (format.getEncoding()==Encoding.PCM_SIGNED) {
				temp = (temp << (64 - format.getSampleSizeInBits()) >> (64 - format.getSampleSizeInBits()));
			} else if (format.getEncoding() == Encoding.PCM_UNSIGNED) {
				temp -= Math.pow(2.0, format.getSampleSizeInBits() - 1);
			}
			samples[i/bytesPerSample] = (float) (temp/Math.pow(2.0, format.getSampleSizeInBits() - 1));
		}
		return samples;
	}

	@Override
	public String toString() {
		return "Sampler: " + source;
	}

}
