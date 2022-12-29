package ru.vsu.cs.volobueva.panels;

import java.awt.Color;
import java.awt.Font;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;

import org.netbeans.lib.awtextra.AbsoluteConstraints;

import ru.vsu.cs.volobueva.engine.DominoBone;
import ru.vsu.cs.volobueva.engine.GameParameters;

public final class Panel extends Game {
	private static final long serialVersionUID = 8185038222875756793L;
	private int xLine;
	private int yLine;
	private int xCenter;
	private int yCenter;
	private int fieldWidth;
	private int fieldHeight;
	private int spaceLeft;
	private int spaceRight;
	private int spaceUp;
	private int spaceDown;
	private Random randomizer = new Random();
	private boolean isTurnTopLeft;
	private boolean isTurnTopRight;
	private boolean isTurnBottomLeft;
	private boolean isTurnBottomRight;
	private boolean isRandomTurn = randomizer.nextBoolean();

	public Panel(String enemyName) {
		setTitle(" Это игровое поле. Возьмите в магазине 7 домино. То же самое делает и противник " + enemyName
				+ " ");
	}

	DominoBone leftBone() { // левый камень на панели
		return getDominoBones().get(0);
	}

	DominoBone rightBone() { // правый камень на панели
		return getDominoBones().get(getDominoBones().size() - 1);
	}

	public void selectFieldBones(Player player, DominoBone dominoBone) { // Выбираем домино на поле
		setSelectedLeft(null);
		setSelectedRight(null);

		player.disableBonesSelect();
		for (DominoBone fieldDominoBone : getDominoBones()) {
			if ((!fieldDominoBone.equals(dominoBone) & (fieldDominoBone.isSelected()))) {
				fieldDominoBone.toggleBoneSelection();
			} else if (fieldDominoBone.equals(dominoBone)) {
				fieldDominoBone.toggleBoneSelection();
			}

			if (fieldDominoBone.isSelected()) {
				if (leftBone().equals(fieldDominoBone)) {
					setSelectedLeft(fieldDominoBone);
				} else if (rightBone().equals(fieldDominoBone)) {
					setSelectedRight(fieldDominoBone);
				}
			}
		}
		player.enableBonesSelect(getSelectedLeft(), getSelectedRight());
		repaint();
	}

	@Override
	protected void rebuildBonesLine(boolean frame) { // цепляем мышку для левого и правого камней, перерисовываем рамку
		getDominoBones().forEach(bone -> {
			bone.removeMouseListener(bone.clickOnField); // убираем обработку мыши и рамку для всех камней
			if (bone.isSelected()) {
				bone.unselect(); // рисуем с нормальной мордой
			}
			bone.hideFrame();
		});
	}

	public void disableBonesSelect() {
		rebuildBonesLine(GameParameters.NOFRAME);
	}

	public void enableFieldSelect(Player player) {
		if (getDominoBones().size() == 1) { // если одна кость на поле, цепляем к ней мышку
			DominoBone temp = leftBone();

			for (DominoBone dominoBone : player.getDominoBones()) {
				if ((dominoBone.isBoneGoodToMove(temp.getLeft())) || (dominoBone.isBoneGoodToMove(temp.getRight()))) { // если хоть
																											// один
																											// домино
					// игрока подходит, разрешаем
					// щелкать по первому домино
					// на поле
					temp.addMouseListener(temp.clickOnField);
					temp.showFrame();
					break;
				}
			}
			getDominoBones().set(0, temp);
		} else {
			getDominoBones().forEach(bone -> {
				bone.removeMouseListener(bone.clickOnField); // убираем обработку мыши и рамку для всех домино
				bone.hideFrame();
			});

			DominoBone temp = leftBone(); // к левой
			for (DominoBone dominoBone : player.getDominoBones()) {
				if (dominoBone.isBoneGoodToMove(temp.getWorkSide())) { // если хоть один домино игрока подходит, разрешаем
																	// щелкать по
					// левому домино на поле
					temp.addMouseListener(temp.clickOnField);
					temp.showFrame();
					break;
				}
			}
			getDominoBones().set(0, temp);

			temp = rightBone(); // к правой
			for (DominoBone dominoBone : player.getDominoBones()) {
				if (dominoBone.isBoneGoodToMove(temp.getWorkSide())) { // если хоть один домино игрока подходит, разрешаем
																	// щелкать по
					// левому домино на поле
					temp.addMouseListener(temp.clickOnField);
					temp.showFrame();
					break;
				}
			}
			getDominoBones().set(getDominoBones().size() - 1, temp);
		}
	}

