package audiohandlerV2_test;

import audioHandlerV2_Core.AudioHandler;
import audioHandlerV2_Core.AudioWorker;
import audioHandlerV2_Processors.SynthProcessor;

//This is not an automated unit test or regression test, just put what you want in here and see what happens
public class Test {

	public static void main(String[] args) {
		//AudioHandler a = new AudioHandler("/Stat up 1.wav", 2);
		//AudioHandler a = new AudioHandler(736);
		AudioHandler a = new AudioHandler(44100, 16, 2, true, false, 736);
		AudioWorker master = a.getMaster();
		//AudioWorker blip = master.addWorker("beep");
		//SampleProcessor sampler = new SampleProcessor("/clipped16bitWAVexample.wav");
		//VolumeProcessor vol = new VolumeProcessor();
		//vol.changeVol(1f);
		//sampler.setLooping(false);
		//ReverbProcessor reverb = new ReverbProcessor(100f, .5f, .5f);
		//blip.addProcessor(sampler);
		//blip.addProcessor(vol);
		//blip.addProcessor(reverb);
		//reverb.setVisible(true);
		//sampler.setVisible(true);
		//master.setVisible(true);
		SynthProcessor synth = new SynthProcessor();
		master.addProcessor(synth);
		synth.setVisible(true);
		
		a.start();
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		a.release();
		//a.release();
	}
	
}
