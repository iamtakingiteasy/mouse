//=========================================================================
//
//  Part of PEG parser generator Mouse.
//
//  Copyright (C) 2017 by Roman R. Redziejowski (www.romanredz.se).
//
//  Licensed under the Apache License, Version 2.0 (the "License");
//  you may not use this file except in compliance with the License.
//  You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//  Unless required by applicable law or agreed to in writing, software
//  distributed under the License is distributed on an "AS IS" BASIS,
//  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//  See the License for the specific language governing permissions and
//  limitations under the License.
//
//-------------------------------------------------------------------------
//
//  Change log
//    170301 Created.
//    170901 Version 1.9.1
//      Removed use of 'NUL' attribute of Expr.
//      Removed computation of Follow.
//
//=========================================================================

package mouse.explorer;

import java.util.BitSet;
import mouse.utility.BitMatrix;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  Relations
//
//-------------------------------------------------------------------------
//
//  Holds relations between Expressions.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

class Relations
{
  static Expr[] index;
  static Expr[] rules;
  static Expr[] terms;
  static int N;  // Total length of index.
  static int T;  // Number of Terminals.

  //-------------------------------------------------------------------
  //  Relation calls.
  //  calls[i,j] = true means that expression i may call expression j
  //  at its starting position.
  //  The relation is used to detect left-recursion.
  //-------------------------------------------------------------------
  static BitMatrix calls;

  //-------------------------------------------------------------------
  //  Relation first.
  //  first[i,j] = true means that expression i may call, at its
  //  starting position, an expression j that consumes some input.
  //  The relation is used to check LL1 condition.
  //-------------------------------------------------------------------
  static BitMatrix first;

  //-------------------------------------------------------------------
  //  Relation First.
  //  First[i,j] = true means that call to expression i may result,
  //  directly or indirectly to a call to expression j that consumes
  //  some input.
  //  The relation is used to check LL1 condition.
  //-------------------------------------------------------------------
  static BitMatrix First;

  //-------------------------------------------------------------------
  //  Relation disjoint over Terminals.
  //  disjoint[i,j] = true means that Terminals with indexes i,j
  //  are disjoint. nonDisjoint[i,j] means they are not disjoint.
  //-------------------------------------------------------------------
  public static BitMatrix disjoint;
  public static BitMatrix nonDisjoint;

