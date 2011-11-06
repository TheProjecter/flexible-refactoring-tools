package JavaRefactoringAPI;

import java.util.ArrayList;
import java.util.Stack;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.internal.corext.refactoring.rename.JavaRenameProcessor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;

import ASTree.CompilationUnitHistoryRecord;
import ASTree.CompilationUnitManipulationMethod;
import Rename.ASTNameChangeInformation;
import Rename.NamesInJavaProject;

import compare.SourceDiff;

public class JavaRefactoringRenameDiff extends JavaRefactoring {

	ASTNameChangeInformation first_dec_change;
	ASTNameChangeInformation last_dec_change;
	ArrayList<ASTNameChangeInformation> dec_changes;
	String bindingKey;
	IJavaProject project;
	String newName;
	String oldName;

	public JavaRefactoringRenameDiff(ICompilationUnit u, int l, IMarker m,
			ArrayList<ASTNameChangeInformation> changes, String nN) 
	{
		super(u, l, m);
		dec_changes = changes;
		first_dec_change = dec_changes.get(0);
		last_dec_change = dec_changes.get(dec_changes.size()-1);
		bindingKey = first_dec_change.getOldNameBindingKey();
		project = u.getJavaProject(); 
		newName = nN;
		oldName = first_dec_change.getOldName();
	}

	
	public String getOldName()
	{
		return this.oldName;
	}
	public String getNewName()
	{
		return this.newName;
	}
	
	@SuppressWarnings("restriction")
	@Override
	protected void performRefactoring(IProgressMonitor pm) throws Exception {
		
		SubMonitor monitor = SubMonitor.convert(pm,"Performing Rename Refactoring",6);
		RenameRefactoring refactoring;
		ArrayList<Name> names = new NamesInJavaProject(project).getNamesOfBindingInJavaProject(bindingKey);	
		ICompilationUnit unit = this.getICompilationUnit();
		if(!names.isEmpty())
		{
			unit.becomeWorkingCopy(monitor.newChild(1));
			IJavaElement element = names.get(0).resolveBinding().getJavaElement();
			
			//new way to get element
			IJavaElement entire_element = element;
			try{
				int name_start = names.get(0).getStartPosition();
				int name_length = names.get(0).getLength();
				IJavaElement[] primary_elements = unit.codeSelect(name_start, name_length);
				element = primary_elements[0];
			} catch (Exception e){
				element = entire_element;
			}
			//new way to get element
			
			JavaRenameProcessor processor = JavaRefactoringRename.getRenameProcessor(element);
			processor.setNewElementName(newName);
			refactoring = new RenameRefactoring(processor);
			refactoring.checkInitialConditions(monitor.newChild(1));
			refactoring.checkFinalConditions(monitor.newChild(1));
			Change change = refactoring.createChange(monitor.newChild(1));
			Change undo = change.perform(monitor.newChild(1));
			this.setUndo(undo);
			unit.commitWorkingCopy(true, monitor.newChild(1));
			unit.discardWorkingCopy();
		}

		monitor.done();
	}
	

	@Override
	protected void performCodeRecovery(IProgressMonitor pm) throws Exception {
		
		SubMonitor monitor = SubMonitor.convert(pm,"Recovering Code",2);
		
		if(first_dec_change == null)
			return;
			
		String source = first_dec_change.getOldCompilationUnitRecord().getSourceCode();
		ArrayList<CompilationUnitHistoryRecord> records = new ArrayList<CompilationUnitHistoryRecord>();
		CompilationUnitHistoryRecord startRecord = first_dec_change.getOldCompilationUnitRecord();
		CompilationUnitHistoryRecord endRecord = startRecord.getAllHistory().getMostRecentRecord();
		CompilationUnitHistoryRecord currentRecord = endRecord;

		
		while (currentRecord != startRecord && currentRecord != null) 
		{
			records.add(0, currentRecord);
			currentRecord = currentRecord.getPreviousRecord();
		}
		records.add(0, currentRecord);
		
		boolean[] does_skips = new boolean[records.size()];
		for(int i = 1; i< does_skips.length; i++)
			does_skips[i] = false;
		
		for(ASTNameChangeInformation change : dec_changes)
		{
			int start = records.indexOf(change.getOldCompilationUnitRecord());
			int end = records.indexOf(change.getNewCompilationUnitRecord());
			
			for(int i = start ; i<= end; i++)
				does_skips[i] = true;
		}
		
		for(int i = 1 ; i< records.size(); i++)
		{
			SourceDiff d = records.get(i).getSourceDiff();
			if(does_skips[i])
				source = d.skipChange(source);
			else 
				source = d.performChange(source);
		}
		
		monitor.worked(1);
	

		CompilationUnitManipulationMethod.UpdateICompilationUnit(this.getICompilationUnit(),source, monitor.newChild(1));

		monitor.done();
	}

	@Override
	public int getRefactoringType() {
		// TODO Auto-generated method stub
		return JavaRefactoringType.RENAME;
	}


	@Override
	public void postProcess() {
		// TODO Auto-generated method stub
		
	}

}