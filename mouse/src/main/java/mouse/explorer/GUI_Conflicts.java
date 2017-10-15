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
//      Make the window slightly smaller than Grammar window.
//
//=========================================================================

package mouse.explorer;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  class GUI_Conflicts
//
//-------------------------------------------------------------------------
//
//  A window displaying list of LL(1) violation.
//  It has a single paragraph with lines representing the conflicts.
//
//  Clicking on a line or selecting line and pressing 'Show'
//  opens GUI_Explorer window for that conflict.
//
//  This window is a singleton. The static method 'display' either creates
//  a new window or displays the existing one.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

public class GUI_Conflicts extends GUI
{
  //=====================================================================
  //
  //  Data
  //
  //=====================================================================
  //-------------------------------------------------------------------
  // Existing instance of this window.
  //-------------------------------------------------------------------
  private static GUI_Conflicts instance = null;

  //-------------------------------------------------------------------
  //  Copy of the paragraph, kept to preserve its type.
  //  Elements of 'paragraphs' in GUI are of type Paragraph.
  //-------------------------------------------------------------------
  private Paragraph_Lines<Line_Conflict> para = new Paragraph_Lines<Line_Conflict>();

  //-------------------------------------------------------------------
  //  Serial version UID.
  //-------------------------------------------------------------------
  static final long serialVersionUID = 4712L;


  //=====================================================================
  //
  //  Constructor
  //
  //=====================================================================
  private GUI_Conflicts()
    {
      super(1,96,15);

      addKey("Show");

      for (Conflict c: Conflicts.conflicts)
        para.addLine(new Line_Conflict(c));
      paragraphs[0] = para;
      write();
      setTitle("Not LL1");
    }

  //-------------------------------------------------------------------
  //  Display
  //-------------------------------------------------------------------
  static void display()
    {
      if (instance==null)
        instance = new GUI_Conflicts();
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
  public void mouseAction(boolean clicked)
    { if (clicked) keyPressed("Show"); }

  //-------------------------------------------------------------------
  //  Key 'command' pressed.
  //-------------------------------------------------------------------
  void keyPressed(String command)
    {
      switch(command)
      {
        case("Show"):
        {
          if (selected==null) return;
          Conflict selectedConflict = para.line(selected.line).conflict;
          GUI_Explorer.display(selectedConflict);
          return;
        }
        default: ;
      }
    }

  //-------------------------------------------------------------------
  //  Window closed.
  //-------------------------------------------------------------------
  void windowClosed()
    { instance = null; }
}