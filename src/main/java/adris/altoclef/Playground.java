package adris.altoclef;

import adris.altoclef.butler.WhisperChecker;
import adris.altoclef.tasks.ArrowMapTests.LastAttackTestTask;
import adris.altoclef.tasks.CraftGenericManuallyTask;
import adris.altoclef.tasks.Test.BaitTrapTest;
import adris.altoclef.tasks.defense.DefenseConstants;
import adris.altoclef.tasks.defense.SecurityShelterTask;
import adris.altoclef.tasks.construction.PlaceBlockNearbyTask;
import adris.altoclef.tasks.construction.PlaceSignTask;
import adris.altoclef.tasks.construction.PlaceStructureBlockTask;
import adris.altoclef.tasks.construction.compound.ConstructIronGolemTask;
import adris.altoclef.tasks.construction.compound.ConstructNetherPortalObsidianTask;
import adris.altoclef.tasks.container.SmeltInFurnaceTask;
import adris.altoclef.tasks.container.StoreInAnyContainerTask;
import adris.altoclef.tasks.defense.TPAura;
import adris.altoclef.tasks.defense.chess.Queen;
import adris.altoclef.tasks.entity.KillEntityTask;
import adris.altoclef.tasks.examples.ExampleTask2;
import adris.altoclef.tasks.misc.EquipArmorTask;
import adris.altoclef.tasks.misc.PlaceBedAndSetSpawnTask;
import adris.altoclef.tasks.misc.RavageDesertTemplesTask;
import adris.altoclef.tasks.misc.RavageRuinedPortalsTask;
import adris.altoclef.tasks.movement.*;
import adris.altoclef.tasks.resources.CollectBlazeRodsTask;
import adris.altoclef.tasks.resources.CollectFlintTask;
import adris.altoclef.tasks.resources.CollectFoodTask;
import adris.altoclef.tasks.resources.TradeWithPiglinsTask;
import adris.altoclef.tasks.speedrun.KillEnderDragonTask;
import adris.altoclef.tasks.speedrun.KillEnderDragonWithBedsTask;
import adris.altoclef.tasks.speedrun.WaitForDragonAndPearlTask;
import adris.altoclef.tasks.stupid.BeeMovieTask;
import adris.altoclef.tasks.stupid.ReplaceBlocksTask;
import adris.altoclef.tasks.stupid.SCP173Task;
import adris.altoclef.tasks.stupid.TerminatorTask;
import adris.altoclef.util.*;
import adris.altoclef.util.helpers.BlockPosHelper;
import adris.altoclef.util.helpers.LookHelper;
import adris.altoclef.util.helpers.WorldHelper;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.SkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.chunk.EmptyChunk;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * For testing.
 * <p>
 * As solonovamax suggested, this stuff should REALLY be moved to unit tests
 * https://github.com/adrisj7-AltoClef/altoclef/pull/7#discussion_r641792377
 * but getting timed tests and testing worlds set up in Minecraft might be
 * challenging, so this is the temporary resting place for garbage test code for now.
 */
@SuppressWarnings("EnhancedSwitchMigration")
public class Playground {

    public static void IDLE_TEST_INIT_FUNCTION(AltoClef mod) {
        // Test code here

        // Print all uncatalogued resources as well as resources that don't have a corresponding item
        /*
        Set<String> collectable = new HashSet<>(TaskCatalogue.resourceNames());
        Set<String> allItems = new HashSet<>();

        List<String> notCollected = new ArrayList<>();

        for (Identifier id : Registry.ITEM.getIds()) {
            Item item = Registry.ITEM.get(id);
            String name = ItemUtil.trimItemName(item.getTranslationKey());
            allItems.add(name);
            if (!collectable.contains(name)) {
                notCollected.add(name);
            }
        }

        List<String> notAnItem = new ArrayList<>();
        for (String cataloguedName : collectable) {
            if (!allItems.contains(cataloguedName)) {
                notAnItem.add(cataloguedName);
            }
        }

        notCollected.sort(String::compareTo);
        notAnItem.sort(String::compareTo);

        Function<List<String>, String> temp = (list) -> {
            StringBuilder result = new StringBuilder("");
            for (String name : list) {
                result.append(name).append("\n");
            }
            return result.toString();
        };

        Debug.logInternal("NOT COLLECTED YET:\n" + temp.apply(notCollected));
        Debug.logInternal("\n\n\n");
        Debug.logInternal("NOT ITEMS:\n" + temp.apply(notAnItem));
        */

        /* Print all catalogued resources

        List<String> resources = new ArrayList<>(TaskCatalogue.resourceNames());
        resources.sort(String::compareTo);
        StringBuilder result = new StringBuilder("ALL RESOURCES:\n");
        for (String name : resources) {
            result.append(name).append("\n");
        }
        Debug.logInternal("We got em:\n" + result.toString());

         */
    }

