package compilation;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.CompilationUnit;

import util.ASTUtil;

import ASTree.ProjectHistoryCollector;

public class JobQueue implements Runnable{
	
	private JobQueue() {}

	private ProjectHistoryCollector collector = new ProjectHistoryCollector();
	private List<ReconcileContext> contexts = new ArrayList<ReconcileContext>();
	private static int SLEEP_TIME = 100;
	private static JobQueue queue;
	
	static public JobQueue getInstance()
	{
		if(queue == null)
		{
			queue = new JobQueue();
			new Thread(queue).start();
		}
		return queue;
	}
	
	public synchronized void enqueue(ReconcileContext con)
	{
		contexts.add(con);
	}

	@Override
	public void run() 
	{
		try {
			for(;;)
			{
				if(contexts.size() > 0)
				{
					handle(contexts.get(0));	
					contexts.remove(0);
				}
				else
					Thread.sleep(SLEEP_TIME);
			}
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private void handle(ReconcileContext context) throws Exception
	{
		IJavaProject pro = context.getWorkingCopy().getJavaProject();
		collector.addNewProjectVersion(pro, context.getWorkingCopy());	
	}
	

	
	

}