  //=====================================================================
  //  Compute.
  //=====================================================================
  public static void compute()
    {
      index = PEG.index;
      rules = PEG.rules;
      terms = PEG.terms;
      N = PEG.N;
      T = PEG.T;

      //---------------------------------------------------------------
      //  Compute calls, first, and First.
      //---------------------------------------------------------------
      calls = BitMatrix.empty(N);
      first = BitMatrix.empty(N);

      MatrixVisitor matrixVisitor = new MatrixVisitor();

      for (Expr e: index)
        e.accept(matrixVisitor);

      First = first.star();

      //---------------------------------------------------------------
      //  Compute nonDisjoint.
      //---------------------------------------------------------------
      disjoint = BitMatrix.empty(T);

      DisjointVisitor disjointVisitor = new DisjointVisitor();

      for (Expr e: terms)
        e.accept(disjointVisitor);

      nonDisjoint = disjoint.not();
    }


  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  MatrixVisitor - builds matrices 'calls' and 'first'.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  static class MatrixVisitor extends mouse.explorer.Visitor
  {
    //-----------------------------------------------------------------
    //  Choice.
    //-----------------------------------------------------------------
    public void visit(Expr.Choice e)
      {
        for (Expr arg: e.args)
        {
          calls.set(e.index,arg.index);
          if (arg.adv) first.set(e.index,arg.index);
        }
      }

    //-----------------------------------------------------------------
    //  Sequence.
    //-----------------------------------------------------------------
    public void visit(Expr.Sequence e)
      {
        for (int i=0; i<e.args.length; i++)
        {
          calls.set(e.index,e.args[i].index);
          if (e.args[i].adv) first.set(e.index,e.args[i].index);
          if (!e.args[i].nul || e.args[i].end) break;
        }
      }

    //-----------------------------------------------------------------
    //  And predicate.
    //-----------------------------------------------------------------
    public void visit(Expr.And e)
      {
        calls.set(e.index,e.arg.index);
      }

    //-----------------------------------------------------------------
    //  Not predicate.
    //-----------------------------------------------------------------
    public void visit(Expr.Not e)
      {
        calls.set(e.index,e.arg.index);
      }

    //-----------------------------------------------------------------
    //  Plus.
    //-----------------------------------------------------------------
    public void visit(Expr.Plus e)
      {
        calls.set(e.index,e.arg.index);
        if (e.arg.adv) first.set(e.index,e.arg.index);
      }

    //-----------------------------------------------------------------
    //  Star.
    //-----------------------------------------------------------------
    public void visit(Expr.Star e)
      {
        calls.set(e.index,e.arg.index);
        if (e.arg.adv) first.set(e.index,e.arg.index);
      }

    //-----------------------------------------------------------------
    //  Query.
    //-----------------------------------------------------------------
    public void visit(Expr.Query e)
      {
        calls.set(e.index,e.arg.index);
        if (e.arg.adv) first.set(e.index,e.arg.index);
      }

    //-----------------------------------------------------------------
    //  Ref.
    //-----------------------------------------------------------------
    public void visit(Expr.Ref e)
      {
        first.set(e.index,e.rule.index);
        calls.set(e.index,e.rule.index);
      }
  }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  DisjointVisitor - builds matrix 'disjoint' for Terminals.
  //
  //-----------------------------------------------------------------------
  //
  //  The visitor is invoked for all terminals in the order of indices.
  //  When invoked for Terminal with index 'i' it checks that Terminal
  //  against all Terminals with higher indices. It was already checked
  //  by previous calls against Terminals with lower indices.
  //  It is obviously not disjoint with itself.
  //  The Terminals of class Any and SigmaStar are not disjoint
  //  with anything, so they are not checked.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  static class DisjointVisitor extends Visitor
  {
    //-----------------------------------------------------------------
    //  StringLit.
    //-----------------------------------------------------------------
    public void visit(Expr.StringLit x)
    {
      int i = x.index;
      for (int j=i+1;j<T;j++)
      {
        if (index[j] instanceof Expr.StringLit)
          check(x,(Expr.StringLit)index[j]);

        if (index[j] instanceof Expr.CharClass)
          check(x,(Expr.CharClass)index[j]);

        if (index[j] instanceof Expr.Range)
          check(x,(Expr.Range)index[j]);
      }
    }

    //-----------------------------------------------------------------
    //  CharClass.
    //-----------------------------------------------------------------
    public void visit(Expr.CharClass x)
    {
      int i = x.index;
      for (int j=i+1;j<T;j++)
      {
        if (index[j] instanceof Expr.StringLit)
          check((Expr.StringLit)index[j],x);

        if (index[j] instanceof Expr.CharClass)
          check(x,(Expr.CharClass)index[j]);

        if (index[j] instanceof Expr.Range)
          check(x,(Expr.Range)index[j]);
      }
    }

    //-----------------------------------------------------------------
    //  Range.
    //-----------------------------------------------------------------
    public void visit(Expr.Range x)
    {
      int i = x.index;
      for (int j=i+1;j<T;j++)
      {
        if (index[j] instanceof Expr.StringLit)
          check((Expr.StringLit)index[j],x);

        if (index[j] instanceof Expr.CharClass)
          check((Expr.CharClass)index[j],x);

        if (index[j] instanceof Expr.Range)
          check(x,(Expr.Range)index[j]);
      }
    }

    //-----------------------------------------------------------------
    //  End.
    //-----------------------------------------------------------------
    public void visit(Expr.End x)
    {
      for (Expr y: terms)
        setDisjoint(x,y);
    }


    //=================================================================
    //  Checks for disjointness
    //=================================================================
    //-----------------------------------------------------------------
    //  String - String
    //-----------------------------------------------------------------
    private void check(final Expr.StringLit x, final Expr.StringLit y)
      {
        if (!x.s.startsWith(y.s) && !y.s.startsWith(x.s))
          setDisjoint(x,y);
      }

    //-----------------------------------------------------------------
    //  String - Class
    //-----------------------------------------------------------------
    private void check(final Expr.StringLit x, final Expr.CharClass y)
      {
        if (y.s.indexOf(x.s.charAt(0))<0) // First of x not in y
        { if (!y.hat) setDisjoint(x,y);}
        else                              // First of x in y
        { if (y.hat) setDisjoint(x,y);}
      }

    //-----------------------------------------------------------------
    //  String - Range
    //-----------------------------------------------------------------
    private void check(final Expr.StringLit x, final Expr.Range y)
      {
        char c = x.s.charAt(0);
        if (c<y.a || c>y.z) setDisjoint(x,y);
      }

    //-----------------------------------------------------------------
    //  Class - Class
    //-----------------------------------------------------------------
    private void check(final Expr.CharClass x, final Expr.CharClass y)
      {
        if ( (!x.hat && !y.hat && disjoint(x.s,y.s))
          || (x.hat && !y.hat && contains(x.s,y.s))
          || (!x.hat && y.hat && contains(y.s,x.s)))
          setDisjoint(x,y);
        // If both x and y have ^, they are disjoint only if they
        // together contain the whole character set.
      }

    //-----------------------------------------------------------------
    //  Class - Range
    //-----------------------------------------------------------------
    private void check(final Expr.CharClass x, final Expr.Range y)
      {
        for (char c=y.a;c<=y.z;c++)
          if (x.s.indexOf(c)>=0) return;
        setDisjoint(x,y);
      }

    //-----------------------------------------------------------------
    //  Range - Range
    //-----------------------------------------------------------------
    private void check(final Expr.Range x, final Expr.Range y)
      {
        if (x.z<y.a || y.z<x.a)
          setDisjoint(x,y);
      }

    //-----------------------------------------------------------------
    //  Set x,y disjoint:
    //  all elements of class x disjoint with all in class y.
    //-----------------------------------------------------------------
    private void setDisjoint(final Expr x, final Expr y)
      {
        disjoint.set(x.index,y.index);
        disjoint.set(y.index,x.index);
      }

    //-----------------------------------------------------------------
    //  Do 'x' and 'y' have no common letters?
    //-----------------------------------------------------------------
    private boolean disjoint(final String x, final String y)
      {
        for (int k=0;k<y.length();k++)
          if (x.indexOf(y.charAt(k))>=0) return false;
        return true;
      }

    //-----------------------------------------------------------------
    //  Does 'x' contain all letters from 'y'?
    //-----------------------------------------------------------------
    private boolean contains(final String x, final String y)
      {
        for (int k=0;k<y.length();k++)
          if (x.indexOf(y.charAt(k))<0) return false;
        return true;
      }
  }

}