package sudoku;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * Place for your code.
 */
public class SudokuSolver {
    private static final int BLOCK_WIDTH = 3;
    private static final int N = 9;

	/**
	 * @return names of the authors and their student IDs (1 per line).
	 */
	public String authors() {
		// TODO write it;
		return "Mitchell Tee (84148014 / c738)\nOlivia Zhang (44406106 / m4s7)"; // TODO: Fill out student numbers
	}

	/**
	 * Performs constraint satisfaction on the given Sudoku board using Arc Consistency and Domain Splitting.
	 * 
	 * @param board the 2d int array representing the Sudoku board. Zeros indicate unfilled cells.
	 * @return the solved Sudoku board
	 */
	public int[][] solve(int[][] board) throws InvalidSudokuBoardException {
		SudokuProblem sp = sudokuProblemWithBoard(board);
		return sp.solve();
	}

    /**
     * Converts varaibles into a board
     * @param v
     * @return
     */
    public static int[][] variablesToBoard(Variable[][] v) {
        int[][] board = new int[N][];
        for (int i = 0; i < N; i++) {
            board[i] = new int[N];
            for (int j = 0; j < N; j++) {
                board[i][j] = v[i][j].getValue();
            }
        }
        return board;
    
    }
    /**
     * Solves a sudoku problem. If the solution is infeasible, returns null
     * @param sp
     * @return
     */
    private static SudokuProblem solve(SudokuProblem sp)
    {
        sp.makeConsistent();
        if (sp.isSolved())
        {
            return sp;
        }
        if (sp.isInfeasible())
        {
            return null;
        }
        SudokuProblem child = sp.split();
        sp.makeConsistent();
        SudokuProblem sp2 = solve(sp);
        if (sp2 != null)
        {
            return sp2;
        }
        else
        {
            child.makeConsistent();
            return solve(child);
        }
        
    }
	/**
     * Creates a new sudoku problem with the given board
     * A 0 should be used to denote an empty space
     * @param board
     * @return
     * @pre  board must be a standard sized 9x9 sudoku board
     */
    public static SudokuProblem sudokuProblemWithBoard(int[][] board)
    {
        SudokuSolver ss = new SudokuSolver();
        SudokuProblem sp = ss.new SudokuProblem();
        // Initialize all the variables
        for (int i = 0; i < N; i++)
        {
            for (int j = 0; j < N; j++)
            {
                sp.getVariables()[i][j] = ss.new Variable(i, j, board[i][j]);
            }
        }
        
        makeConnections(sp);
        
        return sp;
    }
    /**
     * Creates a new sudoku problem based on variables.  Does not change
     * the variables that are passed in.
     * This is meant to be used for domain splitting.
     * @param vars
     * @return
     */
    public static SudokuProblem sudokuProblemWithVariables(Variable[][] vars)
    {
        SudokuSolver ss = new SudokuSolver();
        SudokuProblem sp = ss.new SudokuProblem();
        for (int i = 0; i < N; i++)
        {
            for (int j = 0; j < N; j++)
            {
                Variable var = vars[i][j];
                assert(var.getRow() == i);
                assert(var.getCol() == j);
                sp.variables[i][j] = ss.new Variable(i, j, var.getValue());
                Set<Integer> domainClone = new java.util.HashSet<Integer>();
                for (Integer elem : var.getDomain())
                {
                    domainClone.add(new Integer(elem));
                }
                sp.variables[i][j].setDomain(domainClone);
                if (domainClone.size() == 1)
                {
                    sp.variables[i][j].setValue(domainClone.iterator().next());
                }
            }
        }
        makeConnections(sp);
        return sp;
    }


