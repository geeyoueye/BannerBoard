package me.bigteddy98.bannerboard;

import com.interactiveboard.board.display.BoardLocation;
import com.interactiveboard.board.interaction.BoardInteraction;
import com.interactiveboard.board.rendering.RenderProperties;
import com.interactiveboard.board.rendering.scenes.BoardScene;
import com.interactiveboard.board.rendering.scenes.BoardSceneProvider;
import com.interactiveboard.data.board.BoardProperties;
import com.interactiveboard.data.player.PlayerBoardDataImplementation;
import me.bigteddy98.bannerboard.api.BannerBoardRenderer;
import me.bigteddy98.bannerboard.api.InteractHandler;
import me.bigteddy98.bannerboard.util.SizeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoardMemory {
    private final Main plugin;

    public BoardMemory(Main plugin) {
        this.plugin = plugin;
    }

    public void load(BannerBoard board) {
        if (!Bukkit.isPrimaryThread()) {
            throw new UnsupportedOperationException("Can only register new BannerBoard from main Bukkit thread");
        }
        if (board.getFace() == BlockFace.DOWN || board.getFace() == BlockFace.UP) {
            Bukkit.getLogger().severe("Can not load bannerboard board (" + board.getId() + ") vertical boards are not currently supported through InteractiveBoard");
            return;
        }

        BlockFace direction = board.getFace().getOppositeFace();
        List<Location> locations = board.getLocationList();
        World world = locations.get(0).getWorld();

        int minX = SizeUtil.lowestX(locations);
        int maxX = SizeUtil.highestX(locations);
        int minY = SizeUtil.lowestY(locations);
        int maxY = SizeUtil.highestY(locations);
        int minZ = SizeUtil.lowestZ(locations);
        int maxZ = SizeUtil.highestZ(locations);

        Location firstLocation = new Location(world, direction == BlockFace.NORTH ? maxX : minX, minY, direction == BlockFace.EAST ? maxZ : minZ);
        Location secondLocation = new Location(world, direction == BlockFace.NORTH ? minX : maxX, maxY, direction == BlockFace.EAST ? minZ : maxZ);

        BoardLocation boardLocation = new BoardLocation(firstLocation, secondLocation, direction, 15);
        BoardProperties boardProperties = new BoardProperties(false, false, false, false);
        RenderProperties renderProperties = new RenderProperties(true, false);

        BoardSceneProvider boardSceneProvider = new BoardSceneProvider() {
            @Override
            public String getDefaultSceneName(PlayerBoardDataImplementation playerBoardDataImplementation) {
                return "default";
            }

            @Override
            public BoardScene getSceneFromName(String s, PlayerBoardDataImplementation playerBoardDataImplementation) {
                return new BoardScene(playerBoardDataImplementation.getBoardData()) {
                    int currentSlide = 0;
                    int lastRenderedSlide = -1;
                    long lastSwitch = 0;

                    final byte[][] slideImages = new byte[board.getSlides()][];

                    @Override
                    public byte[] getImage() {
                        if(lastSwitch == 0) {
                            lastSwitch = System.currentTimeMillis();
                        }

                        if(board.getSlideDelay() > 0 && lastSwitch + board.getSlideDelay() * 1000L < System.currentTimeMillis()) {
                            currentSlide++;
                            if(currentSlide >= board.getSlides()) {
                                currentSlide = 0;
                            }
                            lastSwitch = System.currentTimeMillis();
                        }

                        if(lastRenderedSlide != currentSlide) {
                            if(slideImages[currentSlide] == null) {
                                Player player = playerBoardDataImplementation.getPlayerData().getPlayer();
                                final String name = player.getName();

                                final Map<Integer, Object> preps = new HashMap<>();

                                for (BannerBoardRenderer<?> s : board.getReadOnlyRenderers(currentSlide)) {
                                    Object prep = null;
                                    try {
                                        prep = s.asyncRenderPrepare(player);
                                    } catch (Throwable e) {
                                        Bukkit.getLogger().warning("Failed to do preperations for user " + name + ". " + e.getClass().getSimpleName() + " " + (e.getMessage()) + ".");
                                    }
                                    preps.put(s.getId(), prep);
                                }

                                slideImages[currentSlide] = plugin.interactiveBoard.getConverter().imageToBytes(board.getImage(player, preps, currentSlide), boardLocation.getPixelWidth(), boardLocation.getPixelHeight(), renderProperties);
                            }

                            lastRenderedSlide = currentSlide;

                            return slideImages[currentSlide];
                        }

                        return null;
                    }

                    @Override
                    public void interact(BoardInteraction interaction, double x, double y) {
                        if(interaction == BoardInteraction.CLICK) {
                            for (BannerBoardRenderer<?> renderer : board.getReadOnlyRenderers(currentSlide)) {
                                if (renderer instanceof InteractHandler) {
                                    ((InteractHandler<?>) renderer).handle(playerBoardDataImplementation.getPlayerData().getPlayer(), x, y);
                                }
                            }
                        }
                    }
                };
            }
        };

        plugin.interactiveBoard.getBoardDataProvider().createBoard(plugin, String.valueOf(board.getId()), boardLocation, boardProperties, renderProperties, boardSceneProvider);

        Bukkit.getLogger().info("Successfully loaded BannerBoard [" + board.getId() + "]");
    }
}
