package dev.codex.schematicbuilder;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/** Places simple, reachable blocks from the currently selected Litematica placement. */
final class BuildController {
    private static final int MAX_SCAN_BLOCKS = 200_000;
    private static final double REACH = 4.5;
    private static final int ACTION_INTERVAL_TICKS = 4;

    private static final BuildController INSTANCE = new BuildController();

    private final List<BlockPos> pending = new ArrayList<>();
    private Mode mode = Mode.IDLE;
    private String status = "Load and select a Litematica placement";
    private int skippedUnsupported;
    private int actionDelay;
    private int originalHotbarSlot = -1;

    private BuildController() {}

    static BuildController get() {
        return INSTANCE;
    }

    String status() {
        return this.status;
    }

    void toggle(Minecraft client) {
        if (this.mode == Mode.RUNNING) {
            this.restoreHotbar(client);
            this.mode = Mode.PAUSED;
            this.status = "Paused • " + this.pending.size() + " blocks left";
        } else if (this.mode == Mode.PAUSED) {
            this.mode = Mode.RUNNING;
            this.status = "Building • " + this.pending.size() + " blocks left";
        } else {
            this.start(client);
        }
    }

    void stop(Minecraft client) {
        this.restoreHotbar(client);
        this.mode = Mode.IDLE;
        this.pending.clear();
        this.status = "Stopped safely";
    }

    void tick(Minecraft client) {
        if (this.mode != Mode.RUNNING) {
            return;
        }
        if (client.player == null || client.level == null || client.gameMode == null) {
            this.stop(client);
            this.status = "Stopped because the world was closed";
            return;
        }
        if (client.screen != null && !(client.screen instanceof BuilderScreen)) {
            return;
        }
        if (--this.actionDelay > 0) {
            return;
        }
        this.actionDelay = ACTION_INTERVAL_TICKS;

        for (int i = 0; i < this.pending.size(); i++) {
            BlockPos target = this.pending.get(i);
            BlockState wanted = LitematicaBridge.blockAt(target);
            if (wanted == null || wanted.isAir() || client.level.getBlockState(target).equals(wanted)) {
                this.pending.remove(i--);
                continue;
            }

            if (!wanted.equals(wanted.getBlock().defaultBlockState())) {
                this.pending.remove(i--);
                this.skippedUnsupported++;
                continue;
            }
            if (!client.level.getBlockState(target).canBeReplaced()) {
                this.pending.remove(i--);
                this.skippedUnsupported++;
                continue;
            }

            int slot = this.findHotbarSlot(client, wanted);
            if (slot < 0) {
                this.status = "Need " + wanted.getBlock().getName().getString() + " in your hotbar";
                continue;
            }

            BlockHitResult hit = this.findSupport(client, target);
            if (hit == null) {
                continue;
            }

            if (this.originalHotbarSlot < 0) {
                this.originalHotbarSlot = client.player.getInventory().getSelectedSlot();
            }
            client.player.getInventory().setSelectedSlot(slot);
            this.lookAt(client, hit.getLocation());
            client.gameMode.useItemOn(client.player, InteractionHand.MAIN_HAND, hit);
            client.player.swing(InteractionHand.MAIN_HAND);
            this.status = "Building • " + this.pending.size() + " blocks left";
            return;
        }

        if (this.pending.isEmpty()) {
            this.finish(client);
        } else {
            this.status = "Waiting for a reachable spot • " + this.pending.size() + " blocks left";
        }
    }

    private void start(Minecraft client) {
        if (client.player == null || client.level == null) {
            this.status = "Join a world first";
            return;
        }
        try {
            List<BlockPos> targets = LitematicaBridge.findTargets(client);
            if (targets == null) {
                this.status = "Open Litematica and select a placement first";
                return;
            }
            this.pending.clear();
            this.pending.addAll(targets);
            this.pending.sort(Comparator.comparingInt(BlockPos::getY));
            this.skippedUnsupported = 0;
            this.mode = Mode.RUNNING;
            this.actionDelay = 0;
            this.status = this.pending.isEmpty() ? "Nothing to place nearby" : "Building • " + this.pending.size() + " blocks left";
            if (this.pending.isEmpty()) {
                this.mode = Mode.IDLE;
            }
        } catch (ReflectiveOperationException exception) {
            this.mode = Mode.IDLE;
            this.status = "Could not read Litematica placement";
        } catch (RuntimeException exception) {
            this.mode = Mode.IDLE;
            this.status = exception.getMessage() == null ? "Could not start this schematic" : exception.getMessage();
        }
    }

