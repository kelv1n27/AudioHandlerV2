package audiohandlerV2_test;

import audioHandlerV2_Core.AudioHandler;
import audioHandlerV2_Core.AudioWorker;
import audioHandlerV2_Processors.ReverbProcessor;
import audioHandlerV2_Processors.SampleProcessor;
import audioHandlerV2_Processors.VolumeProcessor;

//This is not an automated unit test or regression test, just put what you want in here and see what happens
public class Test {

	public static void main(String[] args) {
		AudioHandler a = new AudioHandler(32);
		AudioWorker master = a.getMaster();
		AudioWorker blip = master.addWorker("beep");
		SampleProcessor sampler = new SampleProcessor("/clipped16bitWAVexample.wav");
		VolumeProcessor vol = new VolumeProcessor();
		vol.changeVol(.1f);
		sampler.setLooping(false);
		ReverbProcessor reverb = new ReverbProcessor(100f, .5f, .5f);
		blip.addProcessor(sampler);
		blip.addProcessor(vol);
		blip.addProcessor(reverb);
		reverb.setVisible(true);
		
		master.setVisible(true);
		
		a.start();
		while(true) {
			
			
		}
		//a.release();
	}
	
}
