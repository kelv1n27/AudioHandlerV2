package audioHandlerV2_Processors;

import javax.sound.sampled.AudioFormat;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BitcrushProcessor extends AudioProcessor{
	
	private int bits = 16;
	private int sampleRate = 44100;
	private JSlider bitSlider = new JSlider(1, 16);
	private JSlider sampleSlider = new JSlider(1, 44100);

	public BitcrushProcessor() {
		setTitle("Bitcrush Processor");
		JPanel panel = new JPanel();
		panel.add(bitSlider);
		bitSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setBits(bitSlider.getValue());
			}
		});
		panel.add(sampleSlider);
		sampleSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setSampleRate(sampleSlider.getValue());
			}
		});
		add(panel);
		pack();
	}
	
	@Override
	public float[] process(float[] samples, AudioFormat sampleFormat) {
		//prevent settings from going above what is possible
		bitSlider.setMinimum(1);
		bitSlider.setMaximum(sampleFormat.getSampleSizeInBits());
		bitSlider.setValue(Math.min(sampleFormat.getSampleSizeInBits(), bits));
		sampleSlider.setMinimum(1);
		sampleSlider.setMaximum((int)sampleFormat.getSampleRate());
		sampleSlider.setValue(Math.min((int)sampleFormat.getSampleRate(), sampleRate));
		//actual processing
		float sampleRatio = sampleFormat.getSampleRate()/sampleRate;
		
		float[] output = new float[samples.length];
		int i = 0;
		float sample = sampleRatio;
		while (i < samples.length) {
			if (i >= sample + sampleRatio) sample += sampleRatio;
			int datBits = (int)(samples[(int) sample] * (0b1 << sampleFormat.getSampleSizeInBits()));
			datBits = datBits >> (sampleFormat.getSampleSizeInBits() - bits);
			datBits = datBits << (sampleFormat.getSampleSizeInBits() - bits);
			output[i] = ((float)datBits/(0b1 << sampleFormat.getSampleSizeInBits()));
//			output[i] = Float.intBitsToFloat((Float.floatToRawIntBits(samples[(int) sample]) >> (sampleFormat.getSampleSizeInBits() - bits)) << (sampleFormat.getSampleSizeInBits() - bits));
			i++;
		}//for (i = 0; i < samples.length; i++) System.out.println(output[i] + " " + bits);
		return output;
	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String toString() {
		return "Bitcrush Processor";
	}
	
	public int getBits() {
		return bits;
	}
	
	public void setBits(int bits) {
		this.bits = bits;
	}
	
	public int getSampleRate() {
		return sampleRate;
	}
	
	public void setSampleRate(int sampleRate) {
		this.sampleRate = sampleRate;
	}

}
