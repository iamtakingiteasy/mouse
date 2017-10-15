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
//      Make the window full-screen.
//
//=========================================================================

package mouse.explorer;

import java.util.HashSet;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  class GUI_Grammar
//
//-------------------------------------------------------------------------
//
//  A window displaying the grammar.
//  Single paragraph with lines representing Expressions.
//  Originally, only the Rules in alphabetical order.
//
//  Selecting a Rule and pressing the button 'Find' shows
//  all rules that contain reference to the selected Rule.
//
//  Pressing 'All' shows all Rules and subexpressions.
//
//  Pressing 'non-LL1' opens the window 'GUI_Conflicts' showing
//  pairs of expressions violating LL(1).
//
//  Pressing 'First' or 'Tail' opens the window 'GUI_First' or
//  'GUI_Tail' showing these properties of selected expression.
//
//  The window is a singleton. The static method 'display' either creates
//  a new window or displays the existing one.
//
//  Closing this window exits the Explorer and closes remaining windows.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

public class GUI_Grammar extends GUI_List
{
  //=====================================================================
  //
  //  Data
  //
  //=====================================================================
  //-------------------------------------------------------------------
  // Existing instance of this window
  //-------------------------------------------------------------------
  static GUI_Grammar instance = null;

  //-------------------------------------------------------------------
  //  Expression in the line selected by mouse action.
  //-------------------------------------------------------------------
  Expr selectedExpr = null;

  //-------------------------------------------------------------------
  //  Serial version UID.
  //-------------------------------------------------------------------
  static final long serialVersionUID = 4715L;


  //=====================================================================
  //
  //  Constructor
  //
  //=====================================================================
  private GUI_Grammar()
    {
      super(100,0);

      addKey("Rules");
      addKey("All");
      addKey("Find");
      addKey("Non-LL1");
      addKey("First");
      addKey("Tail");

      keyPressed("Rules"); // Set up initial contents
    }

  //-------------------------------------------------------------------
  //  Display the window
  //-------------------------------------------------------------------
  public static void display()
    {
      if (instance==null)
        instance = new GUI_Grammar();
      GUI.display(instance);
    }


  //=====================================================================
  //
  //  Actions
  //
  //=====================================================================
  //-------------------------------------------------------------------
  //  Mouse action.
  //-------------------------------------------------------------------
  void mouseAction(boolean clicked)
    {
      if (selected==null)
        selectedExpr = null;
      else
        selectedExpr = para.line(selected.line).expr;
    }

  //-------------------------------------------------------------------
  //  Button with text 'command' pressed.
  //-------------------------------------------------------------------
    void keyPressed(String command)
    {
      switch(command)
      {
        //-------------------------------------------------------------
        //  Rules
        //-------------------------------------------------------------
        case "Rules":
        {
          show(PEG.rules);
          setTitle("Rules");
          return;
        }

        //-------------------------------------------------------------
        //  All
        //-------------------------------------------------------------
        case "All":
        {
          show(PEG.index);
          setTitle("All expressions");
          return;
        }

        //-------------------------------------------------------------
        //  Find
        //-------------------------------------------------------------
        case "Find":
        {
          if (selected==null) return;
          setTitle("Find  " + selectedExpr.name);
          HashSet<Expr> temp = new HashSet<Expr>();
          for (Expr.Ref ref: PEG.refs)
            if (ref.rule==selectedExpr) temp.add(ref.inRule);
          show(temp);
          return;
        }

        //-------------------------------------------------------------
        //  Non-LL1
        //-------------------------------------------------------------
        case "Non-LL1":
        {
          GUI_Conflicts.display();
          return;
        }

        //-------------------------------------------------------------
        //  First
        //-------------------------------------------------------------
        case "First":
        {
          if (selected==null) return;
          GUI_First.display(selectedExpr);
          return;
        }

        //-------------------------------------------------------------
        //  Tail
        //-------------------------------------------------------------
        case "Tail":
        {
          if (selected==null) return;
          GUI_Tail.display(selectedExpr);
          return;
        }
      }
    }

  //-------------------------------------------------------------------
  //  Window closed
  //-------------------------------------------------------------------
  public void windowClosed()
    { /* do nothing */ }
}
