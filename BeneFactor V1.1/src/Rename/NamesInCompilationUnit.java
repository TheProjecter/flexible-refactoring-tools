package Rename;
import java.util.*;
import java.util.Map.Entry;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.dom.*;

import ASTree.ASTreeManipulationMethods;
import ASTree.NameBindingInformationVisitor;

public class NamesInCompilationUnit {
	
	CompilationUnit tree;
	NameBindingInformationVisitor visitor;
	Hashtable<String, ArrayList<Integer>> NameTable;
	
	// the compilation unit should be generated directly from ICompilationUnit
	public NamesInCompilationUnit(ICompilationUnit unit)
	{
		tree = ASTreeManipulationMethods.parseICompilationUnit(unit);
		visitor = new NameBindingInformationVisitor();
		tree.accept(visitor);
		NameTable = visitor.getEntireNameBindingTable();
	}
	
	public ArrayList<Integer> getNameIndicesOfBindingInCompilatioUnit(String bind)
	{
		if(bind == null || bind.equals(""))
			return new ArrayList<Integer>();
		ArrayList<Integer> res = NameTable.get(bind);
		if(res == null)
			return new ArrayList<Integer>(); 
		else
			return res;
	}
	
	public Hashtable<String, ArrayList<Integer>> getEntireBindingTable()
	{
		return NameTable;
	}
	
	public ArrayList<Integer> getNameIndicesInCompilationUnit()
	{
		ArrayList<Integer> names = new ArrayList<Integer>();
		Collection<ArrayList<Integer>> allArrays = NameTable.values();
		for(ArrayList<Integer> array: allArrays)
			names.addAll(array);
		return names;
	}
	

	public ArrayList<Name> getNamesWithBining(String binding)
	{
		ArrayList<Integer> indices = this.getNameIndicesInCompilationUnit();
		ArrayList<Name> names = new ArrayList<Name> ();
		for(int index : indices)
		{
			Name name = (Name)ASTreeManipulationMethods.getASTNodeByIndex(tree, index);
			names.add(name);
		}
		return names;
	}
	
	public boolean isBindingExisting(String binding)
	{
		return NameTable.keySet().contains(binding);
	}
}
