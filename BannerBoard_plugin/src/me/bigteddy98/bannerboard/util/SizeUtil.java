package me.bigteddy98.bannerboard.util;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;

import java.util.ArrayList;
import java.util.List;

public class SizeUtil {

	public static int getWidth(List<Location> list) {
		int smallestX = Integer.MAX_VALUE;
		int biggestX = -Integer.MAX_VALUE;

		int smallestZ = Integer.MAX_VALUE;
		int biggestZ = -Integer.MAX_VALUE;

		for (Location loc : list) {
			if (loc.getBlockX() < smallestX) {
				smallestX = loc.getBlockX();
			}
			if (loc.getBlockX() > biggestX) {
				biggestX = loc.getBlockX();
			}
			if (loc.getBlockZ() < smallestZ) {
				smallestZ = loc.getBlockZ();
			}
			if (loc.getBlockZ() > biggestZ) {
				biggestZ = loc.getBlockZ();
			}
		}

		int width = -1;

		if (smallestZ == biggestZ) {
			width = Math.abs(smallestX - biggestX) + 1;
		}
		if (smallestX == biggestX) {
			width = Math.abs(smallestZ - biggestZ) + 1;
		}
		return width;
	}

	public static int getHeight(List<Location> list) {
		int smallestY = Integer.MAX_VALUE;
		int biggestY = -Integer.MAX_VALUE;

		for (Location loc : list) {
			if (loc.getBlockY() < smallestY) {
				smallestY = loc.getBlockY();
			}
			if (loc.getBlockY() > biggestY) {
				biggestY = loc.getBlockY();
			}
		}
		return Math.abs(smallestY - biggestY) + 1;
	}

	public static int highestX(List<Location> locs) {
		int highest = -Integer.MAX_VALUE;

		for (Location loc : locs) {
			if (loc.getBlockX() > highest) {
				highest = loc.getBlockX();
			}
		}
		return highest;
	}

	public static int highestY(List<Location> locs) {
		int highest = -Integer.MAX_VALUE;

		for (Location loc : locs) {
			if (loc.getBlockY() > highest) {
				highest = loc.getBlockY();
			}
		}
		return highest;
	}

	public static int highestZ(List<Location> locs) {
		int highest = -Integer.MAX_VALUE;

		for (Location loc : locs) {
			if (loc.getBlockZ() > highest) {
				highest = loc.getBlockZ();
			}
		}
		return highest;
	}

	public static int lowestX(List<Location> locs) {
		int lowest = Integer.MAX_VALUE;

		for (Location loc : locs) {
			if (loc.getBlockX() < lowest) {
				lowest = loc.getBlockX();
			}
		}
		return lowest;
	}

	public static int lowestY(List<Location> locs) {
		int lowest = Integer.MAX_VALUE;

		for (Location loc : locs) {
			if (loc.getBlockY() < lowest) {
				lowest = loc.getBlockY();
			}
		}
		return lowest;
	}

	public static int lowestZ(List<Location> locs) {
		int lowest = Integer.MAX_VALUE;

		for (Location loc : locs) {
			if (loc.getBlockZ() < lowest) {
				lowest = loc.getBlockZ();
			}
		}
		return lowest;
	}
}
