/*
 * Copyright (c) 2019 simplity.org
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.simplity.fm.gen.element;

/**
 * <p>
 * Represents a class/component whose primary objective is to interact with the
 * end-user on a display device. Hence, its primary objective is to 'render'
 * itself on a display device
 * </p>
 * <p>
 * A display element primarily has three aspects
 * </p>
 * <li>contents : An element may contain other child-elements. Each such child
 * is also a display-element, there by allowing an infinite level of
 * hierarchy</li>
 * <li>attributes: a fixed set of some of data/meta-data/information that this
 * element uses. These are passed to the element at run time through a suitable
 * API. Each concrete display-element defines these 'parameters' in a structured
 * way, so that an editor can be provided to facilitate creation of input for
 * such an API as well as validate it. These values are typically design-time
 * provided but they can be provided at run time for customized UX. However, the
 * data is not considered 'business-data'. It is just display-related
 * preference.
 * ways. For example user-color-preference.</li>
 * <li>business-data-binding: the purpose of rendering this element may include
 * showing or editing business data. An element may not be bound, bound one-way
 * or bound two-way. Such a data is typically called as 'model' in the MVC-like
 * terminology.</li>
 *
 * <p>
 * This class deals ONLY with the generation of such an element, and not its run
 * time behavior. Hence the class design is not directly driven b the
 * description above.
 * </p>
 *
 * @author simplity.org
 *
 */
public abstract class DisplayElement {
	/**
	 * emit an interface that describes the input json for this element.
	 *
	 * @param sbf
	 */
	public abstract void emitTsInterface(StringBuilder sbf);

	/**
	 * emit this object as part of the collection of elements of its parent.
	 * Note this is actually a static method. It does not depend on the values
	 * of its attributes. Its definition is dependent on the concrete-class.
	 *
	 * @param sbf
	 */
	public abstract void emitTsObject(StringBuilder sbf);
}
