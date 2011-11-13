package Rename;

import java.util.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;

import ASTree.*;

public class NamesInJavaProject {
	
	IJavaProject project;
	ArrayList<NamesInCompilationUnit> UnitsNames;
	
	public NamesInJavaProject(IJavaProject pro)
	{
		try{
			project = pro;
			UnitsNames = new ArrayList<NamesInCompilationUnit>();
			ArrayList<ICompilationUnit> Units = ASTreeManipulationMethods.getICompilationUnitsOfAProject(project);	
			for(ICompilationUnit unit : Units)
				UnitsNames.add(new NamesInCompilationUnit(unit));
		} catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public ArrayList<Name> getNameWithBindingInProject(String bind)
	{
		ArrayList<Name> names = new ArrayList<Name>();
		for(NamesInCompilationUnit nu: UnitsNames)
			names.addAll(nu.getNamesWithBining(bind));
		return names;
	}
	
	public Name getANameWithBinding(String binding)
	{
		for(NamesInCompilationUnit nu: UnitsNames)
		{
			if(nu.isBindingExisting(binding))
				return nu.getNamesWithBining(binding).get(0);
		}
		return null;	
	}


}
