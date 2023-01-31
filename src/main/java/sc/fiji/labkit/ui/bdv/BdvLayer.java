/*-
 * #%L
 * The Labkit image segmentation tool for Fiji.
 * %%
 * Copyright (C) 2017 - 2023 Matthias Arzt
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package sc.fiji.labkit.ui.bdv;

import net.imglib2.Interval;
import sc.fiji.labkit.ui.models.Holder;
import sc.fiji.labkit.ui.utils.ParametricNotifier;

/**
 * Objects that implement {@link BdvLayer}, can easily be made visible in
 * BigDataViewer using {@link BdvLayerLink}.
 */
public interface BdvLayer {

	Holder<BdvShowable> image();

	ParametricNotifier<Interval> listeners();

	Holder<Boolean> visibility();

	String title();

	class FinalLayer implements BdvLayer {

		private final Holder<BdvShowable> image;
		private final String title;
		private final ParametricNotifier<Interval> listeners =
			new ParametricNotifier<>();
		private final Holder<Boolean> visibility;

		public FinalLayer(Holder<BdvShowable> image, String title,
			Holder<Boolean> visibility)
		{
			this.image = image;
			this.title = title;
			this.visibility = visibility;
		}

		@Override
		public Holder<BdvShowable> image() {
			return image;
		}

		@Override
		public ParametricNotifier<Interval> listeners() {
			return listeners;
		}

		@Override
		public Holder<Boolean> visibility() {
			return visibility;
		}

		@Override
		public String title() {
			return title;
		}
	}
}