    private int findHotbarSlot(Minecraft client, BlockState wanted) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = client.player.getInventory().getItem(i);
            if (stack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() == wanted.getBlock()) {
                return i;
            }
        }
        return -1;
    }

    private BlockHitResult findSupport(Minecraft client, BlockPos target) {
        Vec3 eye = client.player.getEyePosition();
        for (Direction side : Direction.values()) {
            BlockPos support = target.relative(side);
            Direction face = side.getOpposite();
            Vec3 hitPoint = Vec3.atCenterOf(support).add(
                    face.getStepX() * 0.5,
                    face.getStepY() * 0.5,
                    face.getStepZ() * 0.5);
            if (eye.distanceToSqr(hitPoint) > REACH * REACH) {
                continue;
            }
            BlockState supportState = client.level.getBlockState(support);
            if (!supportState.isAir() && supportState.isFaceSturdy(client.level, support, side.getOpposite())) {
                return new BlockHitResult(hitPoint, side.getOpposite(), support, false);
            }
        }
        return null;
    }

    private void lookAt(Minecraft client, Vec3 point) {
        Vec3 delta = point.subtract(client.player.getEyePosition());
        double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
        client.player.setYRot((float) (Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0));
        client.player.setXRot((float) -Math.toDegrees(Math.atan2(delta.y, horizontal)));
    }

    private void finish(Minecraft client) {
        this.restoreHotbar(client);
        this.mode = Mode.IDLE;
        this.status = this.skippedUnsupported == 0
                ? "Build complete ♡"
                : "Done • skipped " + this.skippedUnsupported + " complex or blocked blocks";
    }

    private void restoreHotbar(Minecraft client) {
        if (this.originalHotbarSlot >= 0 && client.player != null) {
            client.player.getInventory().setSelectedSlot(this.originalHotbarSlot);
        }
        this.originalHotbarSlot = -1;
    }

    private enum Mode { IDLE, RUNNING, PAUSED }

    /** Uses reflection so this client can still launch without Litematica installed. */
    private static final class LitematicaBridge {
        private static final String DATA_MANAGER = "fi.dy.masa.litematica.data.DataManager";
        private static final String WORLD_HANDLER = "fi.dy.masa.litematica.world.SchematicWorldHandler";

        static List<BlockPos> findTargets(Minecraft client) throws ReflectiveOperationException {
            Class<?> dataManager = Class.forName(DATA_MANAGER);
            Object placementManager = dataManager.getMethod("getSchematicPlacementManager").invoke(null);
            Object placement = placementManager.getClass().getMethod("getSelectedSchematicPlacement").invoke(placementManager);
            if (placement == null) {
                return null;
            }
            if (!(boolean) placement.getClass().getMethod("isEnabled").invoke(placement)) {
                throw new IllegalStateException("Enable the selected placement first");
            }
            Object rotation = placement.getClass().getMethod("getRotation").invoke(placement);
            Object mirror = placement.getClass().getMethod("getMirror").invoke(placement);
            if (!"NONE".equals(rotation.toString()) || !"NONE".equals(mirror.toString())) {
                throw new IllegalStateException("For now, use an unrotated, unmirrored placement");
            }

            Object schematic = placement.getClass().getMethod("getSchematic").invoke(placement);
            BlockPos origin = (BlockPos) placement.getClass().getMethod("getOrigin").invoke(placement);
            Object size = schematic.getClass().getMethod("getTotalSize").invoke(schematic);
            int sizeX = Math.abs((int) size.getClass().getMethod("getX").invoke(size));
            int sizeY = Math.abs((int) size.getClass().getMethod("getY").invoke(size));
            int sizeZ = Math.abs((int) size.getClass().getMethod("getZ").invoke(size));
            long volume = (long) sizeX * sizeY * sizeZ;
            if (volume <= 0 || volume > MAX_SCAN_BLOCKS) {
                throw new IllegalStateException("Schematic is empty or too large to scan safely");
            }

            Class<?> worldHandler = Class.forName(WORLD_HANDLER);
            Object schematicWorld = worldHandler.getMethod("getSchematicWorld").invoke(null);
            if (schematicWorld == null) {
                return List.of();
            }
            Method getBlockState = schematicWorld.getClass().getMethod("getBlockState", BlockPos.class);

            List<BlockPos> targets = new ArrayList<>();
            for (int y = 0; y < sizeY; y++) {
                for (int z = 0; z < sizeZ; z++) {
                    for (int x = 0; x < sizeX; x++) {
                        BlockPos pos = origin.offset(x, y, z);
                        BlockState wanted = (BlockState) getBlockState.invoke(schematicWorld, pos);
                        if (wanted != null && !wanted.isAir() && !client.level.getBlockState(pos).equals(wanted)) {
                            targets.add(pos);
                        }
                    }
                }
            }
            return targets;
        }

        static BlockState blockAt(BlockPos pos) {
            try {
                Class<?> worldHandler = Class.forName(WORLD_HANDLER);
                Object schematicWorld = worldHandler.getMethod("getSchematicWorld").invoke(null);
                if (schematicWorld == null) {
                    return null;
                }
                return (BlockState) schematicWorld.getClass().getMethod("getBlockState", BlockPos.class).invoke(schematicWorld, pos);
            } catch (ReflectiveOperationException exception) {
                return null;
            }
        }
    }
}