	private void putAtPosition(boolean where, DominoBone dominoBone, int x, int y) {
		dominoBone.removeMouseListener(dominoBone.clickOnHumanPlayer);
		if (where == GameParameters.TORIGHT) {
			getDominoBones().add(dominoBone); // даем домино справа
		} else if (where == GameParameters.TOLEFT) {
			getDominoBones().add(0, dominoBone); // даем домино слева
		}

		dominoBone.showBone();
		dominoBone.setLocation(x, y);
		add(dominoBone, new AbsoluteConstraints(x, y, dominoBone.getWidth(), dominoBone.getHeight()));
		repaint();
	}

	public void addFirstBone(DominoBone dominoBone) {
		int angle = GameParameters.Corner.C0.getCorner();
		if (dominoBone.isDoublet()) {
			angle = GameParameters.Corner.C90.getCorner();
			dominoBone.setWorkSide(dominoBone.getRight());
		}
		dominoBone.draw(angle, GameParameters.UNSELECTED);

		fieldWidth = this.getWidth();
		fieldHeight = this.getHeight();

		xCenter = fieldWidth / 2;
		yCenter = fieldHeight / 2;

		xLine = xCenter - dominoBone.getWidth() / 2;
		yLine = yCenter - dominoBone.getHeight() / 2;

		dominoBone.setFirst(true);
		putAtPosition(GameParameters.TORIGHT, dominoBone, xLine, yLine);

		spaceLeft = (fieldWidth - dominoBone.getWidth()) / 2; // свободное пространство слева
		spaceRight = (fieldWidth - dominoBone.getWidth()) / 2; // свободное пространство справа

		spaceUp = (fieldHeight - dominoBone.getHeight()) / 2; // свободное пространство сверху
		spaceDown = (fieldHeight - dominoBone.getHeight()) / 2; // свободное пространство снизу

		isTurnTopLeft = false;
		isTurnTopRight = false;
		isTurnBottomLeft = false;
		isTurnBottomRight = false;
	}

	private void addRightToLeft(DominoBone previous, DominoBone dominoBone) {
		int angle = GameParameters.Corner.C0.getCorner(); // если просто камень, горизонтально

		boolean turnFromHorizontalDoublet = (previous.isDoublet())
				&& ((previous.getAngle() == GameParameters.Corner.C0.getCorner())
						|| (previous.getAngle() == GameParameters.Corner.C180.getCorner()));
		boolean turnFromVerticalBone = (!previous.isDoublet())
				&& ((previous.getAngle() == GameParameters.Corner.C90.getCorner())
						|| (previous.getAngle() == GameParameters.Corner.C270.getCorner()));
		boolean prevVerticalDoublet = (previous.isDoublet())
				&& ((previous.getAngle() == GameParameters.Corner.C90.getCorner())
						|| (previous.getAngle() == GameParameters.Corner.C270.getCorner()));
		boolean prevHorizontalBone = (!previous.isDoublet())
				&& ((previous.getAngle() == GameParameters.Corner.C0.getCorner())
						|| (previous.getAngle() == GameParameters.Corner.C180.getCorner()));

		if (!dominoBone.isDoublet()) {
			if (previous.getWorkSide() == dominoBone.getLeft()) {
				angle += 180; // переворачиваем камень наоборот
			}
		} else {
			angle += 90; // если дупль
		}

		dominoBone.draw(angle, GameParameters.UNSELECTED); // отрисовываем

		xLine = previous.getX() - dominoBone.getWidth() - GameParameters.OFFSET;

		if ((prevVerticalDoublet) || (prevHorizontalBone) || (turnFromHorizontalDoublet)) {
			yLine = previous.getY() + (previous.getHeight() / 2) - (dominoBone.getHeight() / 2);
		}

		if (turnFromVerticalBone) {
			if (isTurnTopRight) {
				if (!dominoBone.isDoublet()) {
					yLine = previous.getY();
				} else {
					yLine = previous.getY() - (dominoBone.getHeight() / 2) - (GameParameters.OFFSET / 2);
				}
				isTurnTopRight = false;
			}
			if (isTurnBottomRight) {
				if (!dominoBone.isDoublet()) {
					yLine = previous.getY() + previous.getHeight() - dominoBone.getHeight();
				} else {
					yLine = previous.getY() + previous.getHeight() - (dominoBone.getHeight() / 2);
				}
				isTurnBottomRight = false;
			}
		}

		dominoBone.setWorkSide(dominoBone.getLeft()); // рабочая часть камня левая

		if (getDominoBones().size() == 1) { // в начале игры ставим камень слева
			putAtPosition(GameParameters.TOLEFT, dominoBone, xLine, yLine);
		} else {
			if (previous.equals(leftBone())) { // если работаем с левым концом, ставим камень слева
				putAtPosition(GameParameters.TOLEFT, dominoBone, xLine, yLine);
			} else {
				putAtPosition(GameParameters.TORIGHT, dominoBone, xLine, yLine); // если с правым то справа
			}
		}
		spaceLeft -= dominoBone.getWidth();
	}

