package audioHandlerV2_Processors;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.sound.sampled.AudioFormat;

public class VolMonitorProcessor extends AudioProcessor{
	
private Canvas canvas = new Canvas();

	public VolMonitorProcessor() {
		setLayout(new BorderLayout());
		add(canvas, BorderLayout.CENTER);
		pack();
		canvas.setVisible(true);
		setTitle("Volume Monitor");
	}

	@Override
	public float[] process(float[] samples, AudioFormat sampleFormat) {
//		System.out.println(sampleFormat.getChannels());
		try {
		if (isVisible()) {
			canvas.setSize(samples.length/sampleFormat.getChannels(), 100 * sampleFormat.getChannels());
			pack();
			BufferedImage image = new BufferedImage(samples.length/sampleFormat.getChannels(), 100 * sampleFormat.getChannels(), BufferedImage.TYPE_INT_RGB);
			if (sampleFormat.getChannels() == 1) {
				for (int x = 0; x < samples.length; x++) {
					for (int y = 0; y < (int)(samples[x]*100); y++) {
						int color = ((int)(255*samples[x])) << 16 + ((int)(255*(1-samples[x]))) << 8;
						System.out.println((x>>1) + "  " + (100 - y));
						image.setRGB(x, 99 - y, color);
					}
				}
			}
			else if (sampleFormat.getChannels() == 2) {
				for (int x = 0; x < samples.length; x++) {
					for (int y = 0; y < (int)(samples[x]*100); y++) {
						int color = (((int)(255*samples[x])) << 16) + ((int)(255*(1-samples[x]))) << 8;
//						System.out.println((x>>1) + "  " + ((x%2==0?100:200) - y));
						image.setRGB(x>>1, (x%2==0?99:199) - y, color);
					}
				}
			}
			for (int i = 0; i < samples.length/sampleFormat.getChannels(); i++) {
				image.setRGB(i, 100, 0xffffff);
			}
			BufferStrategy bs = canvas.getBufferStrategy();
			if (bs == null) {
				canvas.createBufferStrategy(3);
				bs = canvas.getBufferStrategy();
			}
			Graphics g = bs.getDrawGraphics();
			g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
			bs.show();
			g.dispose();
		}
		}catch(Exception e) {e.printStackTrace();}
		return samples;
	}

	@Override
	public void dispose() {
		
	}

	@Override
	public String toString() {
		return "Volume Monitor";
	}

}
