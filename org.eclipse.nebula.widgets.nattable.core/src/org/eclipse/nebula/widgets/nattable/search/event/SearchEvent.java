/*******************************************************************************
 * Copyright (c) 2012 Original authors and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Original authors and others - initial API and implementation
 ******************************************************************************/
package org.eclipse.nebula.widgets.nattable.search.event;

import org.eclipse.nebula.widgets.nattable.coordinate.PositionCoordinate;
import org.eclipse.nebula.widgets.nattable.layer.event.AbstractContextFreeEvent;

public class SearchEvent extends AbstractContextFreeEvent {
	
	private final PositionCoordinate cellCoordinate;

	public SearchEvent(PositionCoordinate cellCoordinate) {
		this.cellCoordinate = cellCoordinate;
	}
	
	public PositionCoordinate getCellCoordinate() {
		return cellCoordinate;
	}
	
	public SearchEvent cloneEvent() {
		return new SearchEvent(cellCoordinate);
	}
	
}