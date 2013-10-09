/**
 * Copyright (c) 2013, ControlsFX
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *     * Neither the name of ControlsFX, any associated website, nor the
 * names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL CONTROLSFX BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.controlsfx.control.spreadsheet;

import impl.org.controlsfx.spreadsheet.CellView;
import impl.org.controlsfx.spreadsheet.GridRow;
import impl.org.controlsfx.spreadsheet.SpreadsheetGridView;
import impl.org.controlsfx.spreadsheet.GridViewSkin;
import impl.org.controlsfx.spreadsheet.SpreadsheetHandle;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Control;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SelectionModel;
import javafx.scene.control.Skin;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.Duration;


/**
 * The SpreadsheetView is a control similar to the JavaFX {@link TableView} control
 * but with different functionalities and use cases. The aim is to have a 
 * powerful grid where data can be written and retrieved.
 * 
 * <h3>Features </h3>
 * <ul>
 *   <li> Cells can span in row and in column.</li>
 *   <li> Rows can be fixed to the top of the {@link SpreadsheetView} so that they are always visible on screen.</li>
 *   <li> Columns can be fixed to the left of the {@link SpreadsheetView} so that they are always visible on screen. Only columns without any spanning cells can be fixed.</li>
 *   <li> A row header can be switched on in order to display the row number.</li>
 *   <li> Selection of several cells can be made with a click and drag.</li>
 *   <li> A copy/paste context menu is accessible with a right-click or the usual shortcuts.</li>
 * </ul>
 * 
 * <br/>
 *  
 * <h3>Fixing Rows and Columns </h3>
 * You can fix some rows and some columns by right-clicking on their header. A context menu will appear if it's possible to fix them.
 * The label will then be in italic to confirm that the fixing has been done properly.
 * Keep in mind that only columns without any spanning cells, and only rows without row-spanning cells can be fixed.
 * <br/>
 * You have also the possibility to fix them manually by adding and removing items from {@link #getFixedRows()} and {@link #getFixedColumns()}.
 * But you are strongly advised to check if it's possible to do so with {@link SpreadsheetColumn#isColumnFixable()} for the fixed columns and with
 * {@link #isRowFixable(int)} for the fixed rows. Calling those methods prior every move will ensure that no exception will be thrown. 
 * 
 * <br/><br/>
 * 
 * <h3>Copy pasting </h3>
 * You can copy every cell you want to paste it elsewhere. Be aware that only the value inside will be pasted, not the style nor the type. 
 * Thus the value you're trying to paste must be compatible with the {@link SpreadsheetCellType} of the receiving cell. Pasting a Double into a String will work but
 * the reverse operation will not. 
 * <br/>
 * A unique cell or a selection of several of them can be copied and pasted.
 * 
 * <br/><br/>
 * <h3>Code Samples</h3>
 * Just like the {@link TableView}, you instantiate the underlying model, a {@link Grid}.
 * You will create some ObservableList<{@link SpreadsheetCell}> filled with {@link SpreadsheetCell}. 
 * 
 * <br/><br/>
 * 
 * <pre>
 * int rowCount = 15;
 * int columnCount = 10;
 * Grid grid = new Grid(rowCount, columnCount);
 * 
 * ArrayList&lt;ObservableList&lt;DataCell&gt;&gt; rows = new ArrayList&lt;ObservableList&lt;DataCell&gt;&gt;(grid.getRowCount());
 * for (int row = 0; row < grid.getRowCount(); ++row) {
 *     final ObservableList&lt;DataCell&gt; ObservableList&lt;DataCell&gt; = new ObservableList&lt;DataCell&gt;(row, grid.getColumnCount());
 *     for (int column = 0; column < grid.getColumnCount(); ++column) {
 *         ObservableList&lt;DataCell&gt;.add(SpreadsheetCellType.STRING.createCell(row, column, 1, 1,""));
 *     }
 *     rows.add(ObservableList&lt;DataCell&gt;);
 * }
 * grid.setRows(rows);
 * </pre>
 * 
 * At that moment you can span some of the cells with the convenient method provided by the grid.
 * Then you just need to instantiate the SpreadsheetView.
 * <br/>
 * <h3>Visual: </h3>
 * <center><img src="spreadsheetView.png"></center>
 * 
 * @see SpreadsheetCell
 * @see SpreadsheetColumn
 * @see Grid
 */
public class SpreadsheetView extends Control {

    /***************************************************************************
     *                                                                         *
     * Static Fields                                                           *
     *                                                                         *
     **************************************************************************/
    
    /**
     * The SpanType describes in which state each cell can be.
     * When a spanning is occurring, one cell is becoming larger and the others are becoming invisible.
     * Thus, that particular cell is masking the others.
     * <br/><br/>
     * But the SpanType cannot be known in advance because it's evolving for each cell
     * during the lifetime of the {@link SpreadsheetView}. Suppose you have a cell spanning in row,
     * the first one is in a ROW_VISIBLE state, and all the other below are in a 
     * ROW_SPAN_INVISIBLE state. But if the user is scrolling down, the first will go out of sight.
     * At that moment, the second cell is switching from ROW_SPAN_INVISIBLE state to ROW_VISIBLE state. 
     * <br/>
     * <br/>
     * 
     * <center><img src="spanType.png"></center>
     *    Refer to {@link SpreadsheetView} for more information.
     */
    public static enum SpanType {
        
        /** Visible cell, can be a unique cell (no span) or the first one inside
         * a column spanning cell.  */
        NORMAL_CELL,
        
        /** Invisible cell because a cell in a NORMAL_CELL state on the left is covering it. */
        COLUMN_SPAN_INVISIBLE,
        
        /** Invisible cell because a cell in a ROW_VISIBLE state on the top is covering it. */
        ROW_SPAN_INVISIBLE,
        
        /** Visible Cell but has some cells below in a ROW_SPAN_INVISIBLE state. */
        ROW_VISIBLE,
        
        /** Invisible cell situated in diagonal of a cell in a ROW_VISIBLE state. */
        BOTH_INVISIBLE;
    }
    
    /***************************************************************************
     *                                                                         *
     * Private Fields                                                          *
     *                                                                         *
     **************************************************************************/

