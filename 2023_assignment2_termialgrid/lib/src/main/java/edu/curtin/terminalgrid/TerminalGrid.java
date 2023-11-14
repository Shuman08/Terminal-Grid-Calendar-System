package edu.curtin.terminalgrid;

import org.apache.commons.text.WordUtils;
import java.util.function.Consumer;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TerminalGrid {


    private enum Box
    { 
        LEFT, MID_VERTICAL, RIGHT, TOP, MID_HORIZONTAL, BOTTOM, TOP_LEFT, TOP_RIGHT, 
        BOTTOM_LEFT, BOTTOM_RIGHT, MID_TOP, MID_BOTTOM, MID_LEFT, MID_RIGHT, MID
    }
    
    public static class BoxChars 
    { 
        private String[] chars;
        public BoxChars(String... chars)
        {
            this.chars = chars;
        }
        
        public String get(Box b)
        {
            return chars[b.ordinal()];
        }
    }

    public static final BoxChars UNICODE_BOX_CHARS = new BoxChars(
        /*LEFT:*/ "\u2502 ", /*MID_VERTICAL:*/ " \u2502 ", /*RIGHT*/" \u2502", 
        /*TOP:*/ "\u2500", /*MID_HORIZONTAL:*/ "\u2500", /*BOTTOM:*/ "\u2500", 
        /*TOP_LEFT:*/ "\u250c\u2500", /*TOP_RIGHT:*/"\u2500\u2510", 
        /*BOTTOM_LEFT:*/ "\u2514\u2500", /*BOTTOM_RIGHT:*/ "\u2500\u2518", 
        /*MID_TOP:*/ "\u2500\u252c\u2500", /*MID_BOTTOM:*/ "\u2500\u2534\u2500", 
        /*MID_LEFT:*/ "\u251c\u2500", /*MID_RIGHT:*/ "\u2500\u2524", 
        /*MID:*/ "\u2500\u253c\u2500");
    public static final BoxChars ASCII_BOX_CHARS = new BoxChars(
        "| ", " | ", " |", "-", "-", "-", "/-", "-\\", "\\-", "-/", "-+-", "-+-", "+-", "-+", "-+-");

    private PrintStream out;
    private int terminalWidth;
    private BoxChars boxChars = UNICODE_BOX_CHARS;

    
    public static TerminalGrid create() {
        int terminalWidth = 80; // Set the default terminal width here
        return new TerminalGrid(System.out, terminalWidth);
    }

    public TerminalGrid(PrintStream out, int terminalWidth) {
        this.out = out;
        this.terminalWidth = terminalWidth;
    }

    public void setTerminalWidth(int terminalWidth) {
        this.terminalWidth = terminalWidth;
    }

    public void setBoxChars(BoxChars boxChars) {
        this.boxChars = boxChars;
    }

    public void setCharset(String charsetName) {
        try {
            this.out.flush();
            this.out.close();
            this.out = new PrintStream(System.out, true, charsetName);
        } catch (UnsupportedEncodingException e) {
           System.out.println(e.getMessage());
        }
    }

    public void print(List<List<String>> listGrid) {
        print(toArrayGrid(listGrid), null, null);
    }

    public void print(List<List<String>> listGrid, List<String> rowHeadings, List<String> colHeadings) {
        print(toArrayGrid(listGrid), rowHeadings.toArray(new String[0]), colHeadings.toArray(new String[0]));
    }

    private static String[][] toArrayGrid(List<List<String>> listGrid) {
        String[][] arrayGrid = new String[listGrid.size()][];
        for (int i = 0; i < listGrid.size(); i++) {
            List<String> row = listGrid.get(i);
            arrayGrid[i] = row.toArray(new String[0]);
        }
        return arrayGrid;
    }

    public void print(String[][] grid) {
        print(grid, null, null);
    }

    public void print(String[][] grid, String[] rowHeadings, String[] colHeadings) {
        int nRows = grid.length;
        if (nRows == 0) {
            throw new IllegalArgumentException("The grid must have at least one row");
        }

        int nColumns = grid[0].length;
        for (int i = 1; i < nRows; i++) {
            if (grid[i].length != nColumns) {
                throw new IllegalArgumentException("All grid rows must have the same number of columns");
            }
        }

        if (nColumns == 0) {
            throw new IllegalArgumentException("The grid must have at least one column");
        }

        if (rowHeadings != null && rowHeadings.length != nRows) {
            throw new IllegalArgumentException("If provided, rowHeadings.length must match the number of rows (" + nRows + ")");
        }

        if (colHeadings != null && colHeadings.length != nColumns) {
            throw new IllegalArgumentException("If provided, colHeadings.length must match the number of columns (" + nColumns + ")");
        }

        // Handle row/column headings by incorporating them directly into the 2D array.
        grid = combineGridHeadings(grid, rowHeadings, colHeadings);
       // nRows = grid.length;
        nColumns = grid[0].length;
        boolean useRowHeadings = rowHeadings != null;
        boolean useColHeadings = colHeadings != null;

        int[] columnWidths = new int[nColumns];

        for (String[] row : grid) {
            for (int colN = 0; colN < nColumns; colN++) {
                for (String line : row[colN].split("\n")) {
                    int len = line.length();
                    if (columnWidths[colN] < len) {
                        columnWidths[colN] = len;
                    }
                }
            }
        }

        int totalWidth = boxChars.get(Box.LEFT).length()
                + boxChars.get(Box.RIGHT).length()
                + ((nColumns - 1) * boxChars.get(Box.MID_VERTICAL).length());
        for (int cw : columnWidths) {
            totalWidth += cw;
        }

        if (totalWidth >= terminalWidth) {
            Integer[] sortedColumns = new Integer[nColumns];
            for (int i = 0; i < nColumns; i++) {
                sortedColumns[i] = i;
            }
            Arrays.sort(sortedColumns, (index1, index2) -> columnWidths[index2] - columnWidths[index1]);

            int i = 0;
            while (totalWidth > terminalWidth && columnWidths[sortedColumns[i]] > 1) {
                columnWidths[sortedColumns[i]]--;
                totalWidth--;
                i++;
                if (i >= nColumns || columnWidths[sortedColumns[i]] <= columnWidths[sortedColumns[0]]) {
                    i = 0;
                }
            }
        }

        int[] rowHeights = new int[grid.length];
        String[][][] formattedGrid = new String[grid.length][nColumns][];
        Arrays.fill(rowHeights, 1);
        for (int rowN = 0; rowN < grid.length; rowN++) {
            for (int colN = 0; colN < nColumns; colN++) {
                var lines = new ArrayList<String>();
                int colWidth = columnWidths[colN];
                forEachLine(grid[rowN][colN], unwrappedLine -> {
                    forEachLine(WordUtils.wrap(unwrappedLine, colWidth, "\n", true), lines::add);
                });
                formattedGrid[rowN][colN] = lines.toArray(String[]::new);

                int height = lines.size();
                if (rowHeights[rowN] < height) {
                    rowHeights[rowN] = height;
                }
            }
        }
        int topRow = 0;
        if (useColHeadings) {
            printRow(formattedGrid[0], useRowHeadings, true, rowHeights[0], columnWidths);
            topRow++;
        }

        printRowDivider(columnWidths, useRowHeadings, Box.TOP_LEFT, Box.TOP, Box.MID_TOP, Box.TOP_RIGHT);
        printRow(formattedGrid[topRow], useRowHeadings, false, rowHeights[topRow], columnWidths);

        for (int rowN = topRow + 1; rowN < grid.length; rowN++) {
            printRowDivider(columnWidths, useRowHeadings, Box.MID_LEFT, Box.MID_HORIZONTAL, Box.MID, Box.MID_RIGHT);
            printRow(formattedGrid[rowN], useRowHeadings, false, rowHeights[rowN], columnWidths);
        }
        printRowDivider(columnWidths, useRowHeadings, Box.BOTTOM_LEFT, Box.BOTTOM, Box.MID_BOTTOM, Box.BOTTOM_RIGHT);
    }

    private String[][] combineGridHeadings(String[][] origGrid, String[] rowHeadings, String[] colHeadings) {
        if (rowHeadings == null && colHeadings == null) {
            return origGrid;
        }

        var rowOffset = (colHeadings != null) ? 1 : 0;
        var colOffset = (rowHeadings != null) ? 1 : 0;
        int nRows = origGrid.length + rowOffset;
        int nCols = origGrid[0].length + colOffset;

        var newGrid = new String[nRows][nCols];
        for (int rowN = rowOffset; rowN < nRows; rowN++) {
            for (int colN = colOffset; colN < nCols; colN++) {
                newGrid[rowN][colN] = origGrid[rowN - rowOffset][colN - colOffset];
            }
        }

        if (rowHeadings != null) {
            for (int rowN = rowOffset; rowN < nRows; rowN++) {
                newGrid[rowN][0] = rowHeadings[rowN - rowOffset];
            }
        }

        if (colHeadings != null) {
            for (int colN = colOffset; colN < nCols; colN++) {
                newGrid[0][colN] = colHeadings[colN - colOffset];
            }
        }

        if (rowHeadings != null && colHeadings != null) {
            newGrid[0][0] = "";
        }

        return newGrid;
    }

    private void printRowDivider(int[] columnWidths, boolean useRowHeadings, Box leftChar, Box horizChar, Box midChar, Box rightChar) {
        int leftCol = 0;
        if (useRowHeadings) {
            out.print(" ".repeat(columnWidths[0] + 1));
            leftCol++;
        }
        out.print(boxChars.get(leftChar));
        out.print(boxChars.get(horizChar).repeat(columnWidths[leftCol]));
        for (int colN = leftCol + 1; colN < columnWidths.length; colN++) {
            out.print(boxChars.get(midChar));
            out.print(boxChars.get(horizChar).repeat(columnWidths[colN]));
        }
        out.println(boxChars.get(rightChar));
    }

    private void printRow(String[][] formattedRow, boolean useRowHeadings, boolean isColHeading, int rowHeight, int[] columnWidths) {
        String left = boxChars.get(Box.LEFT);
        String vert = boxChars.get(Box.MID_VERTICAL);
        String right = boxChars.get(Box.RIGHT);
        if (isColHeading) {
            left = " ".repeat(left.length());
            vert = " ".repeat(vert.length());
            right = " ".repeat(right.length());
        }

        for (int lineN = 0; lineN < rowHeight; lineN++) {
            int leftCol = 0;
            if (useRowHeadings) {
                printCell(formattedRow[0], lineN, columnWidths[0]);
                out.print(" ");
                leftCol++;
            }
            out.print(left);

            printCell(formattedRow[leftCol], lineN, columnWidths[leftCol]);
            for (int colN = leftCol + 1; colN < formattedRow.length; colN++) {
                out.print(vert);
                printCell(formattedRow[colN], lineN, columnWidths[colN]);
            }

            out.println(right);
        }
    }

    private void printCell(String[] cellLines, int lineN, int columnWidth) {
        String text = "";
        if (lineN < cellLines.length) {
            text = cellLines[lineN];
        }
        out.print(text);
        out.print(" ".repeat(columnWidth - text.length()));
    }

    private void forEachLine(String s, Consumer<String> callback) {
        int startIndex = 0, endIndex;
        while ((endIndex = s.indexOf('\n', startIndex)) != -1) {
            callback.accept(s.substring(startIndex, endIndex));
            startIndex = endIndex + 1;
        }
        callback.accept(s.substring(startIndex));
    }
}

