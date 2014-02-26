package com.protoplant.xtruder;

import org.eclipse.swt.widgets.Widget;

public class PanelFocusEvent {
	
	private Widget widget;
	
	public PanelFocusEvent(Widget widget) {
		this.widget = widget;
	}

	public Widget getWidget() {
		return widget;
	}

	public void setWidget(Widget widget) {
		this.widget = widget;
	}

}