	private void addLeftToRight(DominoBone previous, DominoBone dominoBone) {
		int angle = GameParameters.Corner.C0.getCorner(); // если просто домино, горизонтально

		boolean turnFromHorizontalDuplet = (previous.isDoublet())
				&& ((previous.getAngle() == GameParameters.Corner.C0.getCorner())
						|| (previous.getAngle() == GameParameters.Corner.C180.getCorner()));
		boolean turnFromVerticalBone = (!previous.isDoublet())
				&& ((previous.getAngle() == GameParameters.Corner.C90.getCorner())
						|| (previous.getAngle() == GameParameters.Corner.C270.getCorner()));
		boolean prevVerticalDuplet = (previous.isDoublet())
				&& ((previous.getAngle() == GameParameters.Corner.C90.getCorner())
						|| (previous.getAngle() == GameParameters.Corner.C270.getCorner()));
		boolean prevHorizontalBone = (!previous.isDoublet())
				&& ((previous.getAngle() == GameParameters.Corner.C0.getCorner())
						|| (previous.getAngle() == GameParameters.Corner.C180.getCorner()));

		if (!dominoBone.isDoublet()) {
			if (previous.getWorkSide() == dominoBone.getRight()) {
				angle = GameParameters.Corner.C180.getCorner(); // переворачиваем камень наоборот
			}
		} else {
			angle = GameParameters.Corner.C90.getCorner(); // если дупль
		}

		dominoBone.draw(angle, GameParameters.UNSELECTED); // отрисовываем

		xLine = previous.getX() + previous.getWidth() + GameParameters.OFFSET;

		if ((prevVerticalDuplet) || (prevHorizontalBone) || (turnFromHorizontalDuplet)) {
			yLine = previous.getY() + (previous.getHeight() / 2) - (dominoBone.getHeight() / 2);
		}

		if (turnFromVerticalBone) {
			if (isTurnTopLeft) {
				if (!dominoBone.isDoublet()) {
					yLine = previous.getY();
				} else {
					yLine = previous.getY() - (dominoBone.getHeight() / 2) - GameParameters.OFFSET;
				}
				isTurnTopLeft = false;
			}
			if (isTurnBottomLeft) {
				if (!dominoBone.isDoublet()) {
					yLine = previous.getY() + previous.getHeight() - dominoBone.getHeight();
				} else {
					yLine = previous.getY() + previous.getHeight() - (dominoBone.getHeight() / 2);
				}
				isTurnBottomLeft = false;
			}
		}

		dominoBone.setWorkSide(dominoBone.getRight()); // рабочая часть домино правая

		if (getDominoBones().size() == 1) { // в начале игры ставим домино справа
			putAtPosition(GameParameters.TORIGHT, dominoBone, xLine, yLine);
		} else {
			if (previous.equals(leftBone())) { // если работаем с левым концом, ставим домино слева
				putAtPosition(GameParameters.TOLEFT, dominoBone, xLine, yLine);
			} else {
				putAtPosition(GameParameters.TORIGHT, dominoBone, xLine, yLine); // если с правым то справа
			}
		}
		spaceRight -= dominoBone.getWidth();
	}

