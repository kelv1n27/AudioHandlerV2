package audioHandlerV2_Processors;

import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

import javax.sound.sampled.AudioFormat;

public class VolMonitorProcessor extends AudioProcessor{
	
private Canvas canvas = new Canvas();
private int yResolution = 10;

	public VolMonitorProcessor() {
		setLayout(new BorderLayout());
		add(canvas, BorderLayout.CENTER);
		pack();
		canvas.setVisible(true);
		setTitle("Volume Monitor");
	}
	
	public VolMonitorProcessor(int yResolution) {
		setLayout(new BorderLayout());
		add(canvas, BorderLayout.CENTER);
		pack();
		canvas.setVisible(true);
		setTitle("Volume Monitor");
		this.yResolution = yResolution;
	}

	@Override
	public float[] process(float[] samples, AudioFormat sampleFormat) {
		if (isVisible()) {
			canvas.setSize(samples.length/sampleFormat.getChannels(), 100 * sampleFormat.getChannels());
			pack();
			BufferedImage image = new BufferedImage(samples.length/sampleFormat.getChannels(), yResolution * sampleFormat.getChannels(), BufferedImage.TYPE_INT_RGB);
			if (sampleFormat.getChannels() == 1) {
				for (int x = 0; x < samples.length; x++) {
					if (samples[x] <= 1) {
						for (int y = 0; y < (int)(samples[x]*yResolution); y++) {
							int color = 
									(((int)(255*samples[x])) << 16) + 
									(((int)(255*(1-samples[x]))) << 8);
							image.setRGB(x, (yResolution - 1) - y, color);
						}
					} else {
						for (int y = 0; y < yResolution; y++) image.setRGB(x, y, 0x0000ff);
					}
				}
			}
			else if (sampleFormat.getChannels() == 2) {
				for (int x = 0; x < samples.length; x++) {
					if (samples[x] <= 1) {
						for (int y = 0; y < (int)(samples[x]*yResolution); y++) {
							int color = 
									(((int)(255*samples[x])) << 16) + 
									(((int)(255*(1-samples[x]))) << 8);
							image.setRGB(x>>1, (x%2==0?(yResolution - 1):(2 * yResolution - 1)) - y, color);
						}
					} else {
						for (int y = 0; y < yResolution; y++) image.setRGB(x>>1, (x%2==0?(yResolution - 1):(2 * yResolution - 1)) - y, 0x0000ff);
					}
				}
			}
			for (int i = 0; i < samples.length/sampleFormat.getChannels(); i++) {
				image.setRGB(i, yResolution - 1, 0xffffff);
			}
			BufferStrategy bs = canvas.getBufferStrategy();
			if (bs == null) {
				canvas.createBufferStrategy(3);
				bs = canvas.getBufferStrategy();
			}
			Graphics g = bs.getDrawGraphics();
			g.drawImage(image, 0, 0, image.getWidth(), canvas.getHeight(), null);
			bs.show();
			g.dispose();
		}
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
