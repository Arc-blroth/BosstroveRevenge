package ai.arcblroth.boss.engine.gui.dialog;

import ai.arcblroth.boss.engine.gui.GUIConstraints;
import ai.arcblroth.boss.engine.gui.GUIPanel;
import ai.arcblroth.boss.engine.gui.GUIText;
import ai.arcblroth.boss.key.Keybind;
import ai.arcblroth.boss.render.Color;
import ai.arcblroth.boss.render.PixelAndTextGrid;
import ai.arcblroth.boss.util.PadUtils;
import ai.arcblroth.boss.util.TextureUtils;

import java.util.LinkedList;
import java.util.List;

public class GUIListDialog extends GUIPanel {

	private static final GUIConstraints DEFAULT_OPTION_PADDING = new GUIConstraints(0, 0, 1, 0, 1, 2, -1, 4, 0);

	private LinkedList<DialogOption> options;
	private GUIConstraints optionPadding;
	private int viewOffsetPosition;
	private volatile int lastSize;
	private int selectedPosition;

	public GUIListDialog() {
		super();
		this.options = new LinkedList<>();
		this.optionPadding = DEFAULT_OPTION_PADDING;
		viewOffsetPosition = 0;
		selectedPosition = 0;
		lastSize = 0;
	}

	public GUIListDialog(List<DialogOption> options, Color backgroundColor, Color borderColor, int borderWidth) {
		this(options, backgroundColor, borderColor, borderWidth, DEFAULT_OPTION_PADDING);
	}

	public GUIListDialog(List<DialogOption> options, Color backgroundColor, Color borderColor, int borderWidth, GUIConstraints optionPadding) {
		super(backgroundColor, borderColor, borderWidth);
		this.options = new LinkedList<>();
		this.options.addAll(options);
		this.optionPadding = optionPadding;
		viewOffsetPosition = 0;
		selectedPosition = 0;
		lastSize = 0;
	}

	@Override
	public void render(PixelAndTextGrid target) {
		super.render(target);
		if(!options.isEmpty()) {
			if(target.getWidth() - 2 * super.getBorderWidth() > 0 && target.getHeight() - 2 * super.getBorderWidth() > 0) {
				PixelAndTextGrid nonborderedTarget = TextureUtils.buildTransparentTextGrid(target.getWidth() - 2 * super.getBorderWidth(), target.getHeight() - 2 * super.getBorderWidth());

				int yCoord = 0;
				int targetWidth = nonborderedTarget.getWidth();
				int targetHeight = nonborderedTarget.getHeight();
				int resolvedX = optionPadding.resolveX(targetWidth, targetHeight);
				int resolvedY = optionPadding.resolveY(targetWidth, targetHeight);
				int resolvedWidth = Math.min(optionPadding.resolveWidth(targetWidth, targetHeight), targetWidth - resolvedX) - 2;
				int resolvedHeight = optionPadding.resolveHeight(nonborderedTarget.getWidth(), nonborderedTarget.getHeight());

				// Fix selected position rendering
				lastSize = nonborderedTarget.getHeight() / resolvedHeight;
				if(selectedPosition < viewOffsetPosition) viewOffsetPosition--;
				if(selectedPosition > viewOffsetPosition + lastSize - 1) viewOffsetPosition++;

				int viewOffsetCounter = viewOffsetPosition;

				if(resolvedWidth > 0 && resolvedHeight > 0) {
					int counter = 0;
					while (yCoord < nonborderedTarget.getHeight() && viewOffsetCounter < options.size()) {
						if(viewOffsetCounter >= 0) {
							DialogOption option = options.get(viewOffsetCounter);
							Color optionbg = option.isSelected() ? option.getSelectedBackgroundColor() : option.getDeselectedBackgroundColor();
							Color optionfg = option.isSelected() ? option.getSelectedForegroundColor() : option.getDeselectedForegroundColor();

							if (resolvedX < targetWidth && yCoord + resolvedY < targetHeight) {
								PixelAndTextGrid childTarget = TextureUtils.buildTransparentTextGrid(resolvedWidth, resolvedHeight);
								GUIText optionText = new GUIText(
										option.getOptionText() + PadUtils.stringTimes(" ", resolvedWidth - option.getOptionText().length() % resolvedWidth - 1),
										optionbg, optionfg, false);
								optionText.render(childTarget);

								TextureUtils.overlay(childTarget, nonborderedTarget, 2 + resolvedX, yCoord + resolvedY);
								nonborderedTarget.setCharacterAt(1,yCoord + resolvedY, counter == selectedPosition - viewOffsetPosition ? '>' : ' ', optionbg, optionfg);
								nonborderedTarget.setCharacterAt(2,yCoord + resolvedY, ' ', optionbg, optionfg);
							}
						}

						counter++;
						viewOffsetCounter++;
						yCoord += resolvedHeight;
					}

					TextureUtils.overlay(nonborderedTarget, target, super.getBorderWidth(), (int)Math.ceil(super.getBorderWidth() / 2D) * 2);
				}
			}
		}
	}

	@Override
	public void onInput(Keybind k) {
		if (k.equals(new Keybind("boss.up"))) {
			selectedPosition = Math.max(selectedPosition - 1, 0);
		}
		if (k.equals(new Keybind("boss.down"))) {
			selectedPosition = Math.min(selectedPosition + 1, options.size() - 1);
		}
		if (k.equals(new Keybind("boss.use"))) {
			options.get(selectedPosition).setSelected(!options.get(selectedPosition).isSelected());
		}
	}
}
