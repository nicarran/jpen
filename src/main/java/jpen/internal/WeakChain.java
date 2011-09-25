/* [{
Copyright 2011 Nicolas Carranza <nicarran at gmail.com>

This file is part of jpen.

jpen is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License,
or (at your option) any later version.

jpen is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with jpen.  If not, see <http://www.gnu.org/licenses/>.
}] */
package jpen.internal;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

public class WeakChain<E>{

	private static	class Cell<E> extends WeakReference<E>{
		Cell<E> nextCell;
		Cell(E element, Cell<E> nextCell){
			super(element);
			if(element==null)
				throw new IllegalArgumentException();
			this.nextCell=nextCell;
		}
	}

	private Cell<E> headCell;
	private Cell<E> tailCell;

	public synchronized boolean add(E element){
		Cell<E> newCell=new Cell<E>(element, null);
		if(tailCell==null){
			if(headCell!=null)
				throw new AssertionError();
			headCell=tailCell=newCell;
		}else{
			tailCell.nextCell=newCell;
			tailCell=newCell;
		}
		return true;
	}

	public synchronized void clear(){
		headCell=tailCell=null;
	}

	public synchronized boolean remove(final E element){
		Cell<E> prevCell=null;
		Cell<E> cell=headCell;
		if(cell==null)
			return false;
		do{
			E cellElement=cell.get();
			if(cellElement==null)
				removeCell(cell, prevCell);
			else if(cellElement==element){
				removeCell(cell, prevCell);
				return true;
			}else
				prevCell=cell;
		}while((cell=cell.nextCell)!=null);
		return false;
	}

	private void removeCell(Cell<E> cell, Cell<E> prevCell){
		if(prevCell!=null){ // cell is not be the head
			prevCell.nextCell=cell.nextCell;
		}else{ // cell must be the head
			if(cell!=headCell)
				throw new AssertionError();
			headCell=cell.nextCell;
		}
		if(cell==tailCell){
			tailCell=prevCell;
			if(tailCell!=null)
				tailCell.nextCell=null;
		}
	}

	public Collection<E> snapshot(){
		return snapshot(null);
	}

	public synchronized Collection<E> snapshot(Collection<E> elements){
		Cell<E> cell=headCell;
		if(cell==null)
			return elements==null? Collections.<E>emptyList(): elements;
		if(elements==null){
			if(cell==tailCell){// optimization
				E cellElement=cell.get();
				if(cellElement!=null)
					return Collections.singletonList(cellElement);
				removeCell(cell, null);
				return Collections.emptyList();
			}
			elements=new LinkedList<E>();
		}
		Cell<E> prevCell=null;
		do{
			E cellElement=cell.get();
			if(cellElement==null){
				removeCell(cell, prevCell);
			}else{
				elements.add(cellElement);
				prevCell=cell;
			}
		}while((cell=cell.nextCell)!=null);
		return elements;
	}

	public void purge(){
		remove(null);
	}

	public synchronized boolean isEmpty(){
		return headCell==null;
	}

}