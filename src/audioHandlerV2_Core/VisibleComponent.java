package audioHandlerV2_Core;

import javax.swing.JFrame;
import javax.swing.JPanel;

public abstract class VisibleComponent extends JFrame {

	protected JPanel panel = new JPanel();
	
	public VisibleComponent() {
		this.add(panel);
		setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
	}
	
	@Override
	public abstract String toString();
	
}