    /**
     * Takes a sudoku problem full of unconnected variables, 
     * and creates constraints and arcs for them
     * and connects everything.
     * @param sp
     */
    private static void makeConnections(SudokuProblem sp)
    {
        SudokuSolver ss = new SudokuSolver();
        // make the constraints for each variable and connect the arcs to constraints and variables
        int arcId = 0;
        for (int i = 0; i < N; i++)
        {
            for (int j = 0; j < N; j++)
            {
                // make each row constraint
                for (int jj = j+1; jj < N; jj++)
                {
                    Constraint c = ss.new Constraint();
                    Arc a = ss.new Arc(sp.getVariables()[i][j], c, arcId++);
                    sp.getVariables()[i][j].addArc(a);
                    c.setArc1(a);
                    sp.tda.add(a);
                    a = ss.new Arc(sp.getVariables()[i][jj], c, arcId++);
                    sp.getVariables()[i][jj].addArc(a);
                    c.setArc2(a);
                    sp.tda.add(a);
                }
                
                // make each col constraint
                for (int ii = i+1; ii < N; ii++)
                {
                    Constraint c = ss.new Constraint();
                    Arc a = ss.new Arc(sp.getVariables()[i][j], c, arcId++);
                    sp.getVariables()[i][j].addArc(a);
                    c.setArc1(a);
                    sp.tda.add(a);
                    a = ss.new Arc(sp.getVariables()[ii][j], c, arcId++);
                    sp.getVariables()[ii][j].addArc(a);
                    c.setArc2(a);
                    sp.tda.add(a);
                }
                // make each block constraint
                
                for (int _i = 0; _i < BLOCK_WIDTH - (i % BLOCK_WIDTH); _i++)
                {
                    int ii = _i + i;
                    for (int _j = 0; _j < BLOCK_WIDTH; _j++)
                    {
                        int jj = _j +BLOCK_WIDTH*( j/BLOCK_WIDTH);
                        if (!(i == ii || j == jj) && blockNumber(i,j) < blockNumber(ii, jj)) // we already have these constraints made from before
                        {
                            Constraint c = ss.new Constraint();
                            Arc a;
                            a = ss.new Arc(sp.getVariables()[i][j], c, arcId++);
                            sp.getVariables()[i][j].addArc(a);
                            c.setArc1(a);
                            sp.tda.add(a);
                            a = ss.new Arc(sp.getVariables()[ii][jj], c, arcId++);
                            sp.getVariables()[ii][jj].addArc(a);
                            c.setArc2(a);
                            sp.tda.add(a);
                        }
                    }
                }
            }
        }
    }


    /**
     * blockNumber is the position within its block that
     * the variable is in.  For example, variables[0][0]
     * would have blockNumber 0.  variables[2][2] would have
     * blockNumber 8.
     * 
     * eg   0 1 2   3 4 5   6 7 8
     * 
     *  0   0 1 2   0 1 2   0 1 2
     *  1   3 4 5   3 4 5   3 4 5
     *  2   6 7 8   6 7 8   6 7 8
     *  
     *  3   0 1 2   0 1 2   0 1 2
     *  4   3 4 5   3 4 5   3 4 5
     *  5   6 7 8   6 7 8   6 7 8
     *  
     *  6   0 1 2   0 1 2   0 1 2
     *  7   3 4 5   3 4 5   3 4 5
     *  8   6 7 8   6 7 8   6 7 8
     */
    private static int blockNumber(int i, int j)
    {
        return BLOCK_WIDTH * (i % BLOCK_WIDTH) + j % BLOCK_WIDTH;
    }
	
	class SudokuProblem
	{
	    private Variable[][] variables;
	    /**
	     * To do arc list: arcs that might be
	     * inconsistent
	     */
	    private Set<Arc> tda;
	    

	    private final static int N = 9; // size of a sudoku board
	    private final static int BLOCK_WIDTH = 3;
	    
	    
	    
	    public SudokuProblem()
	    {
	        variables = new Variable[N][];
	        for (int i = 0; i < N; i++)
	        {
	            variables[i] = new Variable[N];
	        }
	        tda = new java.util.HashSet<Arc>();
	    }
	    
	    
	    
	    


	    /**
	     * Checks every element in the arc's variable's domain
	     * and eliminates any that are inconsistent.
	     * If any are eliminated, it adds the proper arcs
	     * back into the arc to do list.
	     * @param arc
	     */
	    private void checkArc(Arc arc) {
	        Constraint con = arc.getConstraint();
	        if (con.pruneDomain(arc))
	        {
	            Variable var = arc.getVariable();
	            List<Arc> arcsToAddBack = var.getArcsToAddBackToTDA(arc);
	            tda.addAll(arcsToAddBack);
	        }
	    }
	    
