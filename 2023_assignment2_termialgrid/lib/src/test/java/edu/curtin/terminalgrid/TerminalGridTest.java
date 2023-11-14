package edu.curtin.terminalgrid;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.util.*;

class TerminalGridTest
{
    private ByteArrayOutputStream output = new ByteArrayOutputStream();
    private TerminalGrid terminalGrid = new TerminalGrid(new PrintStream(output), 80);

    @Test
    void test1x1Empty()
    {
        terminalGrid.print(new String[][] {{""}});
        assertEquals(
            "┌──┐" + System.lineSeparator()  + 
            "│  │" + System.lineSeparator()  + 
            "└──┘" + System.lineSeparator() , 
            output.toString());
    }
    
    @Test
    void test2x2()
    {
        terminalGrid.print(new String[][] {{"one", "two"}, {"three", "four"}});
        assertEquals(
            "┌───────┬──────┐" + System.lineSeparator() + 
            "│ one   │ two  │" + System.lineSeparator() + 
            "├───────┼──────┤" + System.lineSeparator() + 
            "│ three │ four │" + System.lineSeparator() + 
            "└───────┴──────┘" + System.lineSeparator(), 
            output.toString());
    }
    
    @Test
    void test2x1Multiline()
    {
        terminalGrid.print(new String[][] {{"one\ntwo"}, {"three\nfour"}});
         assertEquals(
            "┌───────┐" + System.lineSeparator() + 
            "│ one   │" + System.lineSeparator() + 
            "│ two   │" + System.lineSeparator() + 
            "├───────┤" + System.lineSeparator() + 
            "│ three │" + System.lineSeparator() + 
            "│ four  │" + System.lineSeparator() + 
            "└───────┘" + System.lineSeparator(), 
            output.toString());
    }
    
    @Test
    void test1x2Multiline()
    {
        terminalGrid.print(new String[][] {{"one\ntwo", "three\nfour"}});
        assertEquals(
            "┌─────┬───────┐" + System.lineSeparator() + 
            "│ one │ three │" + System.lineSeparator() + 
            "│ two │ four  │" + System.lineSeparator() + 
            "└─────┴───────┘" + System.lineSeparator(), 
            output.toString());
    }
    
    @Test
    void test2x1UnevenMultiline()
    {
        terminalGrid.print(new String[][] {{"one"}, {"two\nthree\nfour"}});
        assertEquals(
            "┌───────┐" + System.lineSeparator() + 
            "│ one   │" + System.lineSeparator() + 
            "├───────┤" + System.lineSeparator() + 
            "│ two   │" + System.lineSeparator() + 
            "│ three │" + System.lineSeparator() + 
            "│ four  │" + System.lineSeparator() + 
            "└───────┘" + System.lineSeparator(), 
            output.toString());
    }
    
    @Test
    void test1x2UnevenMultiline()
    {
        terminalGrid.print(new String[][] {{"one", "two\nthree\nfour"}});
        assertEquals(
            "┌─────┬───────┐" + System.lineSeparator() + 
            "│ one │ two   │" + System.lineSeparator() + 
            "│     │ three │" + System.lineSeparator() + 
            "│     │ four  │" + System.lineSeparator() + 
            "└─────┴───────┘" + System.lineSeparator(), 
            output.toString());
    }
    
    @Test
    void test1x1Wrapped()
    {
        terminalGrid.setTerminalWidth(7);
        terminalGrid.print(new String[][] {{"two three"}});
        assertEquals(
            "┌─────┐" + System.lineSeparator() + 
            "│ two │" + System.lineSeparator() + 
            "│ thr │" + System.lineSeparator() + 
            "│ ee  │" + System.lineSeparator() + 
            "└─────┘" + System.lineSeparator(), 
            output.toString());
    }
    
    @Test
    void test1x2Wrapped2ndCol()
    {
        // It should be the second column that gets squashed, because it's wider (and still wider 
        // after being sufficiently narrowed to fit).
        terminalGrid.setTerminalWidth(15);
        terminalGrid.print(new String[][] {{"one", "two three four"}});
        assertEquals(
            "┌─────┬───────┐" + System.lineSeparator() + 
            "│ one │ two   │" + System.lineSeparator() + 
            "│     │ three │" + System.lineSeparator() + 
            "│     │ four  │" + System.lineSeparator() + 
            "└─────┴───────┘" + System.lineSeparator(), 
            output.toString());
    }

    @Test
    void test1x2Wrapped1stCol()
    {
        // It should be the first column that gets squashed this time.
        terminalGrid.setTerminalWidth(16);
        terminalGrid.print(new String[][] {{"one two three", "four"}});
        assertEquals(
            "┌───────┬──────┐" + System.lineSeparator() + 
            "│ one   │ four │" + System.lineSeparator() + 
            "│ two   │      │" + System.lineSeparator() + 
            "│ three │      │" + System.lineSeparator() + 
            "└───────┴──────┘" + System.lineSeparator(), 
            output.toString());
    }