    public static void IDLE_TEST_TICK_FUNCTION(AltoClef mod) {
        // Test code here
    }

    public static void TEMP_TEST_FUNCTION(AltoClef mod, String arg) {
        //mod.runUserTask();
        Debug.logMessage("Running test...");

        switch (arg) {
            case "":
                // None specified
                Debug.logWarning("Please specify a test (ex. stacked, bed, terminate)");
                break;
            case "idle":
                mod.runUserTask(new IdleTask());
                break;
            case "sign":
                mod.runUserTask(new PlaceSignTask("Hello there!"));
                break;
            case "sign2":
                mod.runUserTask(new PlaceSignTask(new BlockPos(10, 3, 10), "Hello there!"));
                break;
            case "pickup":
                mod.runUserTask(new PickupDroppedItemTask(new ItemTarget(Items.IRON_ORE, 3), true));
                break;
            case "chunk": {
                // We may have missed a chunk that's far away...
                BlockPos p = new BlockPos(100000, 3, 100000);
                Debug.logMessage("LOADED? " + (!(mod.getWorld().getChunk(p) instanceof EmptyChunk)));
                break;
            }
            case "structure":
                mod.runUserTask(new PlaceStructureBlockTask(new BlockPos(10, 6, 10)));
                break;
            case "place": {
                //BlockPos targetPos = new BlockPos(0, 6, 0);
                //mod.runUserTask(new PlaceSignTask(targetPos, "Hello"));
                //Direction direction = Direction.WEST;
                //mod.runUserTask(new InteractItemWithBlockTask(TaskCatalogue.getItemTarget("lava_bucket", 1), direction, targetPos, false));
                mod.runUserTask(new PlaceBlockNearbyTask(Blocks.CRAFTING_TABLE, Blocks.FURNACE));
                //mod.runUserTask(new PlaceStructureBlockTask(new BlockPos(472, 24, -324)));
                break;
            }
            case "deadmeme":
                File file = new File("test.txt");
                try {
                    FileReader reader = new FileReader(file);
                    mod.runUserTask(new BeeMovieTask("bruh", mod.getPlayer().getBlockPos(), reader));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            case "stacked":
                // It should only need:
                // 24 (armor) + 3*3 (pick) + 2 = 35 diamonds
                // 2*3 (pick) + 1 = 7 sticks
                // 4 planks
                /*
                mod.runUserTask(TaskCatalogue.getSquashedItemTask(
                        new ItemTarget("diamond_chestplate", 1),
                        new ItemTarget("diamond_leggings", 1),
                        new ItemTarget("diamond_helmet", 1),
                        new ItemTarget("diamond_boots", 1),
                        new ItemTarget("diamond_pickaxe", 3),
                        new ItemTarget("diamond_sword", 1),
                        new ItemTarget("crafting_table", 1)
                ));
                 */
                mod.runUserTask(new EquipArmorTask(Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_HELMET, Items.DIAMOND_BOOTS));
                break;
            case "stacked2":
                mod.runUserTask(new EquipArmorTask(Items.DIAMOND_CHESTPLATE));
                break;
            case "trap":
                mod.runUserTask(new BaitTrapTest());
                break;
            case "cast":
                BlockPos curr = mod.getPlayer().getBlockPos();
                BlockPos goal = curr.offset(Direction.NORTH, 10);
                System.out.println("curr: " + curr.toString());
                System.out.println("goal: " + goal.toString());
                Box box1 = mod.getPlayer().getBoundingBox();
                Box box2 = box1.offset(box1.getCenter().multiply(-1)).offset(curr);
                System.out.println("offset to: " + box2.getCenter().toString());
                Optional<Vec3d> vec = mod.getPlayer().getBoundingBox().raycast(BlockPosHelper.toVec3dCenter(curr), BlockPosHelper.toVec3dCenter(goal));//box2.raycast(BlockPosHelper.toVec3dCenter(curr), BlockPosHelper.toVec3dCenter(goal));

                if (vec.isEmpty()) {
                    System.out.println("vec empty");
                } else {
                    double dist = vec.get().distanceTo(BlockPosHelper.toVec3dCenter(goal));
                    System.out.println("dist: " + dist);
                    System.out.println("hit: " + vec.get().toString());
                }
                break;
            case "queen":
                Queen.attemptJump(mod, false);
                break;
            case "gqueen"://sample text
                Queen.attemptJump(mod, true);
                break;
            case "uvclip":
                TPAura.tp(mod, mod.getPlayer().getPos().add(0, 3, 0));
                break;
            case "dvclip":
                TPAura.tp(mod, mod.getPlayer().getPos().add(0, -3, 0));
                break;
            case "ravage":
                mod.runUserTask(new RavageRuinedPortalsTask());
                break;
            case "temples":
                mod.runUserTask(new RavageDesertTemplesTask());
                break;
            case "outer":
                mod.runUserTask(new GetToOuterEndIslandsTask());
                break;
            case "ingr1":
                System.out.println(mod.getItemStorage().isFullyCapableToCraft(mod, Items.IRON_CHESTPLATE));
                break;
            case "chorus":
                TPAura.chorusTp(mod, true);
                break;
            case "box":
                System.out.println(mod.getPlayer().getBoundingBox().toString());
                System.out.println("offset to zero: " + mod.getPlayer().getBoundingBox().offset(mod.getPlayer().getPos().multiply(-1)));
                System.out.println("and then back to place pos: " + mod.getPlayer().getBoundingBox().offset(mod.getPlayer().getPos().multiply(-1)).offset(mod.getPlayer().getPos()));

                break;
            case "pempty":
                final Box box = mod.getPlayer().getBoundingBox();
                final Box newBox = new Box(box.minX, box.minY - 3, box.minZ, box.maxX, box.maxY - 3, box.maxZ);
                System.out.println(mod.getWorld().isSpaceEmpty(newBox));
                break;
            case "res":
                System.out.println("start res:");
                /*MinecraftClient.getInstance().getResourceManager().getAllNamespaces().forEach(e -> {
                    System.out.println("res: " + e);
                });
                Identifier identifier = new Identifier("minecraft:crafting_shaped");
                try {
                    MinecraftClient.getInstance().getResourceManager().getResourceOrThrow(identifier);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }*/
/*
                InputStream stream = e.getInputStream();
                System.out.println("resource: ");
                StringBuilder textBuilder = new StringBuilder();
                try (Reader reader = new BufferedReader(new InputStreamReader
                        (stream, Charset.forName(StandardCharsets.UTF_8.name())))) {
                    int c = 0;
                    while ((c = reader.read()) != -1) {
                        textBuilder.append((char) c);
                    }
                }
                System.out.println(textBuilder.toString());*/
                /*JSONParser jsonParser = new JSONParser();

                try (FileReader reader = new FileReader("resources/yellow_wool.json"))
                {
                    //Read JSON file
                    Object obj = null;
                    try {
                        obj = jsonParser.parse(reader);
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }

                    JSONArray employeeList = (JSONArray) obj;
                    System.out.println(employeeList);

                    //Iterate over employee array
                    //employeeList.forEach( emp -> parseEmployeeObject( (JSONObject) emp ) );

                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/

                String inputFilePath = "recipes/yellow_wool.json" ;
                ClassLoader classLoader = Playground.class.getClassLoader();
                URL resource = classLoader.getResource(inputFilePath);
                // File path is passed as parameter
                File f2 = new File(resource.getFile());
                if (f2.exists()){
                    InputStream is;
                    try {
                        is = new FileInputStream(f2);
                        String jsonTxt = IOUtils.toString(is, "UTF-8");
                        System.out.println(jsonTxt);
                        //JSONObject json = new JSONObject(jsonTxt);
                        //String a = json.getString("1000");
                    } catch (FileNotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    //System.out.println(a);
                }
                System.out.println("end res:");

                break;
            case "ingr2":
                System.out.println(mod.getItemStorage().isFullyCapableToCraft(mod, Items.BREAD));
                break;
            case "ingr3":
                System.out.println(mod.getItemStorage().isFullyCapableToCraft(mod, Items.GOLDEN_AXE));
                break;
            case "sec":
                SecurityShelterTask.attemptShelter(mod);
                break;
            case "smelt":
                ItemTarget target = new ItemTarget("iron_ingot", 4);
                ItemTarget material = new ItemTarget("iron_ore", 4);
                mod.runUserTask(new SmeltInFurnaceTask(new SmeltTarget(target, material)));
                break;
            case "iron":
                mod.runUserTask(new ConstructIronGolemTask());
                break;
            case "tp1":
                Vec3d v = mod.getPlayer().getPos().add(1,1,0);
                mod.getPlayer().setPos(v.x, v.y, v.z);
                break;
            case "tp2":
                Vec3d v2 = mod.getPlayer().getPos().add(2,2,0);
                mod.getPlayer().setPos(v2.x, v2.y, v2.z);
                break;
            case "tp3":
                Vec3d v3 = mod.getPlayer().getPos().add(3,3,0);
                mod.getPlayer().setPos(v3.x, v3.y, v3.z);
                break;
            case "tp4":
                Vec3d v4 = mod.getPlayer().getPos().add(4,4,0);
                mod.getPlayer().setPos(v4.x, v4.y, v4.z);
                break;
            case "tp5":
                Vec3d v5 = mod.getPlayer().getPos().add(5,5,0);
                mod.getPlayer().setPos(v5.x, v5.y, v5.z);
                break;
            case "avoid":
                // Test block break predicate
                mod.getBehaviour().avoidBlockBreaking((BlockPos b) -> (-1000 < b.getX() && b.getX() < 1000)
                        && (-1000 < b.getY() && b.getY() < 1000)
                        && (-1000 < b.getZ() && b.getZ() < 1000));
                Debug.logMessage("Testing avoid from -1000, -1000, -1000 to 1000, 1000, 1000");
                break;
            case "portal":
                //mod.runUserTask(new EnterNetherPortalTask(new ConstructNetherPortalBucketTask(), Dimension.NETHER));
                mod.runUserTask(new EnterNetherPortalTask(new ConstructNetherPortalObsidianTask(), WorldHelper.getCurrentDimension() == Dimension.OVERWORLD ? Dimension.NETHER : Dimension.OVERWORLD));
                break;
            case "skel": mod.runUserTask(new LastAttackTestTask()); break;
            case "sees":
                //List<Entity> skels = mod.getEntityTracker().getHostiles().stream().filter(e -> e instanceof SkeletonEntity).collect(Collectors.toList());
                List<SkeletonEntity> skels = mod.getEntityTracker().getTrackedEntities(SkeletonEntity.class);
                if (skels.size() > 0) {
                    Entity skel = skels.get(0);
                    System.out.println(LookHelper.seesPlayer(skel, mod.getPlayer(), 16));
                }

                break;
            case "tracked":
                System.out.println("hostiles: " + mod.getEntityTracker().getHostiles().size());
                System.out.println("close: " + mod.getEntityTracker().getCloseEntities().size());
                System.out.println("close hostiles: " + mod.getEntityTracker().getCloseEntities().stream().filter(e -> e instanceof HostileEntity).collect(Collectors.toList()).size());
                System.out.println("close hostiles that we see: " + mod.getEntityTracker().getCloseEntities().stream().filter(e -> e instanceof HostileEntity && LookHelper.playerSeesEntity(e, mod.getPlayer(), DefenseConstants.PUNCH_RADIUS)).collect(Collectors.toList()).size());

                break;
            case "kill":
                List<ZombieEntity> zombs = mod.getEntityTracker().getTrackedEntities(ZombieEntity.class);
                if (zombs.size() == 0) {
                    Debug.logWarning("No zombs found.");
                } else {
                    LivingEntity entity = zombs.get(0);
                    mod.runUserTask(new KillEntityTask(entity));
                }
                break;
            case "craft":
                // Test de-equip
                new Thread(() -> {
                    for (int i = 3; i > 0; --i) {
                        Debug.logMessage(i + "...");
                        sleepSec(1);
                    }

                    Item[] c = new Item[]{Items.COBBLESTONE};
                    Item[] s = new Item[]{Items.STICK};
                    CraftingRecipe recipe = CraftingRecipe.newShapedRecipe("test pickaxe", new Item[][]{c, c, c, null, s, null, null, s, null}, 1);

                    mod.runUserTask(new CraftGenericManuallyTask(new RecipeTarget(Items.STONE_PICKAXE, 1, recipe)));
                    /*
                    Item toEquip = Items.BUCKET;//Items.AIR;
                    Slot target = PlayerInventorySlot.getEquipSlot(EquipmentSlot.MAINHAND);

                    InventoryTracker t = mod.getItemStorage();

                    // Already equipped
                    if (t.getItemStackInSlot(target).getItem() == toEquip) {
                        Debug.logMessage("Already equipped.");
                    } else {
                        List<Integer> itemSlots = t.getInventorySlotsWithItem(toEquip);
                        if (itemSlots.size() != 0) {
                            int slot = itemSlots.get(0);
                            t.swapItems(Slot.getFromInventory(slot), target);
                            Debug.logMessage("Equipped via swap");
                        } else {
                            Debug.logWarning("Failed to equip item " + toEquip.getTranslationKey());
                        }
                    }
                     */
                }).start();
                //mod.getItemStorage().equipItem(Items.AIR);
                break;
            case "food":
                mod.runUserTask(new CollectFoodTask(20));
                break;
            case "temple":
                mod.runUserTask(new LocateDesertTempleTask());
                break;
            case "blaze":
                mod.runUserTask(new CollectBlazeRodsTask(7));
                break;
            case "flint":
                mod.runUserTask(new CollectFlintTask(5));
                break;
            case "unobtainable":
                String fname = "unobtainables.txt";
                try {
                    int unobtainable = 0;
                    int total = 0;
                    File f = new File(fname);
                    FileWriter fw = new FileWriter(f);
                    for (Identifier id : Registry.ITEM.getIds()) {
                        Item item = Registry.ITEM.get(id);
                        if (!TaskCatalogue.isObtainable(item)) {
                            ++unobtainable;
                            fw.write(item.getTranslationKey() + "\n");
                        }
                        total++;
                    }
                    fw.flush();
                    fw.close();
                    Debug.logMessage(unobtainable + " / " + total + " unobtainable items. Wrote a list of items to \"" + f.getAbsolutePath() + "\".");
                } catch (IOException e) {
                    Debug.logWarning(e.toString());
                }
                break;
            case "piglin":
                mod.runUserTask(new TradeWithPiglinsTask(32, new ItemTarget(Items.ENDER_PEARL, 12)));
                break;
            case "stronghold":
                mod.runUserTask(new GoToStrongholdPortalTask(12));
                break;
            case "terminate":
                mod.runUserTask(new TerminatorTask(mod.getPlayer().getBlockPos(), 900));
                break;
            case "replace":
                // Creates a mini valley of crafting tables.
                BlockPos from = mod.getPlayer().getBlockPos().add(new Vec3i(-100, -20, -100));
                BlockPos to = mod.getPlayer().getBlockPos().add(new Vec3i(100, 255, 100));
                Block[] toFind = new Block[]{Blocks.GRASS_BLOCK};// Blocks.COBBLESTONE};
                ItemTarget toReplace = new ItemTarget("crafting_table");//"stone");
                mod.runUserTask(new ReplaceBlocksTask(toReplace, from, to, toFind));
                break;
            case "bed":
                mod.runUserTask(new PlaceBedAndSetSpawnTask());
                break;
            case "dragon":
                mod.runUserTask(new KillEnderDragonWithBedsTask(new WaitForDragonAndPearlTask()));
                break;
            case "dragon-pearl":
                mod.runUserTask(new ThrowEnderPearlSimpleProjectileTask(new BlockPos(0, 60, 0)));
                break;
            case "dragon-old":
                mod.runUserTask(new KillEnderDragonTask());
                break;
            case "chest":
                mod.runUserTask(new StoreInAnyContainerTask(true, new ItemTarget(Items.DIAMOND, 3)));
                break;
            case "173":
                mod.runUserTask(new SCP173Task());
                break;
            case "example":
                mod.runUserTask(new ExampleTask2());
                break;
            case "netherite":
                mod.runUserTask(TaskCatalogue.getSquashedItemTask(
                        new ItemTarget("netherite_pickaxe", 1),
                        new ItemTarget("netherite_sword", 1),
                        new ItemTarget("netherite_helmet", 1),
                        new ItemTarget("netherite_chestplate", 1),
                        new ItemTarget("netherite_leggings", 1),
                        new ItemTarget("netherite_boots", 1)));
                break;
            case "whisper": {
                File check = new File("whisper.txt");
                try {
                    FileInputStream fis = new FileInputStream(check);
                    Scanner sc = new Scanner(fis);
                    String me = sc.nextLine(),
                            template = sc.nextLine(),
                            message = sc.nextLine();
                    WhisperChecker.MessageResult result = WhisperChecker.tryParse(me, template, message);
                    Debug.logMessage("Got message: " + result);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            }
            default:
                mod.logWarning("Test not found: \"" + arg + "\".");
                break;
        }
    }

    private static void sleepSec(double seconds) {
        try {
            Thread.sleep((int) (1000 * seconds));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


}
