package audioHandlerV2_Processors;

import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;

public class AllpassFilterProcessor extends AudioProcessor{

	private ArrayList<Float> inMem = new ArrayList<Float>();
	private ArrayList<Float> outMem = new ArrayList<Float>();
	private ArrayList<Float> mem = new ArrayList<Float>();
	private float decay;
	private Object syncObject = new Object();
	
	public AllpassFilterProcessor(float decay) {
		this.decay = decay;
	}
	
	/*
	 * 	   .---> -gain ---.
	 * 	   |			  |
	 *  ->-+---> delay --->-+->   
	 *       ^				|
	 *       `-- gain <-----'
	 */
	
	//out[n] = (-decay*in[n]) + in[n - delaySamples] + (decay * out[n-delaySamples])
	public float[] process(float[] samples, AudioFormat sampleFormat) {
		synchronized(syncObject) {
			//delay in samples
			int delaySamples = (int) (90f * (sampleFormat.getSampleRate()/1000f));
			//allocate enough space in memory;
			if (delaySamples + samples.length > mem.size()) {
				mem.ensureCapacity(delaySamples + samples.length);
				for(int i = mem.size(); i < delaySamples + samples.length; i++)
					mem.add(0f);
			}
			//put processed samples into memory, offset so t=0 is t=delaySamples; t=0 is delaySamples samples in the past
			for (int i = 0; i < samples.length; i++)
				mem.set(delaySamples + i,  ((-1f * decay) * samples[i]) + (mem.get(i) * decay)+ samples[i]) ;
			//put needed samples into array and return
			float[] output = new float[samples.length];
			for (int i = 0; i < output.length; i++)
				output[i] = ((-1f * decay) * samples[i]) + mem.remove(0);
			return output;
		}
	}
	
	public void dispose() {
		inMem.clear();
		inMem = null;
		outMem.clear();
		outMem = null;
		mem.clear();
		mem = null;
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
		return "AllpassFilter";
	}
	
}
