package audioHandlerV2_Core;
import java.io.BufferedInputStream;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Control;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;

public class AudioHandler extends Thread{
	
	private AudioFormat format;
	private SourceDataLine masterOut;
	private Control[] masterControls;
	private AudioWorker master;
	private int floatBufferSize;
	private boolean running = true;
	private Object syncObject = new Object();
	
	public AudioHandler(int floatBufferSize){
		try {
			AudioInputStream a = AudioSystem.getAudioInputStream(new BufferedInputStream(getClass().getResourceAsStream("/16bitWAVexample.wav")));
			format = a.getFormat();
			System.out.println(format);
			DataLine.Info i = new DataLine.Info(SourceDataLine.class, format);
			masterOut = (SourceDataLine) AudioSystem.getLine(i);
			masterControls = masterOut.getControls();
			masterOut.open();
			masterOut.start();
			this.floatBufferSize = floatBufferSize;
			master = new AudioWorker(floatBufferSize, format, "Master");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public AudioHandler(String formatExamplePath, int floatBufferSize) {
		try {
			AudioInputStream a = AudioSystem.getAudioInputStream(new BufferedInputStream(getClass().getResourceAsStream(formatExamplePath)));
			format = a.getFormat();
			System.out.println(format);
			DataLine.Info i = new DataLine.Info(SourceDataLine.class, format);
			masterOut = (SourceDataLine) AudioSystem.getLine(i);
			masterControls = masterOut.getControls();
			masterOut.open();
			masterOut.start();
			this.floatBufferSize = floatBufferSize;
			master = new AudioWorker(floatBufferSize, format, "Master");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		while (running) {
			float[] in;
			synchronized(syncObject) {
				in = master.process();
			}
			byte[] out = encodeBytes(in, format);
			masterOut.write(out, 0, out.length);
		}
	}
	
	public AudioWorker getMaster() {
		return master;
	}
	
	public AudioFormat getFormat() {
		return format;
	}
	
	public int getFloatBufferSize() {
		return floatBufferSize;
	}
	
	public void setFloatBufferSize(int newBufferSize) {
		synchronized(syncObject) {
			try {
				if (newBufferSize < 1)
					throw new IllegalArgumentException("AUD ERROR: Illegal float buffer size, float buffer must be >= 1");
				floatBufferSize = newBufferSize;
				master.setBufferSize(newBufferSize);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	public void release() {
		running = false;
		masterOut.drain();
		masterOut.close();
		master.dispose();
	}
	
	private byte[] encodeBytes(float[] samples, AudioFormat format) {
		int bytesPerSample = format.getSampleSizeInBits()/8;
		byte[] bytes = new byte[samples.length*bytesPerSample];
		for (int i = 0; i < samples.length; i++) {
			long temp = 0;
			if (format.getEncoding()==Encoding.PCM_SIGNED) {
				temp = (long) (samples[i] * Math.pow(2.0, format.getSampleSizeInBits() - 1));
			} else if (format.getEncoding() == Encoding.PCM_UNSIGNED) {
				temp = (long) (samples[i] * Math.pow(2.0, format.getSampleSizeInBits() - 1));
				temp += Math.pow(2.0, format.getSampleSizeInBits() - 1);
			}
			bytes[i*bytesPerSample] = (byte) (temp & 0xff);
			if(bytesPerSample == 2)
				bytes[i*bytesPerSample + 1] = (byte) ((temp >> 8) & 0xff);
		}
		
		return bytes;
	}
}
