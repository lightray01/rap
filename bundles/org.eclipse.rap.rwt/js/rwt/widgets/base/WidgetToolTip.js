/*******************************************************************************
 * Copyright (c) 2009, 2013 EclipseSource.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    EclipseSource - initial API and implementation
 ******************************************************************************/

rwt.qx.Class.define( "rwt.widgets.base.WidgetToolTip", {
  type : "singleton",
  extend : rwt.widgets.base.Popup,
  include : rwt.animation.VisibilityAnimationMixin,

  construct : function() {
    this.base( arguments );
    this._label = new rwt.widgets.base.MultiCellWidget( [ "label" ] );
    this._label.setParent( this );
    this._showTimer = new rwt.client.Timer(this.getShowInterval());
    this._showTimer.addEventListener("interval", this._onshowtimer, this);
    this._hideTimer = new rwt.client.Timer(this.getHideInterval());
    this._hideTimer.addEventListener("interval", this._onhidetimer, this);
    this.addEventListener("mouseover", this._onmouseover);
    this.addEventListener("mouseout", this._onmouseover);
  },

  statics : {

    setToolTipText : function( widget, value ) {
      if( value != null && value !== "" ) {
        var EncodingUtil = rwt.util.Encoding;
        var text = EncodingUtil.escapeText( value, false );
        text = EncodingUtil.replaceNewLines( text, "<br/>" );
        widget.setUserData( "toolTipText", text );
        var toolTip = rwt.widgets.base.WidgetToolTip.getInstance();
        widget.setToolTip( toolTip );
        // make sure "boundToWidget" is initialized:
        if( toolTip.getParent() != null ) {
          if( toolTip.getBoundToWidget() == widget ) {
            toolTip.updateText( widget );
          }
        }
      } else {
        widget.setToolTip( null );
        widget.setUserData( "toolTipText", null );
      }
      widget.dispatchSimpleEvent( "changeToolTipText", widget ); // used by Synchronizer.js
    }

  },

  properties : {

    appearance : {
      refine : true,
      init : "widget-tool-tip"
    },

    restrictToPageOnOpen : {
      refine : true,
      init : false // this messes with my own layout code, I'll do it myself!
    },

    hideOnHover : {
      check : "Boolean",
      init : true
    },

    mousePointerOffsetX : {
      check : "Integer",
      init : 1
    },

    mousePointerOffsetY : {
      check : "Integer",
      init : 20
    },

    showInterval : {
      check : "Integer",
      init : 500,
      apply : "_applyShowInterval"
    },

    hideInterval : {
      check : "Integer",
      init : 500,
      apply : "_applyHideInterval"
    },

    boundToWidget : {
      check : "rwt.widgets.base.Widget",
      apply : "_applyBoundToWidget"
    }

  },

  members : {
    _minZIndex : 1e7,
    _isFocusRoot : false,

    _applyHideInterval : function(value, old) {
      this._hideTimer.setInterval(value);
    },

    _applyShowInterval : function(value, old) {
      this._showTimer.setInterval(value);
    },

    _applyBoundToWidget : function(value, old) {
      var manager = rwt.widgets.util.ToolTipManager.getInstance();
      manager.setCurrentToolTip( null );
      if (value) {
        this.setParent(value.getTopLevelWidget());
      } else if (old) {
        this.setParent(null);
      }
    },

    _beforeAppear : function() {
      this.base(arguments);
      this._stopShowTimer();
    },

    _beforeDisappear : function() {
      this.base(arguments);
      this._stopHideTimer();
    },

    _startShowTimer : function() {
      if (!this._showTimer.getEnabled()) {
        this._showTimer.start();
      }
    },

    _startHideTimer : function() {
      if(!this._hideTimer.getEnabled()) {
        this._hideTimer.start();
      }
    },

    _stopShowTimer : function() {
      if (this._showTimer.getEnabled()) {
        this._showTimer.stop();
      }
    },

    _stopHideTimer : function() {
      if (this._hideTimer.getEnabled()) {
        this._hideTimer.stop();
      }
    },

    _onmouseover : function(e) {
      if (this.getHideOnHover()) {
        this.hide();
      }
    },

    _onshowtimer : function(e) {
      this._stopShowTimer();
      this._stopHideTimer();
      this.updateText( this.getBoundToWidget() );
      this.show();
      rwt.widgets.base.Widget.flushGlobalQueues(); // render new dimension
      this._afterAppearLayout();
      rwt.widgets.base.Widget.flushGlobalQueues(); // render position
    },

    _onhidetimer : function(e) {
      return this.hide();
    },

    _afterAppearLayout : function() {
      var newPosition = this._getPositionAfterAppear();
      this.setLeft( newPosition[ 0 ] );
      this.setTop( newPosition[ 1 ] );
    },

    _applyLeft : function( v ) {
      this.base( arguments, v );
    },

    hide : function() {
      this.base( arguments );
    },

    updateText : function( widget ) {
      this._label.setCellContent( 0, widget.getUserData( "toolTipText" ) );
    },

    _getPositionAfterAppear : function() {
      var config = rwt.widgets.util.ToolTipConfig.getConfig( this.getBoundToWidget() );
      switch( config.position ) {
        case "horizontal-center":
          return this._positionHorizontalCenter();
        default:
          return [
            rwt.event.MouseEvent.getPageX() + this.getMousePointerOffsetX(),
            rwt.event.MouseEvent.getPageY() + this.getMousePointerOffsetY()
          ];
      }
    },

    _positionHorizontalCenter : function() {
      var target = this._getWidgetBounds();
      var left = Math.round( target[ 0 ] + ( target[ 2 ] / 2 ) - this.getBoxWidth() / 2 );
      var top = target[ 1 ] + target[ 3 ] + 3;
      return [ left, top ];
    },

    _getWidgetBounds : function() {
      var widget = this.getBoundToWidget();
      var location = rwt.html.Location.get( widget.getElement() );
      return [
        location.left,
        location.top,
        widget.getBoxWidth(),
        widget.getBoxHeight()
      ];
    }

  },


  destruct : function() {
    var mgr = rwt.widgets.util.ToolTipManager.getInstance();
    mgr.remove(this);
    if (mgr.getCurrentToolTip() == this) {
      mgr.resetCurrentToolTip();
    }
    this._disposeObjects("_showTimer", "_hideTimer", "_label");
  }

} );
