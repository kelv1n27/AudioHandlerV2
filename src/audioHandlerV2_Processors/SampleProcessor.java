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
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class SampleProcessor extends AudioProcessor{
	
	private AudioInputStream inStream;
	private AudioFormat format;
	private boolean looping = false;
	private int reader = 0;
	private Object syncObject = new Object();
	private String source;
	private JCheckBox loopBox;
	private JSlider progressSlider;
	private boolean paused = false;
	private float speed = 1.0f;
	
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
		JButton pauseResumeButton = new JButton("Pause/Resume");
		pauseResumeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				togglePause(!paused);
			}
		});
		panel.add(pauseResumeButton);
		setSize(450, 150);
		try {
			inStream = AudioSystem.getAudioInputStream(new BufferedInputStream(getClass().getResourceAsStream(source)));
			format = inStream.getFormat();
			inStream.mark(Integer.MAX_VALUE);
			inStream.reset();
			this.source = source;
			sourceLabel.setText(source);
			setTitle("Sampler: "+ source);
//			System.out.println(inStream.available());
			progressSlider = new JSlider(JSlider.HORIZONTAL, 0, inStream.available(), 0);
			progressSlider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					try {
						inStream.reset();
						inStream.skip(progressSlider.getValue());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			});
			panel.add(progressSlider);
		} catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		JTextField speedBox = new JTextField("1.0", 5);
		speedBox.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					float newSpeed = Float.parseFloat(speedBox.getText());
					if (newSpeed > 0) setSpeed(newSpeed);
					speedBox.setText(speed + "");
				} catch (Exception ex) {
					speedBox.setText(speed + "");
				}
			}
		});
		panel.add(speedBox);
	}

	//out[n] = 1 >= sample[n] >= -1 --if looping out[n] = 1 >= sample[n%sample.length] >= -1
	@Override
	public float[] process(float[] samples, AudioFormat sampleFormat) {
		byte[] inBytes = new byte[(int)(samples.length * speed) * (sampleFormat.getSampleSizeInBits()/8) ];
		float[] output = new float[samples.length];
		synchronized(syncObject) {
			if (reader != -1 && !paused) {
				try {
					reader = inStream.read(inBytes, 0, inBytes.length);
					float[] temp = decodeBytes(inBytes, format);
					for(int i = 0; i < samples.length; i++) {
						output[i] = Math.max(-1.0f, Math.min(1.0f, samples[i]+temp[Math.min(temp.length - 1, (int)(i * speed))]));
					}
					progressSlider.setValue(progressSlider.getValue() + reader);
				} catch (Exception e) {
					e.printStackTrace();
				}
				if (reader == -1 && looping) {				//if looping
					reader = 0;	
					progressSlider.setValue(0);				//if looping
					try {									//if looping
						inStream.reset();
						//if looping
					} catch (IOException e) {				//if looping
						e.printStackTrace();				//if looping
					}										//if looping
				}	
			}
			if (paused)//pass through for when paused
				return samples;
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
	
	public float getSpeed() {
		return speed;
	}
	
	public void setSpeed(float speed) {
		this.speed = speed;
	}
	
	public void resetSample() {
		synchronized(syncObject) {
			try {
				reader = 0;
				inStream.reset();
				progressSlider.setValue(0);
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

	public void togglePause(boolean paused) {
		this.paused = paused;
	}
	
	@Override
	public String toString() {
		return "Sampler: " + source;
	}

}