	private void addDownToUp(DominoBone previous, DominoBone dominoBone) {
		int angle = GameParameters.Corner.C90.getCorner(); // переворачиваем на 90

		boolean turnFromHorizontalBone = (!previous.isDoublet())
				&& ((previous.getAngle() == GameParameters.Corner.C0.getCorner())
						|| (previous.getAngle() == GameParameters.Corner.C180.getCorner())); // крайний камень по
		// горизонтали
		boolean turnFromHorizontalDuplet = (previous.isDoublet())
				&& ((previous.getAngle() == GameParameters.Corner.C90.getCorner())
						|| (previous.getAngle() == GameParameters.Corner.C270.getCorner())); // крайний дупль по
		// горизонтали
		boolean prevVerticalBone = (!previous.isDoublet()) && ((previous.getAngle() == GameParameters.Corner.C90.getCorner())
				|| (previous.getAngle() == GameParameters.Corner.C270.getCorner())); // предыдущий камень по
		// вертикали
		boolean prevVerticalDuplet = (previous.isDoublet()) && ((previous.getAngle() == GameParameters.Corner.C0.getCorner())
				|| (previous.getAngle() == GameParameters.Corner.C180.getCorner())); // предыдущий дупль по
		// вертикали

		if (!dominoBone.isDoublet()) {
			if ((previous.getRight() == dominoBone.getLeft()) || (previous.getLeft() == dominoBone.getLeft())) {
				angle = GameParameters.Corner.C270.getCorner(); // переворачиваем камень наоборот
			}
		} else {
			angle = GameParameters.Corner.C0.getCorner(); // если дупль
		}
		dominoBone.draw(angle, GameParameters.UNSELECTED); // отрисовываем

		yLine = previous.getY() - dominoBone.getHeight() - GameParameters.OFFSET;

		if (previous.equals(rightBone())) { // если работаем с правым концом

			if (turnFromHorizontalBone) { // от не дупля по горизонтали поворачиваем вертикально
				xLine = previous.getX() + previous.getWidth() / 2 + GameParameters.OFFSET;
			}

			if ((turnFromHorizontalDuplet) && (!dominoBone.isDoublet())) { // от дупля по горизонтали поворачиваем
																			// вертикально
				xLine = previous.getX() + (previous.getWidth() / 2) - (dominoBone.getWidth() / 2);
			}

			if ((prevVerticalBone) || (prevVerticalDuplet)) { // уже движемся по вертикали
				xLine = previous.getX() + (previous.getWidth() / 2) - (dominoBone.getWidth() / 2);
			}
			dominoBone.setWorkSide(dominoBone.getLeft());
			putAtPosition(GameParameters.TORIGHT, dominoBone, xLine, yLine);
		}

		if (previous.equals(leftBone())) { // если работаем с левым концом

			if ((turnFromHorizontalBone) && (!dominoBone.isDoublet())) { // от не дупля по горизонтали поворачиваем
																			// вертикально и ставим не дупль
				xLine = previous.getX();
			}

			if ((turnFromHorizontalBone) && (dominoBone.isDoublet())) { // от не дупля по горизонтали поворачиваем
																			// вертикально и ставим дупль
				xLine = previous.getX() - (dominoBone.getWidth() / 2) - GameParameters.OFFSET;
			}

			if ((turnFromHorizontalDuplet) && (!dominoBone.isDoublet())) { // от дупля по горизонтали поворачиваем
																			// вертикально
				xLine = previous.getX();
			}

			if ((prevVerticalBone) || (prevVerticalDuplet)) { // уже движемся по вертикали
				xLine = previous.getX() + (previous.getWidth() / 2) - (dominoBone.getWidth() / 2);
			}

			dominoBone.setWorkSide(dominoBone.getLeft());
			putAtPosition(GameParameters.TOLEFT, dominoBone, xLine, yLine);
		}
		spaceUp -= dominoBone.getHeight();
	}

