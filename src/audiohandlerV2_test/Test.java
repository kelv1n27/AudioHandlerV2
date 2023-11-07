package audiohandlerV2_test;

import audioHandlerV2_Core.AudioHandler;
import audioHandlerV2_Core.AudioWorker;
import audioHandlerV2_Processors.ReverbProcessor;
import audioHandlerV2_Processors.SampleProcessor;

//This is not an automated unit test or regression test, just put what you want in here and see what happens
public class Test {

	public static void main(String[] args) {
		//AudioHandler a = new AudioHandler("/Stat up 1.wav", 2);
		//AudioHandler a = new AudioHandler(736);
		AudioHandler a = new AudioHandler(44100, 16, 2, true, false, 736);//736
		AudioWorker master = a.getMaster();
		master.setVisible(true);
		
		AudioWorker worker = master.addWorker("worker");
//		SynthProcessor otherSynth = new SynthProcessor();
//		otherSynth.changeFreq(180);
//		worker.addProcessor(otherSynth);
		
//		SynthProcessor synth = new SynthProcessor();
//		synth.changeFreq(100);
//		master.addProcessor(synth);
//		synth.setVisible(true);
		
		SampleProcessor sample = new SampleProcessor("/Stat up 1.wav");
		master.addProcessor(sample);
		
		ReverbProcessor reverb = new ReverbProcessor(50, .9f, 1f);
		master.addProcessor(reverb);
		
		a.start();
		try {
			Thread.sleep(1000000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		a.release();
		//a.release();
	}
	
}