    private final SpreadsheetGridView cellsView;// The main cell container. 
    private Grid grid;
    private DataFormat fmt;
    private final ObservableList<Integer> fixedRows = FXCollections.observableArrayList();;
    private final ObservableList<SpreadsheetColumn<?>> fixedColumns = FXCollections.observableArrayList();;

    //Properties needed by the SpreadsheetView and managed by the skin (source is the VirtualFlow)
    private ObservableList<SpreadsheetColumn<?>> columns = FXCollections.observableArrayList();
    private Map<SpreadsheetCellType<?>, SpreadsheetCellEditor<?>> editors = new IdentityHashMap<>();
    private BitSet rowFix; // Compute if we can fix the rows or not.
    private ObservableList<SpreadsheetCell> modifiedCells = FXCollections.observableArrayList();
    // The handle that bridges with implementation.
    final SpreadsheetHandle handle = new SpreadsheetHandle() {
		@Override
		protected SpreadsheetView getView() {
			return SpreadsheetView.this;
		}

		@Override
		protected GridViewSkin getCellsViewSkin() {
			return SpreadsheetView.this.getCellsViewSkin();
		}

		@Override
		protected SpreadsheetGridView getGridView() {
			return SpreadsheetView.this.getCellsView();
		}
	};
    
    /**
     * @return the inner table view skin
     */
    final GridViewSkin getCellsViewSkin() {
        return (GridViewSkin) (cellsView.getSkin());
    }
    
    /**
     * @return the inner table view
     */
    final SpreadsheetGridView getCellsView() {
    	return cellsView;
    }
    
    /***************************************************************************
     *                                                                         *
     * Constructor                                                             *
     *                                                                         *
     **************************************************************************/
    
	/**
     * Creates a default SpreadsheetView control with no content and a Grid set to null. 
     */
    public SpreadsheetView() {
        this(null);
    }

    /**
     * Creates a SpreadsheetView control with the {@link Grid} specified. 
     * @param grid The Grid that contains the items to be rendered
     */
    public SpreadsheetView(final Grid grid){
        super();
        verifyGrid(grid);
        getStyleClass().add("SpreadsheetView");
        // anonymous skin
        setSkin(new Skin<SpreadsheetView>() {
        	@Override public Node getNode() {
        		return SpreadsheetView.this.getCellsView();
        	}

        	@Override public SpreadsheetView getSkinnable() {
        		return SpreadsheetView.this;
        	}

        	@Override public void dispose() {
        		// no-op
        	}
        });

        this.cellsView = new SpreadsheetGridView(handle);
        getChildren().add(cellsView);

        /**
         * Add a listener to the selection model in order to edit the spanned cells when clicked
         */
        SpreadsheetViewSelectionModel<?> selectionModel = new SpreadsheetViewSelectionModel<>(this);
        cellsView.setSelectionModel(selectionModel);
        selectionModel.setCellSelectionEnabled(true);
        selectionModel.setSelectionMode(SelectionMode.MULTIPLE);
        
        /**
         * Set the focus model to track keyboard change and redirect focus on spanned
         * cells
         */
        // We add a listener on the focus model in order to catch when we are on a hidden cell
        cellsView.getFocusModel().focusedCellProperty().addListener((ChangeListener<TablePosition>)(ChangeListener<?>) new FocusModelListener(this));

        /**
         * Keyboard action, maybe use an accelerator
         */
        this.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent arg0) {
				if(arg0.isShortcutDown() && arg0.getCode().compareTo(KeyCode.C) == 0)
					copyClipBoard();
				else if (arg0.isShortcutDown() && arg0.getCode().compareTo(KeyCode.V) == 0)
					pasteClipboard();
				//We want to edit if the user is on a cell and typing
				else if(!arg0.isShortcutDown() 
						&& !arg0.isAltDown()
						&& !arg0.isMetaDown()
						&& !arg0.isShiftDown()
						&& arg0.getCode().compareTo(KeyCode.ESCAPE) != 0){
					TablePosition<ObservableList<SpreadsheetCell>, ?> position = cellsView.getFocusModel().getFocusedCell();
					cellsView.edit(position.getRow(), position.getTableColumn());
				}
			}
		});
       initRowFix(grid);
       
       /**
        * ContextMenu handling.
        */
       this.contextMenuProperty().addListener(new ChangeListener<ContextMenu>(){
			@Override
			public void changed(ObservableValue<? extends ContextMenu> arg0,
					ContextMenu arg1, final ContextMenu arg2) {
				arg2.setOnShowing(new EventHandler<WindowEvent>() {
					@Override
					public void handle(WindowEvent arg0) {
						// We don't want to open a contextMenu when editing because editors
				        // have their own contextMenu
						if(getEditingCell() != null){
							// We're being reactive but we want to be pro-active so we may need a work-around.
							final Runnable r = new Runnable() {
		                        @Override
		                        public void run() {
		                        	arg2.hide();
		                        }
		                    };
		                    Platform.runLater(r);
						}
					}
				});
			}
       	});
       // The contextMenu creation must be on the JFX thread
       final Runnable r = new Runnable() {
           @Override
           public void run() {
           	setContextMenu(getSpreadsheetViewContextMenu());
           }
       };
       Platform.runLater(r);
       
       
        setGrid(grid);
        
        //Listeners
        fixedRows.addListener(fixedRowsListener); 
        fixedColumns.addListener(fixedColumnsListener);
        modifiedCells.addListener(modifiedCellsListener);
    }

    /***************************************************************************
     *                                                                         *
     * Public Methods                                                          *
     *                                                                         *
     **************************************************************************/

    /**
     * Return a {@link TablePosition} of cell being currently edited.
     * @return a {@link TablePosition} of cell being currently edited.
     */
    public TablePosition<ObservableList<SpreadsheetCell>, ?> getEditingCell(){
        return cellsView.getEditingCell();
    }
    
    /**
     * Return an unmodifiable observableList of the {@link SpreadsheetColumn} used.
     * @return An unmodifiable observableList.
     */
    public ObservableList<SpreadsheetColumn<?>> getColumns(){
		return FXCollections.unmodifiableObservableList(columns);
    }

    /**
     * Return the model Grid used by the SpreadsheetView
     * @return the model Grid used by the SpreadsheetView
     */
    public final Grid getGrid(){
        return grid;
    }
    
    //Read only because this functionality is not yet supported, RT-32673
    private final ReadOnlyBooleanProperty showColumnHeader = new SimpleBooleanProperty(true, "showColumnHeader",true);
    
    /**
     * Activate and deactivate the Column Header
     * @param b
     */
