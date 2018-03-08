package lib_duke;

//LineMakerBresenham 
public class LineMaker {

	private ImageResource image;

	private boolean[] xPlus = new boolean[] { true, true, false, false, false, false, true, true };
	private boolean[] yPlus = new boolean[] { true, true, true, true, false, false, false, false };
	private boolean[] xMoreYAbs = new boolean[] { true, false, false, true, true, false, false, true };

	public LineMaker(ImageResource image) {
		this.image = image;
	}

	/**
	 * 
	 * @param dX
	 * @param dY
	 * @return
	 */
	private int determineSector(int dX, int dY) {
		// Edge cases
		if (dY == 0) {
			if (dX > 0)
				return 0;
			else
				return 4;
		} else if (dX == 0) {
			if (dY > 0)
				return 2;
			else
				return 6;
		} else if (dX == dY) {
			if (dX > 0)
				return 1;
			else
				return 5;
		} else if (dX == -dY) {
			if (dX < 0)
				return 3;
			else
				return 7;
		}

		// Else
		for (int sector = 0; sector < 8; sector++) {
			if ((dX > 0) == xPlus[sector] && (dY > 0) == yPlus[sector]
					&& (Math.abs(dX) > Math.abs(dY)) == xMoreYAbs[sector])
				return sector;
		}
		return -1;
	}

	private int xTransformed, yTransformed;

	/**
	 * 
	 * @param x
	 * @param y
	 * @param sector
	 */
	private void transform(int x, int y, int sector) {

		switch (sector) {
		case 0:
			xTransformed = x;
			yTransformed = y;
			break;
		case 1:
			xTransformed = y;
			yTransformed = x;
			break;
		case 2:
			xTransformed = y;
			yTransformed = -x;
			break;
		case 3:
			xTransformed = -x;
			yTransformed = y;
			break;
		case 4:
			xTransformed = -x;
			yTransformed = -y;
			break;
		case 5:
			xTransformed = -y;
			yTransformed = -x;
			break;
		case 6:
			xTransformed = -y;
			yTransformed = x;
			break;
		case 7:
			xTransformed = x;
			yTransformed = -y;
		}
	}

	private void transformBack(int x, int y, int sector) {

		switch (sector) {
		case 0:
			xTransformed = x;
			yTransformed = -y;
			break;
		case 1:
			xTransformed = y;
			yTransformed = -x;
			break;
		case 2:
			xTransformed = -y;
			yTransformed = -x;
			break;
		case 3:
			xTransformed = -x;
			yTransformed = -y;
			break;
		case 4:
			xTransformed = -x;
			yTransformed = y;
			break;
		case 5:
			xTransformed = -y;
			yTransformed = x;
			break;
		case 6:
			xTransformed = y;
			yTransformed = x;
			break;
		case 7:
			xTransformed = x;
			yTransformed = y;
		}
	}

	public void drawLine(int x1, int y1, int x2, int y2, int r, int g, int b) {

		int x, y, xT, yT, dx, dy, p, dxII, dyII;

		dx = x2 - x1;
		dy = -(y2 - y1);

		int sector = determineSector(dx, dy);

		int relativeZeroX = x1;
		int relativeZeroY = y1;

		x1 = 0;
		y1 = 0;

		x2 = x2 - relativeZeroX;
		y2 = -(y2 - relativeZeroY);

		transform(x2, y2, sector);
		x2 = xTransformed;
		y2 = yTransformed;

		dx = x2 - x1;
		dy = y2 - y1;

		y = y1;
		p = 2 * dy - dx;

		dxII = 2 * dx;
		dyII = 2 * dy;

		Pixel px;

		for (x = x1; x <= x2; x++) {

			transformBack(x, y, sector);
			xT = xTransformed;
			yT = yTransformed;
			px = image.getPixel(relativeZeroX + xT, relativeZeroY + yT);

			px.setRed(r);
			px.setGreen(g);
			px.setBlue(b);

			if (p >= 0) {
				y++;
				p = p + dyII - dxII;
			} else {
				p = p + dyII;
			}
		}
	}
}