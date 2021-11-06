package me.bigteddy98.bannerboard;

import me.bigteddy98.bannerboard.api.BannerBoardRenderer;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BannerBoard {
	private final int id;
	private final List<Location> locationList;
	private final BlockFace face;
	private final int width;
	private final int height;
	private final int rotation;
	private int slideDelay = 0;

	private final List<List<BannerBoardRenderer<?>>> internalRenderers = new ArrayList<>();

	public BannerBoard(int id, List<Location> locationList, BlockFace face, int width, int height, int rotation) {
		this.id = id;
		this.locationList = locationList;
		this.face = face;
		this.width = width;
		this.height = height;
		this.rotation = rotation;
	}

	public List<Location> getLocationList() {
		return locationList;
	}

	public int getId() {
		return id;
	}

	public BlockFace getFace() {
		return face;
	}

	public Collection<? extends BannerBoardRenderer<?>> getReadOnlyRenderers(int slide) {
		synchronized (this.internalRenderers) {
			return new ArrayList<>(this.internalRenderers.get(slide));
		}
	}

	public void addTopRenderer(int slide, BannerBoardRenderer<?> renderer) {
		synchronized (this.internalRenderers) {
			if (this.internalRenderers.size() <= slide) {
				this.internalRenderers.add(new ArrayList<>());
			}
			this.internalRenderers.get(slide).add(renderer);
		}
	}

	public int getWidth() {
		return this.width;
	}

	public int getHeight() {
		return this.height;
	}

	public int getPixelWidth() {
		return this.getWidth() * 128;
	}

	public int getPixelHeight() {
		return this.getHeight() * 128;
	}

	public BufferedImage getImage(Player p, Map<Integer, Object> prep, int slide) {

		final int pixelWidth = this.getPixelWidthWithRotation();
		final int pixelHeight = this.getPixelHeightWithRotation();

		BufferedImage tmp = new BufferedImage(pixelWidth, pixelHeight, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = tmp.createGraphics();

		try {
			for (BannerBoardRenderer<?> renderer : this.getReadOnlyRenderers(slide)) {
				if (renderer.hasSetting("permission")) {
					String permission = renderer.getSetting("permission").getValue();
					if (!p.hasPermission(permission)) {
						continue;
					}
				}

				long start = System.currentTimeMillis();
				renderer.render(p, tmp, g);

				Object renderPrep = null;
				if (prep.containsKey(renderer.getId())) {
					renderPrep = prep.get(renderer.getId());
				}
				((BannerBoardRenderer<Object>) renderer).render(p, tmp, g, renderPrep);

				long took = System.currentTimeMillis() - start;

				if (took >= 3000) {
					Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[WARNING] [BannerBoard] Took " + (took / 1000D) + " second(s) to render " + renderer.getClass().getSimpleName() + ", the server might be under heavy load, or did the system time change?");
				}
			}

			int rotateTimes = 0;
			if (this.rotation == -90)
				rotateTimes = 3;
			if (this.rotation == 90)
				rotateTimes = 1;
			if (this.rotation == 180)
				rotateTimes = 2;

			for (int i = 0; i < rotateTimes; i++)
				tmp = rotate(tmp);

			return tmp;
		} finally {
			g.dispose();
		}
	}

	private static BufferedImage rotate(BufferedImage in) {
		BufferedImage res = new BufferedImage(in.getHeight(), in.getWidth(), BufferedImage.TYPE_4BYTE_ABGR);
		final byte[] inPix = ((DataBufferByte) in.getRaster().getDataBuffer()).getData();
		final byte[] resPix = ((DataBufferByte) res.getRaster().getDataBuffer()).getData();

		int pointer = 0;
		for (int y = 0; y < in.getHeight(); y++) {
			for (int x = 0; x < in.getWidth(); x++) {
				int destPos = (((x * res.getWidth()) + ((in.getHeight() - 1) - y)) * 4);
				resPix[destPos++] = inPix[pointer++];
				resPix[destPos++] = inPix[pointer++];
				resPix[destPos++] = inPix[pointer++];
				resPix[destPos++] = inPix[pointer++];
			}
		}

		return res;
	}

	private int slides;

	public void setSlideDelay(int slideDelay) {
		this.slideDelay = slideDelay;
	}

	public int getSlideDelay() {
		return slideDelay;
	}

	public void setSlides(int slides) {
		this.slides = slides;
	}

	public int getSlides() {
		if (this.slides == 0) {
			throw new RuntimeException("getSlides() called before setSlides(), not allowed.");
		}
		return this.slides;
	}


	public int getRotation() {
		return this.rotation;
	}

	public int getPixelWidthWithRotation() {
		if (this.rotation == -90 || this.rotation == 90)
			return this.getPixelHeight();
		return this.getPixelWidth();
	}

	public int getPixelHeightWithRotation() {
		if (this.rotation == -90 || this.rotation == 90)
			return this.getPixelWidth();
		return this.getPixelHeight();
	}
}