	    /**
	     * Adds all arcs that are separated from this variable by one arc
	     * @param var
	     */
	    private void addArcsToTDAAfterSplit(Variable var) {
	        assert(tda.isEmpty());
	        
	        for (Arc a : var.getArcs())
	        {
	            Constraint c = a.getConstraint();
	            if (c.getArc1() == a)
	            {
	                tda.add(c.getArc2());
	            }
	            else
	            {
	                tda.add(c.getArc1());
	            }
	        }
	        
	        
	    }
	    
	    /**
	     * Goes through every arc on the TDA and checks it.
	     */
	    public void makeConsistent()
	    {
	        while (!tda.isEmpty())
	        {
	            Iterator<Arc> it = tda.iterator();
	            Arc a = it.next();
	            tda.remove(a);
	            checkArc(a);
	        }
	    }

	    /**
	     * splits the domain into 2 sub problems, each contains half the domain of the original
	     * domain. 
	     * @return
	     */
	    public Variable domainSplit(Variable var){
	        Set<Integer> current_domain = var.getDomain();
	        Set<Integer> splited_domain1 = new HashSet<Integer>();
	        Iterator<Integer> it = current_domain.iterator();
	        for (int i = 0; i< current_domain.size()/2; i++){
	            int element = it.next();
	            splited_domain1.add(element);
	        }

	        current_domain.removeAll(splited_domain1);
	        
	        
	        var.setDomain(splited_domain1);
	        if (splited_domain1.size() == 1)
	        {
	            Iterator<Integer> i = splited_domain1.iterator();
	            var.setValue(i.next());
	        }
	        
	        Variable otherSubProblem = new Variable(var.getRow(),var.getCol(),var.getValue());
	        otherSubProblem.setDomain(current_domain);
	        if (current_domain.size() ==1)
	        {
	            Iterator<Integer> i = current_domain.iterator();
	            otherSubProblem.setValue(i.next());
	        }
	        
	        return otherSubProblem;
	        }
	    
	    public Set<Arc> getTda()
	    {
	        return tda;
	    }


	    public int[][] solve() throws InvalidSudokuBoardException {
	        makeConsistent();
	        SudokuProblem solution = SudokuSolver.solve(this);
	        if (null == solution)
	        {
	            throw new InvalidSudokuBoardException("No solution");
	        }
	        for (int i = 0; i < N; i++)
	        {
	            for (int j = 0; j < N; j++)
	            {
	                Iterator<Integer> it = solution.variables[i][j].getDomain().iterator();
	                int val = it.next();
	                solution.variables[i][j].setValue(val); // TODO: i think we can get rid of this
	            }
	        }
	        return variablesToBoard(solution.variables);
	    }
	    
	    public SudokuProblem split()
	    {
	        return splitOn(chooseVariableToSplit());
	    }


	    public Variable chooseVariableToSplit()
	    {
	        Variable v = null;
	        for (int i = 0; i < N; i++)
	        {
	            for (int j = 0; j < N; j++)
	            {
	                if (variables[i][j].getDomain().size() > 1)
	                {
	                    return variables[i][j];
	                }
	            }
	        }
	        assert(v != null);
	        return v;
	    }


