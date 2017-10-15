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
//      Define size and position of the window.
//
//=========================================================================

package mouse.explorer;

import java.util.BitSet;
import mouse.utility.BitIter;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  class GUI_First
//
//-------------------------------------------------------------------------
//
//  A window opened by the grammar window to show the property First.
//  It has a single paragraph with lines representing Expressions.
//
//  Intially it shows the expressions belonging to 'first' of expression
//  selected in the grammar window, one line per expression.
//
//  By selecting a line and pressing "More", or clicking on a line,
//  you display under that line the elements of 'first' of the expression
//  thus selected.
//
//  By pressing "All" you display all elements of "First".
//
//  This window is a singleton. The static method 'display' either creates
//  a new window or displays the existing one.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

public class GUI_First extends GUI_List
{
  //=====================================================================
  //
  //  Data
  //
  //=====================================================================
  //-------------------------------------------------------------------
  // Existing instance of this window
  //-------------------------------------------------------------------
  private static GUI_First instance = null;

  //-------------------------------------------------------------------
  //  The expression being examined.
  //-------------------------------------------------------------------
  Expr startExpr;

  //-------------------------------------------------------------------
  //  Serial version UID.
  //-------------------------------------------------------------------
  static final long serialVersionUID = 4714L;


  //=====================================================================
  //
  //  Constructor
  //
  //=====================================================================
  private GUI_First()
    {
      super(70,50);

      addKey("More");
      addKey("All");
    }

  //-------------------------------------------------------------------
  //  Display window for 'expr'.
  //-------------------------------------------------------------------
  static void display(final Expr expr)
    {
      if (instance==null)
        instance = new GUI_First();
      instance.initialize(expr);
      GUI.display(instance);
    }

  //-------------------------------------------------------------------
  //  Initialize.
  //-------------------------------------------------------------------
  private void initialize(final Expr expr)
  {
    startExpr = expr;
    setTitle("First  (" + startExpr.named() + ")");
    show(Relations.first.row(expr.index));

    /*
    System.out.println(expr.asString() + " nul = " + expr.nul
                                       + " adv = " + expr.adv
                                       + " fal = " + expr.fal
                                       + " end = " + expr.end);
    */

  }


  //=====================================================================
  //
  //  Actions
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Mouse action.
  //-------------------------------------------------------------------
  public void mouseAction(boolean clicked)
    { if (clicked) keyPressed("More"); }

  //-------------------------------------------------------------------
  //  Button with text 'command' pressed.
  //-------------------------------------------------------------------
  void keyPressed(String command)
    {
      switch(command)
      {
        //-------------------------------------------------------------
        //  More
        //-------------------------------------------------------------
        case "More":
        {
          if (selected==null) return;
          int lineNr = selected.line;
          Line_Expr theLine = para.line(lineNr);
          Expr theExpr = theLine.expr;
          BitSet first = Relations.first.row(theExpr.index);
          if (first.isEmpty()) return;
          for (BitIter iter=new BitIter(first);iter.hasNext();)
          {
            lineNr++;
            Line_Expr newLine = new Line_Expr(PEG.index[iter.next()],theLine.indent+2);
            para.insertLine(lineNr,newLine);
          }
          write();
          return;
        }

        //-------------------------------------------------------------
        //  All
        //-------------------------------------------------------------
        case "All":
        {
          show(Relations.First.row(startExpr.index));
          return;
        }
        default: ;
      }
    }

  //-------------------------------------------------------------------
  //  Window closed
  //-------------------------------------------------------------------
  public void windowClosed()
    { instance = null; }
}