/*******************************************************************************
 * Copyright (c) 2000, 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.debug.ui;
 

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;

import com.ibm.icu.text.UTF16;

/**
 * Contains a static helper method to search documents for the 'word' that encloses the current
 * selection.
 */
public class JavaWordFinder {
	
	/**
	 * Returns the IRegion containing the java identifier ("word") enclosing the specified offset or
	 * <code>null</code> if the document or offset is invalid.  Checks characters before and after the
	 * offset to see if they are allowed java identifier characters until a separator character (period,
	 * space, etc) is found.
	 * 
	 * @param document The document to search
	 * @param offset The offset to start looking for the word
	 * @return IRegion containing the word or <code>null</code>
	 */
	public static IRegion findWord(IDocument document, int offset) {
		
		if (document == null){
			return null;
		}
		
		int start= -2;
		int end= -1;
		
		
		try {
			
			int pos= offset;
			char c;
			
			while (pos >= 0) {
				c= document.getChar(pos);
				if (!Character.isJavaIdentifierPart(c)) {
					// Check for surrogates
					if (UTF16.isSurrogate(c)) {
						/*
						 * XXX: Here we should create the code point and test whether it is a Java
						 * identifier part. Currently this is not possible because
						 * java.lang.Character in 1.4 does not support surrogates and because
						 * com.ibm.icu.lang.UCharacter.isJavaIdentifierPart(int) is not correctly
						 * implemented.
						 */
					} else {
						break;
					}
				}
				--pos;
			}
			
			start= pos;
			
			pos= offset;
			int length= document.getLength();
			
			while (pos < length) {
				c= document.getChar(pos);
				if (!Character.isJavaIdentifierPart(c))
					break;
				++pos;
			}
			
			end= pos;
			
		} catch (BadLocationException x) {
		}
		
		if (start >= -1 && end > -1) {
			if (start == offset && end == offset)
				return new Region(offset, 0);
			else if (start == offset)
				return new Region(start, end - start);
			else
				return new Region(start + 1, end - start - 1);
		}
		
		return null;
	}
}
