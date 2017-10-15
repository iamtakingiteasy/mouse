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
//    170301 Created
//    170901 Version 1.9.1
//      Total redesign.
//
//=========================================================================

package mouse.explorer;

import java.util.Vector;
import java.util.Hashtable;


//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
//
//  Class Tail
//
//-------------------------------------------------------------------------
//
//  Represents the set of input strings that may follow a successful call
//  to expression 'e' in a completed parse. It is a collection of objects
//  of class 'Tail.Strand'. Each of these objects represents the set of
//  input strings that may follow 'e' when it is called from a specific
//  expression 'E'.
//  If 'e' may be called from different places in 'E', there is a separate
//  Strand object for each such call.
//  If 'e' has the 'end' attribute, meaning it ends the parse, its tail
//  is the empty string, represented by empty collection of Strands.
//
//HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

public class Tail extends Vector<Tail.Strand>
{

  //-------------------------------------------------------------------
  //  Whose Tail it is - used only in testing.
  //-------------------------------------------------------------------
  Expr e;

  //-------------------------------------------------------------------
  //  Serial version UID.
  //-------------------------------------------------------------------
  static final long serialVersionUID = 4719L;


  //=====================================================================
  //
  //  Constructor.
  //
  //=====================================================================
  Tail(final Expr e)
    { this.e = e; }


  //=====================================================================
  //
  //  Add Strand.
  //
  //=====================================================================
  public boolean add(final Strand strand)
    { return super.add(strand); }


  //=====================================================================
  //
  //  As String.
  //
  //=====================================================================
  String asString()
    {
      StringBuffer sb = new StringBuffer();
      String nl = "";
      for (Strand strand: this)
      {
        sb.append(nl + strand.asString());
        nl = "\n";
      }
      return sb.toString();
    }


  //=====================================================================
  //
  //  Expand this Tail to Tail of refined Strands.
  //
  //=====================================================================
  Tail expand()
    {
      Tail result = new Tail(e);
      for (Strand strand: this)
        result.addAll(strand.refine());
      return result;
    }


  //=====================================================================
  //
  //  Expand this Tail to unique refined Strands.
  //  (Strands considered identical if they have the same String form.)
  //
  //=====================================================================
  Tail expandUnique()
    {
      Hashtable<String,Strand> temp = new Hashtable<String,Strand>();
      Tail expanded = expand();
      for (Strand strand: expanded)
        temp.put(strand.asString(),strand);

      Tail result = new Tail(e);
      for (String key: temp.keySet())
        result.add(temp.get(key));
      return result;
    }


  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
  //
  //  Class Tail.Strand
  //
  //-----------------------------------------------------------------------
  //
  //  Represents the fact that expression 'E' contains a call
  //  to expression 'e'. The call is followed by the (possibly empty)
  //  sequence 'seq' expression calls.
  //  If 'E' contains several calls to 'e', each of them is represented
  //  by a separate strand.
  //  The flag 'hasTail' being true indicates that the caller of 'E'
  //  may continue after completion of 'E' (after this call to 'e').
  //  The flag being false indicates that completion of 'E' reaches
  //  the end of input.
  //  The Strand can be expanded, which means replacing the tail of 'E'
  //  by its Strands. To present this process in the window 'explain',
  //  field 'parent' in the expanded strand points to the original one.
  //
  //HHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH

  public static class Strand
  {
    Expr e;
    Expr E;
    Vector<Expr> seq = new Vector<Expr>();
    boolean hasTail;
    Strand parent = null;

    //===================================================================
    //
    //  Constructors
    //
    //===================================================================
    //-----------------------------------------------------------------
    //  With empty 'seq'.
    //-----------------------------------------------------------------
    Strand(final Expr called, final Expr calling)
      {
        e = called;
        E = calling;
        hasTail = !e.end;
      }

    //-----------------------------------------------------------------
    //  With nonempty 'seq'.
    //-----------------------------------------------------------------
    Strand(final Expr called, final Expr calling, final Expr ... follow)
      {
        e = called;
        E = calling;
        hasTail = !e.end;
        for (Expr elem: follow)
        {
          seq.add(elem);
          hasTail &= !elem.end;
        }
      }

    //-----------------------------------------------------------------
    //  Copy.
    //-----------------------------------------------------------------
    Strand(final Strand from)
      {
        e = from.e;
        E = from.E;
        hasTail = from.hasTail;
        seq.addAll(from.seq);
        parent = from.parent;
      }


    //===================================================================
    //
    //  As String.
    //
    //===================================================================
    String asString()
      {
        // If 'e' has 'end' attribute, this strand is empty.
        if (e.end) return "";

        // Otherwise the strand extends to the first element
        // of 'seq' with 'end' attribute.
        StringBuffer sb = new StringBuffer();
        for (Expr elem: seq)
        {
          if (elem.bind()==0)
            sb.append("(" + elem.asString() +  ") ");
          else
            sb.append(elem.asString()+ " ");
          if (elem.end)
            return sb.toString();
        }

        // If there is no 'end' attribute, the strand
        // continues with tail of 'E'.
        sb.append("Tail("+E.name+")");
        return sb.toString();
      }

    //===================================================================
    //
    //  Expand this Strand.
    //  The result is a set of expanded Strands.
    //
    //===================================================================
    private Tail expand()
      {
        Tail result = new Tail(e);
        // If this Strand has no tail
        // return its copy as the only element of result.
        if (!hasTail)
        {
          Strand expanded = new Strand(this);
          expanded.parent = this;
          result.add(expanded);
          return result;
        }

        // If this Strand has tail, it is the tail of E.
        Tail theTail = E.tail;

        // If Tail(E) is empty, return a copy of this Strand
        // with empty tail as the only element of result.
        if (theTail.isEmpty())
        {
          Strand expanded = new Strand(this);
          expanded.hasTail = false;
          expanded.parent = this;
          result.add(expanded);
          return result;
        }

        // If Tail(E) is not empty,
        // expand each of its strands and add to result.
        for (Strand strand: E.tail)
        {
          Strand expanded = new Strand(this);
          expanded.E = strand.E;
          expanded.seq.addAll(strand.seq);
          expanded.hasTail = strand.hasTail;
          expanded.parent = this;
          result.add(expanded);
        }
        return result;
      }


    //===================================================================
    //
    //  Refine this Strand.
    //
    //===================================================================
    Vector<Strand> refine()
      {
        Vector<Strand> toRefine = new Vector<Strand>();
        Vector<Strand> refined = new Vector<Strand>();

        toRefine.add(this);

        refining:
        while(true)
        {
          boolean anythingDone = false;
          for (Strand strand: toRefine)
          {
            if (strand.hasTail && !strand.E.isRule)
            {
              refined.addAll(strand.expand());
              anythingDone = true;
            }
            else
              refined.add(strand);
          }
          if (!anythingDone) break refining;
          toRefine.clear();
          toRefine.addAll(refined);
          refined.clear();
        }
        return refined;
     }
  }
}

