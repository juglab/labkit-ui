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

import bdv.util.BdvFunctions;
import bdv.util.BdvOptions;
import bdv.util.BdvStackSource;
import mpicbg.spim.data.generic.AbstractSpimData;
import mpicbg.spim.data.generic.sequence.AbstractSequenceDescription;
import mpicbg.spim.data.generic.sequence.BasicViewSetup;
import mpicbg.spim.data.registration.ViewRegistration;
import mpicbg.spim.data.sequence.TimePoint;
import net.imglib2.Dimensions;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.RandomAccessibleInterval;
import net.imglib2.realtransform.AffineTransform3D;

/**
 * Implementation of {@link BdvShowable} that wraps around
 * {@link AbstractSpimData}.
 */
class SpimBdvShowable implements BdvShowable {

	private final AbstractSpimData<?> spimData;

	SpimBdvShowable(AbstractSpimData<?> spimData) {
		this.spimData = spimData;
	}

	@Override
	public Interval interval() {
		AbstractSequenceDescription<?, ?, ?> seq = spimData
			.getSequenceDescription();
		BasicViewSetup setup = getFirst(seq);
		Dimensions size = setup.getSize();
		if (size == null) {
			RandomAccessibleInterval<?> image = seq.getImgLoader().getSetupImgLoader(
				setup.getId()).getImage(0);
			return new FinalInterval(image);
		}
		return new FinalInterval(size);
	}

	@Override
	public AffineTransform3D transformation() {
		AbstractSequenceDescription<?, ?, ?> seq = spimData
			.getSequenceDescription();
		BasicViewSetup setup = getFirst(seq);
		TimePoint firstTime = seq.getTimePoints().getTimePointsOrdered().get(0);
		ViewRegistration registration = spimData.getViewRegistrations()
			.getViewRegistration(firstTime.getId(), setup.getId());
		return registration.getModel();
	}

	@Override
	public BdvStackSource<?> show(String title, BdvOptions options) {
		return BdvFunctions.show(spimData, options).get(0);
	}

	private BasicViewSetup getFirst(AbstractSequenceDescription<?, ?, ?> seq) {
		for (final BasicViewSetup setup : seq.getViewSetupsOrdered()) {
			return setup;
		}
		throw new IllegalStateException("SpimData contains no setup.");
	}
}