    @Test
    void test1x2WrappedAllCols()
    {
        // Both columns should end up squashed to the same width (because squashing only the 
        // initially-wider column would make it narrower than the other column).
        terminalGrid.setTerminalWidth(17);
        terminalGrid.print(new String[][] {{"one two", "three four"}});
        assertEquals(
            "┌───────┬───────┐" + System.lineSeparator() +
            "│ one   │ three │" + System.lineSeparator() + 
            "│ two   │ four  │" + System.lineSeparator() +
            "└───────┴───────┘" + System.lineSeparator(), 
            output.toString());
    }

    @Test
    void test2x3WrappedAllCols()
    {
        terminalGrid.setTerminalWidth(25);
        terminalGrid.print(new String[][] {{"one two", "three four", "five six"}, {"seven eight", "nine ten", "zero one"}});
        
        assertEquals(
            "┌───────┬───────┬───────┐" + System.lineSeparator() +
            "│ one   │ three │ five  │" + System.lineSeparator() +
            "│ two   │ four  │ six   │" + System.lineSeparator() + 
            "├───────┼───────┼───────┤" + System.lineSeparator() +
            "│ seven │ nine  │ zero  │" + System.lineSeparator() +
            "│ eight │ ten   │ one   │" + System.lineSeparator() +
            "└───────┴───────┴───────┘" + System.lineSeparator(), 
            output.toString());
    }
    
    @Test
    void test2x3Headings()
    {
        terminalGrid.print(
            new String[][] {{"one", "two", "three"}, {"four", "five", "six"}},
            new String[] {"row 1", "row\n2"}, 
            new String[] {"column\nA", "col B", "C"});
        
        assertEquals("" + System.lineSeparator() + 
            "        column   col B   C      " + System.lineSeparator() +
            "        A                       " + System.lineSeparator() +
            "      ┌────────┬───────┬───────┐" + System.lineSeparator() +
            "row 1 │ one    │ two   │ three │" + System.lineSeparator() +
            "      ├────────┼───────┼───────┤" + System.lineSeparator() +
            "row   │ four   │ five  │ six   │" + System.lineSeparator() +
            "2     │        │       │       │" + System.lineSeparator() +
            "      └────────┴───────┴───────┘" + System.lineSeparator(), 
            "" + System.lineSeparator() + output.toString());
    }

    @Test
    void test2x2NestedLists()
    {
        terminalGrid.print(List.of(List.of("one", "two"), List.of("three", "four")));
        assertEquals(
            "┌───────┬──────┐" + System.lineSeparator() + 
            "│ one   │ two  │" + System.lineSeparator() + 
            "├───────┼──────┤" + System.lineSeparator() + 
            "│ three │ four │" + System.lineSeparator() + 
            "└───────┴──────┘" + System.lineSeparator(), 
            output.toString());
    }

    @Test
    void test2x2NestedListsHeadings()
    {
        terminalGrid.print(List.of(List.of("one", "two"), List.of("three", "four")),
                           List.of("row 1", "row 2"),
                           List.of("A", "B"));
        assertEquals(
            "        A       B     " + System.lineSeparator() +
            "      ┌───────┬──────┐" + System.lineSeparator() + 
            "row 1 │ one   │ two  │" + System.lineSeparator() + 
            "      ├───────┼──────┤" + System.lineSeparator() + 
            "row 2 │ three │ four │" + System.lineSeparator() + 
            "      └───────┴──────┘" + System.lineSeparator(), 
            output.toString());
    }
    
    @ParameterizedTest
    @ValueSource(ints = {0, 1, 12, 13})
    void test3x2SquashLimit(int terminalWidth)
    {
        // TerminalGrid should refuse to squash columns down below one character wide, even if this 
        // doesn't fit within the terminal width.
        terminalGrid.setTerminalWidth(terminalWidth);
        terminalGrid.print(new String[][] {{"one", "two", "three"}, {"four", "five", "six"}});
        assertEquals(
            "┌───┬───┬───┐" + System.lineSeparator() + 
            "│ o │ t │ t │" + System.lineSeparator() + 
            "│ n │ w │ h │" + System.lineSeparator() + 
            "│ e │ o │ r │" + System.lineSeparator() + 
            "│   │   │ e │" + System.lineSeparator() + 
            "│   │   │ e │" + System.lineSeparator() + 
            "├───┼───┼───┤" + System.lineSeparator() + 
            "│ f │ f │ s │" + System.lineSeparator() + 
            "│ o │ i │ i │" + System.lineSeparator() + 
            "│ u │ v │ x │" + System.lineSeparator() + 
            "│ r │ e │   │" + System.lineSeparator() + 
            "└───┴───┴───┘" + System.lineSeparator(), 
            output.toString());
    }
}
