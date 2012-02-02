package animation;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import utitilies.FileUtilities;

public class MovableShell extends Thread
{
	
	public void setX(int x) {
		X = x;
		resetShell();
	}


	public void setY(int y) {
		Y = y;
		resetShell();
	}


	public void setWidth(int w) {
		this.width = w;
		resetShell();
	}


	public void setHeight(int h) {
		this.height = h;
		resetShell();
	}


	
	public int getX() {
		return X;
	}


	public int getY() {
		return Y;
	}


	public int getWidth() {
		return width;
	}


	public int getHeight() {
		return height;
	}

	int X;
	int Y;
	int width;
	int height;
	
	final String path;
	Shell shell;
	Display display;
	
	
	MovableShell(int x, int y, int w, int h, String p)
	{
		super();
		X = x;
		Y = y;
		width = w;
		height = h;
		path = p;	
		
		this.start();
	}
	
	public void finalize()
	{
		try {
			super.finalize();
			FileUtilities.delete(path);
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}
	
	
	public void run ()
	{
		display = new Display();
		Image image = new Image( display, path);
		shell = new Shell(display, SWT.NO_TRIM | SWT.ON_TOP);
	
		shell.setBackgroundImage(image);
		shell.setBounds(X, Y, width, height);
		shell.open ();
		
		
		 while (!shell.isDisposed()) 
		 {
			 if (!display.readAndDispatch ()) 
				 display.sleep ();
		 }
		 display.dispose();
	}
	
	private synchronized void resetShell()
	{
		display.syncExec(new Runnable()
		{
			public void run() {
				shell.setBounds(X, Y, width, height);
			}	
		});
	}
	
	
}