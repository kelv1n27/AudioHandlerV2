package audioHandlerV2_Processors;

import javax.sound.sampled.AudioFormat;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class VolumeProcessor extends AudioProcessor{

	private float vol;
	private Object syncObject = new Object();
	private JSlider slider;
	
	public VolumeProcessor() {
		vol = 1.0f;
		
		setTitle(toString());
		setResizable(false);
		slider = new JSlider(JSlider.VERTICAL, 0, 100, 100);
		slider.setMajorTickSpacing(10);
		slider.setPaintTicks(true);
		slider.setPaintLabels(true);
		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				changeVol((float)slider.getValue()/100);
			}
		});
		JLabel volumeLabel = new JLabel("Volume %");
		panel.add(volumeLabel);
		panel.add(slider);
		setSize(200, 250);
	}
	
	public VolumeProcessor(float vol) {
		this.vol = vol;
	}
	
	//out[n] = 1 >= vol * in[n] >= -1
	@Override
	public float[] process(float[] samples, AudioFormat sampleFormat) {
		float[] output = new float[samples.length];
		for (int i = 0; i < samples.length; i++) {
			synchronized(syncObject) {
				output[i] = Math.max(-1.0f, Math.min(1.0f, samples[i]*vol));
			}
		}
			return output;
	}
	
	public void dispose() {
		
	}
	
	public void changeVol(float newVol) {
		synchronized(syncObject) {
			vol = newVol;
			slider.setValue((int)(newVol * 100));
		}
	}

	@Override
	public String toString() {
		return "VolumeProcessor";
	}

}
