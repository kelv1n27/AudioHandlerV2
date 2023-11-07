package audioHandlerV2_Processors;

import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;

public class CombFilterProcessor extends AudioProcessor{
	
	private ArrayList<Float> mem = new ArrayList<Float>();
	private float delay;
	private float decay;
	private Object syncObject = new Object();
	
	public CombFilterProcessor(float delay, float decay) {
		this.delay = delay;
		this.decay = decay;
	}
	
	/*
	 *  ->-----> delay --->-+->   
	 *       ^				|
	 *       `-- gain <-----'
	 */
	
	//out[n] = in[n] + (decay * out[n - delaySamples])
	public float[] process(float[] samples, AudioFormat sampleFormat){
		synchronized(syncObject){
			//get delay measured in samples
			int delaySamples = (int) (delay * (sampleFormat.getSampleRate()/1000f));
			//allocate enough space in memory
			if (delaySamples + samples.length > mem.size()) {
				mem.ensureCapacity(delaySamples + samples.length);
				for(int i = mem.size(); i < delaySamples + samples.length; i++)
					mem.add(0f);
			}
			//put processed samples into memory, offset so t=0 is t=delaySamples; t=0 is delaySamples samples in the past
			try {
			for (int i = 0; i < samples.length; i++)
				mem.set(delaySamples + i, samples[i] + (mem.get(i) * decay));
			} catch (Exception e) {System.out.println(delay + " " + delaySamples); }
			//put needed samples into array and return
			float[] output = new float[samples.length];
			for (int i = 0; i < output.length; i++)
				output[i] = mem.remove(0);
			return output;
		}
	}
	
	public void dispose() {
		mem.clear();
		mem = null;
	}
	
	public void setDelay(float delay) {
		synchronized(syncObject) {
			this.delay = Math.max(delay, 0);
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
		}
	}
	
	public float getDecay() {
		synchronized(syncObject) {
			return decay;
		}
	}

	@Override
	public String toString() {
		return "CombFilter";
	}

}
