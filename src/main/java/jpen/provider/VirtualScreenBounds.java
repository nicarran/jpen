/* % [{
% (C) Copyright 2008 Nicolas Carranza and individual contributors.
% See the jpen-copyright.txt file in the jpen distribution for a full
% listing of individual contributors.
%
% This file is part of jpen.
%
% jpen is free software: you can redistribute it and/or modify
% it under the terms of the GNU Lesser General Public License as published by
% the Free Software Foundation, either version 3 of the License, or
% (at your option) any later version.
%
% jpen is distributed in the hope that it will be useful,
% but WITHOUT ANY WARRANTY; without even the implied warranty of
% MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
% GNU Lesser General Public License for more details.
%
% You should have received a copy of the GNU Lesser General Public License
% along with jpen.  If not, see <http://www.gnu.org/licenses/>.
% }] */
package jpen.provider;

import java.awt.DisplayMode;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.util.logging.Level;
import java.util.logging.Logger;
import jpen.PLevel;

public final class VirtualScreenBounds{
	static final Logger L=Logger.getLogger(VirtualScreenBounds.class.getName());
	private static VirtualScreenBounds INSTANCE;
	private final Rectangle2D.Float r=new Rectangle2D.Float();

	{
		// first time calc is expensive... I do it once in a background thread
		new Thread(){
			{
				setName("jpen-VirtualScreenBounds");
				setDaemon(true);
			}
			@Override
			public void run(){
				reset();
				L.fine("first calculation done.");
			}
		}.start();
	}

	private VirtualScreenBounds(){}

	public static VirtualScreenBounds getInstance(){
		if(INSTANCE==null)
			INSTANCE=new VirtualScreenBounds();
		return INSTANCE;
	}

	public synchronized void reset(){
		r.x=r.y=r.width=r.height=0;
		calc(r);
	}

	static void calc(Rectangle2D r){
		for (GraphicsDevice gd: GraphicsEnvironment.
		        getLocalGraphicsEnvironment().getScreenDevices()){
			GraphicsConfiguration graphicsConfiguration=gd.getDefaultConfiguration();
			r.add(graphicsConfiguration.getBounds());
		}
	}

	public float getLevelRangeMult(PLevel.Type type) {
		switch(type){
		case X:
			return r.width;
		case Y:
			return r.height;
		default:
			return 1f;
		}
	}

	public float getLevelRangeOffset(PLevel.Type type){
		switch(type){
		case X:
			return r.x;
		case Y:
			return r.y;
		default:
			return 0f;
		}
	}
}
