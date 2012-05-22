package Rename;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import JavaRefactoringAPI.rename.JavaRefactoringRename;
import JavaRefactoringAPI.rename.JavaRefactoringRenameDiff;

import userinterface.RefactoringMarker;
import util.ASTUtil;
import ASTree.*;
import JavaRefactoringAPI.*;

public class ASTNameChangeInformation extends ASTChangeInformation {
	
	final private String originalName;
	final private String originalNameFull;
	final private String modifiedName;
	final private String modifiedNameFull;
	
	final private String bindingKeyOne;
	final private String bindingKeyTwo;
		
	final private boolean isDeclarationChange;
	
	public ASTNameChangeInformation(CompilationUnitHistoryRecord oldRecord ,ASTNode r1, CompilationUnitHistoryRecord newRecord ,ASTNode r2) throws Exception {
		
		super(oldRecord,r1,newRecord,r2);
		
		Name oldName = (Name) r1;
		Name newName = (Name) r2;
		
		bindingKeyOne = oldRecord.getBindingKey(getNodeOneIndex());
		bindingKeyTwo = newRecord.getBindingKey(getNodeTwoIndex());
		
		originalName = oldName.toString();
		originalNameFull = oldName.getFullyQualifiedName();
		modifiedName = newName.toString();
		modifiedNameFull = newName.getFullyQualifiedName();
		
		
		
		if(oldName.isSimpleName())
		{
			SimpleName sOldName = (SimpleName) oldName; 
			isDeclarationChange = sOldName.isDeclaration();
		}
		else 
			isDeclarationChange = false;
	}
	


	public int getOldNameBindingCount()
	{
		//return originalNameBindingCount;
		return 0;
	}
	


	public String getOldNameBindingKey()
	{
		return bindingKeyOne;
	}
	
	public String getNewNameBindingKey()
	{
		return bindingKeyTwo;
	}
	
	public JavaRefactoring getRenameRefactoring(ICompilationUnit unit) throws Exception
	{
		int line = getRefactoringMarkerLine(unit);
		IMarker marker;
		
		if(bindingKeyOne.equals("") && bindingKeyTwo.equals(""))
			return null;
		else if(bindingKeyOne.equals("") && !bindingKeyTwo.equals(""))
		{
			//renaming reference when declaration has been changed
			marker = RefactoringMarker.addRefactoringMarkerIfNo(unit, line);
			List<ASTNameChangeInformation> declarationChanges = 
					NameChangeDetected.getInstance().getSkipedDeclaredNameChangesInHistory(bindingKeyTwo);
			JavaRefactoringRenameDiff refactoringDiff = JavaRefactoringRenameDiff.create(
					unit, line, marker, declarationChanges, modifiedName);
			return refactoringDiff;
		}
		else
		{
			marker = RefactoringMarker.addRefactoringMarkerIfNo(unit, line);
			if(isRenamingDeclaration())
			{
				List<ASTNameChangeInformation> changes = NameChangeDetected.getInstance().
						getSkipedDeclaredNameChangesInHistory(bindingKeyTwo);			
				changes.add(this);
				JavaRefactoringRenameDiff refactoringDiff = JavaRefactoringRenameDiff.create(
					unit, line, marker, changes, modifiedName);					
				return refactoringDiff;
			}
			else
			{
				JavaRefactoringRename refactoring = new JavaRefactoringRename(
					unit, line, marker, bindingKeyOne, bindingKeyOne, originalName, modifiedName);
				return refactoring;
			}
		}
	
	}
	


	
	public boolean isRenamingDeclaration()
	{
		return isDeclarationChange;
	}
	
	public String getOldName()
	{
		return originalName;
	}
	
	public String getOldNameFull()
	{
		return originalNameFull;
	}
	
	public String getNewName()
	{
		return modifiedName;
	}
	
	public String getNewNameFull()
	{
		return modifiedNameFull;
	}
	
	public int getRefactoringMarkerLine(ICompilationUnit unit) throws Exception
	{
		CompilationUnit tree = ASTUtil.parseICompilationUnit(unit);
		ASTNode oldNameNode = ASTUtil.getASTNodeByIndex(tree, getNodeOneIndex());
		int lineNo = tree.getLineNumber(oldNameNode.getStartPosition());
		return lineNo;
	}
	

	

}
