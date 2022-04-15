package audioHandlerV2_Processors;

import javax.sound.sampled.AudioFormat;

import audioHandlerV2_Core.VisibleComponent;

public abstract class AudioProcessor extends VisibleComponent{
	
	public abstract float[] process(float[] samples, AudioFormat sampleFormat); 
	
	public abstract void dispose();
	
}
