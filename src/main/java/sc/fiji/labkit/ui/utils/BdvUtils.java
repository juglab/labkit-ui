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

package sc.fiji.labkit.ui.utils;

import bdv.TransformEventHandler2D;
import bdv.TransformEventHandler3D;
import bdv.util.BdvHandle;
import bdv.viewer.NavigationActions;
import bdv.viewer.Source;
import bdv.viewer.ViewerPanel;
import net.imglib2.FinalInterval;
import net.imglib2.Interval;
import net.imglib2.realtransform.AffineTransform3D;
import org.scijava.ui.behaviour.Behaviour;
import org.scijava.ui.behaviour.BehaviourMap;
import org.scijava.ui.behaviour.util.RunnableAction;

import javax.swing.*;
import java.util.Arrays;
import java.util.Collections;

/**
 * Some utility functions useful for working with BigDataViewer.
 */
public class BdvUtils {

	private static final String[] ROTATE_BEHAVIOURS = {
		TransformEventHandler3D.DRAG_ROTATE,
		TransformEventHandler3D.DRAG_ROTATE_FAST,
		TransformEventHandler3D.DRAG_ROTATE_SLOW,
		TransformEventHandler3D.ROTATE_LEFT,
		TransformEventHandler3D.ROTATE_LEFT_FAST,
		TransformEventHandler3D.ROTATE_LEFT_SLOW,
		TransformEventHandler3D.ROTATE_RIGHT,
		TransformEventHandler3D.ROTATE_RIGHT_FAST,
		TransformEventHandler3D.ROTATE_RIGHT_SLOW,
		TransformEventHandler2D.DRAG_ROTATE,
		TransformEventHandler2D.ROTATE_LEFT,
		TransformEventHandler2D.ROTATE_LEFT_FAST,
		TransformEventHandler2D.ROTATE_LEFT_SLOW,
		TransformEventHandler2D.ROTATE_RIGHT,
		TransformEventHandler2D.ROTATE_RIGHT_FAST,
		TransformEventHandler2D.ROTATE_RIGHT_SLOW
	};

	private static final String[] ROTATE_ACTIONS = {
		NavigationActions.ALIGN_XY_PLANE,
		NavigationActions.ALIGN_XZ_PLANE,
		NavigationActions.ALIGN_ZY_PLANE
	};

	private static final String BLOCK_ROTATE_BEHAVIOURS_ID = "block_rotate_behaviours";
	private static final String BLOCK_ROTATE_ACTIONS_ID = "block_rotate_actions";

	public static void blockRotation(BdvHandle bdv) {
		blockRotateBehaviors(bdv);
		blockRotateActions(bdv);
	}

	private static void blockRotateBehaviors(BdvHandle bdv) {
		final BehaviourMap behaviourMap = new BehaviourMap();
		Behaviour do_nothing = new Behaviour() {};
		for (String id : ROTATE_BEHAVIOURS)
			behaviourMap.put(id, do_nothing);
		bdv.getTriggerbindings().addBehaviourMap(BLOCK_ROTATE_BEHAVIOURS_ID, behaviourMap);
	}

	private static void blockRotateActions(BdvHandle bdv) {
		final ActionMap actionMap = new ActionMap();
		Action do_nothing = new RunnableAction("nop", () -> {});
		for (String id : ROTATE_ACTIONS)
			actionMap.put(id, do_nothing);
		bdv.getKeybindings().addActionMap(BLOCK_ROTATE_ACTIONS_ID, actionMap);
	}

	public static void unblockRotation(BdvHandle bdv) {
		bdv.getTriggerbindings().removeBehaviourMap(BLOCK_ROTATE_BEHAVIOURS_ID);
		bdv.getKeybindings().removeActionMap(BLOCK_ROTATE_ACTIONS_ID);
	}

	/**
	 * Change BigDataViewer's viewer transform such that the view is aligned with
	 * the XY plane, the image (source 0) is fitted into the screen, and the center
	 * pixel of the image is visible.
	 */
	public static void resetView(ViewerPanel viewerPanel) {
		Source<?> spimSource = viewerPanel.state().getSources().get(0).getSpimSource();
		AffineTransform3D transform = new AffineTransform3D();
		int timePoint = viewerPanel.state().getCurrentTimepoint();
		spimSource.getSourceTransform(timePoint, 0, transform);
		Interval interval = new FinalInterval(spimSource.getSource(timePoint, 0));
		resetView(viewerPanel, interval, transform);
	}

	/**
	 * Change BigDataViewer's viewer transform such that the view is aligned with
	 * the XY plan, defined by the given source transform and the interval is fitted
	 * into the screen.
	 */
	public static void resetView(ViewerPanel viewerPanel, Interval interval,
		AffineTransform3D sourceTransform)
	{
		AffineTransform3D concat = new AffineTransform3D();
		int width = viewerPanel.getWidth();
		int height = viewerPanel.getHeight();
		AffineTransform3D m = calculateScreenTransform(interval, width, height);
		concat.set(m);
		concat.concatenate(sourceTransform.inverse());
		viewerPanel.state().setViewerTransform(concat);
	}

	private static AffineTransform3D calculateScreenTransform(Interval interval, int width,
		int height)
	{
		final double[] screenSize = { width, height };
		final double scale = getScaleFactor(screenSize, interval);
		final double[] translate = getTranslation(screenSize, interval, scale);
		final AffineTransform3D transform = new AffineTransform3D();
		transform.scale(scale);
		transform.translate(translate);
		return transform;
	}

	private static double[] getTranslation(final double[] screenSize,
		final Interval interval, final double labelScale)
	{
		final double[] translate = new double[3];
		for (int i = 0; i < Math.min(translate.length, interval
			.numDimensions()); i++)
		{
			translate[i] = -(interval.min(i) + interval.max(i)) * labelScale / 2;
			if (i < 2) {
				translate[i] += screenSize[i] / 2;
			}
		}
		return translate;
	}

	private static double getScaleFactor(final double[] screenSize,
		final Interval interval)
	{
		final Double[] scales = new Double[2];
		for (int i = 0; i < 2; i++)
			scales[i] = screenSize[i] / interval.dimension(i);
		return Collections.min(Arrays.asList(scales));
	}
}