	    /**
	     * Splits the entire sudoku problem.  The variable passed in's domain
	     * is split in half.  The original sudoku problem is mutated to have
	     * a variable with half of that domain, and a new child problem is
	     * created with a variable whose domain is the other half.
	     * Please ensure that the variable has more than one element in its
	     * domain before trying to split.
	     * @param var is a reference to an actual variable of this sudoku problem.
	     * @return
	     * @Pre arc consistency has been run
	     * @Pre the domain has more than one element
	     */
	    public SudokuProblem splitOn(Variable var)
	    {
	        assert(var.getDomain().size() > 1);
	        assert(tda.isEmpty());
	        
	        // assumes an implementation of domainSplit that will return a variable
	        Variable childVar = domainSplit(var);
	        Set<Integer> thisDomain = var.getDomain();
	        Set<Integer> childDomain = childVar.getDomain();
	        Set<Integer> tempDomain = null;
	        
	        // deal with child problem
	        SudokuProblem childSp = null;
	        tempDomain = thisDomain;
	        var.setDomain(childDomain);
	        if (childDomain.size() == 1)
	        {
	            var.setValue(childDomain.iterator().next());
	        }
	        childSp = sudokuProblemWithVariables(variables);
	        childSp.tda.clear();
	        Arc a = var.getArcs().get(0); // don't forget to add this to tda later
	        int aId = a.getId();
	        int row = var.getRow();
	        int col = var.getCol();
	        Arc childArc = null;
	        for (Arc arc : childSp.getVariables()[row][col].getArcs())
	        {
	            if (arc.getId() == aId)
	            {
	                childArc = arc;
	                break;
	            }
	        }
	        childSp.addArcsToTDAAfterSplit(childArc.getVariable());
	        
	        // deal with this problem
	        var.setDomain(tempDomain);
	        if (tempDomain.size() == 1)
	        {
	            var.setValue(tempDomain.iterator().next());
	        }
	        addArcsToTDAAfterSplit(var);
	        
	        
	        return childSp;
	    }
	    

	    public Variable[][] getVariables()
	    {
	        return variables;
	    }
	    
	    
	    public boolean isInfeasible() {
	        boolean emptyDomain = false;
	        for (int i = 0; i < N; i++)
	        {
	            for (int j = 0; j < N; j++)
	            {
	                if (variables[i][j].isDomainEmpty())
	                {
	                    emptyDomain = true;
	                    break;
	                }
	            }
	            if (emptyDomain)
	            {
	                break;
	            }
	        }
	        return emptyDomain;
	    }
	    
	    public boolean isSolved() {
	        boolean isSolved = true;
	        for (int i = 0; i < N; i++)
	        {
	            for (int j = 0; j < N; j++)
	            {
	                if (variables[i][j].getDomain().size() != 1)
	                {
	                    isSolved = false;
	                    break;
	                }
	            }
	            if (!isSolved)
	            {
	                break;
	            }
	        }
	        return isSolved;
	        
	    }

	} // end SudokuProblem class
	
	

    class Variable
    {
        private List<Arc> arcs;
        private Set<Integer> domain;
        private int row;
        private int col;
        private int value;
        private int id;
        
        
        public int getId()
        {
            return id;
        }
    
        public void setId(int id)
        {
            this.id = id;
        }
    
        public Variable(List<Arc> arcs, Set<Integer> domain, int row, int col,
                int value, int id)
        {
            super();
            this.arcs = arcs;
            this.domain = domain;
            this.row = row;
            this.col = col;
            this.value = value;
            this.id = id;
        }
    
        public Variable(List<Arc> arcs, Set<Integer> domain, int row, int col,
                int value)
        {
            super();
            this.arcs = arcs;
            this.domain = domain;
            this.row = row;
            this.col = col;
            this.value = value;
        }
    
        public Variable(int row, int col, int value)
        {
            arcs = new ArrayList<Arc>();
            domain = new HashSet<Integer>();
            if (0 == value)
            {
                for (int i = 1; i <= 9; i++) 
                {
                    domain.add(i);
                }
            }
            else
            {
                domain.add(value);
            }
            this.row = row;
            this.col = col;
            this.value = value;
        }
        
        public List<Arc> getArcs()
        {
            return arcs;
        }
    
        public void setArcs(List<Arc> arcs)
        {
            this.arcs = arcs;
        }
    
        public int getRow()
        {
            return row;
        }
    
        public void setRow(int row)
        {
            this.row = row;
        }
    
        public int getCol()
        {
            return col;
        }
    
        public void setCol(int col)
        {
            this.col = col;
        }
    
        public int getValue()
        {
            return value;
        }
    
        public void setValue(int value)
        {
            this.value = value;
        }
    
        public void setDomain(Set<Integer> domain)
        {
            this.domain = domain;
        }
    
        public Set<Integer> getDomain()
        {
            return domain;
        }
    