//    public final void setShowColumnHeader(final boolean b){
//        //TODO Need to do that again
//        //flow.recreateCells(); // Because otherwise we have at the bottom
//        showColumnHeader.setValue(b);
//    }
    
    /**
     * Return if the Column Header is showing.
     * Always true because it cannot be switched off yet.
     * @return a boolean telling if the column Header is being shown
     */
    public final boolean isShowColumnHeader() {
        return showColumnHeader.get();
    }

    /**
     * BooleanProperty associated with the column Header.
     * @return the BooleanProperty associated with the column Header.
     */
    public final ReadOnlyBooleanProperty showColumnHeaderProperty() {
        return showColumnHeader;
    }

    
    private final BooleanProperty showRowHeader = new SimpleBooleanProperty(true, "showRowHeader",true);
    
    /**
     * Activate and deactivate the Row Header.
     * @param b
     */
    public final void setShowRowHeader(final boolean b){
        showRowHeader.setValue(b);
    }
    
    /**
     * Return if the row Header is showing.
     * @return a boolean telling if the row Header is being shown
     */
    public final boolean isShowRowHeader() {
        return showRowHeader.get();
    }
    
    /**
     * BooleanProperty associated with the row Header.
     * @return the BooleanProperty associated with the row Header.
     */
    public final BooleanProperty showRowHeaderProperty() {
        return showRowHeader;
    }

    /**
     * You can fix or unfix a row by modifying this list.
     * Call {@link #isRowFixable(int)} before trying to fix a row.
     * See {@link SpreadsheetView} description for information.
     * @return an ObservableList of integer representing the fixedRows.
     */
    public ObservableList<Integer> getFixedRows() {
        return fixedRows;
    }
    
    /**
     * Indicate whether a row can be fixed or not.
     * Call that method before adding an item with {@link #getFixedRows()} .
     * @param row
     * @return true if the row can be fixed.
     */
    public boolean isRowFixable(int row){
    	return row<rowFix.size()?rowFix.get(row): false;
    }

    /**
     * You can fix or unfix a column by modifying this list.
     * Call {@link SpreadsheetColumn#isColumnFixable()} on the column before adding an item.
     * @return an ObservableList of the fixed columns. 
     */
    public ObservableList<SpreadsheetColumn<?>> getFixedColumns() {
        return fixedColumns;
    }

    /**
     * Indicate whether this column can be fixed or not.
     * If you have a {@link SpreadsheetColumn}, call {@link SpreadsheetColumn#isColumnFixable()} 
     * on it directly.
     * Call that method before adding an item with {@link #getFixedColumns()} .
     * @param columnIndex
     * @return true if the column if fixable
     */
    public boolean isColumnFixable(int columnIndex){
    	return columnIndex<getColumns().size()?getColumns().get(columnIndex).isColumnFixable():null;
    }
    
    /**
     * Return the selectionModel used by the SpreadsheetView.
     * @return {@link TableViewSelectionModel}
     */
    public TableViewSelectionModel<ObservableList<SpreadsheetCell>> getSelectionModel() {
        return cellsView.getSelectionModel();
    }

    /**
     * Return the editor associated with the CellType. 
     * (defined in {@link SpreadsheetCellType#createEditor(SpreadsheetView)}.
     * @param cellType
     * @return the editor associated with the CellType.
     */
    public SpreadsheetCellEditor<?> getEditor(SpreadsheetCellType<?> cellType) {
    	SpreadsheetCellEditor<?> cellEditor = editors.get(cellType);
    	if (cellEditor == null) {
    		cellEditor = cellType.createEditor(this);
    		editors.put(cellType, cellEditor);
    	}
		return cellEditor;
	}
    
    /**
     * Return a list of {@link SpreadsheetCell} that has been modified.
     * @return a list of {@link SpreadsheetCell} that has been modified.
     */
    public ObservableList<SpreadsheetCell> getModifiedCells(){
    	return modifiedCells;
    }
    
    /***************************************************************************
     *                                                                         *
     * Private/Protected Implementation                                        *
     *                                                                         *
     **************************************************************************/

    /**
     * Verify that the grid is well-formed.
     * Can be quite time-consuming I guess so I would like it not
     * to be compulsory..
     * @param grid
     */
    private void verifyGrid(Grid grid){
    	verifyColumnSpan(grid);
    }
    
    private void verifyColumnSpan(Grid grid){
    	for(int i=0; i< grid.getRows().size();++i){
    		ObservableList<SpreadsheetCell> row = grid.getRows().get(i);
    		int count = 0;
    		for(int j=0; j< row.size();++j){
    			if(row.get(j).getColumnSpan() == 1){
    				++count;
    			}else if(row.get(j).getColumnSpan() > 1){
    				++count;
    				SpreadsheetCell currentCell = row.get(j);
    				for(int k =j+1;k<currentCell.getColumn()+currentCell.getColumnSpan();++k){
    					if(!row.get(k).equals(currentCell)){
    						throw new IllegalStateException("\n At row "+i+" and column "+j
    								+ ": this cell is in the range of a columnSpan but is different. \n"
    								+ "Every cell in a range of a ColumnSpan must be of the same instance.");
    					}
    					++count;
    					++j;
    				}
    			}else{
    				throw new IllegalStateException("\n At row "+i+" and column "+j+": this cell has a negative columnSpan");
    			}
    		}
    		if(count != grid.getColumnCount()){
    			throw new IllegalStateException("The row"+i+" has a number of cells different of the columnCount declared in the grid.");
    		}
    	}
    }
    
    /**
     * Return the {@link SpanType} of a cell.
     * @param row
     * @param column
     * @return
     */
    private SpanType getSpanType(final int row, final int column) {
        Grid grid = getGrid();
        if (grid == null) {
            return SpanType.NORMAL_CELL;
        }
        return grid.getSpanType(this, row, column);
    }

    /**
     * Return a list of {@code ObservableList<SpreadsheetCell>} used by the SpreadsheetView.
     * @return
     */
    private ObservableList<ObservableList<SpreadsheetCell>> getItems() {
        return cellsView.getItems();
    }
    
    /**
     * Return the {@link GridRow} at the specified index
     * @param index
     * @return
     */
    private GridRow getNonFixedRow(int index){
        GridViewSkin skin = (GridViewSkin) cellsView.getSkin();
        return skin.getCell(fixedRows.size()+index);
    }

    /**
     * Indicate whether or not the row at the specified index is currently 
     * being displayed.
     * @param index
     * @return
     */
    private final boolean containsRow(int index){
        GridViewSkin skin = (GridViewSkin) cellsView.getSkin();
        int size = skin.getCellsSize();
        for (int i = 0 ; i < size; ++i) {
            if(skin.getCell(i).getIndex() == index)
                return true;
        }
        return false;
    }

    /**
     * Set a grid for the SpreadsheetView.
     * @param grid
     */
    private final void setGrid(Grid grid) {
        this.grid = grid;

        // TODO move into a property
        if(grid.getRows() != null){
            final ObservableList<ObservableList<SpreadsheetCell>> observableRows = (ObservableList<ObservableList<SpreadsheetCell>>)(Object)FXCollections.observableArrayList(grid.getRows());
            cellsView.getItems().clear();
            cellsView.setItems(observableRows);

            final int columnCount = grid.getColumnCount();
            columns.clear();
            for (int i = 0; i < columnCount; ++i) {
                final int col = i;

                final TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> column = new TableColumn<>(getEquivColumn(col));

                column.setEditable(true);
                // We don't want to sort the column
                column.setSortable(false);
                
                column.impl_setReorderable(false);
                
                // We assign a DataCell for each Cell needed (MODEL).
                column.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<SpreadsheetCell>, SpreadsheetCell>, ObservableValue<SpreadsheetCell>>() {
                    @Override
                    public ObservableValue<SpreadsheetCell> call(TableColumn.CellDataFeatures<ObservableList<SpreadsheetCell>, SpreadsheetCell> p) {
                        return new ReadOnlyObjectWrapper<SpreadsheetCell>(p.getValue().get(col));
                    }
                });
                final SpreadsheetView view = this;
                // We create a SpreadsheetCell for each DataCell in order to specify how to represent the DataCell(VIEW)
                column.setCellFactory(new Callback<TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell>, TableCell<ObservableList<SpreadsheetCell>, SpreadsheetCell>>() {
                    @Override
                    public TableCell<ObservableList<SpreadsheetCell>, SpreadsheetCell> call(TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> p) {
                        return new CellView(handle);
                    }
                });
                cellsView.getColumns().add(column);
                final SpreadsheetColumn<?> spreadsheetColumns = new SpreadsheetColumn(column,this, i);
                columns.add(spreadsheetColumns);
            }
        }
    }

    /**
     * Give the column letter in excel mode with the given number
     * @param number
     * @return
     */
    private final String getEquivColumn(int number){
        String converted = "";
        // Repeatedly divide the number by 26 and convert the
        // remainder into the appropriate letter.
        while (number >= 0)
        {
            final int remainder = number % 26;
            converted = (char)(remainder + 'A') + converted;
            number = number / 26 - 1;
        }

        return converted;
    }

    private void initRowFix(Grid grid){
    	ObservableList< ObservableList<SpreadsheetCell>> rows = grid.getRows();
		rowFix = new BitSet(rows.size());
		rows : for(int r = 0; r < rows.size(); ++r){
			ObservableList<SpreadsheetCell> row = rows.get(r);
			for(SpreadsheetCell cell: row){
				if(cell.getRowSpan() >1){
					continue rows;
				}
			}
			rowFix.set(r);
		}
	}
    /***************************************************************************
     * 						COPY / PASTE METHODS
     **************************************************************************/

    private void checkFormat(){
        if((fmt = DataFormat.lookupMimeType("shuttle"))== null){
            fmt = new DataFormat("shuttle");
        }
    }
    /**
     * Create a menu on rightClick with two options: Copy/Paste
     * @return
     */
    private ContextMenu getSpreadsheetViewContextMenu(){
        final ContextMenu contextMenu = new ContextMenu();
        
        final MenuItem item1 = new MenuItem("Copy");
        item1.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                copyClipBoard();
            }
        });
        final MenuItem item2 = new MenuItem("Paste");
        item2.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                pasteClipboard();
            }
        });
        contextMenu.getItems().addAll(item1, item2);
        return contextMenu;
    }

    /**
     * Put the current selection into the ClipBoard
     */
    private void copyClipBoard(){
        checkFormat();

        //		final ArrayList<ArrayList<DataCell>> temp = new ArrayList<>();
        final ArrayList<SpreadsheetCell> list = new ArrayList<SpreadsheetCell>();
        @SuppressWarnings("rawtypes")
        final ObservableList<TablePosition> posList = getSelectionModel().getSelectedCells();

        for (final TablePosition<?,?> p : posList) {
            list.add(getGrid().getRows().get(p.getRow()).get(p.getColumn()));
        }

        final ClipboardContent content = new ClipboardContent();
        content.put(fmt,list);
        Clipboard.getSystemClipboard().setContent(content);
    }

    /**
     * Try to paste the clipBoard to the specified position
     * Try to paste the current selection into the Grid. If the two contents are
     * not matchable, then it's not pasted.
     */
    private void pasteClipboard(){
        checkFormat();
        final Clipboard clipboard = Clipboard.getSystemClipboard();
        if(clipboard.getContent(fmt) != null){

            @SuppressWarnings("unchecked")
            final ArrayList<SpreadsheetCell> list = (ArrayList<SpreadsheetCell>) clipboard.getContent(fmt);
            //TODO algorithm very bad
            int minRow=grid.getRowCount();
            int minCol=grid.getColumnCount();
            int maxRow=0;
            int maxCol=0;
            for (final SpreadsheetCell p : list) {
                final int tempcol = p.getColumn();
                final int temprow = p.getRow();
                if(tempcol<minCol) {
                    minCol = tempcol;
                }
                if(tempcol>maxCol) {
                    maxCol = tempcol;
                }
                if(temprow<minRow) {
                    minRow = temprow;
                }
                if(temprow>maxRow) {
                    maxRow =temprow;
                }
            }

            final TablePosition<?,?> p = cellsView.getFocusModel().getFocusedCell();

            final int offsetRow = p.getRow()-minRow;
            final int offsetCol = p.getColumn()-minCol;
            int row;
            int column;


            for (final SpreadsheetCell row1 : list) {
                row = row1.getRow();
                column = row1.getColumn();
                if(row+offsetRow < getGrid().getRowCount() && column+offsetCol < getGrid().getColumnCount()
                        && row+offsetRow >= 0 && column+offsetCol >=0 ){
                    final SpanType type = getSpanType(row+offsetRow, column+offsetCol);
                    if(type == SpanType.NORMAL_CELL || type== SpanType.ROW_VISIBLE) {
                    	SpreadsheetCell cell = getGrid().getRows().get(row+offsetRow).get(column+offsetCol);
                    	Object item = cell.getItem();
                        boolean succeed =cell.match(row1);
                        if(succeed && !item.equals(cell.getItem()) && !getModifiedCells().contains(cell))
        					getModifiedCells().add(cell);
                    }
                }
            }
            
        //To be improved
        }else if(clipboard.hasString()){
        	final TablePosition<?,?> p = cellsView.getFocusModel().getFocusedCell();
        	
        	SpreadsheetCell stringCell = SpreadsheetCellType.STRING.createCell(0, 0, 1, 1, clipboard.getString());
        	getGrid().getRows().get(p.getRow()).get(p.getColumn()).match(stringCell);
        	
        }
    }


    /**************************************************************************
     * 
     * 						FOCUS MODEL
     * 
     * *************************************************************************/

    class FocusModelListener implements ChangeListener<TablePosition<ObservableList<SpreadsheetCell>,?>> {

        private final TableView.TableViewFocusModel<ObservableList<SpreadsheetCell>> tfm;

        public FocusModelListener(SpreadsheetView spreadsheetView) {
            tfm = cellsView.getFocusModel();
        }

        @Override
        public void changed(ObservableValue<? extends TablePosition<ObservableList<SpreadsheetCell>,?>> ov, final TablePosition<ObservableList<SpreadsheetCell>,?> t, final TablePosition<ObservableList<SpreadsheetCell>,?> t1) {
            final SpreadsheetView.SpanType spanType = getSpanType(t1.getRow(), t1.getColumn());
            switch (spanType) {
                case ROW_SPAN_INVISIBLE:
                    // If we notice that the new focused cell is the previous one, then it means that we were
                    //already on the cell and we wanted to go below.
                    if (!isPressed()
                            && t.getColumn() == t1.getColumn()
                            && t.getRow() == t1.getRow() - 1) {
                        final Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                tfm.focus(getTableRowSpan(t), t.getTableColumn());
                            }
                        };
                        Platform.runLater(r);

                    } else {
                        // If the current focused cell if hidden by row span, we go above
                        final Runnable r = new Runnable() {
                            @Override
                            public void run() {
                                tfm.focus(t1.getRow() - 1, t1.getTableColumn());
                            }
                        };
                        Platform.runLater(r);
                    }

                    break;
                case BOTH_INVISIBLE:
                    // If the current focused cell if hidden by a both (row and column) span, we go left-above
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            tfm.focus(t1.getRow() - 1, cellsView.getColumns().get(t1.getColumn() - 1));
                        }
                    };
                    Platform.runLater(r);
                    break;
                case COLUMN_SPAN_INVISIBLE:
                    // If we notice that the new focused cell is the previous one, then it means that we were
                    //already on the cell and we wanted to go right.
                    if (!isPressed()
                            && t.getColumn() == t1.getColumn() - 1
                            && t.getRow() == t1.getRow()) {

                        final Runnable r2 = new Runnable() {
                            @Override
                            public void run() {
                                tfm.focus(t.getRow(), getTableColumnSpan(t));
                            }
                        };
                        Platform.runLater(r2);
                    } else {
                        // If the current focused cell if hidden by column span, we go left

                        final Runnable r2 = new Runnable() {
                            @Override
                            public void run() {
                                tfm.focus(t1.getRow(), cellsView.getColumns().get(t1.getColumn() - 1));
                            }
                        };
                        Platform.runLater(r2);
                    }
                default:
                    break;
            }
        }
    }

    /**************************************************************************
     * 
     * 						SELECTION MODEL
     * 
     * *************************************************************************/

    /**
     * Return the TableColumn right after the current TablePosition (including
     * the ColumSpan to be on a visible Cell)
     *
     * @param t the current TablePosition
     * @return
     */
    private TableColumn<ObservableList<SpreadsheetCell>, ?> getTableColumnSpan(final TablePosition<?,?> t) {
        return cellsView.getVisibleLeafColumn(t.getColumn() + cellsView.getItems().get(t.getRow()).get(t.getColumn()).getColumnSpan());
    }

    /**
     * Return the TableColumn right after the current TablePosition (including
     * the ColumSpan to be on a visible Cell)
     *
     * @param t the current TablePosition
     * @return
     */
    private int getTableColumnSpanInt(final TablePosition<?,?> t) {
        return t.getColumn() + cellsView.getItems().get(t.getRow()).get(t.getColumn()).getColumnSpan();
    }

    /**
     * Return the Row number right after the current TablePosition (including
     * the RowSpan to be on a visible Cell)
     *
     * @param t
     * @param spreadsheetView
     * @return
     */
    private int getTableRowSpan(final TablePosition<?,?> t) {
        return cellsView.getItems().get(t.getRow()).get(t.getColumn()).getRowSpan()
                + cellsView.getItems().get(t.getRow()).get(t.getColumn()).getRow();
    }

    /**
     * For a position, return the Visible Cell associated with
     * It can be the top of the span cell if it's visible,
     * or it can be the first row visible if we have scrolled
     * @param row
     * @param column
     * @param col
     * @return
     */
    private TablePosition<ObservableList<SpreadsheetCell>,?> getVisibleCell(int row, TableColumn<ObservableList<SpreadsheetCell>, ?> column, int col) {
        final SpreadsheetView.SpanType spanType = getSpanType(row, col);
        switch (spanType) {
            case NORMAL_CELL:
            case ROW_VISIBLE:
                return new TablePosition<>(cellsView, row, column);
            case BOTH_INVISIBLE:
            case COLUMN_SPAN_INVISIBLE:
            case ROW_SPAN_INVISIBLE:
            default:
                final SpreadsheetCell cellSpan = cellsView.getItems().get(row).get(col);
                if (getCellsViewSkin().getCellsSize() != 0 && getNonFixedRow(0).getIndex() <= cellSpan.getRow()) {
                    return new TablePosition<>(cellsView, cellSpan.getRow(), cellsView.getColumns().get(cellSpan.getColumn()));

                } else { // If it's not, then it's the firstkey
                    return new TablePosition<>(cellsView, getNonFixedRow(0).getIndex(),cellsView.getColumns().get(cellSpan.getColumn()));
                }
        }
    }



    /**
     * A {@link SelectionModel} implementation for the {@link SpreadsheetView}
     * control.
     *
     * @param <S>
     */
    private class SpreadsheetViewSelectionModel<S> extends TableView.TableViewSelectionModel<ObservableList<SpreadsheetCell>> {

        private boolean ctrl = false;   // Register state of 'ctrl' key
        private boolean shift = false;  // Register state of 'shift' key
        private boolean key = false;    // Register if we last touch the keyboard or the mouse
        private boolean drag = false;	//register if we are dragging (no edition)
        private MouseEvent mouseEvent;

        /**
         * Make the tableView move when selection operating outside bounds
         */
        private final Timeline timer = new Timeline(new KeyFrame(Duration.millis(100), new EventHandler<ActionEvent>() {
            @Override public void handle(ActionEvent event) {
                GridViewSkin skin = (GridViewSkin)getCellsViewSkin();
                if (mouseEvent != null && !cellsView.contains(mouseEvent.getX(), mouseEvent.getY())) {
                	double sceneX = mouseEvent.getSceneX();
                	double sceneY = mouseEvent.getSceneY();
                	double layoutX =cellsView.getLayoutX();
                	double layoutY = cellsView.getLayoutY();
                	double layoutXMax = layoutX+cellsView.getWidth();
                	double layoutYMax = layoutY+cellsView.getHeight();
                	
                	if(sceneX > layoutXMax)
                		skin.getHBar().increment();
                	else if(sceneX < layoutX)
                		skin.getHBar().decrement();
                	if(sceneY > layoutYMax)
                		skin.getVBar().increment();
                	else if(sceneY < layoutY)
                		skin.getVBar().decrement();
                }
            }
        }));

        /**
         * When the drag is over, we remove the listener and stop the timer
         */
        private final EventHandler<MouseEvent> dragDoneHandler = new EventHandler<MouseEvent>() {
            @Override public void handle(MouseEvent arg0) {
                drag = false;
                timer.stop();
                removeEventHandler(MouseEvent.MOUSE_RELEASED, this);
            }
        };

        
        
        /***********************************************************************
         *                                                                     
         * Constructors 
         * 
         **********************************************************************/
        
        public SpreadsheetViewSelectionModel(SpreadsheetView spreadsheetView) {
            super(spreadsheetView.cellsView);
            final SpreadsheetGridView cellsView = spreadsheetView.cellsView;
            cellsView.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent t) {
                    key = true;
                    ctrl = t.isControlDown();
                    shift = t.isShiftDown();
                }
            });

            cellsView.setOnMousePressed(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent t) {
                    key = false;
                    ctrl = t.isControlDown();
                    shift = t.isShiftDown();
                }
            });
            cellsView.setOnDragDetected(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                	cellsView.addEventHandler(MouseEvent.MOUSE_RELEASED, dragDoneHandler);
                    drag = true;
                    timer.setCycleCount(Timeline.INDEFINITE);
                    timer.play();
                }
            });

            cellsView.setOnMouseDragged(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent e) {
                    mouseEvent = e;
                }
            });
            selectedCells = FXCollections.<TablePosition<ObservableList<SpreadsheetCell>,?>>observableArrayList();
        }

     
       

        /**
         * *********************************************************************
         *                                                                     *
         * Public selection API * *
         * ********************************************************************
         */
        private TablePosition<ObservableList<SpreadsheetCell>, ?> old = null;

        @Override
        public void select(int row, TableColumn<ObservableList<SpreadsheetCell>, ?> column) {

            if (row < 0 || row >= getItemCount()) {
                return;
            }

            // if I'm in cell selection mode but the column is null, I don't want
            // to select the whole row instead...
            if (isCellSelectionEnabled() && column == null) {
                return;
            }
            //Variable we need for algorithm
            TablePosition<ObservableList<SpreadsheetCell>, ?> posFinal = new TablePosition<>(getTableView(), row, column);

            final SpreadsheetView.SpanType spanType = getSpanType(row, posFinal.getColumn());
 
            /**
             * We check if we are on covered cell. If so we have the
             * algorithm of the focus model to give the selection to the right cell.
             *
             */
            switch (spanType) {
                case ROW_SPAN_INVISIBLE:
                    // If we notice that the new selected cell is the previous one, then it means that we were
                    //already on the cell and we wanted to go below.
                    // We make sure that old is not null, and that the move is initiated by keyboard.
                    //Because if it's a click, then we just want to go on the clicked cell (not below)
                    if (old != null && key && !shift
                    && old.getColumn() == posFinal.getColumn()
                    && old.getRow() == posFinal.getRow() - 1) {
                        posFinal = getVisibleCell(getTableRowSpan(old), old.getTableColumn(), old.getColumn());
                    } else {
                        // If the current selected cell if hidden by row span, we go above
                        posFinal = getVisibleCell(row, column, posFinal.getColumn());
                    }
                    break;
                case BOTH_INVISIBLE:
                    // If the current selected cell if hidden by a both (row and column) span, we go left-above
                    posFinal = getVisibleCell(row, column, posFinal.getColumn());
                    break;
                case COLUMN_SPAN_INVISIBLE:
                    // If we notice that the new selected cell is the previous one, then it means that we were
                    //already on the cell and we wanted to go right.
                    if (old != null && key && !shift
                    && old.getColumn() == posFinal.getColumn() - 1
                    && old.getRow() == posFinal.getRow()) {
                        posFinal = getVisibleCell(old.getRow(), getTableColumnSpan(old), getTableColumnSpanInt(old));
                    } else {
                        // If the current selected cell if hidden by column span, we go left
                        posFinal = getVisibleCell(row, column, posFinal.getColumn());
                    }
                default:
                    break;
            }

            //This is to handle edition
            if (posFinal.equals(old) && !ctrl && !shift && !drag) {
                // If we are on an Invisible row or both (in diagonal), we need to force the edition
                if (spanType == SpreadsheetView.SpanType.ROW_SPAN_INVISIBLE || spanType == SpreadsheetView.SpanType.BOTH_INVISIBLE) {
                    final TablePosition<ObservableList<SpreadsheetCell>, ?> FinalPos = new TablePosition<>(cellsView, posFinal.getRow(), posFinal.getTableColumn());
                    final Runnable r = new Runnable() {
                        @Override
                        public void run() {
                            cellsView.edit(FinalPos.getRow(), FinalPos.getTableColumn());
                        }
                    };
                    Platform.runLater(r);
                }
            }
            old = posFinal;

            if (!getSelectedCells().contains(posFinal)) {
            	getSelectedCells().add(posFinal);
            }

            updateScroll(posFinal);
            addSelectedRowsAndColumns(posFinal);

            setSelectedIndex(posFinal.getRow());
            setSelectedItem(getModelItem(posFinal.getRow()));
            if (getTableView().getFocusModel() == null) {
                return;
            }

            getTableView().getFocusModel().focus(posFinal.getRow(), posFinal.getTableColumn());
        }

        private void updateScroll(TablePosition<ObservableList<SpreadsheetCell>, ?> posFinal) {
            //We try to make visible the rows that may be hidden by Fixed rows
            // We don't want to do any scroll behavior when dragging
            if(!drag && getCellsViewSkin().getCellsSize() != 0 && getNonFixedRow(0).getIndex()> posFinal.getRow() && !getFixedRows().contains(posFinal.getRow())) {
                cellsView.scrollTo(posFinal.getRow());
            }

        }


        @Override
        public void clearSelection(int row, TableColumn<ObservableList<SpreadsheetCell>, ?> column) {
            final TablePosition<ObservableList<SpreadsheetCell>, ?> tp = new TablePosition<>(getTableView(), row, column);
            if (isSelectedRange(row, column, tp.getColumn()) != null) {
                final TablePosition<ObservableList<SpreadsheetCell>, ?> tp1 = isSelectedRange(row, column, tp.getColumn());
                getSelectedCells().remove(tp1);
                removeSelectedRowsAndColumns(tp1);
                focus(tp1.getRow());
            } else {

                final boolean csMode = isCellSelectionEnabled();

                for (final TablePosition<ObservableList<SpreadsheetCell>, ?> pos : getSelectedCells()) {
                    if (!csMode && pos.getRow() == row || csMode && pos.equals(tp)) {
                    	getSelectedCells().remove(pos);
                        removeSelectedRowsAndColumns(pos);

                        // give focus to this cell index
                        focus(row);

                        return;
                    }
                }
            }
        }


        @Override
        public boolean isSelected(int row, TableColumn<ObservableList<SpreadsheetCell>, ?> column) {
            // When in cell selection mode, we currently do NOT support selecting
            // entire rows, so a isSelected(row, null)
            // should always return false.

            if (isCellSelectionEnabled() && column == null || row <0) {
                return false;
            }
            final TablePosition<ObservableList<SpreadsheetCell>, ?> tp1 = new TablePosition<>(getTableView(), row, column);
            if (isSelectedRange(row, column, tp1.getColumn()) != null) {
                return true;
            } else {
                return false;
            }
        }

        /**
         * Return the tablePosition of a selected cell inside a spanned cell if any.
         *
         * @param row
         * @param column
         * @param col
         * @return
         */
        public TablePosition<ObservableList<SpreadsheetCell>, ?> isSelectedRange(int row, TableColumn<ObservableList<SpreadsheetCell>, ?> column, int col) {

            if (isCellSelectionEnabled() && column == null && row >=0) {
                return null;
            }

            final SpreadsheetCell cellSpan = cellsView.getItems().get(row).get(col);
            final int infRow = cellSpan.getRow();
            final int supRow = infRow + cellSpan.getRowSpan();

            final int infCol = cellSpan.getColumn();
            final int supCol = infCol + cellSpan.getColumnSpan();

            for (final TablePosition<ObservableList<SpreadsheetCell>, ?> tp : getSelectedCells()) {
                //boolean columnMatch = (column != null && column.equals(tp.getTableColumn()));

                if (tp.getRow() >= infRow && tp.getRow() < supRow && tp.getColumn() >= infCol && tp.getColumn() < supCol) {
                    return tp;
                }
            }
            return null;
        }


        /**
         * *********************************************************************
         *                                                                     *
         * Support code * *
         * ********************************************************************
         */

        private void addSelectedRowsAndColumns(TablePosition<?, ?> t){
            final SpreadsheetCell cell = cellsView.getItems().get(t.getRow()).get(t.getColumn());
            for(int i=cell.getRow();i<cell.getRowSpan()+cell.getRow();++i){
            	getSpreadsheetViewSkin().getSelectedRows().add(i);
                for(int j=cell.getColumn();j<cell.getColumnSpan()+cell.getColumn();++j){
                	getSpreadsheetViewSkin().getSelectedColumns().add(j);
                }
            }
        }
        private void removeSelectedRowsAndColumns(TablePosition<?, ?> t){
            final SpreadsheetCell cell = cellsView.getItems().get(t.getRow()).get(t.getColumn());
            for(int i=cell.getRow();i<cell.getRowSpan()+cell.getRow();++i){
            	getSpreadsheetViewSkin().getSelectedRows().remove(Integer.valueOf(i));
                for(int j=cell.getColumn();j<cell.getColumnSpan()+cell.getColumn();++j){
                	getSpreadsheetViewSkin().getSelectedColumns().remove(Integer.valueOf(j));
                }
            }
        }

		@Override
		public void clearAndSelect(int arg0,
				TableColumn<ObservableList<SpreadsheetCell>, ?> arg1) {
			quietClearSelection();
            select(arg0, arg1);
			
		}

		@Override
		public ObservableList<TablePosition> getSelectedCells() {
			 return (ObservableList<TablePosition>)(Object)selectedCells;
		}

		// the only 'proper' internal observableArrayList, selectedItems and selectedIndices
        // are both 'read-only and unbacked'.
        private final ObservableList<TablePosition<ObservableList<SpreadsheetCell>, ?>> selectedCells;
        
		@Override
		public void selectAboveCell() {
			final TablePosition<ObservableList<SpreadsheetCell>, ?> pos = getFocusedCell();
            if (pos.getRow() == -1) {
                select(getItemCount() - 1);
            } else if (pos.getRow() > 0) {
                select(pos.getRow() - 1, pos.getTableColumn());
            }
			
		}

		@Override
		public void selectBelowCell() {
			final TablePosition<ObservableList<SpreadsheetCell>, ?> pos = getFocusedCell();

            if (pos.getRow() == -1) {
                select(0);
            } else if (pos.getRow() < getItemCount() - 1) {
                select(pos.getRow() + 1, pos.getTableColumn());
            }
			
		}

		@Override
		public void selectLeftCell() {
			 if (!isCellSelectionEnabled()) {
	                return;
	            }

	            final TablePosition<ObservableList<SpreadsheetCell>, ?> pos = getFocusedCell();
	            if (pos.getColumn() - 1 >= 0) {
	                select(pos.getRow(), getTableColumn(pos.getTableColumn(), -1));
	            }
			
		}

		@Override
		public void selectRightCell() {
			 if (!isCellSelectionEnabled()) {
	                return;
	            }

	            final TablePosition<ObservableList<SpreadsheetCell>, ?> pos = getFocusedCell();
	            if (pos.getColumn() + 1 < getTableView().getVisibleLeafColumns().size()) {
	                select(pos.getRow(), getTableColumn(pos.getTableColumn(), 1));
	            }
			
		}
		
		@Override
        public void clearSelection() {
			setSelectedIndex(-1);
            setSelectedItem(getModelItem(-1));
            focus(-1);
            quietClearSelection();
        }
		
		private void quietClearSelection() {
            getSelectedCells().clear();
            getSpreadsheetViewSkin().getSelectedRows().clear();
            getSpreadsheetViewSkin().getSelectedColumns().clear();
        }

		private TablePosition<ObservableList<SpreadsheetCell>, ?> getFocusedCell() {
            if (getTableView().getFocusModel() == null) {
                return new TablePosition<>(getTableView(), -1, null);
            }
            return cellsView.getFocusModel().getFocusedCell();
        }
		private TableColumn<ObservableList<SpreadsheetCell>, ?> getTableColumn(TableColumn<ObservableList<SpreadsheetCell>, ?> column, int offset) {
            final int columnIndex = getTableView().getVisibleLeafIndex(column);
            final int newColumnIndex = columnIndex + offset;
            return getTableView().getVisibleLeafColumn(newColumnIndex);
        }
		private GridViewSkin getSpreadsheetViewSkin(){
			return (GridViewSkin) getCellsViewSkin();
		}
    }
    
    /**
     * *********************************************************************
     *                                                                     *
     * private listeners
     * ********************************************************************
     */
    
    private ListChangeListener<Integer> fixedRowsListener = new ListChangeListener<Integer>() {
        @Override public void onChanged(Change<? extends Integer> c) {
            while (c.next()) {
                if (c.wasAdded()) {
                    List<? extends Integer> newRows = c.getAddedSubList();
                    for (int row : newRows) {
                        if(! isRowFixable(row)){
                            throw new IllegalArgumentException(computeReason(row)); 
                        }
                    }
                    FXCollections.sort(fixedRows);
                }
            }
        }
        
        private String computeReason(Integer element){
            String reason = "\n This row cannot be fixed.";
            for(SpreadsheetCell cell: getGrid().getRows().get(element)){
                if(cell.getRowSpan() >1){
                    reason+= "The cell situated at line "+cell.getRow()+" and column "+cell.getColumn()
                            +"\n has a rowSpan of "+cell.getRowSpan()+", it must be 1.";
                    return reason;
                }
            }
            return reason;
        }
    };
    
    private ListChangeListener<SpreadsheetColumn<?>> fixedColumnsListener = new ListChangeListener<SpreadsheetColumn<?>>() {
        @Override public void onChanged(Change<? extends SpreadsheetColumn<?>> c) {
            while (c.next()) {
                if (c.wasAdded()) {
                    List<? extends SpreadsheetColumn<?>> newRows = c.getAddedSubList();
                    for (SpreadsheetColumn<?> row : newRows) {
                        if(! row.isColumnFixable()){
                            throw new IllegalArgumentException(computeReason(row)); 
                        }
                    }
                    FXCollections.sort(fixedRows);
                }
            }
        }
        
        private String computeReason(SpreadsheetColumn<?> element){
    		int indexColumn = getColumns().indexOf(element);
    	
    		String reason = "\n This column cannot be fixed.";
    		for (ObservableList<SpreadsheetCell> row : getGrid().getRows()) {
    			int columnSpan = row.get(indexColumn).getColumnSpan();
    			if(columnSpan >1 || row.get(indexColumn).getRowSpan()>1){
    				reason+= "The cell situated at line "+row.get(indexColumn).getRow()+" and column "+indexColumn
							+"\n has a rowSpan or a ColumnSpan superior to 1, it must be 1.";
    				return reason;
    			}
    		}
    		return reason;
    	}
    };
    
    private ListChangeListener<SpreadsheetCell> modifiedCellsListener = new ListChangeListener<SpreadsheetCell>(){

		@Override
		public void onChanged(Change<? extends SpreadsheetCell> arg0) {
			while (arg0.next()) {
                if (arg0.wasAdded()) {
                	 List<? extends SpreadsheetCell> newRows = arg0.getAddedSubList();
                     for (SpreadsheetCell cell : newRows) {
                    	 if(!cell.getStyleClass().contains("modified"))
                             cell.getStyleClass().add("modified");
                     }
                }
			}
		}
    };
}



