/*******************************************************************************
 * Copyright (c) 2007 Innoopract Informationssysteme GmbH.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Innoopract Informationssysteme GmbH - initial API and implementation
 ******************************************************************************/

package org.eclipse.swt.internal.widgets.shellkit;

import org.eclipse.swt.SWT;
import org.eclipse.swt.internal.theme.*;
import org.eclipse.swt.widgets.*;

public class ShellThemeAdapter implements IShellThemeAdapter {

  private static final int TITLE_BAR_HEIGHT = 18;
  private static final int MENU_BAR_HEIGHT = 20;

  public QxBoxDimensions getPadding( final Shell shell ) {
    Theme theme = ThemeUtil.getTheme();
    QxBoxDimensions result;
    if( ( shell.getStyle() & SWT.TITLE ) != 0 ) {
      result = theme.getBoxDimensions( "shell.padding" );
    } else {
      result = new QxBoxDimensions( 0, 0, 0, 0 );
    }
    return result;
  }
  
  public QxBoxDimensions getTitleBarMargin( Shell shell ) {
    Theme theme = ThemeUtil.getTheme();
    QxBoxDimensions result;
    if( ( shell.getStyle() & SWT.TITLE ) != 0 ) {
      result = theme.getBoxDimensions( "shell.titlebar.margin" );
    } else {
      result = new QxBoxDimensions( 0, 0, 0, 0 );
    }
    return result;
  }
  
  public int getBorderWidth( final Control control ) {
    Theme theme = ThemeUtil.getTheme();
    return theme.getBorder( "shell.border" ).width;
  }
  
  public QxColor getForeground( final Control control ) {
    Theme theme = ThemeUtil.getTheme();
    return theme.getColor( "widget.foreground" );
  }
  
  public QxColor getBackground( final Control control ) {
    Theme theme = ThemeUtil.getTheme();
    return theme.getColor( "shell.background" );
  }

  public int getTitleBarHeight( final Shell shell ) {
    return ( shell.getStyle() & SWT.TITLE ) != 0 ? TITLE_BAR_HEIGHT : 0;
  }

  public int getMenuBarHeight( Shell shell ) {
    return shell.getMenuBar() != null ? MENU_BAR_HEIGHT : 0;
  }

  public QxFont getFont( final Control control ) {
    Theme theme = ThemeUtil.getTheme();
    return theme.getFont( "widget.font" );
  }
}
