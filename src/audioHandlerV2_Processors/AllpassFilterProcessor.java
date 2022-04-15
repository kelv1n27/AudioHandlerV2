package audioHandlerV2_Processors;

import java.util.ArrayList;

import javax.sound.sampled.AudioFormat;

public class AllpassFilterProcessor extends AudioProcessor{

	private ArrayList<Float> inMem = new ArrayList<Float>();
	private ArrayList<Float> outMem = new ArrayList<Float>();
	private float decay;
	private Object syncObject = new Object();
	
	public AllpassFilterProcessor(float decay) {
		this.decay = decay;
	}
	
	//out[n] = (-decay*in[n]) + in[n - delaySamples] + (decay * out[n-delaySamples])
	public float[] process(float[] samples, AudioFormat sampleFormat) {
		synchronized(syncObject) {
			//delay in samples
			int delaySamples = (int) (89.27f * (sampleFormat.getSampleRate()/1000f));
			//allocate enough space in memory;
			if (delaySamples + samples.length > inMem.size()) {
				inMem.ensureCapacity(delaySamples + samples.length);
				for(int i = inMem.size(); i < delaySamples + samples.length; i++)
					inMem.add(0f);
				for(int i = outMem.size(); i < delaySamples + samples.length; i++)
					outMem.add(0f);
			}
			//put input samples in memory, offset so t=0 is t=delaySamples; t=0 is delaySamples samples in the past
			for (int i = 0; i < samples.length; i++)
				inMem.set(delaySamples + i, samples[i]);
			//put processed samples into memory, offset so t=0 is t=delaySamples; t=0 is delaySamples samples in the past
			for (int i = 0; i < samples.length; i++)
				outMem.set(delaySamples + i, ((-1f * decay) * samples[i]) + inMem.remove(0) + (outMem.get(i) * decay));
			//put needed samples into array and return
			float[] output = new float[samples.length];
			for (int i = 0; i < output.length; i++)
				output[i] = outMem.remove(0);
			return output;
		}
	}
	
	public void dispose() {
		inMem.clear();
		inMem = null;
		outMem.clear();
		outMem = null;
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
