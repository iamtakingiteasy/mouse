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
//
//=========================================================================

package mouse.explorer;

import java.util.Comparator;
import java.util.Vector;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  Class Conflict
//
//-------------------------------------------------------------------------
//
//  Represents an LL1 violation. It can be one of these:
//
//  (1) A pair of alternatives 'arg1', 'arg2' of a Choice
//      expression 'expr' where 'arg1' and 'arg2 Tail(expr)'
//      have non-disjoint first terminals.
//  (2) The argument 'arg1' of Plus, Star, or Query expression 'expr'
//      that has non-disjoint first terminal(s) with 'Tail(expr)'.
//
//  In case (2), 'arg2' is null.
//  In each case, the non-disjoint pairs of terminals are described
//  by TermPair objects listed in 'termPairs'.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

public class Conflict
{
  public Expr arg1;
  public Expr arg2;
  public Expr expr;
  public TermPair[] termPairs;

  //------------------------------------------------------------------
  //  Constructor.
  //------------------------------------------------------------------
  Conflict(final Expr e1,final Expr e2,final Expr e,final Vector<TermPair> tp)
    {
      arg1 = e1;
      arg2 = e2;
      expr  = e;
      termPairs = tp.toArray(new TermPair[0]);
    }

  //------------------------------------------------------------------
  //  Present both parts of the conflict as a String.
  //------------------------------------------------------------------
  String asString()
    {
      StringBuffer sb = new StringBuffer(arg1.simple());
      if (arg2==null)
        return arg1.simple() + "  <==>  Tail(" + expr.name + ")";
      else
        return arg1.simple() + "  <==>  " + arg2.simple() + " Tail(" + expr.name + ")";
    }



  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  Comparator for sorting by name of 'expr'.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class Compare implements Comparator<Conflict>
  {
    public int compare(final Conflict e1, final Conflict e2)
      { return (e1.expr.name).compareTo(e2.expr.name); }
  }
}
