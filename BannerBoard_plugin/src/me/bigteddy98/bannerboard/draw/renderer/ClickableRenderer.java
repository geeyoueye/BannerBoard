package me.bigteddy98.bannerboard.draw.renderer;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import me.bigteddy98.bannerboard.Main;
import me.bigteddy98.bannerboard.api.DisableBannerBoardException;
import me.bigteddy98.bannerboard.api.InteractHandler;
import me.bigteddy98.bannerboard.api.Setting;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;

public class ClickableRenderer extends InteractHandler<Void> {
	public ClickableRenderer(List<Setting> parameters, int allowedWidth, int allowedHeight) {
		super(parameters, allowedWidth, allowedHeight);

		// format INTERACT -x0 -y0 -x1 -y1 -message Hey you just clicked me! :)
		if (!this.hasSetting("x0")) {
			this.getSettings().add(new Setting("x0", "0"));
		}

		if (!this.hasSetting("x1")) {
			this.getSettings().add(new Setting("x1", allowedWidth + ""));
		}

		if (!this.hasSetting("y0")) {
			this.getSettings().add(new Setting("y0", "0"));
		}
		if (!this.hasSetting("y1")) {
			this.getSettings().add(new Setting("y1", allowedHeight + ""));
		}

		// verify them all
		check("x0");
		check("x1");
		check("y0");
		check("y1");
	}

	private void check(String setting) {
		try {
			Integer.parseInt(this.getSetting(setting).getValue());
		} catch (NumberFormatException e) {
			throw new DisableBannerBoardException("Renderer INTERACT did not have a valid " + setting + " parameter, " + this.getSetting(setting).getValue() + " is not a valid number");
		}
	}

	@Override
	public void handle(final Player clicker, double clickX, double clickY) {
		int x0 = Integer.parseInt(this.getSetting("x0").getValue());
		int y0 = Integer.parseInt(this.getSetting("y0").getValue());
		int x1 = Integer.parseInt(this.getSetting("x1").getValue());
		int y1 = Integer.parseInt(this.getSetting("y1").getValue());

		if (clickX >= x0 && clickX <= x1 && clickY >= y0 && clickY <= y1) {
			if (this.hasSetting("text")) {
				String text = this.getSetting("text").getValue();
				// start with the placeholders
				text = Main.getInstance().applyPlaceholders(text, clicker);
				text = ChatColor.translateAlternateColorCodes('&', text);

				clicker.sendMessage(text);
			}

			if (this.hasSetting("consolecommand")) {
				String command = this.getSetting("consolecommand").getValue();
				if (command.startsWith("/")) {
					command = command.substring(1);
				}
				// apply placeholders
				command = Main.getInstance().applyPlaceholders(command, clicker);
				Main.getInstance().getServer().dispatchCommand(Main.getInstance().getServer().getConsoleSender(), command);
				Bukkit.getLogger().info("Interact handler executed command [" + command + "] as console.");
			}

			if (this.hasSetting("playercommand")) {
				String command = this.getSetting("playercommand").getValue();
				if (command.startsWith("/")) {
					command = command.substring(1);
				}
				// apply placeholders
				command = Main.getInstance().applyPlaceholders(command, clicker);
				Main.getInstance().getServer().dispatchCommand(clicker, command);
				Bukkit.getLogger().info("Interact handler executed command [" + command + "] as player" + clicker.getName() + ".");
			}

			if (this.hasSetting("sendserver")) {
				final String server = this.getSetting("sendserver").getValue();
				
				Bukkit.getScheduler().runTask(Main.getInstance(), () -> {
					@SuppressWarnings("UnstableApiUsage")
					ByteArrayDataOutput out = ByteStreams.newDataOutput();
					out.writeUTF("Connect");
					out.writeUTF(server);
					clicker.sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
				});

				Bukkit.getLogger().info("Interact handler sent player " + clicker.getName() + " to server " + server + ".");
			}
		}
	}
}