	private void addUpToDown(DominoBone previous, DominoBone dominoBone) {
		int angle = GameParameters.Corner.C90.getCorner(); // переворачиваем на 90

		boolean turnFromHorizontalBone = (!previous.isDoublet())
				&& ((previous.getAngle() == GameParameters.Corner.C0.getCorner())
						|| (previous.getAngle() == GameParameters.Corner.C180.getCorner())); // крайний камень по
		// горизонтали
		boolean turnFromHorizontalDuplet = (previous.isDoublet())
				&& ((previous.getAngle() == GameParameters.Corner.C90.getCorner())
						|| (previous.getAngle() == GameParameters.Corner.C270.getCorner())); // крайний дупль по
		// горизонтали
		boolean prevVerticalBone = (!previous.isDoublet()) && ((previous.getAngle() == GameParameters.Corner.C90.getCorner())
				|| (previous.getAngle() == GameParameters.Corner.C270.getCorner())); // предыдущий камень по
		// вертикали
		boolean prevVerticalDuplet = (previous.isDoublet()) && ((previous.getAngle() == GameParameters.Corner.C0.getCorner())
				|| (previous.getAngle() == GameParameters.Corner.C180.getCorner())); // предыдущий дупль по
		// вертикали

		if (!dominoBone.isDoublet()) {
			if ((previous.getRight() == dominoBone.getRight()) || (previous.getLeft() == dominoBone.getRight())) {
				angle = GameParameters.Corner.C270.getCorner(); // переворачиваем камень наоборот
			}
		} else {
			angle = GameParameters.Corner.C0.getCorner(); // если дупль
		}
		dominoBone.draw(angle, GameParameters.UNSELECTED); // отрисовываем

		yLine = previous.getY() + previous.getHeight() + GameParameters.OFFSET;

		if (previous.equals(rightBone())) { // если работаем с правым концом

			if (turnFromHorizontalBone) { // от не дупля по горизонтали поворачиваем вертикально
				xLine = previous.getX() + previous.getWidth() / 2 + GameParameters.OFFSET;
			}

			if ((turnFromHorizontalDuplet) && (!dominoBone.isDoublet())) { // от дупля по горизонтали поворачиваем
																			// вертикально
				xLine = previous.getX() + (previous.getWidth() / 2) - (dominoBone.getWidth() / 2);
			}

			if ((prevVerticalBone) || (prevVerticalDuplet)) { // уже движемся по вертикали
				xLine = previous.getX() + (previous.getWidth() / 2) - (dominoBone.getWidth() / 2);
			}

			dominoBone.setWorkSide(dominoBone.getRight());
			putAtPosition(GameParameters.TORIGHT, dominoBone, xLine, yLine);
		}

		if (previous.equals(leftBone())) { // если работаем с левым концом

			if ((turnFromHorizontalBone) && (!dominoBone.isDoublet())) { // от не дупля по горизонтали поворачиваем
																			// вертикально и ставим не дупль
				xLine = previous.getX();
			}

			if ((turnFromHorizontalBone) && (dominoBone.isDoublet())) { // от не дупля по горизонтали поворачиваем
																			// вертикально и ставим дупль
				xLine = previous.getX() - (dominoBone.getWidth() / 2) - GameParameters.OFFSET;
			}

			if ((turnFromHorizontalDuplet) && (!dominoBone.isDoublet())) { // от дупля по горизонтали поворачиваем
																			// вертикально
				xLine = previous.getX();
			}

			if ((prevVerticalBone) || (prevVerticalDuplet)) { // уже движемся по вертикали
				xLine = previous.getX() + (previous.getWidth() / 2) - (dominoBone.getWidth() / 2);
			}

			dominoBone.setWorkSide(dominoBone.getRight());
			putAtPosition(GameParameters.TOLEFT, dominoBone, xLine, yLine);
		}
		spaceDown -= dominoBone.getHeight();

	}

	public void addToLeft(DominoBone dominoBone) {
		if (spaceLeft > GameParameters.SPACELIMIT) {
			addRightToLeft(leftBone(), dominoBone); // справа налево
		} else {
			if (isRandomTurn) {
				if (spaceUp > GameParameters.SPACELIMIT) {
					addDownToUp(leftBone(), dominoBone); // снизу вверх
				} else {
					isTurnTopLeft = true;
					addLeftToRight(leftBone(), dominoBone); // слева направо
				}
			} else {
				if (spaceDown > GameParameters.SPACELIMIT) {
					addUpToDown(leftBone(), dominoBone); // сверху вниз
				} else {
					isTurnBottomLeft = true;
					addLeftToRight(leftBone(), dominoBone); // слева направо
				}
			}
		}
	}

	public void addToRight(DominoBone dominoBone) {
		if (spaceRight > GameParameters.SPACELIMIT) {
			addLeftToRight(rightBone(), dominoBone);
		} else {
			if (!isRandomTurn) {
				if (spaceUp > GameParameters.SPACELIMIT) {
					addDownToUp(rightBone(), dominoBone);
				} else {
					isTurnTopRight = true;
					addRightToLeft(rightBone(), dominoBone);
				}
			} else {
				if (spaceDown > GameParameters.SPACELIMIT) {
					addUpToDown(rightBone(), dominoBone);
				} else {
					isTurnBottomRight = true;
					addRightToLeft(rightBone(), dominoBone);
				}
			}
		}
	}

	@Override
	public void setTitle(String title) {
		this.setBorder(BorderFactory.createTitledBorder(null, title, TitledBorder.DEFAULT_JUSTIFICATION,
				TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 20), new Color(12, 11, 11)));
	}

	@Override
	protected void addToBones(DominoBone dominoBone) {
	}
}
