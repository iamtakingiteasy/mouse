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
//      Changed for the redesigned Tail class.
//      Corrected initial text for "Explain".
//      Defined size and position of the window.
//
//=========================================================================

package mouse.explorer;

import java.util.Vector;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  class GUI_Tail
//
//-------------------------------------------------------------------------
//
//  A window opened by the grammar window to show Tail of an Expression.
//  It shows refined tail of the expression selected in the grammar window.
//  It has a single paragraph where each line shows one Tail.Strand object.
//
//  By selecting a line and pressing "Explain", or clicking on a line,
//  you open a GUI_Info window with explanation how the Strand was obtained.
//
//  The window is a singleton. The static method 'display' either creates
//  a new window or displays the existing one.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

public class GUI_Tail extends GUI
{
  //=====================================================================
  //
  //  Data
  //
  //=====================================================================
  //-------------------------------------------------------------------
  // Existing instance of this window
  //-------------------------------------------------------------------
  private static GUI_Tail instance = null;

  //-------------------------------------------------------------------
  //  Copy of the paragraph, kept to preserve its type.
  //  Elements of 'paragraphs' in GUI are of type Paragraph.
  //-------------------------------------------------------------------
  Paragraph_Lines<Line_Tail> para;

  //-------------------------------------------------------------------
  //  The expression whose tail is shown.
  //-------------------------------------------------------------------
  Expr theExpr;

  //-------------------------------------------------------------------
  //  Serial version UID.
  //-------------------------------------------------------------------
  static final long serialVersionUID = 4718L;


  //=====================================================================
  //
  //  Constructor
  //
  //=====================================================================
  private GUI_Tail()
    {
      super(1,70,50);

      para = new Paragraph_Lines<Line_Tail>();
      paragraphs[0] = para;

      addKey("Explain");
    }

  //-------------------------------------------------------------------
  //  Display window for 'expr'.
  //-------------------------------------------------------------------
  static void display(final Expr expr)
    {
      if (instance==null)
        instance = new GUI_Tail();
      instance.initialize(expr);
      GUI.display(instance);
    }

  //-------------------------------------------------------------------
  //  Initialize.
  //-------------------------------------------------------------------
  private void initialize(final Expr expr)
  {
    theExpr = expr;
    para.clear();
    Tail theTail = theExpr.tail.expand();
    // Tail theTail = theExpr.tail;      // Skip refining
    for (Tail.Strand strand: theTail)
      para.addLine(new Line_Tail(strand));
    setTitle("Tail  (" + theExpr.named() + ")");
    write();
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
    { if (clicked) keyPressed("Explain"); }

  //-------------------------------------------------------------------
  //  Button with text 'command' pressed.
  //-------------------------------------------------------------------
  void keyPressed(String command)
    {
      switch(command)
      {
        //-------------------------------------------------------------
        //  Explain
        //-------------------------------------------------------------
        case "Explain":
        {
          if (selected==null) return;

          Line_Tail theLine = para.line(selected.line);
          Tail.Strand theStrand = theLine.strand;

          StringBuffer sb = new StringBuffer();

          sb.append(theStrand.E.named());

          while(theStrand.parent!=null)
          {
            theStrand = theStrand.parent;
            sb.insert(0,theStrand.E.named() + "\n    which is called from\n");
          }

          sb.insert(0,theExpr.named() + "\n    is called from\n");

          sb.insert(0,"Explain " + theLine.strand.asString() + "\nbelonging to Tail(" + theExpr.simple() + ")\n\n");

          GUI_Info.display(sb.toString(),"Explain Tail");
          return;
        }
        default: ;
      }
    }

  //-------------------------------------------------------------------
  // Window closed
  //-------------------------------------------------------------------
  public void windowClosed()
    { instance = null; }
}