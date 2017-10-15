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
//      Method 'apply' increments error count in PEG and returns
//        after detecting left recursion.
//      Added detection of end-of-file in Sequence.
//      Messages saved in PEG.notes to be displayed by 'ExplorePEG'.
//      Corrected bugs and message texts.
//
//=========================================================================

package mouse.explorer;

import java.util.HashSet;
import mouse.utility.BitMatrix;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  Class Diagnose
//
//-------------------------------------------------------------------------
//
//  Static class that contains method to detect and write messages about:
//  - left recursion;
//  - no-fail alternatives in Choice;
//  - end-of-file in Sequence;
//  - expressions that always fail;
//  - superfluous '?' operators.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

class Diagnose
{
  //-------------------------------------------------------------------
  //  Matrices for diagnosing left recursion.
  //-------------------------------------------------------------------
  static BitMatrix calls = Relations.calls;
  static BitMatrix Calls = calls.closure();

  //=====================================================================
  //
  //  Lists of expression names to appear in diagnostics.
  //  The grammar often contains duplicate sub-expressions.
  //  To avoid duplication of messages, information is collected
  //  in hash sets.
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Left-recursive expressions.
  //-------------------------------------------------------------------
  static HashSet<String> recur = new HashSet<String>();

  //-------------------------------------------------------------------
  //  Nullable iterations.
  //-------------------------------------------------------------------
  static HashSet<String> iter = new HashSet<String>();

  //-------------------------------------------------------------------
  //  Choice alternatives that cannot fail.
  //-------------------------------------------------------------------
  static HashSet<String> choice = new HashSet<String>();

  //-------------------------------------------------------------------
  //  End-of-file in Sequence.
  //-------------------------------------------------------------------
  static HashSet<String> eof = new HashSet<String>();

  //-------------------------------------------------------------------
  //  Expressions that always fail.
  //-------------------------------------------------------------------
  static HashSet<String> fail = new HashSet<String>();

  //-------------------------------------------------------------------
  //  Superfluous query.
  //-------------------------------------------------------------------
  static HashSet<String> query = new HashSet<String>();


  //=====================================================================
  //
  //  Do the job.
  //
  //=====================================================================
  static void apply()
    {
      //---------------------------------------------------------------
      //  Scan expressions using DiagVisitor.
      //---------------------------------------------------------------
      DiagVisitor diagVisitor = new DiagVisitor();
      for (Expr e: PEG.index)
        e.accept(diagVisitor);

      //---------------------------------------------------------------
      //  Find left recursion.
      //---------------------------------------------------------------
      for (Expr r: PEG.rules)
      {
        int i = r.index;
        if (Calls.at(i,i))
          leftRecursion(i);
      }

      //---------------------------------------------------------------
      //  Find expressions that always fail.
      //---------------------------------------------------------------
      for (Expr e: PEG.index)
        if (!(e.nul|e.adv))
           fail.add(e.simple());

      //---------------------------------------------------------------
      //  If the grammar is left recursive, identify the case(s),
      //  increment error count in PEG, and return.
      //---------------------------------------------------------------
      if (!(iter.isEmpty() && recur.isEmpty()))
      {
        StringBuffer sb = new StringBuffer();
        sb.append("\nThe grammar is left-recursive:\n\n");
        for (String s: iter)
          sb.append(" - '" + s + "' may consume empty string.\n");

        for (String s: recur)
          sb.append(s + ".\n");

        sb.append("\nExploration is abandoned.");

        System.out.println(sb.toString());
        PEG.errors++;
        return;
      }

      //---------------------------------------------------------------
      //  We arrive here only if the grammar is well-formed.
      //  (Otherwise the expression attributes may be incomplete.)
      //---------------------------------------------------------------
      StringBuffer sb = new StringBuffer();
      for (String s: choice)
        sb.append("'" + s + "' never fails and hides other alternative(s).\n");

      for (String s: eof)
        sb.append("'" + s + "' succeeds only on end of input.\n");

      for (String s: fail)
        sb.append("'" + s + "' always fails.\n");

      for (String s: query)
        sb.append("As '" + s + "' never fails, the '?' can be dropped.\n");

      if (sb.length()>0)
        PEG.notes = sb.toString();
    }


  //=====================================================================
  //
  //  Provide left-recursion details of exprs[i].
  //
  //=====================================================================
  private static void leftRecursion(int i)
    {
      StringBuilder sb = new StringBuilder();
      sb.append(" - '" + PEG.index[i].simple() + "' is left-recursive");
      String sep = " via ";
      for (int j=0;j<PEG.N;j++)
      {
        if (calls.at(i,j) && Calls.at(j,i))
        {
          sb.append(sep + "'" + PEG.index[j].simple() + "'");
          sep = " and ";
        }
      }
      recur.add(sb.toString());
    }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  DiagVisitor - collects diagnostic information.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  static class DiagVisitor extends mouse.explorer.Visitor
  {
    //-----------------------------------------------------------------
    //  Choice.
    //-----------------------------------------------------------------
    public void visit(Expr.Choice e)
      {
        Expr args[] = e.args;
        for (int i=0; i<args.length-1; i++)
          if (!args[i].fal)
            choice.add(args[i].simple() + "' in '" + e.simple());
      }

    //-----------------------------------------------------------------
    //  Sequence.
    //-----------------------------------------------------------------
    public void visit(Expr.Sequence e)
      {
        Expr args[] = e.args;
        for (int i=0; i<args.length-1; i++)
          if (args[i].end)
            eof.add(args[i].simple() + "' in '" + e.asString());
      }

    //-----------------------------------------------------------------
    //  Plus.
    //-----------------------------------------------------------------
    public void visit(Expr.Plus e)
      { if (e.arg.nul) iter.add(e.arg.simple() + "' in '" + e.asString()); }

    //-----------------------------------------------------------------
    //  Star.
    //-----------------------------------------------------------------
    public void visit(Expr.Star e)
      { if (e.arg.nul) iter.add(e.arg.simple() + "' in '" + e.asString()); }

    //-----------------------------------------------------------------
    //  Query.
    //-----------------------------------------------------------------
    public void visit(Expr.Query e)
      { if (!e.arg.fal) query.add(e.arg.simple() + "' in '" + e.asString()); }
  }
}
