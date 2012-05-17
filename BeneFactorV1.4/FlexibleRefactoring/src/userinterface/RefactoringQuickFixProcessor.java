package userinterface;

import java.util.ArrayList;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickFixProcessor;

import util.ASTUtil;

import JavaRefactoringAPI.JavaRefactoring;
import JavaRefactoringAPI.JavaRefactoringMoveStaticMember;
import JavaRefactoringAPI.JavaRefactoringRename;
import JavaRefactoringAPI.JavaRefactoringRenameDiff;
import JavaRefactoringAPI.JavaRefactoringType;
import JavaRefactoringAPI.extractmethod.JavaRefactoringExtractMethodChange;

import compilation.RefactoringChances;

public class RefactoringQuickFixProcessor implements IQuickFixProcessor {

	@Override
	public boolean hasCorrections(ICompilationUnit unit, int problemId) {
		return true;
	}

	@Override
	public IJavaCompletionProposal[] getCorrections(IInvocationContext context,
			IProblemLocation[] locations) throws CoreException {
		try{
		ICompilationUnit unit = context.getCompilationUnit();
		CompilationUnit tree = ASTUtil.parseICompilationUnit(unit);
		boolean get_all = false;
		int selection = context.getSelectionOffset();
		int line = tree.getLineNumber(selection);
		if(get_all)
		{
			ArrayList<JavaRefactoring> refactorings = RefactoringChances.getJavaRefactorings(unit, line);
			int size = refactorings.size();
			IJavaCompletionProposal[] results = new IJavaCompletionProposal[size];
			for(int i = 0; i< size; i++)
				results[i] = getRefactoringProposalRefactoring(refactorings.get(i));	
			return results;
		}
		else
		{
			JavaRefactoring refactoring = RefactoringChances.getLatestJavaRefactoring(unit, line);
			IJavaCompletionProposal[] result = new IJavaCompletionProposal[1];
			result[0] = getRefactoringProposalRefactoring(refactoring);
			return result;
		}
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	

	
	
	public static RefactoringProposal getRefactoringProposalRefactoring(JavaRefactoring ref)
	{
		int type = ref.getRefactoringType();
		switch(type)
		{
		case JavaRefactoringType.RENAME:
			return new RefactoringProposalRename(ref);
		case JavaRefactoringType.EXTRACT_METHOD:
			return new RefactoringProposalExtractMethod(ref);
		case JavaRefactoringType.MOVE_STATIC:
			return new RefactoringProposalMoveStaticMember(ref);
		default:
			break;
		}
		try{
			throw new Exception("unknown refactoring type.");
		}catch(Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}

}
