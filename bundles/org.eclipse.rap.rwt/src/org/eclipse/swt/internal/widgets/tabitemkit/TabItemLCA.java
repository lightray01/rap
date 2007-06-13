/*******************************************************************************
 * Copyright (c) 2002-2006 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 ******************************************************************************/

package org.eclipse.swt.internal.widgets.tabitemkit;

import java.io.IOException;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.internal.widgets.ItemLCAUtil;
import org.eclipse.swt.internal.widgets.Props;
import org.eclipse.swt.lifecycle.*;
import org.eclipse.swt.widgets.*;


public class TabItemLCA extends AbstractWidgetLCA {

  private static final String PROP_CHECKED = "checked";

  private final static JSListenerInfo JS_LISTENER_INFO
    = new JSListenerInfo( "changeChecked",
                          "org.eclipse.swt.TabUtil.tabSelected",
                          JSListenerType.STATE_AND_ACTION );


  public void preserveValues( final Widget widget ) {
    TabItem tabItem = ( TabItem )widget;
    ItemLCAUtil.preserve( tabItem );
    IWidgetAdapter adapter = WidgetUtil.getAdapter( widget );
    adapter.preserve( PROP_CHECKED, Boolean.valueOf( isChecked( tabItem ) ) );
    // preserve the listener state of the parent tabfolder here, since the
    // javascript handling is added to the clientside tab buttons and therefore
    // the jswriter will check the preserved state of the tabitem...
    TabFolder parent = tabItem.getParent();
    boolean hasListeners = SelectionEvent.hasListener( parent );
    adapter.preserve( Props.SELECTION_LISTENERS,
                      Boolean.valueOf( hasListeners ) );
  }
  
  public void readData( final Widget widget ) {
    String value = WidgetLCAUtil.readPropertyValue( widget, PROP_CHECKED );
    if( value != null && Boolean.valueOf( value ).booleanValue() ) {
      TabItem tabItem = ( TabItem )widget;
      TabFolder parent = tabItem.getParent();
      TabItem[] items = parent.getItems();
      for( int i = 0; i < items.length; i++ ) {
        if( items[ i ] == tabItem ) {
          TabItem[] oldSelection = parent.getSelection();
          if( oldSelection.length == 1 ) {
            IWidgetAdapter adapter = WidgetUtil.getAdapter( oldSelection[ 0 ] );
            adapter.preserve( PROP_CHECKED, Boolean.FALSE );
          }
          IWidgetAdapter adapter = WidgetUtil.getAdapter( tabItem );
          adapter.preserve( PROP_CHECKED, Boolean.TRUE );
          // TODO: [fappel] see comment in TabFolderLCA.processAction
          parent.setSelection( i );
        }
      }
    }
  }
  
  public void renderInitialization( final Widget widget ) throws IOException {
    TabItem tabItem = ( TabItem )widget;
    JSWriter writer = JSWriter.getWriterFor( widget );
    Object[] args = new Object[] { 
      WidgetUtil.getId( tabItem ), 
      WidgetUtil.getId( tabItem.getParent() )
    };
    writer.callStatic( "org.eclipse.swt.TabUtil.createTabItem", args );
    setJSParent( tabItem );
  }

  public void renderChanges( final Widget widget ) throws IOException {
    TabItem tabItem = ( TabItem )widget;
    JSWriter writer = JSWriter.getWriterFor( tabItem );
    ItemLCAUtil.writeChanges( tabItem );
    writeCheckedState( tabItem );
    writer.updateListener( JS_LISTENER_INFO, 
                           Props.SELECTION_LISTENERS, 
                           SelectionEvent.hasListener( tabItem.getParent() ) );
  }
  
  public void renderDispose( final Widget widget ) throws IOException {
    // TODO [rh] preliminary: find out how to properly dispose of a TabItem
    JSWriter writer = JSWriter.getWriterFor( widget );
    writer.dispose();
  }
  
  //////////////////
  // helping methods
  
  private void writeCheckedState( final TabItem item ) throws IOException {
    JSWriter writer = JSWriter.getWriterFor( item );
    Boolean newValue = Boolean.valueOf( isChecked( item ) );
    if( WidgetLCAUtil.hasChanged( item, PROP_CHECKED, newValue, Boolean.FALSE ) ) {
      writer.set( JSConst.QX_FIELD_CHECKED, isChecked( item ) );
    }
  }
  
  private boolean isChecked( final TabItem tabItem ) {
    TabFolder parent = tabItem.getParent();
    int selectionIndex = parent.getSelectionIndex();
    return selectionIndex != -1 && parent.getItem( selectionIndex ) == tabItem;
  }
  
  private static void setJSParent( final TabItem tabItem ) {
    Control control = tabItem.getControl();
    if( control != null ) {
      IWidgetAdapter itemAdapter = WidgetUtil.getAdapter( tabItem );
      StringBuffer replacementId = new StringBuffer();
      replacementId.append( itemAdapter.getId() );
      replacementId.append( "pg" );
      IWidgetAdapter controlAdapter = WidgetUtil.getAdapter( control );
      controlAdapter.setJSParent( replacementId.toString() );
    }
  }
}