        /**
         * Finds the arcs that should be added back into the TDA.
         * These are the arcs that are attached to the same 
         * @param arcToIgnore
         * @return
         */
        public List<Arc> getArcsToAddBackToTDA(Arc arcToIgnore)
        {
            List<Arc> arcsToAdd = new ArrayList<Arc>();
           for (int i = 0; i < arcs.size(); i++)
           {
               Arc a = arcs.get(i);
               if (a != arcToIgnore)
               {
                   Constraint con = a.getConstraint();
                   Arc a1 = con.getArc1();
                   Arc a2 = con.getArc2();
                   if (a1 == a)
                   {
                       arcsToAdd.add(a2);
                   }
                   else
                   {
                       arcsToAdd.add(a1);
                   }
               }
           }
           return arcsToAdd;
        }
        
        public boolean isDomainEmpty()
        {
            return domain.isEmpty();
        }
        
        public void addArc(Arc a)
        {
            arcs.add(a);
        }
        
    
    }
    
    
    class Arc
    {
        private Variable variable;
        private Constraint constraint;
        private int id;
        
        public int getId()
        {
            return id;
        }

        public void setId(int id)
        {
            this.id = id;
        }

        public Arc(){}
        
        public Arc(Variable variable, Constraint constraint, int id)
        {
            super();
            this.variable = variable;
            this.constraint = constraint;
            this.id = id;
        }

        public Arc(Variable variable, Constraint constraint)
        {
            super();
            this.variable = variable;
            this.constraint = constraint;
        }
        public Variable getVariable()
        {
            return variable;
        }
        public void setVariable(Variable variable)
        {
            this.variable = variable;
        }
        public Constraint getConstraint()
        {
            return constraint;
        }
        public void setConstraint(Constraint constraint)
        {
            this.constraint = constraint;
        }
    }
    
    class Constraint
    {

        private Arc arc1;
        private Arc arc2;
        
        public Constraint(){}
        
        public Constraint(Arc arc1, Arc arc2)
        {
            super();
            this.arc1 = arc1;
            this.arc2 = arc2;
        }

        public void setArc1(Arc arc1)
        {
            this.arc1 = arc1;
        }

        public void setArc2(Arc arc2)
        {
            this.arc2 = arc2;
        }

        /**
         * Prunes the domain of the passed arc
         * @param arcUnderConsideration
         */
        public boolean pruneDomain(Arc arcUnderConsideration)
        {
            Arc otherArc = null;
            if (arc1 == arcUnderConsideration) 
            {
                otherArc = arc2;
            } 
            else if (arc2 == arcUnderConsideration) 
            {
                otherArc = arc1;
            }
            else
            {
                throw new RuntimeException("arc doesn't exist in this constraint");
            }
            Set<Integer> domainToPrune = arcUnderConsideration.getVariable().getDomain();
            
            Set<Integer> domainToCheck = otherArc.getVariable().getDomain();
            
            
            ArrayList<Integer> elementsToPrune = new ArrayList<Integer>();
            for (Integer pruneElem : domainToPrune)
            {
                boolean elementIsValid = false;
                for (Integer checkElem : domainToCheck)
                {
                    if (!elementIsValid)
                    {
                        elementIsValid = pruneElem.intValue() != checkElem.intValue();
                    }
                    else
                    {
                        break;
                    }
                }
                
                if (!elementIsValid)
                {
                    elementsToPrune.add(pruneElem);
                }
            }
            for (Integer elem : elementsToPrune)
            {
                domainToPrune.remove(elem);
            }
            if (domainToPrune.size() == 1)
            {
                java.util.Iterator<Integer> it = domainToPrune.iterator();
                arcUnderConsideration.getVariable().setValue(it.next());
            }
            return elementsToPrune.size() > 0;
        }
        
        public Arc getArc1()
        {
            return arc1;
        }

        public Arc getArc2()
        {
            return arc2;
        }

        /**
         * Get the arcs of the constraint to get to the varaibles
         * say the variable indecies 
         */
        public String toString()
        {
            String arc1_index = "("+arc1.getVariable().getRow()+","+arc1.getVariable().getCol()+")";
            String arc2_index = "("+arc2.getVariable().getRow()+","+arc2.getVariable().getCol()+")";
            return arc1_index + " <> " + arc2_index;
        }


    }
    
    class InvalidSudokuBoardException extends Exception
    {
        public InvalidSudokuBoardException(){super();}
        public InvalidSudokuBoardException(String msg){super(msg);}
    }


}
