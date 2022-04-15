package audioHandlerV2_Processors;

import javax.sound.sampled.AudioFormat;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ReverbProcessor extends AudioProcessor{
	
	private float delay = 0;
	private float decay = 0;
	private float mix = 0;
	
	private CombFilterProcessor comb1;
	private CombFilterProcessor comb2;
	private CombFilterProcessor comb3;
	private CombFilterProcessor comb4;
	private AllpassFilterProcessor allpass1;
	private AllpassFilterProcessor allpass2;
	
	private JSlider delaySlider;
	private JSlider decaySlider;
	private JSlider mixSlider;
	
	private Object syncObject = new Object();
	
	public ReverbProcessor(float delay, float decay, float mix) {
		this.delay = delay;
		this.decay = decay;
		this.mix = Math.min(1, Math.max(-1, mix));
		
		comb1 = new CombFilterProcessor(delay, decay);
		comb2 = new CombFilterProcessor(delay - 11.73f, decay - 0.1313f);
		comb3 = new CombFilterProcessor(delay + 19.31f, decay - 0.2743f);
		comb4 = new CombFilterProcessor(delay - 7.97f, decay - 0.31f);
		allpass1 = new AllpassFilterProcessor(decay);
		allpass2 = new AllpassFilterProcessor(decay);
		
		setSize(500, 300);
		
		JPanel delayPanel = new JPanel();
		JLabel delayLabel = new JLabel("delay (ms)");
		delaySlider = new JSlider(JSlider.VERTICAL, 0, 1000, 100);
		delaySlider.setValue((int) delay);
		delaySlider.setMajorTickSpacing(100);
		delaySlider.setPaintTicks(true);
		delaySlider.setPaintLabels(true);
		delaySlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setDelay((float)delaySlider.getValue());
			}
		});
		delayPanel.add(delayLabel, "North");
		delayPanel.add(delaySlider, "South");
		panel.add(delayPanel, "East");
		
		JPanel decayPanel = new JPanel();
		JLabel decayLabel = new JLabel("decay (%)");
		decaySlider = new JSlider(JSlider.VERTICAL, 0, 100, 100);
		decaySlider.setValue((int)(decay*100));
		decaySlider.setMajorTickSpacing(10);
		decaySlider.setPaintTicks(true);
		decaySlider.setPaintLabels(true);
		decaySlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setDecay((float)decaySlider.getValue()/100);
			}
		});
		decayPanel.add(decayLabel, "North");
		decayPanel.add(decaySlider, "South");
		panel.add(decayPanel, "Center");
		
		JPanel mixPanel = new JPanel();
		JLabel mixLabel = new JLabel("mix (% wet)");
		mixSlider = new JSlider(JSlider.VERTICAL, 0, 100, 100);
		mixSlider.setValue((int)(mix * 100));
		mixSlider.setMajorTickSpacing(10);
		mixSlider.setPaintTicks(true);
		mixSlider.setPaintLabels(true);
		mixSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				setMix((float)mixSlider.getValue()/100);
			}
		});
		mixPanel.add(mixLabel, "North");
		mixPanel.add(mixSlider, "South");
		panel.add(mixPanel, "West");
		
		setTitle("Reverb Processor");
	}

	//     |->combFilter1>-|
	//     |->combFilter2>-|
	// in -|->combFilter3>-|->allpassFilter1->allpassFilter2->out
	//     |->combFilter4>-|
	@Override
	public float[] process(float[] samples, AudioFormat sampleFormat) {//kinda buggy and very audibly distorts when buffer size > 4 or samples array length > 2
		synchronized(syncObject) {
			//comb filters
			float[] comb1 = this.comb1.process(samples, sampleFormat);
			float[] comb2 = this.comb2.process(samples, sampleFormat);
			float[] comb3 = this.comb3.process(samples, sampleFormat);
			float[] comb4 = this.comb4.process(samples, sampleFormat);
			float[] decaySamples = new float[samples.length];
			for(int i = 0; i < decaySamples.length; i++)
				decaySamples[i] = comb1[i] + comb2[i] + comb3[i] + comb4[i];
			//mix feedback and source
			float[] mixSamples = new float[samples.length];
			for (int i = 0; i < samples.length; i++)
				mixSamples[i] = ((1f - mix) * samples[i]) + (mix * decaySamples[i]);
			//allpass filters
			mixSamples = allpass1.process(mixSamples, sampleFormat);
			//mixSamples = allpass1.process(mixSamples, sampleFormat);
			mixSamples = allpass2.process(mixSamples, sampleFormat);
			return mixSamples;
		}
	}
	
	public void dispose() {
		comb1.dispose();
		comb1 = null;
		comb2.dispose();
		comb2 = null;
		comb3.dispose();
		comb3 = null;
		comb4.dispose();
		comb4 = null;
		allpass1.dispose();
		allpass1 = null;
		allpass2.dispose();
		allpass2 = null;
	}
	
	public void setDelay(float delay) {
		synchronized(syncObject) {
			this.delay = delay;
			comb1.setDelay(delay);
			comb2.setDelay(delay - 11.73f);
			comb3.setDelay(delay + 19.31f);
			comb4.setDelay(delay - 7.97f);
			delaySlider.setValue((int) delay);
		}
	}
	
	public float getDelay() {
		synchronized(syncObject) {
			return delay;
		}
	}
	
	public void setDecay(float decay) {
		synchronized(syncObject) {
			this.decay = decay;
			comb1.setDecay(decay);
			comb2.setDecay(decay - 0.1313f);
			comb3.setDecay(decay - 0.2743f);
			comb4.setDecay(decay - 0.31f);
			decaySlider.setValue((int)(decay*100));
		}
	}
	
	public float getDecay() {
		synchronized(syncObject) {
			return decay;
		}
	}
	
	public void setMix(float mix) {
		synchronized(syncObject) {
			try {
				if (mix < 0 || mix > 1)
					throw new IllegalArgumentException("AUD ERROR: mix must be between 0 and 1");
				this.mix = mix;
				mixSlider.setValue((int)(mix * 100));
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
	}
	
	public float getMix() {
		synchronized(syncObject) {
			return mix;
		}
	}

	@Override
	public String toString() {
		return "Reverb";
	}

}